package com.mxx.test;

import com.alibaba.fastjson.JSONObject;
import com.zhang.db.DBConnection;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by zhang on 2019/5/30.
 */
public class AdminTest {

    @Test
    public void  insertRole(){
        List<JSONObject> list = DBConnection.query("select id,role_id from admin_account where account_type='10010010201' and role_id is not null ",null,JSONObject.class,"zxx_admin");
        Connection conn = DBConnection.getConnection("zxx_admin");
        String inSql  = "insert into admin_account_role_ref(user_id,role_id,create_time,modify_time)values(?,?,now(),now())";
        for (JSONObject object:list){
            Long roleId = object.getLong("role_id");
            try {
                DBConnection.insert(conn,inSql,object.getLong("id"),roleId);
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }

        //提交
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
