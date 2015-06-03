package org.gplvote.trustnet;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AServers  extends QRReaderActivity implements View.OnClickListener{
    private Button btnView;
    private Button btnBack;
    private ListView listServersView;
    private ServersListArrayAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servers);

        btnView = (Button) findViewById(R.id.btnServerView);
        btnView.setOnClickListener(this);
        btnView.setVisibility(View.GONE);

        btnBack = (Button) findViewById(R.id.btnServersBack);
        btnBack.setOnClickListener(this);

        reload_servers();

        // Check server registration over URI
        Intent i = getIntent();
        Uri d = null;
        if (i != null) d = i.getData();

        if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("regserver")) {
            String reg_host = d.getPath();
            reg_host = reg_host.replaceAll("^/", "");
            register_server(reg_host);
            reload_servers();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnServerView:
                int curPosition = sAdapter.getCurrentPosition();

                if (curPosition < 0) {
                    return;
                }

                HashMap<String, Object> item = (HashMap<String, Object>) listServersView.getItemAtPosition(curPosition);

                Gson gson = new Gson();
                intent = new Intent(this, AServerView.class);
                intent.putExtra("Server", gson.toJson(item));
                startActivity(intent);
                break;
            case R.id.btnServersBack:
                finish();
                break;
        }
    }

    public void register_server(String host) {
        // 1. Send PING packet to new server
        // 2. If PING is OK, add server to DB
        // 3. Send request for get trusted servers list
        // 3.1. Add new servers in DB after PING
        // 4. Send to main server user public key
        // 5. Send to main server all certifications (main and addition)
        Log.d("register_server", "Start");

        PacketPing ping = new PacketPing();
        Log.i("PacketPing", "Doc = "+ping.doc.hashCode());
        if (!ping.send(host))
            return;
        if (!Servers.add(host, Servers.SOURCE_DIRECT))
            return;

        Log.d("register_server", "Add from server");
        Servers.add_from_server(host);

        Settings settings = Settings.getInstance(this);
        DataPersonalInfo pi = settings.getPersonalInfo();

        Log.d("register_server", "Send public key");
        send_public_key(host);

        Log.d("register_server", "Send all local packets");
        ArrayList<PacketSigned> list =  PacketSigned.db_list();
        for(int i = 0; i < list.size(); i++) {
            PacketSigned packet = list.get(i);
            packet.send(host);
        }
    }

    public void reload_servers() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(100);
        Map<String, Object> m;

        Log.d("SERVERS", "Run update_list");

        // Заполнение m данными из таблицы серверов
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
            c.close();
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

                // Ставим статус кнопки "Просмотр" в зависимости от статуса текущего выбранного документа
                if (item != null) {
                    btnView.setVisibility(View.VISIBLE);
                } else {
                    btnView.setVisibility(View.GONE);
                }
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
            String t_last_online = (String) list.get(position).get("t_last_online");
            if (t_last_online != null && !t_last_online.isEmpty())
                t_last_online = time_to_string(Long.parseLong(t_last_online));
            String t_create = (String) list.get(position).get("t_create");
            if (t_create != null && !t_create.isEmpty())
                t_create = time_to_string(Long.parseLong(t_create));

            // Status parse
            String host = (String) list.get(position).get("host");
            String source = (String) list.get(position).get("source");

            // Если исходник из кода - подчеркиваем
            if ((source != null) && source.equals(Servers.SOURCE_DIRECT)) {
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
    }

    public void send_public_key(String hosts) {
        DataPersonalInfo pi = AMain.settings.getPersonalInfo();
        if (pi == null) return;

        // 1. Ищем в базе документов нужный документ
        // 2. Если его нет - добавляем в базу без подписи
        // 3. Если он не подписан - ставим в очередь на подписание и отправку через ASendSign


        // 1.
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        long public_key_doc_id = -1;
        Boolean public_key_signed = false;

        Cursor c = db.query("docs", new String[]{"id", "doc", "sign"}, "type = 'PUBLIC_KEY'", null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    String doc_json = c.getString(c.getColumnIndex("doc"));

                    Log.d("PK_FROM_DB", "Doc PK = "+doc_json);

                    if (doc_json != null && !doc_json.isEmpty()) {
                        Gson gson = new Gson();
                        DocSigned doc = gson.fromJson(doc_json, DocSigned.class);

                        String[] data = gson.fromJson(doc.dec_data, String[].class);

                        if (data[0].equals(pi.personal_id) && (data[2].equals(pi.public_key))) {
                            String sign = c.getString(c.getColumnIndex("sign"));
                            public_key_signed = !(sign == null || sign.isEmpty());
                            public_key_doc_id = c.getInt(c.getColumnIndex("id"));
                            break;
                        }
                    }
                } while (c.moveToNext());
            }
            c.close();
        }

        // 2.
        PacketSigned pack = null;
        if (public_key_doc_id == -1) {
            DocPublicKey doc = new DocPublicKey();
            doc.site = AMain.SIGN_DOC_APP_TYPE;
            doc.template = getString(R.string.template_doc_public_key);
            doc.dec_data = "[\"" + pi.personal_id + "\",\"" + String.valueOf(System.currentTimeMillis()) + "\",\"" + pi.public_key + "\",\""+ pi.cancel_public_key +"\"]";

            Gson gson = new Gson();
            Log.d("send_public_key", "Doc = "+gson.toJson(doc));
            pack = doc.get_packet();

            Log.d("send_public_key", "Packet = "+gson.toJson(pack));
            pack.insert(DocPublicKey.DOC_TYPE);
            public_key_signed = false;
        } else {
            pack = new PacketSigned(String.valueOf(public_key_doc_id));
        }

        // 3.
        ASendSign.add_to_queue(pack.doc, hosts, !public_key_signed);

        Intent intent = new Intent(this, ASendSign.class);
        startActivity(intent);
    }

    public static String time_to_string(Long time) {
        if (time == null || time <= 0) return("");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return(sdf.format(time));
    }

    public static String time_to_string(String time) {
        Long time_long;
        try {
            time_long = Long.parseLong(time);
        } catch (Exception e) {
            time_long = 0L;
        }

        return(time_to_string(time_long));
    }
}
