/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: GameServer
 || Purpose:  Execution entry point of the game server
 ++-----------------------------------------------------------------------*/
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static java.lang.Thread.sleep;

public class GameServer {

    private static ServerSocket welcomeSocket;
    private static HashMap<String, ClientHandler> clients = new HashMap<String, ClientHandler>();
    private static HashMap<Integer, Game> games = new HashMap<Integer, Game>();
    private static HashMap<String, Consumer<Message>> commands = new HashMap<>();
    private static ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>(); //shared by all the clients.
    private static Random rand;
    private static final char FIELD_SEPARATOR = 0x1e;
    private static int numberOfClients = 0;

    public static void main(String[] args) {

        commands.put(Message.Command.GAMEREQUEST.toString(), (arg1) -> gameRequest(arg1));
        commands.put(Message.Command.MOVE.toString(), (arg1) -> move(arg1));
        commands.put(Message.Command.STOPGAME.toString(), (arg1) -> stopGame(arg1));
        commands.put(Message.Command.DISCONNECT.toString(), (arg1) -> userDisconnect(arg1));

        ServerGUI serverGUI = new ServerGUI();
        rand = new Random();

        //Thread that listens for connections
        new Thread(new Runnable() {
            @Override
            public void run() {
                welcomeSocket = null;
                try {
                    welcomeSocket = new ServerSocket(1337);
                } catch (IOException ex) {
                }
                if (welcomeSocket != null) {
                    listenForConnections();
                }
            }
        }).start();


        //Thread that reads messages from the common queue, messageQueue, and call the appropriate command
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = messageQueue.poll();
                    if (message == null) {continue;}
                    commands.get(message.command.toString()).accept(message);
                }
            }
        }).start();

        //Thread that counts clients and maintains list of available clients to send to the users
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {
                    }
                    boolean change = false;
                    synchronized (games) {
                        ArrayList<Integer> finished = new ArrayList<Integer>();
                        if (!games.isEmpty()) {
                            for (Map.Entry<Integer, Game> entry : games.entrySet()) {
                                if(entry.getValue().player1.isConnected ^ entry.getValue().player2.isConnected) {
                                    entry.getValue().gameFinished = true;
                                    if(entry.getValue().player1.isConnected){
                                        entry.getValue().player1.isAvailable = true;
                                        entry.getValue().player1.outgoingMessageQueue.add(new Message(Message.Command.DISCONNECT,""));
                                    }
                                    else if(entry.getValue().player2.isConnected){
                                        entry.getValue().player2.isAvailable = true;
                                        entry.getValue().player2.outgoingMessageQueue.add(new Message(Message.Command.DISCONNECT,""));
                                    }
                                }
                                if (entry.getValue().gameFinished) {
                                    finished.add(entry.getKey());
                                }
                            }
                        }
                        if (!finished.isEmpty()) {
                            change = true;
                            for (Integer i : finished) {
                                games.remove(i);
                            }
                        }
                    }
                    synchronized (clients) {
                        ArrayList<String> disconnected = new ArrayList<String>();
                        if (!clients.isEmpty()) {
                            for (Map.Entry<String, ClientHandler> entry : clients.entrySet()) {
                                if (!entry.getValue().isConnected) {
                                    disconnected.add(entry.getKey());
                                }
                            }
                            if(!disconnected.isEmpty()){
                                change = true;
                                for (String s : disconnected) {
                                    clients.remove(s);
                                }
                            }
                        }
                        if(change) {sendListOfAvailablePlayers();}
                        int number = clients.size(); {
                            if(number != numberOfClients) {
                                serverGUI.updateClientsCount(number);
                                numberOfClients = number;
                            }
                        }
                    }
                }
            }
        }).start();


    }


    private static void listenForConnections() {

        while (true) {
            try {
                Socket connectionSocket = welcomeSocket.accept();   //blocking call
                ObjectOutputStream objectOutputToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
                ObjectInputStream objectInputFromClient = new ObjectInputStream(connectionSocket.getInputStream());
                Message message = null;
                try {
                    message = (Message) objectInputFromClient.readObject();
                } catch (ClassNotFoundException ex) {
                }
                if(message == null) {
                    continue;
                }
                String clientUsername = message.message;
                synchronized(clients) {
                    if(!clients.containsKey(clientUsername)) {
                        ClientHandler c = new ClientHandler(connectionSocket, objectInputFromClient, objectOutputToClient, messageQueue, clientUsername);
                        Thread t = new Thread(c);
                        t.start();
                        t.setName(clientUsername);
                        clients.put(clientUsername, c);
                        sendListOfAvailablePlayers();
                    }
                    else{
                        objectOutputToClient.writeObject(new Message(Message.Command.ERROR, "Username already exists."));
                        objectOutputToClient.flush();
                        connectionSocket.close();
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }

    private static void sendListOfAvailablePlayers() {
        String s = "";
        for (ClientHandler c : clients.values()) {
            if (c.isAvailable) {
                s = s + Character.toString(FIELD_SEPARATOR) + c.username;
            }
        }
        if (s.length() > 1) {
            Message msg = new Message(Message.Command.USERNAMELIST, s);
            for (ClientHandler c : clients.values()) {
                c.outgoingMessageQueue.add(msg);

            }
        }
    }

    private static void gameRequest(Message message){
        ClientHandler player1 = clients.get(message.sender);
        ClientHandler player2 = clients.get(message.message);
        player2.outgoingMessageQueue.add(message);
        int gameID = rand.nextInt(10000);
        while(games.containsKey(gameID)){
            gameID = rand.nextInt(10000);
        }
        Game game = new Game(player1, player2, gameID);
        games.put(gameID, game);
        player1.isAvailable = false;
        player2.isAvailable = false;
        synchronized(clients) {
            sendListOfAvailablePlayers();
        }
    }

    private static void move(Message message) {
        int gameID = message.gameID;
        String player = message.sender;
        String move = message.message;
        synchronized(games) {
            Game game = games.get(gameID);
            if(player.equals(game.player1.username)){
                game.player1move = move;

            }
            else {
                game.player2move = move;
            }
            if(game.player1move != null && game.player2move != null) {
                game.checkSendResult();
            }
        }
    }

    public static void stopGame(Message message) {
        int gameID = message.gameID;
        synchronized(games) {
            if (games.containsKey(gameID)) {
                if (games.get(gameID).player1.username.equals(message.sender)) {
                    //send stop to player 2
                    games.get(gameID).player2.outgoingMessageQueue.add(new Message(Message.Command.STOPGAME, message.sender, gameID, ""));
                } else {
                    games.get(gameID).player1.outgoingMessageQueue.add(new Message(Message.Command.STOPGAME, message.sender, gameID, ""));
                }
                games.get(gameID).gameFinished = true;
                games.get(gameID).player2.isAvailable = true;
                games.get(gameID).player1.isAvailable = true;
            }
        }
    }

    public static void userDisconnect(Message message){
        String sender = message.sender;
        int gameID = message.gameID;
        ClientHandler opponent;

        synchronized(games) {

            if(games.containsKey(gameID)) {

                if(games.get(gameID).player1.username.equals(sender)) {
                    opponent = games.get(gameID).player2;
                }
                else{
                    opponent = games.get(gameID).player1;
                }
                games.get(gameID).player2.isAvailable = true;
                games.get(gameID).player1.isAvailable = true;
                games.get(gameID).gameFinished = true;
                opponent.outgoingMessageQueue.add(new Message(Message.Command.DISCONNECT,""));
            }
        }
        synchronized (clients){
            clients.get(sender).isConnected = false;
        }
    }

}
