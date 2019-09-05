package com.reflect;

/**
 * Created by zhang on 2019/4/10.
 */
public class Student {

    @Excel("姓名")
    private String name;

    @Excel("性别")
    private String sex;

    @Excel("年龄")
    private int age;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
