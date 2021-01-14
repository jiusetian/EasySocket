package com.easysocket.utils;
import android.util.Log;

import com.easysocket.EasySocket;


public class LogUtil {
    public static final String LOGTAG = "easysocket";
    public static boolean debugEnabled = EasySocket.getInstance().getDefOptions().isDebug();

    public LogUtil() {
    }

    private static String getDebugInfo() {
        Throwable stack = new Throwable().fillInStackTrace();
        StackTraceElement[] trace = stack.getStackTrace();
        int n = 2;
        return trace[n].getClassName() + " " + trace[n].getMethodName() + "()" + ":" + trace[n].getLineNumber() +
                " ";
    }

    private static String getLogInfoByArray(String[] infos) {
        StringBuilder sb = new StringBuilder();
        for (String info : infos) {
            sb.append(info);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static void i(String... s) {
        if (debugEnabled) {
            i(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
        }
    }

    public static void e(Throwable tr) {
        if (debugEnabled) {
            Log.e(LOGTAG, getDebugInfo() ,tr);
        }
    }

    public static void e(String... s) {
        if (debugEnabled) {
            e(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
        }
    }

    public static void d(String... s) {
        if (debugEnabled) {
            d(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
        }
    }

    public static void v(String... s) {
        if (debugEnabled) {
            v(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
        }
    }

    public static void w(String... s) {
        if (debugEnabled) {
            w(LOGTAG, getDebugInfo() + getLogInfoByArray(s));
        }
    }

    private static void i(String name, String log) {
        System.out.println(name + "：" + log);
    }

    private static void d(String name, String log) {
        System.out.println(name + "：" + log);
    }

    private static void v(String name, String log) {
        System.out.println(name + "：" + log);
    }

    private static void e(String name, String log) {
        System.err.println(name + "：" + log);
    }

    private static void w(String name, String log) {
        System.err.println(name + "：" + log);
    }

}
