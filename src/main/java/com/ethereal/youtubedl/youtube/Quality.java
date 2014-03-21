package com.ethereal.youtubedl.youtube;

/**
 * @author Slava
 */
public final class Quality {
    private int formatCode;
    private String extension;
    private String resolution;

    public Quality(int formatCode, String extension, String resolution) {
        this.formatCode = formatCode;
        this.extension = extension;
        this.resolution = resolution;
    }

    public int getFormatCode() {
        return formatCode;
    }

    public void setFormatCode(int formatCode) {
        this.formatCode = formatCode;
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
