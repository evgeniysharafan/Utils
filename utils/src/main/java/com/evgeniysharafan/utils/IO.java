package com.evgeniysharafan.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        long freeSpace = 0;

        if (isMediaStorageMounted()) {
            freeSpace = getFreeSpace(Environment.getExternalStorageDirectory());
        } else {
            L.w("Media storage is unmounted");
        }

        return freeSpace;
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
    public static long getFreeSpace(File path) {
        long freeSpace;

        StatFs stat = new StatFs(path.getPath());
        long blockSize = Utils.hasJellyBeanMr2() ? stat.getBlockSizeLong() : stat.getBlockSize();
        long availableBlocks = Utils.hasJellyBeanMr2() ? stat.getAvailableBlocksLong() : stat.getAvailableBlocks();
        freeSpace = availableBlocks * blockSize;

        return freeSpace;
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

    public static int getFilesCountInDirectory(File folder) {
        int count = 0;

        if (folder != null && folder.exists() && folder.isDirectory()) {
            File[] filesInFolder = folder.listFiles();

            if (filesInFolder != null) {
                count = filesInFolder.length;
            }
        }

        return count;
    }

    public static long getFolderSize(File file) {
        long totalFolderSize = 0;

        if (file == null || !file.exists() || !file.isDirectory()) {
            L.w("File does not exist or not a folder");

            return totalFolderSize;
        }

        File[] filesInFolder = file.listFiles();
        if (filesInFolder != null) {
            for (File f : filesInFolder) {
                if (f.isDirectory()) {
                    totalFolderSize += getFolderSize(f);
                } else {
                    totalFolderSize += f.length();
                }
            }
        }

        return totalFolderSize;
    }

    /**
     * Deletes folder and files and folders inside.
     *
     * @return {@code true} if this folder was deleted, {@code false} otherwise.
     */
    public static boolean deleteFolder(String pathToFolder) {
        return !Utils.isEmpty(pathToFolder) && deleteFolder(new File(pathToFolder));
    }

    /**
     * Deletes folder and all files and folders inside.
     *
     * @return {@code true} if this folder was deleted, {@code false} otherwise.
     */
    public static boolean deleteFolder(File folder) {
        if (folder != null && folder.isDirectory()) {
            deleteFilesInFolder(folder, true);
            return folder.delete();
        } else {
            return false;
        }
    }

    /**
     * Deletes all files in folder.
     *
     * @param deleteFoldersInside delete folders inside
     */
    public static void deleteFilesInFolder(File folder, boolean deleteFoldersInside) {
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (deleteFoldersInside) {
                            deleteFolder(file);
                        }
                    } else {
                        //noinspection ResultOfMethodCallIgnored
                        deleteFile(file);
                    }
                }
            }
        }
    }

    /**
     * Sorts all files in the folder by last modified and deletes the oldest ones. Remains remainLatestFilesCount.
     * It's useful if you need to shrink your cache.
     */
    public static void deleteFilesInFolder(File folder, int remainLatestFilesCount, boolean deleteFoldersInside) {
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                if (files.length > remainLatestFilesCount) {
                    sortByLastModified(files);

                    int filesForDeletionLength = files.length - remainLatestFilesCount;
                    File[] filesForDeletion = new File[filesForDeletionLength];

                    System.arraycopy(files, 0, filesForDeletion, 0, filesForDeletionLength);
                    deleteFiles(filesForDeletion, deleteFoldersInside);
                }
            }
        }
    }

    /**
     * Sorts all files in the folder by last modified and deletes the oldest ones.
     * Remains files last modified after beforeDateInMillis.
     * It's useful if you need to shrink your cache.
     */
    public static void deleteFilesInFolder(File folder, long beforeDateInMillis, boolean deleteFoldersInside) {
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                sortByLastModified(files);

                int filesForDeletionLength = 0;
                for (File file : files) {
                    if (file.lastModified() < beforeDateInMillis) {
                        filesForDeletionLength++;
                    }
                }

                if (filesForDeletionLength > 0) {
                    File[] filesForDeletion = new File[filesForDeletionLength];
                    System.arraycopy(files, 0, filesForDeletion, 0, filesForDeletionLength);
                    deleteFiles(filesForDeletion, deleteFoldersInside);
                }
            }
        }
    }

    /**
     * The oldest files will be at the beginning of the array
     */
    public static void sortByLastModified(File[] array) {
        Arrays.sort(array, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.lastModified() > rhs.lastModified() ? 1
                        : (lhs.lastModified() < rhs.lastModified() ? -1 : 0);
            }
        });
    }

    public static boolean deleteFile(String path) {
        return !Utils.isEmpty(path) && deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        boolean deleted = false;

        if (file != null) {
            deleted = file.delete();
            if (!deleted) {
                L.i("failed to delete " + (file.getPath()));
            }
        }

        return deleted;
    }

    /**
     * Deletes all files in folder.
     *
     * @param deleteFoldersInside delete folders inside
     */
    public static void deleteFiles(File[] files, boolean deleteFoldersInside) {
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (deleteFoldersInside) {
                        deleteFolder(file);
                    }
                } else {
                    //noinspection ResultOfMethodCallIgnored
                    deleteFile(file);
                }
            }
        }
    }

    public static void copyFile(String srcPath, String targetPath) throws IOException {
        copyFile(new File(srcPath), new File(targetPath));
    }

    public static void copyFile(File srcFile, File targetFile) throws IOException {
        FileChannel inChannel = new FileInputStream(srcFile).getChannel();
        FileChannel outChannel = new FileOutputStream(targetFile).getChannel();

        //noinspection TryFinallyCanBeTryWithResources
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            inChannel.close();
            outChannel.close();
        }
    }

    public static void writeFile(String data, File file) throws IOException {
        writeFile(data.getBytes(Charset.forName("UTF-8")), file);
    }

    public static void writeFile(byte[] data, File file) throws IOException {
        BufferedOutputStream bos = null;

        try {
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            bos.write(data);
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {
                //ignore
            }
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

    public static String readFileAsString(String path) throws IOException {
        return readFileAsString(new File(path));
    }

    public static String readFileAsString(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

            String line;
            while ((line = buf.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            try {
                if (buf != null) {
                    buf.close();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return sb.toString();
    }

    public static void createNomediaFileIfNeeded(File folder) {
        File nomediaFile = new File(folder, ".nomedia");
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

}
