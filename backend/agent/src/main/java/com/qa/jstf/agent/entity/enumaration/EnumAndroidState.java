package com.qa.jstf.agent.entity.enumaration;

public enum EnumAndroidState {
    Unknown("Unknown"),
    Offline("Offline"),
    Device("Device"),
    Recovery("Recovery"),
    BootLoader("BootLoader"),
    ;

    String state;

    EnumAndroidState(String state) {
        this.state = state;
    }

    String getState() {
        return this.state;
    }

}
