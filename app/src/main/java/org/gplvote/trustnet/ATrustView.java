package org.gplvote.trustnet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ATrustView extends ActionBarActivity implements View.OnClickListener {

    private TextView txtName;
    private TextView txtPersonalId;
    private TextView txtLevel;
    private TextView txtTime;
    private LinearLayout listHistory;
    private Button   btnBack;

    private String trust_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trust_view);

        txtName = (TextView) findViewById(R.id.txtTrustViewName);
        txtTime = (TextView) findViewById(R.id.txtTrustViewTime);
        txtPersonalId = (TextView) findViewById(R.id.txtTrustViewPersonalId);
        txtLevel = (TextView) findViewById(R.id.txtTrustViewLevel);
        listHistory = (LinearLayout) findViewById(R.id.llyTrustViewHistory);

        btnBack = (Button) findViewById(R.id.btnTrustViewBack);
        btnBack.setOnClickListener(this);

        trust_id = getIntent().getStringExtra("TrustId");

        reload_data();
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    private void reload_data() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        // Считываем данные текущего удостоверения
        Cursor c = db.query("docs", new String[]{"id", "doc", "content_id", "t_create"}, "id = '" + trust_id + "' AND current = 1", null, null, null, null, "1");

        Gson gson = new Gson();
        String content_id = null;
        Long create_time = (long) -1;
        if (c != null && c.moveToFirst()) {
            String doc_json = c.getString(c.getColumnIndex("doc"));
            DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
            String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

            txtName.setText(AMain.get_personal_name(doc_data[2]));
            txtPersonalId.setText(doc_data[2]);
            txtLevel.setText(doc_data[3]);
            txtTime.setText(AMain.time_to_string(doc_data[1]));

            content_id = c.getString(c.getColumnIndex("content_id"));
            c.close();
        }

        // Сделать загрузку данных для текущего атестата и для истории
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
