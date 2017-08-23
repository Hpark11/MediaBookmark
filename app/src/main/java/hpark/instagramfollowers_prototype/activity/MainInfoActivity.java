package hpark.instagramfollowers_prototype.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupAdapter;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.fragment.ShareGroupListFragment;
import hpark.instagramfollowers_prototype.model.ShareGroupItem;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.DatabaseManager;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */
public class MainInfoActivity extends AppCompatActivity {

    private final static String TAG = "UserInfoActivity";

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
    @BindView(R.id.adView)
    AdView adView;

    private InstaSession mSession;
    private HashMap<String, String> ownerInfo;
    private DatabaseManager databaseManager;
    private ArrayList<ShareGroupItem> shareGroupItems = new ArrayList<>();
    private ShareGroupAdapter shareGroupAdapter;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        ownerInfo = (HashMap<String, String>)intent.getSerializableExtra("map");

        initViews();
        databaseManager = new DatabaseManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initViews() {
        userNameTextView.setText(ownerInfo.get(Constants.TAG_USERNAME));
        userIntroTextView.setText(ownerInfo.get(Constants.TAG_BIO).replace("\n", " "));
        numPostsTextView.setText(ownerInfo.get(Constants.TAG_MEDIA));
        numFollowingTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWS));
        numFollowersTextView.setText(ownerInfo.get(Constants.TAG_FOLLOWED_BY));
        Picasso.with(this).load(ownerInfo.get(Constants.TAG_PROFILE_PICTURE)).into(userImageView);

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_app_id));

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.userManageFrameLayout, new ShareGroupListFragment());
        fragmentTransaction.commit();
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

//        shareGroupAdapter = new ShareGroupAdapter(this, shareGroupItems);
//        shareGroupListView = (ListView) findViewById(R.id.shareGroupListView);
//        shareGroupListView.setAdapter(shareGroupAdapter);
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
                //refreshUserInfo();
                break;
            case R.id.menu_logout:
//                if(mSession != null) {
//                    mSession.resetAccessToken();
//                    finish();
//                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
