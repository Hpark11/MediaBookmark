package hpark.instagramfollowers_prototype.api;

import android.content.Context;

/**
 * Created by hpark_ipl on 2017. 7. 4..
 */

public class InstaApp {
    private InstagramSession mSession;
    private InstagramDialog mDialog;
    private OAuthAuthenticationListener mListener;
    private ProgressDialog mProgress;
    private String mAuthUrl;
    private String mAccessToken;
    private Context mCtx;
    private String mClientId;
    private String mClientSecret;
    private static int WHAT_FINALIZE = 0;
    private static int WHAT_ERROR = 1;
    private static int WHAT_FETCH_INFO = 2;

    public static String mCallbackUrl = "";

    private static final String TAG = "InstagramAPI";



    private InstaSession session;
    private static InstaApp instance = new InstaApp();
    private String authUrl;


    public static InstaApp getInstance() {
        if(instance == null) {
            instance = new InstaApp();
        }
        return instance;
    }

    private InstaApp() {

    }

    public void setAppContext(Context context) {
        session = null;
        session = new InstaSession(context);
    }

    public InstagramApp(Context context, String clientId, String clientSecret, String callbackUrl) {
        mClientId = clientId;
        mClientSecret = clientSecret;
        mCtx = context;

        mAccessToken = mSession.getAccessToken();
        mCallbackUrl = callbackUrl;

        mAuthUrl = AUTH_URL + "?client_id=" + clientId + "&redirect_uri="
                + mCallbackUrl + "&response_type=code&display=touch&scope=likes+comments+relationships";


        mDialog = new InstagramDialog(context, mAuthUrl, listener);
        mProgress = new ProgressDialog(context);
        mProgress.setCancelable(false);
    }

    private void getAccessToken(final String code) {
        mProgress.setMessage("Getting access token ...");
        mProgress.show();
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Getting access token");
                int what = WHAT_FETCH_INFO;
                try {
                    URL url = new URL(TOKEN_URL);
                    //URL url = new URL(mTokenUrl + "&code=" + code);
                    Log.i(TAG, "Opening Token URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);
                    //urlConnection.connect();
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write("client_id="+mClientId+
                            "&client_secret="+mClientSecret+
                            "&grant_type=authorization_code" +
                            "&redirect_uri="+mCallbackUrl+
                            "&code=" + code);
                    writer.flush();
                    String response = streamToString(urlConnection.getInputStream());
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
                mHandler.sendMessage(mHandler.obtainMessage(what, 1, 0));
            }
        }.start();
    }


    private void fetchUserName() {
        mProgress.setMessage("Finalizing ...");
        new Thread() {
            @Override
            public void run() {
                Log.i(TAG, "Fetching user info");
                int what = WHAT_FINALIZE;
                try {
                    URL url = new URL(API_URL + "/users/" + mSession.getId() + "/?access_token=" + mAccessToken);
                    Log.d(TAG, "Opening URL " + url.toString());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);
                    urlConnection.connect();
                    String response = streamToString(urlConnection.getInputStream());
                    System.out.println(response);
                    JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                    String name = jsonObj.getJSONObject("data").getString("full_name");
                    String bio = jsonObj.getJSONObject("data").getString("bio");
                    Log.i(TAG, "Got name: " + name + ", bio [" + bio + "]");
                } catch (Exception ex) {
                    what = WHAT_ERROR;
                    ex.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
            }
        }.start();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_ERROR) {
                mProgress.dismiss();
                if(msg.arg1 == 1) {
                    mListener.onFail("Failed to get access token");
                }
                else if(msg.arg1 == 2) {
                    mListener.onFail("Failed to get user information");
                }
            }
            else if(msg.what == WHAT_FETCH_INFO) {
                fetchUserName();
            }
            else {
                mProgress.dismiss();
                mListener.onSuccess();
            }
        }
    };


    public boolean hasAccessToken() {
        return (mAccessToken == null) ? false : true;
    }
    public void setListener(OAuthAuthenticationListener listener) {
        mListener = listener;
    }
    public String getUserName() {
        return mSession.getUsername();
    }
    public String getId() {
        return mSession.getId();
    }
    public String getName() {
        return mSession.getName();
    }
    public void authorize() {
        //Intent webAuthIntent = new Intent(Intent.ACTION_VIEW);
        //webAuthIntent.setData(Uri.parse(AUTH_URL));
        //mCtx.startActivity(webAuthIntent);
        mDialog.show();
    }
    private String streamToString(InputStream is) throws IOException {
        String str = "";
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            } finally {
                is.close();
            }
            str = sb.toString();
        }
        return str;
    }

    public void resetAccessToken() {
        if (mAccessToken != null) {
            mSession.resetAccessToken();
            mAccessToken = null;
        }
    }

    public interface OAuthAuthenticationListener {
        public abstract void onSuccess();
        public abstract void onFail(String error);
    }


    private class InstaSession {

        private SharedPreferences sharedPref;
        private Editor editor;

        private static final String SHARED = "Instagram_Preferences";
        private static final String API_USERNAME = "username";
        private static final String API_ID = "id";
        private static final String API_NAME = "name";
        private static final String API_ACCESS_TOKEN = "access_token";

        public InstaSession(Context context) {
            sharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
            editor = sharedPref.edit();
        }

        public void storeAccessToken(String accessToken, String id, String username, String name) {
            editor.putString(API_ID, id);
            editor.putString(API_NAME, name);
            editor.putString(API_ACCESS_TOKEN, accessToken);
            editor.putString(API_USERNAME, username);
            editor.commit();
        }

        public void storeAccessToken(String accessToken) {
            editor.putString(API_ACCESS_TOKEN, accessToken);
            editor.commit();
        }

        /**
         * Reset access token and user name
         */
        public void resetAccessToken() {
            editor.putString(API_ID, null);
            editor.putString(API_NAME, null);
            editor.putString(API_ACCESS_TOKEN, null);
            editor.putString(API_USERNAME, null);
            editor.commit();
        }

        /**
         * Get user name
         *
         * @return User name
         */
        public String getUsername() {
            return sharedPref.getString(API_USERNAME, null);
        }
        /**
         *
         * @return
         */
        public String getId() {
            return sharedPref.getString(API_ID, null);
        }
        /**
         *
         * @return
         */
        public String getName() {
            return sharedPref.getString(API_NAME, null);
        }

        /**
         * Get access token
         *
         * @return Access token
         */
        public String getAccessToken() {
            return sharedPref.getString(API_ACCESS_TOKEN, null);
        }
    }
}