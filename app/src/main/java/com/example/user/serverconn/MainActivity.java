package com.example.user.serverconn;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private String jsonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
    }

    public void test(View view){
        switch (view.getId()){
            case R.id.btn1: // 스레드 시작
                AnotherThread thread = new AnotherThread("http://10.10.17.67:8888/test/jsontest");
                thread.start(); // 아파치 톰캣 서버에 요청 보냄.
                break;

            case R.id.btn2:
                if(jsonText == null) return;
                JSONArray jsonArray = null;
                JSONObject jsonObject = null;

                try {
                    jsonArray = new JSONArray(jsonText);
                    StringBuilder sb2 = new StringBuilder();

                    for(int i=0; i<jsonArray.length() ;i++){
                        jsonObject = jsonArray.getJSONObject(i);
                        sb2.append("번호: ");
                        sb2.append(jsonObject.getString("num"));
                        sb2.append("이름: ");
                        sb2.append(jsonObject.getString("name"));
                        sb2.append("\n");
                    }

                    textView.setText(sb2.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private class AnotherThread extends Thread{
        String addr;

        public AnotherThread(String addr){
            this.addr = addr;
        }

        @Override
        public void run() {
            super.run();

            String result = test2(addr); // 서버에 메세지 보내고 받음.
            Message message = handler.obtainMessage();
            message.obj = result;
            handler.sendMessage(message);
        }

        // 네트워크 로직 작성
        // 서버에 요청 보냄. (서버 주소, 프로젝트 명)
        // 메소드 따로 작성
        private String test2(String addr){
            StringBuilder sb = new StringBuilder();

            try {
                URL url = new URL(addr);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                if(httpURLConnection != null){
                    // 서버에 보낼 준비 작업
                    // 서버에 요청 보냈지만 응답 없을 때, 몇초까지 기다릴 것인가.
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setUseCaches(false); // 캐시 사용 여부
                    httpURLConnection.setRequestMethod("POST"); // get 또는 post 방식

                    //추가
                    httpURLConnection.setRequestProperty("Content-Type", "text/plain;charset=UTF-8");


                    // 서버에 메세지 보내기
                    OutputStream outputStream = httpURLConnection.getOutputStream(); // 서버에 보낼 문자열
                    outputStream.write("안드로이드 메세지".getBytes("utf-8")); // 보내는 방식이 조금 특이
                    outputStream.close(); // 자원 봔환; 메모리 반환

                    // 서버에 응답했을 때 로직
                    if(httpURLConnection.getResponseCode() == httpURLConnection.HTTP_OK){
                        InputStreamReader is = new InputStreamReader(httpURLConnection.getInputStream());
                        // Reader 가 붙으면 문자열로 인풋, 아웃풋 스트림으로 주고받음..

                        int ch;
                        while((ch = is.read()) != -1){
                            sb.append((char)ch); // 한 글자씩 붙여줌
                        }
                        is.close(); // 작업 끝나면 close
                       // jsonText = sb.toString(); // streamBuilder 를 문자열로 받아서 잠시 jsonText 에 담아 둠
                    }
                    httpURLConnection.disconnect(); // 더이상 필요하지 않으므로 통신 끊어줌.
                }

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "스레드 에러", Toast.LENGTH_SHORT).show();
                //e.printStackTrace();
            }
            return sb.toString();
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            jsonText =  (String)msg.obj;
            textView.setText(jsonText);
        }
    };
}
