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

// TODO: Реализовать обработку заверения доп. удостоверений из QR-кода и по проямому вызову
public class ATagChange extends ActionBarActivity implements View.OnClickListener {

    private TextView txtTagId;
    private TextView txtTagName;
    private TextView txtTagInfo;
    private TextView txtPersonalId;
    private TextView txtPersonalName;
    private TextView txtTagData;
    private EditText edtLevel;
    private Button   btnBack;
    private Button   btnConfirm;
    private LinearLayout listHistory;

    private String tag_id;

    private String mode = "act";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_change);

        txtTagId = (TextView) findViewById(R.id.txtTagChangeTagId);
        txtTagName = (TextView) findViewById(R.id.txtTagChangeTagName);
        txtTagInfo = (TextView) findViewById(R.id.txtTagChangeTagInfo);
        txtPersonalId = (TextView) findViewById(R.id.txtTagChangePersonalId);
        txtPersonalName = (TextView) findViewById(R.id.txtTagChangeName);
        txtTagData = (TextView) findViewById(R.id.txtTagChangeData);
        edtLevel = (EditText) findViewById(R.id.edtTagChangeLevel);
        edtLevel.setFilters(new InputFilter[]{new InputFilterMinMax("-10", "10")});
        listHistory = (LinearLayout) findViewById(R.id.llyTagChangeHistory);

        btnBack = (Button) findViewById(R.id.btnTagChangeBack);
        btnConfirm = (Button) findViewById(R.id.btnTagChangeConfirm);
        btnBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        tag_id = getIntent().getStringExtra("TagId");
        if (tag_id == null || tag_id.isEmpty()) {
            Intent i = getIntent();
            Uri d = null;
            if (i != null) d = i.getData();

            String json_tag_info = null;
            if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("tag")) {
                json_tag_info = d.getPath();
                json_tag_info = json_tag_info.replaceAll("^/", "");
            }

            if (json_tag_info == null || json_tag_info.isEmpty()) {
                Log.e("ConfirmTag", "Error when get attestation info from URL");
                throw new RuntimeException("ConfirmTag: Error when get attestation info from URL");
            }

            Gson gson = new Gson();
            HashMap<String, Object> tag_att_info = gson.fromJson(json_tag_info, new TypeToken<HashMap<String, Object>>() {}.getType());

            String tag_id = (String) tag_att_info.get("tid");
            String tag_name = AMain.get_tag_name(tag_id);
            String tag_info = AMain.get_tag_info(tag_id);
            String personal_id = (String) tag_att_info.get("pid");
            String name = AMain.get_personal_name(personal_id);
            String data = (String) tag_att_info.get("d");
            String level = (String) tag_att_info.get("lv");

            if (level == null || level.isEmpty() || Integer.valueOf(level) < -10 || Integer.valueOf(level) > 10)
                level = "0";

            txtTagId.setText(tag_id);
            txtTagName.setText(tag_name);
            txtTagInfo.setText(tag_info);
            txtPersonalId.setText(personal_id);
            txtPersonalName.setText(name);
            txtTagData.setText(data);
            edtLevel.setText(level);
        } else {
            reload_data();
        };
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnTagChangeBack:
                if (ATags.instance != null)
                    ATags.instance.reload_data();
                finish();
                break;
            case R.id.btnTagChangeConfirm:
                DataPersonalInfo pi = AMain.settings.getPersonalInfo();

                String tag_id = txtTagId.getText().toString();
                String personal_id = txtPersonalId.getText().toString();
                String tag_data = txtTagData.getText().toString();
                String level = edtLevel.getText().toString();

                DocTag doc_tag = new DocTag();

                doc_tag.site = AMain.SIGN_DOC_APP_TYPE;
                doc_tag.dec_data = "[\"" + pi.personal_id + "\",\"" + String.valueOf(System.currentTimeMillis()) + "\",\"" + tag_id + "\",\"" + personal_id + "\",\"" + tag_data + "\",\"" + level + "\"]";
                doc_tag.template = getString(R.string.template_doc_tag);

                PacketSigned pack_tag = doc_tag.get_packet();
                pack_tag.insert(DocTag.DOC_TYPE);
                ASendSign.add_to_queue(pack_tag.doc, "*", true);

                Intent intent = new Intent(this, ASendSign.class);
                startActivity(intent);

                if (ATags.instance != null)
                    ATags.instance.reload_data();

                finish();

                break;
        }
    }

    private void reload_data() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();
        Gson gson = new Gson();

        // Считываем данные текущего удостоверения
        Cursor c = db.query("docs", new String[]{"id", "doc", "content_id", "t_create"}, "id = " + tag_id, null, null, null, null, "1");

        Long create_time = (long) -1;
        if (c != null && c.moveToFirst()) {
            String doc_json = c.getString(c.getColumnIndex("doc"));
            DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
            String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

            txtTagId.setText(doc_data[2]);
            txtTagName.setText(AMain.get_tag_name(doc_data[2]));
            txtTagInfo.setText(AMain.get_tag_info(doc_data[2]));
            txtPersonalId.setText(doc_data[3]);
            txtPersonalName.setText(AMain.get_personal_name(doc_data[3]));
            txtTagData.setText(doc_data[4]);
            edtLevel.setText(doc_data[5]);

            reload_history(c.getString(c.getColumnIndex("content_id")), true);

            c.close();
        }
    }

    private void reload_history(String content_id, boolean include_current) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();
        Gson gson = new Gson();

        // Загрузка данных для истории
        Cursor c;
        if (include_current)
            c = db.query("docs", new String[]{"id", "doc", "t_create"}, "content_id = '" + content_id + "'", null, null, null, "t_create desc", "100");
        else
            c = db.query("docs", new String[]{"id", "doc", "t_create"}, "content_id = '" + content_id + "' AND current = 0", null, null, null, "t_create desc", "100");

        ArrayList<Map<String, Object>> items_list = new ArrayList<Map<String, Object>>();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String doc_json = c.getString(c.getColumnIndex("doc"));
                    DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                    String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                    HashMap<String, Object> m = new HashMap<String, Object>();
                    m.put("t_create", doc_data[1]);
                    m.put("level", doc_data[5]);

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
