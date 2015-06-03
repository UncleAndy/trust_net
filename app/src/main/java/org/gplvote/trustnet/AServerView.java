package org.gplvote.trustnet;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.gplvote.trustnet.R;

import java.util.HashMap;

public class AServerView extends ActionBarActivity implements View.OnClickListener {

    private TextView txtHost;
    private TextView txtSource;
    private TextView txtCreateTime;
    private TextView txtOnlineTime;
    private ImageView imgQRCode;

    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_view);

        txtHost         = (TextView) findViewById(R.id.txtServerHost);
        txtSource       = (TextView) findViewById(R.id.txtServerSource);
        txtCreateTime   = (TextView) findViewById(R.id.txtServerCreateTime);
        txtOnlineTime   = (TextView) findViewById(R.id.txtServerLastOnlineTime);
        btnBack         = (Button) findViewById(R.id.btnServerViewBack);
        imgQRCode       = (ImageView) findViewById(R.id.imgServerQR);

        btnBack.setOnClickListener(this);

        String server_json = getIntent().getStringExtra("Server");
        Gson gson = new Gson();
        HashMap<String, Object> item = gson.fromJson(server_json, new TypeToken<HashMap<String, Object>>() {}.getType());

        txtHost.setText((String) item.get("host"));
        txtSource.setText((String) item.get("source"));
        txtCreateTime.setText(AServers.time_to_string((String) item.get("t_create")));
        txtOnlineTime.setText(AServers.time_to_string((String) item.get("t_last_online")));

        AMain.showQRCode(AMain.TRUSTNET_INT_URL_REG_SERVER+"/"+txtHost.getText(), imgQRCode);
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}
