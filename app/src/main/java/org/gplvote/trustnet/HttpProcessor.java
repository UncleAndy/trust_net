package org.gplvote.trustnet;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpProcessor {
    private HttpClient httpclient;
    public JsonResponse json_response;

    public HttpProcessor() {
        httpclient = new DefaultHttpClient();
    }

    public boolean postData(String url, String json) {
        Log.d("HTTP postData", "HTTP deliver doc to "+url+": " + json);
        HttpPost httppost = new HttpPost(url);

        try {
            // Add your data
            Log.d("http_processor", "Send data: "+json);
            StringEntity se = null;
            se = new StringEntity(json, "UTF-8");
            httppost.setEntity(se);
            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            String body = EntityUtils.toString(response.getEntity(), "UTF-8");
            int http_status = response.getStatusLine().getStatusCode();
            if (http_status == 200) {
                Log.d("HTTP postData", "HTTP response 200 with body: " + body);
                Gson gson = new Gson();
                json_response = gson.fromJson(body, JsonResponse.class);
                return (json_response != null && json_response.status == 200);
            } else {
                Log.e("HTTP postData", "HTTP response: "+http_status+" body: "+body);
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            Log.e("HTTP postData", "Error HTTP request: ", e);
            return false;
        } catch (ClientProtocolException e) {
            Log.e("HTTP postData", "Error HTTP request: ", e);
            return false;
        } catch (IOException e) {
            Log.e("HTTP postData", "Error HTTP request: ", e);
            e.printStackTrace();
            return false;
        }
    }

    public String getData(String url) {
        Log.d("HTTP getData", "URL = "+url);

        HttpGet httpget = null;
        try {
            httpget = new HttpGet(new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpclient.execute(httpget);

            String body = EntityUtils.toString(response.getEntity(), "UTF-8");

            Log.d("HTTP getData", "Answer body = "+body);
            int http_status = response.getStatusLine().getStatusCode();
            if (http_status == 200) {
                Log.d("HTTP getData", "Status 200");
                return body;
            } else {
                Log.e("AsyncHTTP", "Error HTTP response status " + http_status);
                return null;
            }
        } catch (Exception e) {
            Log.e("AsyncHTTP", "Error HTTP request: ", e);
            return null;
        }
    }
}
