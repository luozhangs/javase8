package com.test;

/**
 * Created by zhang on 2018/12/13.
 */
public enum EnumTest {
    ZHANGSAN("张三",1l),LISI("里斯",2l);


    private String name;
    private Long code;

    EnumTest(String name, Long code) {
        this.name = name;
        this.code = code;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }
}
