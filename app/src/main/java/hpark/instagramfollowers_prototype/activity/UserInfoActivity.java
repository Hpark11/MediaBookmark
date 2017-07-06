package hpark.instagramfollowers_prototype.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.util.Constants;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */

public class UserInfoActivity extends AppCompatActivity {

    private InstaSession mSession;
    private HashMap<String, String> userInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        Intent intent = getIntent();
        userInfo = (HashMap<String, String>)intent.getSerializableExtra("map");

        mSession = new InstaSession(this);
    }

    public void followingButtonTapped(View view) {
        String url = "https://api.instagram.com/v1/users/self/follows?access_token=" + mSession.getAccessToken();
        startActivity(new Intent(UserInfoActivity.this, FollowInfoActivity.class).putExtra("userInfo", url));
    }

    public void followerButtonTapped(View view) {
        String url = "https://api.instagram.com/v1/users/self/followed-by?access_token=" + mSession.getAccessToken();
        startActivity(new Intent(UserInfoActivity.this, FollowInfoActivity.class).putExtra("userInfo", url));
    }

    public void notEachOtherButtonTapped(View view) {
    }

//    public void loginButtonTapped(View view) {
//        Intent intent = new Intent(UserInfoActivity.this, InstagramLoginActivity.class);
//        startActivity(intent);
//    }
}
