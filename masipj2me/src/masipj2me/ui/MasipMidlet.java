/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package masipj2me.ui;

import com.sun.lwuit.Command;
import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.List;
import com.sun.lwuit.TextArea;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.layouts.BorderLayout;
import javax.microedition.midlet.*;
import masipj2me.model.DataSync;
import org.json.me.JSONArray;

/**
 * @author john
 */
public class MasipMidlet extends MIDlet {

    private DataSync dataSync = new DataSync();
    private Form homeForm;
    private TextArea status;
    private Form challengesForm;
    private List challengesList;
    private Command showCommand;
    private Command exitCommand;
    private Command homeCommand;

    public void startApp() {
        Display.init(this);

        homeForm = new Form("Challenges Home");
        homeForm.setLayout(new BorderLayout());

        status = new TextArea("Use 'Show' to fetch challenges");
        status.setEditable(false);
        homeForm.addComponent(BorderLayout.CENTER, status);

        challengesForm = new Form("Current Challenges");
        challengesList = new List();
        challengesForm.addComponent(challengesList);

        showCommand = new Command("Show");
        homeForm.addCommand(showCommand);

        exitCommand = new Command("Exit");
        homeForm.addCommand(exitCommand);

        homeCommand = new Command("Home");
        challengesForm.addCommand(homeCommand);

        homeForm.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getCommand() == showCommand) {
                    showChallenges();
                } else if (e.getCommand() == exitCommand) {
                    notifyDestroyed();
                }
            }
        });

        challengesForm.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getCommand() == homeCommand) {
                    homeForm.show();
                }
            }
        });

        // Start at home page
        homeForm.show();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    private void showChallenges() {
        try {
            status.setText("Fetching latest challenges from server..");
            JSONArray challenges = dataSync.getChallengeList();
            challengesList.setModel(new ChallengeListModel(challenges));
            status.setText("Challenges loaded. Use 'Show' to see them.");
            challengesForm.show();
        } catch (Exception e) {
            status.setText("Cannot load challenges: " + String.valueOf(e));
        }
    }
}
