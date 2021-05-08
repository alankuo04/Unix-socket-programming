#ifndef __CODED__
#define __CODED__


#include<string>
#include<vector>
#include<tuple>


std::tuple<int, int, std::string, std::string> fixed_length_encode(std::string origin, int char_count[]);
std::string fixed_length_decode(std::string encoded, std::string code_list, int length, int padding);

bool compare(std::pair<int,std::string> a, std::pair<int,std::string> b);
std::tuple<int, std::string, std::vector<std::string>> huffman_encode(std::string origin, int char_count[]);
std::string huffman_decode(std::string encoded, std::vector<std::string> code_list, int padding);

#endif