package hpark.instagramfollowers_prototype.model;

/**
 * Created by hpark_ipl on 2017. 8. 26..
 */

public class ImageItem {
    String imageUrl;
    String imageDesc;

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageDesc() {
        return imageDesc;
    }

    public ImageItem(String imageUrl, String imageDesc) {
        this.imageUrl=imageUrl;
        this.imageDesc=imageDesc;
    }
}
