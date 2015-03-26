/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.demo.photoframe.image;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Used to asynchronously decode {@link Bitmap} from files and show 
 * resulting {@link Bitmap} via {@link LoadingImageView}.
 * Handles {@link Bitmap} caching in {@link MemoryCache} and {@link FileCache}.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    MemoryCache mMemoryCache = new MemoryCache();

    FileCache mFileCache;

    private Map<LoadingImageView, ImageKey> imageViews = Collections
            .synchronizedMap(new WeakHashMap<LoadingImageView, ImageKey>());

    private ExecutorService mExecutorService;

    public ImageLoader(Context context) {
        mFileCache = new FileCache(context);
        mExecutorService = Executors.newFixedThreadPool(2);
    }

    public void loadImage(String path, LoadingImageView imageView, ImageType type) {
        ImageKey key = new ImageKey(type, path);
        imageViews.put(imageView, key);
        Bitmap bitmap = mMemoryCache.get(key);
        if (bitmap != null) {
            imageView.showBitmap(bitmap);
        } else {
            queuePhoto(key, imageView);
            imageView.setLoading();
        }
    }

    private void queuePhoto(ImageKey key, LoadingImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(key, imageView);
        mExecutorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(ImageKey key) {
        File cacheFile = mFileCache.getFile(key);
        Bitmap b = decodeFile(cacheFile, key.type);
        if (b != null) {
            return b;
        }
        try {
            Bitmap bitmap = null;
            File imageFile = new File(key.path);
            bitmap = decodeFile(imageFile, key.type);
            FileOutputStream fos = new FileOutputStream(cacheFile);
            bitmap.compress(CompressFormat.JPEG, 80, fos);
            fos.close();
            return bitmap;
        } catch (Throwable ex) {
            Log.e(TAG, "Unable to load bitmap!", ex);
            if (ex instanceof OutOfMemoryError) {
                mMemoryCache.clear();
            }
            return null;
        }
    }

    private Bitmap decodeFile(File f, ImageType type) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            FileDescriptor fd = fis.getFD();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fd, null, options);

            int w = options.outWidth;
            int h = options.outHeight;
            
            float scale = (float) type.targetSize / Math.max(w, h);
            
            if (type == ImageType.SCREENAIL) {
                options.inSampleSize = computeSampleSizeLarger(scale);
            } else {
                options.inSampleSize = computeSampleSize(scale);
            }
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (Exception e) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {}
            }
        }
        return null;
    }
    
    private static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    private static int prevPowerOf2(int n) {
        return nextPowerOf2(n) / 2;
    }
    
    private static int computeSampleSizeLarger(float scale) {
        int initialSize = (int) Math.floor(1f / scale);        
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }

    private static int computeSampleSize(float scale) {
        int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
        return initialSize <= 8
                ? nextPowerOf2(initialSize)
                : (initialSize + 7) / 8 * 8;
    }

    private class PhotoToLoad {
        public ImageKey key;
        public LoadingImageView imageView;

        public PhotoToLoad(ImageKey key, LoadingImageView i) {
            this.key = key;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }
            Bitmap bmp = getBitmap(photoToLoad.key);
            mMemoryCache.put(photoToLoad.key, bmp);
            if (imageViewReused(photoToLoad)) {
                return;
            }
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        ImageKey key = imageViews.get(photoToLoad.imageView);
        if (key == null || !key.equals(photoToLoad.key)) {
            return true;
        }
        return false;
    }

    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad)) {
                return;
            }
            if (bitmap != null) {
                photoToLoad.imageView.showBitmap(bitmap);
            } else {
                photoToLoad.imageView.showFailedBitmap();
            }
        }
    }

    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

    public static enum ImageType {

        THUMBNAIL(128), SCREENAIL(512);

        int targetSize;

        ImageType(int targetSize) {
            this.targetSize = targetSize;
        }

    }

    public static class ImageKey {
        ImageType type;
        String path;

        public ImageKey(ImageType type, String path) {
            this.type = type;
            this.path = path;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ImageKey other = (ImageKey) obj;
            if (type != other.type)
                return false;
            if (path == null) {
                if (other.path != null)
                    return false;
            } else if (!path.equals(other.path))
                return false;
            return true;
        }

    }

}
