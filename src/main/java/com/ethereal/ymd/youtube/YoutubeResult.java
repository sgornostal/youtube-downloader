package com.ethereal.ymd.youtube;

/**
 * @author Slava
 */
public final class YoutubeResult {

    private String videoId;
    private String title;
    private String thumbnail;

    public YoutubeResult(final String videoId, final String title, final String thumbnail) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnail = thumbnail;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(final String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(final String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        return title;
    }
}
