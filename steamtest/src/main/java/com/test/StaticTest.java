package com.test;

/**
 * Created by zhang on 2018/12/27.
 */
public class StaticTest {

    public static void main(String[] args) {
        ceshi();
    }

    static StaticTest t = new StaticTest();
    static StaticTest t2 = new StaticTest();

    public int a = 10;

    public static int b = 20;

    static {
        System.out.println("1");
    }
    {
        System.out.println(2);
    }

    public static void ceshi(){
        System.out.println(b);
    }

    public StaticTest() {
        System.out.println("a: "+a);
        System.out.println("b: "+b);
    }
}
