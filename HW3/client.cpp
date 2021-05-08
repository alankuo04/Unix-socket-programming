#include<netinet/in.h>
#include<sys/socket.h>
#include<arpa/inet.h>
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<string.h>

#include<iostream>
#include<string>
#include<vector>

#define QLEN 32
#define BUFSIZE 4096

using namespace std;

int main(int argc, char* argv[]){
    struct sockaddr_in srv;

    int fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    int n_bytes;
    char buf[BUFSIZE];
    string mode, ip, port, username;
    bool isLink = false;

    cout<<endl<<"Welcome to the online chatting service."<<endl;
    while(1){
        cout<<endl;
        cout<<"==========MODE========="<<endl;
        cout<<"1) login (ip) (port) (username)"<<endl;
        cout<<"2) chat (username) (message)"<<endl;
        cout<<"3) logout"<<endl;
        cout<<"4) other (refresh)."<<endl;
        cout<<"========================"<<endl;
        cout<<"\e[0;32m[Client]$ \e[0m";

        cin>>mode;
        if(mode=="login" || mode=="chat" || mode=="logout"){
            if(!isLink){
                if(mode=="login"){
                    cin>>ip>>port>>username;
                    srv.sin_family = AF_INET;
                    srv.sin_port = htons(uint16_t(stoi(port)));
                    srv.sin_addr.s_addr = inet_addr(ip.c_str());

                    if(connect(fd, (struct sockaddr*)&srv, sizeof(srv)) < 0){
                        perror("connect");
                        exit(1);
                    }
                    strcpy(buf, ("login "+username).c_str());
                    n_bytes = write(fd, buf, sizeof(buf));
                    if(n_bytes < 0){
                        perror("write");
                        exit(1);
                    }
                    cout<<"The server with IP address \""+ip+"\" has accepted your connection."<<endl;
                    isLink = true;
                }
                else{
                    cout<<"Please connect to the server first."<<endl;
                }
            }
            else{
                if(mode=="login"){
                    cout<<"The connect is already linked."<<endl;
                }
                else if(mode=="chat"){
                    string user[QLEN], message;
                    string temp;
                    int i;
                    for(i=0;i<QLEN;i++){
                        cin>>temp;
                        if(temp[0] == '\"'){
                            message = temp;
                            break;
                        }
                        user[i]=temp;
                    }
                    for(int j=0;j<i;j++){
                       strcpy(buf, ("chat "+user[j]+" "+message).c_str());
                       n_bytes = write(fd, buf, sizeof(buf));
                       if(n_bytes < 0){
                           perror("write");
                           exit(1);
                       }
                    }
                }
                else if(mode=="logout"){
                    cout<<"Bye bye."<<endl;
                    strcpy(buf, ("logout "+username).c_str());
                    n_bytes = write(fd, buf, sizeof(buf));
                    if(n_bytes < 0){
                        perror("write");
                        exit(1);
                    }
                    shutdown(fd, SHUT_RDWR);
                    break;
                }
            }
        }
        else{
            if(isLink){
                strcpy(buf, "trash");
                n_bytes = write(fd, buf, sizeof(buf));
                if(n_bytes < 0){
                    perror("write");
                    exit(1);
                }
            }
        }
        if(isLink){
            int cc;
            while(cc = read(fd, buf, sizeof(buf))){
                if(cc < 0)
                    perror("read");
                if(string(buf)=="end"){
                    break;
                }
                if(string(buf)=="bad login"){
                    cout<<"Log into the online chat failed."<<endl;
                    shutdown(fd, SHUT_RDWR);
                    exit(1);
                }
                printf("%s\n", buf); 
            }
        }
    }
}