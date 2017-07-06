package hpark.instagramfollowers_prototype.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.StreamManager;

/**
 * Created by hpark_ipl on 2017. 7. 4..
 */

public class InstagramLoginActivity extends AppCompatActivity {

    private String mAccessToken;
    private String mAuthUrl;
    private InstaSession mSession;

    private WebView webView;
    private ProgressDialog progressDialog;
    private HashMap<String, String> userInfo = new HashMap<String, String>();

    private static int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;

    private static final String TAG = "InstagramLoginActivity";

    private interface OAuthAuthenticationListener {
        public abstract void onSuccess();
        public abstract void onFail(String error);
    }

    public interface OAuthWebViewListener {
        public abstract void onComplete(String accessToken);
        public abstract void onError(String error);
    }

    private OAuthAuthenticationListener authenticationListener = new OAuthAuthenticationListener() {
        @Override
        public void onSuccess() {
            fetchUserName();
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(InstagramLoginActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    private OAuthWebViewListener authWebViewListener = new OAuthWebViewListener() {
        @Override
        public void onComplete(String code) {
            getAccessToken(code);
        }
        @Override
        public void onError(String error) {
            authenticationListener.onFail("Authorization failed");
        }
    };

    private Handler accessTokenHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                progressDialog.dismiss();
                if (msg.arg1 == 1) {
                    authenticationListener.onFail("Failed to get access token");
                } else if (msg.arg1 == 2) {
                    authenticationListener.onFail("Failed to get user information");
                }
            } else if (msg.what == WHAT_FETCH_INFO) {
                progressDialog.dismiss();
                authenticationListener.onSuccess();
            }
        }
    };

    private Handler userInfoHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_FINALIZE) {
                Intent intent = new Intent(InstagramLoginActivity.this, UserInfoActivity.class);
                intent.putExtra("map", userInfo);
                startActivity(intent);
            } else if (msg.what == WHAT_ERROR) {
                Toast.makeText(InstagramLoginActivity.this, "Check your network.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSession = new InstaSession(this);
        mAccessToken = mSession.getAccessToken();

        mAuthUrl = Constants.AUTH_URL
                + "?client_id="
                + Constants.CLIENT_ID
                + "&redirect_uri="
                + Constants.CALLBACK_URL
                + "&response_type=code&display=touch&scope=basic+public_content+follower_list+comments+relationships+likes";

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (mAccessToken != null) {
            fetchUserName();
        } else {
            setUpWebView();
        }
    }

    private void setUpWebView() {
        webView = (WebView) findViewById(R.id.webView);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(mAuthUrl);
        //webView.setLayoutParams(FILL);
        //mContent.addView(mWebView);
    }

    private void getAccessToken(final String code) {
        progressDialog.setMessage("Getting access token ...");
        progressDialog.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(Constants.TOKEN_URL);
                    // URL url = new URL(mTokenUrl + "&code=" + code);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id=" + Constants.CLIENT_ID + "&client_secret="
                            + Constants.CLIENT_SECRET + "&grant_type=authorization_code"
                            + "&redirect_uri=" + Constants.CALLBACK_URL + "&code=" + code);
                    writer.flush();
                    String response = StreamManager.streamToString(urlConnection.getInputStream());
                    Log.i(TAG, "response " + response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

                    mAccessToken = jsonObj.getString("access_token");
                    Log.i(TAG, "Got access token: " + mAccessToken);

                    String id = jsonObj.getJSONObject("user").getString("id");
                    String user = jsonObj.getJSONObject("user").getString("username");
                    String name = jsonObj.getJSONObject("user").getString("full_name");

                    mSession.storeAccessToken(mAccessToken, id, user, name);

                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }

                accessTokenHandler.sendMessage(accessTokenHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }

    public void fetchUserName() {
        progressDialog.setTitle("로딩 중..");
        progressDialog.show();

        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FINALIZE;
                try {
                    URL url = new URL(Constants.API_URL + "/users/" + mSession.getId() + "/?access_token=" + mAccessToken);

                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();

                    String response = StreamManager.streamToString(urlConnection.getInputStream());
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();

                    JSONObject data_obj = jsonObj.getJSONObject(Constants.TAG_DATA);
                    userInfo.put(Constants.TAG_ID, data_obj.getString(Constants.TAG_ID));
                    userInfo.put(Constants.TAG_PROFILE_PICTURE, data_obj.getString(Constants.TAG_PROFILE_PICTURE));
                    userInfo.put(Constants.TAG_USERNAME, data_obj.getString(Constants.TAG_USERNAME));
                    userInfo.put(Constants.TAG_BIO, data_obj.getString(Constants.TAG_BIO));
                    userInfo.put(Constants.TAG_WEBSITE, data_obj.getString(Constants.TAG_WEBSITE));

                    JSONObject counts_obj = data_obj.getJSONObject(Constants.TAG_COUNTS);
                    userInfo.put(Constants.TAG_FOLLOWS, counts_obj.getString(Constants.TAG_FOLLOWS));
                    userInfo.put(Constants.TAG_FOLLOWED_BY, counts_obj.getString(Constants.TAG_FOLLOWED_BY));
                    userInfo.put(Constants.TAG_MEDIA, counts_obj.getString(Constants.TAG_MEDIA));
                    userInfo.put(Constants.TAG_FULL_NAME, data_obj.getString(Constants.TAG_FULL_NAME));

                    JSONObject meta_obj = jsonObj.getJSONObject(Constants.TAG_META);
                    userInfo.put(Constants.TAG_CODE, meta_obj.getString(Constants.TAG_CODE));
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                progressDialog.dismiss();
                userInfoHandler.sendMessage(userInfoHandler.obtainMessage(what, 2, 0));
            }
        }.start();
    }

    private class OAuthWebViewClient extends WebViewClient {

        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);
            if (url.startsWith(Constants.CALLBACK_URL)) {
                String urls[] = url.split("=");
                authWebViewListener.onComplete(urls[1]);
                return true;
            }
            return false;
        }

        @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description);
            super.onReceivedError(view, errorCode, description, failingUrl);
            authWebViewListener.onError(description);
            finish();
        }

        @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            progressDialog.show();
        }

        @Override public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            Log.d(TAG, "onPageFinished URL: " + url);
            progressDialog.dismiss();
        }
    }
}
