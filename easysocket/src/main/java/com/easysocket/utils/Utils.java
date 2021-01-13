package com.easysocket.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Random;

/**
 * Created by LXR ON 2018/8/30.
 */
public class Utils {

    /**
     * 获取泛型参数的类型
     *
     * @param <T>
     * @return
     */
    public static <T> Type findGenericityType(Class<T> cls) {
        Type genType = cls.getGenericSuperclass(); //返回直接继承的父类（包含泛型参数）类型,如果有泛型T,也要包括进去
        //getActualTypeArguments 获取泛型中的实际类型，比如Map<Sting,String>中的String类型
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Type type = params[0]; //泛型的实际类型
        Type finalNeedType;
        if (type instanceof ParameterizedType) { //二级泛型，这里就处理最多二级吧，形如 A<B<T>>，两个<>
            finalNeedType = ((ParameterizedType) type).getActualTypeArguments()[0];
        } else { // 一级泛型，形如A<T>
            finalNeedType = type;
        }
        //如果泛型类型还是变量类型，比如T、V之类的，代表没有填写泛型参数
        if (finalNeedType instanceof TypeVariable) throw new IllegalStateException("没有填写泛型参数");
        return finalNeedType;
    }

    /**
     * 字符串是否为空
     *
     * @param str
     * @return
     */
    public static boolean isStringEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * 生成随机字符串
     *
     * @param length
     * @return
     */
    public static String getRandomChar(int length) {
        char[] chr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        Random random = new Random();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(chr[random.nextInt(36)]);
        }
        return buffer.toString();
    }


    /**
     * 获取handler对象
     *
     * @param isMainHandler 是否为主线程的handler，为false时返回的是当前线程handler
     * @return
     */
    public static Handler getHandler(boolean isMainHandler) {
        Handler handler;
        if (isMainHandler) {
            handler = new Handler(Looper.getMainLooper());
        } else {
            Looper.prepare();
            handler = new Handler();
        }
        return handler;
    }

    /**
     * 睡眠多少毫秒
     *
     * @param milliSecond 毫秒
     */
    public static void sleep(long milliSecond) {
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 非空检查
     *
     * @param object
     * @param emsg
     * @throws
     */
    public static void checkNotNull(Object object, String emsg) {
        try {
            if (object == null) {
                throw new Exception(emsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void throwNotNull(Object object, String emsg) throws Exception {
        if (object == null) {
            throw new Exception(emsg);
        }
    }

    // 判断是否连接网络
    public static boolean isNetConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 拼接两个byte[]
     *
     * @param
     * @param
     * @return
     */
    public static byte[] concatBytes(byte[] bt1, byte[] bt2) {
        if (bt1 == null) {
            return bt2;
        }
        if (bt2 == null) {
            return bt1;
        }
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

}
