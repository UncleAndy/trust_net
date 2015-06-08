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

// Реализовать вывод типов тэгов с переходм внутрь по клику на строке
public class ATags extends ActionBarActivity implements View.OnClickListener {
    private ListView listTags;
    private Button btnBack;

    private TagsListArrayAdapter sAdapter;

    public static ATags instance = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags);

        instance = this;

        listTags = (ListView) findViewById(R.id.listTags);
        btnBack = (Button) findViewById(R.id.btnTagsBack);

        btnBack.setOnClickListener(this);

        reload_data();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        HashMap<String, Object> item;
        int curPosition;
        switch(v.getId()) {
            case R.id.btnTagsBack:
                instance = null;
                finish();
                break;
        }
    }

    public void reload_data() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>(100);
        Map<String, Object> m;

        Log.d("TAGS", "Run update_list");

        // Заполнение m данными из таблицы документов
        DbHelper dbStorage = DbHelper.getInstance(this);
        SQLiteDatabase db = dbStorage.getWritableDatabase();

        Cursor c = db.query("docs", new String[]{"id", "doc", "t_create"}, "type = '" + DocTag.DOC_TYPE + "' AND current = 1", null, null, null, "t_create desc, content_id", "100");

        Gson gson = new Gson();
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    // Извлекаем данные из документа
                    String doc_json = c.getString(c.getColumnIndex("doc"));
                    DocSigned doc = gson.fromJson(doc_json, DocSigned.class);
                    String[] doc_data = gson.fromJson(doc.dec_data, String[].class);

                    m = new HashMap<String, Object>();

                    m.put("id", c.getString(c.getColumnIndex("id")));
                    m.put("tag_id", doc_data[2]);
                    m.put("tag_name", AMain.get_tag_name(doc_data[2]));
                    m.put("person_id", doc_data[3]);
                    m.put("person_name", AMain.get_personal_name(doc_data[3]));
                    m.put("level", doc_data[5]);

                    list.add(m);
                } while (c.moveToNext());
            }
            c.close();
        }

        listTags = (ListView) findViewById(R.id.listTags);
        listTags.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        sAdapter = new TagsListArrayAdapter(this, list);
        listTags.setAdapter(sAdapter);

        listTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, Object> item = (HashMap<String, Object>) listTags.getItemAtPosition(position);

                sAdapter.setCurrentPosition(position);
                // sAdapter.notifyDataSetChanged();

                // TODO: Переход "внутрь" выбранного пункта (просмотр с возможностью изменения)
                Log.d("TAG_CLICK", (String) item.get("id"));

                Intent intent = new Intent(ATags.this, ATagChange.class);
                intent.putExtra("TagId", (String) item.get("id"));
                startActivity(intent);
            }
        });
    }

    public class TagsListArrayAdapter extends ArrayAdapter<Map<String, Object>> {
        private final Context context;
        private final List<Map<String, Object>> list;
        private int currentPosition = -1;

        public TagsListArrayAdapter(Context context, List<Map<String, Object>> objects) {
            super(context, R.layout.list_item_tag, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.list_item_tag, parent, false);
            } else {
                rowView = convertView;
            }

            TextView txtName = (TextView) rowView.findViewById(R.id.txtTagListName);
            TextView txtTagName = (TextView) rowView.findViewById(R.id.txtTagListTagName);
            TextView txtLevel = (TextView) rowView.findViewById(R.id.txtTagListLevel);

            String name = (String) list.get(position).get("person_name");
            if (name == null || name.isEmpty())
                name = (String) list.get(position).get("person_id");
            String tag_name = (String) list.get(position).get("tag_name");
            if (tag_name == null || tag_name.isEmpty())
                tag_name = (String) list.get(position).get("tag_id");
            String level = (String) list.get(position).get("level");

            txtName.setText(name);
            txtTagName.setText(tag_name);
            txtLevel.setText(level);

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
