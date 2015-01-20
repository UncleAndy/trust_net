package org.gplvote.trustnet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.gplvote.trustnet.PersonalInfo;
import org.gplvote.trustnet.R;

public class ConfirmOther extends Activity {
    private Gson gson;

    private TextView txtConfirmFirstName;
    private TextView txtConfirmMiddleName;
    private TextView txtConfirmLastName;
    private TextView txtConfirmBirthday;
    private TextView txtConfirmTaxNumber;
    private EditText edtVerifyLevel;
    private EditText edtTrustLevel;

    private PersonalInfo personal_info;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_other);

        gson = new Gson();

        String json_personal_info = getIntent().getStringExtra("PersonalInfo");
        personal_info = gson.fromJson(json_personal_info, new TypeToken<PersonalInfo>(){}.getType());

        txtConfirmFirstName = (TextView) findViewById(R.id.txtConfirmFirstName);
        txtConfirmMiddleName = (TextView) findViewById(R.id.txtConfirmMiddleName);
        txtConfirmLastName = (TextView) findViewById(R.id.txtConfirmLastName);
        txtConfirmBirthday = (TextView) findViewById(R.id.txtConfirmBirthday);
        txtConfirmTaxNumber = (TextView) findViewById(R.id.txtConfirmTaxNumber);

        txtConfirmFirstName.setText(personal_info.first_name);
        txtConfirmMiddleName.setText(personal_info.middle_name);
        txtConfirmLastName.setText(personal_info.last_name);
        txtConfirmBirthday.setText(personal_info.birthday);
        txtConfirmTaxNumber.setText(personal_info.tax_number);
    }
}
