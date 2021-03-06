package com.ethereal.youtubedl.youtube;

import java.util.List;

/**
 * @author Slava
 *         Video search interface
 */
public interface VideoSearch {
    public List<YoutubeResult> searchResults(String query);
}
