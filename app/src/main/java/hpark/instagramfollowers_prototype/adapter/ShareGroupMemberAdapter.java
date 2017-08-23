package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;


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
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = inflater.inflate(R.layout.inflator_followinfo, null);

        final Holder holder = new Holder();
        holder.userImageView = (ImageView) view.findViewById(R.id.userImageView);
        holder.userIdTextView = (TextView) view.findViewById(R.id.userIdTextView);

        return view;
    }

    private class Holder {
        private ImageView userImageView;
        private TextView userIdTextView;
        private CheckBox shareGroupMemberCheckbox;
    }
}
