package src.server_files;

import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket ss;
    private int numPlayers;
    private ServerSideConnection Player1;
    private ServerSideConnection Player2;
    private int Player1Num;
    private int Player2Num;

    Server() {
        System.out.println("BINGO Server Started!!");
        numPlayers = 0;
        try {
            ss = new ServerSocket(2000);
        } catch (IOException io) {
            System.out.println("IOException Occurred in BINGO Server constructor !! -> " + io);
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");
            while (numPlayers < 2) {
                Socket S = ss.accept();
                numPlayers++;
                System.out.println("Player #0" + numPlayers + " has connected.");
                ServerSideConnection ssc = new ServerSideConnection(S, numPlayers);
                if (numPlayers == 1) {
                    Player1 = ssc;
                } else {
                    Player2 = ssc;
                }
                Thread t = new Thread(ssc);
                t.start();
            }
            System.out.println("Two Players are connected to the BINGO Server. No longer accepting connections.");
        } catch (IOException io) {
            System.out.println("IOException occurred from acceptConnections() -> " + io);
        }
    }

    private class ServerSideConnection implements Runnable {
        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private int PlayerID;

        public ServerSideConnection(Socket s, int ID) {
            socket = s;
            PlayerID = ID;
            try {
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException io) {
                System.out.println("IOException occurred from ServerSideConnection Constructor -> " + io);
            }
        }

        @Override
        public void run() {
            try {
                dataOut.writeInt(PlayerID);
                while (true) {
                    if (PlayerID == 1) {
                        Player1Num = dataIn.read();
                        System.out.println("Player 1 clicked " + Player1Num);
                        // Send to Player2.
                        Player2.sendToClient(Player1Num);
                    } else {
                        Player2Num = dataIn.read();
                        System.out.println("Player 2 clicked " + Player2Num);
                        // Send to Player1.
                        Player1.sendToClient(Player2Num);
                    }
                }
            } catch (IOException io) {
                System.out.println("IOException occurred from ServerSideConnection run() ->" + io);
            }
        }

        public void sendToClient(int data) {
            try {
                dataOut.write(data);
            } catch (IOException e) {
                System.out.println("Exception Occurred in the SendToClient function -> " + e);
            }
            System.out.println("Data Sent to Other Player");
        }
    }

    public static void main(String args[]) {
        Server BS = new Server();
        BS.acceptConnections();
    }
}
