package org.gplvote.trustnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.gplvote.trustnet.PersonalInfo;
import org.gplvote.trustnet.R;
import org.gplvote.trustnet.Settings;

import java.nio.charset.Charset;
import java.util.Hashtable;

public class ConfirmMe extends ActionBarActivity {
    private Settings settings;

    private ImageView imgQRCode;

    PersonalInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_me);

        settings = Settings.getInstance(this);
        info = settings.getPersonalInfo();

        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");
        intent.putExtra("Command", "GetPublicKeyId");
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}

        String user_key_id = data.getStringExtra("PUBLIC_KEY_ID");

        if (user_key_id == null || user_key_id.equals("")) {return;}

        String json_qr_data = "{\"first_name\":\""+info.first_name+"\",\"middle_name\":\""+info.middle_name+"\",\"last_name\":\""+info.last_name+"\",\"birthday\":\""+info.birthday+"\",\"tax_number\":\""+info.tax_number+"\",\"user_key_id\":\""+user_key_id+"\"}";

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

            matrix = writer.encode(json_qr_data, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
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
