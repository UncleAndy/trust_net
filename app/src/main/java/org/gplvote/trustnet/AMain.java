package org.gplvote.trustnet;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class AMain extends ActionBarActivity implements View.OnClickListener {

    private Button btnMe;
    private Button btnServers;
    private Button btnAttestations;
    private Button btnTrusts;
    private Button btnTagsAttestations;
    private Button btnMessages;
    private Button btnQRCode;

    private static final Integer RESULT_PUBLIC_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // Считываем публичный ключ и его идентификатор из приложения SignDoc
        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");
        intent.putExtra("Command", "GetPublicKeyId");
        startActivityForResult(intent, RESULT_PUBLIC_KEY);
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
                Settings settings = Settings.getInstance(this);
                DataPersonalInfo pi = settings.getPersonalInfo();

                Log.d("GETKEY", "Personal info = " + pi.toString());
                Log.d("GETKEY", "Returned data key id = " + data.getStringExtra("PUBLIC_KEY_ID"));

                pi.public_key_id = data.getStringExtra("PUBLIC_KEY_ID");
                pi.public_key = data.getStringExtra("PUBLIC_KEY");

                if (pi.public_key_id == null || pi.public_key_id.equals("") || pi.public_key == null || pi.public_key.equals(""))
                    return;

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
}