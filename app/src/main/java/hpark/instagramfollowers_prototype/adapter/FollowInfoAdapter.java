package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.api.HttpRequestManager;
import hpark.instagramfollowers_prototype.api.InstaSession;
import hpark.instagramfollowers_prototype.util.Constants;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */

public class FollowInfoAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> usersInfo;
    private LayoutInflater inflater;
    private int relationship = -1;
    private InstaSession instaSession;
    private Context mContext;

    public FollowInfoAdapter(Context context, ArrayList<HashMap<String, String>> usersInfo, final int relationship) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.usersInfo = usersInfo;
        this.relationship = relationship;
        this.mContext = context;
        instaSession = new InstaSession(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.inflator_followinfo, null);

        final Holder holder = new Holder();
        holder.userImageView = (ImageView) view.findViewById(R.id.userImageView);
        holder.userIdTextView = (TextView) view.findViewById(R.id.userIdTextView);
        holder.followButton = (Button) view.findViewById(R.id.followButton);

        String url = Constants.USER_URL;
        HashMap<String, String> hash = new HashMap<String, String>();

        switch (relationship) {
            case Constants.REL_EACH_OTHER:
                holder.followButton.setText("언팔로우");
                url = url + usersInfo.get(position).get(Constants.TAG_ID) + "/relationship?access_token=" + instaSession.getAccessToken();
                hash.put("action","unfollow");
                break;
            case Constants.REL_ONLY_FOLLOWED_BY:
                holder.followButton.setText("팔로우");
                url = url + usersInfo.get(position).get(Constants.TAG_ID) + "/relationship?access_token=" + instaSession.getAccessToken();
                hash.put("action","follow");
                break;
            case Constants.REL_UNFOLLOWED_BY:
                holder.followButton.setText("언팔로우");
                url = url + usersInfo.get(position).get(Constants.TAG_ID) + "/relationship?access_token=" + instaSession.getAccessToken();
                hash.put("action","unfollow");
                break;
            case Constants.REL_BLOCKED_BY:
                holder.followButton.setText("차단하기");
                url = url + usersInfo.get(position).get(Constants.TAG_ID) + "/relationship?access_token=" + instaSession.getAccessToken();
                break;
            default:
                break;
        }

        final String reqUrl = url;
        final HashMap<String, String> postDataParams = hash;

        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpRequestManager httpRequestManager = new HttpRequestManager();
                            JSONObject jsonObject = httpRequestManager.acquireJsonwithPostRequest(reqUrl, postDataParams);
                            JSONObject meta = jsonObject.getJSONObject(Constants.TAG_META);

                            if(meta.getInt(Constants.TAG_CODE) == 200) {
                                usersInfo.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(mContext, "요청완료", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                }).start();
                holder.followButton.setText("요청완료");
                holder.followButton.setEnabled(false);
            }
        });

        holder.userIdTextView.setText(usersInfo.get(position).get(Constants.TAG_USERNAME));

        Picasso.with(mContext).load(usersInfo.get(position).get(Constants.TAG_PROFILE_PICTURE))
                .placeholder(R.drawable.placeholder)
                .into(holder.userImageView);
        return view;
    }

    private class Holder {
        private ImageView userImageView;
        private TextView userIdTextView;
        private Button followButton;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return usersInfo.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
}
