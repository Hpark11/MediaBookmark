package hpark.instagramfollowers_prototype.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

    private InstaSession mSession;
    private HashMap<String, String> userInfo;

    ImageView userImageView;
    TextView usernameTextView;
    TextView followInfoTextView;

    private ProgressDialog progressDialog;

    private ArrayList<HashMap<String, String>> eachOtherUsersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> onlyFollowedByUsersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> unfollowedByUsersInfo = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> blockedByUsersInfo = new ArrayList<HashMap<String, String>>();

    private ArrayList<HashMap<String, String>> usersInfo = new ArrayList<HashMap<String, String>>();

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ImageManager imageManager = new ImageManager(this);
        imageManager.displayImage(userInfo.get(Constants.TAG_PROFILE_PICTURE), userImageView);
        usernameTextView.setText(userInfo.get(Constants.TAG_USERNAME));
        followInfoTextView.setText("팔로워 : " + userInfo.get(Constants.TAG_FOLLOWED_BY) + ", 팔로잉 : " + userInfo.get(Constants.TAG_FOLLOWS));
    }

    public void followerButtonTapped(View view) {
        final String url = "https://api.instagram.com/v1/users/self/followed-by?access_token=" + mSession.getAccessToken();
        startActivity(new Intent(UserInfoActivity.this, FollowInfoActivity.class).putExtra("userInfo", url));
    }

    public void followingButtonTapped(View view) {
        String url = "https://api.instagram.com/v1/users/self/follows?access_token=" + mSession.getAccessToken();
        startActivity(new Intent(UserInfoActivity.this, FollowInfoActivity.class).putExtra("userInfo", url));
    }

    public void notEachOtherButtonTapped(View view) {

    }

    public void eachOtherButtonTapped(View view) {

    }

    public void blockmeButtonTapped(View view) {

    }

    public void unfollowButtonTapped(View view) {

    }

    public void onlyFollowedByButtonTapped(View view) {

    }

//    public void loginButtonTapped(View view) {
//        Intent intent = new Intent(UserInfoActivity.this, InstagramLoginActivity.class);
//        startActivity(intent);
//    }

    private Handler followeByHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (pd != null && pd.isShowing())
                pd.dismiss();
            if (msg.what == Constants.WHAT_FINALIZE) {
                setImageGridAdapter();
            } else {
                Toast.makeText(context, "Check your network.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

//    private Handler followingHandler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message msg) {
//            if (pd != null && pd.isShowing())
//                pd.dismiss();
//            if (msg.what == Constants.WHAT_FINALIZE) {
//                setImageGridAdapter();
//            } else {
//                Toast.makeText(context, "Check your network.", Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }
//    });

    private Handler followingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                queryUserRelationShipAndClassifyUsers(usersInfo);
            }
            return false;
        }
    });

    private Handler classifyUsersHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return false;
        }
    });

    private void fetchAllFollowInfo(final String url, final Handler handler) {
        progressDialog = ProgressDialog.show(this, "로딩 중", "Loading...");

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

                        usersInfo.add(hashMap);
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = Constants.WHAT_ERROR;
                }
                handler.sendEmptyMessage(what);
            }
        }).start();
    }



    private void queryUserRelationShipAndClassifyUsers(final ArrayList<HashMap<String, String>> usersInfo) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int[] msgData = new int[usersInfo.size()];
                int what = Constants.WHAT_FINALIZE;

                try {

                    for (int i = 0; i < usersInfo.size(); i++) {
                        final String url = "https://api.instagram.com/v1/users/"
                                + usersInfo.get(i).get(Constants.TAG_ID)
                                + "/relationship?access_token="
                                + mSession.getAccessToken();

                        HttpRequestManager httpRequestManager = new HttpRequestManager();
                        JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(url);
                        JSONObject data = jsonObject.getJSONObject(Constants.TAG_DATA);

                        String incomingStatus = data.getString(Constants.TAG_INCOMING_STATUS);

                        switch (incomingStatus) {
                            case Constants.STATUS_FOLLOWED_BY:
                                msgData[i] = Constants.REL_EACH_OTHER;
                                break;
                            case Constants.STATUS_NONE:
                                msgData[i] = Constants.REL_UNFOLLOWED_BY
                                break;
                            default:
                                break;
                        }
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = Constants.WHAT_ERROR;
                }

                Bundle bundle = new Bundle();
                bundle.putIntArray("", msgData);
                Message message = new Message();
                message.setData(bundle);
                classifyUsersHandler.sendMessage(message);
            }
        });
    }




}
