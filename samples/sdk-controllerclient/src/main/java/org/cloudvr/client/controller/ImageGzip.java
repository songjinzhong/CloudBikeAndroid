package org.cloudvr.client.controller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by songjinzhong on 2017/5/15.
 */

public class ImageGzip {
    // 压缩
//    public static String compress(String str) throws IOException {
//        if (str == null || str.length() == 0) {
//            return str;
//        }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        GZIPOutputStream gzip = new GZIPOutputStream(out);
//        gzip.write(str.getBytes());
//        gzip.close();
//        return out.toString("ISO-8859-1");
//    }
//
//    // 解压缩
//    public static String uncompress(String str) throws IOException {
//        if (str == null || str.length() == 0) {
//            return str;
//        }
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        ByteArrayInputStream in = new ByteArrayInputStream(str
//                .getBytes("ISO-8859-1"));
//        GZIPInputStream gunzip = new GZIPInputStream(in);
//        byte[] buffer = new byte[256];
//        int n;
//        while ((n = gunzip.read(buffer))>= 0) {
//            out.write(buffer, 0, n);
//        }
//        // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
//        return out.toString();
//    }

    public static byte[] compress(byte[] b) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(b);
        gzip.close();
        return out.toByteArray();
    }

    public static byte[] uncompress(byte[] b) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(b);
        GZIPInputStream ungzip = new GZIPInputStream(in);
        byte[] buffer = new byte[1024];
        int n;
        while ((n = ungzip.read(buffer))>= 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
//    // 测试方法
//    public static void main(String[] args) throws IOException {
//
//        //测试字符串
//        String str="abcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefgabcdefg";
//
//        System.out.println("原长度："+str.length());
//
//        System.out.println("压缩后："+ImageGzip.compress(str).length());
//
//        System.out.println("压缩后："+ImageGzip.compress(str));
//
//        System.out.println("解压缩："+ImageGzip.uncompress(ImageGzip.compress(str)));
//    }
}
