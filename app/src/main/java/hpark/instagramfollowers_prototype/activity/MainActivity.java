package hpark.instagramfollowers_prototype.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import hpark.instagramfollowers_prototype.R;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loginButtonTapped(View view) {
        Intent intent = new Intent(MainActivity.this, InstagramLoginActivity.class);
        startActivity(intent);
    }
}
