package hpark.instagramfollowers_prototype.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.adapter.ImageItemAdapter;
import hpark.instagramfollowers_prototype.model.ImageItem;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */
public class MediaShareGroupActivity extends AppCompatActivity {

    Context context;
    RecyclerView recyclerView;
    RecyclerView.Adapter Adapter;
    RecyclerView.LayoutManager layoutManager;

    private ArrayList<String> idList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_group);
        idList = getIntent().getStringArrayListExtra("idList");
        context = getApplicationContext();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        // Item 리스트에 아이템 객체 넣기
        ArrayList items = new ArrayList<>();

        items.add(new ImageItem(R.drawable.common_google_signin_btn_icon_dark_disabled, "미키마우스"));
        items.add(new ImageItem(R.drawable.common_google_signin_btn_icon_dark_pressed, "인어공주"));
        items.add(new ImageItem(R.drawable.common_google_signin_btn_icon_light_disabled, "디즈니공주"));
        items.add(new ImageItem(R.drawable.common_google_signin_btn_text_light_normal, "토이스토리"));
        items.add(new ImageItem(R.drawable.common_full_open_on_phone, "니모를 찾아서"));

        // StaggeredGrid 레이아웃을 사용한다
        layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        //layoutManager = new LinearLayoutManager(this);
        //layoutManager = new GridLayoutManager(this,3);

        // 지정된 레이아웃매니저를 RecyclerView에 Set 해주어야한다.
        recyclerView.setLayoutManager(layoutManager);

        Adapter = new ImageItemAdapter(items, context);
        recyclerView.setAdapter(Adapter);
    }
}
