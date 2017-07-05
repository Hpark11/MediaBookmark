package hpark.instagramfollowers_prototype.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import hpark.instagramfollowers_prototype.R;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */

public class UserInfoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
    }

//    public void loginButtonTapped(View view) {
//        Intent intent = new Intent(UserInfoActivity.this, InstagramLoginActivity.class);
//        startActivity(intent);
//    }
}
