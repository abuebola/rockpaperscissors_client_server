/*+----------------------------------------------------------------------
 || ELEC463 Lab 5 - Andre Al-Khoury - 26017029
 || Class: ServerGUI
 || Purpose:  GUI of the game server
 ++-----------------------------------------------------------------------*/
import javax.swing.*;
import java.awt.*;

public class ServerGUI {

    private JFrame frame;
    private JPanel panel;
    private JLabel clientsConnected;

    public ServerGUI() {

        frame = new JFrame();
        panel = new JPanel();
        clientsConnected = new JLabel("No client connected.");

        frame.setSize(new Dimension(250, 150));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Game Server");
        frame.setResizable(false);

        clientsConnected.setLayout(null);
        clientsConnected.setBounds(60, 50, 150, 20);
        panel.add(clientsConnected);


        panel.setLayout(null);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void updateClientsCount(int number) {
        if(number == 0) {
            clientsConnected.setText("No client connected.");
        }
        else if(number == 1){
            clientsConnected.setText("1 client connected.");
        }
        else{
            clientsConnected.setText(number + " clients connected.");
        }
    }
}
