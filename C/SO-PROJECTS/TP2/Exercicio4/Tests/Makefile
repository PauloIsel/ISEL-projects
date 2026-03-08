CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = test_log

all: $(BINS)

test_log: test_log.o log.o error.o socket_utils.o
	$(CC) $(CFLAGS) -o $@ $^ $(LDLIBS)

log.o: ../Models/log.c ../Models/log.h ../Models/error.h
	$(CC) $(CFLAGS) -c $< -o $@

error.o: ../Models/error.c ../Models/error.h
	$(CC) $(CFLAGS) -c $< -o $@

socket_utils.o: ../Models/socket_utils.c ../Models/socket_utils.h
	$(CC) $(CFLAGS) -c $< -o $@

test_log.o: test_log.c ../Models/log.h ../Models/error.h ../Models/socket_utils.h
	$(CC) $(CFLAGS) -c $< -o $@

clean:
	rm -f $(BINS) *.o logfile.txt