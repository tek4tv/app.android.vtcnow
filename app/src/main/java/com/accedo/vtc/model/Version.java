package com.accedo.vtc.model;

import com.google.gson.annotations.SerializedName;

public class Version {
    @SerializedName("versionCode")
    private String versionCode;
    @SerializedName("versionName")
    private String versionName;

    public Version() {
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
