package com.ethereal.youtubedl.youtube;

import com.ethereal.youtubedl.utils.YoutubeUtils;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Slava
 *         <p/>
 *         Youtube video search implementation based on sample from google.
 * @see { https://github.com/youtube/api-samples/tree/master/java }
 */
public final class VideoSearchProvider implements VideoSearch {

    private static final Logger logger = Logger.getLogger(VideoSearchProvider.class);
    private static final long NUMBER_OF_VIDEOS_RETURNED = 3;
    private final YouTube youtube;

    public VideoSearchProvider() {
        youtube = new YouTube.Builder(YoutubeUtils.HTTP_TRANSPORT, YoutubeUtils.JSON_FACTORY, new HttpRequestInitializer() {
            public void initialize(HttpRequest request) throws IOException {
            }
        }).setApplicationName("Youtube Downloader").build();
    }

    @Override
    public List<YoutubeResult> searchResults(final String queryTerm) {
        List<YoutubeResult> searchResultList = new ArrayList<>();
        try {
            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(YoutubeUtils.getYouTubeKey());
            search.setQ(queryTerm);

            // Restrict the search results to only include videos
            search.setType("video");

            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResults = searchResponse.getItems();
            for (SearchResult searchResult : searchResults) {
                searchResultList.add(new YoutubeResult(
                        searchResult.getId().getVideoId(),
                        searchResult.getSnippet().getTitle(),
                        searchResult.getSnippet().getThumbnails().getDefault().getUrl()
                ));

            }
        } catch (GoogleJsonResponseException e) {
            logger.error("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (Exception e) {
            logger.error("There was internal error.", e);
        }
        return searchResultList;
    }
}
