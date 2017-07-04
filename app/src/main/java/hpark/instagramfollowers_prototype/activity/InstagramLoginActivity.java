package hpark.instagramfollowers_prototype.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.api.InstaApp;
import hpark.instagramfollowers_prototype.util.Constants;

/**
 * Created by hpark_ipl on 2017. 7. 4..
 */

public class InstagramLoginActivity extends AppCompatActivity {

    private InstaApp instaApp;
    private HashMap<String, String> userInfoHashmap = new HashMap<String, String>();

    private InstaApp.OAuthAuthenticationListener authenticationListener = new InstaApp.OAuthAuthenticationListener() {
        @Override
        public void onSuccess() {
            instaApp.fetchUserName(handler);
        }

        @Override
        public void onFail(String error) {
            Toast.makeText(InstagramLoginActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        instaApp = new InstaApp(this, Constants.CLIENT_ID, Constants.CLIENT_SECRET, Constants.CALLBACK_URL);
        instaApp.setListener(authenticationListener);

        setWidgetReference();
        bindEventHandlers();
        if (mApp.hasAccessToken()) {
            // tvSummary.setText("Connected as " + mApp.getUserName());
            btnConnect.setText("Disconnect");
            llAfterLoginView.setVisibility(View.VISIBLE);
            mApp.fetchUserName(handler);
        }

    }

    private void bindEventHandlers() {
        btnConnect.setOnClickListener(this);
        btnViewInfo.setOnClickListener(this);
        btnGetAllImages.setOnClickListener(this);
    }

    private void setWidgetReference() {
        llAfterLoginView = (LinearLayout) findViewById(R.id.llAfterLoginView);
        btnConnect = (Button) findViewById(R.id.btnConnect);
        btnViewInfo = (Button) findViewById(R.id.btnViewInfo);
        btnGetAllImages = (Button) findViewById(R.id.btnGetAllImages);
    }

    // OAuthAuthenticationListener listener ;
    @Override
    public void onClick(View v) {
        if (v == btnConnect) {
            connectOrDisconnectUser();
        } else if (v == btnViewInfo) {
            displayInfoDialogView();
        } else if (v == btnGetAllImages) {
            startActivity(new Intent(MainActivity.this, AllMediaFiles.class).putExtra("userInfo", userInfoHashmap));
        }
    }

    private void connectOrDisconnectUser() {
        if (mApp.hasAccessToken()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("Disconnect from Instagram?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mApp.resetAccessToken();
                            // btnConnect.setVisibility(View.VISIBLE);
                            llAfterLoginView.setVisibility(View.GONE);
                            btnConnect.setText("Connect");
                            // tvSummary.setText("Not connected");
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            final AlertDialog alert = builder.create();
            alert.show();
        } else {
            mApp.authorize();
        }
    }


    private void displayInfoDialogView() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Profile Info");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.profile_view, null);
        alertDialog.setView(view);
        ImageView ivProfile = (ImageView) view.findViewById(R.id.ivProfileImage);
        TextView tvName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvNoOfFollwers = (TextView) view.findViewById(R.id.tvNoOfFollowers);
        TextView tvNoOfFollowing = (TextView) view.findViewById(R.id.tvNoOfFollowing);

        new ImageLoader(MainActivity.this).DisplayImage(userInfoHashmap.get(InstagramApp.TAG_PROFILE_PICTURE), ivProfile);

        tvName.setText(userInfoHashmap.get(InstagramApp.TAG_USERNAME));
        tvNoOfFollowing.setText(userInfoHashmap.get(InstagramApp.TAG_FOLLOWS));
        tvNoOfFollwers.setText(userInfoHashmap.get(InstagramApp.TAG_FOLLOWED_BY));
        alertDialog.create().show();
    }






























    static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };
    static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };

    static final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.FILL_PARENT,
            ViewGroup.LayoutParams.FILL_PARENT
    );

    private Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == InstagramApp.WHAT_FINALIZE) {
                userInfoHashmap = mApp.getUserInfo();
            } else if (msg.what == InstagramApp.WHAT_FINALIZE) {
                Toast.makeText(MainActivity.this, "Check your network.", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    });


    static final int MARGIN = 4;
    static final int PADDING = 2;
    private String mUrl;
    private OAuthDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
    private static final String TAG = "Instagram-WebView";

    public InstagramDialog(Context context, String url, OAuthDialogListener listener) {
        super(context);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        addContentView(mContent, new FrameLayout.LayoutParams( (int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1] * scale + 0.5f)));
        CookieSyncManager.createInstance(getContext()); CookieManager cookieManager = CookieManager.getInstance(); cookieManager.removeAllCookie();
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mTitle = new TextView(getContext());
        mTitle.setText("Instagram");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(Color.BLACK);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mContent.addView(mTitle);
    }

    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new OAuthWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView); }

    private class OAuthWebViewClient extends WebViewClient {
        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirecting URL " + url);
            if (url.startsWith(InstagramApp.mCallbackUrl)) {
                String urls[] = url.split("="); mListener.onComplete(urls[1]);
                InstagramDialog.this.dismiss(); return true; } return false;
        }

        @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Log.d(TAG, "Page error: " + description); super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(description); InstagramDialog.this.dismiss();
        }

        @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "Loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            Log.d(TAG, "onPageFinished URL: " + url);
            mSpinner.dismiss();
        }
    }

    public interface OAuthDialogListener {
        public abstract void onComplete(String accessToken);
        public abstract void onError(String error);
    }
}
