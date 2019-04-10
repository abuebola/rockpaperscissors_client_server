/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: ClientHandler
 || Purpose:  Class that handles the on-going communication between server and client
 ++-----------------------------------------------------------------------*/
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import static java.lang.Thread.sleep;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream inputFromClient;
    private ObjectOutputStream outputToClient;
    public String username;
    public boolean isConnected;
    public boolean isAvailable;
    public ConcurrentLinkedQueue<Message> outgoingMessageQueue = new ConcurrentLinkedQueue<Message>();
    ConcurrentLinkedQueue<Message> incomingMessageQueue; //common to all

    public ClientHandler(Socket socket, ObjectInputStream inputFromClient, ObjectOutputStream outputToClient, ConcurrentLinkedQueue<Message> incomingMessageQueue, String username){
        this.socket = socket;
        this.incomingMessageQueue = incomingMessageQueue;
        this.inputFromClient = inputFromClient;
        this.outputToClient = outputToClient;
        this.username = username;
        isConnected = true;
        isAvailable = true;
    }

    public void run() {
        //Thread that sends messages to clients
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnected) {
                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                    }
                    Message outgoingMessage = outgoingMessageQueue.poll();
                    if(outgoingMessage != null) {
                        try{
                            outputToClient.writeObject(outgoingMessage);
                            outputToClient.flush();
                        } catch(Exception ex) {}
                    }

                }
            }
        }).start();

        //reads incoming messages from clients
        while (isConnected) {
            Message msg = null;
            try{
                msg = (Message) inputFromClient.readObject();
            }
            catch(ClassNotFoundException ex) {}
            catch(IOException ex) {
                isAvailable = false;
                isConnected = false;}
            if(msg != null) {
                incomingMessageQueue.add(msg);
            }
        }
    }
}
