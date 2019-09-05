package com.mxx.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mxx.account.Account;
import com.zhang.db.DBConnection;
import excel.ExcelDataFormatter;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/2/27.
 */
public class DBTest {

    @Test
    public void testQuery(){
        String sql = "    select count(1) from mxx_base_account a LEFT JOIN mxx_base_role r on a.roleId = r.id where a.cdmsCenter in\n" +
                "(select center_id from mxx_medical_center_relation where medical_id=12)\n" +
                "and r.name in ('全科医生','专科医生','专科护士','全科护士')\n" +
                " ;";
        List<JSONObject> list = DBConnection.query(sql,null,JSONObject.class);
        List<Account> list2 = DBConnection.query(sql,null,Account.class);
        int count = DBConnection.queryOne(sql,null,Integer.class);
        try {
            ExcelDataFormatter edf = new ExcelDataFormatter();
            Map<String,String> map = new HashMap<>();
            map.put("男","1");
            map.put("女","0");
            edf.set("sex",map);
//            ExcelUtil.writeToFile(list,edf,"C:\\Users\\zhang\\Desktop\\生成的数据\\测试.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void importExcel(){
        ExcelDataFormatter edf = new ExcelDataFormatter();
        Map<String,String> map = new HashMap<>();
        map.put("1","男");
        map.put("0","女");
        edf.set("sex",map);
        try {
            List<JSONObject> list =  new ExcelUtil<JSONObject>(JSONObject.class).readFromFile(edf,new File("C:\\Users\\zhang\\Desktop\\生成的数据\\测试.xlsx"));
            System.out.println(JSON.toJSONString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testClassName(){
        System.out.println(new JSONObject().getClass().getSimpleName());
    }

    @Test
    public void testDisease(){
        //查看患者疾病
        String sql = "select * from mxx_user_disease_rel where member_id = ?";
//        String inSql = "insert into mxx_user_disease_rel(member_id,disease_id,disease_title,`order`,create_time) values (?,?,?,?,now())";
        String inSql = "insert into mxx_user_disease_rel(member_id,disease_id,disease_title,`order`,create_time) values (#{member_id},#{disease_id},#{disease_title},#{order},now())";
        String memSql = "select * from mxx_base_member where phone like '143%' and description is NOT NULL";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        d.put("member_id",n.getString("id"));
//                        DBConnection.insert(connection,inSql,n.getString("id"),d.getString("disease_id"),d.getString("disease_title"),d.getInteger("order"));
                        DBConnection.insert(connection,inSql,d);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void allergy(){
        //查看患者疾病
        String sql = "select * from mxx_member_history_allergy where member_id = ? and is_delete=0";
        String inSql = "insert into mxx_member_history_allergy(member_id,account_id,category_code,allergy_code,allergy_title,remark,create_time) values (?,?,?,?,?,?,now())";
        String memSql = "select * from mxx_base_member where phone like '143%'";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        String memberSql = "select health_officer from mxx_base_member where id=?";
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        //查询患者详情
                        String accountId = (String) DBConnection.queryOne(memberSql,n.getString("id"));
                        DBConnection.insert(connection,inSql,n.getString("id"),accountId,d.getLong("category_code"),d.getLong("allergy_code"),d.getString("allergy_title"),d.getString("remark"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void familyDis(){
        //查看患者疾病
        String sql = "select * from mxx_member_history_family_dis where member_id = ? and is_delete=0";
        String inSql = "insert into mxx_member_history_family_dis(member_id,account_id,nexus_title,disease_id,disease_title,remark,create_time) values (?,?,?,?,?,?,now())";
        String memSql = "select * from mxx_base_member where phone like '143%'";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        String memberSql = "select health_officer from mxx_base_member where id=?";
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        //查询患者详情
                        String accountId = (String) DBConnection.queryOne(memberSql,n.getString("id"));
                        DBConnection.insert(connection,inSql,n.getString("id"),accountId,d.getString("nexus_title"),d.getString("disease_id"),d.getString("disease_title"),d.getString("remark"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void medicineRecord(){
        String sql = "select * from mxx_member_medicine_record where member_id = ? and is_delete=0";
        String inSql = "insert into mxx_member_medicine_record(member_id,medicine_id,medicine_name," +
                "use_method,single_dose,unit,frequency,medication,start_time,end_time,creator,create_time)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,now())";
        String memSql = "select * from mxx_base_member where phone like '143%'";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        String memberSql = "select health_officer from mxx_base_member where id=?";
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        //查询患者详情
//                        String accountId = (String) DBConnection.queryOne(memberSql,n.getString("id"));
                        DBConnection.insert(connection,inSql,n.getString("id"),d.getInteger("medicine_id"),d.getString("medicine_name"),d.getString("use_method"),
                                d.getString("single_dose"),d.getString("unit"),
                                d.getString("frequency"),d.getString("medication"),d.getDate("start_time"),d.getDate("end_time"),d.getString("creator"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void recordStatus(){
        String sql = "select * from mxx_member_medicine_record_status where member_id = ? ";
        String inSql = "insert into mxx_member_medicine_record_status(member_id,account_id,status,record_type,create_time) values (?,?,?,?,now())";
        String memSql = "select * from mxx_base_member where phone like '143%'";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        String memberSql = "select health_officer from mxx_base_member where id=?";
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        //查询患者详情
                        String accountId = (String) DBConnection.queryOne(memberSql,n.getString("id"));
                        DBConnection.insert(connection,inSql,n.getString("id"),accountId,d.getInteger("status"),d.getLong("record_type"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void diseaseHistory(){
        String sql = "select * from mxx_disease_history where user_id = ? ";
        String inSql = "insert into mxx_disease_history(user_id,disease_id,disease_title,begin_date,treat_method,treat_effect" +
                ",treat_effect_title,treat_effect_code,remark,create_id,create_user_type,is_main_dis,create_time) values (?,?,?,?,?,?,?,?,?,?,?,0,now())";
        String memSql = "select * from mxx_base_member where phone like '143%'";
        List<JSONObject> mems = DBConnection.query(memSql,null);
        Map<String,List<JSONObject>> map = mems.stream().collect(Collectors.groupingBy(n->n.getString("description")));
        Connection connection = DBConnection.getConnection();
        String memberSql = "select health_officer from mxx_base_member where id=?";
        map.forEach((k,v)->{
            List<JSONObject> dis = DBConnection.query(sql,k);
            if(dis.size()==0){
                return;
            }
            //遍历新增的复制患者
            v.forEach(n->{
                //添加该患者疾病信息
                dis.forEach(d->{
                    try {
                        //查询患者详情
                        String accountId = (String) DBConnection.queryOne(memberSql,n.getString("id"));
                        Long historyId = Long.valueOf(DBConnection.insert(connection,inSql,n.getString("id"),d.getString("disease_id"),d.getString("disease_title"),d.getDate("begin_date"),
                                d.getString("treat_method"),d.getString("treat_effect"),d.getString("treat_effect_title"),d.getLong("treat_effect_code"),d.getString("remark"),
                                accountId, d.getLong("create_user_type")).toString());
                        //增加 mxx_disease_history_attachment
                        String attachSal = "insert into mxx_disease_history_attachment(history_id,attachment_name,attachment_path,attachment_size,oss_info,create_time)" +
                                "values(?,?,?,?,?,now())";
                        List<JSONObject> list = DBConnection.query("select * from mxx_disease_history_attachment where history_id=?",d.getLong("history_id"));
                        for (JSONObject o:list){
                            DBConnection.insert(connection,attachSal,historyId,o.getString("attachment_name"),o.getString("attachment_path"),o.getString("oss_info"));
                        }
                        //增加 mxx_disease_history_treatmethod_ref
                        String treatmethod = "insert into mxx_disease_history_treatmethod_ref(history_id,treat_method_title,treat_method_code,create_time)" +
                                "values(?,?,?,now())";
                        List<JSONObject> list2 = DBConnection.query("select * from mxx_disease_history_treatmethod_ref where history_id=?",d.getLong("history_id"));
                        for (JSONObject o:list2){
                            DBConnection.insert(connection,treatmethod,historyId,o.getString("treat_method_title"),o.getLong("treat_method_code"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            connection.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            });
        });
        //提交
        try {
            connection.commit();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void accountDisease(){
        String accountSql = "select * from mxx_base_account a where a.phone like '143%' and a.roleId = 'eb3bc21f45074a65893ee87305948ac6'";
        String inSql = "insert into mxx_account_disease_rel(account_id,account_name,disease_code,disease_title,keyword,account_role_id,account_role_title,create_time)" +
                "values(?,?,?,?,?,'eb3bc21f45074a65893ee87305948ac6','护师',now())";
        List<JSONObject> dis = DBConnection.query("select disease_code,disease_title,keyword from mxx_account_disease_rel GROUP BY disease_code",null);
        Connection conn = DBConnection.getConnection();
        List<JSONObject> acs = DBConnection.query(accountSql,null);
        for (JSONObject o:acs){
            JSONObject oo = dis.get(new Random().nextInt(4));
            try {
                DBConnection.insert(conn,inSql,o.getString("id"),o.getString("name"),oo.getLong("disease_code"),oo.getString("disease_title"),oo.getString("keyword"));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    conn.rollback();
                    return;
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

    @Test
    public void insertRuleItem(){
//        String query = "select * from mxx_report_rule where category_code = 10050030130000002 and mode_code in (10050030140000001,10050030140000002)";
        String query = "select id  from mxx_report_rule where id not in ( \n" +
                "select rule_id from mxx_report_rule_item where rule_id in (\n" +
                "select id from mxx_report_rule where category_code != 10050030130000001\n" +
                ") and norm_item_code in (100200101000130,100200101000131,100200101000132) GROUP BY rule_id \n" +
                ") and  category_code != 10050030130000001";
        String inquery = "select * from mxx_report_rule_item where  norm_item_code in( 100200101000130,100200101000131,100200101000132 ) and rule_id=5";
        String insql = "insert into mxx_report_rule_item (rule_id,norm_item_code,norm_item_title,module_attr,module_title,create_time)" +
                "values(?,?,?,?,?,now())";
        Connection conn = DBConnection.getConnection();
        List<JSONObject> list = DBConnection.query(query,null);
        List<JSONObject> list1 = DBConnection.query(inquery,null);
        for (JSONObject object:list){
            for (JSONObject object1:list1){
                try {
                    DBConnection.insert(conn,insql,object.getLong("id"),object1.getLong("norm_item_code"),object1.getString("norm_item_title"),object1.getString("module_attr"),object1.getString("module_title"));
                } catch (SQLException e) {
                    try {
                        conn.rollback();
                        break;
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
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

    @Test
    public void testRand(){
        List<JSONObject> dis = DBConnection.query("select disease_code,disease_title,keyword from mxx_account_disease_rel GROUP BY disease_code",null);
        for (int i=0;i<100;i++){
            JSONObject oo = dis.get(new Random().nextInt(4));
            System.out.println(JSON.toJSONString(oo));
        }
    }

    @Test
    public void insertRuleItem2(){
        String inSql = "insert into mxx_report_rule_item(rule_id,norm_item_code,norm_item_title,module_attr,module_title,create_time)" +
                "values(?,?,'总的资料完善率平均分','finishModule','完善率模块',now())";
        String query = "select * from mxx_report_rule where category_code=10050030130000001";
        List<JSONObject> list = DBConnection.query(query,null);
        Connection conn = DBConnection.getConnection();
        for (JSONObject o:list){
            try {
                DBConnection.insert(conn,inSql,o.getLong("id"),100200101000441L);
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
        //提交
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void updateRuleItem1(){
        String upSql = "update mxx_report_rule_item set norm_item_title=? where norm_item_code=? and rule_id=?";
        String querySql = "select * from mxx_report_rule_item where norm_item_code in (100200101000006,100200101000008,100200101000010) and  FIND_IN_SET(rule_id, ?)";
        String query = "select mode_code,GROUP_CONCAT(id) ids from mxx_report_rule GROUP BY mode_code";
        List<JSONObject> list = DBConnection.query(query,null);
        Connection conn = DBConnection.getConnection();
        for (JSONObject o:list){
            String modeCode = o.getString("mode_code");
            String title = "";
            switch (modeCode){
                case "10050030140000001":title = "本周";break;
                case "10050030140000002":title = "本月";break;
                case "10050030140000003":title = "本季";break;
                case "10050030140000004":title = "半年";break;
                case "10050030140000005":title = "年度";break;
            }
            String ids = o.getString("ids");
            List<JSONObject> list1 = DBConnection.query(querySql,ids);
            for (JSONObject oo:list1){
                try {
                    DBConnection.update(conn,upSql,title+oo.getString("norm_item_title").substring(2),oo.getLong("norm_item_code"),oo.getLong("rule_id"));
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
        }
        //提交
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void TestArray(){
        Object ids = new String[]{"1","2"};
        System.out.println(ids instanceof Object[]);
        System.out.println(String.valueOf("本周有效新增给").substring(2));
    }

    @Test
    public void testDB(){
        List<JSONObject> list = DBConnection.query("SELECT\n" +
                "\tid AS `key`,\n" +
                "\tpatient_name AS name,\n" +
                "\tsex,\n" +
                "\tbirthday,\n" +
                "\tid_no AS identity_card,\n" +
                "\tphone_no AS phone,\n" +
                "\taddress_home AS home_address,\n" +
                "\tdoc_name,\n" +
                "\tGROUP_CONCAT(DISTINCT disease_title separator  '$') as disease_title\n" +
                "FROM\n" +
                "\thospital_patient_hospitalization\n" +
                "GROUP BY id_no limit 10",null,JSONObject.class,"db2");
        System.out.println(JSON.toJSONString(list));
    }

}
