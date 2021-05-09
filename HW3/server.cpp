#include<sys/types.h>
#include<sys/signal.h>
#include<sys/socket.h>
#include<sys/time.h>
#include<sys/resource.h>
#include<sys/wait.h>
#include<sys/errno.h>
#include<sys/stat.h>
#include<netinet/in.h>
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

int SLAVE(int fd){
    char buf[BUFSIZE];
    int cc;
    string username;
    string userList[QLEN];
    DIR *dir;
    struct dirent *ent;
    while (cc = read(fd, buf, sizeof(buf))){
        int i = 0;
        if(cc < 0)
            perror("read");
        
        bool onlineList[QLEN] = {};
        // Who has been logged in before
        if ((dir = opendir ("./user/")) != NULL) {
            while ((ent = readdir(dir)) != NULL) {
                if(string(ent->d_name)!="online" && string(ent->d_name)!="." && string(ent->d_name)!=".."){
                    userList[i++] = string(ent->d_name);
                }
            }
            closedir (dir);
        } 
        else{
            perror ("dir");
            exit(1);
        }
        
        
        // login mode
        if(string(buf).substr(0, string(buf).find_first_of(" ")) == "login"){
            bool alreadyLoginUser = false;
            username = string(buf).substr(string(buf).find_first_of(" ")+1, string(buf).size());
            for(int j=0;j<i;j++){
                if(userList[j]==username && onlineList[j]){
                    alreadyLoginUser = true;
                }
            }
            if(alreadyLoginUser){
                cout<<"already log in user."<<endl;
                strcpy(buf, "bad login");
                if(write(fd, buf, cc) < 0){
                    perror("write");
                }
                exit(1);
            }
            else{
                mkdir(("./user/"+username).c_str(), 0777);
                mkdir(("./user/online/"+username).c_str(), 0777);
                userList[i]=username;
                onlineList[i]=true;
            }
        }
        // chat command
        else if(string(buf).substr(0, string(buf).find_first_of(" ")) == "chat"){
            string temp = string(buf).substr(string(buf).find_first_of(" ")+1, string(buf).size());
            string receiver = temp.substr(0, temp.find_first_of(" "));
            string message = temp.substr(temp.find_first_of(" ")+1, temp.size());
            
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
        // logout command
        else if(string(buf).substr(0, string(buf).find_first_of(" ")) == "logout"){
            for(int j=0;j<i;j++){
                if(userList[j]==username){
                    onlineList[j]=false;
                    rmdir(("./user/online/"+username).c_str());
                }
            }
        }
        
        printf("%s: %s\n", username.c_str(), buf);

        // Who is online now
        bool tempOnlineList[QLEN]={};
        if ((dir = opendir ("./user/online/")) != NULL) {
            while ((ent = readdir(dir)) != NULL) {
                if(string(ent->d_name)!="." && string(ent->d_name)!=".."){
                    for(int j=0;j<QLEN;j++){
                        if(string(ent->d_name)==userList[j]){
                            tempOnlineList[j]=true;
                        }
                    }
                }
            }
            closedir(dir);
        } 
        else{
            perror ("dir");
            exit(1);
        }
        // show the online/offline message
        for(int t=0;t<i;t++){
            cout<<userList[t]<<":"<<tempOnlineList[t]<<" "<<onlineList[t]<<endl;
            if(tempOnlineList[t]!=onlineList[t]){
                if(tempOnlineList[t]){
                    //cout<<userList[t]<<" is on-line."<<endl;
                    strcpy(buf, ("<User "+userList[t]+" is on-line, IP address: "+"123"+".> ").c_str());
                    if(write(fd, buf, cc) < 0){
                        perror("write");
                    }
                }
                else{
                    //cout<<userList[t]<<" is off-line."<<endl;
                    strcpy(buf, ("<User "+userList[t]+" is off-line.> ").c_str());
                    if(write(fd, buf, cc) < 0){
                        perror("write");
                    }
                }
            }
        }
        for(int t=0;t<i;t++)
            onlineList[t] = tempOnlineList[t];

        // get message
        vector<string> filenameList;

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
            string sender = filenameList[f].substr(0, filenameList[f].find_first_of("_"));
            string sendtime = filenameList[f].substr(filenameList[f].find_first_of("_")+1, filenameList[f].find_first_of(".")-2);
            for(int c=0;c<sendtime.size();c++)
                if(sendtime[c]=='_')
                    sendtime[c]=' ';
            fstream file("./user/"+username+"/"+filenameList[f], ios::in);
            string message;
            file>>message;
            strcpy(buf, ("<User "+sender+" has sent you a message "+message+" at "+sendtime+".> ").c_str());
            if(write(fd, buf, cc) < 0){
                perror("write");
            }
            remove(("./user/"+username+"/"+filenameList[f]).c_str());
        }
        strcpy(buf, "end");
        if(write(fd, buf, cc) < 0){
            perror("write");
        }

    }
    return cc;
}

void reaper(int sig){
    int status;
    while(wait3(&status, WNOHANG, (struct rusage *)0) < 0);
}

int main(){
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
        switch (fork()){
            case 0:
                close(msock);
                //perror("read");
                exit(SLAVE(ssock));
            
            default:
                close(ssock);
                break;
            case -1:
                perror("fork");
        }
    }
}