package hpark.instagramfollowers_prototype.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupAdapter;
import hpark.instagramfollowers_prototype.model.ShareGroupItem;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */

public class MainInfoActivity extends AppCompatActivity {

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
    @BindView(R.id.shareGroupListView)
    ListView shareGroupListView;

    private HashMap<String, String> ownerInfo;
    DatabaseManager databaseManager;
    ArrayList<ShareGroupItem> shareGroupItems = new ArrayList<>();
    ShareGroupAdapter shareGroupAdapter;

    List<String> list;
    int size;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);

        userNameTextView.setText(ownerInfo.get(Constants.TAG_USERNAME));
        userIntroTextView.setText(ownerInfo.get(Constants.TAG_BIO));
        numPostsTextView.setText(ownerInfo.get(Constants.TAG_MEDIA));
        numFollowingTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWS));
        numFollowersTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWED_BY));

        databaseManager = new DatabaseManager(this);
        Picasso.with(this).load(ownerInfo.get(Constants.TAG_PROFILE_PICTURE)).into(userImageView);
    }

    @Override
    protected void onStart() {
        super.onStart();




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
