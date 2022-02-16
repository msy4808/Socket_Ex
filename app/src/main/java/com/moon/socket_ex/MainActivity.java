package com.moon.socket_ex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView cliText, serText;
    Button sendBtn, serBtn;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_text);
        cliText = findViewById(R.id.cli_log);
        serText = findViewById(R.id.ser_log);
        sendBtn = findViewById(R.id.send_btn);
        serBtn = findViewById(R.id.ser_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = editText.getText().toString();
                //클릭하면 스레드 생성 ※ 스레드를 생성 안하고 send()를 호출하면 반응X(네트워킹은 스레드 필수!)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(data);
                    }
                }).start(); //스레드 실행
            }
        });
    serBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            new Thread(new Runnable() {
                @Override
                public void run() { //스레드 생성
                    startServer();
                }
            }).start(); //스레드 실행
        }
    });
    }

    private void printClientLog(final String str){
        Log.d("MainActivity", str);
        //새로 만들어진 스레드에서 메서드를 호출하여 UI를 수정하므로 핸들러를 사용해 메인스레드에 전달

        handler.post(new Runnable() {
            @Override
            public void run() {
                //append 메서드로 전달되는 파라미터는 그래도 전달되어야하므로 final로 정의
                cliText.append(str + "\n");
            }
        });
    }
    private void printServerLog(final String str){
        Log.d("MainActivity", str);
        //새로 만들어진 스레드에서 메서드를 호출하여 UI를 수정하므로 핸들러를 사용해 메인스레드에 전달
        handler.post(new Runnable() {
            @Override
            public void run() {
                //append 메서드로 전달되는 파라미터는 그래도 전달되어야하므로 final로 정의
                serText.append(str + "\n");
            }
        });
    }

    private void send(String str){
        try{
            int port = 5001;
            Socket socket = new Socket("localhost",port); //IP주소와 포트 지정
            printClientLog("소켓 연결함");

            //outputStream 생성
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(str); //파라미터로 받은 문자열 전송
            out.flush(); //outputStream 닫음
            printClientLog("서버로 데이터 전송함");

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            printClientLog("서버로 부터 받음 : " + in.readObject());
            socket.close(); //소켓 닫음
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startServer(){
        try{
            int port = 5001;

            ServerSocket server = new ServerSocket(port); //서버 포트 지정
            printServerLog("서버 시작함 : " + port);

            while (true){ //while(true)로 클라이언트의 접속요청을 기다림
                Socket socket = server.accept(); //연결 요청이 들어오면 소켓정보를 반환
                InetAddress clientHost = socket.getLocalAddress(); //클라이언트의 IP주소 확인
                int clientPort = socket.getPort(); //클라이언트의 포트정보 확인
                printServerLog("클라이언트 연결됨 : " + clientHost + " / " + clientPort);

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Object obj = in.readObject(); //클라이언트에서 보낸 데이터 저장
                printServerLog("클라이언트에서 데이터 받음 : " + obj);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(obj + " / 서버에서 보내는 답장"); //클라이언트로 데이터 보냄
                out.flush();
                printServerLog("클라이언트로 데이터 보냄");
                socket.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}