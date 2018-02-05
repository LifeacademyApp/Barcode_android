package com.example.danny.scanbarcode;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.os.Handler;
import java.io.*;
import android.os.AsyncTask;


import android.net.Uri;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class MainActivity extends Activity {
    private final static int CAMERA_RESULT = 0;
    private static final int ZXING_SCAN = 3;
    private String Content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        callCamera();

    }
    public void callCamera(){
        String[] permissionNeed = {
                Manifest.permission.CAMERA,
        };
        if( hasPermission(permissionNeed)){
            Scanner();
        }else {
            getPermission();
        }
    }

    @TargetApi(23)
    public void getPermission(){
        if(Build.VERSION.SDK_INT>=23) {
            String[] permissionNeed = {
                    Manifest.permission.CAMERA,
            };
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(this, "需要相機權限掃描條碼", Toast.LENGTH_SHORT).show();

            }
            requestPermissions(permissionNeed, CAMERA_RESULT);
        }
    }

    public void Scanner(){

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("請對準條碼");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();

    }
    private boolean hasPermission(String[] permission) {
        if (canMakeSmores()) {
            for (String permissions : permission) {

                return (ContextCompat.checkSelfPermission(this, permissions) == PackageManager.PERMISSION_GRANTED);

            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int [] grantResults ){
        switch (requestCode){
            case CAMERA_RESULT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Scanner();
                } else {

                    Toast.makeText(this,"需要相機權限掃描條碼",Toast.LENGTH_SHORT).show();
                }
                break;


            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }}
    private boolean canMakeSmores() {
        return(Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void sendResult(String code) {
        Intent tIntent = this.getIntent();         //取得Schema，值為：usccbarcode
        Uri myURI = tIntent.getData();            //取得URL的URI
        String tValue = myURI.getQueryParameter("returnurl");    //取得URL中的Query String參數
        //String[] get_url=myURI.toString().split("=");
        //String send_url = "http://" + get_url[0].substring(21) + "=" + code;
        //String send_url = "http://mmm.lifeacademy.org/erpweb/Home/PutScanCode?code=" + code;
        String return_url = "http://" + tValue + "?code=" + code;
        //String return_url = "http://www.yahoo.com.tw";

        // /Toast.makeText(this, myURI.toString() ,Toast.LENGTH_SHORT).show();
        Toast.makeText(this, return_url ,Toast.LENGTH_SHORT).show();

        //開始送字串
        //new SendingPacketTask().execute(send_url);
        if (myURI.getQueryParameter("returnurl")==null){
            return_url = "http://" + myURI.toString().substring(21) + "?code=" + code;
        }
        Intent intent_main = new Intent(Intent.ACTION_VIEW);
        intent_main.setData(Uri.parse(return_url));

        //pause(200);
        startActivity(intent_main);

    }

    class SendingPacketTask extends AsyncTask<String, Integer, Integer>{
        @Override
        protected Integer doInBackground(String... param) {
            request(param[0]);
            return null;
        }
        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private void request(String urlString) {
        try{
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setReadTimeout(0);
            connection.setConnectTimeout(0);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                int code = 200;
            } else {
                //Toast.makeText(this, "Failure" ,Toast.LENGTH_SHORT).show();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
       }
    }


    public void pause(int mSec) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
            }
        }, mSec);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {  //假如有收到掃描結果資料
            if(result.getContents() == null) {   //假如是空的資料
                Log.d("MainActivity", "Cancelled scan");   //紀錄掃描失敗
                Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();  //顯示"取消"
            } else {   //假如掃瞄有成功
                Log.d("MainActivity", "Scanned");  //紀錄掃描成功
                Toast.makeText(this, "掃描結果: " + result.getContents(), Toast.LENGTH_LONG).show();   //顯示掃描出的條碼內容
                if (this.getIntent().getDataString() != null) {
                    sendResult(result.getContents()); //送條碼資料到伺服器
                    this.finish();
                }
                else {
                    Toast.makeText(this, "請從網頁開啟本程式", Toast.LENGTH_LONG).show();  //顯示"取消"
                    this.finish();
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}