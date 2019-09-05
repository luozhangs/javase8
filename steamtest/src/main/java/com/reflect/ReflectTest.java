package com.reflect;

import com.alibaba.fastjson.JSON;
import com.socket.client.Student;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhang on 2019/3/30.
 */
public class ReflectTest {

    @Test
    public void declareMethodTest() throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        try {
            Class clazz = Class.forName("com.socket.client.Student");
            Method method1 = clazz.getMethod("setId",Integer.class);
            Method method = clazz.getMethod("getAge",null);
            Student student = new Student();
            method1.invoke(student,10);
            System.out.println(method.invoke(student));
            System.out.println(student.getId());
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method2 : methods) {
                System.out.println(method2.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void faceTest(){
        Class c  = com.reflect.Student.class;
        Field[] fields = c.getDeclaredFields();
        for (Field field:fields){
            Excel excel = field.getAnnotation(Excel.class);
            System.out.println(field.getName());
            if(excel!=null){
                System.out.println(excel.value());
            }
        }

    }
}
