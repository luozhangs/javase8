package com.mxx.test;


import com.alibaba.fastjson.JSONObject;
import com.zhang.db.DBConnection;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/7/4.
 */
public class KettleTest {

    @Test
    public void delRecordItem_690(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("D:\\workspace\\matrix\\javav2\\kettle\\kettle-test\\kettle_item_data_error\\error_690_691_收缩压舒张压.xlsx"));
            String sql = "update maxx_fp_user_item_record set is_delete=1,modify_time=now() where FIND_IN_SET(id,?)";
            Connection conn = DBConnection.getConnection("prod");
            String ids = list.stream().map(n->{
                return n.getString("原表690id")+","+n.getString("原表691id");
            }).collect(Collectors.joining(","));
            System.out.println(ids);
            DBConnection.update(conn, sql, ids);
            //提交
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void delRecordItem_2070(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("D:\\workspace\\matrix\\javav2\\kettle\\kettle-test\\kettle_item_data_error\\error_2070_血糖.xlsx"));
            String sql = "update maxx_fp_user_item_record set is_delete=1,modify_time=now() where FIND_IN_SET(id,?)";
            Connection conn = DBConnection.getConnection("prod");
            DBConnection.update(conn, sql, list.stream().map(n->n.getString("原表id")).collect(Collectors.joining(",")));
            //提交
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
