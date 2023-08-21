import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Client {
    private ClientSideConnection csc;
    private IPframe frame;
    private MainFrame MF;
    private String serverIP;
    private int PlayerID;
    private int OtherPlayerID;
    private boolean[][] crossed; // A boolean flag to check whether a button is crossed(clicked) or not.

    Client(String S) {
        serverIP = S;
        crossed = new boolean[5][5];
        connectToServer();
        SetGUI();
    }

    public void connectToServer() {
        csc = new ClientSideConnection(serverIP);
    }

    public void SetGUI() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    frame = new IPframe();
                    frame.setVisible(true);
                } catch (Exception e) {
                    System.out.println("Exception in the SetGUI function -> : " + e);
                }
            }
        });
    }

    void destroy_IPframe() // A function to hide the input (GUI)frame.
    {
        frame.setVisible(false);
    }

    void winnerFrame(int ID, String text) {
        JFrame winnerFrame = new JFrame();
        winnerFrame.setType(Type.POPUP);
        winnerFrame.setFont(new Font("Times New Roman", Font.BOLD, 20));
        winnerFrame.setTitle("GAME OVER !");
        winnerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        winnerFrame.setBounds(100, 100, 500, 100);
        JLabel tf = new JLabel();
        tf.setText(text);
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setVerticalAlignment(SwingConstants.CENTER);
        winnerFrame.add(tf);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    winnerFrame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class IPframe extends JFrame implements ActionListener {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        JTextField textField[][];
        private JPanel contentPane;
        int M1[][] = { { 1, 7, 11, 15, 19 }, { 6, 10, 14, 18, 20 }, { 3, 8, 12, 16, 4 }, { 9, 13, 17, 21, 25 },
                { 5, 22, 24, 23, 2 } };
        int M2[][] = { { 1, 2, 3, 4, 5 }, { 22, 25, 6, 23, 24 }, { 10, 12, 7, 13, 11 }, { 18, 19, 8, 20, 21 },
                { 14, 15, 9, 16, 17 } };

        public IPframe() {
            contentPane = new JPanel();
            textField = new JTextField[5][5];

            if (PlayerID == 1)
                OtherPlayerID = 2;
            else
                OtherPlayerID = 1;

            setType(Type.POPUP);
            setFont(new Font("Times New Roman", Font.BOLD, 20));
            setTitle("INPUT FRAME FOR PLAYER#0" + PlayerID);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setBounds(100, 100, 500, 500);
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(new GridLayout(5, 5, 10, 10));
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++) {
                    String text = new String("");

                    if (PlayerID == 1)
                        text = Integer.toString(M1[i][j]);
                    else
                        text = Integer.toString(M2[i][j]);

                    textField[i][j] = new JTextField(text);
                    textField[i][j].setHorizontalAlignment(SwingConstants.CENTER);
                    contentPane.add(textField[i][j]);
                    textField[i][j].addActionListener(this);
                }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[][] Arr = new int[5][5];
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    Arr[i][j] = 0;

            if (checkCondition(Arr) && checkComplete(Arr)) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        try {
                            MF = new MainFrame(Arr, csc.socket);
                            MF.setVisible(true);
                            destroy_IPframe();
                        } catch (Exception e) {
                            System.out.println("Exception in the Creation of MainFrame -> " + e);
                        }
                    }
                });
            }
        }

        boolean checkCondition(int[][] A) {
            boolean[] visited = new boolean[25]; // check for repeated values
            boolean flag = true;

            for (int i = 0; i < 25; i++) { // Initialize visited
                visited[i] = false;
            }

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    A[i][j] = Integer.parseInt(textField[i][j].getText());
                    if (textField[i][j].getText() == null)
                        continue;
                    if ((A[i][j] < 1) || (A[i][j] > 25)) {
                        textField[i][j].setEditable(true);
                        textField[i][j].setText("");
                        A[i][j] = 0;
                        flag = false;
                        continue;
                    }
                    if (visited[A[i][j] - 1]) { // to eliminate repeated values
                        textField[i][j].setEditable(true);
                        textField[i][j].setText("");
                        A[i][j] = 0;
                        flag = false;
                        continue;
                    }
                    visited[A[i][j] - 1] = true;
                    textField[i][j].setEditable(false);

                }
            }
            return flag;
        }

        boolean checkComplete(int[][] A) {
            boolean flag = true;
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++) {
                    if (A[i][j] == 0)
                        flag = false;
                }
            return flag;
        }

    }

    class MainFrame extends JFrame implements ActionListener {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        Socket socket;
        DataOutputStream dataOut;
        int[][] Arr;
        private JPanel contentPane;
        private JButton[][] ToggleButton;
        private JButton[] BINGO;
        private boolean buttonsEnable; // A boolean flag to enable and disable all the buttons for changing the turn of
                                       // the player

        int bingos = 0; // number of bingos completed
        boolean[] rowBingos = new boolean[5];
        boolean[] columnBingos = new boolean[5];
        boolean SE, SW; // South-East , South-West (diagonals)

        public MainFrame(int[][] A, Socket s) {
            Arr = A;
            socket = s;
            ToggleButton = new JButton[5][5];
            BINGO = new JButton[5];
            contentPane = new JPanel();

            try {
                dataOut = new DataOutputStream(s.getOutputStream());
            } catch (IOException e) {
                System.out.println("Exception in the MainFrame Constructor(creation of dataOutputStream) ->" + e);
            }

            if (PlayerID == 1) {
                OtherPlayerID = 2;
            } else {
                OtherPlayerID = 1;
            }

            GUI();
        }

        void togglebuttons() // A core function which toggles the buttons
        {
            try {
                for (int i = 0; i < 5; i++)
                    for (int j = 0; j < 5; j++) {
                        if (crossed[i][j]) // if it's already disabled then continue
                            continue;
                        ToggleButton[i][j].setEnabled(buttonsEnable);
                    }
            } catch (NullPointerException ex) {
                System.out.println("Exception Occurred in the togglebuttons function -> " + ex);
            }
        }

        void updateTurn() // Then function which implements the change of turns
        {
            boolean BINGOcompleted = false;
            int d = csc.receiveData(); // waits for the data to be received

            if (d == 0) // if the received data is 0 then it means that the other player has won the
                        // game (i.e got 5 BINGOS)
            {
                setTitle("Player#0" + OtherPlayerID + " IS THE WINNER.");
                winnerFrame(PlayerID, "OTHER PLAYER IS THE WINNER :(  BETTER LUCK NEXT TIME!!");
                buttonsEnable = false;
            } else {
                setTitle("IT'S YOUR TURN.. PLS PLAY"); // else it disables the received number
                for (int i = 0; i < 5; i++)
                    for (int j = 0; j < 5; j++)
                        if (Arr[i][j] == d) {
                            crossed[i][j] = true;
                            ToggleButton[i][j].setEnabled(false);
                            ToggleButton[i][j].setBackground(Color.green);
                        }
                buttonsEnable = true; // because after receiving the data from another player, now it's the current
                                      // player's move hence buttonsEnable = true
            }

            if (isBingo()) // checking whether a BINGO is occurred when the current player receives a data.
            {
                BINGO[bingos].setBackground(Color.red);
                bingos++;
            }

            if (bingos == 5) {
                BINGOcompleted = true;
            }

            if (BINGOcompleted) {
                setTitle("YOU ARE THE WINNER.\r\n");
                winnerFrame(PlayerID, "YOU ARE THE WINNER :) \n HURRAYYY!!");
                for (int i = 0; i < 5; i++)
                    for (int j = 0; j < 5; j++) {
                        crossed[i][j] = false;
                    }

                try {
                    dataOut.write(0); // feedback to other client indicating that current player has won the game.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            togglebuttons();
        }

        void GUI() {
            setType(Type.POPUP);
            setFont(new Font("Times New Roman", Font.BOLD, 20));

            if (PlayerID == 1) {
                setTitle(" YOU ARE PLAYER#0" + PlayerID + ". YOU GO FIRST");
                buttonsEnable = true;
            }
            if (PlayerID == 2) {
                setTitle(" YOU ARE PLAYER#0" + PlayerID + ". WAIT FOR YOUR TURN");
                buttonsEnable = false;

                Thread t = new Thread(new Runnable() { // A thread to continuously wait for player 2's turn (1st wait)
                    @Override
                    public void run() {
                        updateTurn();
                    }
                });
                t.start();
            }

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setBounds(100, 100, 500, 600);
            contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
            setContentPane(contentPane);
            contentPane.setLayout(new GridLayout(6, 5, 10, 10));
            BINGO[0] = new JButton("B");
            BINGO[1] = new JButton("I");
            BINGO[2] = new JButton("N");
            BINGO[3] = new JButton("G");
            BINGO[4] = new JButton("O");
            for (int i = 0; i < 5; i++) {
                BINGO[i].setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 17));
                BINGO[i].setEnabled(false);
                contentPane.add(BINGO[i]);
            }
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++) {
                    String text = Integer.toString(Arr[i][j]);
                    ToggleButton[i][j] = new JButton(text);
                    ToggleButton[i][j].setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 17));
                    contentPane.add(ToggleButton[i][j]);
                    ToggleButton[i][j].addActionListener(this);
                }
            togglebuttons();
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            boolean BINGOcompleted = false;

            JButton jtb = (JButton) ae.getSource();
            jtb.setEnabled(false);
            jtb.setBackground(Color.green);
            System.out.println(jtb.getText() + " sent to Player#0" + OtherPlayerID);
            for (int i = 0; i < 5; i++)
                for (int j = 0; j < 5; j++)
                    if (!crossed[i][j] && Arr[i][j] == Integer.parseInt(jtb.getText())) {
                        crossed[i][j] = true; // Updating the crossed matrix.
                    }

            if (isBingo()) {
                BINGO[bingos].setBackground(Color.red);
                bingos++;
            }

            if (bingos == 5) {
                BINGOcompleted = true;
            }

            if (!BINGOcompleted) {

                setTitle("Player#0" + OtherPlayerID + "'s TURN.. PLS WAIT");

                try {
                    dataOut.write(Integer.parseInt(jtb.getText()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setTitle("YOU ARE THE WINNER.\r\n");
                winnerFrame(PlayerID, "YOU ARE THE WINNER :) \n HURRAYYY!!");
                for (int i = 0; i < 5; i++)
                    for (int j = 0; j < 5; j++) {
                        crossed[i][j] = false;
                    }

                try {
                    dataOut.write(0); // feedback to other client indicating that current player has won the game.
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            buttonsEnable = false;
            togglebuttons();

            if (!BINGOcompleted) {
                Thread t = new Thread(new Runnable() { // the current player waits for the other player to complete
                                                       // his/her turn
                    @Override
                    public void run() {
                        updateTurn();
                    }
                });
                t.start();
            }
        }

        boolean isBingo() {
            if (crossed[0][0] && crossed[1][1] && crossed[2][2] && crossed[3][3] && crossed[4][4] && SE != true) {
                SE = true;
                return true;
            }
            if (crossed[0][4] && crossed[1][3] && crossed[2][2] && crossed[3][1] && crossed[4][0] && SW != true) {
                SW = true;
                return true;
            }

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j <= i; j++) {
                    // check for vertical bingos
                    if (crossed[0][j] && crossed[1][j] && crossed[2][j] && crossed[3][j] && crossed[4][j]
                            && columnBingos[j] != true) {

                        columnBingos[j] = true;
                        return true;
                    }
                    // check for horizontal bingos
                    else if (crossed[i][0] && crossed[i][1] && crossed[i][2] && crossed[i][3] && crossed[i][4]
                            && rowBingos[i] != true) {
                        rowBingos[i] = true;
                        return true;
                    }
                }
            }

            return false;
        }
    }

    // Client Connection Inner Class
    private class ClientSideConnection {
        private Socket socket;
        private DataInputStream dataIn;
        // private DataOutputStream dataOut;
        private int dataReceived;

        public ClientSideConnection(String serverIP) {
            System.out.println("----Client----");
            try {
                socket = new Socket(InetAddress.getByName(serverIP), 5000);
                dataIn = new DataInputStream(socket.getInputStream());
                // dataOut = new DataOutputStream(socket.getOutputStream());
                PlayerID = dataIn.readInt();
                System.out.println("Connected to Server as Player#0" + PlayerID + ".");
            } catch (IOException io) {
                System.out.println("IOException occurred from ClientSideConnection Constructor -> " + io);
            }
        }

        public int receiveData() {
            dataReceived = -1;
            try {
                dataReceived = csc.dataIn.read();
                System.out.println(dataReceived + " From other player");
            } catch (IOException e) {
                System.out.println("Exception Occurred from receiveData Function -> " + e);
            }
            return dataReceived;
        }
    }

    public static void main(String args[]) {
        if (args.length == 1)
            new Client(args[0]);
        else
            new Client("localhost");
    }
}