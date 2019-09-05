package com.mxx.member;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.FileUtil;
import com.maxx.base.util.StringUtil;
import com.mxx.base.utils.security.CipherUtil;
import com.zhang.db.DBConnection;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by zhang on 2019/5/11.
 */
public class MemberTest2 {

    @Test
    public void importMemberLIXI(){
        try {
            //角色集合
            List<JSONObject>  roles = DBConnection.query("SELECT id,name,cdmsCenter,roleId from mxx_base_account where cdmsCenter in (SELECT center_id from mxx_medical_center_relation where medical_id=19)",null);
            Map<String,JSONObject> roleMap = new HashMap<>();
            for (JSONObject object:roles){
                roleMap.put(object.getString("name").trim(),object);
            }
            //患者数据
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下\\历下基层错误数据(健康责任人匹配不上).xlsx"));
            List<JSONObject> newList = new ArrayList<>();
            List<JSONObject> defect = new ArrayList<>();
            List<String> identifys = new ArrayList<>();
            newList = list.stream().map(n->{
                n.forEach((k,v)->{
                    if(!StringUtil.empty(String.valueOf(v))){
                        n.put(k,v.toString().trim().replaceAll(" ",","));
                    }
                });
                return n;
            }).collect(Collectors.toList());
            //新增患者
            String inMemberSql = "INSERT INTO mxx_base_member_data_lixia (id, phone, password, salt, name, identity_card, birthday, isValid, createTime, updateTime, description, cdmsCenter,  head_pic,home_address, creator, status, health_officer,current_health_problem,medical_attribution_status)\n" +
                    "values(#{id},#{phone},#{password},#{salt},#{name},#{identity_card},#{birthday},0,now(),now(),#{description},#{cdmsCenter},#{head_pic},#{home_address},#{creator},#{status},#{health_officer},#{current_health_problem},#{medical_attribution_status})";
            //疾病对应的医护数据
            List<JSONObject> disease = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下\\disease_account基层.xlsx"));
            Map<String,String> map = new HashMap<>();
            for (JSONObject object:disease){
                object.forEach((k,v)->{
                    if(!"diagnois_num".equals(k)&&"1".equals(v)){
                        map.put(object.getString("disease_name").trim().replaceAll(" ",","),k);
                    }
                });
            }
            Connection conn = DBConnection.getConnection();
            int i= 1;
            System.out.println(newList.size());
            for (JSONObject object:newList){
                System.out.println(newList.size()+"--"+i);
                i++;
                //判断其他关键字段是否错误
                object = this.checkMemberData(object,roleMap);
                if(!object.getBoolean("flag")){
                    defect.add(object);
                    continue;
                }
                try {
//                    object.put("home_tel",StringUtil.empty(object.getString("home_tel"))?object.getString("phone"):object.getString("home_tel"));
                    if(object.getString("phone")!=null&&object.getString("phone").length()>15){
                       object.put("phone","");
                    }
                   /* try {
                        object.put("birthday", DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        object.put("reason","身份证格式错误");
                        defect.add(object);
                        continue;
                    }*/
                    object.put("description","历下基层数据导入");
                    String memberId = UUID.randomUUID().toString().replaceAll("-","");
                    object.put("id",memberId );
                    String pwrsMD5 = CipherUtil.generatePassword("123456");//第一次加密md5，
                    String salt = CipherUtil.createSalt();
                    object.put("password",CipherUtil.createPwdEncrypt(memberId, pwrsMD5, salt));
                    object.put("salt",salt);
                    object.put("status",100101700000001L);
//                    object.put("health_officer",healthOfficer.getString("id"));
                    object.put("medical_attribution_status",10050040040000002L);//基层
//                    object.put("head_pic", PicCreate.picCreate(object.getString("name"),object.getString("id")));
                    object.put("current_health_problem",object.getString("disease_title"));
                    object.put("creator","zz");
                    DBConnection.insert(conn,inMemberSql,object);
                    //添加医护团队
                    Pattern p = Pattern.compile("[a-zA-z_]");
                    JSONObject finalObject = object;
                    object.forEach((k, v)->{
                        if(!p.matcher(k).find()){
                            try {
                                if("专科医生".equals(k)){
                                    if(map.get(finalObject.getString("disease_title"))!=null){
                                        addMemberRef(conn,finalObject.getString("id"),map.get(finalObject.getString("disease_title")),"66efc7ef5aa64f3b846a368676d7a375",k);
                                    }
                                }else{
                                    JSONObject account = roleMap.get(v);
                                    if(account==null){
                                        System.out.println(v);
                                        finalObject.put("reason","异常数据:"+k+"库中不存在");
                                        defect.add(finalObject);
                                        return;
                                    }else{
                                        if(account.getString("name").equals(k)){
                                            addMemberRef(conn,finalObject.getString("id"),account.getString("id"),account.getString("roleId"),k);
                                        }else{
                                            finalObject.put("reason","异常数据:"+v+"与库中角色不一致");
                                            defect.add(finalObject);
                                        }
                                    }
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    object.put("reason","异常数据:插入数据异常");
                    defect.add(object);
                    break;
                }

            }
            //提交
            try {
                conn.commit();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //导出缺失的数据
            //将失败的数据保存到json文件中重新导入
            if(defect.size()>0){
//                FileUtil.saveAsFileOutputStream("src//main/resources/member_defect.json", JSON.toJSONString(defect));
                Map<String,List<JSONObject>> listMap = defect.stream().collect(Collectors.groupingBy(n->n.getString("reason").split(":")[0]));
                listMap.forEach((k,v)->{
                    try {
                        ExcelUtil.writeToFile(v,null,"C:\\Users\\zhang\\Desktop\\历下基层错误数据("+k+").xlsx");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMemberRef(Connection conn,String memberId,String accountId,String roleId,String roleTitle) throws SQLException {
        String inRefSql = "INSERT into mxx_member_ref_lixia(member_id,account_id,role_id,role_title,grant_type,is_on_ref,remarke,status,create_time)\n" +
                "values(#{member_id},#{account_id},#{role_id},#{role_title},#{grant_type},#{is_on_ref},#{remarke},#{status},now())";
        //添加中间表
        JSONObject ref = new JSONObject();
        ref.put("member_id",memberId);
        ref.put("account_id",accountId);
        ref.put("role_id",roleId);
        ref.put("role_title",roleTitle);
        ref.put("grant_type",10050030060000001L);
        ref.put("is_on_ref",0);
        ref.put("remarke","2019-05-15导入数据");
        ref.put("status",100101700000001L);
        DBConnection.insert(conn,inRefSql,ref);
    }

    /**
     * 校验数据
     * @param object
     * @return
     */
    public JSONObject checkMemberData(JSONObject object,Map<String,JSONObject> roleMap){
        Boolean flag = true;
        //验证身份证是否为空
        if(StringUtil.empty(object.getString("identity_card"))){
            object.put("reason","异常数据:身份证为空");
            flag =  false;
        }
        //判断手机号
     /*   if(StringUtil.empty(object.getString("phone"))){
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:手机号为空":object.getString("reason")+",手机号为空");
            flag =  false;
        }*/
        //判断身份证是否已入库
        Long count= DBConnection.queryOne("SELECT COUNT(1) from mxx_base_member where identity_card=?",object.getString("identity_card"),Long.class);
        if(count>0){
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:身份证已入库":object.getString("reason")+",身份证已入库");
            flag =  false;
        }
        //验证身份证格式
        /*try {
            object.put("birthday", DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
        } catch (Exception e) {
            e.printStackTrace();
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:身份证格式错误":object.getString("reason")+",身份证格式错误");
            flag =  false;
        }*/
        JSONObject healthOfficer = roleMap.get(object.getString("全科护士"));
        if(healthOfficer==null){
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:健康责任人匹配不上":object.getString("reason")+",健康责任人匹配不上");
           flag = false;
        }else{
            object.put("cdmsCenter",healthOfficer.getInteger("cdmsCenter"));
            object.put("health_officer",healthOfficer.getString("id"));
        }
        object.put("flag",flag);
        return object;
    }

    @Test
    public void exportExcel(){
        try {
            String str = FileUtil.readTextFile("src//main/resources/member_defect.json");
            List<JSONObject> list = JSON.parseArray(str,JSONObject.class);
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\历下基层错误数据.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertUserTag(){
        String accountSql = "SELECT * from mxx_member_ref_lixia where DATE(create_time)=DATE(NOW()) and remarke like '2019-05-15%' GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagSql = "INSERT INTO mxx_user_tag(title,account_id,remark,create_time)values(#{title},#{account_id},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        for (JSONObject object:list){
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member where id in (\n" +
                    "SELECT member_id from mxx_member_ref_lixia where remarke like '2019-05-15%' and DATE(create_time)=DATE(NOW()) and account_id=?\n" +
                    ") GROUP BY current_health_problem",object.getString("account_id"));
            for (JSONObject member:members){
                //查询是否已存在
                Long count = DBConnection.queryOne("SELECT count(1) from mxx_user_tag where title  like '历下区人民医院导入%' and remark=?",member.getString("current_health_problem"),Long.class);
                if(count==0){
                    JSONObject tag = new JSONObject();
                    tag.put("title","历下区人民医院导入,"+member.getString("current_health_problem"));
                    tag.put("account_id",object.getString("account_id"));
                    tag.put("remark",member.getString("current_health_problem"));
                    try {
                        DBConnection.insert(conn,tagSql,tag);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        try {
                            conn.rollback();
                        } catch (SQLException e1) {
                            e1.printStackTrace();
                        }
                        break;
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
    public void insertTagRefTest(){
        String accountSql = "SELECT * from mxx_member_ref_lixia where DATE(create_time)=DATE(NOW()) and remarke like '2019-05-15%' GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagRefSql = "INSERT into mxx_user_tag_ref (tag_id,member_id,account_id,status,remark,create_time)values(#{tag_id},#{member_id},#{account_id},#{status},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        int i = 1;
        for (JSONObject account:list){
            System.out.println(i);
            i++;
            String accountId = account.getString("account_id");
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member where id in (\n" +
                    "SELECT member_id from mxx_member_ref_lixia where DATE(create_time)=DATE(NOW()) and remarke like '2019-05-15%' and account_id=?\n" +
                    ")",accountId);
            List<JSONObject> tags = DBConnection.query("SELECT id,remark from  mxx_user_tag where title  like '历下区人民医院导入%'  and account_id=?",accountId);
            Map<String,Long> tagMap = new HashMap<>();
            for (JSONObject tag:tags){
                tagMap.put(tag.getString("remark"),tag.getLong("id"));
            }
            int j=1;
            for (JSONObject member:members){
                System.out.println(members.size()+"--"+j);
                j++;
                JSONObject tagRef = new JSONObject();
                tagRef.put("tag_id",tagMap.get(member.getString("current_health_problem")));
                tagRef.put("member_id",member.getString("id"));
                tagRef.put("account_id",accountId);
                tagRef.put("status",100101700000001L);
                tagRef.put("remark","历下区人民医院导入");
                try {
                    DBConnection.insert(conn,tagRefSql,tagRef);
                } catch (SQLException e) {
                    e.printStackTrace();
                    try {
                        conn.rollback();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    break;
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
    public void importData(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下\\基层患者数据.xlsx"));
            String sql = "insert into mxx_base_member_import (`name`, `sex`, `age`, `identity_card`, `phone`,`disease_title`, `home_address`, `center_name`, `quanke_doctor`, `quanke_nurse`, `gongwei`, `zhuanke_doctor`, `zhuanke_nurse`, `zhongyi`, `descn`, `create_time`)" +
                    "values(#{name},#{sex},#{age},#{identity_card},#{phone},#{disease_title},#{home_address},#{center_name},#{quanke_doctor},#{quanke_nurse},#{gongwei},#{zhuanke_doctor},#{zhuanke_nurse},#{zhongyi},#{descn},now())";
            Connection conn = DBConnection.getConnection();
            for (JSONObject object:list){
            object.put("quanke_doctor",object.getString("全科医生"));
            object.put("quanke_nurse",object.getString("全科护士"));
            object.put("gongwei",object.getString("公卫"));
            object.put("zhuanke_doctor",object.getString("专科医生"));
            object.put("zhuanke_nurse",object.getString("专科护士"));
            object.put("zhongyi",object.getString("中医"));
            object.put("descn","基层");
            DBConnection.insert(conn,sql,object);
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

}
