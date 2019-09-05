package db;

import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.StringUtil;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by zhang on 2019/5/15.
 */
public class MemberTest3 {

    @Test
    public void gongwei(){
        //角色集合
        List<JSONObject> roles = DBConnection.query("SELECT id,name,cdmsCenter,roleId from mxx_base_account where cdmsCenter in (SELECT center_id from mxx_medical_center_relation where medical_id=19)",null);
        Map<String,JSONObject> roleMap = new HashMap<>();
        for (JSONObject object:roles){
            roleMap.put(object.getString("name").trim(),object);
        }
        //患者数据
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下\\历下基层错误数据(全科医生库中不存在).xlsx"));
            List<JSONObject> defect = new ArrayList<>();
            Connection conn = DBConnection.getConnection();
            int i= 1;
            System.out.println(list.size());
            for (JSONObject object:list){
                System.out.println(list.size()+"--"+i);
                i++;
                String name= object.getString("全科医生").trim();
                JSONObject account = roleMap.get(name);
                if(account==null){
                    System.out.println(name);
                    object.put("reason","异常数据:全科医生库中不存在");
                    defect.add(object);
                    continue;
                }else{
                    String memberId = (String) DBConnection.queryOne("select id from mxx_base_member where identity_card=?",object.getString("identity_card"));
                    addMemberRef(conn,memberId,account.getString("id"),account.getString("roleId"),"全科医生");
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
                        ExcelUtil.writeToFile(v,null,"C:\\Users\\zhang\\Desktop\\历下全科医生错误数据("+k+").xlsx");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addMemberRef(Connection conn, String memberId, String accountId, String roleId, String roleTitle) throws SQLException {
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
        ref.put("remarke","2019-05-15导入全科医生数据");
        ref.put("status",100101700000001L);
        DBConnection.insert(conn,inRefSql,ref);
    }


    @Test
    public void updateMember(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\社区慢病人员名单（信息不全完善后.xlsx"));
            List<JSONObject> defect = new ArrayList<>();
            Connection conn = DBConnection.getConnection();
            int i= 1;
            System.out.println(list.size());
            for (JSONObject object:list){
                Long count= DBConnection.queryOne("SELECT COUNT(1) from mxx_base_member where identity_card=?",object.getString("identity_card"),Long.class);
                if(count>0){
                    try {
                        DBConnection.update(conn,"update mxx_base_member set phone=? where identity_card=?",object.getString("phone"),object.getString("identity_card"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        conn.rollback();
                        break;
                    }
                }else{
                    defect.add(object);
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
                ExcelUtil.writeToFile(defect,null,"C:\\Users\\zhang\\Desktop\\历下补全手机号未入库的患者.xlsx");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
