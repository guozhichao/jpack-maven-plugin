package com.blinkfox.jpack.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * 压缩工具类.
 *
 * @author blinkfox on 2019-04-23.
 * @since v1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompressKit {

    /**
     * 压缩文件夹为 zip 格式.
     *
     * @param dir 文件夹
     * @param zipPath zip全路径名
     * @throws IOException IO异常
     */
    public static void zip(String dir, String zipPath) throws IOException {
        compressFilesZip(getDirFiles(dir), zipPath, dir);
    }

    /**
     * 递归取到当前目录所有文件.
     *
     * @param dir 文件夹
     * @return 文件集合
     */
    private static List<String> getDirFiles(String dir) {
        File[] files = new File(dir).listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        List<String> filePaths = new ArrayList<>(files.length);
        for (File file : files) {
            if (file.isDirectory()) {
                filePaths.add(file.getAbsolutePath());
                filePaths.addAll(getDirFiles(file.getAbsolutePath()));
            } else {
                filePaths.add(file.getAbsolutePath());
            }
        }
        return filePaths;
    }

    /**
     * 文件名处理.
     *
     * @param dir 文件夹
     * @param path 路径
     * @return 文件名
     */
    private static String getFilePathName(String dir, String path) {
        String p = path.replace(dir + File.separator, "");
        p = p.replace("\\", "/");
        return p;
    }

    /**
     * 把文件压缩成 zip 格式.
     *
     * @param filePaths 需要压缩的文件数组
     * @param zipFilePath 压缩后的 zip 文件路径,如"D:/test/aa.zip";
     * @param dir 待压缩的目录
     * @throws IOException IO异常
     */
    private static void compressFilesZip(List<String> filePaths, String zipFilePath, String dir) throws IOException {
        if (filePaths == null || filePaths.isEmpty()) {
            return;
        }

        List<File> files = getAllFiles(filePaths);
        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(new File(zipFilePath))) {
            zaos.setUseZip64(Zip64Mode.AsNeeded);

            // 将每个文件用 ZipArchiveEntry 封装
            // 再用 ZipArchiveOutputStream 写到压缩文件中
            for (File file : files) {
                zaos.putArchiveEntry(new ZipArchiveEntry(file, getFilePathName(dir, file.getAbsolutePath())));
                if (!file.isDirectory()) {
                    compressFile(zaos, file);
                }
            }
            zaos.finish();
        }
    }

    /**
     * 根据文件路径创建文件放入集合中，须将目录放在文件之前.
     *
     * @param filePaths 文件路径集合
     * @return 文件对象集合
     */
    private static List<File> getAllFiles(List<String> filePaths) {
        List<File> dirs = new ArrayList<>();
        List<File> files = new ArrayList<>();
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (file.isDirectory()) {
                dirs.add(file);
            } else {
                files.add(file);
            }
        }
        dirs.addAll(files);
        return dirs;
    }

    /**
     * 压缩写入单个文件.
     *
     * @param zaos ZipArchiveOutputStream对象
     * @param file 单个的文件对象
     * @throws IOException IO异常
     */
    private static void compressFile(ZipArchiveOutputStream zaos, File file) throws IOException {
        try (
                InputStream is = new FileInputStream(file);
                InputStream bis = new BufferedInputStream(is)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                //把缓冲区的字节写入到 ZipArchiveEntry.
                zaos.write(buffer, 0, len);
            }
            zaos.closeArchiveEntry();
        }
    }

    /**
     * 将给定目录压缩成 `.tar.gz` 格式.
     *
     * @param dirPath 文件夹路径
     * @param tarGzPath 压缩文件的路径
     * @throws IOException IOException
     */
    public static void tarGz(String dirPath, String tarGzPath) throws IOException {
        File tarGzFile = new File(tarGzPath);
        try (FileOutputStream fos = new FileOutputStream(tarGzFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(bos);
                TarArchiveOutputStream taos = new TarArchiveOutputStream(gcos)) {
            String tarGzName = tarGzFile.getName().split(".tar.gz")[0] + File.separator;
            File[] children = new File(dirPath).listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    addFileToTarGz(taos, child.getAbsolutePath(), tarGzName);
                }
            }
            taos.finish();
        }
    }

    /**
     * 将文件添加到 tar.gz 压缩文件夹中.
     *
     * @param taos TarArchiveOutputStream实例
     * @param dirPath 文件夹路径
     * @param base 基础路径
     * @throws IOException IO异常
     */
    private static void addFileToTarGz(TarArchiveOutputStream taos, String dirPath, String base) throws IOException {
        File f = new File(dirPath);
        String entryName = base + f.getName();
        taos.putArchiveEntry(new TarArchiveEntry(f, entryName));

        if (f.isFile()) {
            try (FileInputStream in = new FileInputStream(f)) {
                IOUtils.copy(in, taos);
            }
            taos.closeArchiveEntry();
        } else {
            taos.closeArchiveEntry();
            File[] children = f.listFiles();
            if (children != null && children.length > 0) {
                for (File child : children) {
                    addFileToTarGz(taos, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

}
