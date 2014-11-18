package com.larefri;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageButton;
import android.widget.ImageView;

class BitmapWorkerTask extends AsyncTask<File, Void, Bitmap> {
    private final WeakReference<ImageButton> imageButtonReference;
    private File data;

    static class AsyncRecyclingDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        private int mCacheRefCount = 0;
        private int mDisplayRefCount = 0;
        private boolean mHasBeenDisplayed;       

        public AsyncRecyclingDrawable(Resources res, Bitmap bitmap,  BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
        
        /**
         * Notify the drawable that the displayed state has changed. Internally a
         * count is kept so that the drawable knows when it is no longer being
         * displayed.
         *
         * @param isDisplayed - Whether the drawable is being displayed or not
         */
        public void setIsDisplayed(boolean isDisplayed) {
            //BEGIN_INCLUDE(set_is_displayed)
            synchronized (this) {
                if (isDisplayed) {
                    mDisplayRefCount++;
                    mHasBeenDisplayed = true;
                } else {
                    mDisplayRefCount--;
                }
            }

            // Check to see if recycle() can be called
            checkState();
            //END_INCLUDE(set_is_displayed)
        }

        /**
         * Notify the drawable that the cache state has changed. Internally a count
         * is kept so that the drawable knows when it is no longer being cached.
         *
         * @param isCached - Whether the drawable is being cached or not
         */
        public void setIsCached(boolean isCached) {
            //BEGIN_INCLUDE(set_is_cached)
            synchronized (this) {
                if (isCached) {
                    mCacheRefCount++;
                } else {
                    mCacheRefCount--;
                }
            }

            // Check to see if recycle() can be called
            checkState();
            //END_INCLUDE(set_is_cached)
        }

        private synchronized void checkState() {
            //BEGIN_INCLUDE(check_state)
            // If the drawable cache and display ref counts = 0, and this drawable
            // has been displayed, then recycle
            if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed && hasValidBitmap()) {
            	getBitmap().recycle();
            }
            //END_INCLUDE(check_state)
        }

        private synchronized boolean hasValidBitmap() {
            Bitmap bitmap = getBitmap();
            return bitmap != null && !bitmap.isRecycled();
        }
    }
    
    public BitmapWorkerTask(ImageView tmp_imageButtom, MainActivity master) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
    	imageButtonReference = new WeakReference<ImageButton>((ImageButton) tmp_imageButtom);
    }

    // Decode image in background.
    protected Bitmap doInBackground(File... params) {
        setData(params[0]);
        Bitmap bitmap = LoadFridgeMagnetsFromFileTask.decodeSampledBitmapFromResource(getData());
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
    	if (isCancelled()) {
            bitmap = null;
        }

        if (imageButtonReference != null && bitmap != null) {
            final ImageButton imageButton = imageButtonReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    LoadFridgeMagnetsFromFileTask.getBitmapWorkerTask(imageButton);
            if (this == bitmapWorkerTask && imageButton != null) {
            	imageButton.setImageBitmap(bitmap);
            }
        }
    }

	public File getData() {
		return data;
	}

	public void setData(File data) {
		this.data = data;
	}
}