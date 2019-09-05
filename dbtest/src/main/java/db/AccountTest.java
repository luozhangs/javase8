package db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.DateUtil;
import com.maxx.base.util.FileUtil;
import com.maxx.base.util.HttpUtils;
import com.maxx.base.util.StringUtil;
import com.mxx.base.utils.HttpUtil;
import com.mxx.base.utils.StringUtils;
import com.mxx.base.utils.security.CipherUtil;
import com.mxx.headPic.PicCreate;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/3/13.
 */
public class AccountTest {
    @Test
    public void insertAccount(){

        List<JSONObject> list = new ArrayList<>();
        List<JSONObject> error = new ArrayList<>();
        String insql = "INSERT into mxx_base_account_data_yijishan(id,loginName,password,salt,roleId,picUrl,name,email,\n" +
                "skin,isValid,createTime,updateTime,description,cdmsCenter,major,isLeader,motto,sex,education,birthday,\n" +
                "identity_card,phone,department,title,skill,administration_id,create_id,role_title,manage_diease,\n" +
                "center_name,manage_region,address,cdms_name1) VALUES(#{id},#{loginName},#{password},#{salt},#{roleId},#{picUrl},#{name},#{email},\n" +
                "#{skin},#{isValid},now(),now(),#{description},#{cdmsCenter},#{major},#{isLeader},#{motto},#{sex},#{education},#{birthday},\n" +
                "#{identity_card},#{phone},#{department},#{title},#{skill},#{administration_id},#{create_id},#{role_title},#{manage_diease},\n" +
                "#{center_name},#{manage_region},#{address},#{cdms_name1})";
        try {
            list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\工作簿1.xlsx"));
            //从文件中读取json
//            JSONArray array= JSONArray.parseArray(FileUtil.readTextFile("src//main/resources/data.json"));
//            array.forEach(n->list.add((JSONObject) n));
            Connection conn = DBConnection.getConnection();
            for (JSONObject object:list){
                Integer  num = object.getInteger("num");
                if(num==-1){
                    continue;
                }
                String phone = object.getString("phone").replace(" ","");
                Long count = DBConnection.queryOne("select count(1) from mxx_base_account where loginName=?",phone,Long.class,"prod");
                if(count!=0){
                    error.add(object);
                    continue;
                }
                object.put("id", UUID.randomUUID().toString().replaceAll("-",""));
                if("e7c73a9832a94627911e90ea72778600".equals(object.getString("roleId"))||"eb3bc21f45074a65893ee87305948ac6".equals(object.getString("roleId"))){
                    object.put("picUrl","cdms/default_head_pic/nurse.png");
                }else{
                    object.put("picUrl","cdms/default_head_pic/specialist_doctor.png");
                }
                object.put("isValid",1);
                object.put("salt",CipherUtil.createSalt());
                object.put("loginName",phone);
                object.put("password", CipherUtil.createPwdEncrypt(object.getString("loginName"),CipherUtil.generatePassword("123456"),object.getString("salt")));
                if(!StringUtil.empty(object.getString("identity_card"))){
                    try {
                        object.put("birthday",DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
                    } catch (Exception e) {
                        e.printStackTrace();
//                        System.out.println(JSON.toJSONString(object));
                        error.add(object);
                        continue;
                    }
                }
                try {
                    DBConnection.insert(conn,insql,object);
                } catch (SQLException e) {
                    e.printStackTrace();
//                    System.out.println(JSON.toJSONString(object));
                    error.add(object);
                    continue;
                }
            }
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
        //将失败的数据保存到json文件中重新导入
        if(error.size()>0){
            FileUtil.saveAsFileOutputStream("src//main/resources/data.json",JSON.toJSONString(error));
        }
    }


    @Test
    public void aaaada(){
        String card = "350524198504260020";
//		String card = "350524198504260040";
        card = card.substring(0,card.length()-4)+ String.format("%04d",Integer.valueOf(card.substring(card.length()-4))+20 );
        System.out.println(card);
    }

    //	@Test
    public String getIdenticard(String card){
//        List<JSONObject> list1 = reportFormDao.findAccountListByIdentity(card);
        List<JSONObject> list1 = new ArrayList<>();
        if(list1.size()>0){
            //重新生成
            card = card.substring(0,card.length()-4)+ String.format("%04d",Integer.valueOf(card.substring(card.length()-4))+20 );
            this.getIdenticard(card);
        }else{
            return card;
        }
        return "";
    }

    @Test
    public void updateMember(){
        List<JSONObject> error = new ArrayList<>();
//        List<JSONObject> list = DBConnection.query("select * from mxx_base_member m where m.creator = 'sz_lh_sq' and m.createTime > '2019-04-30 00:00:00'",null);
        List<JSONObject> list = DBConnection.query("select id,name from mxx_base_member m where head_pic is NULL",null);
//        String sql = "update mxx_base_member set `password`=#{password},salt=#{salt},head_pic=#{head_pic},updateTime=NOW() where id=#{id}";
        String sql = "update mxx_base_member set head_pic=#{head_pic},updateTime=NOW() where id=#{id}";
        Connection conn = DBConnection.getConnection();
        for (JSONObject o:list){
          /*  String pwrsMD5 = CipherUtil.generatePassword("123456");//第一次加密md5，
            String salt = CipherUtil.createSalt();
            o.put("password",CipherUtil.createPwdEncrypt(o.getString("id"), pwrsMD5, salt));
            o.put("salt",salt);*/
            String name = StringUtil.replaceAll(o.getString("name")," ","");
            try {
                o.put("head_pic", PicCreate.picCreate(name,o.getString("id")));
            } catch (IOException e) {
                error.add(o);
                e.printStackTrace();
                continue;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            try {
                DBConnection.update(conn,sql,o);
            } catch (SQLException e) {
                e.printStackTrace();
                error.add(o);
                continue;
            }
        }
        //提交
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(error.size()>0){
            FileUtil.saveAsFileOutputStream("src//main/resources/data.json",JSON.toJSONString(error));
        }
    }

    @Test
    public void bakPlan(){
        String recordListSql = "SELECT *,COUNT(1) as `count` from maxx_fp_user_record where create_id='K5Iw586c887J5e7N3L81T4xh4ROe74N5' and fp_plan_id NOT in (\n" +
                "SELECT id from maxx_fp_user_plan where create_id='K5Iw586c887J5e7N3L81T4xh4ROe74N5'\n" +
                ") and fp_plan_id not in (43013,43012,43011,43010,43009,56294) GROUP BY fp_plan_id";
        List<JSONObject> list = DBConnection.query(recordListSql);
        Connection conn = DBConnection.getConnection();
        String insql = "insert into maxx_fp_user_plan (id,user_id,user_name,user_type,create_id," +
                "create_user_type,q_id,q_title,q_type,freq_type,cycle_type,freq_time," +
                "set_exec_sum,suc_exec_sum,already_exec_sum,exec_time,next_exec_time," +
                "status,create_time,modify_time,update_time)values(#{id},#{user_id},#{user_name}," +
                "#{user_type},#{create_id},#{create_user_type},#{q_id},#{q_title},#{q_type}," +
                "#{freq_type},#{cycle_type},#{freq_time},#{set_exec_sum},#{suc_exec_sum}," +
                "#{already_exec_sum},#{exec_time},#{next_exec_time},#{status},now()," +
                "now(),now())";
        for (JSONObject o:list){
            JSONObject object = new JSONObject();
            object.put("id",o.getString("fp_plan_id"));
            object.put("user_id",o.getString("user_id"));
            object.put("user_name",o.getString("user_name"));
            object.put("user_type",o.getString("user_type"));
            object.put("create_id",o.getString("create_id"));
            object.put("create_user_type",o.getString("create_user_type"));
            object.put("q_id",o.getString("q_id"));
            object.put("q_title",o.getString("q_title"));
            object.put("q_type",o.getString("q_type"));
            Integer count = o.getInteger("count");
            if(count==1){
                object.put("set_exec_sum",1);
                object.put("suc_exec_sum",0);
                object.put("already_exec_sum",1);
                object.put("freq_type",100100600000001L);
                object.put("cycle_type",null);
                object.put("freq_time",null);
            }else{
                object.put("set_exec_sum",-1);
                object.put("suc_exec_sum",0);
                object.put("already_exec_sum",count);
                object.put("freq_type",100100600000003L);
                object.put("cycle_type",100100200000001L);
                object.put("freq_time","86400000");
            }
            String time = DateUtil.date2Str(o.getDate("record_time"),"yyyy-MM-dd HH:mm:ss");
            object.put("exec_time",time);
            object.put("next_exec_time",time);
            object.put("status",100100400000002L);
            try {
                DBConnection.insert(conn,insql,object);
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
    public void bakPlanRemind(){
        String recordListSql = "select * from maxx_fp_user_remind where fp_plan_id NOT IN(\n" +
                "select id from maxx_fp_user_plan \n" +
                ") GROUP BY fp_plan_id";
        List<JSONObject> list = DBConnection.query(recordListSql);
        Connection conn = DBConnection.getConnection();
        String insql = "insert into maxx_fp_user_plan (id,user_id,user_name,user_type,create_id," +
                "create_user_type,q_id,q_title,q_type,freq_type,cycle_type,freq_time," +
                "set_exec_sum,suc_exec_sum,already_exec_sum,exec_time,next_exec_time," +
                "status,create_time,modify_time,update_time)values(#{id},#{user_id},#{user_name}," +
                "#{user_type},#{create_id},#{create_user_type},#{q_id},#{q_title},#{q_type}," +
                "#{freq_type},#{cycle_type},#{freq_time},#{set_exec_sum},#{suc_exec_sum}," +
                "#{already_exec_sum},#{exec_time},#{next_exec_time},#{status},now()," +
                "now(),now())";
        for (JSONObject o:list){
            JSONObject object = new JSONObject();
            object.put("id",o.getString("fp_plan_id"));
            object.put("user_id",o.getString("user_id"));
            object.put("user_name",o.getString("user_name"));
            object.put("user_type",o.getString("user_type"));
            object.put("create_id",o.getString("exec_user_id"));
            object.put("create_user_type",o.getString("exec_user_type"));
            object.put("q_id",o.getString("q_id"));
            object.put("q_title",o.getString("q_title"));
            object.put("q_type",o.getString("q_type"));
//            Integer count = o.getInteger("count");
            object.put("set_exec_sum",1);
            object.put("suc_exec_sum",0);
            object.put("already_exec_sum",1);
            object.put("freq_type",100100600000001L);
            object.put("cycle_type",null);
            object.put("freq_time",null);
            String time = DateUtil.date2Str(o.getDate("exec_time"),"yyyy-MM-dd HH:mm:ss");
            object.put("exec_time",time);
            object.put("next_exec_time",time);
            object.put("status",100100400000001L);
            try {
                DBConnection.insert(conn,insql,object);
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
    public void addSchema(){
        String sql = "select * from maxx_fp_questionnaire where FIND_IN_SET(title, ?)";
        String[] names = {"",""};
        List<JSONObject> list = DBConnection.query(sql, org.apache.commons.lang3.StringUtils.join(names,","));
        Connection conn = DBConnection.getConnection();
        try {
            Long schemaId = (Long) DBConnection.insert(conn,"insert into mxx_disease_scheme(name,remark,create_time)values(?,?,now())","","");
            for (JSONObject object:list){
                JSONObject o = new JSONObject();
                o.put("scheme_id",schemaId);
                o.put("fp_id",object.getLong("object"));
                o.put("type_code",object.getLong("type_code"));
                o.put("frequency_code",-1);
//                DBConnection.insert(conn,"insert into mxx_disease_scheme_fp_ref (scheme_id,fp_id,type_code,frequency_code,create_time)values(#{scheme_id},#{fp_id},#{type_code},#{frequency_code},NOW())",o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                conn.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
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

    //更新错误数据
    @Test
    public void updatePlan(){
        try {
            List<JSONObject> list = new ExcelUtil<JSONObject>(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\bak\\备份错误的数据.xlsx"));
            Connection conn = DBConnection.getConnection();
            for (JSONObject object:list){
                //查询主要负责人
                String accountId = (String) DBConnection.queryOne("select health_officer from mxx_base_member where name = ?",object.getString("user_name"));
                JSONObject o = new JSONObject();
                o.put("id",object.getLong("id"));
                o.put("create_id",accountId);
                o.put("create_user_type",100100900000001L);
                o.put("status",100100400000001L);
                try {
                    DBConnection.update(conn,"update maxx_fp_user_plan set create_id=#{create_id},create_user_type=#{create_user_type},status=#{status} where id=#{id}",o);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void importMember(){
        List<JSONObject> error = new ArrayList<>();
        String inMemberSql = "INSERT INTO mxx_base_member (id, phone, password, salt, name, identity_card, birthday, isValid, createTime, updateTime, description, cdmsCenter,  head_pic, creator, status, health_officer)\n" +
                "values(#{id},#{phone},#{password},#{salt},#{name},#{identity_card},#{birthday},0,now(),now(),#{description},#{cdmsCenter},#{head_pic},'zz',#{status},#{health_officer})";
        String refSql = "INSERT into mxx_member_ref(member_id,account_id,role_id,role_title,grant_type,is_on_ref,remarke,status,create_time)\n" +
                "values(#{member_id},#{account_id},#{role_id},#{role_title},#{grant_type},#{is_on_ref},#{remarke},#{status},now())";
        String jcSql = "insert into  maxx_fp_user_record(descn,user_id,user_name,q_title,q_type,record_time,image_urls,create_time,modify_time,total,ss_code,st_id,test_no,specimen,result_diagnose,ss_title)\n" +
                "values(#{descn},#{user_id},#{user_name},#{q_title},#{q_type},#{record_time},#{image_urls},now(),now(),0,#{ss_code},#{st_id},#{test_no},#{specimen},#{result_diagnose},#{ss_title})";
        try {
            List<JSONObject> members = new ExcelUtil<>(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\member.xlsx"));
            List<JSONObject> jiancha = new ExcelUtil<>(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\检查.xlsx"));
            Connection conn = DBConnection.getConnection();
            int i=0;
            for (JSONObject m:members){
                //判断患者是否存在
                if(StringUtil.empty(m.getString("idNo"))){
                    continue;
                }
                Long count = (Long) DBConnection.queryOne("select count(1) from mxx_base_member where phone=?",m.getString("phoneNumberHome"));
                if(count!=0){
                    error.add(m);
                    continue;
                }
                JSONObject object = new JSONObject();
                object.put("id",UUID.randomUUID().toString().replaceAll("-",""));
                object.put("phone",m.getString("phoneNumberHome"));
                String pwrsMD5 = CipherUtil.generatePassword("123456");//第一次加密md5，
                String salt = CipherUtil.createSalt();
                object.put("password",CipherUtil.createPwdEncrypt(object.getString("id"), pwrsMD5, salt));
                object.put("salt",salt);
                object.put("name",m.getString("patientName"));
                object.put("identity_card",m.getString("idNo"));
                object.put("birthday",DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
                object.put("cdmsCenter",90);
                object.put("description","戈矶山患者");
                object.put("status",100101700000001L);
                object.put("health_officer",m.getString("accountId"));
                try {
                    object.put("head_pic", PicCreate.picCreate(object.getString("name"),object.getString("id")));
                } catch (IOException e) {
                    error.add(object);
                    e.printStackTrace();
                    continue;
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                //添加患者
                try {
                    DBConnection.insert(conn,inMemberSql,object);
                    //添加中间表
                    JSONObject ref = new JSONObject();
                    ref.put("member_id",object.getString("id"));
                    ref.put("account_id",m.getString("accountId"));
                    ref.put("role_id","e7c73a9832a94627911e90ea72778600");
                    ref.put("role_title","专科护士");
                    ref.put("grant_type",10050030060000001L);
                    ref.put("is_on_ref",0);
                    ref.put("remarke","戈矶山患者");
                    ref.put("status",100101700000001L);
                    DBConnection.insert(conn,refSql,ref);
                    //添加检查
                    Map<String,List<JSONObject>> map = jiancha.stream().collect(Collectors.groupingBy(n->n.getString("TjNo")));
                    List<JSONObject> mjiancha = map.get(m.getString("TjNo"));
                    for (JSONObject o:mjiancha){
                        o.put("descn","戈矶山患者手动录入检查数据");
                        o.put("user_id",object.getString("id"));
                        o.put("user_name",object.getString("name"));
                        o.put("q_title",o.getString("name"));
                        o.put("q_type",100100300000003L);
                        o.put("record_time",DateUtil.date2Str(o.getDate("operatetime")));
                        o.put("image_urls","[]");
                        o.put("ss_code",10410011000000003L);
                        o.put("st_id","xxxxx"+i);
                        o.put("test_no","xxxxx");
                        o.put("specimen",o.getString("reportdescribe"));
                        o.put("result_diagnose",o.getString("reportdiagnose"));
                        o.put("ss_title","戈矶山");
                        DBConnection.insert(conn,jcSql,o);
                        i++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    error.add(object);
                    conn.rollback();
                    break;
                }
            }
            //将失败的数据保存到json文件中重新导入
            if(error.size()>0){
                FileUtil.saveAsFileOutputStream("src//main/resources/data.json",JSON.toJSONString(error));
            }
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
    public void httpTest(){
        try {
            HttpUtils.get("","");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void report(){
        String sql = "SELECT GROUP_CONCAT(member_id) as ids,COUNT(1) as count,disease_title from mxx_user_disease_rel where member_id in (\n" +
                "SELECT id from mxx_base_member where health_officer  in (\n" +
                "SELECT id from mxx_base_account  WHERE cdmsCenter=36\n" +
                ")\nand status = 100101700000001" +
                ") GROUP BY disease_id";
        String cSql = "SELECT count(DISTINCT user_id) from maxx_fp_user_record where FIND_IN_SET(user_id,?)";
        List<JSONObject> list = DBConnection.query(sql, null);
        for (JSONObject object:list){
            Long count = (Long) DBConnection.queryOne(cSql,object.getString("ids"));
            object.put("随访人次",count);
        }
        try {
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\生成的数据\\测试.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void updateAccount(){
        String dbKeyOutInput = "test_3307";
        List<JSONObject> list = DBConnection.query("SELECT * from mxx_base_account  where DATE(createTime)=DATE(NOW())",null,JSONObject.class,dbKeyOutInput);
        Connection conn = DBConnection.getConnection(dbKeyOutInput);
        String phone = "";
        for (JSONObject object:list){
            phone = object.getString("phone");
            object.put("password", CipherUtil.createPwdEncrypt(object.getString("loginName"),CipherUtil.generatePassword(phone.substring(phone.length()-6,phone.length())),object.getString("salt")));
            try {
                DBConnection.update(conn,"update mxx_base_account set password=#{password} where id=#{id}",object);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
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
    public void insertAccount2(){

        List<JSONObject> list = new ArrayList<>();
        List<JSONObject> error = new ArrayList<>();
        String insql = "INSERT into mxx_base_account (id,loginName,password,salt,roleId,picUrl,name,isValid,cdmsCenter,birthday,identity_card,phone,title,createTime,updateTime)\n" +
                "values(#{open_id},#{loginName},#{password},#{salt},#{role_id},#{head_pic},#{user_name},#{isValid},#{center_id},#{birthday},#{identity_card},#{phone},#{role_title},now(),now())";
        String dbKeyInput = "test_3306";
        String dbKeyOutInput = "prod";
        try {
            list = DBConnection.query("select * from mxx_account_collect_info where id>2",null,JSONObject.class,dbKeyInput);
            Connection conn = DBConnection.getConnection(dbKeyOutInput);
            for (JSONObject object:list){
                String phone = object.getString("phone").replace(" ","");
                Long count = DBConnection.queryOne("select count(1) from mxx_base_account where id=?",object.getString("open_id"),Long.class,dbKeyOutInput);
                if(count!=0){
//                    error.add(object);
                    continue;
                }
//                object.put("id", UUID.randomUUID().toString().replaceAll("-",""));
                if(StringUtil.empty(object.getString("head_pic"))||"./images/u25.png".equals(object.getString("head_pic"))){
                    if("e7c73a9832a94627911e90ea72778600".equals(object.getString("roleId"))||"eb3bc21f45074a65893ee87305948ac6".equals(object.getString("roleId"))){
                        object.put("head_pic","cdms/default_head_pic/nurse.png");
                    }else{
                        object.put("head_pic","cdms/default_head_pic/specialist_doctor.png");
                    }
                }
                object.put("isValid",1);
                object.put("salt",CipherUtil.createSalt());
                object.put("loginName",phone);
                object.put("password", CipherUtil.createPwdEncrypt(object.getString("loginName"),CipherUtil.generatePassword(phone.substring(phone.length()-6,phone.length())),object.getString("salt")));
                if(!StringUtil.empty(object.getString("identity_card"))){
                    try {
                        object.put("birthday",DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
                    } catch (Exception e) {
                        e.printStackTrace();
//                        System.out.println(JSON.toJSONString(object));
                        error.add(object);
                        continue;
                    }
                }

                try {
                    DBConnection.insert(conn,insql,object);
//                    DBConnection.update(conn,"update mxx_base_account set password=#{password} where id=#{open_id}",object);
                } catch (SQLException e) {
                    e.printStackTrace();
//                    System.out.println(JSON.toJSONString(object));
                    error.add(object);
                    continue;
                }
            }
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
        //将失败的数据保存到json文件中重新导入
        if(error.size()>0){
            FileUtil.saveAsFileOutputStream("src//main/resources/data.json",JSON.toJSONString(error));
        }
    }

    @Test
    public void testPwd(){
        String phone = "13242102810";
        System.out.println(phone.substring(phone.length()-6,phone.length()));
    }

    @Test
    public void exportExcel(){
        List<JSONObject> list = DBConnection.query("SELECT user_name `名称`,CONCAT(\"'\",identity_card) `身份证`,phone `手机号`,center_name `医院`,role_title `角色`,area_info `区域` from mxx_account_collect_info  WHERE id>2 order by center_id asc;\n",null,JSONObject.class,"test_3306");
        list.forEach(n->{
            JSONObject object = JSON.parseObject(n.getString("区域"));
            n.put("区域",addressStr(object,object.getString("title")));
        });
        try {
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\生成的数据\\佛山医护数据.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String addressStr(JSONObject object,String str){
        if(!object.containsKey("childs")){
            return str;
        }else{
            if(object.getString("childs").equals("[null]")){
                return str;
            }
            JSONArray array = object.getJSONArray("childs");
            if(array.size()==0){
                return str;
            }
            object = array.getJSONObject(0);
            try {
                str = str+","+ (object==null?"":object.getString("title"));
            } catch (Exception e) {
                System.out.println(str);
                System.out.println(JSON.toJSONString(object));
                System.out.println(JSON.toJSONString(array));
                e.printStackTrace();
            }

            return addressStr(object,str);
        }
    }
}
