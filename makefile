TARGET=server
TARGET2=client
TARGET3=coded
VERSION=-std=c++11

all:
	g++ $(VERSION) -c $(TARGET3).cpp -o $(TARGET3).o
	g++ -Wall $(VERSION) $(TARGET).cpp -o $(TARGET) $(TARGET3).o
	g++ -Wall $(VERSION) $(TARGET2).cpp -o $(TARGET2) $(TARGET3).o
	./$(TARGET)
clean:
	rm $(TARGET) $(TARGET2)