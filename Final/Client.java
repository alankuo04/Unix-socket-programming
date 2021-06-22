import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client {
    private JFrame lobby, room;
    private JLabel loginSign, text;
    private JTable onlineList;
    private JTextArea loginUserName;
    private JButton login,join,logout,refresh, refresh_room,start,leave,get,skip,addMoney;
    private JButton L1,L2,L3,L4,L5,T1,T2,T3,T4,T5,R1,R2,R3,R4,R5,D1,D2,D3,D4,D5;
    private JScrollPane scrollPane;
    private Vector<Vector<String>> playerList = new Vector<>();
    
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private BufferedReader inFromUser;

    public static void main(String[] args) throws Exception{
        try{
            Client client = new Client();
            // setting up the socket, data output stream and input stream
            String buffer = "";
            client.clientSocket = new Socket(args[0], 50005);
            client.outToServer = new DataOutputStream(client.clientSocket.getOutputStream());
            client.inFromServer = new BufferedReader(new InputStreamReader(client.clientSocket.getInputStream()));
            client.inFromUser = new BufferedReader((new InputStreamReader(System.in)));
            client.guiInitialize();

            // getting data until "END" token
            while(true){
                
                //outToServer.writeBytes(buffer +" 1 "+ '\n');
                buffer = client.inFromServer.readLine();
                if(buffer.equals("END")){
                    break;
                }
                System.out.println(buffer);
                String place = buffer.split("/")[0];
                String message = buffer.split("/")[1];
                String data = buffer.split("/")[2];
                if(place.equals("lobby")){
                    client.lobby.setVisible(true);
                    client.room.setVisible(false);
                    client.setPlayerList(data);
                    client.onlineList.updateUI();
                }
                else{
                    client.lobby.setVisible(false);
                    client.room.setVisible(true);
                    if(data.substring(0, 5).equals("00000")){
                        client.D1.setVisible(false);
                        client.D2.setVisible(false);
                        client.D3.setVisible(false);
                        client.D4.setVisible(false);
                        client.D5.setVisible(false);
                    }
                    if(data.substring(0, 5).charAt(0)!='0'){
                        client.D1.setVisible(true);
                        client.D1.setText(data.substring(0, 1));
                    }
                    if(data.substring(0, 5).charAt(1)!='0'){
                        client.D2.setVisible(true);
                        client.D2.setText(data.substring(1, 2));
                    }
                    if(data.substring(0, 5).charAt(2)!='0'){
                        client.D3.setVisible(true);
                        client.D3.setText(data.substring(2, 3));
                    }
                    if(data.substring(0, 5).charAt(3)!='0'){
                        client.D4.setVisible(true);
                        client.D4.setText(data.substring(3, 4));
                    }
                    if(data.substring(0, 5).charAt(4)!='0'){
                        client.D5.setVisible(true);
                        client.D5.setText(data.substring(4, 5));
                    }
                    if(data.substring(5, 10).equals("00000")){
                        client.L1.setVisible(false);
                        client.L2.setVisible(false);
                        client.L3.setVisible(false);
                        client.L4.setVisible(false);
                        client.L5.setVisible(false);
                    }
                    if(data.substring(5, 10).charAt(0)!='0'){
                        client.L1.setVisible(true);
                        client.L1.setText(data.substring(5, 6));
                    }
                    if(data.substring(5, 10).charAt(1)!='0'){
                        client.L2.setVisible(true);
                        client.L2.setText(data.substring(6, 7));
                    }
                    if(data.substring(5, 10).charAt(2)!='0'){
                        client.L3.setVisible(true);
                        client.L3.setText(data.substring(7, 8));
                    }
                    if(data.substring(5, 10).charAt(3)!='0'){
                        client.L4.setVisible(true);
                        client.L4.setText(data.substring(8, 9));
                    }
                    if(data.substring(5, 10).charAt(4)!='0'){
                        client.L5.setVisible(true);
                        client.L5.setText(data.substring(9, 10));
                    }
                    if(data.substring(10, 15).equals("00000")){
                        client.R1.setVisible(false);
                        client.R2.setVisible(false);
                        client.R3.setVisible(false);
                        client.R4.setVisible(false);
                        client.R5.setVisible(false);
                    }
                    if(data.substring(10, 15).charAt(0)!='0'){
                        client.R1.setVisible(true);
                        client.R1.setText(data.substring(10, 11));
                    }
                    if(data.substring(10, 15).charAt(1)!='0'){
                        client.R2.setVisible(true);
                        client.R2.setText(data.substring(11, 12));
                    }
                    if(data.substring(10, 15).charAt(2)!='0'){
                        client.R3.setVisible(true);
                        client.R3.setText(data.substring(12, 13));
                    }
                    if(data.substring(10, 15).charAt(3)!='0'){
                        client.R4.setVisible(true);
                        client.R4.setText(data.substring(13, 14));
                    }
                    if(data.substring(10, 15).charAt(4)!='0'){
                        client.R5.setVisible(true);
                        client.R5.setText(data.substring(14, 15));
                    }
                    if(data.substring(15, 20).equals("00000")){
                        client.T1.setVisible(false);
                        client.T2.setVisible(false);
                        client.T3.setVisible(false);
                        client.T4.setVisible(false);
                        client.T5.setVisible(false);
                    }
                    if(data.substring(15, 20).charAt(0)!='0'){
                        client.T1.setVisible(true);
                        client.T1.setText(data.substring(15, 16));
                    }
                    if(data.substring(15, 20).charAt(1)!='0'){
                        client.T2.setVisible(true);
                        client.T2.setText(data.substring(16, 17));
                    }
                    if(data.substring(15, 20).charAt(2)!='0'){
                        client.T3.setVisible(true);
                        client.T3.setText(data.substring(17, 18));
                    }
                    if(data.substring(15, 20).charAt(3)!='0'){
                        client.T4.setVisible(true);
                        client.T4.setText(data.substring(18, 19));
                    }
                    if(data.substring(15, 20).charAt(4)!='0'){
                        client.T5.setVisible(true);
                        client.T5.setText(data.substring(19, 20));
                    }
                }
                if(message.equals("login successful")){
                    // set the player name and change the gui into next mode
                    client.loginUserName.setVisible(false);
                    client.login.setVisible(false);
                    client.join.setVisible(true);
                    client.logout.setVisible(true);
                    client.loginSign.setText("Hello! Now you can join to play the game.");
                    
                }
                
                client.text.setText(message);
                
            }
            System.out.println("Client end.");
            client.clientSocket.close();
            client.outToServer.close();
            client.inFromServer.close();
            
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    void guiInitialize(){
        lobby = new JFrame();
        room = new JFrame();
        lobby.setTitle("Lobby");
        lobby.setLayout(null);
        room.setTitle("Room");
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
                String username = loginUserName.getText();
                try{
                    System.out.println("login " +username);
                    outToServer.writeBytes("login " +username+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        // join button to play the game
        join = new JButton("Join");
        join.setBounds(330, 5, 100, 20);
        lobby.add(join);
        join.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("join");
                    text.setText("");
                    outToServer.writeBytes("join 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });
        
        // logout button to shutdown the connection to socket
        logout = new JButton("Logout");
        logout.setBounds(330, 30, 100, 20);
        logout.setVisible(false);
        lobby.add(logout);
        logout.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("logout");
                    outToServer.writeBytes("end 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });
        
        text = new JLabel("Message: ");
        text.setBounds(10, 30, 300, 20);
        lobby.add(text);
        room.add(text);

        refresh = new JButton("Update");
        refresh.setBounds(10, 60, 100, 20);
        refresh.setVisible(true);
        lobby.add(refresh);
        refresh.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("refresh");
                    outToServer.writeBytes("refresh 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        Vector<String> columns = new Vector<String>(Arrays.asList("User Name", "Money", "Online"));
        onlineList = new JTable(getPlayerList(), columns);
        scrollPane = new JScrollPane(onlineList);
        scrollPane.setBounds(10, 80, 430, 250);
        lobby.add(scrollPane);

        refresh_room = new JButton("Update");
        refresh_room.setBounds(10, 80, 100, 20);
        refresh_room.setVisible(true);
        room.add(refresh_room);
        refresh_room.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("refresh_room");
                    outToServer.writeBytes("refresh 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        start = new JButton("start");
        start.setBounds(10, 60, 80, 20);
        start.setVisible(true);
        room.add(start);
        start.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("start");
                    outToServer.writeBytes("start 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        leave = new JButton("leave");
        leave.setBounds(350, 60, 80, 20);
        leave.setVisible(true);
        room.add(leave);
        leave.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("leave");
                    outToServer.writeBytes("leave 1"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        get = new JButton("get");
        get.setBounds(90, 60, 80, 20);
        get.setVisible(true);
        room.add(get);
        get.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("get");
                    outToServer.writeBytes("play get"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        skip = new JButton("skip");
        skip.setBounds(170, 60, 80, 20);
        skip.setVisible(true);
        room.add(skip);
        skip.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("skip");
                    outToServer.writeBytes("play nomore"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        addMoney = new JButton("addMoney");
        addMoney.setBounds(250, 60, 100, 20);
        addMoney.setVisible(true);
        room.add(addMoney);
        addMoney.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    System.out.println("addMoney");
                    outToServer.writeBytes("play money"+ '\n');
                }
                catch(Exception ee){
                    ee.printStackTrace();
                }
            }
        });

        D1 = new JButton("J");
        D1.setBounds(180, 500, 60, 90);
        D1.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(D1);
        
        D2 = new JButton("Q");
        D2.setBounds(240, 500, 45, 90);
        D2.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(D2);
        
        D3 = new JButton("K");
        D3.setBounds(285, 500, 45, 90);
        D3.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(D3);
        
        D4 = new JButton("A");
        D4.setBounds(330, 500, 45, 90);
        D4.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(D4);
        
        D5 = new JButton("2");
        D5.setBounds(375, 500, 45, 90);
        D5.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(D5);

        L1 = new JButton("J");
        L1.setBounds(10, 200, 90, 60);
        L1.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(L1);
        
        L2 = new JButton("Q");
        L2.setBounds(10, 260, 90, 45);
        L2.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(L2);
        
        L3 = new JButton("K");
        L3.setBounds(10, 305, 90, 45);
        L3.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(L3);
        
        L4 = new JButton("A");
        L4.setBounds(10, 350, 90, 45);
        L4.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(L4);
        
        L5 = new JButton("2");
        L5.setBounds(10, 395, 90, 45);
        L5.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(L5);

        R1 = new JButton("J");
        R1.setBounds(450, 200, 90, 60);
        R1.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(R1);
        
        R2 = new JButton("Q");
        R2.setBounds(450, 260, 90, 45);
        R2.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(R2);
        
        R3 = new JButton("K");
        R3.setBounds(450, 305, 90, 45);
        R3.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(R3);
        
        R4 = new JButton("A");
        R4.setBounds(450, 350, 90, 45);
        R4.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(R4);
        
        R5 = new JButton("2");
        R5.setBounds(450, 395, 90, 45);
        R5.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(R5);

        T1 = new JButton("J");
        T1.setBounds(180, 150, 60, 90);
        T1.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(T1);
        
        T2 = new JButton("Q");
        T2.setBounds(240, 150, 45, 90);
        T2.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(T2);
        
        T3 = new JButton("K");
        T3.setBounds(285, 150, 45, 90);
        T3.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(T3);
        
        T4 = new JButton("A");
        T4.setBounds(330, 150, 45, 90);
        T4.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(T4);
        
        T5 = new JButton("2");
        T5.setBounds(375, 150, 45, 90);
        T5.setFont(new Font("Calibri", Font.PLAIN, 10));
        room.add(T5);

        lobby.setSize(450, 400);
        lobby.setVisible(true);
        room.setSize(600, 800);
        room.setVisible(false);

        lobby.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        room.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    public void setPlayerList(String s){
        String[] temp = s.split(" ");
        for(int i=0;i<temp.length;i++){
            boolean alreadyIn = false;
            for(int j=0;j<playerList.size();j++){
                if(playerList.get(j).get(0).equals(temp[i].split(",")[0])){
                    alreadyIn = true;
                    playerList.set(j, new Vector<String>(Arrays.asList(temp[i].split(","))));
                }
            }
            if(!alreadyIn)
                playerList.add(new Vector<String>(Arrays.asList(temp[i].split(","))));
        }
    }

    public Vector<Vector<String>> getPlayerList(){
        return playerList;
    }
}
