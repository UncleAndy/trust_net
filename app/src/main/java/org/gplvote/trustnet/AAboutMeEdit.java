package org.gplvote.trustnet;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AAboutMeEdit extends Activity implements View.OnClickListener {

    private Settings settings;

    private EditText edtName;
    private EditText edtBirthday;
    private EditText edtTaxNumber;
    private EditText edtSocialNumber;
    private Button btnSave;
    private Button btnBack;
    private TextView txtInfo;
    private ProgressBar progressSave;
    private LinearLayout lyFields;

    private TaskSettingSave task_save = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me_edit);

        settings = Settings.getInstance(this);

        lyFields = (LinearLayout) findViewById(R.id.lyAboutMeFields);

        btnSave = (Button) findViewById(R.id.btnAboutMeSave);
        btnSave.setOnClickListener(this);
        btnBack = (Button) findViewById(R.id.btnAboutMeEditBack);
        btnBack.setOnClickListener(this);

        edtName         = (EditText) findViewById(R.id.edtAboutMeName);
        edtBirthday     = (EditText) findViewById(R.id.edtAboutMeBirthday);
        edtTaxNumber    = (EditText) findViewById(R.id.edtAboutMeTaxNumber);
        edtSocialNumber = (EditText) findViewById(R.id.edtAboutMeSocialNumber);

        txtInfo         = (TextView) findViewById(R.id.textAboutMeEditInfo);
        progressSave    = (ProgressBar) findViewById(R.id.progressSave);

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

                // Запуск фоновой задачи сохранения данных
                task_save = new TaskSettingSave();
                task_save.execute(info);
                break;
            case R.id.btnAboutMeEditBack:
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (task_save == null) {
            super.onBackPressed();
        }
    }

    // Асинхронный таск для записи персональных данных
    // Необходим потому, что генерация персонального идентификатора может занять существенное время
    // При старте в форме ввода:
    // 1. показывает прогресс-бар
    // 2. отображает в текстовом поле с пояснениями предупреждение о длительном процессе
    // 3. запрещает редактирование всех полей и кнопку "Сохранить"
    // 4. вешает на кнопку "Назад" сообщение "Прервать" и обработчик для остановки фоновой задачи

    class TaskSettingSave extends AsyncTask<DataPersonalInfo, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            txtInfo.setText(getText(R.string.txt_about_me_generate));

            edtName.setEnabled(false);
            edtBirthday.setEnabled(false);
            edtTaxNumber.setEnabled(false);
            edtSocialNumber.setEnabled(false);
            btnSave.setEnabled(false);
            btnBack.setEnabled(false);

            lyFields.setVisibility(View.GONE);

            progressSave.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(DataPersonalInfo... params) {
            settings.setPersonalInfo(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            try {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_DTMF_0, 300);
                Thread.sleep(300);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            AAboutMe.instance.setData();

            task_save = null;

            AMain.instance.check_personal_id_status();

            AAboutMeEdit.this.finish();
        }
    }
}
