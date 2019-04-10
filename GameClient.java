/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: GameClient
 || Purpose:  Execution entry point of the game Client
 ++-----------------------------------------------------------------------*/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

public class GameClient {

    private static ClientGUI clientGUI;
    private static Socket clientSocket;
    private static boolean isConnected = false;
    private static String clientUsername;
    private static ObjectOutputStream outputObjectToServer;
    private static ObjectInputStream inputObjectFromServer;
    private static HashMap<String, Consumer<Message>> commands = new HashMap<>();
    private static final char FIELD_SEPARATOR = 0x1e;
    private static String opponent = null;
    private static int gameID = -1;
    private static boolean isPlaying = false;

    public static void main(String args[]) {

        clientGUI = new ClientGUI();
        clientGUI.setDisconnectedView();

        commands.put(Message.Command.USERNAMELIST.toString(), (arg1) -> updateUsernameList(arg1));
        commands.put(Message.Command.GAMEID.toString(), (arg1) -> gameIDReceived(arg1));
        commands.put(Message.Command.RESULT.toString(), (arg1) -> gameResultReceived(arg1));
        commands.put(Message.Command.GAMEREQUEST.toString(), (arg1) -> gameRequestReceived(arg1));
        commands.put(Message.Command.STOPGAME.toString(), (arg1) -> stopGameReceived(arg1));
        commands.put(Message.Command.DISCONNECT.toString(), (arg1) -> opponentDisconnected(arg1));
        commands.put(Message.Command.ERROR.toString(), (arg1) -> errorReceived(arg1));


        Thread listenForMessages = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnected) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Message receivedMessage = null;
                    try {
                        receivedMessage = (Message) inputObjectFromServer.readObject();
                    } catch (Exception ex) {}
                    if(receivedMessage != null){
                        commands.get(receivedMessage.command.toString()).accept(receivedMessage);
                    }
                }
            }
        });

        clientGUI.setConnectButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!isConnected) {
                    if (!clientGUI.nameTextBox.getText().isEmpty()) {
                        try {
                            clientSocket = new Socket("localhost", 1337);
                        } catch (Exception ex) {
                            clientGUI.displayError("Error connecting to the server.");
                            return;
                        }
                        if(clientSocket.isConnected()) {
                            clientUsername = clientGUI.nameTextBox.getText();
                            clientGUI.setConnectedView();
                            isConnected = true;
                            try {
                                outputObjectToServer = new ObjectOutputStream(clientSocket.getOutputStream());
                                inputObjectFromServer = new ObjectInputStream(clientSocket.getInputStream());
                            } catch(Exception ex) {}
                            sendUsername();
                            new Thread(listenForMessages).start();
                        }
                    } else {
                        clientGUI.displayError("Enter a nickname.");
                    }
                }
                else{
                    //user wants to disconnect
                    try {
                        outputObjectToServer.writeObject(new Message(Message.Command.DISCONNECT, clientUsername, gameID,""));
                        outputObjectToServer.flush();
                        isConnected = false;
                        isPlaying = false;
                        clientSocket.close();
                        outputObjectToServer.close();
                        inputObjectFromServer.close();
                        clientGUI.setDisconnectedView();
                    }
                    catch(Exception ex) {}
                }
            }
        });

        clientGUI.setPlayButtonActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isPlaying) {
                    if (clientGUI.clientsList.getSelectedItem() != null) {
                        isPlaying = true;
                        opponent = (String) clientGUI.clientsList.getSelectedItem();
                        Message msg = new Message(Message.Command.GAMEREQUEST, clientUsername, opponent);
                        try {
                            outputObjectToServer.writeObject(msg);
                            outputObjectToServer.flush();
                        } catch (Exception ex) {
                        }
                        clientGUI.setPlayingView();
                    }
                }
                else {
                    sendStop();
                    isPlaying = false;
                    clientGUI.setGameStoppedView();
                    clientGUI.gameAnnoncer.setText("<html><font color='blue' size='4'>You have stopped the game.</font></html>");
                }
            }
        });

        clientGUI.setMoveActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Message msg = new Message(Message.Command.MOVE,clientUsername,gameID,e.getActionCommand());
                try {
                    outputObjectToServer.writeObject(msg);
                    outputObjectToServer.flush();
                    clientGUI.setWaitingForMoveView();
                }
                catch(Exception ex) {}
            }
        });


    }

    private static void sendUsername() {
        Message msg = new Message(Message.Command.USERNAME,clientUsername,clientUsername);
        try {
            outputObjectToServer.writeObject(msg);
            outputObjectToServer.flush();
        } catch(Exception ex) {}

    }

    public static void sendStop(){
        Message msg = new Message(Message.Command.STOPGAME, clientUsername, gameID, "");
        try {
            outputObjectToServer.writeObject(msg);
            outputObjectToServer.flush();
        } catch(Exception ex) {}
    }

    private static void updateUsernameList(Message message) {
        if (!isPlaying) {
            String string = message.message;
            clientGUI.clientsList.removeAllItems();
            String[] usernameList = string.split(Character.toString(FIELD_SEPARATOR));
            for (int i = 0; i < usernameList.length; i++) {
                if (!usernameList[i].equals(clientUsername)) {
                    clientGUI.clientsList.addItem(usernameList[i]);
                }
            }
            if(opponent != null) {
                clientGUI.clientsList.setSelectedItem(opponent);
                opponent = null;
            }
        }
    }

    public static void gameIDReceived(Message message){
        gameID = message.gameID;
    }

    public static void gameResultReceived(Message message){
        String[] s = message.message.split(Character.toString(FIELD_SEPARATOR));
        clientGUI.updateResult(s);
        if(s[0].equals("draw")) {
            clientGUI.rockButton.setEnabled(true);
            clientGUI.scissorsButton.setEnabled(true);
            clientGUI.paperButton.setEnabled(true);
        }
        else {
            isPlaying = false;
            clientGUI.setGameEndedView();
        }
    }

    public static void gameRequestReceived(Message message){
        isPlaying = true;
        opponent = message.sender;
        clientGUI.clientsList.setSelectedItem(opponent);
        clientGUI.setPlayingView();
    }

    public static void stopGameReceived(Message message){
        isPlaying = false;
        clientGUI.setGameStoppedView();
        clientGUI.gameAnnoncer.setText("<html><font color='blue' size='4'>Opponent has stopped the game</font></html>");
    }

    public static void opponentDisconnected(Message message){
        isPlaying = false;
        opponent = null;
        clientGUI.setGameStoppedView();
        clientGUI.gameAnnoncer.setText("<html><font color='blue' size='4'>Opponent has disconnected.</font></html>");
    }

    public static void errorReceived(Message message){
        try {
            clientSocket.close();
            outputObjectToServer.close();
            inputObjectFromServer.close();
        } catch(Exception ex) {}
        clientGUI.displayError(message.message);
        isConnected = false;
        clientGUI.setDisconnectedView();
    }
}
