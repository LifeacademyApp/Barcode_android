package com.example.danny.scanbarcode;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import java.io.*;


import android.net.Uri;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import 	android.provider.Browser;
import 	android.os.Looper;

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
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
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
        final String call_url = myURI.getQueryParameter("callurl");    //取得URL中的Query String參數
        final String sendcode_url = myURI.getQueryParameter("returnurl") + "&code=" + code;    //取得URL中的 returnurl 值

        //開始送字串
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(sendcode_url);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    connection.setReadTimeout(0); //?
                    connection.setConnectTimeout(0); //?
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();

                    if (responseCode == 200) {
                        Looper.prepare();
                        Toast.makeText(MainActivity.this,"條碼傳送成功",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(MainActivity.this,"條碼傳送失敗",Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        try{
            // delay 200 milisecond for finishing sending barcode
            Thread.sleep(200);
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        Intent intent_main = new Intent(Intent.ACTION_VIEW);
        intent_main.setPackage("com.android.chrome");         // 指定開啟 chrome
        intent_main.setData(Uri.parse(call_url));
        intent_main.putExtra(Browser.EXTRA_APPLICATION_ID, "com.android.chrome");      //重複使用同一分頁

        // 開啟網頁
        try{
            startActivity(intent_main);
        }catch (ActivityNotFoundException ex){
            //如果手機裡沒有裝 chrome，就會改用 default browser 開啟
            intent_main.setPackage(null);
            startActivity(intent_main);
            Toast.makeText(MainActivity.this,"建議下載 chrome，可避免產生多餘分頁",Toast.LENGTH_SHORT).show();
        }
    }


    // 掃描結果會被傳到這個 function
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {  //假如有收到掃描結果資料
            if(result.getContents() == null) {   //假如是空的資料
                Log.d("MainActivity", "Cancelled scan");   //紀錄掃描失敗
                Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();  //顯示"取消"
                this.finish();
            } else {   //假如掃瞄有成功
                Log.d("MainActivity", "Scanned");  //紀錄掃描成功
                Toast.makeText(this, "掃描結果: " + result.getContents(), Toast.LENGTH_LONG).show();   //顯示掃描出的條碼內容

                if (this.getIntent().getDataString() != null) { //this.getIntent().getDataString() : usccbarcodescanner://?callurl=http://mmm.lifeacademy.org/erpweb/testbarcodeapp&returnurl=http://mmm.lifeacademy.org/erpweb/Scancode/PutScanCode?username=
                    sendResult(result.getContents()); //送條碼資料到伺服器
                    this.finish();
                }
                else {
                    Toast.makeText(this, "請從網頁開啟本程式", Toast.LENGTH_LONG).show();
                    this.finish();
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}