TARGET=server
TARGET2=client
TARGET3=coded
VERSION=-std=c++11

all:
	g++ $(VERSION) -c $(TARGET3).cpp -o $(TARGET3).o
	g++ $(VERSION) $(TARGET).cpp -o $(TARGET) $(TARGET3).o
	g++ $(VERSION) $(TARGET2).cpp -o $(TARGET2) $(TARGET3).o
clean:
	rm $(TARGET) $(TARGET2) $(TARGET3).o