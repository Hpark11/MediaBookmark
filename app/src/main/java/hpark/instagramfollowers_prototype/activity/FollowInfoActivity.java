package hpark.instagramfollowers_prototype.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.FollowInfoAdapter;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */
public class FollowInfoActivity extends AppCompatActivity {

    private ListView followInfoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        followInfoListView = (ListView) findViewById(R.id.followInfoListView);

        url = getIntent().getStringExtra("userInfo");
        context = FollowInfoActivity.this;
        getAllMediaImages();
    }

    private String url = "";

    private ArrayList<HashMap<String, String>> usersInfo = new ArrayList<HashMap<String, String>>();
    private Context context;
    private int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private ProgressDialog pd;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (pd != null && pd.isShowing())
                pd.dismiss();
            if (msg.what == WHAT_FINALIZE) {
                setImageGridAdapter();
            } else {
                Toast.makeText(context, "Check your network.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    public static final String TAG_DATA = "data";
    public static final String TAG_ID = "id";
    public static final String TAG_PROFILE_PICTURE = "profile_picture";
    public static final String TAG_USERNAME = "username";
    public static final String TAG_BIO = "bio";
    public static final String TAG_WEBSITE = "website";
    public static final String TAG_FULL_NAME = "full_name";

    private void setImageGridAdapter() {
        followInfoListView.setAdapter(new FollowInfoAdapter(context, usersInfo));
    }

    private void getAllMediaImages() {
        pd = ProgressDialog.show(context, "", "Loading...");
        new Thread(new Runnable() {

            @Override
            public void run() {
                int what = WHAT_FINALIZE;
                try {
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    HttpRequestManager httpRequestManager = new HttpRequestManager();
                    JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(url);
                    JSONArray data = jsonObject.getJSONArray(TAG_DATA);
                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        JSONObject data_obj = data.getJSONObject(data_i);
                        String str_id = data_obj.getString(TAG_ID);

                        hashMap.put(TAG_PROFILE_PICTURE, data_obj.getString(TAG_PROFILE_PICTURE));

                        // String str_username =
                        // data_obj.getString(TAG_USERNAME);
                        //
                        // String str_bio = data_obj.getString(TAG_BIO);
                        //
                        // String str_website = data_obj.getString(TAG_WEBSITE);

                        hashMap.put(TAG_USERNAME,
                                data_obj.getString(TAG_USERNAME));
                        usersInfo.add(hashMap);
                    }
                    System.out.println("jsonObject::" + jsonObject);

                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = WHAT_ERROR;
                }
                // pd.dismiss();
                handler.sendEmptyMessage(what);
            }
        }).start();
    }

}