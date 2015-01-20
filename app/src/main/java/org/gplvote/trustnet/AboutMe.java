package org.gplvote.trustnet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AboutMe extends Activity implements View.OnClickListener {

    private Settings settings;

    private EditText txtFirstName;
    private EditText txtMiddleName;
    private EditText txtLastName;
    private EditText txtBirthday;
    private EditText txtTaxNumber;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me);

        settings = Settings.getInstance(this);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        txtFirstName    = (EditText) findViewById(R.id.txtFirstName);
        txtMiddleName   = (EditText) findViewById(R.id.txtMiddleName);
        txtLastName     = (EditText) findViewById(R.id.txtLastName);
        txtBirthday     = (EditText) findViewById(R.id.txtBirthday);
        txtTaxNumber    = (EditText) findViewById(R.id.txtTaxNumber);

        PersonalInfo info = settings.getPersonalInfo();
        txtFirstName.setText(info.first_name);
        txtMiddleName.setText(info.middle_name);
        txtLastName.setText(info.last_name);
        txtBirthday.setText(info.birthday);
        txtTaxNumber.setText(info.tax_number);

        // TODO: Сделать получение идентификатора публичного ключа из SignDoc и отображение его на экране

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSave:
                // Сохраняем персональную информацию
                PersonalInfo info = new PersonalInfo();
                info.first_name = txtFirstName.getText().toString();
                info.middle_name = txtMiddleName.getText().toString();
                info.last_name = txtLastName.getText().toString();
                info.birthday = txtBirthday.getText().toString();
                info.tax_number = txtTaxNumber.getText().toString();
                settings.setPersonalInfo(info);

                finish();
                break;
        }
    }
}
