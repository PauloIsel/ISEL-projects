CC = gcc
CFLAGS = -Wall -g
LDLIBS = -lm

BINS = test_thread_pool
SRCS = thread-pool.c thread-pool_test.c
DEPS = thread-pool.h

all: $(BINS)

$(BINS): thread-pool.o thread-pool_test.o
	$(CC) $(CFLAGS) -o $@ $^

%.o: %.c
	$(CC) $(CFLAGS) -c $<

clean:
	$(RM) $(BINS) *.o
