import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class Client {
    public static void main(String[] args) throws Exception{
        TicTacToeClient game = new TicTacToeClient();
        clientThread socketThread = new clientThread(game);
        clientGUI lobby = new clientGUI(game);
        socketThread.start();
        lobby.start();
    }
}

class clientThread extends Thread{
    private TicTacToeClient game;
    public clientThread(TicTacToeClient game) {
        this.game = game;
    }
    public void run() {
        try{
            String buffer = "", backBuffer = "";
            Socket clientSocket = new Socket("192.168.91.128", 50005);
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while(!buffer.split(" ")[0].equals("end")){
                buffer = game.getAndSetOutputString("l l");
                System.out.println(buffer);
                outToServer.writeBytes(buffer + '\n');
                backBuffer = inFromServer.readLine();
                if(backBuffer.split("/")[0].equals("room")){
                    game.setMap(backBuffer.split("/")[2]);
                    game.setInGame(true);
                }
                else{
                    game.setInGame(false);
                    game.setMessage("Message: "+backBuffer.split("/")[1]);
                    if(backBuffer.split("/").length>=3)
                        game.setUserList(backBuffer.split("/")[2]);
                }
                System.out.println("FROM SERVER: " + backBuffer);
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

    public clientGUI(TicTacToeClient game) {
        this.game = game;
    }
    public void run(){
        lobby = new JFrame();
        room = new JFrame();
        initialize();
        try{
            while(!end){
                if(game.isInGame() && !game.getMessage().equals("Playing room is full.")){
                    lobby.setVisible(false);
                    room.setVisible(true);
                }
                else{
                    lobby.setVisible(true);
                    room.setVisible(false);    
                }
                refresh();
                sleep(1000);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
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

        login = new JButton("Login");
        login.setBounds(330, 5, 100, 20);
        lobby.add(login);
        login.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.setPlayerName(loginUserName.getText());
                loginUserName.setVisible(false);
                login.setVisible(false);
                join.setVisible(true);
                logout.setVisible(true);
                loginSign.setText("Hello, "+game.getPlayerName()+". Now you can join to play the game.");
                game.getAndSetOutputString("login "+game.getPlayerName());
            }
        });
        
        join = new JButton("Join");
        join.setBounds(330, 5, 100, 20);
        lobby.add(join);
        join.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                game.setInGame(true);
                game.getAndSetOutputString("play l");
            }
        });

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

        Vector<String> columns = new Vector<String>(Arrays.asList("User Name", "Win", "Lose", "Online"));
        onlineList = new JTable(game.getUserList(), columns);
        scrollPane = new JScrollPane(onlineList);
        scrollPane.setBounds(10, 80, 430, 250);
        lobby.add(scrollPane);
        
        textInRoom = new JLabel("Message: ");
        textInRoom.setBounds(10, 5, 300, 20);
        room.add(textInRoom);

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

        lobby.setSize(450, 400);
        lobby.setVisible(true);
        room.setSize(330, 380);
        room.setVisible(false);

        lobby.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        room.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    void refresh(){
        if(game.checkWin()){
            game.setInGame(false);
        }
        else{
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
        onlineList.updateUI();
        text.setText(game.getMessage());
    }
}

class TicTacToeClient{
    private String[][] map = {{"","",""},{"","",""},{"","",""}};
    private Vector<Vector<String>> userList = new Vector<>();
    //private String[][] userList = {{"user1","1","0","0"},{"user2","1","0","0"}};
    private String playerName = "";
    private boolean inGame;
    private String outputString = "l l";
    private String message = "";

    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }
    public String getPlayerName(){
        return playerName;
    }
    public void setUserList(String data){
        System.out.println("here:"+data);
        String[] temp = data.split(" ");
        for(int i=0;i<temp.length;i++){
            boolean alreadyIn = false;
            for(int j=0;j<userList.size();j++){
                System.out.println(userList.get(j).get(0)+", "+temp[i].split(",")[0]);
                if(userList.get(j).get(0).equals(temp[i].split(",")[0])){
                    alreadyIn = true;
                    userList.set(j, new Vector<String>(Arrays.asList(temp[i].split(","))));
                }
            }
            if(!alreadyIn)
                userList.add(new Vector<String>(Arrays.asList(temp[i].split(","))));
        }
    }
    public Vector<Vector<String>> getUserList(){
        for(int i=0;i<userList.size();i++){
            for(int j=0;j<userList.get(i).size();j++){
                System.out.print(userList.get(i).get(j)+ " ");
            }
            System.out.print("\n");
        }
        return userList;
    }
    public void setMap(String map){
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                this.map[i][j]=map.substring(i*3+j, i*3+j+1);
            }
        }
    }
    public String getMap(int row, int col){
        return map[row][col];
    }
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
    public boolean isInGame(){
        return inGame;
    }
    public String getAndSetOutputString(String output){
        String temp = outputString;
        outputString = output;
        return temp;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
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
}