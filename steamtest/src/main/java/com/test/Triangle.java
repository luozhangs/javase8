package com.test;

/**
 * Created by zhang on 2019/1/9.
 */
public class Triangle {

    private int a;
    private int b;
    private int c;

    public Triangle(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Double getArea() throws NotTriangleException{
        Double area = 0d;
        if(a!=0&&b!=0&&c!=0){
            if((a+b)>c&&(a+c)>b&&(c+b)>a){
                Double p = (a+b+c)/2d;
                area = Math.sqrt((p-a)*(p-b)*(p-c)*p);
            }else{
                throw new NotTriangleException("不能构成三角形",new Throwable("两边之和小于第三边"),500);
            }

        }else{
            throw new NotTriangleException("不能构成三角形",new Throwable("边长存在为0"),500);
        }
        return area;
    }
    class NotTriangleException extends Exception {


        public NotTriangleException() {
            super();
        }

        public NotTriangleException(String message, int value) {
            super(message);
        }

        public NotTriangleException(String message, Throwable cause, int value) {
            super(message, cause);
        }

    }
}
