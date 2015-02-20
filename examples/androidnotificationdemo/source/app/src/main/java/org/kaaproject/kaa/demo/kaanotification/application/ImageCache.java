package org.kaaproject.kaa.demo.kaanotification.application;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.kaaproject.www.kaanotification.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.WeakHashMap;


public class ImageCache {

    private WeakHashMap<String, Bitmap> imageMap;

    public static final ImageCache cache = new ImageCache();

    private ImageCache() {
        imageMap = new WeakHashMap();
        Bitmap bmp = BitmapFactory.decodeResource(KaaNotificationApp.getContext().getResources(), R.drawable.default_image);
        imageMap.put("default", bmp);
    }

    public Bitmap getImage(String imageUrl) {
        Bitmap bmp;

        if (!imageMap.containsKey(imageUrl)) {
            try {
                URL url = new URL(imageUrl);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                imageMap.put(imageUrl, bmp);
            } catch (MalformedURLException e) {
                bmp = imageMap.get("default");
                Log.e("KAA-ERR", e.getMessage());
            } catch (IOException e) {
                bmp = imageMap.get("default");
                Log.e("KAA-ERR", e.getMessage());
            }
        } else return imageMap.get(imageUrl);
        return bmp;
    }


}
