/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: ClientGUI
 || Purpose:  GUI of the game client
 ++-----------------------------------------------------------------------*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ClientGUI {

    private JFrame frame;
    private JPanel panel;

    private JLabel nameLabel;
    private JButton buttonConnectionToggle;
    private JButton playButton;
    private JLabel playWithLabel;
    public JComboBox<String> clientsList;
    public JTextField nameTextBox;
    public JButton rockButton;
    public JButton paperButton;
    public JButton scissorsButton;
    public JLabel gameAnnoncer;
    private JLabel status;

    public ClientGUI() {

        frame = new JFrame();
        panel = new JPanel();
        nameLabel = new JLabel("Client name:");
        playWithLabel = new JLabel("Play with:");
        buttonConnectionToggle = new JButton("Connect");
        playButton = new JButton("Play");
        nameTextBox = new JTextField();
        rockButton = new JButton("ROCK");
        paperButton = new JButton("PAPER");
        scissorsButton = new JButton("SCISSORS");
        gameAnnoncer = new JLabel();
        status = new JLabel("<html><font color='red'>Status: disconnected</font></html>");


        /* FRAME */
        frame.setSize(new Dimension(400, 550));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Game Client");
        frame.setResizable(false);

        /* NAME LABEL */
        nameLabel.setLayout(null);
        nameLabel.setBounds(30, 40, 70, 20);
        panel.add(nameLabel);

        /* NAME TEXT BOX */
        nameTextBox.setLayout(null);
        nameTextBox.setBounds(110, 40, 150, 25);
        panel.add(nameTextBox);

        /*CONNECT BUTTON*/
        buttonConnectionToggle.setLayout(null);
        buttonConnectionToggle.setBounds(270, 35, 100, 30);
        panel.add(buttonConnectionToggle);

        /* PLAY WITH LABEL */
        playWithLabel.setLayout(null);
        playWithLabel.setBounds(30, 80, 60, 20);
        panel.add(playWithLabel);

        /* COMBO BOX */
        clientsList = new JComboBox<String>();
        clientsList.setLayout(null);
        clientsList.setBounds(110, 80, 150, 25);
        panel.add(clientsList);

        clientsList.addItemListener (new ItemListener()
        {

            public void itemStateChanged (ItemEvent e)
            {
                boolean enable = clientsList.getSelectedIndex() > 0;
                playButton.setEnabled(enable);
            }

        });


        /*PLAY BUTTON*/
        playButton.setLayout(null);
        playButton.setBounds(270, 80, 100, 30);
        panel.add(playButton);

        /* ROCK, PAPER, SCISSORS */
        rockButton.setLayout(null);
        paperButton.setLayout(null);
        scissorsButton.setLayout(null);
        rockButton.setBounds(150,150,100,70);
        paperButton.setBounds(150,240,100,70);
        scissorsButton.setBounds(150,330, 100, 70);
        panel.add(rockButton);
        panel.add(paperButton);
        panel.add(scissorsButton);
        rockButton.setVisible(false);
        paperButton.setVisible(false);
        scissorsButton.setVisible(false);

        /* GAME ANNOUNCER */
        gameAnnoncer.setLayout(null);
        gameAnnoncer.setBounds(20,450, 400, 70);
        panel.add(gameAnnoncer);

        /* STATUS */
        status.setLayout(null);
        status.setBounds(10,5, 150, 25);
        panel.add(status);

        /*PANEL*/
        panel.setLayout(null);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public void setConnectButtonActionListener(ActionListener al) {
        buttonConnectionToggle.addActionListener(al);
    }

    public void setPlayButtonActionListener(ActionListener al) {
        playButton.addActionListener(al);
    }
    public void setMoveActionListener(ActionListener al) {
        rockButton.addActionListener(al);
        scissorsButton.addActionListener(al);
        paperButton.addActionListener(al);
    }

    public void setConnectedView() {
        buttonConnectionToggle.setText("Disconnect");
        nameTextBox.setEditable(false);
        rockButton.setVisible(false);
        paperButton.setVisible(false);
        scissorsButton.setVisible(false);
        playWithLabel.setText("Play with: ");
        clientsList.setEnabled(true);
        playButton.setText("Play");
        gameAnnoncer.setVisible(true);
        status.setText("<html><font color='green'>Status: connected</font></html>");
    }

    public void setGameEndedView() {
        playWithLabel.setText("Play with: ");
        clientsList.setEnabled(true);
        playButton.setText("Play");
        gameAnnoncer.setVisible(true);
        rockButton.setEnabled(false);
        paperButton.setEnabled(false);
        scissorsButton.setEnabled(false);

    }

    public void setGameStoppedView() {
        clientsList.removeAllItems();
        playWithLabel.setText("Play with: ");
        clientsList.setEnabled(true);
        playButton.setText("Play");
        gameAnnoncer.setVisible(true);
        rockButton.setEnabled(false);
        paperButton.setEnabled(false);
        scissorsButton.setEnabled(false);

    }

    public void setDisconnectedView() {
        buttonConnectionToggle.setText("Connect");
        playButton.setEnabled(false);
        nameTextBox.setEditable(true);
        rockButton.setVisible(false);
        paperButton.setVisible(false);
        scissorsButton.setVisible(false);
        playWithLabel.setText("Play with: ");
        clientsList.setEnabled(false);
        playButton.setText("Play");
        gameAnnoncer.setVisible(false);
        clientsList.removeAllItems();
        gameAnnoncer.setText("");
        status.setText("<html><font color='red'>Status: disconnected</font></html>");
    }

    public void setPlayingView(){
        rockButton.setVisible(true);
        paperButton.setVisible(true);
        scissorsButton.setVisible(true);
        playWithLabel.setText("Playing...");
        clientsList.setEnabled(false);
        playButton.setText("Stop");
        rockButton.setEnabled(true);
        paperButton.setEnabled(true);
        scissorsButton.setEnabled(true);
        gameAnnoncer.setText("<html><font color='blue' size='4'>Select a move.</font></html>");
    }

    public void setWaitingForMoveView(){
        rockButton.setEnabled(false);
        paperButton.setEnabled(false);
        scissorsButton.setEnabled(false);
        gameAnnoncer.setText("<html><font color='blue' size='4'>Waiting for opponent's move</font></html>");
    }

    public void displayError(String errorMessage){
        JOptionPane.showMessageDialog(null, errorMessage);
    }

    public void updateResult(String[] s) {
        if(s[0].equals("win")) {
            gameAnnoncer.setText("<html><font color='green' size='4'>" + s[1] + " x " + s[2] + "... you win!" + "</font></html>");
        }
        else if(s[0].equals("loss")) {
            gameAnnoncer.setText("<html><font color='red' size='4'>" + s[1] + " x " + s[2] + "... you lose." + "</font></html>");
        }
        else if(s[0].equals("draw")){
           gameAnnoncer.setText("<html><font color='orange' size='4'>Draw! Select another move.</font></html>");
        }

        shakeScreen();


    }

    public void shakeScreen() {
        Point location = frame.getLocationOnScreen();
        Point pos1 = new Point(location.x + 5, location.y - 10);
        Point pos2 = new Point(location.x - 5, location.y + 10);
        for (int i = 0; i < 200; i++) {
            frame.setLocation(pos1);
            frame.setLocation(pos2);
        }
        frame.setLocation(location);
    }

}
