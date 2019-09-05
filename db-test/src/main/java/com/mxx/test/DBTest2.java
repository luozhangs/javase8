package com.mxx.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.FileUtil;
import com.maxx.base.util.StringUtil;
import com.maxx.base.util.security.MD5Util;
import com.mxx.base.utils.security.CipherUtil;
import com.zhang.db.DBConnection;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/5/6.
 */
public class DBTest2 {

    @Test
    public void jsonTest(){
        Long areaId = 440606000000L;
        List<JSONObject> list = DBConnection.query("SELECT title,area_id,type_name,p_area_id,type_code from mxx_base_area where p_area_id=?",areaId,JSONObject.class,"prod");
        for (JSONObject o:list){
            List<JSONObject> childs = DBConnection.query("SELECT title,area_id,type_name,p_area_id,type_code from mxx_base_area where p_area_id=?",o.getLong("area_id"),JSONObject.class,"prod");
            o.put("childs",childs);
        }
        JSONObject object = new JSONObject();
        object.put("title","佛山市顺德区");
        object.put("area_id",areaId);
        object.put("childs",list);
        FileUtil.saveAsFileOutputStream("src//main/resources/data.json", JSON.toJSONString(object));

    }

    public List<JSONObject> setChilds(List<JSONObject> list){
        for (JSONObject o:list){
            List<JSONObject> childs = DBConnection.query("SELECT title,area_id,type_name,p_area_id,type_code from mxx_base_area where p_area_id=?",o.getLong("area_id"),JSONObject.class,"prod");
            if(childs.size()>0){
                o.put("childs",setChilds(childs));
            }
        }
        return list;
    }

    @Test
    public void diseaseTest(){
        List<JSONObject> list = DBConnection.query("SELECT id,icd_code,name from mxx_base_disease where name in ('高血压','2型糖尿病','慢性肾脏病','冠心病','心律失常')",null,JSONObject.class);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void roleTest(){
//        List<JSONObject> list = DBConnection.query("SELECT id,name from mxx_base_role where name in ('专科医生','专科护士','中医')",null);
//        List<JSONObject> list = DBConnection.query("SELECT id,name from mxx_base_role where name in ('村医','公卫','全科医生','全科护士','中医')",null);
        List<JSONObject> list = DBConnection.query("SELECT id,name from mxx_base_role where isValid=1",null);
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void unionTest(){
        List<JSONObject> list = DBConnection.query("SELECT name ,id from mxx_center_category where id in (\n" +
                "SELECT center_id from mxx_medical_center_relation where medical_id=23\n" +
                ")",null,JSONObject.class,"prod");

        for (JSONObject object:list){
            object.put("type","医院");
            /*if(object.getString("name").contains("中心")){
                object.put("type","乡镇医院");
            }else{
                object.put("type","市级医院");
            }*/
        }
        System.out.println(JSON.toJSONString(list));
    }

    @Test
    public void export(){
        String sql = "select mem.name as `姓名`,mem.sex as `性别`,phone as `联系方式`,area_info as `地址`,'高血压' as `诊断` from mxx_base_member mem, mxx_member_ref ref,mxx_user_disease_rel rel\n" +
                "\t\twhere mem.id = ref.member_id\n" +
                "        and ref.member_id = rel.member_id\n" +
                "\t\tand ref.account_id in \n" +
                "\t\t  (select id from mxx_base_account where cdmsCenter in (select center_id from mxx_medical_center_relation where medical_id=12))\n" +
                "\t\tand ref.is_on_ref = 0\n" +
                "\t\tand ref.is_delete = 0\n" +
                "\t\tand ref.`status` = '100101700000001'\n" +
                "        and rel.disease_title like '%高血压%'\n" +
                "GROUP BY ref.member_id";
        List<JSONObject> list = DBConnection.query(sql,null);
        for (JSONObject object:list){
            JSONArray array = JSON.parseArray(object.getString("地址"));
            StringBuffer stringBuffer = new StringBuffer();
            for (int i=0;i<array.size();i++){
                JSONObject o = array.getJSONObject(i);
                stringBuffer.append(o.getString("title"));
            }
            object.put("地址",stringBuffer.toString());
        }
        try {
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\安溪医联体高血压(糖尿病)患者.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void distinct(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下基层错误数据.xlsx"));
            List<JSONObject> newList = new ArrayList<>();
            List<String> identifys = new ArrayList<>();
            newList = list.stream().filter(n->{
                if(StringUtil.empty(n.getString("identity_card"))){
                    return true;
                }
                //去重
                if(identifys.contains(n.getString("identity_card"))){
                    return false;
                }else{
                    identifys.add(n.getString("identity_card"));
                    return true;
                }
            }).collect(Collectors.toList());
            ExcelUtil.writeToFile(newList,null,"C:\\Users\\zhang\\Desktop\\历下基层错误数据(去重后).xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void readJson(){
        try {
            JSONObject object = JSONObject.parseObject(FileUtil.readTextFile("src//main/resources/read.json"));
            object.forEach((k,v)->{
                System.out.println(v);
//                JSONObject o = JSON.parseObject(JSON.toJSONString(v));
//                System.out.println(o.getString("value"));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public  void pwdTest(){

//        String salt = "E987b8U07eBLDX84258x4akIRJOfm11A";
////        String pwd = UUID.randomUUID().toString().substring(0,6);
//        Integer pwrs = (int)(Math.random()*1000000);
//        System.out.println(pwrs);
////        System.out.println(pwd);
//        String pwrsMD5 = CipherUtil.generatePassword(pwrs.toString());
//        String str = CipherUtil.createPwdEncrypt("chenxintan", pwrsMD5, salt);
//        System.out.println(str);
//        System.out.println(325/10.0);
//        System.out.println(Math.ceil(325/10));
        System.out.println(MD5Util.encode("cdms$maxxrootaviptcare"));
    }

    @Test
    public void exportAccount(){
        List<JSONObject> list = DBConnection.query("SELECT id,loginName,salt,`name`,phone,identity_card,title from mxx_base_account where cdmsCenter=114 and  DATE(createTime)=DATE(NOW())",null,JSONObject.class,"prod");
        Connection conn = DBConnection.getConnection("prod");
        for (JSONObject object:list){
            try {
                Integer pwrs = (int)(Math.random()*1000000);
                System.out.println(pwrs);
                object.put("pwd",pwrs);
                String pwrsMD5 = CipherUtil.generatePassword(pwrs.toString());
                String str = CipherUtil.createPwdEncrypt(object.getString("loginName"), pwrsMD5, object.getString("salt"));
                object.put("password",str);
                DBConnection.update(conn,"update mxx_base_account set password=? where id=?",object.getString("password"),object.getString("id"));
            } catch (SQLException e) {
                e.printStackTrace();
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\安溪账户信息.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
