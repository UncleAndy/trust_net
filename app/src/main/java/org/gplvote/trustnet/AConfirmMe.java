package org.gplvote.trustnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

public class AConfirmMe extends ActionBarActivity {
    private Settings settings;

    private ImageView imgQRCode;

    DataPersonalInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_me);

        settings = Settings.getInstance(this);
        info = settings.getPersonalInfo();

        show_qr_code();
    }

    public void show_qr_code() {
        if (info == null) {return;}

        if (info.public_key_id == null || info.public_key_id.isEmpty()) {return;}

        DataAttestationInfo att = new DataAttestationInfo();

        att.nm = info.name;
        att.bd = info.birthday;
        att.sn = info.social_number;
        att.tn = info.tax_number;
        att.pkid = info.public_key_id;
        att.pid = info.personal_id;

        Gson gson = new Gson();
        String json_qr_data = gson.toJson(att);

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

            matrix = writer.encode(AMain.TRUSTNET_INT_URL_VERIFICATION + json_qr_data, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }

        Bitmap bmp = Bitmap.createBitmap(smallerDimension, smallerDimension, Bitmap.Config.RGB_565);
        for (int x = 0; x < smallerDimension; x++){
            for (int y = 0; y < smallerDimension; y++){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        imgQRCode = (ImageView) findViewById(R.id.imgQRCode);
        imgQRCode.setImageBitmap(bmp);
    }
}
