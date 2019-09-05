package com.mxx.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.maxx.base.util.HttpClientSend;
import org.junit.Test;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by zhang on 2018/7/27.
 */
public class Test1 {

   /* public static void main(String[] args)  {
        List<Student> list = new ArrayList<>();
        Student student = new Student("张三",20);
        Student student1 = new Student("李四",21);
        list.add(student);
        list.add(student1);
//        list.forEach(System.out::println);
        System.err.println("---------------------");
        List<List> lists =  new ArrayList<>();
       *//* lists = list.stream().map(n->{
            List<JSONObject> list1 = new ArrayList();
            JSON.parseObject(JSON.toJSONString(n)).forEach((k,v)->{
                JSONObject object = new JSONObject();
                object.put("name",k);
                object.put("value",v);
                list1.add(object);
            });
            return list1;
        }).collect(Collectors.toList());*//*

       *//* lists = list.stream().map(n->{
            JSONObject object = JSON.parseObject(JSON.toJSONString(n));
            return object.keySet().stream().map(k->JSON.parseObject("{\"name\":\""+k+"\",\"value\":\""+object.get(k)+"\"}")).collect(Collectors.toList());
        }).collect(Collectors.toList());
        System.out.println(JSON.toJSONString(lists));*//*
        *//*list.forEach(n->n.setAge(n.getAge()+1));
        list.forEach(n-> System.out.println(n));*//*
      *//*  long a = 10101010101010101l;
        long b = 10101010101010101l;
        System.out.println(a==b);
        System.out.println(a==10101010101010101l);*//*
        *//*String str = "I LOVE YOU";
        for(int i=0;i<str.length();i++){
            System.out.print((int)(str.charAt(i))+" ");
        }
        Pattern pattern = Pattern.compile("[abc]");
        Matcher matcher = pattern.matcher("hello abc");
        System.out.println(matcher.replaceAll("hello abc abc sdaa"));*//*

        *//*try {
            Class c = student.getClass();
            Field[] fields = c.getDeclaredFields();
            for (Field f:fields){
                f.setAccessible(true);
                System.out.println(f.getName());
                System.out.println(f.get(student));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*//*

        //创建 ObjectOutputStream 输出流
       *//* try {
            File file = new File("d:/file.txt");
            if(!file.exists()){
                file.createNewFile();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("d:/file.txt"));
            oos.writeObject(student);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("d:/file.txt"));
            Student stu = (Student) ois.readObject();
            System.out.println(stu.getName());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*//*
        JSONObject object = new JSONObject();
        String str = "{\"list\":[{\"id\":1},{\"id\":2}]}";
        object = JSON.parseObject(str);
        System.out.println(JSON.toJSONString(object));
        System.out.println(object.getString("list"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Long mis = ((calendar.getTimeInMillis()-System.currentTimeMillis())/1000);
        Student student2 = new Student();
        student2.setName("张三");
        student2.setAge(20);
        Optional.ofNullable(student2)
                .filter(n->n.getName().contains("张"))
                .ifPresent(n-> System.out.println(n.getAge()));
        Optional.ofNullable(list)
                .get().stream().findFirst().ifPresent(n-> System.out.println(n.getName()));
    }*/

    @Test
    public void MapTest(){
        Map<Object,String> map = new LinkedHashMap<>();
        map.put("b","1");
        map.put(3,"3");
        map.put("a","2");
        System.out.println("-------------");
        map.forEach((k,v)->{
            System.out.println(k+"--"+v);
        });
        StringBuffer a = new StringBuffer();
        StringBuffer b =  new StringBuffer();
        System.out.println(a==b);
//        b = a;
        System.out.println(a.hashCode());
        b = changeAB(a,b);
        System.out.println(a==b);
    }

    StringBuffer changeAB(StringBuffer a,StringBuffer b){
        System.out.println(a.hashCode());
        b=a;
        return b;
    }

    @Test
    public void arr(){
//        String[] ar = {"a","d"};
//        System.out.println(StringUtils.join(ar,","));
//        System.out.println(DateUtil.date2Str(DateUtil.str2Date("142301197301144110".substring(6,14),"yyyyMMdd"),"yyyy-MM-dd"));
//        List<JSONObject> error = new ArrayList<>();
//        error.add(JSON.parseObject("{\"序号\":\"164\",\"address\":\"西辛庄\",\"education\":\"\",\"salt\":\"ad8aad68c867c65f6c4a1a38deb4f61f\",\"sex\":\"男\",\"isValid\":0,\"description\":\"\",\"role_title\":\"\",\"title\":\"\",\"identity_card\":\"\",\"manage_diease\":\"糖尿病、高血压\",\"password\":\"1ymT8sljIK8NdozI+iEojA==\",\"center_name\":\"西辛庄中心卫生院\",\"phone\":\"15935822898\",\"skill\":\"\",\"loginName\":\"15935822898\",\"name\":\"张由爱\",\"id\":\"7bb5d2e7adce463a94b08ff984ab0a8a\",\"department\":\"\",\"email\":\"1216193783@qq.com\"}"));
//        error.add(JSON.parseObject("{\"\":\"\",\"序号\":\"278\",\"address\":\"梧桐\",\"education\":\"大专\",\"salt\":\"f2f9e8c4ea37fe7239449fd8b6492946\",\"sex\":\"男\",\"isValid\":0,\"description\":\"从医21年至今，擅长牙科和口腔颌面外科、口腔内科及修复专业的诊断治疗。\",\"role_title\":\"公卫医生\",\"title\":\"助理医师\",\"identity_card\":\"142324197906304118\",\"password\":\"WI8LyFa99NxVf5dN0GmJhQ==\",\"center_name\":\"梧桐中心卫生院\",\"phone\":\"13753815158\",\"skill\":\"擅长牙科和口腔颌面外科、口腔内科及修复专业的诊断治疗。\",\"loginName\":\"13753815158\",\"name\":\"郭伟\",\"id\":\"7fe5ca58892d446b9d7d97529f645fb7\",\"department\":\"公卫科\",\"email\":\"779001180@qq.com\"}"));
//        FileUtil.saveAsFileOutputStream("src//main/resources/data.json",JSON.toJSONString(error));
//
//        System.out.println(CipherUtil.generateKey());
        String str = URLEncoder.encode("http://rams.admin.test.aviptcare.com/h5/");
        System.out.println(str);
        String string = "糖尿吧$慢性病";
        String[] strings = string.split("[$]");
        System.out.println(strings.length);
    }

    @Test
    public void testFilter(){
        List<JSONObject> list = new ArrayList<>();
        JSONObject object = new JSONObject();
        object.put("name","张三");
        object.put("id","1");
        object.put("id2","1");
        JSONObject object2 = new JSONObject();
        object2.put("name","李四");
        object2.put("id","1");
        object2.put("id2","1");
        list.add(object);
        list.add(object2);
        Map<String,String> map = new HashMap<>();
        for (JSONObject o:list){
            o.forEach((k,v)->{
                System.out.println(k+"--"+v);
                if("1".equals(v)){
                    map.put(o.getString("name"),k);
                    return;
                }
            });
        }
        System.out.println(JSON.toJSONString(map));
    }

    @Test
    public void httpTest(){
        String url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/keyword?charset=UTF-8&access_token=24.74d09b3c9b8ae4a798ff51fed8233677.2592000.1560754719.282335-16283146";
        Map<String,String> head = new HashMap<>();
        head.put("contentType","application/json");
        StringBuffer requestBody = new StringBuffer();
        JSONObject object = new JSONObject();
        object.put("content","你好");
        object.put("title","测试");
        requestBody.append(JSON.toJSONString(object));
        String result = "";
        try {
            result = HttpClientSend.request("POST",url,head,requestBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result);
    }


    public static void main(String[] args) {
        new Thread(()->{
            System.out.println("-----------------thread_1--------------");
            new Thread(()->{
                while (true){
                    try {
                        Thread.sleep(1000);
                        System.out.println("-----------------thread_2--------------");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }).start();
    }

}
