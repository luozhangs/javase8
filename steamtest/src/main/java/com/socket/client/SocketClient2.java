package com.socket.client;


import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Created by zhang on 2018/6/8.
 */
public class SocketClient2 extends Thread {



    public static void main(String[] args) {
        try{
            Socket socket = new Socket("localhost",2000);

            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write("用户名：Administrator;密码：1235");
            pw.flush();

            socket.shutdownOutput();

            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;

            while((info = br.readLine()) !=null){
                System.out.println("我是客户端，服务器说：" + info);
            }

            pw.close();
            is.close();
            br.close();
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
