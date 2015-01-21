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

    private TextView txtConfirmName;
    private TextView txtConfirmBirthday;
    private TextView txtConfirmTaxNumber;
    private TextView txtConfirmSocialNumber;
    private TextView txtConfirmUserKeyId;
    private EditText edtVerifyLevel;
    private EditText edtTrustLevel;

    private PersonalInfo personal_info;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_other);

        gson = new Gson();

        String json_personal_info = getIntent().getStringExtra("PersonalInfo");
        personal_info = gson.fromJson(json_personal_info, new TypeToken<PersonalInfo>(){}.getType());

        txtConfirmName = (TextView) findViewById(R.id.txtConfirmName);
        txtConfirmBirthday = (TextView) findViewById(R.id.txtConfirmBirthday);
        txtConfirmTaxNumber = (TextView) findViewById(R.id.txtConfirmTaxNumber);
        txtConfirmSocialNumber = (TextView) findViewById(R.id.txtConfirmSocialNumber);
        txtConfirmUserKeyId = (TextView) findViewById(R.id.txtConfirmUserKeyId);

        txtConfirmName.setText(personal_info.name);
        txtConfirmBirthday.setText(personal_info.birthday);
        txtConfirmTaxNumber.setText(personal_info.tax_number);
        txtConfirmSocialNumber.setText(personal_info.social_number);
        txtConfirmUserKeyId.setText(personal_info.user_key_id);
    }
}
