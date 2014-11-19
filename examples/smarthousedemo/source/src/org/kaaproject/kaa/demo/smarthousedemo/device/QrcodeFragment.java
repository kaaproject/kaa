/*
 * Copyright 2014 CyberVision, Inc.
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
package org.kaaproject.kaa.demo.smarthousedemo.device;

import org.kaaproject.kaa.demo.qrcode.FinishListener;
import org.kaaproject.kaa.demo.qrcode.encode.QRCodeEncoder;
import org.kaaproject.kaa.demo.smarthousedemo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

public class QrcodeFragment extends Fragment {
    
    private static final String TAG = QrcodeFragment.class.getSimpleName();

    public static final String QR_CONTENTS = "qr_contents";
    
    public static final String QR_CONTENTS_DESCRIPTION = "qr_contents_description";
    
    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static QrcodeFragment newInstance(String qrcontens, String contentsDesc) {
        QrcodeFragment fragment = new QrcodeFragment();
        Bundle args = new Bundle();
        args.putString(QR_CONTENTS, qrcontens);
        args.putString(QR_CONTENTS_DESCRIPTION, contentsDesc);
        fragment.setArguments(args);
        return fragment;
    }
    
    private QRCodeEncoder qrCodeEncoder;

    public QrcodeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_qrcode, container,
                false);

        Bundle args = this.getArguments();  
        String contents = args.getString(QR_CONTENTS);
        String contentsDesc = args.getString(QR_CONTENTS_DESCRIPTION);

        WindowManager manager = (WindowManager) getActivity().getSystemService(Activity.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 7 / 8;
        
        qrCodeEncoder = new QRCodeEncoder(contents, smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            if (bitmap == null) {
                Log.w(TAG, "Could not encode barcode");
                showErrorMessage(R.string.msg_encode_contents_failed);
                qrCodeEncoder = null;
                return rootView;
            }
            ImageView view = (ImageView) rootView.findViewById(R.id.image_view);
            view.setImageBitmap(bitmap);
            TextView contentsView = (TextView) rootView.findViewById(R.id.contents_text_view);
            contentsView.setText(contentsDesc);
        }
        catch (WriterException e) {
            Log.w(TAG, "Could not encode barcode", e);
            showErrorMessage(R.string.msg_encode_contents_failed);
            qrCodeEncoder = null;
        }
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    private void showErrorMessage(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton(R.string.button_ok, new FinishListener(getActivity()));
        builder.setOnCancelListener(new FinishListener(getActivity()));
        builder.show();
    }
}
