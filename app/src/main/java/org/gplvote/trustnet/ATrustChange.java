package org.gplvote.trustnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ATrustChange extends ActionBarActivity implements View.OnClickListener {

    private TextView txtName;
    private TextView txtPersonalId;
    private EditText edtLevel;
    private Button   btnBack;
    private Button   btnConfirm;

    private String trust_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trust_change);

        txtName = (TextView) findViewById(R.id.txtTrustChangeName);
        txtPersonalId = (TextView) findViewById(R.id.txtTrustChangePersonalId);
        edtLevel = (EditText) findViewById(R.id.edtTrustChangeLevel);
        edtLevel.setFilters(new InputFilter[]{new InputFilterMinMax("-10", "10")});

        btnBack = (Button) findViewById(R.id.btnTrustChangeBack);
        btnConfirm = (Button) findViewById(R.id.btnTrustChangeConfirm);
        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        trust_id = getIntent().getStringExtra("TrustId");

        reload_data();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnTrustChangeBack:
                if (ATrusts.instance != null)
                    ATrusts.instance.reload_data();
                finish();
                break;
            case R.id.btnTrustChangeConfirm:
                // TODO: Если уровень не совпадает с предыдущим, формируем документ
                // TODO: об уровне доверия, подписываем и отправляем его
                DataPersonalInfo pi = AMain.settings.getPersonalInfo();

                String personal_id = (String) txtPersonalId.getText();
                String level = edtLevel.getText().toString();

                DocTrust doc_tr = new DocTrust();

                doc_tr.site = AMain.SIGN_DOC_APP_TYPE;
                doc_tr.dec_data = "[\"" + pi.personal_id + "\",\"" + String.valueOf(System.currentTimeMillis()) + "\",\"" + personal_id + "\",\""+ level +"\"]";;
                doc_tr.template = getString(R.string.template_doc_trust);

                PacketSigned pack_tr = doc_tr.get_packet();
                pack_tr.insert(DocTrust.DOC_TYPE);
                ASendSign.add_to_queue(pack_tr.doc, "*", true);

                Intent intent = new Intent(this, ASendSign.class);
                startActivity(intent);

                if (ATrusts.instance != null)
                    ATrusts.instance.reload_data();

                finish();

                break;
        }
    }

    private void reload_data() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        // Считываем данные текущего удостоверения
        Cursor c = db.query("docs", new String[]{"id", "doc", "content_id", "t_create"}, "id = '" + trust_id + "' AND current = 1", null, null, null, null, "1");

        Gson gson = new Gson();
        Long create_time = (long) -1;
        if (c != null && c.moveToFirst()) {
            String doc_json = c.getString(c.getColumnIndex("doc"));
            DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
            String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

            txtName.setText(AMain.get_personal_name(doc_data[2]));
            txtPersonalId.setText(doc_data[2]);
            edtLevel.setText(doc_data[3]);

            c.close();
        }
    }
}
