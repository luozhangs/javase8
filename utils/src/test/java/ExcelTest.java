import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lkx.util.ExcelUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang on 2019/3/13.
 */
public class ExcelTest {

    @Test
    public void importTest(){
        String filePath = "C:\\Users\\zhang\\Desktop\\生成的数据\\佛山医护数据.xlsx";
        Map<String,String> map = new HashMap<>();
        map.put("id","id");
        try {
            List<JSONObject> list = ExcelUtil.readXls(filePath,map,"com.alibaba.fastjson.JSONObject",null);
            System.out.println(JSON.toJSONString(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
