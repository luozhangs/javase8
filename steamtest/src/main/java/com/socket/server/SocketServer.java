package com.socket.server;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhang on 2018/6/8.
 */
public class SocketServer extends JFrame{


    public static void main(String[] args) {

        ChatManager ct = ChatManager.getCM();
        try {
            ServerSocket server = new ServerSocket(2000);
            Socket sk  = null;
            while (true) {
                System.out.println("accept in 1000 mintes.....");
                sk = server.accept();//获取客户端socket
                ServerThread serverThread = new ServerThread(sk);//创建服务端对象
                String key = sk.getInetAddress()+"_"+sk.getPort();
//                System.out.println(key);
                ct.addServerTheard(key,serverThread);
                System.out.println("聊天室在线人数："+ct.getUserlist().size());
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List list = new ArrayList(20);

    }
}
