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
#include<vector>
#include"coded.h"
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
    int temp_bytes = 0, file_size, table_size, length, padding;
    bool first_block = true, is_code_list=false;
    fstream file, code_list_file, origin_file;
    string encoded_file, code_list_string;
    vector<string> code_list(256,"");

    while(1){
        if(first_block){
            read_data = "";
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
                    string file_name, size1, size2, size3, coded_file_name, code_list_name;
                    // compressed file size, origin file size, codelist size, length, padding
                    ss>>file_name>>size1>>size2>>size3>>length>>padding;
                    //cout<<file_name<<" "<<size1<<endl;
                    coded_file_name = file_name+"-coded";
                    code_list_name = file_name+"-code.txt";
                    //cout<<coded_file_name<<" "<<code_list_name<<endl;
                    file.open("./test/"+coded_file_name, ios::out|ios::binary);
                    code_list_file.open("./test/"+code_list_name, ios::out|ios::binary);
                    origin_file.open("./test/"+file_name, ios::out|ios::binary);
                    temp_bytes=0;
                    first_block=false;
                    file_size=stoi(size1);
                    table_size=stoi(size3);
                    if(length==0)
                        cout<<"The client sends a file \""+file_name+"\" with size of "+size2+" bytes. The Huffman coding data are stored in \""+coded_file_name+"\""<<endl;
                    else
                        cout<<"The client sends a file \""+file_name+"\" with size of "+size2+" bytes. The Fixed length coding data are stored in \""+coded_file_name+"\""<<endl;
                }
            }
        }
        else{
            //cout<<"now is file block"<<endl;
            n_bytes = read(newfd, buf, sizeof(buf));
            for(int i=0;i<n_bytes;i++){
                read_data += buf[i];
            }
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
                if(temp_bytes==(file_size+table_size)){
                    encoded_file = read_data.substr(0,file_size);
                    code_list_string = read_data.substr(file_size, table_size);
                    first_block=true;
                    temp_bytes=0;
                    file.write(encoded_file.c_str(), encoded_file.size());
                    file.close();
                    code_list_file.write(code_list_string.c_str(), code_list_string.size());
                    code_list_file.close();

                    string decoded_file;
                    if(length!=0){
                        decoded_file = fixed_length_decode(encoded_file, code_list_string, length, padding);
                    }
                    else{
                        stringstream cs(code_list_string);
                        string temp;
                        vector<string> code_vec;
                        while(getline(cs, temp))
                            code_vec.push_back(temp);
                        decoded_file = huffman_decode(encoded_file, code_vec, padding);
                    }
                    origin_file.write(decoded_file.c_str(), decoded_file.size());
                    origin_file.close();
                }
            }
        }
        //cout<<"temp_bytes:"<<temp_bytes<<endl;
        //cout<<buf<<endl;
    }
}