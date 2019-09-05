package com.mxx.test;

import com.alibaba.fastjson.JSONObject;
import com.zhang.db.DBConnection;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/7/18.
 */
public class UserTagUpdate {


    public static void main(String[] args) {
        List<JSONObject> list = DBConnection.query("SELECT t.* from (\n" +
                "SELECT\n" +
                "\t*\n" +
                "FROM\n" +
                "\tmxx_user_tag t\n" +
                "WHERE\n" +
                "\tt.account_id in (SELECT id from mxx_base_account where cdmsCenter=90)\n" +
                "AND is_delete = 0\n" +
                "ORDER BY create_time desc\n" +
                ") t \n" +
                "GROUP BY\n" +
                "\tt.title,t.account_id\n" +
                "HAVING\n" +
                "\tCOUNT(1) > 1",null,JSONObject.class,"report_prepro");

//        int avg = list.size()/20;
        for (int j=0;j<1;j++){
            int finalJ = j;
            new Thread(()->{
//                List<JSONObject> list1 = list.subList(finalJ *avg,(finalJ +1)*avg);
                Connection conn = DBConnection.getConnection("report_prepro");
                int i=0;
                for (JSONObject object:list){
                    System.out.println("thread_"+finalJ+":"+list.size()+"--"+i++);
                    Long tagId = object.getLong("id");
                    object.put("tag_id",tagId);
                    List<Long> tags = DBConnection.query("select tr.id from mxx_user_tag_ref tr, mxx_user_tag t where tr.tag_id = t.id and t.account_id=#{account_id} and t.title=#{title} and tr.tag_id!=#{tag_id}",object,Long.class,"report_prepro");
                    try {
                        if(tags.size()>0){
                            DBConnection.update(conn,"update mxx_user_tag_ref set tag_id=? where find_in_set(id,?)",tagId,tags.stream().map(n->n.toString()).collect(Collectors.joining(",")));
                        }
                        DBConnection.delete(conn,"delete from mxx_user_tag where account_id=? and title=? and id!=?",object.getString("account_id"),object.getString("title"),tagId);
                        conn.commit();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        try {
                            conn.rollback();
                            break;
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }).start();
        }

    }

    @Test
    public void  tagTest(){
        List<JSONObject> list = DBConnection.query("SELECT * from  mxx_user_tag where is_delete=0 GROUP BY account_id,title HAVING COUNT(1)>1",null,JSONObject.class,"prod");

        Connection conn = DBConnection.getConnection("prod");
        int i=0;
        for (JSONObject object:list){
            System.out.println(list.size()+"--"+i++);
            Long tagId = object.getLong("id");
            object.put("tag_id",tagId);
            List<Long> tags = DBConnection.query("select tr.id from mxx_user_tag_ref tr, mxx_user_tag t where tr.tag_id = t.id and t.account_id=#{account_id} and t.title=#{title} and tr.tag_id!=#{tag_id}",object,Long.class,"prod");
            try {
                if(tags.size()>0){
                    DBConnection.update(conn,"update mxx_user_tag_ref set tag_id=? where find_in_set(id,?)",tagId,tags.stream().map(n->n.toString()).collect(Collectors.joining(",")));
                }
                DBConnection.delete(conn,"delete from mxx_user_tag where account_id=? and title=? and id!=?",object.getString("account_id"),object.getString("title"),tagId);
                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.rollback();
                    break;
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
