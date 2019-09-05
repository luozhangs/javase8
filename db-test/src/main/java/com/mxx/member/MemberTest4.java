package com.mxx.member;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.FileUtil;
import com.maxx.base.util.StringUtil;
import com.zhang.db.DBConnection;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by zhang on 2019/5/11.
 */
public class MemberTest4 {

    @Test
    public void importMemberLIXI(){
        try {

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

            List<JSONObject> list = DBConnection.query("SELECT id,current_health_problem from mxx_base_member_data_yijishan where id='bea4850da0724b849f15174e2b429584'",null);
            List<JSONObject> newList = new ArrayList<>();
            //异常数据集合
            List<JSONObject> defect = new ArrayList<>();
            //用于排重的集合，存身份证
            List<String> identifys = new ArrayList<>();
            newList = list.stream()
                    .map(n->{
                n.forEach((k,v)->{
                    if(!StringUtil.empty(String.valueOf(v))){  //去除前后空格
                        n.put(k,String.valueOf(v).trim());
                    }
                });
                return n;
            }).collect(Collectors.toList());

            Connection conn = DBConnection.getConnection();
            int i= 1;
            String sql = "update mxx_base_member_data_yijishan set current_health_problem =#{current_health_problem} where id=#{id}";
            for (JSONObject object:newList){
                System.out.println(newList.size()+"--"+i);
                i++;
                //判断其他关键字段是否错误
                object = this.checkMemberData(object,null,map,null,null);
                if(!object.getBoolean("flag")){
                    defect.add(object);
                    continue;
                }
                if(object.getString("id")!=null){
                    object.put("current_health_problem",object.getString("tag")==null?object.getString("current_health_problem"):object.getString("current_health_problem")+"$"+object.getString("tag"));
                    try {
                        DBConnection.update(conn,sql,object);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        conn.rollback();
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

    /**
     * 校验数据
     * @param object
     * @return
     */
    public JSONObject checkMemberData(JSONObject object,Map<String,JSONObject> roleMap,Map<String,String> diseaseMap,Map<String,String[]> docorMap,Map<String,String[]> nurseMap){
        Boolean flag = true;

        if(StringUtil.empty(object.getString("current_health_problem"))){
            flag =  false;
        }else{
            //医护团队
            //根据字典表定位疾病
            String[] diseases = object.getString("current_health_problem").split("[$]");
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
        String accountSql = "SELECT * from mxx_member_ref_yijishan where remarke='2019-05-16导入数据' GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagSql = "INSERT INTO mxx_user_tag(title,account_id,remark,create_time)values(#{title},#{account_id},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        for (JSONObject object:list){
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member_data_yijishan where id in (\n" +
                    "SELECT member_id from mxx_member_ref_yijishan where remarke='2019-05-16导入数据' and account_id=?\n" +
                    ") GROUP BY current_health_problem",object.getString("account_id"));
            for (JSONObject member:members){
                member.put("current_health_problem",member.getString("current_health_problem"));
                //查询是否已存在
                Long count = DBConnection.queryOne("SELECT count(1) from mxx_user_tag where title  like 'HIS数据导入%' and remark=?",member.getString("current_health_problem"),Long.class);
                if(count==0){
                    JSONObject tag = new JSONObject();
                    tag.put("title","HIS数据导入,"+member.getString("current_health_problem"));
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
        String accountSql = "SELECT * from mxx_member_ref_yijishan where remarke='2019-05-16导入数据'  GROUP BY account_id";
        List<JSONObject> list = DBConnection.query(accountSql,null);
        String tagRefSql = "INSERT into mxx_user_tag_ref (tag_id,member_id,account_id,status,remark,create_time)values(#{tag_id},#{member_id},#{account_id},#{status},#{remark},now())";
        Connection conn = DBConnection.getConnection();
        int i = 1;
        for (JSONObject account:list){
            System.out.println(i);
            i++;
            String accountId = account.getString("account_id");
            List<JSONObject> members = DBConnection.query("SELECT * from mxx_base_member_data_yijishan where id in (\n" +
                    "SELECT member_id from mxx_member_ref_yijishan where remarke='2019-05-16导入数据'  and account_id=?\n" +
                    ")",accountId);
            List<JSONObject> tags = DBConnection.query("SELECT id,remark from  mxx_user_tag where title  like 'HIS数据导入%'  and account_id=?",accountId);
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
        //提交
        try {
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
