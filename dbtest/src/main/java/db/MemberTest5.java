package db;

import com.maxx.base.util.StringUtil;
import excel.ExcelUtil;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang on 2019/6/6.
 */
public class MemberTest5 {


    @Test
    public void updateMember(){
        List<JSONObject> list = DBConnection.query("SELECT * from mxx_base_member where cdmsCenter=90 ",null,JSONObject.class,"prod");
        //疾病对应的疾病数据数据（字典表）
        List<com.alibaba.fastjson.JSONObject> disease = null;
        try {
            disease = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\弋矶山\\以及山字典表.xlsx"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String,String> map = new HashMap<>();
        for (com.alibaba.fastjson.JSONObject object:disease){
            object.forEach((k,v)->{
                if(!"diagnois_num".equals(k)&&"1".equals(v)){
                    map.put(object.getString("disease_name").trim(),k);
                }
            });
        }
        Map<String,String> diseasemap = new HashMap<>();
        diseasemap.put("I10.X02","高血压");
        diseasemap.put("E14.901","糖尿病 NOS");
        diseasemap.put("N19.X61","慢性肾脏病");
        diseasemap.put("I25.101","冠心病");
        diseasemap.put("36.09","冠状动脉球囊扩张及支架植入");
        diseasemap.put("I49.904","心律失常");
        diseasemap.put("37.711","心室暂时性起搏器植入");
        Connection conn = DBConnection.getConnection("prod");
        String insql = "insert into mxx_user_disease_rel (member_id,disease_id,disease_title,create_time)values(#{member_id},#{disease_id},#{disease_title},now())";
        int i = 1;
        for (JSONObject object:list){
            System.out.println(list.size()+"--"+i++);
            String diseaseName = object.getString("current_health_problem");
            String[] names = {};
            if(!StringUtil.empty(diseaseName)){
                names = diseaseName.split("[$]");
            }
            for (String name:names){
                String icdCode = map.get(name);
                if(icdCode!=null){
                    //添加诊断
                    String diseaseTitle = diseasemap.get(icdCode);
                    JSONObject param = new JSONObject();
                    param.put("member_id",object.getString("id"));
                    param.put("disease_id",icdCode);
                    param.put("disease_title",diseaseTitle);
                    Long count = DBConnection.queryOne("select count(1) from mxx_user_disease_rel where member_id=#{member_id} and disease_id=#{disease_id}",param,Long.class,"prod");
                    if(count==0){
                        try {
                            DBConnection.insert(conn,insql,param);
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
    public void insertTag(){
        List<JSONObject> list = DBConnection.query("SELECT * from mxx_base_member where cdmsCenter=90 ",null,JSONObject.class,"prod");
        int i = 1;
        String tagSql = "INSERT INTO mxx_user_tag(title,account_id,remark,create_time)values(#{title},#{account_id},#{remark},now())";
        String tagRefSql = "INSERT into mxx_user_tag_ref (tag_id,member_id,account_id,status,remark,create_time)values(#{tag_id},#{member_id},#{account_id},#{status},#{remark},now())";
        Connection conn = DBConnection.getConnection("prod");
        for (JSONObject object:list){
            System.out.println(list.size()+"--"+i++);
            //查询就诊医生
            List<JSONObject> mems = DBConnection.query("SELECT * from hospital_patient_hospitalization where id_no=?",object.getString("identity_card"),JSONObject.class,"zxx_prepro");
            if(mems.size()==0){
                mems = DBConnection.query("SELECT * from hospital_patient_outpatient where id_no=?",object.getString("identity_card"),JSONObject.class,"zxx_prepro");
            }
            if(mems.size()==0){
                continue;
            }else{
                String docName = mems.get(0).getString("doc_name");
                //通过名字查询医生对象
                if(!StringUtil.empty(docName)){
                    Long count = DBConnection.queryOne("select count(1) from mxx_base_account where  name=?",docName,Long.class,"prod");
                    String tagName = "";
                    if(count==0){
                        tagName="接诊医生:其它医生";
                    }else{
                        tagName="按诊医生:"+docName;
                    }
                    //接诊医生：其它医生”
                    //查询该患者的医护团队
                    List<JSONObject> accountRefs = DBConnection.query("SELECT * from mxx_member_ref where member_id=? and is_delete=0 and is_on_ref=0 and grant_type='10050030060000001' and `status`='100101700000001'",object.getString("id"),JSONObject.class,"prod");
                    for (JSONObject accountRef:accountRefs){
                        //查询标签是否已存在
                        JSONObject tag = new JSONObject();
                        tag.put("account_id",accountRef.getString("account_id"));
                        tag.put("title",tagName);
                        tag.put("remark","就诊医生:"+docName);
                        List<JSONObject> tags = DBConnection.query("SELECT * from mxx_user_tag where title  =#{title} and account_id=#{account_id}",tag,JSONObject.class,"prod");
                        Long tagId = null;
                        try {
                            if(tags.size()==0){
                                tagId = Long.valueOf(DBConnection.insert(conn,tagSql,tag).toString()) ;
                            }else{
                                tagId = tags.get(0).getLong("id");
                            }
                            JSONObject tagRef = new JSONObject();
                            tagRef.put("tag_id",tagId);
                            tagRef.put("member_id",object.getString("id"));
                            tagRef.put("account_id",accountRef.getString("account_id"));
                            tagRef.put("status",object.getLong("status"));
                            tagRef.put("remark","就诊医生:"+docName);
                            DBConnection.insert(conn,tagRefSql,tagRef);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            try {
                                conn.rollback();
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            }
                        }
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
    public void updateRecord(){
        List<JSONObject> list = DBConnection.query("SELECT id,record_id,result_data from mxx_report_record_item where norm_item_code='100200101000440'",null,JSONObject.class,"prod");
        Connection conn = DBConnection.getConnection("prod");
        for (JSONObject object:list){
            JSONObject object1 = DBConnection.queryOne("SELECT id,record_id,result_data from  mxx_report_record_item where record_id="+object.getLong("record_id")+" and norm_item_code='100200101000419'",null,JSONObject.class,"prod");
            try {
                JSONObject param = new JSONObject();
                param.put("id",object.getLong("id"));
                param.put("result_data",object1.getString("result_data"));
                DBConnection.update(conn,"update mxx_report_record_item set result_data=#{result_data} where id=#{id}",param);
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
