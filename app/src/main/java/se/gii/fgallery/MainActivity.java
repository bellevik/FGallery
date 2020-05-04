package se.gii.fgallery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import se.gii.fgallery.adapter.GridViewAdapter;
import se.gii.fgallery.model.FlickrApi;
import se.gii.fgallery.model.FlickrImage;
import se.gii.fgallery.model.FlickrSearchResponse;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Initialized in `onCreate`
     */
    SearchView searchView;
    GridView gridView;
    GridViewAdapter gridViewAdapter;
    FlickrApi flickrApi;
    Resources r;

    ArrayList<FlickrImage> imageArrayList = new ArrayList<FlickrImage>();
    String[] currentSearchTags = null;
    int currentSearchPage = 0;

    /**
     * Number of columns in thumbnail grid
     */
    int columns = 3;

    /**
     * Handler for the Flickr API response
     */
    public Handler flickrResponseHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message message) {
            FlickrSearchResponse flickrResponse = (FlickrSearchResponse) message.obj;
            if (message.what == FlickrApi.MSG_ID_COMPLETE) {
                if (currentSearchPage == 0) {
                    // Jump to top of grid when keywords are changed
                    imageArrayList = new ArrayList<FlickrImage>();
                    gridView.setSelection(0);
                }

                FlickrImage[] images = flickrResponse.getImages();
                if (images.length > 0) {
                    Collections.addAll(imageArrayList, images);
                    gridViewAdapter.update(imageArrayList);
                } else {
                    if (currentSearchPage == 0) {
                        Toast.makeText(MainActivity.this, r.getString(R.string.no_results), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, r.getString(R.string.no_more_results), Toast.LENGTH_LONG).show();
                    }
                }
            } else if (message.what == FlickrApi.MSG_ID_FAIL) {
                Toast.makeText(MainActivity.this, flickrResponse.getError(), Toast.LENGTH_LONG).show();
            }
        }
    };

    /**
     * Create activity, search and grid. Calculate thumbnail sizes depending on screen width
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = getResources();

        flickrApi = new FlickrApi(r.getString(R.string.api_key), r);

        searchView = (SearchView) findViewById(R.id.searchView);
        gridView = (GridView) findViewById(R.id.gridView);

        // Find horizontal padding for layout
        LinearLayout layout = findViewById(R.id.layout);
        int layoutPaddingH = layout.getPaddingLeft() + layout.getPaddingRight();

        // Calculate thumbnail size and grid size depending on number of columns and padding sized
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
        int columnWidth = (int) ((getScreenWidth() - ((columns + 1) * padding) - layoutPaddingH) / columns);
        gridView.setNumColumns(columns);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gridView.setPadding((int) padding, (int) padding, (int) padding, (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);

        // Initialize the grid view adapter for the thumbnails
        gridViewAdapter = new GridViewAdapter(MainActivity.this, imageArrayList, columnWidth);
        gridView.setAdapter(gridViewAdapter);

        // Listen to search field changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchTags = query.split("\\s*,\\s*");
                currentSearchPage = 0;

                flickrApi.search(currentSearchTags, flickrResponseHandler, currentSearchPage);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Listen to grid scrolling
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Check if we're at the bottom of the grid. If we are - attempt to load more thumbnails
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount) {
                    flickrApi.search(currentSearchTags, flickrResponseHandler, ++currentSearchPage);
                }
            }
        });
    }

    /**
     * Helper method to find device screen width
     *
     * @return int screen width in pixels or fallback 320 if unable to calculate
     */
    private int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return 320;

        Display display = wm.getDefaultDisplay();
        final Point point = new Point();

        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) {
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
}
