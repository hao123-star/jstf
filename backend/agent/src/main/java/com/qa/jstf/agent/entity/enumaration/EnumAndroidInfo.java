package com.qa.jstf.agent.entity.enumaration;

public enum EnumAndroidInfo {

    Battery("Battery"),
    Display("DisPlay"),
    Rotation("Rotation"),
    SIM("SIM"),
    NetWork("NetWork"),
    ;

    String info;

    EnumAndroidInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return this.info;
    }

}
