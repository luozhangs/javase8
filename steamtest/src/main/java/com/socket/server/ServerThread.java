package com.socket.server;

import java.io.*;
import java.net.Socket;

/**
 * Created by zhang on 2018/6/11.
 */
public class ServerThread extends Thread {

    //和本线程相关的socket，通过构造方法初始化socket
    private Socket socket = null;

    public Socket getSocket() {
        return socket;
    }

    public ServerThread(Socket socket){
        this.socket=socket;
    }

    public void out(String info){
        OutputStream os=null;
        PrintWriter pw=null;
        try {
            os = socket.getOutputStream();
            pw = new PrintWriter(os);
            pw.write(info);
            pw.flush();
//            socket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //线程执行的操作：响应客户端的请求
    public void run(){
        InputStream is=null;
        InputStreamReader isr=null;
        BufferedReader br=null;
        try {
            is = socket.getInputStream();
            isr = new  InputStreamReader(is);
            br = new BufferedReader(isr);
            String info = null;
            while((info = br.readLine())!= null){
                System.out.println("我是服务器，客户端说：" + info);
//                this.out(info);
                ChatManager.getCM().sendClientMsg(this,info);
            }
//            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
