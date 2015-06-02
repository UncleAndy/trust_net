package org.gplvote.trustnet;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class AConfirmOther extends Activity implements View.OnClickListener {
    private Gson gson;

    private TextView txtInfo;

    private TextView txtConfirmName;
    private TextView txtConfirmBirthday;
    private TextView txtConfirmTaxNumber;
    private TextView txtConfirmSocialNumber;
    private TextView txtConfirmUserKeyId;
    private EditText edtVerifyLevel;
    private EditText edtTrustLevel;

    private Button btnBack;
    private Button btnConfirm;

    private DataAttestationInfo att_info;

    private TaskCalcPersonalId task = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress);

        txtInfo = (TextView) findViewById(R.id.txtProgressInfo);
        txtInfo.setText(getString(R.string.text_progress_check_personal_id));

        gson = new Gson();

        // Получение данных только из URL
        Intent i = getIntent();
        Uri d = null;
        if (i != null) d = i.getData();

        String json_att_info = null;
        if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("verification")) {
            json_att_info = d.getPath();
            json_att_info = json_att_info.replaceAll("^/", "");
        }

        if (json_att_info == null) {
            Log.e("ConfirmOther", "Error when get attestation info from URL");
            throw new RuntimeException("ConfirmOther: Error when get attestation info from URL");
        }

        att_info = gson.fromJson(json_att_info, DataAttestationInfo.class);

        // Запуск процесса определения персонального идентификатора и сравнения его с переданным (в асинхронной задаче)
        task = new TaskCalcPersonalId();
        task.execute(att_info);
    }

    private void show_confirm_info(String calc_personal_id) {
        if (calc_personal_id.equals(att_info.pid)) {
            setContentView(R.layout.confirm_other);

            txtConfirmName = (TextView) findViewById(R.id.txtConfirmName);
            txtConfirmBirthday = (TextView) findViewById(R.id.txtConfirmBirthday);
            txtConfirmTaxNumber = (TextView) findViewById(R.id.txtConfirmTaxNumber);
            txtConfirmSocialNumber = (TextView) findViewById(R.id.txtConfirmSocialNumber);
            txtConfirmUserKeyId = (TextView) findViewById(R.id.txtConfirmUserKeyId);

            btnBack = (Button) findViewById(R.id.btnAttestationBack);
            btnConfirm = (Button) findViewById(R.id.btnAttestationConfirm);

            edtVerifyLevel = (EditText) findViewById(R.id.edtConfirmVerifyLevel);
            edtVerifyLevel.setFilters(new InputFilter[]{new InputFilterMinMax("-10", "10")});
            edtVerifyLevel.setText("10");

            edtTrustLevel = (EditText) findViewById(R.id.edtConfirmTrustLevel);
            edtTrustLevel.setFilters(new InputFilter[]{new InputFilterMinMax("-10", "10")});
            edtTrustLevel.setText("10");

            txtConfirmName.setText(att_info.nm);
            txtConfirmBirthday.setText(att_info.bd);
            txtConfirmTaxNumber.setText(att_info.tn);
            txtConfirmSocialNumber.setText(att_info.sn);
            txtConfirmUserKeyId.setText(att_info.pkid);
        } else {
            setContentView(R.layout.error);

            txtInfo = (TextView) findViewById(R.id.txtError);
            txtInfo.setText(getString(R.string.error_bad_personal_id));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAttestationConfirm:
                // TODO: Формирование подтверждения и отправка его на подписание и сервер через ASendSign




                break;
            case R.id.btnAttestationBack:
                finish();
                break;
        }
    }

    private class TaskCalcPersonalId extends AsyncTask<DataAttestationInfo, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(DataAttestationInfo... params) {
            DataPersonalInfo pi = new DataPersonalInfo();
            pi.birthday = att_info.bd;
            pi.social_number = att_info.sn;
            pi.tax_number = att_info.tn;
            return(pi.gen_personal_id());
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d("CheckPersonalId", "Pid = "+result);

            try {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_DTMF_0, 300);
                Thread.sleep(300);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            show_confirm_info(result);

            task = null;
        }
    }

}
