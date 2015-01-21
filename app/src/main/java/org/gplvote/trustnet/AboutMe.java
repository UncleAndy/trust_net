package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AboutMe extends Activity implements View.OnClickListener {

    private Settings settings;

    private EditText txtName;
    private EditText txtBirthday;
    private EditText txtTaxNumber;
    private EditText txtSocialNumber;
    private TextView txtPublicKeyId;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);

        settings = Settings.getInstance(this);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        txtName         = (EditText) findViewById(R.id.txtName);
        txtBirthday     = (EditText) findViewById(R.id.txtBirthday);
        txtTaxNumber    = (EditText) findViewById(R.id.txtTaxNumber);
        txtSocialNumber = (EditText) findViewById(R.id.txtSocialNumber);
        txtPublicKeyId  = (TextView) findViewById(R.id.txtPublicKeyId);

        PersonalInfo info = settings.getPersonalInfo();
        if (info != null) {
            txtName.setText(info.name);
            txtBirthday.setText(info.birthday);
            txtTaxNumber.setText(info.tax_number);
            txtSocialNumber.setText(info.social_number);
        }

        Intent intent = new Intent("org.gplvote.signdoc.DO_SIGN");
        intent.putExtra("Command", "GetPublicKeyId");
        startActivityForResult(intent, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        String user_key_id = data.getStringExtra("PUBLIC_KEY_ID");

        if (user_key_id == null || user_key_id.equals("")) {
            return;
        }

        txtPublicKeyId.setText(user_key_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                // Сохраняем персональную информацию
                PersonalInfo info = new PersonalInfo();
                info.name = txtName.getText().toString();
                info.birthday = txtBirthday.getText().toString();
                info.tax_number = txtTaxNumber.getText().toString();
                info.social_number = txtSocialNumber.getText().toString();
                settings.setPersonalInfo(info);

                finish();
                break;
        }
    }
}
