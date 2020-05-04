package se.gii.fgallery.model;

/**
 * Search Response from flickr api
 */
public class FlickrSearchResponse {
    private String error = null;
    private FlickrImage[] images = null;

    public void setError (String error) {
        this.error = error;
    }

    public String getError() {
        return this.error == null ? "" : this.error;
    }

    public FlickrImage[] getImages() {
        return images;
    }

    public void setImages(FlickrImage[] images) {
        this.images = images;
    }
}
