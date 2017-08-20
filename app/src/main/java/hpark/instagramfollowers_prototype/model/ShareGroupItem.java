package hpark.instagramfollowers_prototype.model;

import java.util.ArrayList;

/**
 * Created by hpark_ipl on 2017. 8. 14..
 */

public class ShareGroupItem {
    private int id;
    private String shareGroupName;
    private ArrayList<String> userInfoList = new ArrayList<>();

    public ShareGroupItem(int id, String name, String infoList) {
        this.id = id;
        this.shareGroupName = name;
        loadUserInfoList(infoList);
    }

    private void loadUserInfoList(String infoList) {
        String[] list = infoList.split(":");
        for(String str: list) {
            userInfoList.add(str);
        }
    }

    public int getShareGroupId() {
        return id;
    }

    public String getShareGroupName() {
        return shareGroupName;
    }

    public ArrayList<String> getShareGroupUsersInfoList() {
        return userInfoList;
    }
}
