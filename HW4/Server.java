import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) throws Exception{
        // setting the socket for the server
        ServerSocket server = new ServerSocket(50005);
        Socket socket = null;
        // the sharing game room
        TicTacToeGame game = new TicTacToeGame();
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

// thread for multiple connection
class serverThread extends Thread{
    private Socket socket;
    private TicTacToeGame game;
    private String buffer = "";
    private String playerName = "";
    private boolean inGame = false;

    // constructor for setting connection and sharing game
    public serverThread(Socket socket, TicTacToeGame game){
        this.socket = socket;
        this.game = game;
    }
    public void run() {
        try{
            // set up for the sending and receiving message
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            String message = " ";
            // connection start at the room "lobby"
            String place = "lobby";
            while(true){
                // getting client sending message
                buffer = inFromClient.readLine();
                // different mode for the client sending message
                String mode = buffer.split(" ")[0];
                String info = "";
                if(buffer.split(" ").length>=2)
                    info = buffer.split(" ")[1];
                //System.out.println(buffer);
                //message = game.message;

                // login mode, add a new player into this game
                if(mode.equals("login")){
                    game.addNewPlayer(info);
                    playerName = info;
                }
                // play mode
                else if(mode.equals("play")){
                    // first need to join into the game, then change the position into game room
                    if(!inGame){
                        game.join(playerName);
                        game.reset();
                        inGame = true;
                        place = "room";
                    }
                    else{
                        // play mode, but other message is about what action you want to do in the game
                        if(game.isMyTurn(playerName) && game.checkWin().equals("no winner")){
                            int row = Integer.parseInt(info.split(",")[0]);
                            int col = Integer.parseInt(info.split(",")[1]);
                            String type = game.getPlayerType(playerName);
                            String win = game.setMove(row, col, type);
                            if(!win.equals("no winner")){
                                // this player win the game
                                if(win.equals(playerName)){
                                    game.message = "You win the game.";
                                    //System.out.println(playerName+" win.");
                                    
                                    // set the win/lose on user list, and go back to the lobby
                                    game.setPlayerList(playerName, String.valueOf(Integer.parseInt(game.getPlayerWin(playerName))+1), game.getPlayerLose(playerName), game.getPlayerOnline(playerName));
                                    inGame = false;
                                    place = "lobby";
                                }
                            }
                        }
                        else{
                            game.message = "This is not your turn.";
                            //System.out.println("This is not "+playerName+" turn.");
                        }
                    }
                }
                // end mode, shutdown the connection
                else if(mode.equals("end")){
                    outToClient.writeBytes("end/ / " + '\n');
                    // set the user list of this player into offline
                    game.setPlayerList(playerName, game.getPlayerWin(playerName), game.getPlayerLose(playerName), "0");
                    socket.close();
                    break;
                }
                else{
                    // you notice you opponent win the game
                    if(inGame && !game.checkWin().equals("no winner")){
                        inGame = false;
                        // set the win/lose on user list
                        game.setPlayerList(playerName, game.getPlayerWin(playerName), String.valueOf(Integer.parseInt(game.getPlayerLose(playerName))+1), game.getPlayerOnline(playerName));
                        game.message = "You lose the game.";
                        // go back to the lobby
                        place = "lobby";
                        game.leave();
                    }
                }
                message = game.message;
                // different message for different room
                if(inGame){
                    //System.out.println(game.showMap());
                    outToClient.writeBytes(place+"/"+message+"/"+game.showMap() + "\n");
                }
                else{
                    //System.out.println(game.showPlayerList());
                    outToClient.writeBytes(place+"/Message: "+message+"/"+game.showPlayerList() + "\n");
                }
                if(game.message.length()>=10 && !game.message.split(" ")[0].equals("Player"))
                    game.message = " ";
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

// sharing game class
class TicTacToeGame{
    private ArrayList<String> userList = new ArrayList<String>();
    private int nowUserCount;
    private int turn;
    private String[] player;
    private String[][] map;
    public String message = " ";
    int MAX_USER = 2;

    // constructor to set the number of user is playing, the player name, and the gaming map
    public TicTacToeGame(){
        nowUserCount = 0;
        player = new String[2];
        map = new String[3][3];
        
    }
    // add a new player, but if it was an old user login before, just change the offline into online
    public void addNewPlayer(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                setPlayerList(playerName, getPlayerWin(playerName), getPlayerLose(playerName), "1");
                return;
            }
        }
        // not find in the user list, so add the new user in it
        userList.add(playerName+",0,0,1");
    }
    // get the player win data
    public String getPlayerWin(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[1];
            }
        }
        return "";
    }
    // get the player lose data
    public String getPlayerLose(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[2];
            }
        }
        return "";
    }
    // get the player online data
    public String getPlayerOnline(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[3];
            }
        }
        return "";
    }
    // set the player data
    public void setPlayerList(String playerName, String win, String lose, String online){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                userList.set(i, new String(playerName+","+win+","+lose+","+online));
            }
        }
    }
    // show the whole player list in indicated format
    public String showPlayerList(){
        String temp = "";
        for(int i=0;i<userList.size();i++){
            temp += userList.get(i)+" ";
        }
        return temp;
    }
    // synchronized function for joining the game
    public synchronized void join(String playerName){
        message = "Waiting for other player joining...";
        //System.out.println("Waiting for other player joining...");
        
        // the room is full, just waiting...
        while(nowUserCount == MAX_USER){
            try{
                wait();
            }
            catch(InterruptedException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        // set the playing user name
        player[nowUserCount++] = playerName;
        notifyAll();
        message = "Player "+playerName+" join in the game.";
        //System.out.println("Player "+playerName+" join in the game.");
    }
    // leave the game
    public void leave(){
        nowUserCount = 0;
        player = new String[2];
        map = new String[3][3];
    }
    // reset the gaming map
    public void reset(){
        turn = 0;
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                map[i][j]= " ";
    }
    // get the gaming map
    public String showMap(){
        String temp = "";
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                temp += map[i][j];
        return temp;
    }
    // set the move on the map for the position
    public String setMove(int row, int col, String type){
        if(map[row][col].equals(" ")){
            turn = (turn+1)%2;
            map[row][col] = type;
            return checkWin();
        }
        message = "Illegal move.";
        //System.out.println("Illegal move.");
        return "";
    }
    // get the player type, "O" or "X"
    public String getPlayerType(String playerName){
        if(playerName.equals(player[0]))
            return "O";
        else
            return "X";
    }
    // check this turn is this player or not
    public boolean isMyTurn(String playerName){
        if(player[turn].equals(playerName))
            return true;
        else
            return false;
    }
    // check the game has a winner or not
    String checkWin(){
        if(map[0][0].equals("O") && map[0][1].equals("O") && map[0][2].equals("O") || map[1][0].equals("O") && map[1][1].equals("O") && map[1][2].equals("O") || map[2][0].equals("O") && map[2][1].equals("O") && map[2][2].equals("O"))
            return player[0];
        else if(map[0][0].equals("O") && map[1][0].equals("O") && map[2][0].equals("O") || map[0][1].equals("O") && map[1][1].equals("O") && map[2][1].equals("O") || map[0][2].equals("O") && map[1][2].equals("O") && map[2][2].equals("O"))
            return player[0];
        else if(map[0][0].equals("O") && map[1][1].equals("O") && map[2][2].equals("O") || map[0][2].equals("O") && map[1][1].equals("O") && map[2][0].equals("O"))
            return player[0];
        else if(map[0][0].equals("X") && map[0][1].equals("X") && map[0][2].equals("X") || map[1][0].equals("X") && map[1][1].equals("X") && map[1][2].equals("X") || map[2][0].equals("X") && map[2][1].equals("X") && map[2][2].equals("X"))
            return player[1];
        else if(map[0][0].equals("X") && map[1][0].equals("X") && map[2][0].equals("X") || map[0][1].equals("X") && map[1][1].equals("X") && map[2][1].equals("X") || map[0][2].equals("X") && map[1][2].equals("X") && map[2][2].equals("X"))
            return player[1];
        else if(map[0][0].equals("X") && map[1][1].equals("X") && map[2][2].equals("X") || map[0][2].equals("X") && map[1][1].equals("X") && map[2][0].equals("X"))
            return player[1];
        else
            return "no winner";
    }
}