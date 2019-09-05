package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by zhang on 2018/5/9.
 */
public class HasStatic {
    private static int x = 100;

    {
        System.out.println("aaaaaaaaaaa");
    }

    static {
        System.out.println("bbbbbbbbbbbb");
    }

    public void run(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Scanner scanner = new Scanner(System.in);
       /* try {
            System.out.println(br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
       while (true){
           System.out.println(scanner.nextLine());
       }

    }

    public static void main(String args[]) {

        HasStatic hs1 = new HasStatic();

        hs1.x++;
        System.out.println(hs1.x);
        HasStatic hs2 = new HasStatic();

        hs2.x++;
        System.out.println(hs2.x);
        hs1 = new HasStatic();

        hs1.x++;
        System.out.println(hs1.x);
        HasStatic.x--;
        System.out.println("x=" + x);

        x=10;

        x+=x-=x-x;// 相当于  x= x + (x-(x-x))

        new HasStatic();
        new HasStatic();
        HasStatic b = new HasStatic();
//        b.run();
//        System.out.println(x);
        float f= -1;
        System.out.println(2<<3);

    }

    public final static native int a();
}
