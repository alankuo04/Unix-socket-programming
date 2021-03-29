#include<netinet/in.h>
#include<sys/socket.h>
#include<arpa/inet.h>
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>
#include<string.h>
#include<string>
#include<iostream>
#include<fstream>
#include<sstream>
#define PORT 50005
using namespace std;

int main(){
    struct sockaddr_in srv;
    struct sockaddr_in cli;
    

    int fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    int newfd;
    unsigned int cli_len = sizeof(cli);
    char buf[512];
    int n_bytes;

    srv.sin_family = AF_INET;
    srv.sin_port = htons(PORT);
    srv.sin_addr.s_addr = htonl(INADDR_ANY);
    
    cout<<"\e[0;32m[Server]$ \e[0mWaiting for client connecting."<<endl;
    if(bind(fd, (struct sockaddr*) &srv, sizeof(srv)) < 0){
        perror("bind");
        exit(1);
    }
    if(listen(fd, 5) < 0){
        perror("listen");
        exit(1);
    }
    newfd = accept(fd, (struct sockaddr*) &cli, &cli_len);
    if(newfd < 0){
        perror("accept");
        exit(1);
    }
    cout<<"A client \""<<inet_ntoa(cli.sin_addr)<<"\" has connected via port num "<<ntohs(cli.sin_port)<<" using SOCK_STREAM (TCP)"<<endl;

    string read_data;
    int temp_bytes = 0, file_size;
    bool first_block = true;
    fstream file;

    while(1){
        if(first_block){
            //cout<<"first block"<<endl;
            n_bytes = read(newfd, buf, sizeof(buf));
            temp_bytes += n_bytes;
            if(n_bytes < 0){
                perror("read");
                exit(1);
            }
            else if(n_bytes == 0){
                cout<<"The client \""<<inet_ntoa(cli.sin_addr)<<"\" with port "<<ntohs(cli.sin_port)<<" has terminated the connection."<<endl;
                shutdown(fd, SHUT_RDWR);
                break;
            }
            else{
                if(temp_bytes==512){
                    stringstream ss((string(buf)));
                    string file_name, size, table_size, coded_name;
                    ss>>file_name>>size;
                    //cout<<file_name<<" "<<file_size<<endl;
                    file.open("./test/"+file_name, ios::out|ios::binary);
                    temp_bytes=0;
                    first_block=false;
                    file_size=stoi(size);
                    coded_name = string(file_name).replace(file_name.find_last_of("."), 1, "-coded.");
                    cout<<"The client sends a file \""+file_name+"\" with size of "+size+" bytes. The Huffman coding data are stored in \""+coded_name+"\""<<endl;
                }
            }
        }
        else{
            //cout<<"now is file block"<<endl;
            n_bytes = read(newfd, buf, sizeof(buf));
            //cout<<n_bytes<<endl;
            temp_bytes += n_bytes;
            if(n_bytes < 0){
                perror("read");
                exit(1);
            }
            else if(n_bytes == 0){
                cout<<"The client \""<<inet_ntoa(cli.sin_addr)<<"\" with port "<<ntohs(cli.sin_port)<<" has terminated the connection."<<endl;
                shutdown(fd, SHUT_RDWR);
                break;
            }
            else{
                file.write(buf, n_bytes);
                if(temp_bytes==file_size){
                    temp_bytes=0;
                    first_block=true;
                    file.close();
                }
            }
        }
        //cout<<"temp_bytes:"<<temp_bytes<<endl;
        //cout<<buf<<endl;
    }
}