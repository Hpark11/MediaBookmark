package hpark.instagramfollowers_prototype.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hpark.instagramfollowers_prototype.R;

/**
 * Created by hpark_ipl on 2017. 8. 23..
 */

public class ManageFollowersFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getActivity()).inflate(R.layout.fragment_manage_followers, container, false);
        return v;
    }
}
