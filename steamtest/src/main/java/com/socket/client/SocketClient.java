package com.socket.client;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2018/6/8.
 */
public class SocketClient extends Thread {


    Socket s = null;
    BufferedReader br = null;
    PrintWriter pw = null;

    public SocketClient() {
        try {
            s = new Socket("127.0.0.1", 2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reciveMsg(){
        try {
            System.out.println(s.isClosed());
            if(s.isClosed()){
                return;
            }
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            if (br != null) {
                String line = br.readLine();
                System.out.println("from server: " + line);
            }
            s.shutdownInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public void sendMsg(String info){
       try {
           pw = new PrintWriter(s.getOutputStream());
           if (info != null && info.length() > 0) {
               pw.println(info);
               pw.flush();
           }
           s.shutdownOutput();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    @Override
    public void run() {
       while (true){
           Scanner in = new Scanner(System.in);
           String info = in.nextLine();
           this.sendMsg(info);
           this.reciveMsg();
       }
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        SocketClient socketClient = new SocketClient();
        socketClient.start();
        /*new Thread(()->{
            while (true){
                try {
                    Thread.sleep(1000);
                    System.out.println(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                socketClient.reciveMsg();
            }
        }).start();*/
        /*String string = "鎮ㄦ\uE11C鍦ㄨ繘琛?娉ㄥ唽閲戝北閫氳\uE511璇?鎿嶄綔锛屽畼鏂圭粷涓嶄細绱㈠彇姝ら獙璇佺爜锛岃\uE1EC鍕垮憡鐭ヤ粬浜恒€傞獙璇佺爜锛?58612锛堟湁鏁堟湡30鍒嗛挓锛?";
        byte[] bytes = string.getBytes("gbk");
        System.out.println(new String(bytes));*/
        /*List<Student> list = new ArrayList<>();
        list.add(new Student(1,"a"));
        list.add(new Student(2,"b"));
        list.add(new Student(3,"c"));
        list.add(new Student(4,"d"));
        list.add(new Student(5,"e"));
        List<Integer> ids = Arrays.asList(1,3,4,2);
        List<String> names = new ArrayList<>();
        Map<Integer,String> map = list.stream().collect(Collectors.toMap(n->n.getId(), n->n.getName(),(k1, k2)->k2));
        ids.forEach(n->names.add(map.get(n)));
        //names = list.stream().filter(n->ids.contains(n.getId())).map(n->n.getName()).collect(Collectors.toList());
        names.forEach(System.out::println);
        Integer[] idss = (Integer[]) ids.toArray();
        Arrays.sort(idss,Collections.reverseOrder());
        Arrays.asList(idss).forEach(System.out::print);*/
    }
}
