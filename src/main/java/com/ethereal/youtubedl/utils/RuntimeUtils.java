package com.ethereal.youtubedl.utils;

import com.ethereal.youtubedl.youtube.Quality;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Slava
 */
public final class RuntimeUtils {

    private static final Logger logger = Logger.getLogger(RuntimeUtils.class);
    private static final Pattern QUALITY_REGEX = Pattern.compile("^(\\d+)\\s+(\\w+)\\s+(\\w+).*$");
    private static final Pattern DOWNLOAD_PROGRESS = Pattern.compile("^.+\\s+(\\d+)\\.(\\d+)%.+$");

    public static interface DownloadProgressListener {
        public void onChange(int progress);
    }

    private static String getYouTubeDl() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return "youtube-dl.exe";
        }
        return "youtube-dl";
    }

    public static String getOSSeparator() {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return "\\";
        }
        return "/";
    }

    public static String updateYoutubeDl() throws Exception {
        Process p = Runtime.getRuntime().exec(getYouTubeDl() + " -U");
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        String retLine = null;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                retLine = line;
            }
        }
        return retLine;
    }

    public static String getVideoDuration(final String videoId) throws Exception {
        final Process p = Runtime.getRuntime().exec(getYouTubeDl() + " --get-duration " + videoId);
        p.waitFor();
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        String retLine = null;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty()) {
                retLine = line;
            }
        }
        return retLine;
    }

    public static void download(final String videoId, final String params, final DownloadProgressListener listener) throws Exception {
        final String command = getYouTubeDl() + params + " " + YoutubeUtils.makeYouTubeLink(videoId);
        final Process p = Runtime.getRuntime().exec(command);
        final BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        Matcher matcher = DOWNLOAD_PROGRESS.matcher(line);
                        if (matcher.matches()) {
                            listener.onChange(Integer.parseInt(matcher.group(1)));
                        }
                    }
                } catch (IOException e) {
                    logger.error("Progress Retrieve Error", e);
                }
            }
        }).start();
        p.waitFor();
    }

    public static List<Quality> getVideoQualities(final String videoId) throws Exception {
        final List<Quality> qualityList = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(getYouTubeDl() + " -F " + YoutubeUtils.makeYouTubeLink(videoId));
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            final Matcher matcher = QUALITY_REGEX.matcher(line);
            if (matcher.matches() && Integer.parseInt(matcher.group(1)) < 100) {
                qualityList.add(new Quality(
                        Integer.parseInt(matcher.group(1)),
                        matcher.group(2),
                        matcher.group(3)
                ));
            }
        }
        return qualityList;
    }

}
