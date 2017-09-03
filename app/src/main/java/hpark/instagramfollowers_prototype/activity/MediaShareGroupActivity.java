package hpark.instagramfollowers_prototype.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.Holder;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ImageItemAdapter;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.model.ImageItem;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */
public class MediaShareGroupActivity extends AppCompatActivity implements ImageItemAdapter.OnImageExpandListener {
    private final static String TAG = "MediaShareGroupActivity";

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter = null;
    RecyclerView.LayoutManager layoutManager;

    private InstaSession mSession;
    private String name;
    private String id;
    private ArrayList<String> idList;
    private ArrayList<HashMap<String, String>> storage = new ArrayList<>();
    private ArrayList<HashMap<String, String>> imagesInfo = new ArrayList<>();

    private ImageView memberImageView1;
    private ImageView memberImageView2;
    private ImageView memberImageView3;
    private ImageView memberImageView4;
    private ImageView memberImageView5;

    private TextView memberTextView1;
    private TextView memberTextView2;
    private TextView memberTextView3;
    private TextView memberTextView4;
    private TextView memberTextView5;

    private DatabaseManager databaseManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_group);
        idList = getIntent().getStringArrayListExtra("idList");
        name = getIntent().getStringExtra("shareGroupName");
        context = getApplicationContext();
        id = getIntent().getStringExtra("id");

        initViews();
        databaseManager = new DatabaseManager(this);

        verifyExternalStoragePermission();
    }

    private void initViews() {
        setTitle(name + "    (총 : " + idList.size() + "명)");

        memberImageView1 = (ImageView) findViewById(R.id.memberImageView1);
        memberImageView2 = (ImageView) findViewById(R.id.memberImageView2);
        memberImageView3 = (ImageView) findViewById(R.id.memberImageView3);
        memberImageView4 = (ImageView) findViewById(R.id.memberImageView4);
        memberImageView5 = (ImageView) findViewById(R.id.memberImageView5);

        memberTextView1 = (TextView) findViewById(R.id.memberTextView1);
        memberTextView2 = (TextView) findViewById(R.id.memberTextView2);
        memberTextView3 = (TextView) findViewById(R.id.memberTextView3);
        memberTextView4 = (TextView) findViewById(R.id.memberTextView4);
        memberTextView5 = (TextView) findViewById(R.id.memberTextView5);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setLayoutManager(layoutManager);
        mSession = new InstaSession(this);
        fetchAllRecentMediaPerUser();
        fetchAllGroupMembersImageUrl();
    }

    private String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    private void setUpdatedImageAdapter() {
        int likesUnicode = 0x1F49C;
        int commentUnicode = 0x1F4AC;

        String likes = getEmojiByUnicode(likesUnicode);
        String comments = getEmojiByUnicode(commentUnicode);

        ArrayList items = new ArrayList<>();

        imagesInfo = storage;
        for(HashMap<String, String> hashMap: imagesInfo) {
            items.add(new ImageItem(hashMap.get(Constants.TAG_IMAGE_STANDARD), likes + ": " + hashMap.get(Constants.TAG_LIKES)+"    " + comments + ": " + hashMap.get(Constants.TAG_COMMENTS)));
        }
        if (Adapter != null) {
            ((ImageItemAdapter)Adapter).updateDataSet(items);
        } else {
            Adapter = new ImageItemAdapter(this, items, R.layout.activity_share_group);
            recyclerView.setAdapter(Adapter);
        }
    }

    private Handler mediaRecvHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                setUpdatedImageAdapter();
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(MediaShareGroupActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    private void fetchAllRecentMediaPerUser() {
        final String url = "https://api.instagram.com/v1/users/{user-id}/media/recent/?access_token=" + mSession.getAccessToken() + "&COUNT=10";

        for (int i = 0; i < idList.size(); i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int what = Constants.WHAT_FINALIZE;
                    try {
                        HttpRequestManager httpRequestManager = new HttpRequestManager();
                        String reqUrl = url.replace("{user-id}", idList.get(index));
                        JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(reqUrl);
                        JSONArray data = jsonObject.getJSONArray(Constants.TAG_DATA);

                        for (int data_i = 0; data_i < data.length(); data_i++) {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            JSONObject data_obj = data.getJSONObject(data_i);
                            JSONObject images = data_obj.getJSONObject(Constants.TAG_IMAGES);
                            JSONObject standardImage = images.getJSONObject(Constants.TAG_IMAGE_STANDARD);
                            JSONObject user = data_obj.getJSONObject("user");
                            JSONObject comments = data_obj.getJSONObject(Constants.TAG_COMMENTS);
                            JSONObject likes = data_obj.getJSONObject(Constants.TAG_LIKES);

                            hashMap.put(Constants.TAG_IMAGE_STANDARD, standardImage.getString("url"));
                            hashMap.put(Constants.TAG_USERNAME, user.getString(Constants.TAG_USERNAME));
                            hashMap.put(Constants.TAG_CREATED_TIME, data_obj.getString(Constants.TAG_CREATED_TIME));
                            hashMap.put(Constants.TAG_COMMENTS, comments.getString("count"));
                            hashMap.put(Constants.TAG_LIKES, likes.getString("count"));

                            storage.add(hashMap);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        what = Constants.WHAT_ERROR;
                    }

                    mediaRecvHandler.sendMessage(mediaRecvHandler.obtainMessage(what, index + 1));
                }
            }).start();
        }
    }

    private Handler profileImageRecvHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                Transformation transformation = new RoundedTransformationBuilder().borderColor(Color.GRAY)
                        .borderWidthDp(1).cornerRadiusDp(30).oval(false).build();
                switch (msg.arg1) {
                    case 0: Picasso.with(getApplicationContext()).load((String)msg.obj).transform(transformation).into(memberImageView1);
                        break;
                    case 1: Picasso.with(getApplicationContext()).load((String)msg.obj).transform(transformation).into(memberImageView2);
                        break;
                    case 2: Picasso.with(getApplicationContext()).load((String)msg.obj).transform(transformation).into(memberImageView3);
                        break;
                    case 3: Picasso.with(getApplicationContext()).load((String)msg.obj).transform(transformation).into(memberImageView4);
                        break;
                    case 4: Picasso.with(getApplicationContext()).load((String)msg.obj).transform(transformation).into(memberImageView5);
                        break;
                    default: break;
                }
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(MediaShareGroupActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    private Handler usernameRecvHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                switch (msg.arg1) {
                    case 0: memberTextView1.setText((String)msg.obj); break;
                    case 1: memberTextView2.setText((String)msg.obj); break;
                    case 2: memberTextView3.setText((String)msg.obj); break;
                    case 3: memberTextView4.setText((String)msg.obj); break;
                    case 4: memberTextView5.setText((String)msg.obj); break;
                    default: break;
                }
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(MediaShareGroupActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    private void fetchAllGroupMembersImageUrl() {
        final String url = "https://api.instagram.com/v1/users/{user-id}/?access_token=" + mSession.getAccessToken();

        for (int i = 0; i < idList.size(); i++) {
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int what = Constants.WHAT_FINALIZE;
                    String urlString = "";
                    String username = "";
                    try {
                        HttpRequestManager httpRequestManager = new HttpRequestManager();
                        String reqUrl = url.replace("{user-id}", idList.get(index));
                        JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(reqUrl);
                        JSONObject data = jsonObject.getJSONObject(Constants.TAG_DATA);
                        urlString = data.getString(Constants.TAG_PROFILE_PICTURE);
                        username = data.getString(Constants.TAG_USERNAME);

                    } catch (Exception exception) {
                        exception.printStackTrace();
                        what = Constants.WHAT_ERROR;
                    }

                    profileImageRecvHandler.sendMessage(profileImageRecvHandler.obtainMessage(what, index, 0, urlString));
                    usernameRecvHandler.sendMessage(usernameRecvHandler.obtainMessage(what, index, 0, username));
                }
            }).start();
        }
    }

    private void deleteCurrentBookmark() {
        databaseManager.deleteShareGroupValues(DatabaseManager.colId+"="+ id, null);
        Toast.makeText(getApplicationContext(), "성공적으로 현재 북마킹이 제거되었습니다.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sharegroup_query_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu()");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");

        int id = item.getItemId();
        switch(id) {
            case R.id.deleteBookmark:
                deleteCurrentBookmark();
            case R.id.queryByNewest:
                QueryByNewest queryByNewest = new QueryByNewest();
                Collections.sort(imagesInfo, queryByNewest);
                break;
            case R.id.queryByLatest:
                QueryByLatest queryByLatest = new QueryByLatest();
                Collections.sort(imagesInfo, queryByLatest);
                break;
            case R.id.queryByLikes:
                QueryByLikes queryByLikes = new QueryByLikes();
                Collections.sort(imagesInfo, queryByLikes);
                break;
            case R.id.queryByComments:
                QueryByComments queryByComments = new QueryByComments();
                Collections.sort(imagesInfo, queryByComments);
                break;
            default:
                break;
        }
        setUpdatedImageAdapter();
        return super.onOptionsItemSelected(item);
    }

    class QueryByNewest implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
            int lhs = Integer.parseInt(o1.get(Constants.TAG_CREATED_TIME));
            int rhs = Integer.parseInt(o2.get(Constants.TAG_CREATED_TIME));
            return ((Integer)rhs).compareTo(lhs);
        }
    }

    class QueryByLatest implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
            int lhs = Integer.parseInt(o1.get(Constants.TAG_CREATED_TIME));
            int rhs = Integer.parseInt(o2.get(Constants.TAG_CREATED_TIME));
            return ((Integer)lhs).compareTo(rhs);
        }
    }

    class QueryByLikes implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
            int lhs = Integer.parseInt(o1.get(Constants.TAG_LIKES));
            int rhs = Integer.parseInt(o2.get(Constants.TAG_LIKES));
            return ((Integer)rhs).compareTo(lhs);
        }
    }

    class QueryByComments implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
            int lhs = Integer.parseInt(o1.get(Constants.TAG_COMMENTS));
            int rhs = Integer.parseInt(o2.get(Constants.TAG_COMMENTS));
            return ((Integer)rhs).compareTo(lhs);
        }
    }

    private String imageUrlForShare = "";

    private void selectWhereToShare() {
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "SomeText", null);
                Log.d("Path", path);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "Media Share");
                Uri screenshotUri = Uri.parse(path);
                intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                intent.setType("image/*");
                startActivity(Intent.createChooser(intent, "Share image via..."));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(getApplicationContext()).load(imageUrlForShare).into(target);

    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(DialogPlus dialog, View view) {
            switch (view.getId()) {
              case R.id.shareCancelButton: break;
              case R.id.shareConfirmButton:
                  selectWhereToShare();
                break;
            }
            dialog.dismiss();
        }
    };

    @Override
    public void onImageExpand(String imageUrl) {
        imageUrlForShare = imageUrl;
        Holder holder = new ImageShareViewHolder(R.layout.dialog_popup_image, imageUrl);

        final DialogPlus dialog = DialogPlus.newDialog(this)
                .setContentHolder(holder)
                .setHeader(R.layout.dialog_header)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .setOnClickListener(onClickListener)
                .create();
        dialog.show();
    }

    private class ImageShareViewHolder extends ViewHolder {
        String imageUrl = "";
        ImageView imageView;

        public ImageShareViewHolder(int viewResourceId, String imageUrl) {
            super(viewResourceId);
            this.imageUrl = imageUrl;
        }

        @Override
        public View getView(LayoutInflater inflater, ViewGroup parent) {
            View v = super.getView(inflater, parent);
            imageView = (ImageView) v.findViewById(R.id.expandedImageView);

            Picasso.with(context).load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
            return v;
        }
    }

    private static final int RECORD_REQUEST_CODE = 101;
    protected boolean isExternalStorageAllowed = false;

    private void showAskingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("").setTitle("");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeRequest();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verifyExternalStoragePermission() {
        int permission;
        if(Build.VERSION.SDK_INT >= 23) {
            permission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permission != PackageManager.PERMISSION_GRANTED) {
                if(this.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAskingPermissionDialog();
                } else makeRequest();
            } else isExternalStorageAllowed = true;
        } else {
            permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(permission != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAskingPermissionDialog();
                } else makeRequest();
            } else isExternalStorageAllowed = true;
        }
    }

    private void makeRequest() {
        if(Build.VERSION.SDK_INT >= 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RECORD_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RECORD_REQUEST_CODE: {
                if(grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isExternalStorageAllowed = true;
                }
            }
            break;
            default: break;
        }
    }
}
