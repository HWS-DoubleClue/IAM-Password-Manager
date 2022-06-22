package com.doubleclue.utils;

public class ActivationParameters {

    private String username;
    private String activationCode;
    private long validTill;

    public ActivationParameters() {
    }

    public ActivationParameters(String username, String activationCode, long validTill) {
        this.username = username;
        this.activationCode = activationCode;
        this.validTill = validTill;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public long getValidTill() {
        return validTill;
    }

    public void setValidTill(long validTill) {
        this.validTill = validTill;
    }
}
