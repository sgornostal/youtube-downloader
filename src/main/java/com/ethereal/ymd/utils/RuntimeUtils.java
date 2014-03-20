package com.ethereal.ymd.utils;

import com.ethereal.ymd.youtube.Quality;
import com.ethereal.ymd.youtube.YoutubeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Slava
 */
public final class RuntimeUtils {

    private static final Pattern QUALITY_REGEX_PATTERN = Pattern.compile("(\\d+)\\s+(\\w+)\\s+(\\w+).*$");

    private static String getYouTubeDl() {
        if(System.getProperty("os.name").startsWith("Windows")){
            return "youtube-dl.exe";
        }
        return "youtube-dl";
    }

    public static String updateYoutubeDl() throws Exception {
        Process p = Runtime.getRuntime().exec(getYouTubeDl()+" -U");
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        String retLine = null;
        while ((line = reader.readLine())!= null) {
            if(!line.isEmpty())  {
                retLine = line;
            }
        }
        return retLine;
    }

    public static void download(final String videoId, final String params) throws Exception{
        final String command = getYouTubeDl()+ params + " " + YoutubeUtils.makeYouTubeLink(videoId);
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
    }

    public static List<Quality> getVideoQualities(final String videoId) throws Exception{
        final List<Quality> qualityList = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(getYouTubeDl()+" -F "+ YoutubeUtils.makeYouTubeLink(videoId));
        p.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine())!= null) {
            final Matcher matcher = QUALITY_REGEX_PATTERN.matcher(line);
            if(matcher.matches() && Integer.parseInt(matcher.group(1))<100) {
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
