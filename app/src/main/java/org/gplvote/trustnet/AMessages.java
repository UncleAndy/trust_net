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
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Вывод имеющихся входящих сообщений
public class AMessages extends ActionBarActivity implements View.OnClickListener {
    private ListView listMessages;
    private Button btnBack;
    private Button btnCheck;

    private MessagesListArrayAdapter sAdapter;

    public static AMessages instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        instance = this;

        listMessages = (ListView) findViewById(R.id.listMessages);
        btnBack = (Button) findViewById(R.id.btnMessagesBack);
        btnCheck = (Button) findViewById(R.id.btnMessagesCheck);

        btnBack.setOnClickListener(this);
        btnCheck.setOnClickListener(this);

        reload_data();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        HashMap<String, Object> item;
        int curPosition;
        switch(v.getId()) {
            case R.id.btnMessagesBack:
                instance = null;
                finish();
                break;
            case R.id.btnMessagesCheck:
                intent = new Intent(this, AMessagesCheck.class);
                startActivity(intent);
                break;
        }
    }

    public void reload_data() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;

        Log.d("MESSAGES", "Run update_list");

        // Заполнение m данными из таблицы документов
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        Cursor c = db.query("message_inbox", new String[]{"id", "sender", "dec_text", "is_decrypted", "t_create"}, "is_decrypted = 1", null, null, null, "t_create desc", "100");

        Gson gson = new Gson();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    m = new HashMap<String, Object>();

                    m.put("id", c.getString(c.getColumnIndex("id")));
                    m.put("sender", c.getString(c.getColumnIndex("sender")));
                    m.put("text", c.getString(c.getColumnIndex("dec_text")));

                    list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        listMessages = (ListView) findViewById(R.id.listMessages);
        listMessages.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sAdapter = new MessagesListArrayAdapter(this, list);
        listMessages.setAdapter(sAdapter);

        listMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) listMessages.getItemAtPosition(position);

                sAdapter.setCurrentPosition(position);

                Intent intent = new Intent(AMessages.this, AMessageCreate.class);
                intent.putExtra("MessageTo", (String) item.get("sender"));
                intent.putExtra("MessageText", (String) item.get("text"));
                startActivity(intent);
            }
        });
    }

    public class MessagesListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> list;
        private int currentPosition = -1;

        public MessagesListArrayAdapter(Context context, List<Map<String, Object>> objects) {
            super(context, R.layout.list_item_message, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_message, parent, false);
            } else {
                rowView = convertView;
            }

            TextView txtFrom = (TextView) rowView.findViewById(R.id.txtListMessageFrom);
            TextView txtText = (TextView) rowView.findViewById(R.id.txtListMessageText);

            txtFrom.setText((String) list.get(position).get("sender"));
            txtText.setText((String) list.get(position).get("text"));

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
