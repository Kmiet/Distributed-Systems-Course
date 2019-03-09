CC = gcc -Wall
Opt = -O0

main: protocol.c protocol.o client.c client.o queue.c queue.o
	$(CC) $(Opt) client.o protocol.o queue.o -pthread -o client

protocol.o: protocol.c
	$(CC) $(Opt) -c protocol.c

queue.o: queue.c
	$(CC) $(Opt) -c queue.c

client.o: client.c
	$(CC) $(Opt) -c client.c

clear:
	rm *.o *.exe