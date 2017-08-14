package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.util.Constants;

/**
 * Created by hpark_ipl on 2017. 8. 12..
 */

public class ShareGroupAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> shareGroups;
    private LayoutInflater inflater;
    private Context context;

    public ShareGroupAdapter(Context context, ArrayList<HashMap<String, String>> shareGroups) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.shareGroups = shareGroups;
        this.context = context;
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
        View view = inflater.inflate(R.layout.inflator_share_group, null);

        final Holder holder = new Holder();
        holder.shareGroupImageView = (ImageView) view.findViewById(R.id.shareGroupImageView);
        holder.shareGroupTextView = (TextView) view.findViewById(R.id.shareGroupTextView);

        holder.shareGroupImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO get info about selected Room
            }
        });

        holder.shareGroupTextView.setText(shareGroups.get(position).get(Constants.TAG_SHARE_GROUP_NAME));
        return null;
    }

    private class Holder {
        private ImageView shareGroupImageView;
        private TextView shareGroupTextView;
    }
}
