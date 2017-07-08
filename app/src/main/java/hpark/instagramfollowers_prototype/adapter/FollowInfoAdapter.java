package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.activity.FollowInfoActivity;
import hpark.instagramfollowers_prototype.util.Constants;
import hpark.instagramfollowers_prototype.util.ImageManager;

/**
 * Created by hpark_ipl on 2017. 7. 5..
 */

public class FollowInfoAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> usersInfo;
    private LayoutInflater inflater;
    private ImageManager imageManager;
    private int relationship = -1;


    public FollowInfoAdapter(Context context, ArrayList<HashMap<String, String>> usersInfo, final int relationship) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.usersInfo = usersInfo;
        this.imageManager = new ImageManager(context);
        this.relationship = relationship;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.inflator_followinfo, null);

        Holder holder = new Holder();
        holder.userImageView = (ImageView) view.findViewById(R.id.userImageView);
        holder.userIdTextView = (TextView) view.findViewById(R.id.userIdTextView);
        holder.followButton = (Button) view.findViewById(R.id.followButton);

        holder.followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Constants.USER_URL;

                switch (relationship) {
                    case Constants.REL_EACH_OTHER:
                        break;
                    case Constants.REL_ONLY_FOLLOWED_BY:
                        break;
                    case Constants.REL_UNFOLLOWED_BY:
                        break;
                    case Constants.REL_BLOCKED_BY:
                        break;
                    default:
                        break;
                }
            }
        });

        holder.userIdTextView.setText(usersInfo.get(position).get(Constants.TAG_USERNAME));
        imageManager.displayImage(usersInfo.get(position).get(Constants.TAG_PROFILE_PICTURE), holder.userImageView);
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
