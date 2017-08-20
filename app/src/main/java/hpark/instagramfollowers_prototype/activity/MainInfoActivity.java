package hpark.instagramfollowers_prototype.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupAdapter;
import hpark.instagramfollowers_prototype.model.ShareGroupItem;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */

public class MainInfoActivity extends AppCompatActivity {

    @BindView(R.id.shareGroupListView)
    ListView shareGroupListView;

    DatabaseManager databaseManager;
    ArrayList<ShareGroupItem> shareGroupItems = new ArrayList<>();
    ShareGroupAdapter shareGroupAdapter;

    List<String> list;
    int size;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);

        databaseManager = new DatabaseManager(this);
    }

    private void setShareGroupAdapter() {
        //shareGroupListView.setAdapter(new ShareGroupAdapter());
        //shareGroupListView.setAdapter(new ShareGroupAdapter());
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
        shareGroupListView = (ListView) findViewById(R.id.shareGroupListView);
        shareGroupListView.setAdapter(shareGroupAdapter);
    }





}
