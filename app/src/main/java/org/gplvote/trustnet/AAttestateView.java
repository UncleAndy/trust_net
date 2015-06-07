package org.gplvote.trustnet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AAttestateView extends ActionBarActivity implements View.OnClickListener {

    private TextView txtName;
    private TextView txtPersonalId;
    private TextView txtPublicKeyId;
    private TextView txtLevel;
    private TextView txtTime;
    private LinearLayout listHistory;
    private Button   btnBack;

    private String attestate_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attestate_view);

        txtName = (TextView) findViewById(R.id.txtAttestateViewName);
        txtTime = (TextView) findViewById(R.id.txtAttestateViewTime);
        txtPersonalId = (TextView) findViewById(R.id.txtAttestateViewPersonalId);
        txtPublicKeyId = (TextView) findViewById(R.id.txtAttestateViewPublicKeyId);
        txtLevel = (TextView) findViewById(R.id.txtAttestateViewLevel);
        listHistory = (LinearLayout) findViewById(R.id.llyAttestateViewHistory);

        btnBack = (Button) findViewById(R.id.btnAttestateViewBack);
        btnBack.setOnClickListener(this);

        attestate_id = getIntent().getStringExtra("AttestateId");

        reload_data();
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    private void reload_data() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        // Считываем данные текущего удостоверения
        Cursor c = db.query("docs", new String[]{"id", "doc", "content_id", "t_create"}, "id = '" + attestate_id + "' AND current = 1", null, null, null, null, "1");

        Gson gson = new Gson();
        String content_id = null;
        Long create_time = (long) -1;
        if (c != null && c.moveToFirst()) {
            String doc_json = c.getString(c.getColumnIndex("doc"));
            DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
            String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

            txtName.setText(AMain.get_personal_name(doc_data[2]));
            txtPersonalId.setText(doc_data[2]);
            txtPublicKeyId.setText(doc_data[3]);
            txtLevel.setText(doc_data[4]);
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

                    Log.d("ATT_FROM_DB", "Level = "+doc_data[4]);

                    HashMap<String, Object> m = new HashMap<String, Object>();
                    m.put("t_create", doc_data[1]);
                    m.put("level", doc_data[4]);

                    items_list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        Log.d("ATT_FROM_DB", "List size = " + items_list.size());

        // listHistory.setChoiceMode(ListView.CHOICE_MODE_NONE);
        // sAdapter = new AttHistoryListArrayAdapter(this, items_list);
        // listHistory.setAdapter(sAdapter);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 0;i<items_list.size();i++) {
            HashMap<String, Object> m = (HashMap<String, Object>) items_list.get(i);

            View rowView = inflater.inflate(R.layout.list_item_attestate_history, null);

            TextView txtTime = (TextView) rowView.findViewById(R.id.txtAttestateHistoryTime);
            TextView txtLevel = (TextView) rowView.findViewById(R.id.txtAttestateHistoryLevel);

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
