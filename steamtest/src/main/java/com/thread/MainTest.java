package com.thread;

/**
 * Created by zhang on 2018/12/14.
 */
public class MainTest {

    public static void main(String[] args) {
        Food food = new Food();
        new Thread(new Producer(food)).start();
        new Thread(new Customer(food)).start();
    }
}
