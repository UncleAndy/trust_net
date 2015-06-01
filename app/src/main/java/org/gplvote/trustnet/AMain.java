package org.gplvote.trustnet;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;


public class AMain extends ActionBarActivity implements View.OnClickListener {
    public static final String SIGN_DOC_PACKAGE_NAME = "org.gplvote.signdoc";
    public static final int SIGN_DOC_MIN_VERSION_CODE_REQUIRED = 17;
    public static final String SIGN_DOC_APP_TYPE = "app:trustnet";
    public static AMain instance;

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

                if (pi.public_key_id == null || pi.public_key_id.equals("") || pi.public_key == null || pi.public_key.equals("")) {
                    runSignDocApplication();
                    return;
                }

                settings.setPersonalInfo(pi);
            } else {
                Log.d("QRCODE", "onActivityResult");
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (scanResult != null) {
                    String uri = scanResult.getContents();

                    if (uri != null) {
                        Log.d("SCAN", "Scan result = " + uri);

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
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
                break;
            case R.id.btnTrusts:
                break;
            case R.id.btnTagsAttestations:
                break;
            case R.id.btnMessages:
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
}
