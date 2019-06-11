package com.easysocket.utils;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Random;

/**
 * Created by LXR ON 2018/8/30.
 */
public class EUtil {

    /**
     * 普通类反射获取泛型方式，获取需要实际解析的类型
     *
     * @param <T>
     * @return
     */
    public static <T> Type findNeedClass(Class<T> cls) {
        //以下代码是通过泛型解析实际参数,泛型必须传
        Type genType = cls.getGenericSuperclass(); //返回直接继承的父类（包含泛型参数）,如果有泛型T,也要包括进去
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments(); //获取泛型中的实际类型
        Type type = params[0];
        Type finalNeedType;
        if (params.length > 1) { //这个类似是：CacheResult<SkinTestResult> 2层
            if (!(type instanceof ParameterizedType)) throw new IllegalStateException("没有填写泛型参数");
            finalNeedType = ((ParameterizedType) type).getActualTypeArguments()[0];
            //Type rawType = ((ParameterizedType) type).getRawType();
        } else { //这个类似是:SkinTestResult  1层
            finalNeedType = type;
        }
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
     * @param isMainHandler 是否为主线程的handler，为false时返回的是当前线程handler
     * @return
     */
    public static Handler getHandler(boolean isMainHandler){
        Handler handler;
        if (isMainHandler){
            handler=new Handler(Looper.getMainLooper());
        }else {
            Looper.prepare();
            handler=new Handler();
        }
        return handler;
    }

    /**
     * 睡眠多少毫秒
     * @param milliSecond 毫秒
     */
    public static void sleep(long milliSecond){
        try {
            Thread.sleep(milliSecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
