/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: Game
 || Purpose:  Game class used by the server to handle the games being played
 ++-----------------------------------------------------------------------*/
public class Game{

    public ClientHandler player1;
    public ClientHandler player2;
    private int gameID;
    public String player1move;
    public String player2move;
    public boolean gameFinished = false;
    private static final char FIELD_SEPARATOR = 0x1e;

    public Game(ClientHandler player1, ClientHandler player2, int gameID) {
        this.player1 = player1;
        this.player2 = player2;
        this.gameID = gameID;
        Message msg = new Message(Message.Command.GAMEID, gameID);
        player1.outgoingMessageQueue.add(msg);
        player2.outgoingMessageQueue.add(msg);
    }

    public void checkSendResult() {
        if(player1move.equals("ROCK")) {
            if(player2move.equals("ROCK")) {
                player1move = null;
                player2move = null;
                Message msg = new Message(Message.Command.RESULT, "draw");
                player1.outgoingMessageQueue.add(msg);
                player2.outgoingMessageQueue.add(msg);
            }
            else if(player2move.equals("SCISSORS")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;

            }
            else if(player2move.equals("PAPER")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;
            }
        }
        else if(player1move.equals("PAPER")) {
            if(player2move.equals("ROCK")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;
            }
            else if(player2move.equals("SCISSORS")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;
            }
            else if(player2move.equals("PAPER")) {
                player1move = null;
                player2move = null;
                Message msg = new Message(Message.Command.RESULT, "draw");
                player1.outgoingMessageQueue.add(msg);
                player2.outgoingMessageQueue.add(msg);
            }

        }
        else if(player1move.equals("SCISSORS")) {
            if(player2move.equals("ROCK")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;
            }
            else if(player2move.equals("SCISSORS")) {
                player1move = null;
                player2move = null;
                Message msg = new Message(Message.Command.RESULT, "draw");
                player1.outgoingMessageQueue.add(msg);
                player2.outgoingMessageQueue.add(msg);
            }
            else if(player2move.equals("PAPER")) {
                player1.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "win" + FIELD_SEPARATOR + player1move + FIELD_SEPARATOR + player2move));
                player2.outgoingMessageQueue.add(new Message(Message.Command.RESULT, "loss" + FIELD_SEPARATOR + player2move + FIELD_SEPARATOR + player1move));
                gameFinished = true;
            }
        }
        if(gameFinished){
            player1.isAvailable = true;
            player2.isAvailable = true;
        }
    }

}
