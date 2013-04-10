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
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.layouts.BorderLayout;
import javax.microedition.midlet.*;
import masipj2me.model.DataSync;
import org.json.me.JSONArray;
import org.json.me.JSONObject;

/**
 * @author john
 */
public class MasipMidlet extends MIDlet {

    private DataSync dataSync = new DataSync();
    private Form homeForm;
    private TextArea status;
    private Form challengesForm;
    private List challengesList;
    private Command showChallengesCommand;
    private Command showActionsCommand;
    private Command exitCommand;
    private Command homeCommand;
    private Command backCommand;
   private TextArea challengeDescription;
    private Form microactionsForm;
    private List microactionsList;

    public void startApp() {
        Display.init(this);

        homeForm = new Form("Challenges Home");
        homeForm.setLayout(new BorderLayout());

        status = new TextArea("Use 'Show' to fetch challenges");
        status.setEditable(false);
        homeForm.addComponent(BorderLayout.CENTER, status);

        challengesForm = new Form("Current Challenges");
        challengesForm.setLayout(new BorderLayout());
        challengesList = new List();
        challengesForm.addComponent(BorderLayout.CENTER, challengesList);
        challengeDescription = new TextArea();
        challengeDescription.setEditable(false);
        challengesForm.addComponent(BorderLayout.SOUTH, challengeDescription);
        
        microactionsForm = new Form("Current Actions");
        microactionsForm.setLayout(new BorderLayout());
        microactionsList = new List();
        microactionsForm.addComponent(BorderLayout.CENTER, microactionsList);

        showChallengesCommand = new Command("Show");
        homeForm.addCommand(showChallengesCommand);

        exitCommand = new Command("Exit");
        homeForm.addCommand(exitCommand);

        homeCommand = new Command("Home");
        challengesForm.addCommand(homeCommand);
        
        showActionsCommand = new Command("Show");
        challengesForm.addCommand(showActionsCommand);
        
        backCommand = new Command("Back");
        microactionsForm.addCommand(backCommand);

        homeForm.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getCommand() == showChallengesCommand) {
                    showChallenges();
                } else if (e.getCommand() == exitCommand) {
                    notifyDestroyed();
                }
            }
        });

        challengesForm.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getCommand() == showActionsCommand) {
                    showActions();
                } else if (e.getCommand() == homeCommand) {
                    showHome();
                }
            }
        });
        
        microactionsForm.addCommandListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getCommand() == backCommand) {
                    showChallenges();
                }
            }
        });
        
        // Start at home page
        showHome();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
    
    private void showHome() {
        homeForm.show();
        challengesList.requestFocus(); // Make should list goes up & down
    }

    private void showChallenges() {
        try {
            status.setText("Fetching latest challenges..");
            final JSONArray challenges = dataSync.getChallengeList(true);
            ChallengeListModel model = new ChallengeListModel(challenges); 
            model.addSelectionListener(new SelectionListener() {
                public void selectionChanged(int i, int i1) {
                    try {
                        challengeDescription.setText(challenges.getJSONObject(i).getJSONObject("challenge").getString("chalDescription"));
                    } catch (Exception x) {
                        challengeDescription.setText(String.valueOf(x));
                    }
                }
            });
            challengesList.setModel(model);
            status.setText("Challenges loaded. Use 'Show' to see them.");
            challengesForm.show();
        } catch (Exception e) {
            homeForm.show();
            status.setText("Cannot load challenges: " + String.valueOf(e));
        }
    }
    
    private void showActions() {
        try {
            status.setText("Fetching actions for selection..");
            int selected = challengesList.getSelectedIndex();
            JSONObject wrapper = dataSync.getChallengeList(true).getJSONObject(selected);
            JSONObject challenge = wrapper.getJSONObject("challenge");
            String id = challenge.getString("id");
            final JSONArray actions = dataSync.getChallengeActions(id);
            ActionListModel model = new ActionListModel(actions); 
            microactionsList.setModel(model);
            microactionsForm.show();
            microactionsList.requestFocus();
        } catch (Exception e) {
            homeForm.show();
            status.setText("Cannot load actions: " + String.valueOf(e));
        }
    }
}
