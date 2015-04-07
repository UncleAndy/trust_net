package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRReaderActivity extends ActionBarActivity {
    public void qrScanStart() {
        Log.d("QRCODE", "qr_scan");
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("QRCODE", "onActivityResult");
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String uri = scanResult.getContents();

            if (uri != null) {
                Log.d("SCAN", "Scan result = " + uri);

                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        }
    }
}
