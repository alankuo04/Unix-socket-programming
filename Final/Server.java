import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class Server {
    public static void main(String[] args) throws Exception{
        // setting the socket for the server
        ServerSocket server = new ServerSocket(50005);
        Socket socket = null;
        // the sharing game room
        BlackJackServer game = new BlackJackServer();
        try {
            // loop for getting new connection
            while (true) {
                // accept a new connection
                socket = server.accept();
                System.out.println("started: " + server);
                // put it into a new thread, and put the sharing game too
                serverThread socketThread = new serverThread(socket, game);
                socketThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            server.close();
        }
    }
}

class serverThread extends Thread{
    private Socket socket;
    private BlackJackServer game;
    private String buffer = "";
    private String playerName = "";

    // constructor for setting connection and sharing game
    public serverThread(Socket socket, BlackJackServer game){
        this.socket = socket;
        this.game = game;
    }
    
    public void run(){
        try{
            // set up for the sending and receiving message
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            String message = "";
            String place = "lobby";

            while(true){
                // getting client sending message
                buffer = inFromClient.readLine();
                System.out.println(buffer);
                String mode = buffer.split(" ")[0];
                String info = buffer.split(" ")[1];

                if(mode.equals("login")){
                    // check the player name is useful or not
                    if(game.loginPlayer(info)){
                        message = "login successful";
                        playerName = info;
                    }
                    else{
                        message = "login failed";
                    }
                }
                // join the game room
                else if(mode.equals("join")){
                    message = "";
                    if(game.join(playerName)){
                        place = "room";
                    }
                    else{
                        message = "The room is full";
                    }
                }
                // start to play the game
                else if(mode.equals("start") && place.equals("room")){
                    game.start();
                }
                // in the room and client send some message to play the game
                else if(mode.equals("play") && game.isStart()){
                    if(game.isMyTurn(playerName)){
                        if(info.equals("get")){
                            game.getNewCard(playerName);
                        }
                        else if(info.equals("nomore")){
                            game.nextTurn();
                        }
                        else if(info.equals("money")){
                            game.addMoney(playerName);
                            //System.out.println(game.getPlayerMoney(playerName));
                        }
                    }
                    else{
                        message = "Not your turn";
                    }
                }
                // leave the room
                else if(mode.equals("leave")){
                    message = game.getMessage(place);
                    place = "lobby";
                }
                // do nothing
                else if(mode.equals("refresh")){
                    message = game.getMessage(place);
                }
                // end the connection
                else if(mode.equals("end")){
                    game.setPlayerList(playerName, game.getPlayerMoney(playerName), "offline");
                    outToClient.writeBytes("END"+'\n');
                    inFromClient.close();
                    outToClient.close();
                    socket.close();
                    break;
                }
                // send different message in different place
                if(place.equals("lobby"))
                    outToClient.writeBytes(place+"/"+message+"/"+game.showPlayerList()+'\n');
                else
                    outToClient.writeBytes(place+"/"+message+"/"+game.showPlayerCard()+'\n');
                    
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

// sharing data in different thread
class BlackJackServer{
    private ArrayList<String> playerList = new ArrayList<String>();
    private int playerCount;
    private int startCount;
    private int turn;
    private String[] playing;
    private int[][] card;
    private int[] money;
    private String message = "";
    int MAX_PLAYER = 3;

    // constructor to reset all the data
    public BlackJackServer(){
        playerCount = 0;
        startCount = 0;
        playing = new String[3];
        for(int i=0;i<3;i++)
            playing[i]="";
        card = new int[4][5];
        for(int i=0;i<4;i++)
            for(int j=0;j<5;j++)
                card[i][j]=0;
        money = new int[3];
        for(int i=0;i<3;i++)
            money[i]=0;
    }
    // check the player can be login or not
    public boolean loginPlayer(String playerName){
        for(int i=0;i<playerList.size();i++){
            if(playerList.get(i).split(",")[0].equals(playerName) && playerList.get(i).split(",")[2].equals("offline")){
                setPlayerList(playerName, getPlayerMoney(playerName), "online");
                return true;
            }
            else if(playerList.get(i).split(",")[0].equals(playerName) && playerList.get(i).split(",")[2].equals("online")){
                return false;
            }
        }
        playerList.add(playerName+",1000,online");
        return true;
    }
    
    public int getPlayerCount(){
        return playerCount;
    }
    // get the player having money
    public String getPlayerMoney(String playerName){
        for(int i=0;i<playerList.size();i++){
            if(playerList.get(i).split(",")[0].equals(playerName)){
                return playerList.get(i).split(",")[1];
            }
        }
        return "";
    }
    // get the player is online or offline
    public String getPlayerOnline(String playerName){
        for(int i=0;i<playerList.size();i++){
            if(playerList.get(i).split(",")[0].equals(playerName)){
                return playerList.get(i).split(",")[2];
            }
        }
        return "";
    }

    // set the player data
    public void setPlayerList(String playerName, String money, String online){
        for(int i=0;i<playerList.size();i++){
            if(playerList.get(i).split(",")[0].equals(playerName)){
                playerList.set(i, new String(playerName+","+money+","+online));
            }
        }
    }

    // show the whole player list in indicated format
    public String showPlayerList(){
        String temp = "";
        for(int i=0;i<playerList.size();i++){
            temp += playerList.get(i)+" ";
        }
        return temp;
    }
    // show all the player cards
    public String showPlayerCard(){
        String temp = "";
        for(int i=0;i<4;i++){
            for(int j=0;j<5;j++)
                if(card[i][j]==10){
                    temp += "T";
                }
                else if(card[i][j]==11){
                    temp += "J";
                }
                else if(card[i][j]==12){
                    temp += "Q";
                }
                else if(card[i][j]==13){
                    temp += "K";
                }
                else{
                    temp += card[i][j];
                }
        }
        return temp;
    }

    // synchronized function to set the startCount in correct number
    public synchronized void start(){
        startCount++;
        for(int i=0;i<4;i++)
            for(int j=0;j<5;j++)
                card[i][j]=0;
    }

    // synchronized function to set the playerCount in correct number
    public synchronized boolean join(String playerName){
        message = "";
        if(playerCount >= MAX_PLAYER || isStart()){
            return false;
        }
        else{
            playing[playerCount++] = playerName;
            return true;
        }
    }
    // get the message
    public String getMessage(String place){
        return message;
    }
    // check the game is start or not
    public boolean isStart(){
        if(playerCount != 0 && startCount >= playerCount)
            return true;
        else
            return false;
    }
    // check now is your turn or not
    public boolean isMyTurn(String playerName){
        if(playing[turn].equals(playerName))
            return true;
        else
            return false;
    }
    // get the sum of the cards
    public int getCardSum(int[] card){
        int sum = 0;
        for(int i=0;i<5;i++){
            if(card[i]>=10){
                sum += 10;
            }
            else{
                sum += card[i];
            }
        }
        return sum;
    }
    // get another new card
    public void getNewCard(String playerName){
        Random rand = new Random();
        int pokerCard = 1+rand.nextInt(12);
        if(playerName.equals("host")){
            for(int i=0;i<5;i++){
                if(card[3][i]==0){
                    card[3][i] = pokerCard;
                    break;
                }
            }
        }
        else{
            for(int i=0;i<5;i++){
                if(card[turn][i]==0){
                    card[turn][i] = pokerCard;
                    break;
                }
            }
            if(getCardSum(card[turn]) > 21 && turn <= playerCount){
                nextTurn();
            }
        }
    }
    // change to next player turn
    public void nextTurn(){
        turn++;
        // every player play end, change to the host turn
        if(turn >= playerCount){
            for(int i=0;i<5;i++){
                if(getCardSum(card[3]) < 17){
                    getNewCard("host");
                }
                else if(getCardSum(card[3]) > 21){
                    break;
                }
            }
            int winner = 0;
            int maxValue = 0;
            int[] cardSum = new int[4];
            for(int i=0;i<MAX_PLAYER+1;i++){
                cardSum[i]=getCardSum(card[i]);
                if(cardSum[i]==0){
                    continue;
                }
                System.out.println(i+" "+cardSum[i]);
                if(cardSum[i]<=21 && cardSum[i]>=maxValue){
                    maxValue = cardSum[i];
                    winner = i;
                }
            }
            // check who is the winner
            if(winner == 3){
                message = "The winner is host.";
                for(int i=0;i<playerList.size();i++){
                    for(int j=0;j<3;j++){
                        if(playerList.get(i).split(",")[0].equals(playing[j])){
                            setPlayerList(playing[j], String.valueOf(Integer.parseInt(getPlayerMoney(playing[j]))-money[j]), getPlayerOnline(playing[j]));
                        }
                    }
                }
            }
            else{
                message = "The winner is "+playing[winner];
            
                for(int i=0;i<playerList.size();i++){
                    for(int j=0;j<playerCount;j++){
                        if(j == winner && playerList.get(i).split(",")[0].equals(playing[j])){
                            setPlayerList(playing[j], String.valueOf(Integer.parseInt(getPlayerMoney(playing[j]))+2*money[j]), getPlayerOnline(playing[j]));
                        }
                        else if(playerList.get(i).split(",")[0].equals(playing[j])){
                            setPlayerList(playing[j], String.valueOf(Integer.parseInt(getPlayerMoney(playing[j]))-money[j]), getPlayerOnline(playing[j]));
                        }
                    }
                }
            }
            // reset the player counting in room
            startCount = 0;
            playerCount = 0;
            turn = 0;
            for(int i=0;i<3;i++)
                playing[i]="";
            /*for(int i=0;i<4;i++)
                for(int j=0;j<5;j++)
                    card[i][j]=0;*/
            for(int i=0;i<3;i++){
                money[i]=0;
            }
        }
    }
    // add money in this game
    public void addMoney(String playerName){
        money[turn] += 50;
    }
}