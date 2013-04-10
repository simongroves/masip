/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masipj2me.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author john
 */
public class DataSync {

    private final String CHALLENGE_LIST_URL = "http://microactions.net/mobile/tsla/dataSync/challengeList.php";
    private final String MICROACTIONS_LIST_URL = "http://microactions.net/mobile/tsla/dataSync/challengeActionList.php";
    private final String USER_AGENT = "Mozilla/5.0 (Series40)";
    private final int JSON_BUFFER = 8 * 1024; // 8K buffer (if json bigger than this it becomes inefficient)
    private final String CHALLENGE_STORE = "Challenges";
    private final String MICROACTION_STORE = "MicroActions";
    
    private JSONArray challenges;
    private JSONArray microactions;
    
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
    
    private JSONArray fetchMicroActionListRemote() throws IOException, JSONException {
        String json = fetchJsonRemote(MICROACTIONS_LIST_URL);
        return new JSONArray(json);       
    }

    private JSONArray fetchChallengeListLocal() throws IOException, JSONException, RecordStoreException {
        String json = fetchJsonLocal(CHALLENGE_STORE);
        if (json != null) {
            return new JSONArray(json);
        }

        return null;
    }
    
    private JSONArray fetchMicroActionListLocal() throws IOException, JSONException, RecordStoreException {
        String json = fetchJsonLocal(MICROACTION_STORE);
        if (json != null) {
            return new JSONArray(json);
        }

        return null;
    }

    public JSONArray getChallengeList(boolean useCache) throws IOException, JSONException, RecordStoreException {
        if (challenges == null) {
            // try local storage first (null if nothing stored)
            if (useCache) {
                challenges = fetchChallengeListLocal();
            }

            if (challenges == null) {
                // get from server and cache local
                challenges = fetchChallengeListRemote();
                storeChallengeListLocal(challenges);
            }
        }

        return challenges;
    }
    
    public JSONArray getMicroActionList(boolean useCache) throws IOException, JSONException, RecordStoreException {
        if (microactions == null) {
            // try local storage first (null if nothing stored)
            if (useCache) {
                microactions = fetchMicroActionListLocal();
            }

            if (microactions == null) {
                // get from server and cache local
                microactions = fetchMicroActionListRemote();
                storeMicroActionListLocal(microactions);
            }
        }

        return microactions;
    }

    public void storeChallengeListLocal(JSONArray challenges) throws RecordStoreException {
        storeJsonLocal(CHALLENGE_STORE, challenges.toString());
    }
    
    public void storeMicroActionListLocal(JSONArray microactions) throws RecordStoreException {
        storeJsonLocal(MICROACTION_STORE, microactions.toString());
    }


    public void storeJsonLocal(String key, String json) throws RecordStoreException {
        RecordStore recordStore = RecordStore.openRecordStore(key, true);
        byte[] bytes = json.getBytes();
        RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
        if (re.hasNextElement()) {
            int id = re.nextRecordId();
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
            RecordEnumeration re = recordStore.enumerateRecords(null, null, false);
            if (re.hasNextElement()) {
                byte[] bytes = re.nextRecord();
                String json = new String(bytes);
                recordStore.closeRecordStore();
                return json;
            } else {
                recordStore.closeRecordStore();
                return null;
            }
        } catch (RecordStoreNotFoundException x) {
            return null;
        }
    }
    
    public JSONArray getChallengeActions(String challengeId) throws IOException, JSONException, RecordStoreException {
        if (microactions == null) {
            getMicroActionList(true);
        }
        
        // Filter microactions to produce a list only for that challenge
        Vector result = new Vector();
        for (int i = 0; i < microactions.length(); i++) {
            JSONObject wrapper = microactions.getJSONObject(i);
            JSONObject action = wrapper.getJSONObject("challengeAction");
            if (challengeId.equals(action.getString("challengeId"))) {
                result.addElement(wrapper);
            }
        }
        
        return new JSONArray(result);
    }
}
