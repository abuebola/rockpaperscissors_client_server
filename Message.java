/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: Message
 || Purpose:  Client and server communicate by exchanging this object
 ++-----------------------------------------------------------------------*/
import java.io.Serializable;

public class Message implements Serializable {

    public enum Command {
        USERNAME,
        USERNAMELIST,
        GAMEREQUEST,
        STOPGAME,
        MOVE,
        ERROR,
        RESULT,
        DISCONNECT,
        GAMEID
    }

    public String sender;
    public String message;
    public Command command;
    public int gameID;



    public Message(Command command, String sender, String message) {
        this.command = command;
        this.sender = sender;
        this.message = message;
    }

    //GAMEID
    public Message(Command command, int gameID) {
        this.command = command;
        this.gameID = gameID;
    }

    //RESULT, ERROR
    public Message(Command command, String message) {
        this.command = command;
        this.message = message;
    }

    //MOVE, STOP, DISCONNECT
    public Message(Command command, String sender, int gameID, String message) {
        this.command = command;
        this.sender = sender;
        this.message = message;
        this.gameID = gameID;
    }
}
