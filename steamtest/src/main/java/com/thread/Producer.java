package com.thread;

/**
 * 生产者
 * Created by zhang on 2018/12/14.
 */
public class Producer implements Runnable {

    private Food food;

    public Producer(Food food) {
        this.food = food;
    }

    /**
     * 生产者生产食物
     */
    @Override
    public void run() {
        int i = 0;
        while (true){
            food.setName("西红柿"+i);
            i++;
//            food.setFlag(false);
            System.out.println("厨师做的食物是：->"+food.getName());

            try {
                //延时一秒
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
