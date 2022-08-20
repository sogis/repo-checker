package ch.so.agi.repochecker;

import java.io.File;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class Utils {
    /**
     * Returns the host of the application with context and api gateways respected.
     * @return Host of application.
     */
    public static String getHost() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    }

    /**
     * Returns the url of the log file.
     * @param logFileName
     * @return The log file url.
     */
    public static String getLogFileLocation(String logFileName) {
        String LOG_ENDPOINT = "logs";
         return fixUrl(getHost() + "/" + LOG_ENDPOINT + "/" + Utils.getLogFileUrlPathElement(logFileName));
    }
    
    /**
     * Returns the (partial) path as part of an url at which the log file is available.
     * @param logFilename
     * @return The unique (partial) path of the log file url.
     */
    public static String getLogFileUrlPathElement(String logFileName) {
        return new File(new File(logFileName).getParent()).getName() + "/" + new File(logFileName).getName();
    }

    /**
     * Fixes the url, e.g. finds multiple slashes in url preserving ones after protocol regardless of it. 
     * @param url
     * @return fixed url
     */
    public static String fixUrl(String url) {
        return url.replaceAll("(?<=[^:\\s])(\\/+\\/)", "/");
    }

}
