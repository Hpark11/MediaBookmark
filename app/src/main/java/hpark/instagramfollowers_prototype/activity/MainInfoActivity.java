package hpark.instagramfollowers_prototype.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ShareGroupAdapter;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */

public class MainInfoActivity extends AppCompatActivity {

    @BindView(R.id.shareGroupListView)
    ListView shareGroupListView;

    List<String> list;

    int size;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_info);
    }

    private void setShareGroupAdapter() {
        //shareGroupListView.setAdapter(new ShareGroupAdapter());
    }


}
