package com.qa.jstf.agent.entity.enumaration;

public enum EnumCmd {

    Input("Input"),
    Install("Install"),
    Restart("Restart"),
    ScreenShot("ScreenShot"),
    UnWakeLock("UnWakeLock"),
    FileSystem("FileSystem"),
    ;

    String type;

    EnumCmd(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
