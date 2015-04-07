package org.gplvote.trustnet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AAboutMeEdit extends Activity implements View.OnClickListener {

    private Settings settings;

    private EditText edtName;
    private EditText edtBirthday;
    private EditText edtTaxNumber;
    private EditText edtSocialNumber;
    private Button btnSave;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me_edit);

        settings = Settings.getInstance(this);

        btnSave = (Button) findViewById(R.id.btnAboutMeSave);
        btnSave.setOnClickListener(this);
        btnBack = (Button) findViewById(R.id.btnAboutMeEditBack);
        btnBack.setOnClickListener(this);

        edtName         = (EditText) findViewById(R.id.edtAboutMeName);
        edtBirthday     = (EditText) findViewById(R.id.edtAboutMeBirthday);
        edtTaxNumber    = (EditText) findViewById(R.id.edtAboutMeTaxNumber);
        edtSocialNumber = (EditText) findViewById(R.id.edtAboutMeSocialNumber);

        DataPersonalInfo info = settings.getPersonalInfo();
        if (info != null) {
            edtName.setText(info.name);
            edtBirthday.setText(info.birthday);
            edtTaxNumber.setText(info.tax_number);
            edtSocialNumber.setText(info.social_number);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAboutMeSave:
                // Сохраняем персональную информацию
                DataPersonalInfo info = settings.getPersonalInfo();
                info.name = edtName.getText().toString();
                info.birthday = edtBirthday.getText().toString();
                info.tax_number = edtTaxNumber.getText().toString();
                info.social_number = edtSocialNumber.getText().toString();
                settings.setPersonalInfo(info);

                AAboutMe.instance.setData();

                finish();
                break;
            case R.id.btnAboutMeEditBack:
                finish();
                break;
        }
    }
}
