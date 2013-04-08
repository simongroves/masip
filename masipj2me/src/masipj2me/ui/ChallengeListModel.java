/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masipj2me.ui;

import com.sun.lwuit.list.DefaultListModel;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author john
 */
public class ChallengeListModel extends DefaultListModel {

    private JSONArray challenges;
    private int selectedIndex;

    public ChallengeListModel(JSONArray challenges) {
        this.challenges = challenges;
    }

    public Object getItemAt(int i) {
        try {
            JSONObject descriptor = challenges.getJSONObject(i);
            JSONObject challenge = descriptor.getJSONObject("challenge");
            return challenge.get("chalName");
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    public int getSize() {
        return challenges.length();
    }

    public void addItem(Object o) {
        throw new UnsupportedOperationException("not impl");
    }

    public void removeItem(int i) {
        throw new UnsupportedOperationException("not impl");
    }
}
