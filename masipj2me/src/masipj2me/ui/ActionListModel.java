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
public class ActionListModel extends DefaultListModel {

    private JSONArray microactions;
 
    public ActionListModel(JSONArray microactions) {
        this.microactions = microactions;
    }

    public Object getItemAt(int i) {
        try {
            JSONObject descriptor = microactions.getJSONObject(i);
            JSONObject action = descriptor.getJSONObject("challengeAction");
            return action.get("description");
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    public int getSize() {
        return microactions.length();
    }

    public void addItem(Object o) {
        throw new UnsupportedOperationException("not impl");
    }

    public void removeItem(int i) {
        throw new UnsupportedOperationException("not impl");
    }
}
