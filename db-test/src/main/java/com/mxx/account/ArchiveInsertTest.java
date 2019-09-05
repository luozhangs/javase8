package com.mxx.account;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.HttpClientSend;
import com.maxx.base.util.StringUtil;
import com.zhang.db.DBConnection;
import excel.ExcelUtil;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang on 2019/5/22.
 */
public class ArchiveInsertTest {

    @Test
    public void insert(){
        try {
            List<JSONObject> list = new ExcelUtil(JSONObject.class).readFromFile(null,new File("C:\\Users\\zhang\\Desktop\\历下归档.xlsx"));
            List<JSONObject> defect = new ArrayList<>();
//            Connection conn = DBConnection.getConnection();
            for (JSONObject object:list){
                if(StringUtil.empty(object.getString("identity_card"))){
                    defect.add(object);
                }else{
                    //判断是否存在该患者
                    JSONObject member =  DBConnection.queryOne("select * from mxx_base_member where identity_card=#{identity_card} and name=#{name}",object,JSONObject.class);
                    if(member!=null){
                        //修改mxx_base_member 和mxx_member_ref状态以及增加归档记录
                        JSONObject param = new JSONObject();
                        param.put("memberId",member.getString("id"));
                        param.put("account_id",member.getString("health_officer"));
                        param.put("tag","死亡");
                        String url = "http://localhost:8061/memberService/archived";
                        String result = "";
                        Map<String,String> head = new HashMap<>();
                        head.put("contentType","application/json;charset=UTF-8");
//                        head.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
                        StringBuffer requestBody = new StringBuffer(JSON.toJSONString(param));
                        try {
                            result = HttpClientSend.request("POST",url,head,requestBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                            defect.add(object);
                        }
                    }else{
                        defect.add(object);
                    }
                }
            }
            if(defect.size()>0){
//                FileUtil.saveAsFileOutputStream("src//main/resources/member_defect.json", JSON.toJSONString(defect));
                  ExcelUtil.writeToFile(defect,null,"C:\\Users\\zhang\\Desktop\\历下归档死亡患者(有问题).xlsx");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
