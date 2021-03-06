package org.stenerud.kscrash.filter;

import org.stenerud.kscrash.MultipartPostBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





/**
 * 通过HTTP接口发送crash日志.
 *
 * Reports will be given file names of the form:
 *     [prefix]-[index].[extension]
 *
 * Index starts at 1
 *
 * Requires <uses-permission android:name="android.permission.INTERNET"/>
 *
 * Input: byte[]
 * Output: byte[]
 */
public class KSCrashReportFilterHttp implements KSCrashReportFilter {

    private URL url;
    private String charset;
    private String filePrefix;
    private String fileExtension;
    private Map<String, String> requestProperties = new HashMap<String, String>();
    private Map<String, String> stringFields = new HashMap<String, String>();
    private Map<String, byte[]> dataFields = new HashMap<String, byte[]>();



    /**
     * Constructor.
     *
     * @param url The URL to post the reports to.
     * @param filePrefix The prefix for the file names
     * @param fileExtension The extension for the file names.
     * @param charset What character set to use for encoding the request
     */
    public KSCrashReportFilterHttp(URL url, String filePrefix, String fileExtension, String charset) {
        this.url = url;
        this.filePrefix = filePrefix;
        this.fileExtension = fileExtension;
        this.charset = charset;
        setRequestProperty("User-Agent", "KSCrashReporter");
        setRequestProperty("Content-Type", MultipartPostBody.contentType);
    }

    /**
     * Constructor
     *
     * @param url The URL to post the reports to.
     * @param filePrefix The prefix for the file names
     * @param fileExtension The extension for the file names.
     */
    public KSCrashReportFilterHttp(URL url, String filePrefix, String fileExtension) {
        this(url, filePrefix, fileExtension, "UTF-8");
    }

    /**
     * Add a custom request property.
     *
     * @param name Property name.
     * @param value Property value.
     */
    public void setRequestProperty(String name, String value) {
        requestProperties.put(name, value);
    }

    /**
     * Add a custom string field.
     *
     * @param name Field name.
     * @param value Field value.
     */
    public void setField(String name, String value) {
        stringFields.put(name, value);
    }

    /**
     * Add a custom data field.
     *
     * @param name Field name.
     * @param value Field value.
     */
    public void setField(String name, byte[] value) {
        dataFields.put(name, value);
    }

    @Override
    public void filterReports(List reports, CompletionCallback completionCallback) throws KSCrashReportFilteringFailedException {
        try {
            byte[] body = getBodyWithReports((List<byte[]>)reports);
            HttpURLConnection connection = openConnection();
            try {
                connection.getOutputStream().write(body);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Unhandled HTTP Response code: " + responseCode + ": " + getResponseText(connection));
                }
                completionCallback.onCompletion(reports);
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            throw new KSCrashReportFilteringFailedException(e, reports);
        }
    }

    private byte[] getBodyWithReports(List<byte[]> reports) throws IOException {
        MultipartPostBody body = new MultipartPostBody(charset);
        body.setStringFields(stringFields);
        body.setDataFields(dataFields);
        for(int i = 0; i < reports.size(); i++) {
            body.setField(filePrefix + "-" + (i+1) + "." + fileExtension, reports.get(i));
        }
        return body.toByteArray();
    }

    private HttpURLConnection openConnection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        for(String key: requestProperties.keySet()) {
            connection.setRequestProperty(key, requestProperties.get(key));
        }
        return connection;
    }

    private String getResponseText(HttpURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer stringBuf = new StringBuffer();
            String line;
            boolean isFirst = true;
            while ((line = reader.readLine()) != null) {
                if(isFirst) {
                    isFirst = false;
                } else {
                    stringBuf.append("\r\n");
                }
                stringBuf.append(line);
            }
            reader.close();
            return stringBuf.toString();
        } catch(IOException e) {
            return "(unknown)";
        }
    }
}
