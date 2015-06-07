package org.gplvote.trustnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class ATrusts extends ActionBarActivity implements View.OnClickListener {
    private ListView listTrusts;
    private Button btnBack;
    private Button btnView;
    private Button btnChange;

    private TrustsListArrayAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trusts);

        listTrusts = (ListView) findViewById(R.id.listTrusts);
        btnBack = (Button) findViewById(R.id.btnTrustBack);
        btnView = (Button) findViewById(R.id.btnTrustView);
        btnChange = (Button) findViewById(R.id.btnTrustChange);

        btnView.setVisibility(View.GONE);
        btnChange.setVisibility(View.GONE);

        btnBack.setOnClickListener(this);
        btnView.setOnClickListener(this);
        btnChange.setOnClickListener(this);

        reload_data();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()) {
            case R.id.btnTrustBack:
                finish();
                break;
            case R.id.btnTrustView:
                int curPosition = sAdapter.getCurrentPosition();

                if (curPosition < 0) {
                    return;
                }

                HashMap<String, Object> item = (HashMap<String, Object>) listTrusts.getItemAtPosition(curPosition);

                intent = new Intent(this, ATrustView.class);
                intent.putExtra("TrustId", (String) item.get("id"));
                startActivity(intent);
                break;
            case R.id.btnTrustChange:
                break;
        }
    }


    public void reload_data() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(100);
        Map<String, Object> m;

        Log.d("TRUSTS", "Run update_list");

        // Заполнение m данными из таблицы документов
        DbHelper dbStorage = DbHelper.getInstance(this);
        SQLiteDatabase db = dbStorage.getWritableDatabase();

        Cursor c = db.query("docs", new String[]{"id", "doc", "t_create"}, "type = '" + DocTrust.DOC_TYPE + "' AND current = 1", null, null, null, "t_create desc, content_id", "100");

        Gson gson = new Gson();
        if (c != null) {
            Log.d("TRUSTS", "update_list p1");
            if (c.moveToFirst()) {
                Log.d("TRUSTS", "update_list p2");
                do {
                    Log.d("TRUSTS", "update_list p3");
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
                    m.put("level", doc_data[3]);

                    list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        listTrusts = (ListView) findViewById(R.id.listTrusts);
        listTrusts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sAdapter = new TrustsListArrayAdapter(this, list);
        listTrusts.setAdapter(sAdapter);

        listTrusts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) listTrusts.getItemAtPosition(position);

                sAdapter.setCurrentPosition(position);
                sAdapter.notifyDataSetChanged();

                // Ставим статус кнопки "Просмотр" в зависимости от статуса текущего выбранного документа
                if (item != null) {
                    btnView.setVisibility(View.VISIBLE);
                    btnChange.setVisibility(View.VISIBLE);
                } else {
                    btnView.setVisibility(View.GONE);
                    btnChange.setVisibility(View.GONE);
                }
            }
        });
    }

    public class TrustsListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> list;
        private int currentPosition = -1;

        public TrustsListArrayAdapter(Context context, List<Map<String, Object>> objects) {
            super(context, R.layout.list_item_trust, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            Log.d("TrustS", "Start getView for position = " + position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_trust, parent, false);
            } else {
                rowView = convertView;
            }

            TextView txtName = (TextView) rowView.findViewById(R.id.txtTrustListName);
            TextView txtTime = (TextView) rowView.findViewById(R.id.txtTrustListTime);
            TextView txtPersonalId = (TextView) rowView.findViewById(R.id.txtTrustListPersonalId);
            TextView txtLevel = (TextView) rowView.findViewById(R.id.txtTrustLevel);

            // Time parse
            String t_create = (String) list.get(position).get("t_create");
            if (t_create != null && !t_create.isEmpty())
                t_create = AMain.time_to_string(Long.parseLong(t_create));
            else
                t_create = "-";

            String name = (String) list.get(position).get("name");
            String personal_id = (String) list.get(position).get("personal_id");
            String level = (String) list.get(position).get("level");

            txtName.setText(name);
            txtTime.setText(t_create);
            txtPersonalId.setText(personal_id);
            txtLevel.setText(level);

            CheckedTextView checkedTextView = (CheckedTextView) rowView.findViewById(R.id.radioTrustListSel);
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
