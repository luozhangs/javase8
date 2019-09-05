package com.thread;

/**
 * Created by zhang on 2018/12/14.
 */
public class Customer implements Runnable {
    private Food food;

    public Customer(Food food) {
        this.food = food;
    }

    /**
     * 消费者消费食物
     */
    @Override
    public void run() {
        while (true){
            System.out.println("顾客吃的食物是：->"+food.getName());
//            food.setFlag(true);
            try {
                //延时一秒
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
