package com.example.uvsensor.service.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class zipFolder {
    public zipFolder(String folderPath, String zipFilePath) {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File folder = new File(folderPath);
            zipFile(folder, folder.getName(), zos);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void zipFile(File file, String basePath, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                zipFile(subFile, basePath + "/" + file.getName(), zos);
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                ZipEntry zipEntry = new ZipEntry(basePath + "/" + file.getName());
                zos.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];  // 每次读1024个字符，读不完下次接着读
                int length;
                while ((length = fis.read(buffer))!= -1) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }
}
