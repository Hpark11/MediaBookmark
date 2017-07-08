package hpark.instagramfollowers_prototype.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.ImageManager;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */

public class UserInfoActivity extends AppCompatActivity {

    private final static String TAG = "UserInfoActivity";
    private InstaSession mSession;
    private HashMap<String, String> userInfo;

    ImageView userImageView;
    TextView usernameTextView;
    TextView followInfoTextView;

    private ProgressDialog progressDialog;

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
        setContentView(R.layout.activity_userinfo);

        Intent intent = getIntent();
        userInfo = (HashMap<String, String>)intent.getSerializableExtra("map");
        mSession = new InstaSession(this);

        userImageView = (ImageView) findViewById(R.id.userImageView);
        usernameTextView = (TextView) findViewById(R.id.usernameTextView);
        followInfoTextView = (TextView) findViewById(R.id.followInfoTextView);

        eachOtherButton = (Button) findViewById(R.id.eachOtherButton);
        onlyFollowedByButton = (Button) findViewById(R.id.onlyFollowedByButton);
        unfollowedButton = (Button) findViewById(R.id.unfollowButton);
        blockmeButton = (Button) findViewById(R.id.blockmeButton);

        final ImageManager imageManager = new ImageManager(this);
        imageManager.displayImage(userInfo.get(Constants.TAG_PROFILE_PICTURE), userImageView);
        usernameTextView.setText(userInfo.get(Constants.TAG_USERNAME));
        followInfoTextView.setText("팔로워 : " + userInfo.get(Constants.TAG_FOLLOWED_BY) + ", 팔로잉 : " + userInfo.get(Constants.TAG_FOLLOWS));

        refreshUserInfo();
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
        Intent intent = new Intent(UserInfoActivity.this, FollowInfoActivity.class);
        intent.putExtra("usersInfo", selectedUsersInfo);
        intent.putExtra("relationship", relationship);
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
                Toast.makeText(UserInfoActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
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

                    eachOtherButton.setText("서로 팔로우\n" + eachOtherUsersInfo.size());
                    unfollowedButton.setText("나를 언팔로우\n" + unfollowedByUsersInfo.size());
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

                    onlyFollowedByButton.setText("상대만 나를 팔로우\n" + onlyFollowedByUsersInfo.size());
                }
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(UserInfoActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
