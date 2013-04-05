/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masipj2me.ui;

import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.list.ListModel;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 *
 * @author john
 */
public class ChallengeListModel implements ListModel {

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

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int i) {
        this.selectedIndex = i;
    }

    public void addDataChangedListener(DataChangedListener dl) {
    }

    public void removeDataChangedListener(DataChangedListener dl) {
    }

    public void addSelectionListener(SelectionListener sl) {
    }

    public void removeSelectionListener(SelectionListener sl) {
    }

    public void addItem(Object o) {
        // Don't allow adding yet
    }

    public void removeItem(int i) {
        // Don't allow yet
    }
}
