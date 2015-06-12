package org.gplvote.trustnet;

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AServersSync extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AMain.isInternetPresent(this)) {
            String hosts = getIntent().getStringExtra("Hosts");
            if (hosts == null || hosts.isEmpty())
                hosts = "*";

            setContentView(R.layout.progress);
            sync(hosts);
            finish();
        } else {
            setContentView(R.layout.error);
            TextView txtError = (TextView) findViewById(R.id.txtError);
            txtError.setText(R.string.error_internet_required);
        }
    }

    private void sync(String hosts) {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        // Сначала отправляем публичные ключи
        Cursor c = db.query("docs", new String[]{"id", "doc", "sign"}, "type = 'PUBLIC_KEY' AND sign IS NOT NULL AND sign != ''", null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ASendSign.add_to_queue(c.getString(c.getColumnIndex("id")), hosts, false);
                } while (c.moveToNext());
            }
            c.close();
        }

        // Затем все остальные данные
        c = db.query("docs", new String[]{"id", "doc", "sign"}, "type != 'PUBLIC_KEY' AND sign IS NOT NULL AND sign != ''", null, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ASendSign.add_to_queue(c.getString(c.getColumnIndex("id")), hosts, false);
                } while (c.moveToNext());
            }
            c.close();
        }

        Intent intent = new Intent(this, ASendSign.class);
        startActivity(intent);
    }
}
