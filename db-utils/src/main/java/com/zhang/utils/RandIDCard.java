package com.zhang.utils;

import java.util.*;

/**
 * Created by zhang on 2019/2/27.
 */
public class RandIDCard {
    /**
     * 随机生成身份证号
     *
     * @return
     */
    private static String makeidCardNumber() {
        // 身份证号是啥？户籍所在地（第1到第6位） + 出生日期（第7到第14位） + 落户派出所代码（第15、16位） + 性别代码（第17位） + 验证码（第18位）
        // 户籍所在地(以北京为例)
        Map<String, Integer> registerLocation = new HashMap<>();
        registerLocation.put("北京市", 110000);
        registerLocation.put("市辖区", 110100);
        registerLocation.put("东城区", 110101);
        registerLocation.put("西城区", 110102);
        registerLocation.put("崇文区", 110103);
        registerLocation.put("宣武区", 110104);
        registerLocation.put("朝阳区", 110105);
        registerLocation.put("丰台区", 110106);
        registerLocation.put("石景山区", 110107);
        registerLocation.put("海淀区", 110108);
        registerLocation.put("门头沟区", 110109);
        registerLocation.put("房山区", 110111);
        registerLocation.put("通州区", 110112);
        registerLocation.put("顺义区", 110113);
        registerLocation.put("昌平区", 110114);
        registerLocation.put("大兴区", 110115);
        registerLocation.put("怀柔区", 110116);
        registerLocation.put("平谷区", 110117);
        registerLocation.put("县", 110200);
        registerLocation.put("密云县", 110228);
        registerLocation.put("延庆县", 110229);
        registerLocation.put("天津市", 120000);
        registerLocation.put("市辖区", 120100);
        registerLocation.put("和平区", 120101);
        registerLocation.put("河东区", 120102);
        registerLocation.put("河西区", 120103);
        registerLocation.put("南开区", 120104);
        registerLocation.put("河北区", 120105);
        registerLocation.put("红桥区", 120106);
        registerLocation.put("东丽区", 120110);
        registerLocation.put("西青区", 120111);
        registerLocation.put("津南区", 120112);
        registerLocation.put("北辰区", 120113);
        registerLocation.put("武清区", 120114);
        registerLocation.put("宝坻区", 120115);
        registerLocation.put("县", 120200);
        registerLocation.put("宁河县", 120221);
        registerLocation.put("静海县", 120223);
        registerLocation.put("蓟　县", 120225);

        StringBuffer strBuffer = new StringBuffer();
        // 区号
        strBuffer.append(randomLocationCode(registerLocation));
        // 身份证号
        strBuffer.append(randomBirthday());
        // 15、16、17位
        strBuffer.append(randomCode());
        // 利用前十七位获取第十八位
        String eighteenth = verificationCode(strBuffer.toString());
        strBuffer.append(eighteenth);
        return strBuffer.toString();
    }

    /**
     * 随机获取区号
     * @param registerLocation
     * @return
     */
    public static String randomLocationCode(Map<String, Integer> registerLocation) {
        int index = (int) (Math.random() * registerLocation.size());
        Collection<Integer> values = registerLocation.values();
        Iterator<Integer> it = values.iterator();
        int i = 0;
        int locationCode = 0;
        while (i <= index && it.hasNext()) {
            i++;
            if (i == index) {
                locationCode = it.next();
            }
        }
        return String.valueOf(locationCode);
    }

    /**
     * 随机生成出生日期
     *
     * @return
     */
    public static String randomBirthday() {
        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.YEAR, (int) (Math.random() * 60) + 1950);
        birthday.set(Calendar.MONTH, (int) (Math.random() * 12));
        birthday.set(Calendar.DATE, (int) (Math.random() * 31));

        StringBuilder builder = new StringBuilder();
        builder.append(birthday.get(Calendar.YEAR));
        long month = birthday.get(Calendar.MONTH) + 1;
        if (month < 10) {
            builder.append("0");
        }
        builder.append(month);
        long date = birthday.get(Calendar.DATE);
        if (date < 10) {
            builder.append("0");
        }
        builder.append(date);
        return builder.toString();
    }
    /**
     * 随机获取落户派出所代码（第15、16位） + 性别代码（第17位）
     * 直接生成三位数
     * @return
     */
    public static String randomCode() {
        int code = (int) (Math.random() * 1000);
        if (code < 10) {
            return "00" + code;
        } else if (code < 100) {
            return "0" + code;
        } else {
            return "" + code;
        }
    }
    /**
     * 生成第18位身份证号
     * @param
     * @return
     * 身份证校验码的计算方法
     * 将前面的身份证号码17位数分别乘以不同的系数。从第一位到第十七位的系数分别为：7－9－10－5－8－4－2－1－6－3－7－9－10－5－8－4－2。
     * 将这17位数字和系数相乘的结果相加。
     * 用加出来和除以11，看余数是多少？
     * 余数只可能有0－1－2－3－4－5－6－7－8－9－10这11个数字。
     * 其分别对应的最后一位身份证的号码为1－0－X －9－8－7－6－5－4－3－2。
     */
    public static String verificationCode(String str17) {
        char[] chars = str17.toCharArray();
        if (chars.length < 17) {
            return " ";
        }
        // 前十七位分别对应的系数
        int[] coefficient = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
        // 最后应该取得的第十八位的验证码
        char[] resultChar = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
        int[] numberArr = new int[17];
        int result = 0;
        for (int i = 0; i < numberArr.length; i++) {
            numberArr[i] = Integer.parseInt(chars[i] + "");
        }
        for (int i = 0; i < numberArr.length; i++) {
            result += coefficient[i] * numberArr[i];
        }
        return String.valueOf(resultChar[result % 11]);
    }

    public static void main(String[] args) {
        System.out.println(makeidCardNumber());
    }

}
