import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client {
    public static void main(String[] args) throws Exception{
        // sharing game for two thread
        TicTacToeClient game = new TicTacToeClient();
        // first thread for sending message
        clientThread socketThread = new clientThread(args[0], game);
        // second thread for gui maintaining
        clientGUI lobby = new clientGUI(game);
        socketThread.start();
        lobby.start();
    }
}

// thread for sending message
class clientThread extends Thread{
    // sharing game for the thread
    private TicTacToeClient game;
    private String ip = "localhost";
    // constructor for setting the game
    public clientThread(String ip, TicTacToeClient game) {
        this.game = game;
        this.ip = ip;
    }
    public void run() {
        try{
            // setting up the socket, data output stream and input stream
            String buffer = "", backBuffer = "";
            Socket clientSocket = new Socket(ip, 50005);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // getting data until "end" token
            while(!buffer.split(" ")[0].equals("end")){
                // sending some garbage message to get the new message from server
                buffer = game.getAndSetOutputString("l l");
                //System.out.println(buffer);
                
                outToServer.writeBytes(buffer + '\n');
                backBuffer = inFromServer.readLine();
                
                // checking the state is lobby or room
                if(backBuffer.split("/")[0].equals("room")){
                    // get the message from server
                    game.setMessage(backBuffer.split("/")[1]);
                    if(game.getMessage().length()>=10 && game.getMessage().split(" ")[0].equals("Player"))
                        game.setInGame(true);
                    if(!game.getMessage().equals(" "))                
                        game.setMessage(backBuffer.split("/")[1]);
                    game.setMap(backBuffer.split("/")[2]);
                }
                else{
                    game.setInGame(false);
                    game.setMessage(backBuffer.split("/")[1]);
                    if(backBuffer.split("/").length>=3)
                        game.setUserList(backBuffer.split("/")[2]);
                }
                //System.out.println("FROM SERVER: " + backBuffer);
                // sleep one second for not sending message too fast
                sleep(1000);
            }
            System.out.println("Client end.");
            clientSocket.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

// gui thread
class clientGUI extends Thread{
    private TicTacToeClient game;
    JFrame lobby, room;
    JLabel loginSign, text, seperateLine, textInRoom;
    JTable onlineList;
    JTextArea loginUserName;
    JButton login,join,logout,TL,T,TR,L,M,R,DL,D,DR;
    JScrollPane scrollPane;
    boolean end = false;
    final int BUTTON_SIZE = 100;

    // constructor to set the sharing game
    public clientGUI(TicTacToeClient game) {
        this.game = game;
    }
    public void run(){
        // set two page to emulate two rooms
        lobby = new JFrame();
        room = new JFrame();
        // initialize the gui button, label, ...
        initialize();
        try{
            // renew the gui in one second
            while(!end){
                // in the lobby room
                if(game.isInGame()){
                    lobby.setVisible(false);
                    room.setVisible(true);
                }
                // in the game room
                else{
                    lobby.setVisible(true);
                    room.setVisible(false);    
                }
                // refreshing the gui button, label,...
                refresh();
                sleep(1000);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    // gui initialize function
    void initialize(){
        lobby.setTitle("Client");
        lobby.setLayout(null);
        room.setTitle(game.getPlayerName());
        room.setLayout(null);

        loginSign = new JLabel("Enter user name: ");
        loginSign.setBounds(10, 5, 300, 20);
        lobby.add(loginSign);

        loginUserName = new JTextArea();
        loginUserName.setBounds(130, 5, 200, 20);
        lobby.add(loginUserName);

        // login button let server know a new player is going to login
        login = new JButton("Login");
        login.setBounds(330, 5, 100, 20);
        lobby.add(login);
        // get the action of pressing the button
        login.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                // set the player name and change the gui into next mode
                game.setPlayerName(loginUserName.getText());
                room.setTitle(game.getPlayerName());
                loginUserName.setVisible(false);
                login.setVisible(false);
                join.setVisible(true);
                logout.setVisible(true);
                loginSign.setText("Hello, "+game.getPlayerName()+". Now you can join to play the game.");
                game.getAndSetOutputString("login "+game.getPlayerName());
            }
        });
        
        // join button to play the game
        join = new JButton("Join");
        join.setBounds(330, 5, 100, 20);
        lobby.add(join);
        join.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play l");
                game.setMessage("Waiting the empty room to join...");
            }
        });

        // logout button to shutdown the connection to socket
        logout = new JButton("Logout");
        logout.setBounds(330, 30, 100, 20);
        logout.setVisible(false);
        lobby.add(logout);
        logout.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("end l");
                text.setText("You are logged out.");
                end = true;
            }
        });

        text = new JLabel("Message: ");
        text.setBounds(10, 30, 300, 20);
        lobby.add(text);

        seperateLine = new JLabel("----------------------------------------------------------------------------------------------------------");
        seperateLine.setBounds(10, 50, 450, 20);
        lobby.add(seperateLine);

        // online user list showing
        // it can renew itself by getting the value in vector
        Vector<String> columns = new Vector<String>(Arrays.asList("User Name", "Win", "Lose", "Online"));
        onlineList = new JTable(game.getUserList(), columns);
        scrollPane = new JScrollPane(onlineList);
        scrollPane.setBounds(10, 80, 430, 250);
        lobby.add(scrollPane);
        
        textInRoom = new JLabel("Message: ");
        textInRoom.setBounds(10, 5, 300, 20);
        room.add(textInRoom);

        // a lot of button in the game
        TL = new JButton("");
        TL.setBounds(10, 30, BUTTON_SIZE, BUTTON_SIZE);
        TL.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(TL);
        TL.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 0,0");
            }
        });

        T = new JButton("");
        T.setBounds(10+BUTTON_SIZE, 30, BUTTON_SIZE, BUTTON_SIZE);
        T.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(T);
        T.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 0,1");
            }
        });

        TR = new JButton("");
        TR.setBounds(10+2*BUTTON_SIZE, 30, BUTTON_SIZE, BUTTON_SIZE);
        TR.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(TR);
        TR.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 0,2");
            }
        });

        L = new JButton("");
        L.setBounds(10, 30+BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        L.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(L);
        L.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 1,0");
            }
        });

        M = new JButton("");
        M.setBounds(10+BUTTON_SIZE, 30+BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        M.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(M);
        M.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 1,1");
            }
        });

        R = new JButton("");
        R.setBounds(10+2*BUTTON_SIZE, 30+BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        R.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(R);
        R.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 1,2");
            }
        });

        DL = new JButton("");
        DL.setBounds(10, 30+2*BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        DL.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(DL);
        DL.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 2,0");
            }
        });

        D = new JButton("");
        D.setBounds(10+BUTTON_SIZE, 30+2*BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        D.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(D);
        D.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 2,1");
            }
        });

        DR = new JButton("");
        DR.setBounds(10+2*BUTTON_SIZE, 30+2*BUTTON_SIZE, BUTTON_SIZE, BUTTON_SIZE);
        DR.setFont(new Font("Calibri", Font.PLAIN, 50));
        room.add(DR);
        DR.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.getAndSetOutputString("play 2,2");
            }
        });
        
        // set the window size
        lobby.setSize(450, 400);
        lobby.setVisible(true);
        room.setSize(330, 380);
        room.setVisible(false);

        lobby.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        room.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    // refreshing gui function
    void refresh(){
        if(game.checkWin()){
            game.setInGame(false);
        }
        else{
            // in the game room, update the message of each button
            textInRoom.setText(game.getMessage());
            TL.setText(game.getMap(0,0));
            T.setText(game.getMap(0,1));
            TR.setText(game.getMap(0,2));
            L.setText(game.getMap(1,0));
            M.setText(game.getMap(1,1));
            R.setText(game.getMap(1,2));
            DL.setText(game.getMap(2,0));
            D.setText(game.getMap(2,1));
            DR.setText(game.getMap(2,2));
        }
        // in the lobby
        if(!game.isInGame()){
            // update the online user list
            onlineList.updateUI();
            text.setText(game.getMessage());
            // recommend the weak player to you
            if(!game.getPlayerName().equals(""))
                loginSign.setText("Hello, "+game.getPlayerName()+". Recommend "+game.recommend()+" as your opponent.");    
        }
    }
}

// the sharing game class
class TicTacToeClient{
    private String[][] map = {{"","",""},{"","",""},{"","",""}};
    private Vector<Vector<String>> userList = new Vector<>();
    //private String[][] userList = {{"user1","1","0","0"},{"user2","1","0","0"}};
    private String playerName = "";
    private boolean inGame;
    private String outputString = "l l";
    private String message = "";

    // set the player name
    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }
    // get the player name
    public String getPlayerName(){
        return playerName;
    }
    // set the user list by string getting from server
    public void setUserList(String data){
        String[] temp = data.split(" ");
        for(int i=0;i<temp.length;i++){
            boolean alreadyIn = false;
            for(int j=0;j<userList.size();j++){
                //System.out.println(userList.get(j).get(0)+", "+temp[i].split(",")[0]);
                if(userList.get(j).get(0).equals(temp[i].split(",")[0])){
                    alreadyIn = true;
                    userList.set(j, new Vector<String>(Arrays.asList(temp[i].split(","))));
                }
            }
            if(!alreadyIn)
                userList.add(new Vector<String>(Arrays.asList(temp[i].split(","))));
        }
    }
    // get the user list
    public Vector<Vector<String>> getUserList(){
        /*for(int i=0;i<userList.size();i++){
            for(int j=0;j<userList.get(i).size();j++){
                System.out.print(userList.get(i).get(j)+ " ");
            }
            System.out.print("\n");
        }*/
        return userList;
    }
    // set the game room map
    public void setMap(String map){
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                this.map[i][j]=map.substring(i*3+j, i*3+j+1);
            }
        }
    }
    // get the game room map by indicated position
    public String getMap(int row, int col){
        return map[row][col];
    }
    // set the situation of the game
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
    // get the situation of the game
    public boolean isInGame(){
        return inGame;
    }
    // get the temporary output string, then set it into the new one
    public String getAndSetOutputString(String output){
        String temp = outputString;
        outputString = output;
        return temp;
    }
    // get the message
    public String getMessage(){
        return message;
    }
    // set the message
    public void setMessage(String message){
        this.message = message;
    }
    // check the game is win or not
    public boolean checkWin(){
        if(map[0][0].equals("O") && map[0][1].equals("O") && map[0][2].equals("O") || map[1][0].equals("O") && map[1][1].equals("O") && map[1][2].equals("O") || map[2][0].equals("O") && map[2][1].equals("O") && map[2][2].equals("O"))
            return true;
        else if(map[0][0].equals("O") && map[1][0].equals("O") && map[2][0].equals("O") || map[0][1].equals("O") && map[1][1].equals("O") && map[2][1].equals("O") || map[0][2].equals("O") && map[1][2].equals("O") && map[2][2].equals("O"))
            return true;
        else if(map[0][0].equals("O") && map[1][1].equals("O") && map[2][2].equals("O") || map[0][2].equals("O") && map[1][1].equals("O") && map[2][0].equals("O"))
            return true;
        else if(map[0][0].equals("X") && map[0][1].equals("X") && map[0][2].equals("X") || map[1][0].equals("X") && map[1][1].equals("X") && map[1][2].equals("X") || map[2][0].equals("X") && map[2][1].equals("X") && map[2][2].equals("X"))
            return true;
        else if(map[0][0].equals("X") && map[1][0].equals("X") && map[2][0].equals("X") || map[0][1].equals("X") && map[1][1].equals("X") && map[2][1].equals("X") || map[0][2].equals("X") && map[1][2].equals("X") && map[2][2].equals("X"))
            return true;
        else if(map[0][0].equals("X") && map[1][1].equals("X") && map[2][2].equals("X") || map[0][2].equals("X") && map[1][1].equals("X") && map[2][0].equals("X"))
            return true;
        else
            return false;
    }
    // recommend the weak opponent to you
    public String recommend(){
        String weakOpponet = "";
        float winRate = 1;
        for(int i=0;i<userList.size();i++){
            if(!userList.get(i).get(0).equals(playerName)){
                float w = Float.parseFloat(userList.get(i).get(1));
                float l = Float.parseFloat(userList.get(i).get(2));
                if(w/l <= winRate){
                    winRate = w/l;
                    weakOpponet = userList.get(i).get(0);
                }
            }
        }
        return weakOpponet;
    }
}