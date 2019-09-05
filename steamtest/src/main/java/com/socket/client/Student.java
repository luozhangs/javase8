package com.socket.client;

import com.alibaba.fastjson.JSON;
import com.test.EnumTest;

import java.io.Serializable;

/**
 * Created by zhang on 2018/6/22.
 */
public class Student implements Serializable{

    private Integer id;
    private String name;
    private String enumTest;

    public String getEnumTest() {
        return enumTest;
    }

    public void setEnumTest(EnumTest enumTest) {
        this.enumTest = JSON.toJSONString(enumTest);
    }

    private int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public Student(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Student() {
    }

    public Student(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
