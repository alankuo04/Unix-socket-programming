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
#include<iomanip>
#include"coded.h"
using namespace std;

string huffman(string, int[]);

int main(){
    struct sockaddr_in srv;

    int fd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    int n_bytes;

    string mode, ip, port;
    char buf[512];
    bool isLink = false;
    
    while(1){
        // user interface with color that look more comfortable
        cout<<"\e[0;32m[Client]$ \e[0m";

        cin>>mode;
        // check the command is valid or invalid
        if(mode=="link" || mode=="send" || mode=="leave"){
            // user should link to the server first
            if(!isLink){
                if(mode=="link"){
                    cin>>ip>>port;
                    srv.sin_family = AF_INET;
                    srv.sin_port = htons(uint16_t(stoi(port)));
                    srv.sin_addr.s_addr = inet_addr(ip.c_str());

                    if(connect(fd, (struct sockaddr*) &srv, sizeof(srv)) < 0){
                        perror("connect");
                        exit(1);
                    }
                    cout<<"The server with IP address \""<<ip<<"\" has accepted your connection."<<endl;
                    isLink = true;
                }
                else{
                    cout<<"Please connect to the server first."<<endl;
                }
            }
            // other valid command
            else{
                if(mode=="link"){
                    cout<<"The connect is already linked."<<endl;
                }
                else if(mode=="send"){
                    cin>>buf;
                    fstream file(buf, ios::in|ios::binary);
                    if(!file){
                        perror("Can't open the file.");
                    }
                    else{
                        char c;
                        string origin_file = "";
                        string new_file;
                        int char_count[256] = {0};
                        while (file.get(c)){
                            origin_file += c;
                            char_count[(unsigned char)(c)]++;
                        }
                        int padding;
                        string compressed_file;
                        vector<string> code_list(256, "");
                        tie(padding, compressed_file, code_list) = huffman_encode(origin_file, char_count);
                        string code_list_string="";
                        for(int i=0;i<code_list.size();i++){
                            code_list_string += code_list[i];
                            code_list_string += "\n";
                        }
                        //cout<<code_list_string;
                        strcpy(buf, (string(buf)+"\n"+to_string(compressed_file.size())).c_str());
                        strcpy(buf, (string(buf)+"\n"+to_string(origin_file.size())).c_str());
                        strcpy(buf, (string(buf)+"\n"+to_string(code_list_string.size())).c_str());
                        strcpy(buf, (string(buf)+"\n"+to_string(padding)).c_str());
                        int temp_bytes = 0;
                        bool first_block = true;
                        cout<<buf<<endl;
                        while(1){
                            // send the data information first
                            if(first_block){
                                n_bytes = write(fd, buf, sizeof(buf));
                                temp_bytes += n_bytes;
                                if(n_bytes < 0){
                                    perror("write");
                                    exit(1);
                                }
                                else{
                                    if(temp_bytes==512){
                                        temp_bytes=0;
                                        first_block=false;
                                    }
                                }
                            }
                            else{
                                n_bytes = write(fd, compressed_file.c_str(), compressed_file.size());
                                temp_bytes += n_bytes;
                                n_bytes = write(fd, code_list_string.c_str(), code_list_string.size());
                                temp_bytes += n_bytes;
                                if(n_bytes < 0){
                                    perror("write");
                                    exit(1);
                                }
                                else{
                                    if((long unsigned int)temp_bytes==compressed_file.size()+code_list_string.size()){
                                        time_t timer;
                                        struct tm *upload_time;
                                        char time_buffer[20];
                                        time(&timer);
                                        upload_time = localtime(&timer);
                                        strftime (time_buffer,20,"%Y/%m/%d %R",upload_time);
                                        cout<<"Original file length: "<<origin_file.size()<<" bytes, compressed file length: "<<compressed_file.size();
                                        cout<<" bytes (ratio: "<<(float(compressed_file.size())/float(origin_file.size()))*100<<"%)"<<endl;
                                        cout<<"Time to upload: "<<time_buffer<<endl;
                                        first_block=true;
                                        temp_bytes=0;
                                        break;
                                    }
                                }
                            }
                            //cout<<buf.size()<<endl<<origin_file.size()<<endl;
                        }
                        
                    }
                }
                else if(mode=="leave"){
                    cout<<"Bye bye."<<endl;
                    shutdown(fd, SHUT_RDWR);
                    break;
                }
                
            }
        }
        else{
            cout<<"Unknown command."<<endl;
        }
    }
}

string huffman(string origin_file, int char_count[]){
    //cout<<origin_file.size()<<endl;
    return "1";
}