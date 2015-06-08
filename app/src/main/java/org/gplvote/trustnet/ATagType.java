package org.gplvote.trustnet;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

public class ATagType extends ActionBarActivity implements View.OnClickListener {

    private TextView txtTagId;
    private TextView txtTagName;
    private TextView txtTagInfo;
    private Button btnBack;
    private Button btnSave;

    private HashMap<String, Object> tag_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_type);

        // Получаем URL из QR кода и показываем код и описание дополнительного удостоверения
        // И спрашиваем добавить-ли его в локальную базу?

        txtTagId = (TextView) findViewById(R.id.txtTagTypeId);
        txtTagName = (TextView) findViewById(R.id.txtTagTypeShortName);
        txtTagInfo = (TextView) findViewById(R.id.txtTagTypeInfo);
        btnBack = (Button) findViewById(R.id.btnTagTypeBack);
        btnSave = (Button) findViewById(R.id.btnTagTypeSave);

        btnBack.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        Intent i = getIntent();
        Uri d = null;
        if (i != null) d = i.getData();

        String json_tag_info = null;
        if (d != null && d.getScheme().equals("trustnet") && d.getHost().equals("taginfo")) {
            json_tag_info = d.getPath();
            json_tag_info = json_tag_info.replaceAll("^/", "");
        }

        if (json_tag_info == null || json_tag_info.isEmpty()) {
            Log.e("ConfirmTrust", "Error when get attestation info from URL");
            throw new RuntimeException("ConfirmTrust: Error when get attestation info from URL");
        }

        Gson gson = new Gson();
        tag_info = gson.fromJson(json_tag_info, new TypeToken<HashMap<String, Object>>() {}.getType());

        txtTagId.setText((String) tag_info.get("id"));
        txtTagName.setText((String) tag_info.get("nm"));
        txtTagInfo.setText((String) tag_info.get("inf"));
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnTagTypeBack:
                finish();
                break;
            case R.id.btnTagTypeSave:
                insert_tag_type();
                finish();
                break;
        }
    }

    private void insert_tag_type() {
        SQLiteDatabase db = AMain.db.getWritableDatabase();

        ContentValues cv = new ContentValues();

        String id = (String) tag_info.get("id");
        cv.put("id", id);
        cv.put("name", (String) tag_info.get("nm"));
        cv.put("info", (String) tag_info.get("inf"));

        String name = AMain.get_tag_name(id);
        if (name == null || name.isEmpty()) {
            db.insert("tags_info", null, cv);
        } else {
            db.update("tags_info", cv, "id = ?", new String[]{id});
        }
    }
}
