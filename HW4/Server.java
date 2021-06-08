import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) throws Exception{
        ServerSocket server = new ServerSocket(50005);
        Socket socket = null;
        TicTacToeGame game = new TicTacToeGame();
        try {
            while (true) {
                socket = server.accept();
                System.out.println("started: " + server);
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
    private TicTacToeGame game;
    private String buffer = "";
    private String playerName = "";
    private boolean inGame = false;

    public serverThread(Socket socket, TicTacToeGame game){
        this.socket = socket;
        this.game = game;
    }
    public void run() {
        try{
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
            String message = " ";
            String place = "lobby";
            while(true){
                buffer = inFromClient.readLine();
                String mode = buffer.split(" ")[0];
                String info = "";
                if(buffer.split(" ").length>=2)
                    info = buffer.split(" ")[1];
                System.out.println(buffer);
                message = game.message;
                if(mode.equals("login")){
                    game.addNewPlayer(info);
                    playerName = info;
                }
                else if(mode.equals("play")){
                    if(!inGame){
                        game.join(playerName);
                        game.reset();
                        inGame = true;
                        place = "room";
                    }
                    else{
                        if(game.isMyTurn(playerName) && game.checkWin().equals("no winner")){
                            int row = Integer.parseInt(info.split(",")[0]);
                            int col = Integer.parseInt(info.split(",")[1]);
                            String type = game.getPlayerType(playerName);
                            String win = game.setMove(row, col, type);
                            if(!win.equals("no winner")){
                                if(win.equals(playerName)){
                                    message = playerName + " win.";
                                    //System.out.println(playerName+" win.");
                                    game.setPlayerList(playerName, String.valueOf(Integer.parseInt(game.getPlayerWin(playerName))+1), game.getPlayerLose(playerName), game.getPlayerOnline(playerName));
                        
                                    inGame = false;
                                    place = "lobby";
                                }
                            }
                        }
                        else{
                            message = "This is not "+playerName+" turn.";
                            //System.out.println("This is not "+playerName+" turn.");
                        }
                    }
                }
                else if(mode.equals("end")){
                    outToClient.writeBytes("end/ / " + '\n');
                    game.setPlayerList(playerName, game.getPlayerWin(playerName), game.getPlayerLose(playerName), "0");
                    socket.close();
                    break;
                }
                else{
                    if(inGame && !game.checkWin().equals("no winner")){
                        inGame = false;
                        game.setPlayerList(playerName, game.getPlayerWin(playerName), String.valueOf(Integer.parseInt(game.getPlayerLose(playerName))+1), game.getPlayerOnline(playerName));
                        place = "lobby";
                        game.leave();
                    }
                }

                if(inGame){
                    System.out.println(game.showMap());
                    outToClient.writeBytes(place+"/"+message+"/"+game.showMap() + "\n");
                }
                else{
                    System.out.println(game.showPlayerList());
                    outToClient.writeBytes(place+"/"+message+"/"+game.showPlayerList() + "\n");
                }
                game.message = " ";
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}

class TicTacToeGame{
    private ArrayList<String> userList = new ArrayList<String>();
    private int nowUserCount;
    private int turn;
    private String[] player;
    private String[][] map;
    public String message = " ";
    int MAX_USER = 2;

    public TicTacToeGame(){
        nowUserCount = 0;
        player = new String[2];
        map = new String[3][3];
        
    }
    public void addNewPlayer(String playerName){
        userList.add(playerName+",0,0,1");
    }
    public String getPlayerWin(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[1];
            }
        }
        return "";
    }
    public String getPlayerLose(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[2];
            }
        }
        return "";
    }
    public String getPlayerOnline(String playerName){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                return userList.get(i).split(",")[3];
            }
        }
        return "";
    }
    public void setPlayerList(String playerName, String win, String lose, String online){
        for(int i=0;i<userList.size();i++){
            if(userList.get(i).split(",")[0].equals(playerName)){
                userList.set(i, new String(playerName+","+win+","+lose+","+online));
            }
        }
    }
    public String showPlayerList(){
        String temp = "";
        for(int i=0;i<userList.size();i++){
            temp += userList.get(i)+" ";
        }
        return temp;
    }
    public synchronized void join(String playerName){
        message = "Waiting for other player joining...";
        //System.out.println("Waiting for other player joining...");
        while(nowUserCount == MAX_USER){
            message = "Playing room is full.";
            try{
                wait();
            }
            catch(InterruptedException e){
                e.printStackTrace();
                System.exit(0);
            }
        }
        player[nowUserCount++] = playerName;
        notifyAll();
        message = "Player "+playerName+" join in the game.";
        System.out.println("Player "+playerName+" join in the game.");
    }
    public void leave(){
        nowUserCount = 0;
        player = new String[2];
    }
    public void reset(){
        turn = 0;
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                map[i][j]= " ";
    }
    public String showMap(){
        String temp = "";
        for(int i=0;i<3;i++)
            for(int j=0;j<3;j++)
                temp += map[i][j];
        return temp;
    }
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
    public String getPlayerType(String playerName){
        if(playerName.equals(player[0]))
            return "O";
        else
            return "X";
    }
    public boolean isMyTurn(String playerName){
        if(player[turn].equals(playerName))
            return true;
        else
            return false;
    }
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