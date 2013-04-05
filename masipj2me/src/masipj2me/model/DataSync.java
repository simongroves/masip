/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masipj2me.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import org.json.me.JSONArray;
import org.json.me.JSONException;

/**
 *
 * @author john
 */
public class DataSync {

    private final String CHALLENGE_LIST_URL = "http://microactions.net/mobile/tsla/dataSync/challengeList.php";
    private final String USER_AGENT = "Mozilla/5.0 (Series40)";
    private final int JSON_BUFFER = 8 * 1024; // 8K buffer (if json bigger than this it becomes inefficient)
    private JSONArray challengeList;

    public DataSync() {
    }

    private String fetchJson(String url) throws IOException {
        HttpConnection connection = (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
        connection.setRequestMethod(HttpConnection.POST);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Connection", "close");

        int code = connection.getResponseCode();
        String msg = connection.getResponseMessage();

        try {
            if (code == HttpConnection.HTTP_OK) {
                StringBuffer json = new StringBuffer(JSON_BUFFER);
                InputStream is = connection.openInputStream();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");

                int ch;
                while ((ch = isr.read()) != -1) {
                    // Strip any BOM characters as we go - they confuse the parser
                    if (ch != '\ufeff') {
                        json.append((char) ch);
                    }
                }

                isr.close();
                is.close();
                return json.toString();
            } else {
                throw new IOException("Server returned error while fetching json: " + code + ": " + msg + ": " + url);
            }
        } finally {
            // Clean up always
            connection.close();
        }
    }

    private JSONArray fetchChallengeList() throws IOException, JSONException {
        String json = fetchJson(CHALLENGE_LIST_URL);
        return new JSONArray(json);
    }

    public JSONArray getChallengeList() throws IOException, JSONException {
        if (challengeList == null) {
            challengeList = fetchChallengeList();
        }

        return challengeList;
    }
}
