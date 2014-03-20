package com.ethereal.ymd.youtube;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * @author Slava
 */
public final class YoutubeUtils {

    private static final String PROPERTIES_FILENAME = "youtube.properties";

    /**
     * Retrieve youtube api key
     */
    public static String getYouTubeKey() throws IOException {
        Properties properties = new Properties();

        InputStream in = YouTube.Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
        properties.load(in);
        return properties.getProperty("youtube.apikey");
    }

    /**
     * Define a global instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static String makeYouTubeLink(final String videoId) {
        return "http://www.youtube.com/watch?v=" + videoId;
    }

}
