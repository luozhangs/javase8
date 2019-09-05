package com.socket.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * Created by zhang on 2019/6/28.
 */
public class ChatManager {

    private final static ChatManager CM = new ChatManager();
    //聊天列表
    //现成安全list
    private static Map<String,ServerThread> userlist = new HashMap<>();

    public static ChatManager getCM() {
        return CM;
    }

    public static Map<String,ServerThread> getUserlist() {
        return userlist;
    }

    /**
     * 循环判断是否断开连接
     */
    public void delConnect(){
        for (Map.Entry<String,ServerThread> entry:userlist.entrySet()){
            if(entry.getValue().getSocket().isClosed()){
                userlist.remove(entry.getKey());
            }
        }
    }

    /**
     * 添加客户端到聊天列表
     * @param serverThread
     */
    public void addServerTheard(String key,ServerThread serverThread){
        this.delConnect();
        userlist.put(key,serverThread);
    }

    public void sendClientMsg(ServerThread serverThread,String msg){
        userlist.forEach((k,v)->{
            if(!v.getSocket().isClosed()){
                if(v.equals(serverThread)){
                    v.out(msg);
                }
            }
        });
    }
}
