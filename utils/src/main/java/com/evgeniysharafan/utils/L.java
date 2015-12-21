package com.evgeniysharafan.utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Logger
 */
@SuppressWarnings("unused")
public final class L {

    public static final int MAX_CHUNK_LENGTH = 2000;
    public static final int MAX_MESSAGE_LENGTH = 100000;

    private static String tag;
    private static String loggerClassName;

    private static boolean needWriteToFile = false;
    private static ExecutorService logFileExecutor;
    private static StringBuffer logFileBuilder;
    private static SimpleDateFormat logFileSessionNameFormatter;
    private static File logFile;
    private static SparseArray<String> logFileLevels;

    private L() {
    }

    public static void init() {
        tag = L.class.getSimpleName();
        loggerClassName = L.class.getName();

        // don't write in release build
        if (needWriteToFile) {
            needWriteToFile = Utils.isDebug();
        }

        if (needWriteToFile) {
            logFileExecutor = Executors.newSingleThreadExecutor();
            logFileBuilder = new StringBuffer();

            File dataDirPath = Utils.getApp().getExternalCacheDir();
            if (dataDirPath != null) {
                logFileSessionNameFormatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
                File dir = new File(dataDirPath.getParent() + "/Logs/");
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdir();
                }

                logFile = new File(dir, logFileSessionNameFormatter.format(System.currentTimeMillis()) + ".log");

                logFileLevels = new SparseArray<>(6);
                logFileLevels.put(Log.VERBOSE, "V");
                logFileLevels.put(Log.DEBUG, "D");
                logFileLevels.put(Log.INFO, "I");
                logFileLevels.put(Log.WARN, "W");
                logFileLevels.put(Log.ERROR, "E");
                logFileLevels.put(Log.ASSERT, "A");
            } else {
                needWriteToFile = false;
            }
        }

        log(Log.VERBOSE, "L logging is enabled, isDebug = " + Utils.isDebug() + ", needWriteToFile = " + needWriteToFile);
    }

    public static void v(int resId) {
        if (Utils.isDebug()) {
            v(Res.getString(resId));
        }
    }

    public static void d(int resId) {
        if (Utils.isDebug()) {
            d(Res.getString(resId));
        }
    }

    public static void i(int resId) {
        i(Res.getString(resId));
    }

    public static void w(int resId) {
        w(Res.getString(resId));
    }

    public static void e(int resId) {
        e(Res.getString(resId));
    }

    public static void wtf(int resId) {
        wtf(Res.getString(resId));
    }

    public static void v(String msg, Object... args) {
        if (Utils.isDebug()) {
            log(Log.VERBOSE, msg, args);
        }
    }

    public static void d(String msg, Object... args) {
        if (Utils.isDebug()) {
            log(Log.DEBUG, msg, args);
        }
    }

    public static void i(String msg, Object... args) {
        log(Log.INFO, msg, args);
    }

    public static void w(String msg, Object... args) {
        log(Log.WARN, msg, args);
    }

    public static void w(Throwable throwable) {
        log(Log.WARN, throwable);
    }

    public static void w(Throwable throwable, String msg, Object... args) {
        log(Log.WARN, msg, args);
        log(Log.WARN, throwable);
    }

    public static void e(String msg, Object... args) {
        log(Log.ERROR, msg, args);
    }

    public static void e(String msg, Throwable throwable) {
        log(Log.ERROR, msg);
        log(Log.ERROR, throwable);
    }

    public static void e(Throwable throwable) {
        log(Log.ERROR, throwable);
    }

    public static void wtf(String msg, Object... args) {
        log(Log.ASSERT, msg, args);
    }

    public static void wtf(Throwable msg) {
        log(Log.ASSERT, msg);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private static void log(int level, Object msg, Object... args) {
        if (msg == null) {
            log(Log.ERROR, "Message can not be null");
            return;
        }

        if (needWriteToFile) {
            logFileBuilder.append(logFileSessionNameFormatter.format(
                    System.currentTimeMillis())).append(": ").append(getLevel(level)).append(": ");
        }

        String location = getLocation();

        if (msg instanceof Throwable) {
            Throwable throwable = (Throwable) msg;
            String stackTraceStringWithLocation = location + Log.getStackTraceString(throwable);
            Log.println(level, tag, stackTraceStringWithLocation);

            if (needWriteToFile) {
                logFileBuilder.append(stackTraceStringWithLocation);
            }
        } else {
            String message = (String) msg;

            if (args.length > 0) {
                message = String.format(message, args);
            }

            if (message.length() > MAX_MESSAGE_LENGTH) {
                String chunkWithLocation = location + message.substring(0, MAX_CHUNK_LENGTH);
                String lengthWithLocation = location
                        + "!!! message length > MAX_MESSAGE_LENGTH !!!, MAX_MESSAGE_LENGTH == "
                        + MAX_MESSAGE_LENGTH;

                Log.println(level, tag, chunkWithLocation);
                Log.println(level, tag, lengthWithLocation);

                if (needWriteToFile) {
                    logFileBuilder.append(chunkWithLocation);
                    logFileBuilder.append(lengthWithLocation);
                }
            } else if (message.length() > MAX_CHUNK_LENGTH) {
                String chunkWithLocation = location + message.substring(0, MAX_CHUNK_LENGTH);
                Log.println(level, tag, chunkWithLocation);

                if (needWriteToFile) {
                    logFileBuilder.append(chunkWithLocation);
                }

                log(level, message.substring(MAX_CHUNK_LENGTH));
            } else {
                String messageWithLocation = location + message;
                Log.println(level, tag, messageWithLocation);

                if (needWriteToFile) {
                    logFileBuilder.append(messageWithLocation);
                }
            }
        }

        if (needWriteToFile) {
            logFileBuilder.append("\n");
            write(logFileBuilder.toString());
            logFileBuilder.delete(0, logFileBuilder.length());
        }
    }

    private static String getLevel(int level) {
        return logFileLevels.get(level);
    }

    private static String getLocation() {
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean startsWith;
        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                startsWith = trace.getClassName().startsWith(loggerClassName);
                if (found) {
                    if (!startsWith) {
                        return "[" + getClassName(Class.forName(trace.getClassName())) + "."
                                + trace.getMethodName() + "() : " + trace.getLineNumber() + "]: ";
                    }
                } else if (startsWith) {
                    found = true;
                }
            } catch (ClassNotFoundException e) {
                // no need, it`s not fatal
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            String simpleName = clazz.getSimpleName();
            if (!TextUtils.isEmpty(simpleName)) {
                return simpleName;
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    private static void write(final String str) {
        logFileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(logFile, true));
                    writer.write(str);
                } catch (Exception e) {
                    e(e);
                    needWriteToFile = false;
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        });
    }

    public static boolean needWriteToFile() {
        return needWriteToFile;
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void logIntent(Intent intent) {
        if (Utils.isDebug()) {
            if (intent == null) {
                d("intent == null");
                return;
            }

            StringBuilder sb = new StringBuilder();

            sb.append("component = " + intent.getComponent() + "\n");
            sb.append("action = " + intent.getAction() + "\n");
            sb.append("flags = " + intent.getFlags() + "\n");
            sb.append("data = " + intent.getData() + "\n");
            sb.append("type = " + intent.getType() + "\n");
            sb.append("categories = " + intent.getCategories() + "\n");
            sb.append("package = " + intent.getPackage() + "\n");

            Bundle extras = intent.getExtras();
            if (extras != null) {
                for (String key : extras.keySet()) {
                    Object entry = extras.get(key);
                    if (entry != null) {
                        sb.append("extra " + entry.getClass().getSimpleName() + " " + key + " = "
                                + entry + "\n");
                    }
                }
            } else {
                sb.append("extras = null");
            }

            d(sb.toString());
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    public static void logBundle(Bundle bundle) {
        if (Utils.isDebug()) {
            StringBuilder sb = new StringBuilder();

            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    Object entry = bundle.get(key);
                    if (entry != null) {
                        sb.append("extra " + entry.getClass().getSimpleName() + " " + key + " = "
                                + entry + "\n");
                    }
                }
            } else {
                sb.append("extras = null");
            }

            d(sb.toString());
        }
    }

}