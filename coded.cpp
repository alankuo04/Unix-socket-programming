#include<iostream>
#include<string>
#include<cmath>
#include<queue>
#include<vector>
#include<bitset>
#include<tuple>
#include<unordered_map>
#include<algorithm>
#include<fstream>
#include"coded.h"
using namespace std;

tuple<int, int, string, string> fixed_length_encode(string origin, int char_count[]){
    int count=0;
    int new_code[256]={0};
    string code_list="";
    for(int i=0;i<256;i++){
        if(char_count[i]!=0){
            count++;
            new_code[i]=count;
            code_list+=char(i);
        }
    }
    if(count>128)
        return {8, 0, origin, code_list};
    else{
        string new_byte_file="";
        //cout<<origin.size()<<endl;
        for(int i=0;i<origin.size();i++){
            if(new_code[(unsigned char)(origin[i])]!=0){
                size_t t=ceil(log(count)/log(2));
                string temp = bitset<8>(new_code[(unsigned char)(origin[i])]-1).to_string();
                new_byte_file+=temp.substr(8-t);
            }
        }
        int complement=(8-new_byte_file.size()%8)%8;
        for(int i=0;i<complement;i++)
            new_byte_file+="0";
        string new_file="";
        for(int i=0;i<new_byte_file.size();i+=8){
            new_file += char(stoi(bitset<8>(new_byte_file.substr(i,8)).to_string(), 0, 2));
        }
        //cout<<new_file.size()<<endl;
        return {ceil(log(count)/log(2)), complement, new_file, code_list};
    }
}

string fixed_length_decode(string encoded, string code_list, int length, int padding){
    string bytes="";
    for(int i=0;i<encoded.size();i++){
        bytes += bitset<8>(encoded[i]).to_string();
    }
    bytes = bytes.substr(0, bytes.size()-padding);
    string origin_file="";
    for(int i=0;i<bytes.size();i+=length){
        origin_file += code_list[stoi(bytes.substr(i,length), 0, 2)];
    }
    return origin_file;
}

bool compare(pair<int,string> a, pair<int,string> b){ return a.first>=b.first;}

tuple<int, string, vector<string>> huffman_encode(string origin, int char_count[]){
    priority_queue<pair<int, string>, vector<pair<int, string>>, decltype(&compare) > huffman_tree(&compare);
    for(int i=0;i<256;i++){
        if(char_count[i]!=0){
            //cout<<i<<" ";
            huffman_tree.push({char_count[i], string()+char(i)});
        }
    }
    //cout<<endl;
    vector<string> code_list(256, "");
    int size = huffman_tree.size();
    //cout<<size<<endl;
    for(int i=0;i<size, huffman_tree.size()>1;i++){
        pair<int, string> node_left, node_right;
        node_left = huffman_tree.top();
        huffman_tree.pop();
        node_right = huffman_tree.top();
        huffman_tree.pop();
        huffman_tree.push({node_left.first+node_right.first, node_left.second+node_right.second});
        for(int j=0;j<node_left.second.size();j++){
            code_list[(unsigned char)(node_left.second[j])] = "0"+code_list[(unsigned char)(node_left.second[j])];
            //cout<<code_list[node_left.second[j]]<<" "<<node_left.second<<endl;
        }
        for(int j=0;j<node_right.second.size();j++){
            code_list[(unsigned char)(node_right.second[j])] = "1"+code_list[(unsigned char)(node_right.second[j])];
            //cout<<code_list[node_right.second[j]]<<" "<<node_right.second<<endl;
        }
    }
    //cout<<huffman_tree[0].first<<" "<<huffman_tree[0].second<<endl;
    string new_byte_file="", new_file="";
    for(int i=0;i<origin.size();i++){
        new_byte_file += code_list[(unsigned char)(origin[i])];
    }
    int complement=(8-new_byte_file.size()%8)%8;
    for(int i=0;i<complement;i++)
        new_byte_file+="0";
    for(int i=0;i<new_byte_file.size();i+=8){
        new_file += char(stoi(bitset<8>(new_byte_file.substr(i,8)).to_string(), 0, 2));
    }
    //cout<<new_byte_file<<endl;
    return {complement, new_file, code_list};
}

string huffman_decode(string encoded, vector<string> code_list, int padding){
    string bytes="";
    for(int i=0;i<encoded.size();i++){
        bytes += bitset<8>(encoded[i]).to_string();
    }
    bytes = bytes.substr(0, bytes.size()-padding);
    //cout<<bytes<<endl;
    unordered_map<string, int> code_map;
    for(int i=0;i<256;i++){
        code_map[code_list[i]]=char(i);
    }
    string output="";
    for(int i=1;i<=bytes.size();i++){
        if(code_map.find(bytes.substr(0,i)) != code_map.end()){
            output+=code_map[bytes.substr(0,i)];
            bytes.erase(bytes.begin(), bytes.begin()+i);
            //bytes = bytes.substr(i,bytes.size());
            i=0;
            //cout<<bytes<<endl;
        }
    }
    //cout<<output<<endl;
    return output;
}

/*
int main(){
    string origin="";
    fstream file("123.jpg", ios::in|ios::binary);
    int char_count[256]={0};
    char temp;
    while(file.get(temp)){
        origin+=temp;
        char_count[(unsigned char)(temp)]++;
    }
    file.close();
    int a,b;
    string c,d;
    vector<string> e(256, "");
    //tie(a,b,c,d) = fixed_length_encode(origin, char_count);
    
    tie(b,c,e) = huffman_encode(origin, char_count);
    //cout<<a<<" "<<b<<" "<<c<<" "<<d<<endl;
    string output = huffman_decode(c,e,b);

    //string output = fixed_length_decode(c, d, a, b);
    cout<<c.size()<<" "<<output.size()<<endl;
    cout<<(float(c.size())/float(output.size()))*100<<" %"<<endl;
    //
    file.open("123-decoded.jpg", ios::out|ios::binary);
    file.write(output.c_str(), output.size());
    file.close();
    
}
*/