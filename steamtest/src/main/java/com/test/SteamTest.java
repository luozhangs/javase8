package com.test;

import com.alibaba.fastjson.JSONObject;
import com.socket.client.Student;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by zhang on 2018/5/2.
 */
public class SteamTest {

    int a  = 10;
    @Test
    public void test1(){
        try {
            String contents = new String(Files.readAllBytes(Paths.get("C:\\Users\\zhang\\Desktop\\m_enum.sql")), StandardCharsets.UTF_8);
            System.out.printf(contents);
            List list = Arrays.asList(contents.split("\\R+"));
            long count = list.stream().filter(w->w.toString().length()>12).count();
//            Stream.of(contents.split("\\R+"));
            System.out.println(count);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2(){
        String arr[] = {"aaaasd","dasdasda"};
        System.out.println(Arrays.asList(arr));
        Student student = new Student();
        Student student1 = student;
        System.out.println(student==student1);
        List<Student> list = new ArrayList();
        list.forEach(n->n.setName(""));
        for (Student o:list){
            o.setName("");
        }
//        list.forEach(n -> System.out.println(n));
        System.out.println(list.stream().map(n->n.toString()).collect(Collectors.joining(",")));

    }

    @Test
    public void testList(){
        List list = new ArrayList();
        for (int i=0;i<10;i++){
            list.add(i);
        }

        Iterator it=list.iterator();
        while (it.hasNext()){
            if(it.next().equals(1)){
                it.remove();
            }
        }
        System.out.println(list.toString());
    }

    @Test
    public void str(){
        String inSql = "insert into mxx_user_disease_rel(member_id,disease_id,disease_title,`order`,create_time) values (#{member_id},#{disease_id},#{disease_title},#{order},now())";
        Pattern p = Pattern.compile("#\\{\\w+\\}");
        Matcher m = p.matcher(inSql);
        System.out.println(inSql.replaceAll("#\\{\\w+\\}","?"));
        while(m.find()){
            System.out.println(m.group());
        }
        int a=10;
        Class c = Integer.class;
        System.out.println(c.getTypeName());
    }
}
