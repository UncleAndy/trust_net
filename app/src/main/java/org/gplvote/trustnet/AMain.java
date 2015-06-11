package org.gplvote.trustnet;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AMain extends ActionBarActivity implements View.OnClickListener {
    public static final String SIGN_DOC_PACKAGE_NAME = "org.gplvote.signdoc";
    public static final int SIGN_DOC_MIN_VERSION_CODE_REQUIRED = 19;
    public static final String SIGN_DOC_APP_TYPE = "app:trustnet";
    public static AMain instance;

    public static final String TRUSTNET_INT_URL_REG_SERVER = "trustnet://regserver/";
    public static final String TRUSTNET_INT_URL_VERIFICATION = "trustnet://verification/";
    public static final String TRUSTNET_INT_URL_TRUST = "trustnet://trust/";
    public static final String TRUSTNET_INT_URL_TAG_INFO = "trustnet://taginfo/";
    public static final String TRUSTNET_INT_URL_TAG = "trustnet://tag/";
    public static final String TRUSTNET_INT_URL_PUBLIC_KEY = "trustnet://publickey/";
    public static final String TRUSTNET_INT_URL_MESSAGE = "trustnet://message/";

    public static Settings settings;

    private Button btnMe;
    private Button btnServers;
    private Button btnAttestations;
    private Button btnTrusts;
    private Button btnTagsAttestations;
    private Button btnMessages;
    private Button btnQRCode;

    private Button btnSignDocInstall;

    private static final Integer RESULT_PUBLIC_KEY = 1;

    public static DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Проверяем наличие установленного приложения Sign Doc
        if (isPackageInstalled(SIGN_DOC_PACKAGE_NAME)) {
            setContentView(R.layout.activity_main);

            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            settings = Settings.getInstance(this);
            instance = this;

            btnMe               = (Button) findViewById(R.id.btnMe);
            btnServers          = (Button) findViewById(R.id.btnServers);
            btnAttestations     = (Button) findViewById(R.id.btnAttestations);
            btnTrusts           = (Button) findViewById(R.id.btnTrusts);
            btnTagsAttestations = (Button) findViewById(R.id.btnTagsAttestations);
            btnMessages         = (Button) findViewById(R.id.btnMessages);
            btnQRCode           = (Button) findViewById(R.id.btnQRRead);

            btnMe.setOnClickListener(this);
            btnServers.setOnClickListener(this);
            btnAttestations.setOnClickListener(this);
            btnTrusts.setOnClickListener(this);
            btnTagsAttestations.setOnClickListener(this);
            btnMessages.setOnClickListener(this);
            btnQRCode.setOnClickListener(this);

            check_personal_id_status();

            db = DbHelper.getInstance(this);

            // Считываем публичный ключ и его идентификатор из приложения SignDoc
            Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");
            intent.putExtra("Command", "GetPublicKeyId");
            startActivityForResult(intent, RESULT_PUBLIC_KEY);
        } else {
            requireSignDocApplication();
        }
    }

    public void qrScanStart() {
        Log.d("QRCODE", "qr_scan");
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_PUBLIC_KEY) {
                // Получаем публичный ключ и его идентификатор и записываем их в настройки
                DataPersonalInfo pi = settings.getPersonalInfo();

                Log.d("GETKEY", "Personal info = " + pi.toString());
                Log.d("GETKEY", "Returned data key id = " + data.getStringExtra("PUBLIC_KEY_ID"));

                pi.public_key_id = data.getStringExtra("PUBLIC_KEY_ID");
                pi.public_key = data.getStringExtra("PUBLIC_KEY");
                pi.cancel_public_key_id = data.getStringExtra("CANCEL_PUBLIC_KEY_ID");
                pi.cancel_public_key = data.getStringExtra("CANCEL_PUBLIC_KEY");

                if (pi.public_key_id == null || pi.public_key_id.equals("") || pi.public_key == null || pi.public_key.equals("")) {
                    runSignDocApplication();
                    return;
                }

                settings.setPersonalInfo(pi);
            } else {
                Log.d("QRCODE", "onActivityResult");
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    String uri_str = scanResult.getContents();

                    if (uri_str != null) {
                        Log.d("SCAN", "Scan result = " + uri_str);

                        Uri uri = Uri.parse(uri_str);
                        Log.d("SCAN", "URI = " + uri.getScheme()+ " - " +uri.getHost() + " - " + uri.getQuery());

                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnMe:
                intent = new Intent(this, AAboutMe.class);
                startActivity(intent);
                break;
            case R.id.btnServers:
                intent = new Intent(this, AServers.class);
                startActivity(intent);
                break;
            case R.id.btnAttestations:
                intent = new Intent(this, AAttestations.class);
                startActivity(intent);
                break;
            case R.id.btnTrusts:
                intent = new Intent(this, ATrusts.class);
                startActivity(intent);
                break;
            case R.id.btnTagsAttestations:
                intent = new Intent(this, ATags.class);
                startActivity(intent);
                break;
            case R.id.btnMessages:
                intent = new Intent(this, AMessages.class);
                startActivity(intent);
                break;
            case R.id.btnQRRead:
                qrScanStart();
                break;
            case R.id.btnSignDocInstall:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SIGN_DOC_PACKAGE_NAME)));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + SIGN_DOC_PACKAGE_NAME)));
                }
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void check_personal_id_status() {
        DataPersonalInfo pi = settings.getPersonalInfo();

        Boolean need_add_info = (pi == null || pi.personal_id == null || pi.personal_id.isEmpty());
        btnAttestations.setEnabled(!need_add_info);
        btnQRCode.setEnabled(!need_add_info);
        btnMessages.setEnabled(!need_add_info);
        btnServers.setEnabled(!need_add_info);
        btnTagsAttestations.setEnabled(!need_add_info);
        btnTrusts.setEnabled(!need_add_info);
    }

    boolean isPackageInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        boolean available = false;
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, 0);
            available = (pi != null && pi.versionCode >= SIGN_DOC_MIN_VERSION_CODE_REQUIRED);
        } catch (PackageManager.NameNotFoundException e) {
        }

        return available;
    }

    void requireSignDocApplication() {
        setContentView(R.layout.sign_doc_app);

        btnSignDocInstall = (Button) findViewById(R.id.btnSignDocInstall);
        btnSignDocInstall.setOnClickListener(this);
    }

    void runSignDocApplication() {
        PackageManager pm = this.getPackageManager();

        try
        {
            Intent it = pm.getLaunchIntentForPackage(SIGN_DOC_PACKAGE_NAME);

            if (null != it) {
                this.startActivity(it);
                finish();
            } else {
                requireSignDocApplication();
            }
        } catch (ActivityNotFoundException e) {
            requireSignDocApplication();
        }
    }

    public static void showQRCode(String content, ImageView element) {
        final ImageView iv = element;
        final String cont = content;
        ViewTreeObserver vto = iv.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                iv.getViewTreeObserver().removeOnPreDrawListener(this);
                int width = iv.getMeasuredWidth();
                int height = iv.getMeasuredHeight();

                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension * 3 / 4;

                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix matrix = null;
                try {
                    Hashtable hints = new Hashtable();
                    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

                    matrix = writer.encode(cont, BarcodeFormat.QR_CODE, smallerDimension, smallerDimension, hints);
                } catch (WriterException ex) {
                    ex.printStackTrace();
                }

                Bitmap bmp = Bitmap.createBitmap(smallerDimension, smallerDimension, Bitmap.Config.RGB_565);
                for (int x = 0; x < smallerDimension; x++) {
                    for (int y = 0; y < smallerDimension; y++) {
                        bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }

                iv.setImageBitmap(bmp);

                return true;
            }
        });
    }

    public static String time_to_string(Long time) {
        if (time == null || time <= 0) return("");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return(sdf.format(time));
    }

    public static String time_to_string(String time) {
        Long time_long;
        try {
            time_long = Long.parseLong(time);
        } catch (Exception e) {
            time_long = 0L;
        }

        return(time_to_string(time_long));
    }

    public static String get_personal_name(String personal_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("names", new String[]{"name"}, "personal_id = '"+personal_id+"'", null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            return(c.getString(c.getColumnIndex("name")));
        }
        return("");
    }

    public static String get_tag_name(String tag_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("tags_info", new String[]{"name"}, "id = '"+tag_id+"'", null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            return(c.getString(c.getColumnIndex("name")));
        }
        return("");
    }

    public static String get_tag_info(String tag_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("tags_info", new String[]{"info"}, "id = '"+tag_id+"'", null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            return(c.getString(c.getColumnIndex("info")));
        }
        return("");
    }

    public static void update_name(String name, String personal_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("names", new String[]{"id", "name"}, "personal_id = '" + personal_id + "'", null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            String id = c.getString(c.getColumnIndex("id"));
            String old_name = c.getString(c.getColumnIndex("name"));
            if (!name.equals(old_name)) {
                ContentValues cv = new ContentValues();
                cv.put("name", name);

                db.update("names", cv, "id = ?", new String[] {id});
            }
        } else {
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            cv.put("personal_id", personal_id);
            cv.put("t_create", System.currentTimeMillis());

            db.insert("names", null, cv);
        }
    }

    public static byte[] getPublicKeyId(byte[] public_key_bytes) {
        MessageDigest digest;
        byte[] hash;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(public_key_bytes);
            return(hash);
        } catch (NoSuchAlgorithmException e) {
            Log.e("SHA-256", "Hash create error: " + e.getMessage());
        }

        return(null);
    }

    public static String getPublicKeyIdBase64(String public_key_base64) {
        byte[] hash = getPublicKeyId(Base64.decode(public_key_base64, Base64.NO_WRAP));
        if (hash != null) {
            return (Base64.encodeToString(hash, Base64.NO_WRAP));
        }
        return(null);
    }
}
