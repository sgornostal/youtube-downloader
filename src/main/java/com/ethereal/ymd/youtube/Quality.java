package com.ethereal.ymd.youtube;

/**
 * @author Slava
 */
public final class Quality {
    private int format;
    private String extension;
    private String resolution;

    public Quality(int format, String extension, String resolution) {
        this.format = format;
        this.extension = extension;
        this.resolution = resolution;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return getResolution() + " : " + getExtension().toUpperCase();
    }
}
