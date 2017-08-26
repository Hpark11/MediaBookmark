package hpark.instagramfollowers_prototype.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import hpark.instagramfollowers_prototype.R;
import hpark.instagramfollowers_prototype.model.ImageItem;

/**
 * Created by hpark_ipl on 2017. 8. 26..
 */

public class ImageItemAdapter extends RecyclerView.Adapter {
    private Context context;
    private ArrayList<ImageItem> mItems; // Allows to remember the last item shown on screen
    private int lastPosition = -1;

    public ImageItemAdapter(ArrayList<ImageItem> items, Context mContext) {
        mItems = items; context = mContext;
    }

    // 필수로 Generate 되어야 하는 메소드 1 : 새로운 뷰 생성
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { // 새로운 뷰를 만든다
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cardview,parent,false);
        ViewHolder holder = new ViewHolder(v); return holder;
    }

    // 필수로 Generate 되어야 하는 메소드 2 : ListView의 getView 부분을 담당하는 메소드


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).imageView.setImageResource(mItems.get(position).getImage());
        ((ViewHolder)holder).textView.setText(mItems.get(position).getImageTitle());
        setAnimation(((ViewHolder)holder).imageView, position);
    }

//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        //super.onBindViewHolder(holder, position);
//        holder.imageView.setImageResource(mItems.get(position).getImage());
//        holder.textView.setText(mItems.get(position).getImageTitle());
//        setAnimation(holder.imageView, position);
//    }

    // 필수로 Generate 되어야 하는 메소드 3
    @Override public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;
        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.image);
            textView = (TextView) view.findViewById(R.id.imagetitle);
        }
    }

    private void setAnimation(View viewToAnimate, int position) { // 새로 보여지는 뷰라면 애니메이션을 해줍니다
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
