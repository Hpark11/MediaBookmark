package hpark.instagramfollowers_prototype.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.FollowInfoAdapter;
import hpark.instagramfollowers_prototype.util.Constants;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */
public class FollowInfoActivity extends AppCompatActivity {

    private SearchView searchView;
    private ListView followInfoListView;
    private int relationship = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        followInfoListView = (ListView) findViewById(R.id.followInfoListView);

        relationship = getIntent().getIntExtra("relationship", -1);
        usersInfo = (ArrayList<HashMap<String, String>>)getIntent().getSerializableExtra("usersInfo");
        setImageGridAdapter(usersInfo);
    }

    private ArrayList<HashMap<String, String>> usersInfo = new ArrayList<HashMap<String, String>>();

    private void setImageGridAdapter(ArrayList<HashMap<String, String>> filteredUsersInfo) {
        followInfoListView.setAdapter(new FollowInfoAdapter(this, filteredUsersInfo, relationship));
    }

    private void queryByUserId(String query) {
        ArrayList<HashMap<String, String>> searchedUsersInfo = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < usersInfo.size(); i++) {
            if((usersInfo.get(i).get(Constants.TAG_USERNAME)).contains(query)) {
                searchedUsersInfo.add(usersInfo.get(i));
            }
        }

        setImageGridAdapter(searchedUsersInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.followinfo_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
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
}