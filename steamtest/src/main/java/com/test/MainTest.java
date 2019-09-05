package com.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created by zhang on 2019/1/9.
 */
public class MainTest {
    public static void main(String[] args) throws Triangle.NotTriangleException {
        Triangle t = new Triangle(0,2,5);
        System.out.println(t.getArea().toString());
        Integer[]  arr = new Integer[10];
        Scanner in = new Scanner(System.in);
        int i=0;
        while (i<10){
            arr[i] = in.nextInt();
            i++;
        }
        MainTest mainTest = new MainTest();
        mainTest.show(arr);
        System.out.println("=============");
        mainTest.show(mainTest.sort(arr,"desc"));
    }

    public void show(Integer[] d){
        for (Integer n:d){
            System.out.print(n+" ");
        }
    }

    public Integer[] sort(Integer[] d,String mode){
        if("asc".equals(mode)){
            Arrays.sort(d);
        }
        if("desc".equals(mode)){
            Arrays.sort(d,Collections.reverseOrder());
        }
        return d;
    }


}
