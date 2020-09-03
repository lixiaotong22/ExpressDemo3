package com.example.expressdemo3;

public class AppConfig {
    private static AppConfig config;

    private Long appID;
    private String appSign;
    private boolean isTestEnv;
    private boolean isOpenAdvanced;
    private String initDomain;
    private String serverSecret;
    private boolean isPlayUltra;
    private boolean isLoginAuth;
    private boolean isPublishAuth;
    private boolean isPlayAuth;


    private AppConfig() {//默认值
        this.appID = 2452251574L;
        this.appSign = "a8721f34e19651914d3e800ee1855973b842bdc4a3a432fd4c3f728ef0923d55";
        this.isTestEnv = true;
        this.isOpenAdvanced = false;
        this.initDomain = "";
        this.isPlayUltra = false;
        this.isLoginAuth = false;
        this.isPublishAuth = false;
        this.isPlayAuth = false;
        this.serverSecret = "";
    }

    public static synchronized AppConfig getInstance() {
        if (config == null) {
            config = new AppConfig();
        }
        return config;
    }

    public Long getAppID() {
        return appID;
    }

    public void setAppID(Long appID) {
        this.appID = appID;
    }

    public String getAppSign() {
        return appSign;
    }

    public void setAppSign(String appSign) {
        this.appSign = appSign;
    }

    public boolean isTestEnv() {
        return isTestEnv;
    }

    public void setTestEnv(boolean testEnv) {
        isTestEnv = testEnv;
    }

    public boolean isOpenAdvanced() {
        return isOpenAdvanced;
    }

    public void setOpenAdvanced(boolean openAdvanced) {
        isOpenAdvanced = openAdvanced;
    }

    public String getInitDomain() {
        return initDomain;
    }

    public void setInitDomain(String initDomain) {
        this.initDomain = initDomain;
    }

    public boolean isPlayUltra() {
        return isPlayUltra;
    }

    public void setPlayUltra(boolean playUltra) {
        isPlayUltra = playUltra;
    }

    public boolean isLoginAuth() {
        return isLoginAuth;
    }

    public void setLoginAuth(boolean loginAuth) {
        isLoginAuth = loginAuth;
    }

    public boolean isPublishAuth() {
        return isPublishAuth;
    }

    public void setPublishAuth(boolean publishAuth) {
        isPublishAuth = publishAuth;
    }

    public boolean isPlayAuth() {
        return isPlayAuth;
    }

    public void setPlayAuth(boolean playAuth) {
        isPlayAuth = playAuth;
    }

    public String getServerSecret() {
        return serverSecret;
    }

    public void setServerSecret(String serverSecret) {
        this.serverSecret = serverSecret;
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "appID=" + appID +
                ", appSign='" + appSign + '\'' +
                ", isTestEnv=" + isTestEnv +
                ", isOpenAdvanced=" + isOpenAdvanced +
                ", initDomain='" + initDomain + '\'' +
                ", serverSecret='" + serverSecret + '\'' +
                ", isPlayUltra=" + isPlayUltra +
                ", isLoginAuth=" + isLoginAuth +
                ", isPublishAuth=" + isPublishAuth +
                ", isPlayAuth=" + isPlayAuth +
                '}';
    }
}