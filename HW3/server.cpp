#include<sys/types.h>
#include<sys/signal.h>
#include<sys/socket.h>
#include<sys/time.h>
#include<sys/resource.h>
#include<sys/wait.h>
#include<sys/errno.h>
#include<sys/stat.h>
#include<netinet/in.h>
#include<arpa/inet.h>
#include<unistd.h>
#include<string.h>
#include<dirent.h>

#include<iostream>
#include<string>
#include<fstream>
#include<vector>

#define QLEN 32
#define BUFSIZE 4096
#define PORT 50005

using namespace std;

// Slave process for handling client connection and chatting
int SLAVE(int fd, char* ip){
    char buf[BUFSIZE];
    int cc;
    string username;
    vector<string> userList, onlineList;
    DIR *dir;
    struct dirent *ent;
    // always getting the message from client until getting end signal
    while (cc = read(fd, buf, sizeof(buf))){
        int i = 0;
        if(cc < 0)
            perror("read");
        
        // Who has been logged in before
        userList.clear();
        onlineList.clear();
        if ((dir = opendir ("./user/")) != NULL) {
            while ((ent = readdir(dir)) != NULL) {
                if(string(ent->d_name)!="online" && string(ent->d_name)!="." && string(ent->d_name)!=".."){
                    userList.push_back(string(ent->d_name));
                }
            }
            closedir(dir);
        } 
        else{
            perror ("dir");
            exit(1);
        }
        // Who is online now
        if ((dir = opendir ("./user/online/")) != NULL) {
            while ((ent = readdir(dir)) != NULL) {
                if(string(ent->d_name)!="." && string(ent->d_name)!=".."){
                    onlineList.push_back(string(ent->d_name));
                }
            }
            closedir(dir);
        } 
        else{
            perror ("dir");
            exit(1);
        }

        // login mode
        if(string(buf).substr(0, string(buf).find_first_of(" ")) == "login"){
            bool alreadyLoginUser = false;
            username = string(buf).substr(string(buf).find_first_of(" ")+1, string(buf).size());
            //cout<<"login: "<<username<<endl;
            
            // check the username has already logged in or not
            for(int j=0;j<onlineList.size();j++){
                if(onlineList[j]==username){
                    alreadyLoginUser = true;
                }
            }
            // send message back to client and shutdown the connection
            if(alreadyLoginUser){
                cout<<"already log in user."<<endl;
                strcpy(buf, "bad login");
                if(write(fd, buf, cc) < 0){
                    perror("write");
                }
                exit(1);
            }
            else{
                // make directory for setting new user
                mkdir(("./user/"+username).c_str(), 0777);
                mkdir(("./user/online/"+username).c_str(), 0777);
                userList.push_back(username);
                // broadcast for on-line
                for(int j=0;j<userList.size();j++){
                    //cout<<userList[j]<<"_"<<onlineList[j]<<endl;
                    if(userList[j]!=username){
                        fstream file("./user/"+userList[j]+"/online_"+username, ios::out);
                        if(!file){
                            perror("file");
                            exit(1);
                        }
                        file<<username;
                        file.close();
                    }
                }
                
            }
        }
        // chat command
        else if(string(buf).substr(0, string(buf).find_first_of(" ")) == "chat"){
            string temp = string(buf).substr(string(buf).find_first_of(" ")+1, string(buf).size());
            string receiver = temp.substr(0, temp.find_first_of(" "));
            string message = temp.substr(temp.find_first_of(" ")+1, temp.size());
            
            bool availableUser=false;
            // checking the user client want to send is available or not
            for(int r=0;r<userList.size();r++){
                if(receiver==userList[r])
                    availableUser=true;
            }
            if(availableUser){
                // sending message to the chosen user
                bool onlineUser = false;
                for(int r=0;r<onlineList.size();r++){
                    if(onlineList[r]==receiver)
                        onlineUser = true;
                }
                if(!onlineUser){
                    strcpy(buf, ("<User "+receiver+" is off-line. The message will be passed when he comes back.>").c_str());
                    if(write(fd, buf, cc) < 0){
                        perror("write");
                    }
                }
                time_t timer;
                struct tm *upload_time;
                char time_buffer[20];
                time(&timer);
                upload_time = localtime(&timer);
                strftime (time_buffer,20,"%Y_%m_%d_%R:%S",upload_time);
                fstream file("./user/"+receiver+"/"+username+"_"+time_buffer+".txt", ios::out);
                if(!file){
                    perror("file");
                    exit(1);
                }
                file<<message;
                file.close();
            }
            else{
                // telling the client the chosen user is not exist
                strcpy(buf, ("<User "+receiver+" does not exist.>").c_str());
                if(write(fd, buf, cc) < 0){
                    perror("write");
                }
            }
            
        }
        // logout command
        else if(string(buf).substr(0, string(buf).find_first_of(" ")) == "logout"){
            for(int j=0;j<userList.size();j++){
                if(userList[j]==username){
                    for(int k=0;k<onlineList.size();k++){
                        // set the user into off-line
                        if(onlineList[k]==username){
                            onlineList.erase(onlineList.begin()+k);
                            rmdir(("./user/online/"+username).c_str());
                            break;
                        }
                    }
                }
                else{
                    // broadcast for off-line
                    fstream file("./user/"+userList[j]+"/offline_"+username, ios::out);
                    if(!file){
                        perror("file");
                        exit(1);
                    }
                    file<<username;
                    file.close();
                }
            }
        }
        
        // showing the message in server program for debbuging
        printf("%s: %s\n", username.c_str(), buf);
        
        // sending message back to client
        vector<string> filenameList;

        // getting all the message others users send to this client
        if ((dir = opendir (("./user/"+username+"/").c_str())) != NULL) {
            while ((ent = readdir(dir)) != NULL) {
                if(string(ent->d_name)!="." && string(ent->d_name)!=".."){
                    cout<<ent->d_name<<endl;
                    filenameList.push_back(string(ent->d_name));
                }
            }
        }
        else{
            perror ("dir");
            exit(1);
        }
        for(int f=0;f<filenameList.size();f++){
            // open the file and send back the message to this client
            fstream file("./user/"+username+"/"+filenameList[f], ios::in);
            string message;
            getline(file, message);
            
            // the other off-line message
            if(filenameList[f].substr(0, filenameList[f].find_first_of("_"))=="offline"){
                strcpy(buf, ("<User "+message+" is off-line, IP address: "+ip+".>").c_str());
            }
            // the other on-line message
            else if(filenameList[f].substr(0, filenameList[f].find_first_of("_"))=="online"){
                strcpy(buf, ("<User "+message+" is on-line, IP address: "+ip+".>").c_str());
            }
            // the other send to this client message
            else{
                string sender = filenameList[f].substr(0, filenameList[f].find_first_of("_"));
                string sendtime = filenameList[f].substr(filenameList[f].find_first_of("_")+1, filenameList[f].find_first_of(".")-2);
                for(int c=0;c<sendtime.size();c++)
                    if(sendtime[c]=='_')
                        sendtime[c]=' ';
                strcpy(buf, ("<User "+sender+" has sent you a message "+message+" at "+sendtime+".> ").c_str());
                
            }

            // sending the message successfully and remove this message
            if(write(fd, buf, cc) < 0){
                perror("write");
            }
            else{
                remove(("./user/"+username+"/"+filenameList[f]).c_str());
            }
        }
        // End of sending, telling the client stop to receive the message
        strcpy(buf, "end");
        if(write(fd, buf, cc) < 0){
            perror("write");
        }

    }
    return cc;
}

// reaper function to handle the signal
void reaper(int sig){
    int status;
    while(wait3(&status, WNOHANG, (struct rusage *)0) > 0);
}

int main(){
    // bind, listen the socket for server
    struct sockaddr_in srv;
    struct sockaddr_in cli;

    int msock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    int ssock;
    unsigned int cli_len = sizeof(cli);

    srv.sin_family = AF_INET;
    srv.sin_port = htons(PORT);
    srv.sin_addr.s_addr = htonl(INADDR_ANY);

    if(bind(msock, (struct sockaddr*) &srv, sizeof(srv)) < 0){
        perror("bind");
        exit(1);
    }
    if(listen(msock, 5) < 0){
        perror("listen");
        exit(1);
    }
    
    // reset all the user file when the program start
    system("rm -r ./user");
    mkdir("./user/", 0777);
    mkdir("./user/online/", 0777);

    signal(SIGCHLD, reaper);
    while (1){
        ssock = accept(msock, (struct sockaddr*)&cli, &cli_len);
        if(ssock < 0){
            perror("accept");
            exit(1);
        }
        // fork a new process for a new client coming
        switch (fork()){
            case 0:
                close(msock);
                //perror("read");
                exit(SLAVE(ssock, inet_ntoa(cli.sin_addr)));
            
            default:
                close(ssock);
                break;
            case -1:
                perror("fork");
        }
    }
}