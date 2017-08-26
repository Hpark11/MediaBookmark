package hpark.instagramfollowers_prototype.model;

/**
 * Created by hpark_ipl on 2017. 8. 26..
 */

public class ImageItem {
    int image;
    String imageTitle;

    public int getImage() {
        return image;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public ImageItem(int image, String imageTitle) {
        this.image=image;
        this.imageTitle=imageTitle;
    }
}
