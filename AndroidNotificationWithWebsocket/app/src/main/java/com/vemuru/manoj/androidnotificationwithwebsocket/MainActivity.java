package com.vemuru.manoj.androidnotificationwithwebsocket;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends Activity {
    private WebSocketClient mWebSocketClient;
    private EditText editText;
    private EditText toEditText,fromEditText;
    private GoogleCloudMessaging gcm;
    private String regid;
    private String PROJECT_NUMBER = "424656961184";
    private String msg = "";
    public static boolean isConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        connectWebSocket();

        editText = (EditText) findViewById(R.id.edittext);
        toEditText = (EditText) findViewById(R.id.edittextto);
        fromEditText =(EditText) findViewById(R.id.edittextid);
        ((Button) findViewById(R.id.register)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                JSONObject o = new JSONObject();
                try {
                    jsonObject.put("vidaoId",fromEditText.getText().toString());
                    jsonObject.put("deviceId",regid);
                    jsonObject.put("type","android");
                    o.put("category","register");
                    o.put("data",jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mWebSocketClient.send(o.toString());
            }
        });
        ((Button) findViewById(R.id.send)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("category", "chat");

                    JSONObject o = new JSONObject();
                    o.put("to", toEditText.getText().toString());

                    JSONObject msgObject = new JSONObject();

                    msgObject.put("msgdata", editText.getText().toString());

                    msgObject.put("from", fromEditText.getText().toString());     // change to actual from vidaoID for production

                    o.put("msg", msgObject);

                    jsonObject.put("data", o);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mWebSocketClient.send(jsonObject.toString());

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mWebSocketClient == null)
        getRegId();

    }

    @Override
    protected void onPause() {
        super.onPause();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("disconnect","device Disconnected");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebSocketClient.send(jsonObject.toString());
        mWebSocketClient.close();
        isConnected= false;
    }

    public void getRegId(){
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM",  msg);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
//                etRegId.setText(msg + "\n");
                Log.d("reg Id",msg);
                connectWebSocket();
            }
        }.execute(null, null, null);
    }
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://192.168.1.118:1337");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
//                JSONObject jsonObject = new JSONObject();
//                JSONObject o = new JSONObject();
//                try {
//                    jsonObject.put("vidaoId","manoj");
//                    jsonObject.put("deviceId",regid);
//                    jsonObject.put("type","android");
//                        o.put("category","register");
//                    o.put("data",jsonObject);
//
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                mWebSocketClient.send(o.toString());
                isConnected = true;
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
//        mWebSocketClient.send("inti");
    }

}
