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
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
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
    private final String CHALLENGE_STORE = "Challenges";
    
    private JSONArray challenges;

    public DataSync() {
    }

    private String fetchJsonRemote(String url) throws IOException {
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

    private JSONArray fetchChallengeListRemote() throws IOException, JSONException {
        String json = fetchJsonRemote(CHALLENGE_LIST_URL);
        return new JSONArray(json);
    }
    
    private JSONArray fetchChallengeListLocal() throws IOException, JSONException, RecordStoreException {
        String json = fetchJsonLocal(CHALLENGE_STORE);
        if (json != null) {
            return new JSONArray(json);
        }      

        return null;
    }

    public JSONArray getChallengeList() throws IOException, JSONException, RecordStoreException {
        if (challenges == null) {
            // try local storage first (null if nothing stored)
            challenges = fetchChallengeListLocal();
            
            if (challenges == null) {                 
               // get from server and cache local
               challenges = fetchChallengeListRemote();
               storeChallengeListLocal(challenges);
           }
        }

        return challenges;
    }

    public void storeChallengeListLocal(JSONArray challenges) throws RecordStoreException {
        storeJsonLocal(CHALLENGE_STORE, challenges.toString());
    }
    
    public void storeJsonLocal(String key, String json) throws RecordStoreException {
        RecordStore recordStore = RecordStore.openRecordStore(key, true);
        byte[] bytes = json.getBytes();
        
        if (recordStore.getNumRecords() > 0) {
            int id = recordStore.getNextRecordID();
            recordStore.setRecord(id, bytes, 0,
                bytes.length);
        } else {
            recordStore.addRecord(bytes, 0,
                bytes.length);
        }
        
        recordStore.closeRecordStore();
    }

    public String fetchJsonLocal(String key) throws RecordStoreException {
        try {
            RecordStore recordStore = RecordStore.openRecordStore(key, false);
            int id = recordStore.getNextRecordID();
            byte[] bytes = recordStore.getRecord(id);
            String json = new String(bytes);
            recordStore.closeRecordStore();
            return json;
        } catch (RecordStoreNotFoundException x) {
            return null;
        }
    }
}
