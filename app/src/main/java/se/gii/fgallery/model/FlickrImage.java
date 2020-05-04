package se.gii.fgallery.model;

import android.graphics.Bitmap;

/**
 * Represents one flickr image
 */
public class FlickrImage {
    private String URL;
    private String title;
    private Bitmap bitmap;

    FlickrImage(String URL, String title, Bitmap bitmap) {
        this.URL = URL;
        this.title = title;
        this.bitmap = bitmap;
    }

    public String getURL() {
        return URL;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getBitmap() { return bitmap; }
}
