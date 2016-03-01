package com.evgeniysharafan.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.evgeniysharafan.utils.Res.getString;

/**
 * Logger
 */
@SuppressWarnings({"unused", "StringBufferReplaceableByString", "StringConcatenationInsideStringBufferAppend"})
public final class L {

    private static final int MAX_CHUNK_LENGTH = 2000;
    private static final int MAX_MESSAGE_LENGTH = 100000;
    private static final String LOGS_FILE_PROVIDER_SUFFIX = ".logsfileprovider";

    private static String tag;
    private static String loggerClassName;

    private static boolean needWriteToFile;
    private static ExecutorService logFileExecutor;
    private static StringBuffer logFileBuilder;
    private static SimpleDateFormat logFileSessionNameFormatter;
    private static File logFile;
    private static SparseArray<String> logFileLevels;

    private L() {
    }

    static {
        tag = L.class.getSimpleName();
        loggerClassName = L.class.getName();

        log(Log.VERBOSE, "L logging is enabled, isDebug = " + Utils.isDebug());
    }

    public static void v(int resId) {
        if (Utils.isDebug()) {
            v(getString(resId));
        }
    }

    public static void d(int resId) {
        if (Utils.isDebug()) {
            d(getString(resId));
        }
    }

    public static void i(int resId) {
        i(getString(resId));
    }

    public static void w(int resId) {
        w(getString(resId));
    }

    public static void e(int resId) {
        e(getString(resId));
    }

    public static void wtf(int resId) {
        wtf(getString(resId));
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

    public static void e(Throwable throwable) {
        log(Log.ERROR, throwable);
    }

    public static void e(Throwable throwable, String msg, Object... args) {
        log(Log.ERROR, msg, args);
        log(Log.ERROR, throwable);
    }

    public static void wtf(String msg, Object... args) {
        log(Log.ASSERT, msg, args);
    }

    public static void wtf(Throwable msg) {
        log(Log.ASSERT, msg);
    }

    private static void log(int level, Object msg, Object... args) {
        if (msg == null) {
            log(Log.ERROR, "Message can not be null");
            return;
        }

        if (isNeedWriteToFile()) {
            logFileBuilder.append(logFileSessionNameFormatter.format(
                    System.currentTimeMillis())).append(": ").append(getLevel(level)).append(": ");
        }

        String location = getLocation();

        if (msg instanceof Throwable) {
            Throwable throwable = (Throwable) msg;
            String stackTraceStringWithLocation = location + Log.getStackTraceString(throwable);
            Log.println(level, tag, stackTraceStringWithLocation);

            if (isNeedWriteToFile()) {
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

                if (isNeedWriteToFile()) {
                    logFileBuilder.append(chunkWithLocation);
                    logFileBuilder.append(lengthWithLocation);
                }
            } else if (message.length() > MAX_CHUNK_LENGTH) {
                String chunkWithLocation = location + message.substring(0, MAX_CHUNK_LENGTH);
                Log.println(level, tag, chunkWithLocation);

                if (isNeedWriteToFile()) {
                    logFileBuilder.append(chunkWithLocation);
                }

                log(level, message.substring(MAX_CHUNK_LENGTH));
            } else {
                String messageWithLocation = location + message;
                Log.println(level, tag, messageWithLocation);

                if (isNeedWriteToFile()) {
                    logFileBuilder.append(messageWithLocation);
                }
            }
        }

        if (isNeedWriteToFile()) {
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

    public static boolean isNeedWriteToFile() {
        return needWriteToFile;
    }

    public static void setNeedWriteToFile(boolean needWrite, boolean writeInRelease) {
        needWriteToFile = needWrite && (writeInRelease || Utils.isDebug());
        if (needWriteToFile) {
            initWriteToFile();
        }
    }

    private static void initWriteToFile() {
        logFileExecutor = Executors.newSingleThreadExecutor();
        logFileBuilder = new StringBuffer();

        File logsDir = getLogsDir();
        if (logsDir != null) {
            logFileSessionNameFormatter = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
            logFile = new File(logsDir, logFileSessionNameFormatter.format(System.currentTimeMillis()) + ".txt");

            logFileLevels = new SparseArray<>(6);
            logFileLevels.put(Log.VERBOSE, "V");
            logFileLevels.put(Log.DEBUG, "D");
            logFileLevels.put(Log.INFO, "I");
            logFileLevels.put(Log.WARN, "W");
            logFileLevels.put(Log.ERROR, "E");
            logFileLevels.put(Log.ASSERT, "A");

            log(Log.VERBOSE, DeviceInfo.getDeviceInfo());
        } else {
            setNeedWriteToFile(false, false);
            e("Can't log to file, logsDir == null");
        }
    }

    @Nullable
    private static File getLogsDir() {
        File dataDirPath = Utils.getApp().getExternalFilesDir(null);
        if (dataDirPath != null) {
            File dir = new File(dataDirPath + "/Logs/");
            if (dir.exists()) {
                return dir;
            } else {
                return dir.mkdir() ? dir : null;
            }
        }

        return null;
    }

    private static void write(final String str) {
        logFileExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (isNeedWriteToFile()) {
                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(logFile, true));
                        writer.write(str);
                    } catch (Exception e) {
                        setNeedWriteToFile(false, false);
                        e(e);
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
            }
        });
    }

    public static void sendLogsToEmail(Activity activity, @Nullable String... emails) {
        setNeedWriteToFile(false, false);

        File logsDir = getLogsDir();
        if (logsDir != null) {
            ArrayList<Uri> uris = new ArrayList<>();
            String fileProviderAuthority = Utils.getPackageName() + LOGS_FILE_PROVIDER_SUFFIX;

            for (File file : logsDir.listFiles()) {
                Uri uri = FileProvider.getUriForFile(Utils.getApp(), fileProviderAuthority, file);
                uris.add(uri);
            }

            if (!uris.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("text/plain");

                if (emails != null) {
                    intent.putExtra(Intent.EXTRA_EMAIL, emails);
                }
                intent.putExtra(Intent.EXTRA_SUBJECT, DeviceInfo.getBaseSendSubject() + " logs");
                intent.putExtra(Intent.EXTRA_TEXT, "Describe the issue here please");
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                activity.startActivity(Intent.createChooser(intent, "Send logs through (Gmail is preferred)…"));
            } else {
                i("Can't send logs, logsDir is empty");
                Toasts.showLong("Can't send logs, logs directory is empty");
            }
        } else {
            e("Can't send logs, logsDir == null");
            Toasts.showLong("Can't send logs, logs directory doesn't exist or unavailable");
        }
    }

    public static void clearLogsDirectory() {
        setNeedWriteToFile(false, false);

        File logsDir = getLogsDir();
        if (logsDir != null) {
            deleteFiles(logsDir.listFiles());
        } else {
            e("Can't clear logs, logsDir == null");
            Toasts.showLong("Can't clear logs, logs directory doesn't exist or unavailable");
        }
    }

    private static void deleteFiles(File... files) {
        if (files != null) {
            for (File file : files) {
                // we don't create directories here, so we don't delete them.
                if (!file.isDirectory()) {
                    if (!file.delete()) {
                        e("Can't delete " + file.getName() + " file");
                        Toasts.showLong("Can't delete " + file.getName() + " file");
                    }
                }
            }
        }
    }

    public static int getLogsQuantity() {
        int quantity = 0;

        File logsDir = getLogsDir();
        if (logsDir != null) {
            quantity = logsDir.listFiles().length;
        }

        return quantity;
    }

}