package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.activity.MediaShareGroupActivity;
import hpark.instagramfollowers_prototype.model.ImageItem;

/**
 * Created by hpark_ipl on 2017. 8. 26..
 */

public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ViewHolder> {
    Context context;
    List<ImageItem> items;
    int item_layout;


    public ImageItemAdapter(Context context, List<ImageItem> items, int item_layout) {
        this.context = context;
        this.items = items;
        this.item_layout = item_layout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ImageItem item = items.get(position);

        Picasso.with(context).load(item.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.image);

        holder.title.setText(item.getImageDesc());
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof MediaShareGroupActivity) {
                    ((MediaShareGroupActivity) context).onImageExpand(item.getImageUrl());
                }
            }
        });
    }

    public void updateDataSet(List<ImageItem> items) {
        this.items = items;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        CardView cardview;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            cardview = (CardView) itemView.findViewById(R.id.cardview);
        }
    }

    public interface OnImageExpandListener {
        public void onImageExpand(String imageUrl);
    }
}
