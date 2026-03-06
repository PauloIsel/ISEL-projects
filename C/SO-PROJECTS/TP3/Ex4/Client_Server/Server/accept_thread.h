#ifndef __ACCEPT_THREAD_H__
#define __ACCEPT_THREAD_H__

void* accept_thread_tcp(void *args);
void* accept_thread_unix(void *args);

#endif