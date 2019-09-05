package db;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.DateUtil;
import com.maxx.base.util.FileUtil;
import com.maxx.base.util.StringUtil;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Created by zhang on 2019/5/11.
 */
public class MemberTest {

    @Test
    public void importMemberLIXI(){
        try {
            //角色集合
            List<JSONObject>  roles = DBConnection.query("SELECT id,name,cdmsCenter,roleId from mxx_base_account a where \n" +
                    "name in ('季霞','王娟','胡碧云','周蓉','王晓红','倪芳','郑元英','王连梅','宋霞','陈月云','唐军','夏朝红','姚新明','赵咏莉','高家林','汪裕伟','汤圣兴','王新','王德国')\n" +
                    "and id !='2Xi8aw3d4PMfDmJSm6rTY8qb4iT1mS22' ",null);
            Map<String,JSONObject> roleMap = new HashMap<>();
            for (JSONObject object:roles){
                roleMap.put(object.getString("name").trim(),object);
            }
            //疾病对应的疾病数据数据（字典表）
            List<JSONObject> disease = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\以及山字典表.xlsx"));
            Map<String,String> map = new HashMap<>();
            for (JSONObject object:disease){
                object.forEach((k,v)->{
                    if(!"diagnois_num".equals(k)&&"1".equals(v)){
                        map.put(object.getString("disease_name").trim(),k);
                    }
                });
            }
            //疾病对应的医护人员数据
            Map<String,String[]> doctorMap = new HashMap<>();
            doctorMap.put("高血压",new String[]{"陈月云","唐军","夏朝红"});
            doctorMap.put("糖尿病",new String[]{"姚新明","赵咏莉","高家林"});
            doctorMap.put("慢性肾脏病",new String[]{"汪裕伟"});
            doctorMap.put("冠心病",new String[]{"汤圣兴","王新"});
            doctorMap.put("心律失常",new String[]{"王德国"});
            Map<String,String[]> nurseMap = new HashMap<>();
            String[] arr = {"季霞","王娟","胡碧云","周蓉"};
            nurseMap.put("高血压",arr );
            nurseMap.put("冠心病",arr);
            nurseMap.put("心律失常",arr);
            nurseMap.put("糖尿病",new String[]{"王晓红","倪芳"});
            nurseMap.put("慢性肾脏病",new String[]{"郑元英","王连梅","宋霞"});//专科护士周蓉
            //患者数据
//            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下\\基层患者数据.xlsx"));
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
                    "\thospital_patient_outpatient where id_no not in (select id_no from hospital_patient_hospitalization)\n" +
                    "GROUP BY id_no ",null,JSONObject.class,"db2");
            List<JSONObject> newList = new ArrayList<>();
            //异常数据集合
            List<JSONObject> defect = new ArrayList<>();
            //用于排重的集合，存身份证
            List<String> identifys = new ArrayList<>();
            newList = list.stream()
                    .filter(n->{
                //先去重
                if(identifys.contains(n.getString("identity_card"))){
                    n.put("reason","重复数据:身份证重复");
                    defect.add(n);
                    return false;
                }else{
                    identifys.add(n.getString("identity_card"));
                    return true;
                }
            })
                    .map(n->{
                n.forEach((k,v)->{
                    if(!StringUtil.empty(String.valueOf(v))){  //去除前后空格
                        n.put(k,String.valueOf(v).trim());
                    }
                });
                return n;
            }).collect(Collectors.toList());

            //新增患者
            String inMemberSql = "INSERT INTO mxx_base_member_data_yijishan (id, phone, password, salt, name, identity_card, birthday, isValid, createTime, updateTime, description, cdmsCenter,  head_pic,home_address, creator, status, health_officer,current_health_problem,medical_attribution_status)\n" +
                    "values(#{id},#{phone},#{password},#{salt},#{name},#{identity_card},#{birthday},0,now(),now(),#{description},#{cdmsCenter},#{head_pic},#{home_address},#{creator},#{status},#{health_officer},#{current_health_problem},#{medical_attribution_status})";
            String inExtraSql = "INSERT into mxx_member_extra_yijishan(member_id,home_address,home_tel,contact_phone,source_info)\n" +
                    "values(#{member_id},#{home_address},#{home_tel},#{contact_phone},#{source_info})";
            Connection conn = DBConnection.getConnection();
            int i= 1;
            for (JSONObject object:newList){
                System.out.println(newList.size()+"--"+i);
                i++;
                //判断其他关键字段是否错误
                object = this.checkMemberData(object,roleMap,map,doctorMap,nurseMap);
                if(!object.getBoolean("flag")){
                    defect.add(object);
                    continue;
                }
                try {
//                    object.put("home_tel",StringUtil.empty(object.getString("home_tel"))?object.getString("phone"):object.getString("home_tel"));
                    if(object.getString("phone")!=null&&object.getString("phone").length()>15){
                       object.put("phone",object.getString("phone").substring(0,13));
                    }

//                    object.put("cdmsCenter",healthOfficer.getInteger("cdmsCenter"));
                    object.put("description","HIS数据导入门诊数据");
                    String memberId = UUID.randomUUID().toString().replaceAll("-","");
                    object.put("id",memberId );
                    String pwrsMD5 = CipherUtil.generatePassword("123456");//第一次加密md5，
                    String salt = CipherUtil.createSalt();
                    object.put("password",CipherUtil.createPwdEncrypt(memberId, pwrsMD5, salt));
                    object.put("salt",salt);
                    object.put("status",100101700000001L);
//                    object.put("health_officer",healthOfficer.getString("id"));
                    object.put("medical_attribution_status",10050040040000001L);//县域
//                    object.put("head_pic", PicCreate.picCreate(object.getString("name"),object.getString("id")));
                    object.put("current_health_problem",object.getString("tag")==null?object.getString("disease_title"):object.getString("disease_title")+"$"+object.getString("tag"));
                    object.put("creator","zz");
                    DBConnection.insert(conn,inMemberSql,object);
                    //添加医护团队
                    //根据excel数据以及匹配规则情况适当修改
                    Pattern p = Pattern.compile("[a-zA-z_]");
                    object.forEach((k,v)->{
                        if(!p.matcher(k).find()){
                            try {
                                /*if("专科医生".equals(k)){
//                                    String roleId = (String) DBConnection.queryOne("select id from mxx_base_role where  name=?",k);
                                    if(map.get(object.getString("disease_title"))!=null){
                                        addMemberRef(conn,object.getString("id"),map.get(object.getString("disease_title")),"66efc7ef5aa64f3b846a368676d7a375",k);
                                    }
                                }else{
                                    JSONObject account = roleMap.get(v);
                                    if(account==null){
                                        JSONObject object1 = (JSONObject) object.clone();
                                        object1.put("reason","缺失医护人员:"+k+"库中不存在");
                                        defect.add(object1);
                                        return;
                                    }else{
                                        addMemberRef(conn,object.getString("id"),account.getString("id"),account.getString("roleId"),k);
                                    }
                                }*/
                                //弋矶山HIS数据导入流程
                                JSONObject account = (JSONObject) v;
                                this.addMemberRef(conn,memberId,account.getString("id"),account.getString("roleId"),k);
                            } catch (SQLException e) {
                                e.printStackTrace();
//                                defect.add(object);
                            }
                        }
                    });


                    //添加患者额外信息数据
                    /*JSONObject extra = new JSONObject();
                    extra.put("member_id",object.getString("id"));
                    extra.put("home_address",object.getString("home_address"));
                    extra.put("home_tel",object.getString("home_tel"));
                    if(object.getString("phone")!=null&&object.getString("phone").length()<=11){
                        extra.put("contact_phone",object.getString("phone"));
                    }
                    extra.put("source_info","2019-05-16导入数据历下数据");

                    DBConnection.insert(conn,inExtraSql,extra);*/
                } catch (Exception e) {
                    e.printStackTrace();
                    object.put("reason","异常数据:插入数据异常");
                    defect.add(object);
                    conn.rollback();
                    break;
                }

            }
            //导出缺失的数据
            //将失败的数据保存到json文件中重新导入
            if(defect.size()>0){
//                FileUtil.saveAsFileOutputStream("src//main/resources/member_defect.json", JSON.toJSONString(defect));
                Map<String,List<JSONObject>> listMap = defect.stream().collect(Collectors.groupingBy(n->n.getString("reason").split(":")[0]));
                listMap.forEach((k,v)->{
                    try {
                        ExcelUtil.writeToFile(v,null,"C:\\Users\\zhang\\Desktop\\弋矶山门诊错误数据("+k+").xlsx");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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

    /**
     * 校验数据
     * @param object
     * @return
     */
    public JSONObject checkMemberData(JSONObject object,Map<String,JSONObject> roleMap,Map<String,String> diseaseMap,Map<String,String[]> docorMap,Map<String,String[]> nurseMap){
        Boolean flag = true;
        //验证身份证是否为空
        if(StringUtil.empty(object.getString("identity_card"))){
            object.put("reason","异常数据:身份证为空");
            flag =  false;
        }
        //判断手机号
        //库中没有为空的
        /*if(StringUtil.empty(object.getString("phone"))){
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
        //已有数据不需要生成
        try {
            object.put("birthday", DateUtil.date2Str(DateUtil.str2Date(object.getString("identity_card").substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
           /* String sex = "女";
            if (Integer.parseInt(object.getString("identity_card").substring(16).substring(0, 1)) % 2 == 0) {// 判断性别
                sex = "女";
            } else {
                sex = "男";
            }
            object.put("sex",sex);*/
        } catch (Exception e) {
            e.printStackTrace();
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:身份证格式错误":object.getString("reason")+",身份证格式错误");
            flag =  false;
        }
        if(StringUtil.empty(object.getString("disease_title"))){
            object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:诊断疾病为空":object.getString("reason")+",诊断疾病为空");
            flag =  false;
        }else{
            //医护团队
            //根据字典表定位疾病
            String[] diseases = object.getString("disease_title").split("[$]");
            Map<String,Integer> disMap = new HashMap<>();
            for (String dis:diseases){
                String diseaseTitle = diseaseMap.get(dis);
                if(diseaseTitle!=null){
                    disMap.put(diseaseTitle,1);
                }
            }

            if(disMap.size()==0){
                object.put("reason",StringUtil.empty(object.getString("reason"))?"异常数据:字典表中无该诊断疾病":object.getString("reason")+",字典表中无该诊断疾病");
                flag =  false;
            }else{
                //判断优先级
                String diseaseTitle = this.highLevelDisease(disMap);
                if("PCI术后".equals(diseaseTitle)){
                    object.put("tag","PCI术后");
                    diseaseTitle = "冠心病";
                }
                if("起搏器术后".equals(diseaseTitle)){
                    object.put("tag","起搏器术后");
                    diseaseTitle = "心律失常";
                }
                //判断病人的专科医生库中是否存在
                //专科医生是谁（如果库中不存在也随机分配）
                //专科护士是是谁（随机分配）
                //慢性肾脏病有全科护士   健康责任人人是专科护士
                JSONObject doctor = (JSONObject) DBConnection.queryOne("select id,name,roleId,cdmsCenter from mxx_base_account where name=? and roleId='66e1d82e9a90418f98ac8e3371a60dfe'",object.getString("doc_name"));
                if(doctor==null){
                    doctor = roleMap.get(randAccountName(docorMap.get(diseaseTitle)));
                }
                object.put("专科医生",doctor);
                if("慢性肾脏病".equals(diseaseTitle)){
                    JSONObject quanke = roleMap.get(this.randAccountName(nurseMap.get(diseaseTitle)));
                    JSONObject zhuanke = roleMap.get("周蓉");
                    object.put("全科护士",quanke);
                    object.put("专科护士",zhuanke);
                    object.put("cdmsCenter",zhuanke.getInteger("cdmsCenter"));
                    object.put("health_officer",zhuanke.getString("id"));
                }else{
                    JSONObject zhuanke = roleMap.get(this.randAccountName(nurseMap.get(diseaseTitle)));
                    object.put("专科护士",zhuanke);
                    object.put("cdmsCenter",zhuanke.getInteger("cdmsCenter"));
                    object.put("health_officer",zhuanke.getString("id"));
                }

            }

        }

        object.put("flag",flag);
        return object;
    }

    /**
     * 疾病优先级
     * @param diseaseTags
     * @return
     */
    public String highLevelDisease(Map<String,Integer> diseaseTags){
        String[] levelDis = {"PCI术后","冠心病","起搏器术后","心律失常","慢性肾脏病","糖尿病","高血压"};
        for (String dis:levelDis){
            if(diseaseTags.containsKey(dis)){
                return dis;
            }
        }
        return null;
    }

    /**
     * 随机获取医护名称
     * @param arr
     * @return
     */
    public String randAccountName(String[] arr){
        Random random = new Random();
        return  arr[random.nextInt(arr.length)];
    }


    public void addMemberRef(Connection conn,String memberId,String accountId,String roleId,String roleTitle) throws SQLException {
        String inRefSql = "INSERT into mxx_member_ref_yijishan(member_id,account_id,role_id,role_title,grant_type,is_on_ref,remarke,status,create_time)\n" +
                "values(#{member_id},#{account_id},#{role_id},#{role_title},#{grant_type},#{is_on_ref},#{remarke},#{status},now())";
        //添加中间表
        JSONObject ref = new JSONObject();
        ref.put("member_id",memberId);
        ref.put("account_id",accountId);
        ref.put("role_id",roleId);
        ref.put("role_title",roleTitle);
        ref.put("grant_type",10050030060000001L);
        ref.put("is_on_ref",0);
        ref.put("remarke","2019-05-16导入数据");
        ref.put("status",100101700000001L);
        DBConnection.insert(conn,inRefSql,ref);
    }

    @Test
    public void exportExcel(){
        try {
            String str = FileUtil.readTextFile("src//main/resources/member_defect.json");
            List<JSONObject> list = JSON.parseArray(str,JSONObject.class);
            ExcelUtil.writeToFile(list,null,"C:\\Users\\zhang\\Desktop\\历下县域错误数据.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insertUserTag(){
        String accountSql = "SELECT * from mxx_member_ref_yijishan  GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagSql = "INSERT INTO mxx_user_tag(title,account_id,remark,create_time)values(#{title},#{account_id},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        int i=1;
        for (JSONObject object:list){
            System.out.println(list.size()+"--"+i);
            i++;
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member_data_yijishan where id in (\n" +
                    "SELECT member_id from mxx_member_ref_yijishan where  account_id=?\n" +
                    ") GROUP BY current_health_problem",object.getString("account_id"));
            for (JSONObject member:members){
                String[] diseases = member.getString("current_health_problem").split("[$]");
                Map<String,Object> param = new HashMap<>();
                param.put("account_id",object.getString("account_id"));
                for (String disease:diseases){
                    param.put("remark",disease);
                    //查询是否已存在
                    Long count = DBConnection.queryOne("SELECT count(1) from mxx_user_tag where  account_id=#{account_id} and title  like 'HIS数据导入%' and remark=#{remark} ",param,Long.class);
                    if(count==0){
                        JSONObject tag = new JSONObject();
                        tag.put("title","HIS数据导入,"+disease);
                        tag.put("account_id",object.getString("account_id"));
                        tag.put("remark",disease);
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
                try {
                    conn.commit();
                } catch (SQLException e) {
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
    public void insertTagRefTest(){
        String accountSql = "SELECT * from mxx_member_ref_yijishan   GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagRefSql = "INSERT into mxx_user_tag_ref (tag_id,member_id,account_id,status,remark,create_time)values(#{tag_id},#{member_id},#{account_id},#{status},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        int i = 0;
        for (JSONObject account:list){
            i++;
            String accountId = account.getString("account_id");
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member_data_yijishan where id in (\n" +
                    "SELECT member_id from mxx_member_ref_yijishan where  account_id=?\n" +
                    ")",accountId);
            List<JSONObject> tags = DBConnection.query("SELECT id,remark from  mxx_user_tag where title  like 'HIS数据导入%'  and account_id=?",accountId);
            Map<String,Long> tagMap = new HashMap<>();
            for (JSONObject tag:tags){
                tagMap.put(tag.getString("remark"),tag.getLong("id"));
            }
            int j=1;
            for (JSONObject member:members){
                System.out.println(i+"--"+members.size()+"--"+j);
                j++;
                String[] diseases = member.getString("current_health_problem").split("[$]");
                for (String disease:diseases){
                    JSONObject tagRef = new JSONObject();
                    tagRef.put("tag_id",tagMap.get(disease));
                    tagRef.put("member_id",member.getString("id"));
                    tagRef.put("account_id",accountId);
                    tagRef.put("status",100101700000001L);
                    tagRef.put("remark","HIS数据导入");
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
