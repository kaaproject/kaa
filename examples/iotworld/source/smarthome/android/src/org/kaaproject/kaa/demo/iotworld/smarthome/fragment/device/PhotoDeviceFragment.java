package org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.kaaproject.kaa.demo.iotworld.photo.PhotoAlbumInfo;
import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.data.PhotoDevice;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.photo.PhotoAlbumsAdapter;
import org.kaaproject.kaa.demo.iotworld.smarthome.fragment.device.photo.PhotoAlbumsAdapter.PhotoAlbumSelectionListener;
import org.kaaproject.kaa.demo.iotworld.smarthome.widget.AutoSpanRecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PhotoDeviceFragment extends AbstractGeoFencingDeviceFragment<PhotoDevice> 
implements PhotoAlbumSelectionListener {
    
    private static final int REQUEST_SELECT_IMAGE = 3005;
    
    private TextView mNoDataText;
    private AutoSpanRecyclerView mRecyclerView;
    private PhotoAlbumsAdapter mPhotoAlbumsAdapter;
    
    public PhotoDeviceFragment() {
        super();
    }

    public PhotoDeviceFragment(String endpointKey) {
        super(endpointKey);
    }

    @Override
    protected int getDeviceLayout() {
        return R.layout.fragment_photo_device;
    }

    @Override
    public String getFragmentTag() {
        return PhotoDeviceFragment.class.getSimpleName();
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo, menu);        
        MenuItem item = menu.findItem(R.id.action_upload_photo);
        View actionView = MenuItemCompat.getActionView(item);
        if (actionView != null) {
            Button button = (Button)actionView.findViewById(R.id.uploadPhotoButton);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadPhoto();
                }
            });
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_photo:
                uploadPhoto();
                return true;
            case R.id.delete_uploaded_photos:
                mDevice.deleteUploadedPhotos();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void setupView(LayoutInflater inflater, View rootView) {
        super.setupView(inflater, rootView);
        
        mNoDataText = (TextView) rootView.findViewById(R.id.noDataText);
        
        mRecyclerView = (AutoSpanRecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        
        int cardsWidth = getResources().getDimensionPixelSize(R.dimen.photo_album_card_width);
        int cardsSpacing = getResources().getDimensionPixelSize(R.dimen.card_spacing);
        mRecyclerView.setGridLayoutManager(GridLayoutManager.VERTICAL, 1, cardsWidth, cardsSpacing);
        mPhotoAlbumsAdapter = new PhotoAlbumsAdapter(mRecyclerView, mDevice, this);
    }
    
    @Override
    protected void bindDevice(boolean firstLoad) {
        super.bindDevice(firstLoad);
        
        mPhotoAlbumsAdapter.notifyDataSetChanged();
        if (mPhotoAlbumsAdapter.getItemCount() > 0) {
            mNoDataText.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mNoDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPhotoAlbumSelected(PhotoAlbumInfo album) {
        mDevice.startStopSlideshow(album.getId());
    }
    
    private void uploadPhoto() {
        Intent intent = new Intent(
        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && 
                resultCode == Activity.RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] columns = { MediaStore.Images.Media.DATA};
 
            Cursor cursor = mActivity.getContentResolver().query(selectedImage,
                    columns, null, null, null);
            cursor.moveToFirst();
 
            int columnIndex = cursor.getColumnIndex(columns[0]);
            String imagePath = cursor.getString(columnIndex);
            cursor.close();
            
            String imageFileName = null;
            byte[] imageData = null;
            
            try {
                File f = new File(imagePath);
                imageFileName = f.getName();
                FileInputStream fis = new FileInputStream(imagePath);
                imageData = new byte[fis.available()];
                fis.read(imageData);
                fis.close();
            } catch (IOException e) {
                Log.e(getFragmentTag(), "Unable to read image from path: " + imagePath);
            }
            if (imageData != null) {                
                mDevice.uploadPhoto(imageFileName, imageData);
            }
        }
    }
    
    

}
