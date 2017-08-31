package hpark.instagramfollowers_prototype.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

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

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */
public class MediaShareGroupActivity extends AppCompatActivity {
    private final static String TAG = "MediaShareGroupActivity";

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter = null;
    RecyclerView.LayoutManager layoutManager;

    private InstaSession mSession;
    private ArrayList<String> idList;
    private ArrayList<HashMap<String, String>> imagesInfo = new ArrayList<>();

    private TextView groupTitleTextView;
    private TextView numMemberTextView;

    private ImageView memberImageView1;
    private ImageView memberImageView2;
    private ImageView memberImageView3;
    private ImageView memberImageView4;
    private ImageView memberImageView5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_group);
        idList = getIntent().getStringArrayListExtra("idList");
        context = getApplicationContext();

        initViews();
    }

    private void initViews() {
        groupTitleTextView = (TextView) findViewById(R.id.groupTitleTextView);
        numMemberTextView = (TextView) findViewById(R.id.numMemberTextView);

        memberImageView1 = (ImageView) findViewById(R.id.memberImageView1);
        memberImageView2 = (ImageView) findViewById(R.id.memberImageView2);
        memberImageView3 = (ImageView) findViewById(R.id.memberImageView3);
        memberImageView4 = (ImageView) findViewById(R.id.memberImageView4);
        memberImageView5 = (ImageView) findViewById(R.id.memberImageView5);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setLayoutManager(layoutManager);

        mSession = new InstaSession(this);

        fetchAllRecentMediaPerUser();
    }

    private void setUpdatedImageAdapter() {
        ArrayList items = new ArrayList<>();
        for(HashMap<String, String> hashMap: imagesInfo) {
            items.add(new ImageItem(hashMap.get(Constants.TAG_IMAGE_STANDARD), hashMap.get(Constants.TAG_USERNAME)));
        }
        if (Adapter != null) {
            ((ImageItemAdapter)Adapter).updateDataSet(items);
        } else {
            Adapter = new ImageItemAdapter(getApplicationContext(), items, R.layout.activity_share_group);
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

                            imagesInfo.add(hashMap);
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
            return ((Integer)lhs).compareTo(rhs);
        }
    }

    class QueryByComments implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
            int lhs = Integer.parseInt(o1.get(Constants.TAG_COMMENTS));
            int rhs = Integer.parseInt(o2.get(Constants.TAG_COMMENTS));
            return ((Integer)lhs).compareTo(rhs);
        }
    }


}
