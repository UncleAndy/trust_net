package org.gplvote.trustnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AServers  extends QRReaderActivity implements View.OnClickListener{

    private Button btnView;
    private ListView listServersView;
    private ServersListArrayAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servers);

        btnView = (Button) findViewById(R.id.btnServerView);
        btnView.setOnClickListener(this);

        reload_servers();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnServerView:
                break;
        }
    }

    public void reload_servers() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(100);
        Map<String, Object> m;

        Log.d("SERVERS", "Run update_list");

        // Заполнение m данными из таблицы документов
        DbHelper dbStorage = DbHelper.getInstance(this);
        SQLiteDatabase db = dbStorage.getWritableDatabase();

        Cursor c = db.query("servers", new String[]{"id", "host", "source", "t_last_online", "t_create"}, null, null, null, null, "source, t_create desc", "100");

        if (c != null) {
            Log.d("SERVERS", "update_list p1");
            if (c.moveToFirst()) {
                Log.d("SERVERS", "update_list p2");
                do {
                    Log.d("SERVERS", "update_list p3");
                    m = new HashMap<String, Object>();

                    m.put("id", c.getString(c.getColumnIndex("id")));
                    m.put("host", c.getString(c.getColumnIndex("host")));
                    m.put("source", c.getString(c.getColumnIndex("source")));
                    m.put("t_last_online", c.getString(c.getColumnIndex("t_last_online")));
                    m.put("t_create", c.getString(c.getColumnIndex("t_create")));

                    list.add(m);
                } while (c.moveToNext());
            }
        }

        listServersView = (ListView) findViewById(R.id.listServersView);
        listServersView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sAdapter = new ServersListArrayAdapter(this, list);
        listServersView.setAdapter(sAdapter);

        listServersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) listServersView.getItemAtPosition(position);

                sAdapter.setCurrentPosition(position);
                sAdapter.notifyDataSetChanged();
            }
        });
    }


    public class ServersListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> list;
        private int currentPosition = -1;

        public ServersListArrayAdapter(Context context, List<Map<String, Object>> objects) {
            super(context, R.layout.list_item_server, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;
            Log.d("SERVERS", "Start getView for position = " + position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_server, parent, false);
            } else {
                rowView = convertView;
            }

            TextView txtHost = (TextView) rowView.findViewById(R.id.txtSrvListHost);

            // Time parse
            String t_last_online = time_to_string(Long.parseLong((String) list.get(position).get("t_last_online")));
            String t_create = time_to_string(Long.parseLong((String) list.get(position).get("t_create")));

            // Status parse
            String host = (String) list.get(position).get("host");
            String source = (String) list.get(position).get("source");

            // Если исходник из кода - подчеркиваем
            if ((source != null) && source.equals("code")) {
                txtHost.setPaintFlags(txtHost.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            txtHost.setText(host);

            CheckedTextView checkedTextView = (CheckedTextView) rowView.findViewById(R.id.radioServerListSel);
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

        private String time_to_string(Long time) {
            if (time == null || time <= 0) return("");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return(sdf.format(time));
        }

        private String time_to_string(String time) {
            Long time_long;
            try {
                time_long = Long.parseLong(time);
            } catch (Exception e) {
                time_long = 0L;
            }

            return(time_to_string(time_long));
        }
    }
}
