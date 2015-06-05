package org.gplvote.trustnet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AAttestations extends ActionBarActivity implements View.OnClickListener {
    private ListView listAttestates;
    private Button btnBack;
    private Button btnView;
    private Button btnRecreate;

    private AttestationsListArrayAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attestations);

        listAttestates = (ListView) findViewById(R.id.listAttestations);
        btnBack = (Button) findViewById(R.id.btnAttestateBack);
        btnView = (Button) findViewById(R.id.btnAttestateView);
        btnRecreate = (Button) findViewById(R.id.btnAttestateRecreate);

        btnView.setVisibility(View.GONE);
        btnRecreate.setVisibility(View.GONE);

        btnBack.setOnClickListener(this);
        btnView.setOnClickListener(this);
        btnRecreate.setOnClickListener(this);

        reload_data();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAttestateBack:
                finish();
                break;
            case R.id.btnAttestateView:
                // TODO: Реализовать просмотр удостоверения
                break;
            case R.id.btnAttestateRecreate:
                // TODO: Реализовать перевыпуск удостоверения
                break;
        }
    }


    public void reload_data() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(100);
        Map<String, Object> m;

        Log.d("ATTESTATIONS", "Run update_list");

        // Заполнение m данными из таблицы документов
        DbHelper dbStorage = DbHelper.getInstance(this);
        SQLiteDatabase db = dbStorage.getWritableDatabase();

        Cursor c = db.query("docs", new String[]{"id", "doc", "t_create"}, "type = '" + DocAttestation.DOC_TYPE + "'", null, null, null, "t_create desc, content_id", "100");

        Gson gson = new Gson();
        if (c != null) {
            Log.d("ATTESTATIONS", "update_list p1");
            if (c.moveToFirst()) {
                Log.d("SERVERS", "update_list p2");
                do {
                    Log.d("ATTESTATIONS", "update_list p3");
                    m = new HashMap<String, Object>();

                    m.put("id", c.getString(c.getColumnIndex("id")));

                    // Извлекаем данные из документа
                    String doc_json = c.getString(c.getColumnIndex("doc"));
                    DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                    String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                    // Имя извлекаем из отдельной таблицы соответствия персональных идентификаторов именам
                    m.put("name", AMain.get_personal_name(doc_data[2]));
                    m.put("t_create", doc_data[1]);
                    m.put("personal_id", doc_data[2]);
                    m.put("public_key_id", doc_data[3]);

                    list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        listAttestates = (ListView) findViewById(R.id.listAttestations);
        listAttestates.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sAdapter = new AttestationsListArrayAdapter(this, list);
        listAttestates.setAdapter(sAdapter);

        listAttestates.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) listAttestates.getItemAtPosition(position);

                sAdapter.setCurrentPosition(position);
                sAdapter.notifyDataSetChanged();

                // Ставим статус кнопки "Просмотр" в зависимости от статуса текущего выбранного документа
                if (item != null) {
                    btnView.setVisibility(View.VISIBLE);
                    btnRecreate.setVisibility(View.VISIBLE);
                } else {
                    btnView.setVisibility(View.GONE);
                    btnRecreate.setVisibility(View.GONE);
                }
            }
        });
    }

    public class AttestationsListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> list;
        private int currentPosition = -1;

        public AttestationsListArrayAdapter(Context context, List<Map<String, Object>> objects) {
            super(context, R.layout.list_item_attestation, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            Log.d("ATTESTATES", "Start getView for position = " + position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_attestation, parent, false);
            } else {
                rowView = convertView;
            }

            TextView txtName = (TextView) rowView.findViewById(R.id.txtAttestateListName);
            TextView txtTime = (TextView) rowView.findViewById(R.id.txtAttestateListTime);
            TextView txtPersonalId = (TextView) rowView.findViewById(R.id.txtAttestateListPersonalId);
            TextView txtPublicKeyId = (TextView) rowView.findViewById(R.id.txtAttestateListPublicKeyId);

            // Time parse
            String t_create = (String) list.get(position).get("t_create");
            if (t_create != null && !t_create.isEmpty())
                t_create = AMain.time_to_string(Long.parseLong(t_create));
            else
                t_create = "-";

            String name = (String) list.get(position).get("name");
            String personal_id = (String) list.get(position).get("personal_id");
            String public_key_id = (String) list.get(position).get("public_key_id");

            txtName.setText(name);
            txtTime.setText(t_create);
            txtPersonalId.setText(personal_id);
            txtPublicKeyId.setText(public_key_id);

            CheckedTextView checkedTextView = (CheckedTextView) rowView.findViewById(R.id.radioAttestationListSel);
            if (position == currentPosition) {
                checkedTextView.setChecked(true);
            } else {
                checkedTextView.setChecked(false);
            }

            return rowView;
        }

        public void setCurrentPosition(int position) {
            currentPosition = position;
        }

        public int getCurrentPosition() {
            return(currentPosition);
        }
    }

}
