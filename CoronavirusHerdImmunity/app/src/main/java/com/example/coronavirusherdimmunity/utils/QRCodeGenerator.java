package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import static android.content.Context.WINDOW_SERVICE;


public class QRCodeGenerator {

    private Context mContext;
    private String TAG = "GenerateQRCode";
    private String inputValue;
    private Bitmap bitmap;
    private QRGEncoder qrgEncoder;

    public QRCodeGenerator(Context mContext) {
        this.mContext = mContext;
    }

    public void generateQRCode(Long deviceId, ImageView qrImage) {

        inputValue = "covid-outbreak-control:" + Long.toString(deviceId);

        WindowManager manager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;
        int dimension = (Math.min(width, height)) / 2;


        qrgEncoder = new QRGEncoder(inputValue, null, QRGContents.Type.TEXT, dimension);

        qrgEncoder.setColorWhite(0xF9F9F9);

        // Getting QR-Code as Bitmap
        bitmap = qrgEncoder.getBitmap();
        // Setting Bitmap to ImageView
        qrImage.setImageBitmap(bitmap);
    }

}
