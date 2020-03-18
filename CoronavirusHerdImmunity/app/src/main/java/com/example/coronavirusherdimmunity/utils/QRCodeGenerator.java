package com.example.coronavirusherdimmunity.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import com.google.zxing.WriterException;
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

    public void generateQRCode(int deviceId, ImageView qrImage) {

        inputValue = Integer.toString(deviceId);

        WindowManager manager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;
        int dimension = (Math.min(width, height)) * 2 / 3;


        qrgEncoder = new QRGEncoder(inputValue, null, QRGContents.Type.TEXT, dimension);

        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qrImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.v(TAG, e.toString());
        }
    }

}
