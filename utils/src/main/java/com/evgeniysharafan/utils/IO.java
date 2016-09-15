package com.evgeniysharafan.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

/**
 * IO utils
 */
@SuppressWarnings("unused")
public final class IO {

    private IO() {
    }

    public static boolean isMediaStorageMounted() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * @return free space in bytes. 0 if storage is unmounted.
     */
    public static long getFreeExternalSpace() {
        if (!isMediaStorageMounted()) {
            L.w("Media storage is unmounted");
            return 0;
        }

        return getFreeSpace(Environment.getExternalStorageDirectory());
    }

    /**
     * @return free space in bytes
     */
    public static long getFreeInternalSpace() {
        return getFreeSpace(Environment.getDataDirectory());
    }

    /**
     * @return free space in bytes
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long getFreeSpace(File file) {
        if (!checkFileNotNull(file)) {
            return 0;
        }

        StatFs stat = new StatFs(file.getPath());
        long blockSize = Utils.hasJellyBeanMr2() ? stat.getBlockSizeLong() : stat.getBlockSize();
        long availableBlocks = Utils.hasJellyBeanMr2() ? stat.getAvailableBlocksLong() : stat.getAvailableBlocks();

        return availableBlocks * blockSize;
    }

    public static long getApkSize() {
        long apkSize;

        File apk = new File(Utils.getApp().getPackageCodePath());
        apkSize = apk.length();

        return apkSize;
    }

    /**
     * Formats a content size to be in the form of bytes, kilobytes, megabytes, etc.
     * For example 7146242048 bytes converts to 6.66 GB
     */
    public static String getFormattedFileSize(long sizeInBytes) {
        return Formatter.formatFileSize(Utils.getApp(), sizeInBytes);
    }

    /**
     * Like {@link #getFormattedFileSize}, but trying to generate shorter numbers
     * (showing fewer digits of precision).
     * For example 7146242048 bytes converts to 6.7 GB
     */
    public static String getFormattedShortFileSize(long sizeInBytes) {
        return Formatter.formatShortFileSize(Utils.getApp(), sizeInBytes);
    }

    public static int getFilesCountInDir(File dir) {
        if (!checkDirExists(dir)) {
            return 0;
        }

        File[] filesInDir = dir.listFiles();
        if (!checkFilesArrayNotNull(filesInDir)) {
            return 0;
        }

        return filesInDir.length;
    }

    public static long getDirSize(File dir) {
        if (!checkDirExists(dir)) {
            return 0;
        }

        File[] filesInDir = dir.listFiles();
        if (!checkFilesArrayNotNull(filesInDir)) {
            return 0;
        }

        long totalDirSize = 0;
        for (File f : filesInDir) {
            if (f.isDirectory()) {
                totalDirSize += getDirSize(f);
            } else {
                totalDirSize += f.length();
            }
        }

        return totalDirSize;
    }

    /**
     * Deletes dir and files and dirs inside.
     *
     * @return {@code true} if this dir was deleted, {@code false} otherwise.
     */
    public static boolean deleteDir(String pathToDir) {
        return checkPathNotEmpty(pathToDir) && deleteDir(new File(pathToDir));
    }

    /**
     * Deletes dir and all files and dirs inside.
     *
     * @return {@code true} if this dir was deleted, {@code false} otherwise.
     */
    public static boolean deleteDir(File dir) {
        if (!checkDirExists(dir)) {
            return false;
        }

        deleteFilesInDir(dir, true);
        return deleteFile(dir);
    }

    /**
     * Deletes all files in dir.
     *
     * @param deleteDirsInside delete dirs inside
     */
    public static void deleteFilesInDir(File dir, boolean deleteDirsInside) {
        if (!checkDirExists(dir)) {
            return;
        }

        File[] filesInDir = dir.listFiles();
        if (!checkFilesArrayNotNull(filesInDir)) {
            return;
        }

        for (File file : filesInDir) {
            if (file.isDirectory()) {
                if (deleteDirsInside) {
                    deleteDir(file);
                }
            } else {
                //noinspection ResultOfMethodCallIgnored
                deleteFile(file);
            }
        }
    }

    /**
     * Sorts all files in the dir by last modified and deletes the oldest ones. Remains remainLatestFilesCount.
     * It's useful if you need to shrink your cache.
     */
    public static void deleteFilesInDirRemainCount(File dir, int remainLatestFilesCount, boolean deleteDirsInside) {
        if (!checkDirExists(dir)) {
            return;
        }

        File[] filesInDir = dir.listFiles();
        if (!checkFilesArrayNotNull(filesInDir)) {
            return;
        }

        if (filesInDir.length > remainLatestFilesCount) {
            sortByLastModified(filesInDir);

            int filesForDeletionLength = filesInDir.length - remainLatestFilesCount;
            File[] filesForDeletion = new File[filesForDeletionLength];

            System.arraycopy(filesInDir, 0, filesForDeletion, 0, filesForDeletionLength);
            deleteFiles(filesForDeletion, deleteDirsInside);
        }
    }

    /**
     * Sorts all files in the dir by last modified and deletes the oldest ones.
     * Remains remainFilesForDays.
     * It's useful if you need to shrink your cache.
     */
    public static void deleteFilesInDirRemainDays(File dir, int remainFilesForDays, boolean deleteDirsInside) {
        deleteFilesInDirBeforeDate(dir, System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS * remainFilesForDays,
                deleteDirsInside);
    }

    /**
     * Sorts all files in the dir by last modified and deletes the oldest ones.
     * Deletes files last modified beforeDateInMillis.
     * It's useful if you need to shrink your cache.
     */
    public static void deleteFilesInDirBeforeDate(File dir, long beforeDateInMillis, boolean deleteDirsInside) {
        if (!checkDirExists(dir)) {
            return;
        }

        File[] filesInDir = dir.listFiles();
        if (!checkFilesArrayNotNull(filesInDir)) {
            return;
        }

        sortByLastModified(filesInDir);

        int filesForDeletionLength = 0;
        for (File file : filesInDir) {
            if (file.lastModified() < beforeDateInMillis) {
                filesForDeletionLength++;
            }
        }

        if (filesForDeletionLength > 0) {
            File[] filesForDeletion = new File[filesForDeletionLength];
            System.arraycopy(filesInDir, 0, filesForDeletion, 0, filesForDeletionLength);
            deleteFiles(filesForDeletion, deleteDirsInside);
        }
    }

    /**
     * The oldest files will be at the beginning of the array
     */
    public static void sortByLastModified(File[] files) {
        if (!checkFilesArrayNotNull(files)) {
            return;
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.lastModified() > rhs.lastModified() ? 1
                        : (lhs.lastModified() < rhs.lastModified() ? -1 : 0);
            }
        });
    }

    public static boolean deleteFile(String path) {
        return checkPathNotEmpty(path) && deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        if (!checkFileNotNull(file)) {
            return false;
        }

        boolean deleted = file.delete();
        if (!deleted) {
            L.i("failed to delete " + file);
        }

        return deleted;
    }

    /**
     * Deletes all files in dir.
     *
     * @param deleteDirsInside delete dirs inside
     */
    public static void deleteFiles(File[] files, boolean deleteDirsInside) {
        if (!checkFilesArrayNotNull(files)) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                if (deleteDirsInside) {
                    deleteDir(file);
                }
            } else {
                //noinspection ResultOfMethodCallIgnored
                deleteFile(file);
            }
        }
    }

    public static void copyFile(String srcPath, String targetPath) throws IOException {
        if (checkPathNotEmpty(srcPath) && checkPathNotEmpty(targetPath)) {
            copyFile(new File(srcPath), new File(targetPath));
        }
    }

    public static void copyFile(File srcFile, File targetFile) throws IOException {
        if (checkFileNotNull(srcFile) && checkFileNotNull(targetFile)) {
            FileChannel inChannel = new FileInputStream(srcFile).getChannel();
            FileChannel outChannel = new FileOutputStream(targetFile).getChannel();

            //noinspection TryFinallyCanBeTryWithResources
            try {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            } finally {
                closeQuietly(inChannel);
                closeQuietly(outChannel);
            }
        }
    }

    public static void writeFile(String data, File file) throws IOException {
        if (!Utils.isEmpty(data) && checkFileNotNull(file)) {
            writeFile(data.getBytes(Charset.forName("UTF-8")), file);
        }
    }

    public static void writeFile(byte[] data, File file) throws IOException {
        if (data == null || !checkFileNotNull(file)) {
            return;
        }

        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            bos.write(data);
        } finally {
            closeQuietly(bos);
        }
    }

    public static String getAppExternalDataDirectory() {
        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory())
                .append(File.separator)
                .append("Android")
                .append(File.separator)
                .append("data")
                .append(File.separator)
                .append(Utils.getPackageName())
                .append(File.separator);

        return sb.toString();
    }

    public static String readFileAsString(String pathToFile) throws IOException {
        if (!checkPathNotEmpty(pathToFile)) {
            return "";
        }

        return readFileAsString(new File(pathToFile));
    }

    public static String readFileAsString(File file) throws IOException {
        if (!checkFileNotNull(file) || file.isDirectory()) {
            return "";
        }

        return readStreamAsString(new FileInputStream(file));
    }

    public static String readStreamAsString(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        BufferedReader buf = null;

        try {
            buf = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = buf.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            closeQuietly(buf);
        }

        return sb.toString();
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];

        int n;
        while (-1 != (n = input.read(buffer))) {
            byteArrayOutputStream.write(buffer, 0, n);
        }

        return byteArrayOutputStream.toByteArray();
    }

    public static void close(@Nullable Closeable closeable, boolean swallowIOException) throws IOException {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            if (swallowIOException) {
                L.w(e, "IOException thrown while closing Closeable.");
            } else {
                throw e;
            }
        }
    }

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            close(closeable, true);
        } catch (IOException e) {
            L.w(e, "IOException should not have been thrown.");
        }
    }

    public static void createNomediaFileIfNeeded(File dir) {
        if (!checkDirExists(dir)) {
            return;
        }

        File nomediaFile = new File(dir, ".nomedia");
        L.d("path to nomediaFile = " + nomediaFile.getPath());

        if (!nomediaFile.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                nomediaFile.createNewFile();
            } catch (IOException e) {
                L.e(e);
            }
        }
    }

    public static boolean checkPathNotEmpty(String path) {
        if (Utils.isEmpty(path)) {
            L.e("Utils.isEmpty(path)");
            return false;
        }

        return true;
    }

    public static boolean checkFileNotNull(File file) {
        if (file == null) {
            L.e("file == null");
            return false;
        }

        return true;
    }

    public static boolean checkFilesArrayNotNull(File[] files) {
        if (files == null) {
            L.e("files == null");
            return false;
        }

        return true;
    }

    public static boolean checkDirExists(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            L.e("dir == null || !dir.exists() || !dir.isDirectory()");
            return false;
        }

        return true;
    }

    @Nullable
    public static String readStreamAsString(@NonNull InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = r.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            L.e(e);
        }
        return stringBuilder.toString();
    }

}
