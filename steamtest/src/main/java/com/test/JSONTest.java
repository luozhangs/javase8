package com.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.maxx.base.util.DateUtil;
import org.junit.Test;

import java.util.Calendar;

/**
 * Created by zhang on 2019/3/11.
 */
public class JSONTest {

    @Test
    public void json(){
        String arr = "[[1,2,3,4],[4,3,45,6]]";
        JSONArray array = JSON.parseArray(arr);
        for (int i=0;i<array.size();i++){
            JSONArray array1 = array.getJSONArray(i);
            for (Object oo:array1){
                System.out.println(oo);
            }

        }
    }
    public String[] getDateFromWeek(int year,int weekofyear){
        String[] strings = new String[2];
        Calendar calendar = Calendar.getInstance();
        calendar.setWeekDate(year,weekofyear,Calendar.MONDAY);
        strings[0]= DateUtil.date2Str(calendar.getTime(),"yyyy-MM-dd")+" 00:00:00";
        calendar.setWeekDate(year,weekofyear+1,Calendar.SUNDAY);
        strings[1] = DateUtil.date2Str(calendar.getTime(),"yyyy-MM-dd")+" 23:59:59";
        return strings;
    }

    @Test
    public void dateTest(){
        String[] strings = getDateFromWeek(2019,1);
        System.out.println(strings[0]+"----"+strings[1]);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(2018,12,31);
        System.out.println(calendar.getWeeksInWeekYear());
    }
}
