package se.gii.fgallery.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import se.gii.fgallery.model.FlickrImage;

/**
 * Grid view adapter to handle flickr thumbnails
 */
public class GridViewAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<FlickrImage> flickrImages = new ArrayList<FlickrImage>();
    private int imageWidth;

    /**
     *
     * @param activity Activity to where this adapter instance belongs
     * @param images ArrayList<FlickrImage> Flicker Images in grid
     * @param imageWidth int thumbnail width
     */
    public GridViewAdapter(Activity activity, ArrayList<FlickrImage> images, int imageWidth) {
        this.activity = activity;
        this.flickrImages = images;
        this.imageWidth = imageWidth;
    }

    /**
     * Update grid with new set of flickr images (thumbnails)
     * @param images ArrayList<FlickrImage> to replace the old set
     */
    public void update(ArrayList<FlickrImage> images) {
        this.flickrImages = images;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.flickrImages.size();
    }

    @Override
    public Object getItem(int position) {
        return this.flickrImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(activity);
        } else {
            imageView = (ImageView) convertView;
        }

        // Get screen dimensions
        Bitmap image = flickrImages.get(position).getBitmap();

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(imageWidth,
                imageWidth));
        imageView.setImageBitmap(image);

        // Image view click listener
        imageView.setOnClickListener(new OnImageClickListener(position));

        return imageView;
    }

    /**
     * Inner class to handle clicks on thumbnail (TODO)
     */
    class OnImageClickListener implements OnClickListener {

        int position;

        // constructor
        public OnImageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            // TODO: Show image full screen activity
        }

    }
}
