package se.gii.fgallery.model;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.RequiresApi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import se.gii.fgallery.R;

/**
 * Implements (limited parts) of the flickr API, see https://www.flickr.com/services/api/
 *
 */
public class FlickrApi {
    /**
     * ID of successful response callback
     */
    public static int MSG_ID_COMPLETE = 100;

    /**
     * ID of failed response callback
     */
    public static int MSG_ID_FAIL = 101;

    /**
     * Flickr API key
     */
    private String apiKey;

    /**
     * Avoid multiple search requests by UX by debouncing
     */
    private boolean debounce = false;

    /**
     *
     */
    private Resources r;

    /**
     * Constructor
     *
     * @param apiKey String Must contain a valid Flickr api key
     * @param r Resources
     */
    public FlickrApi(String apiKey, Resources r)
    {
        this.apiKey = apiKey;
        this.r = r;
    }

    /**
     * Handler for passing results
     */
    private Handler flickrResultHandler;

    /**
     * API endpoint: flickr.photos.search
     *
     * @param keywords String[] all keywords to search for
     * @param flickrResultHandler Handler handler to pass response to
     * @param offset int offset "page", zero indexed
     */
    public void search(String[] keywords, Handler flickrResultHandler, int offset) {
        if (debounce) return;
        this.flickrResultHandler = flickrResultHandler;
        Thread fetchImagesThread = this.fetchImages(keywords, offset);
        fetchImagesThread.start();
    }

    private Thread fetchImages(final String[] strings, int offset) {
        final int count = 15; // Number of results to fetch from API
        final int page = 1 + offset; // Page number, one based

        return new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                debounce = true;

                FlickrSearchResponse flickrResponse = new FlickrSearchResponse();
                try {
                    URL url = new URL("https://api.flickr.com/services/rest" +
                            "?api_key=" + FlickrApi.this.apiKey +
                            "&method=flickr.photos.search" +
                            "&page=" + page +
                            "&per_page=" + count +
                            "&tags=" + String.join(",", strings));

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    if (conn.getResponseCode() != 200) {
                        throw new IOException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(conn.getInputStream());

                    Element root = document.getDocumentElement();
                    String status = root.getAttribute("stat");
                    if (status.equals("ok")) {
                        NodeList photos = root.getElementsByTagName("photo");

                        // For each photo node in the response, fetch thumbnail bitmap
                        FlickrImage[] flickrImages = new FlickrImage[photos.getLength()];
                        int idx = 0;
                        for (int i = 0; i < photos.getLength(); i++) {
                            Node photo = photos.item(i);
                            if (photo.getNodeType() == Node.ELEMENT_NODE) {
                                Element el = (Element) photo;
                                String imageUrl =
                                        "https://farm" + el.getAttribute("farm") +
                                        ".staticflickr.com/" + el.getAttribute("server") +
                                        "/" + el.getAttribute("id") +
                                        "_" + el.getAttribute("secret") +
                                        "_m.jpg";

                                flickrImages[idx++] = new FlickrImage(
                                        imageUrl,
                                        ((Element) photo).getAttribute("title"),
                                        BitmapFactory.decodeStream(new URL(imageUrl).openConnection().getInputStream())
                                );
                            }
                        }
                        flickrResponse.setImages(flickrImages);
                    } else {
                        flickrResponse.setError(r.getString(R.string.flickr_error)); //TODO: Parse and return Flickr error code and string
                        Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_FAIL, flickrResponse);
                        msg.sendToTarget();
                        return;
                    }

                    conn.disconnect();

                } catch (MalformedURLException malformedURLException) {
                    flickrResponse.setError(r.getString(R.string.flickr_error_malformed));
                    Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_FAIL, flickrResponse);
                    msg.sendToTarget();
                    return;
                } catch (IOException ioException) {
                    flickrResponse.setError(r.getString(R.string.flickr_error_io));
                    Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_FAIL, flickrResponse);
                    msg.sendToTarget();
                    return;
                } catch (ParserConfigurationException parserConfigurationException) {
                    flickrResponse.setError(r.getString(R.string.flickr_error_parser));
                    Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_FAIL, flickrResponse);
                    msg.sendToTarget();
                    return;
                } catch (SAXException saxException) {
                    flickrResponse.setError(r.getString(R.string.flickr_error_sax));
                    Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_FAIL, flickrResponse);
                    msg.sendToTarget();
                    return;
                } finally {
                    debounce = false;
                }

                Message msg = Message.obtain(FlickrApi.this.flickrResultHandler, FlickrApi.MSG_ID_COMPLETE, flickrResponse);
                msg.sendToTarget();
            }
        };
    }
}
