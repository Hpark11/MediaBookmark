package hpark.instagramfollowers_prototype.util;

/**
 * Created by hpark_ipl on 2017. 7. 4..
 */

public class Constants {
    public static final String CLIENT_ID = "21cf0c6cda8b45ac80366132b356f92a";
    public static final String CLIENT_SECRET = "b7e5212b14e046ebbf34a657b8589b90";
    public static final String CALLBACK_URL = "http://sknbiz.com/";
    //instagram://connect";//http://www.google.com";//"instagram://connect";

    public static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    public static final String TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    public static final String API_URL = "https://api.instagram.com/v1";
    public static final String USER_URL = "https://api.instagram.com/v1/users/";

    public static final String TAG_DATA = "data";
    public static final String TAG_ID = "id";
    public static final String TAG_PROFILE_PICTURE = "profile_picture";
    public static final String TAG_USERNAME = "username";
    public static final String TAG_BIO = "bio";
    public static final String TAG_WEBSITE = "website";
    public static final String TAG_COUNTS = "counts";
    public static final String TAG_FOLLOWS = "follows";
    public static final String TAG_FOLLOWED_BY = "followed_by";
    public static final String TAG_MEDIA = "media";
    public static final String TAG_FULL_NAME = "full_name";
    public static final String TAG_META = "meta";
    public static final String TAG_CODE = "code";

    public static final String TAG_SHARE_GROUP_NAME = "share_group_name";
    public static final String TAG_SHARE_GROUP_PICTURE = "share_group_picture";

    public static final String TAG_OUTGOING_STATUS = "outgoing_status";
    public static final String TAG_INCOMING_STATUS = "incoming_status";

    public static final String STATUS_FOLLOWS = "follows";
    public static final String STATUS_REQUESTED = "requested";
    public static final String STATUS_NONE = "none";
    public static final String STATUS_FOLLOWED_BY = "followed_by";
    public static final String STATUS_REQUESTED_BY = "requested_by";
    public static final String STATUS_BLOCKED_BY_YOU = "blocked_by_you";

    public static final int REL_EACH_OTHER = 5;
    public static final int REL_UNFOLLOWED_BY = 6;
    public static final int REL_ONLY_FOLLOWED_BY = 7;
    public static final int REL_BLOCKED_BY = 8;

    public static int WHAT_FINALIZE = 0;
    public static int WHAT_ERROR = 1;
}
