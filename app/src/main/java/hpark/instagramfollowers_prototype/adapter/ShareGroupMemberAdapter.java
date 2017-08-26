package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.activity.AddNewShareGroupActivity;
import hpark.instagramfollowers_prototype.util.Constants;


/**
 * Created by hpark_ipl on 2017. 8. 14..
 */

public class ShareGroupMemberAdapter extends BaseAdapter {

    private ArrayList<HashMap<String, String>> usersInfo;
    private LayoutInflater inflater;
    private Context context;

    public ShareGroupMemberAdapter(Context context, ArrayList<HashMap<String, String>> usersInfo) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.usersInfo = usersInfo;
    }

    @Override
    public int getCount() {
        return usersInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.inflator_add_share_group, null);

        final int pos = position;
        final Holder holder = new Holder();
        holder.userImageView = (ImageView) view.findViewById(R.id.userImageView);
        holder.userIdTextView = (TextView) view.findViewById(R.id.userIdTextView);
        holder.shareGroupMemberCheckbox = (CheckBox) view.findViewById(R.id.shareGroupMemberCheckbox);

        Picasso.with(context).load(usersInfo.get(position).get(Constants.TAG_PROFILE_PICTURE))
                .placeholder(R.drawable.placeholder).into(holder.userImageView);

        holder.userIdTextView.setText(usersInfo.get(position).get(Constants.TAG_USERNAME));

        if (usersInfo.get(pos).get("isChecked") == "yes") {
            holder.shareGroupMemberCheckbox.setChecked(true);
        } else {
            holder.shareGroupMemberCheckbox.setChecked(false);
        }

        holder.shareGroupMemberCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (context instanceof AddNewShareGroupActivity) {
                    ((OnAddSharegroupMemberCheckedListener)context)
                            .onAddSharegroupMemberCheckedListener(usersInfo.get(pos).get(Constants.TAG_ID) +"|" +
                                    usersInfo.get(pos).get(Constants.TAG_USERNAME), isChecked, pos);
                }
            }
        });
        return view;
    }

    private class Holder {
        private ImageView userImageView;
        private TextView userIdTextView;
        private CheckBox shareGroupMemberCheckbox;
    }

    public interface OnAddSharegroupMemberCheckedListener {
        public void onAddSharegroupMemberCheckedListener(String newMember, boolean isChecked, int pos);
    }
}
