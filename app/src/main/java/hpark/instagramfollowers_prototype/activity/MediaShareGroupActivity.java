package hpark.instagramfollowers_prototype.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
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

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    private InstaSession mSession;
    private ArrayList<String> idList;
    private ArrayList<HashMap<String, String>> imagesInfo = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_group);
        idList = getIntent().getStringArrayListExtra("idList");
        context = getApplicationContext();

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

    private Handler mediaRecvHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                ArrayList items = new ArrayList<>();
                for(HashMap<String, String> hashMap: imagesInfo) {
                    items.add(new ImageItem(hashMap.get(Constants.TAG_IMAGE_STANDARD), hashMap.get(Constants.TAG_USERNAME)));
                }
                Adapter = new ImageItemAdapter(getApplicationContext(), items, R.layout.activity_share_group);
                recyclerView.setAdapter(Adapter);
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

                            hashMap.put(Constants.TAG_IMAGE_STANDARD, standardImage.getString("url"));
                            hashMap.put(Constants.TAG_USERNAME, user.getString(Constants.TAG_USERNAME));

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
}
