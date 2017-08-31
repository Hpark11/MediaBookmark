package hpark.instagramfollowers_prototype.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupAdapter;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.model.ShareGroupItem;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */
public class MainInfoActivity extends AppCompatActivity implements ShareGroupAdapter.OnCheckShareGroupDetailListener {

    private final static String TAG = "MainInfoActivity";

    @BindView(R.id.userImageView)
    ImageView userImageView;
    @BindView(R.id.userNameTextView)
    TextView userNameTextView;
    @BindView(R.id.userIntroTextView)
    TextView userIntroTextView;
    @BindView(R.id.numPostsTextView)
    TextView numPostsTextView;
    @BindView(R.id.numFollowingTextView)
    TextView numFollowingTextView;
    @BindView(R.id.numFollowersTextView)
    TextView numFollowersTextView;
    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.shareGroupListView)
    ListView shareGroupListView;

    private InstaSession mSession;
    private HashMap<String, String> ownerInfo;
    private DatabaseManager databaseManager;
    private ArrayList<ShareGroupItem> shareGroupItems = new ArrayList<>();
    private ShareGroupAdapter shareGroupAdapter;
    private ProgressDialog progressDialog;

    private final static int MODE_MANAGE = 1;
    private final static int MODE_SHAREGROUP = 2;
    private int currentMode = MODE_SHAREGROUP;

    private ArrayList<HashMap<String, String>> eachOtherUsersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> onlyFollowedByUsersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> unfollowedByUsersInfo = new ArrayList<HashMap<String, String>>();
    //private ArrayList<HashMap<String, String>> blockedByUsersInfo = new ArrayList<HashMap<String, String>>();

    private ArrayList<HashMap<String, String>> usersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> followersInfo = new ArrayList<HashMap<String, String>>();

    Button eachOtherButton;
    Button onlyFollowedByButton;
    Button unfollowedButton;
    Button blockmeButton;

    private final static int FOLLOWS = 1;
    private final static int FOLLOWED = 2;

    private int progressDone = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        ownerInfo = (HashMap<String, String>)intent.getSerializableExtra("map");
        mSession = new InstaSession(this);

        initViews();
        databaseManager = new DatabaseManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshUserInfo();
    }

    private void initViews() {
        userNameTextView.setText(ownerInfo.get(Constants.TAG_USERNAME));
        userIntroTextView.setText(ownerInfo.get(Constants.TAG_BIO).replace("\n", " "));
        numPostsTextView.setText(ownerInfo.get(Constants.TAG_MEDIA));
        numFollowingTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWS));
        numFollowersTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWED_BY));
        Picasso.with(this).load(ownerInfo.get(Constants.TAG_PROFILE_PICTURE)).into(userImageView);

        eachOtherButton = (Button) findViewById(R.id.eachOtherButton);
        eachOtherButton.setVisibility(View.INVISIBLE);
        onlyFollowedByButton = (Button) findViewById(R.id.onlyFollowedByButton);
        onlyFollowedByButton.setVisibility(View.INVISIBLE);
        unfollowedButton = (Button) findViewById(R.id.unfollowButton);
        unfollowedButton.setVisibility(View.INVISIBLE);
        blockmeButton = (Button) findViewById(R.id.blockmeButton);
        blockmeButton.setVisibility(View.INVISIBLE);
        blockmeButton.setEnabled(false);

        shareGroupListView.setVisibility(View.VISIBLE);

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_app_id));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentMode == MODE_MANAGE) {
                    floatingActionButton.setImageResource(R.drawable.ic_settings_white_24dp);
                    currentMode = MODE_SHAREGROUP;
                } else {
                    floatingActionButton.setImageResource(R.drawable.ic_face_white_24dp);
                    currentMode = MODE_MANAGE;
                }
                changeMode();
            }
        });
    }

    private void changeMode() {
        if (currentMode == MODE_MANAGE) {
            shareGroupListView.setVisibility(View.INVISIBLE);
            eachOtherButton.setVisibility(View.VISIBLE);
            onlyFollowedByButton.setVisibility(View.VISIBLE);
            unfollowedButton.setVisibility(View.VISIBLE);
            blockmeButton.setVisibility(View.VISIBLE);
        } else {
            shareGroupListView.setVisibility(View.VISIBLE);
            eachOtherButton.setVisibility(View.INVISIBLE);
            onlyFollowedByButton.setVisibility(View.INVISIBLE);
            unfollowedButton.setVisibility(View.INVISIBLE);
            blockmeButton.setVisibility(View.INVISIBLE);
        }
    }

    private void loadShareGroupList() {
        shareGroupItems.clear();
        Cursor cursor = databaseManager.queryShareGroupValues(null, null, null, DatabaseManager.colShareGroupName);

        if(cursor.moveToFirst()) {
            do  {
                shareGroupItems.add(new ShareGroupItem(cursor.getInt(cursor.getColumnIndex(DatabaseManager.colId)),
                        cursor.getString(cursor.getColumnIndex(DatabaseManager.colShareGroupName)),
                        cursor.getString(cursor.getColumnIndex(DatabaseManager.colUsersInfo))));
            } while (cursor.moveToNext());
        }

        shareGroupAdapter = new ShareGroupAdapter(this, shareGroupItems);
        shareGroupListView.setAdapter(shareGroupAdapter);
    }

    public void refreshUserInfo() {
        progressDialog = ProgressDialog.show(this, "로딩 중", "Loading...");
        progressDone = 0;

        eachOtherUsersInfo.clear();
        onlyFollowedByUsersInfo.clear();
        unfollowedByUsersInfo.clear();

        usersInfo.clear();
        followersInfo.clear();

        String urlFollows = "https://api.instagram.com/v1/users/self/follows?access_token=" + mSession.getAccessToken();
        String urlFollowers = "https://api.instagram.com/v1/users/self/followed-by?access_token=" + mSession.getAccessToken();
        fetchAllFollowInfo(urlFollows, followingHandler, FOLLOWS);
        fetchAllFollowInfo(urlFollowers, followingHandler, FOLLOWED);

        loadShareGroupList();
    }

    public void eachOtherButtonTapped(View view) {
        startFollowInfoActivity(eachOtherUsersInfo, Constants.REL_EACH_OTHER);
    }

    public void blockmeButtonTapped(View view) {
        startFollowInfoActivity(unfollowedByUsersInfo, Constants.REL_BLOCKED_BY);
    }

    public void unfollowButtonTapped(View view) {
        startFollowInfoActivity(unfollowedByUsersInfo, Constants.REL_UNFOLLOWED_BY);
    }

    public void onlyFollowedByButtonTapped(View view) {
        startFollowInfoActivity(onlyFollowedByUsersInfo, Constants.REL_ONLY_FOLLOWED_BY);
    }

    private void startFollowInfoActivity(final ArrayList<HashMap<String, String>> selectedUsersInfo, final int relationship) {
        Intent intent = new Intent(MainInfoActivity.this, FollowInfoActivity.class);
        intent.putExtra("usersInfo", selectedUsersInfo);
        intent.putExtra("relationship", relationship);
        startActivity(intent);
    }

    private Handler followingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                if(msg.arg1 == FOLLOWS) {
                    queryUserRelationShipAndClassifyUsers(usersInfo, FOLLOWS);
                } else if(msg.arg1 == FOLLOWED) {
                    queryUserRelationShipAndClassifyUsers(followersInfo, FOLLOWED);
                }
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(MainInfoActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    private void fetchAllFollowInfo(final String url, final Handler handler, final int identifier) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int what = Constants.WHAT_FINALIZE;
                try {
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    HttpRequestManager httpRequestManager = new HttpRequestManager();
                    JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(url);
                    JSONArray data = jsonObject.getJSONArray(Constants.TAG_DATA);

                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        JSONObject data_obj = data.getJSONObject(data_i);

                        hashMap.put(Constants.TAG_ID, data_obj.getString(Constants.TAG_ID));
                        hashMap.put(Constants.TAG_PROFILE_PICTURE, data_obj.getString(Constants.TAG_PROFILE_PICTURE));
                        hashMap.put(Constants.TAG_USERNAME, data_obj.getString(Constants.TAG_USERNAME));

                        if(identifier == FOLLOWS) {
                            usersInfo.add(hashMap);
                        } else if(identifier == FOLLOWED) {
                            followersInfo.add(hashMap);
                        }
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = Constants.WHAT_ERROR;
                }

                if(identifier == FOLLOWS) {
                    handler.sendMessage(handler.obtainMessage(what, FOLLOWS, 0));
                } else if(identifier == FOLLOWED) {
                    handler.sendMessage(handler.obtainMessage(what, FOLLOWED, 0));
                }
            }
        }).start();
    }

    private Handler classifyUsersHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            final int what = bundle.getInt("what", 0);
            final int identifier = bundle.getInt("identifier", 0);
            final int[] msgData = bundle.getIntArray("msgData");
            progressDone++;

            if (what == Constants.WHAT_FINALIZE) {
                if(identifier == FOLLOWS) {
                    for (int i = 0; i < msgData.length; i++) {
                        switch (msgData[i]) {
                            case Constants.REL_EACH_OTHER:
                                eachOtherUsersInfo.add(usersInfo.get(i));
                                break;
                            case Constants.REL_UNFOLLOWED_BY:
                                unfollowedByUsersInfo.add(usersInfo.get(i));
                                break;
                            default:
                                break;
                        }
                    }

                    eachOtherButton.setText("서로 팔로우\n\n" + eachOtherUsersInfo.size());
                    unfollowedButton.setText("나를 언팔로우\n\n" + unfollowedByUsersInfo.size());
                } else if(identifier == FOLLOWED) {
                    for (int i = 0; i < msgData.length; i++) {
                        switch (msgData[i]) {
                            case Constants.REL_ONLY_FOLLOWED_BY:
                                onlyFollowedByUsersInfo.add(followersInfo.get(i));
                                break;
                            default:
                                break;
                        }
                    }

                    onlyFollowedByButton.setText("상대만 팔로우\n\n" + onlyFollowedByUsersInfo.size());
                }
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(MainInfoActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }

            if(progressDone >= 2) {
                progressDialog.dismiss();
            }

            return false;
        }
    });

    private void queryUserRelationShipAndClassifyUsers(final ArrayList<HashMap<String, String>> usersInfo, final int identifier) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int[] msgData = new int[usersInfo.size()];
                int what = Constants.WHAT_FINALIZE;
                try {
                    HttpRequestManager httpRequestManager = new HttpRequestManager();

                    for (int i = 0; i < usersInfo.size(); i++) {
                        final String url = "https://api.instagram.com/v1/users/"
                                + usersInfo.get(i).get(Constants.TAG_ID)
                                + "/relationship?access_token="
                                + mSession.getAccessToken();

                        JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(url);
                        JSONObject data = jsonObject.getJSONObject(Constants.TAG_DATA);

                        if (identifier == FOLLOWS) {
                            String incomingStatus = data.getString(Constants.TAG_INCOMING_STATUS);
                            switch (incomingStatus) {
                                case Constants.STATUS_FOLLOWED_BY:
                                    msgData[i] = Constants.REL_EACH_OTHER;
                                    break;
                                case Constants.STATUS_NONE:
                                    msgData[i] = Constants.REL_UNFOLLOWED_BY;
                                    break;
                                default:
                                    break;
                            }
                        } else if(identifier == FOLLOWED) {
                            String outgoingStatus = data.getString(Constants.TAG_OUTGOING_STATUS);
                            switch (outgoingStatus) {
                                case Constants.STATUS_NONE:
                                    msgData[i] = Constants.REL_ONLY_FOLLOWED_BY;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = Constants.WHAT_ERROR;
                }

                Bundle bundle = new Bundle();
                bundle.putIntArray("msgData", msgData);
                bundle.putInt("what", what);
                bundle.putInt("identifier", identifier);
                Message message = new Message();
                message.setData(bundle);
                classifyUsersHandler.sendMessage(message);
            }
        }).start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu()");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");

        int id = item.getItemId();
        switch(id) {
            case R.id.refresh:
                refreshUserInfo();
                break;
            case R.id.menu_logout:
                if(mSession != null) {
                    mSession.resetAccessToken();
                    finish();
                }
                break;
            case R.id.menu_new_sharegroup:
                startActivity(new Intent(MainInfoActivity.this, AddNewShareGroupActivity.class));
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckShareGroupDetail(ArrayList<String> idList) {
        Intent intent = new Intent(MainInfoActivity.this, MediaShareGroupActivity.class);
        intent.putStringArrayListExtra("idList", idList);
        startActivity(intent);
    }
}
