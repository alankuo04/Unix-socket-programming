all:
	g++ -Wall -std=c++11 server.cpp -o server
	g++ -Wall -std=c++11 client.cpp -o client
	./server
clean:
	rm server client