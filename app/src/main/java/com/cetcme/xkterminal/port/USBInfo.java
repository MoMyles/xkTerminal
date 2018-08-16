package com.cetcme.xkterminal.port;

import com.ftdi.j2xx.FT_Device;

public class USBInfo {
    private String path;
    private String baudrate = "9600";
    private AisReadThread readThread;
    private FT_Device ftDevice;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(String baudrate) {
        this.baudrate = baudrate;
    }

    public AisReadThread getReadThread() {
        return readThread;
    }

    public void setReadThread(AisReadThread readThread) {
        this.readThread = readThread;
    }

    public FT_Device getFtDevice() {
        return ftDevice;
    }

    public void setFtDevice(FT_Device ftDevice) {
        this.ftDevice = ftDevice;
    }
}
