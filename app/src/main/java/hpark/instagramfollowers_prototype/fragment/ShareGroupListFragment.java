package hpark.instagramfollowers_prototype.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;
import hpark.instagramfollowers_prototype.R;

/**
 * Created by hpark_ipl on 2017. 8. 23..
 */

public class ShareGroupListFragment extends Fragment {

    @BindView(R.id.shareGroupListView)
    ListView shareGroupListview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(R.layout.fragment_manage_followers, container, false);
        ButterKnife.bind(this, v);

        return v;
    }
}
