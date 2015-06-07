package org.gplvote.trustnet;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class ATrustMe extends ActionBarActivity {
    DataPersonalInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_me);

        Settings settings = Settings.getInstance(this);
        info = settings.getPersonalInfo();

        show_qr_code();
    }

    public void show_qr_code() {
        if (info == null) {return;}

        if (info.personal_id == null || info.personal_id.isEmpty()) {return;}

        DataTrustInfo tr = new DataTrustInfo();

        tr.nm = info.name;
        tr.pid = info.personal_id;

        Gson gson = new Gson();
        String json_qr_data = gson.toJson(tr);

        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = null;
        try {
            Hashtable hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            matrix = writer.encode(AMain.TRUSTNET_INT_URL_TRUST + json_qr_data, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }

        Bitmap bmp = Bitmap.createBitmap(smallerDimension, smallerDimension, Bitmap.Config.RGB_565);
        for (int x = 0; x < smallerDimension; x++){
            for (int y = 0; y < smallerDimension; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        ImageView imgQRCode = (ImageView) findViewById(R.id.imgQRCode);
        imgQRCode.setImageBitmap(bmp);
    }
}
