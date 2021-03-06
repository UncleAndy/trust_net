package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AAboutMe extends Activity implements View.OnClickListener {
    public static AAboutMe instance;

    private Settings settings;

    private Button btnShowQRCode;
    private Button btnTrustQRCode;
    private TextView txtName;
    private TextView txtBirthday;
    private TextView txtTaxNumber;
    private TextView txtSocialNumber;
    private TextView txtPublicKeyId;
    private TextView txtCancelPublicKeyId;
    private TextView txtPersonalId;
    private Button btnChange;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);

        instance = this;

        settings = Settings.getInstance(this);

        btnChange = (Button) findViewById(R.id.btnAboutMeChange);
        btnChange.setOnClickListener(this);
        btnBack = (Button) findViewById(R.id.btnAboutMeBack);
        btnBack.setOnClickListener(this);
        btnShowQRCode = (Button) findViewById(R.id.btnAboutMeShowQRCode);
        btnShowQRCode.setOnClickListener(this);
        btnTrustQRCode = (Button) findViewById(R.id.btnAboutMeTrustQRCode);
        btnTrustQRCode.setOnClickListener(this);

        txtName         = (TextView) findViewById(R.id.txtAboutMeName);
        txtBirthday     = (TextView) findViewById(R.id.txtAboutMeBirthday);
        txtTaxNumber    = (TextView) findViewById(R.id.txtAboutMeTaxNumber);
        txtSocialNumber = (TextView) findViewById(R.id.txtAboutMeSocialNumber);
        txtPublicKeyId  = (TextView) findViewById(R.id.txtAboutMePublicKeyId);
        txtCancelPublicKeyId  = (TextView) findViewById(R.id.txtAboutMeCancelPublicKeyId);
        txtPersonalId   = (TextView) findViewById(R.id.txtAboutMePersonalId);

        setData();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnAboutMeShowQRCode:
                // Выводим на экран QR код с данными для удостоверения
                intent = new Intent(this, AConfirmMe.class);
                startActivity(intent);
                break;
            case R.id.btnAboutMeTrustQRCode:
                // Выводим на экран QR код с данными для удостоверения
                intent = new Intent(this, ATrustMe.class);
                startActivity(intent);
                break;
            case R.id.btnAboutMeChange:
                intent = new Intent(this, AAboutMeEdit.class);
                startActivity(intent);
                break;
            case R.id.btnAboutMeBack:
                finish();
                break;
        }
    }

    public void setData() {
        DataPersonalInfo info = settings.getPersonalInfo();
        if (info != null) {
            txtName.setText(info.name);
            txtBirthday.setText(info.birthday);
            txtTaxNumber.setText(info.tax_number);
            txtSocialNumber.setText(info.social_number);
            txtPublicKeyId.setText(info.public_key_id);
            txtCancelPublicKeyId.setText(info.cancel_public_key_id);
            txtPersonalId.setText(info.personal_id);
        }
    }
}
