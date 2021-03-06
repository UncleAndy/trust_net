package org.gplvote.trustnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ATrustChange extends ActionBarActivity implements View.OnClickListener {

    private TextView txtName;
    private TextView txtPersonalId;
    private EditText edtLevel;
    private Button   btnBack;
    private Button   btnConfirm;
    private Button   btnMinus;
    private Button   btnPlus;
    private LinearLayout listHistory;

    private String trust_id;

    private String mode = "act";

    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trust_change);

        gson = new Gson();

        txtName = (TextView) findViewById(R.id.txtTrustChangeName);
        txtPersonalId = (TextView) findViewById(R.id.txtTrustChangePersonalId);
        edtLevel = (EditText) findViewById(R.id.edtTrustChangeLevel);
        edtLevel.setFilters(new InputFilter[]{new InputFilterMinMax("-10", "10")});
        listHistory = (LinearLayout) findViewById(R.id.llyTrustChangeHistory);

        btnBack = (Button) findViewById(R.id.btnTrustChangeBack);
        btnConfirm = (Button) findViewById(R.id.btnTrustChangeConfirm);
        btnMinus = (Button) findViewById(R.id.btnTrustLevelMinus);
        btnPlus = (Button) findViewById(R.id.btnTrustLevelPlus);
        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnMinus.setOnClickListener(this);
        btnPlus.setOnClickListener(this);

        trust_id = getIntent().getStringExtra("TrustId");
        if (trust_id == null || trust_id.isEmpty()) {
            Intent i = getIntent();
            Uri d = null;
            if (i != null) d = i.getData();

            String json_trust_info = null;
            if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("trust")) {
                json_trust_info = d.getPath();
                json_trust_info = json_trust_info.replaceAll("^/", "");
            }

            if (json_trust_info == null || json_trust_info.isEmpty()) {
                Log.e("ConfirmTrust", "Error when get attestation info from URL");
                throw new RuntimeException("ConfirmTrust: Error when get attestation info from URL");
            }

            Gson gson = new Gson();
            HashMap<String, Object> trust_info = gson.fromJson(json_trust_info, new TypeToken<HashMap<String, Object>>() {}.getType());

            String personal_id = (String) trust_info.get("pid");
            String name_qr = (String) trust_info.get("nm");
            String name_db = AMain.get_personal_name(personal_id);
            String level = (String) trust_info.get("lv");

            String name = name_qr;
            if (name_db != null && !name_db.isEmpty() && !name_db.equals(name_qr))
                name = name + "(" + name_db + ")";

            if (level == null || level.isEmpty() || Integer.valueOf(level) < -10 || Integer.valueOf(level) > 10)
                level = "0";

            txtName.setText(name);
            txtPersonalId.setText(personal_id);
            edtLevel.setText(level);
        } else {
            reload_data();
        };
    }

    @Override
    public void onClick(View v) {
        Integer lvl;
        switch(v.getId()) {
            case R.id.btnTrustChangeBack:
                if (ATrusts.instance != null)
                    ATrusts.instance.reload_data();
                finish();
                break;
            case R.id.btnTrustLevelMinus:
                if (String.valueOf(edtLevel.getText()).isEmpty() || String.valueOf(edtLevel.getText()).equals("-"))
                    lvl = 0;
                else
                    lvl = Integer.valueOf(String.valueOf(edtLevel.getText())) - 1;
                if (lvl < -10) lvl = -10;
                edtLevel.setText(String.valueOf(lvl));
                break;
            case R.id.btnTrustLevelPlus:
                if (String.valueOf(edtLevel.getText()).isEmpty() || String.valueOf(edtLevel.getText()).equals("-"))
                    lvl = 0;
                else
                    lvl = Integer.valueOf(String.valueOf(edtLevel.getText())) + 1;
                if (lvl > 10) lvl = 10;
                edtLevel.setText(String.valueOf(lvl));
                break;
            case R.id.btnTrustChangeConfirm:
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

            reload_history(c.getString(c.getColumnIndex("content_id")));

            c.close();
        }
    }

    private void reload_history(String content_id) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();
        // Загрузка данных для истории
        Cursor c = db.query("docs", new String[]{"id", "doc", "t_create"}, "content_id = '" + content_id + "'", null, null, null, "t_create desc", "100");

        ArrayList<Map<String, Object>> items_list = new ArrayList<Map<String, Object>>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String doc_json = c.getString(c.getColumnIndex("doc"));
                    DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                    String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                    HashMap<String, Object> m = new HashMap<String, Object>();
                    m.put("t_create", doc_data[1]);
                    m.put("level", doc_data[3]);

                    items_list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0;i<items_list.size();i++) {
            HashMap<String, Object> m = (HashMap<String, Object>) items_list.get(i);

            View rowView = inflater.inflate(R.layout.list_item_trust_history, null);

            TextView txtTime = (TextView) rowView.findViewById(R.id.txtTrustHistoryTime);
            TextView txtLevel = (TextView) rowView.findViewById(R.id.txtTrustHistoryLevel);

            // Time parse
            String t_create = (String) m.get("t_create");
            if (t_create != null && !t_create.isEmpty())
                t_create = AMain.time_to_string(Long.parseLong(t_create));
            else
                t_create = "-";

            String level = (String) m.get("level");

            txtTime.setText(t_create);
            txtLevel.setText(level);

            listHistory.addView(rowView);
        }
    }
}
