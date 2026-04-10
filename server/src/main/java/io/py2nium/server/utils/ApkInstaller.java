package io.py2nium.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.py2nium.server.model.api.InstallModel;

public class ApkInstaller {
    public static void installApk(InstallModel installInfo) throws IOException {
        File apkFile = new File(installInfo.apkPath);
        if (!apkFile.exists() || !apkFile.isFile()) {
            throw new IllegalArgumentException("Apk file does not exist: " + installInfo.apkPath);
        }
        StringBuilder command = new StringBuilder("pm install ");
        addInstallFlags(command, installInfo);
        command.append("\"").append(installInfo.apkPath).append("\"");

        String result = ShellRunner.exec(command.toString());
        if (!result.toLowerCase().contains("success")) {
            throw new IOException("Failed to install APK: " + result);
        }
    }

    public static void installXApk(InstallModel installInfo) throws IOException {
        File xapkFile = new File(installInfo.apkPath);
        if (!xapkFile.exists() || !xapkFile.isFile()) {
            throw new IllegalArgumentException("XApk file does not exist: " + installInfo.apkPath);
        }

        File extractDir = xapkFile.getParentFile();
        extractXApk(xapkFile, extractDir);

        StringBuilder command = new StringBuilder("pm install-multiple ");
        addInstallFlags(command, installInfo);

        File[] files = extractDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".apk"));
        if (files == null || files.length == 0) {
            throw new IOException("No APK files found in the extracted XApk.");
        }
        for (File apk : files) {
            command.append("\"").append(apk.getAbsolutePath()).append("\" ");
        }

        String result = ShellRunner.exec(command.toString());
        if (!result.toLowerCase().contains("success")) {
            throw new IOException("Failed to install XApk: " + result);
        }

        // Check android obb files
        String cpObbResult = ShellRunner.exec("cp " + extractDir.getAbsolutePath() + "Android/* /sdcard/Android/");
        Logger.d(ApkInstaller.class, "Copy OBB result: " + cpObbResult);

    }

    private static void extractXApk(File xapkFile, File extractDir) throws IOException {
        byte[] buffer = new byte[8192];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(xapkFile));
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            String fileName = entry.getName();
            File newFile = new File(extractDir, fileName);
            if (entry.isDirectory()) {
                newFile.mkdirs();
            } else {
                File parent = newFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
            zis.closeEntry();
        }
    }

    private static void addInstallFlags(StringBuilder out, InstallModel installInfo) {
        if (Boolean.TRUE.equals(installInfo.reinstall)) {
            out.append("-r ");
        }
        if (Boolean.TRUE.equals(installInfo.allowDowngrade)) {
            out.append("-d ");
        }
        if (Boolean.TRUE.equals(installInfo.grantPermissions)) {
            out.append("-g ");
        }
        if (installInfo.userId != null) {
            out.append("--user ").append(installInfo.userId).append(" ");
        }
    }
}
