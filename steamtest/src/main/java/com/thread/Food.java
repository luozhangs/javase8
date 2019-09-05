package com.thread;

/**
 * Created by zhang on 2018/12/14.
 */
public class Food {

    private String name;

    private Double price;

    private Boolean flag = true;//默认生产

    public Food(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public Food() {
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public  String getName() {
        //如果正在生产，不能消费，消费等待，否则释放消费的锁
     /*   if(flag){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        notify();*/
        return name;
    }

    public  void setName(String name) {
       /* if(!flag){//如果正在消费，不能生产，生产等待，否则释放锁，进行生产
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        notify();*/
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
