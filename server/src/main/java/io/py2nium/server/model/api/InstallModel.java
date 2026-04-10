package io.py2nium.server.model.api;

import io.py2nium.server.common.annotation.RequiredField;

public class InstallModel extends InputModel {
    @RequiredField
    public String apkPath;

    public Boolean reinstall = false;

    public Boolean allowDowngrade = false;

    public Integer userId = 0;

    public Boolean grantPermissions = true;

    @Override
    public void validate() {
        super.validate();
        if (userId != null && userId < 0) {
            throw new IllegalArgumentException("Field 'userId' cannot be negative");
        }

        if (apkPath == null || apkPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Field 'apkPath' cannot be null or empty");
        }

        String ext = apkPath.substring(apkPath.lastIndexOf('.') + 1).toLowerCase();
        if (!ext.equals("apk") && !ext.equals("xapk")) {
            throw new IllegalArgumentException("Field 'apkPath' must point to a .apk or .xapk file");
        }
    }

    public boolean isXApk() {
        String ext = apkPath.substring(apkPath.lastIndexOf('.') + 1).toLowerCase();
        return ext.equals("xapk");
    }
}
