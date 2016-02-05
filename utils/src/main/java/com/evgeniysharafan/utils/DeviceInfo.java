package com.evgeniysharafan.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "StringBufferReplaceableByString"})
public final class DeviceInfo {

    private static final Pattern IPV4_PATTERN = Pattern
            .compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private DeviceInfo() {
    }

    public static void sendDeviceInfoToEmail(Activity activity, @Nullable String... emails) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        if (emails != null) {
            intent.putExtra(Intent.EXTRA_EMAIL, emails);
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, getBaseSendSubject() + " device info");
        intent.putExtra(Intent.EXTRA_TEXT, getDeviceInfo());

        activity.startActivity(Intent.createChooser(intent, "Send device info through (Gmail is preferred)…"));
    }

    public static String getBaseSendSubject() {
        return new StringBuilder(70).append(DeviceInfo.getDeviceName())
                .append(" ")
                .append(Utils.getPackageName())
                .append(" ")
                .append(Utils.isDebug() ? "debug" : "release")
                .append(" ")
                .append(Utils.getVersionName())
                .append(" ")
                .append(Utils.getVersionCode())
                .toString();
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = " Android " + String.valueOf(Build.VERSION.RELEASE);
        if (model.startsWith(manufacturer)) {
            return model + version;
        } else {
            return manufacturer + " " + model + version;
        }
    }

    public static String getDeviceInfo() {
        return new StringBuilder(1500)
                .append("*** Device info start ***\n\n")

                .append("Configuration: ").append(Res.get().getConfiguration().toString()).append("\n")
                .append("Locale: ").append(Locale.getDefault().toString()).append("\n\n")

                .append("TimeZone: ").append(TimeZone.getDefault().getID()).append("\n")
                .append("Current date time: ").append(String.valueOf(Calendar.getInstance(
                        TimeZone.getDefault(), Locale.getDefault()).getTimeInMillis() / 1000)).append("\n\n")

                .append("Number of processors: ").append(Runtime.getRuntime().availableProcessors()).append("\n")
                .append("IPv4 address: ").append(getIpAddress(true)).append("\n")
                .append("IPv6 address: ").append(getIpAddress(false)).append("\n\n")

                .append(getMemoryInfo()).append("\n\n")

                .append("Network type: ").append(getNetworkType()).append("\n")
                .append("Has internet connection: ").append(Utils.hasInternetConnection()).append("\n\n")

                .append("Tablet: ").append(Utils.isTablet()).append("\n")
                .append("Can make calls: ").append(Utils.canMakeCalls()).append("\n")
                .append("Package name: ").append(Utils.getPackageName()).append("\n")
                .append("Version name: ").append(Utils.getVersionName()).append("\n")
                .append("Version code: ").append(Utils.getVersionCode()).append("\n")
                .append("Is debug: ").append(Utils.isDebug()).append("\n")
                .append("Is RTL: ").append(Utils.isRtl()).append("\n\n")

                .append("Build.BOARD: ").append(Build.BOARD).append("\n")
                .append("Build.BOOTLOADER: ").append(Build.BOOTLOADER).append("\n")
                .append("Build.BRAND: ").append(Build.BRAND).append("\n")
                .append("Build.DEVICE: ").append(Build.DEVICE).append("\n")
                .append("Build.DISPLAY: ").append(Build.DISPLAY).append("\n")
                .append("Build.FINGERPRINT: ").append(Build.FINGERPRINT).append("\n")
                .append("Build.getRadioVersion(): ").append(Build.getRadioVersion()).append("\n")
                .append("Build.HARDWARE: ").append(Build.HARDWARE).append("\n")
                .append("Build.ID: ").append(Build.ID).append("\n")
                .append("Build.MANUFACTURER: ").append(Build.MANUFACTURER).append("\n")
                .append("Build.MODEL: ").append(Build.MODEL).append("\n")
                .append("Build.PRODUCT: ").append(Build.PRODUCT).append("\n")
                .append("Build.SERIAL: ").append(Build.SERIAL).append("\n")
                .append("Build.TAGS: ").append(Build.TAGS).append("\n")
                .append("Build.TIME: ").append(Build.TIME).append("\n")
                .append("Build.TYPE: ").append(Build.TYPE).append("\n")
                .append("Build.USER: ").append(Build.USER).append("\n\n")

                .append("Build.VERSION.SDK_INT: ").append(Build.VERSION.SDK_INT).append("\n")
                .append("Build.VERSION.INCREMENTAL: ").append(Build.VERSION.INCREMENTAL).append("\n")
                .append("Build.VERSION.RELEASE: ").append(Build.VERSION.RELEASE).append("\n\n")

                .append("Res.getDisplayMetrics().widthPixels: ").append(Res.getDisplayMetrics().widthPixels).append("\n")
                .append("Res.getDisplayMetrics().heightPixels: ").append(Res.getDisplayMetrics().heightPixels).append("\n")
                .append("Res.getDisplayMetrics().density: ").append(Res.getDisplayMetrics().density).append("\n")
                .append("Res.getDisplayMetrics().densityDpi: ").append(Res.getDisplayMetrics().densityDpi).append("\n")
                .append("Res.getDisplayMetrics().scaledDensity: ").append(Res.getDisplayMetrics().scaledDensity).append("\n")
                .append("Res.getDisplayMetrics().xdpi: ").append(Res.getDisplayMetrics().xdpi).append("\n")
                .append("Res.getDisplayMetrics().ydpi: ").append(Res.getDisplayMetrics().ydpi).append("\n\n")

                .append("*** Device info end ***\n")

                .toString();
    }

    private static String getIpAddress(boolean useIpv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                List<InetAddress> inetAddresses = Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : inetAddresses) {
                    if (!inetAddress.isLoopbackAddress()) {
                        String address = inetAddress.getHostAddress().toUpperCase();
                        boolean isIPv4 = isIPv4Address(address);
                        if (useIpv4) {
                            if (isIPv4)
                                return address;
                        } else {
                            if (!isIPv4) {
                                int delimiter = address.indexOf('%'); // drop ip6 port
                                // suffix
                                return delimiter < 0 ? address : address.substring(0, delimiter);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            L.w(e);
        }

        return "";
    }

    private static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static String getMemoryInfo() {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) Utils.getApp()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);

            return new StringBuilder(100)
                    .append("Total RAM: ").append(Utils.hasJellyBean() ? mi.totalMem / 1048576L : 0).append(" MB \n")
                    .append("Available RAM: ").append(mi.availMem / 1048576L).append(" MB \n")
                    .append("Threshold RAM: ").append(mi.threshold / 1048576L).append(" MB \n")
                    .append("Low memory: ").append(mi.lowMemory)
                    .toString();
        } catch (Exception e) {
            L.w(e);
            return "";
        }
    }

    @SuppressWarnings("deprecation")
    private static String getNetworkType() {
        String networkStatus;

        ConnectivityManager connMgr = (ConnectivityManager) Utils.getApp()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isAvailable()) {
            networkStatus = "Wifi";
        } else if (mobile.isAvailable()) {
            networkStatus = getDataType();
        } else {
            networkStatus = "noNetwork";
        }

        return networkStatus;
    }

    private static String getDataType() {
        String type;

        TelephonyManager tm = (TelephonyManager) Utils.getApp().getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = tm.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                type = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                type = "4G";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                type = "GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                type = "2G";
                break;
            default:
                type = String.valueOf("Network type: " + networkType);
        }

        return type;
    }
}