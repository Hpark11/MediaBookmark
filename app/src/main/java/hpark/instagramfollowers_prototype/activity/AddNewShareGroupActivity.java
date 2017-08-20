package hpark.instagramfollowers_prototype.activity;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupMemberAdapter;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */

public class AddNewShareGroupActivity extends AppCompatActivity {

    private final static String TAG = "AddNewShareGroupActi";

    @BindView(R.id.followUsersInfoListView)
    ListView followUsersInfoListView;
    @BindView(R.id.shareGroupEditText)
    EditText shareGroupEditText;

    private DatabaseManager databaseManager;
    private SearchView searchView;
    private InstaSession instaSession;
    private ArrayList<HashMap<String, String>> usersInfo = new ArrayList<HashMap<String, String>>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_share_group);

        instaSession = new InstaSession(this);
        databaseManager = new DatabaseManager(this);

        String urlFollows;
        if(instaSession.getAccessToken() != null) {
            urlFollows = "https://api.instagram.com/v1/users/self/follows?access_token=" + instaSession.getAccessToken();
            fetchAllFollowInfo(urlFollows, followingHandler);
        }
    }

    private void setShareGroupAdapter(ArrayList<HashMap<String, String>> filteredUsersInfo) {
        followUsersInfoListView.setAdapter(new ShareGroupMemberAdapter(this, filteredUsersInfo));
    }

    private Handler followingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.WHAT_FINALIZE) {
                setShareGroupAdapter(usersInfo);
                //followUsersInfoListView.setAdapter(new FollowInfoAdapter(this, usersInfo, ));
            } else if (msg.what == Constants.WHAT_ERROR) {
                Toast.makeText(AddNewShareGroupActivity.this, "네트워크 에러", Toast.LENGTH_LONG).show();
            }
            return false;
        }
    });

    private void fetchAllFollowInfo(final String url, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int what = Constants.WHAT_FINALIZE;
                try {
                    HttpRequestManager httpRequestManager = new HttpRequestManager();
                    JSONObject jsonObject = httpRequestManager.acquireJsonwithGetRequest(url);
                    JSONArray data = jsonObject.getJSONArray(Constants.TAG_DATA);

                    for (int data_i = 0; data_i < data.length(); data_i++) {
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        JSONObject data_obj = data.getJSONObject(data_i);

                        hashMap.put(Constants.TAG_ID, data_obj.getString(Constants.TAG_ID));
                        hashMap.put(Constants.TAG_USERNAME, data_obj.getString(Constants.TAG_USERNAME));

                        usersInfo.add(hashMap);
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    what = Constants.WHAT_ERROR;
                }
                handler.sendMessage(handler.obtainMessage(what, 0));
            }
        }).start();
    }

    private void queryByUserId(String query) {
        ArrayList<HashMap<String, String>> searchedUsersInfo = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < usersInfo.size(); i++) {
            if((usersInfo.get(i).get(Constants.TAG_USERNAME)).contains(query)) {
                searchedUsersInfo.add(usersInfo.get(i));
            }
        }
        setShareGroupAdapter(usersInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sharegroup_add_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_sharegroup_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("유저 검색");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryByUserId(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryByUserId(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "onOptionsItemSelected()");

        int id = item.getItemId();
        switch(id) {
            case R.id.action_sharegroup_add:
                //addNewShareGroup()
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addNewShareGroup(String name) {

        String usersInfoData = "";
        for (HashMap<String, String> info : usersInfo) {
            usersInfoData += info.get(Constants.TAG_ID) + "|";
            usersInfoData += info.get(Constants.TAG_USERNAME) + " ";
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseManager.colShareGroupName, name);
        contentValues.put(DatabaseManager.colUsersInfo, usersInfoData);

        if (databaseManager != null) {
            long id = databaseManager.insertShareGroupValue(contentValues);

            if(id > 0) {
                Toast.makeText(getApplicationContext(), "userId : " + id, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "unable to Insert", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
