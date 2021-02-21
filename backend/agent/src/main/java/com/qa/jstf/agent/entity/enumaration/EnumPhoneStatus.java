package com.qa.jstf.agent.entity.enumaration;

public enum EnumPhoneStatus {
    Idle("Idle"),
    Offline("Offline"),
    Down("Down"),
    Occupied("Occupied")
    ;

    String status;

    EnumPhoneStatus(String status) {
        this.status = status;
    }

    String getStatus() {
        return this.status;
    }

}
