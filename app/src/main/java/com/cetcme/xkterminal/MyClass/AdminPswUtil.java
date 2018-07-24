package com.cetcme.xkterminal.MyClass;

import java.security.MessageDigest;
import java.util.Scanner;

/**
 * v1.0
 * 2018-07-12 09:05
 *
 * getCode : 生成4位码
 * getAdminPsw : 根据4位码 生成6位管理密码
 *
 * by qiuhong
 */
public class AdminPswUtil {

    public static String getCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append((int) (Math.random() * 10));
        }
        return code.toString();
    }

    public static String getAdminPsw(String code) {
        String md5 = MD5(code + "cetcme");
        if (md5 != null && md5.length() > 6) {
            return md5.substring(0, 6);
        } else {
            return "000000";
        }
    }

    private static String MD5(String key) {
        char hexDigits[] = {
            '3', '1', '8', '0', '4', '8', '6', '5', '2', '1', '1', '3', '4', '7', '9', '0'
        };
        try {
            byte[] btInput = key.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("请输入code：");
            String code = sc.nextLine();
            String adminPsw = getAdminPsw(code);
            System.out.println("管理密码: " + adminPsw);
        }
    }
}
