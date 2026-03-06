#ifndef __MENU_THREAD_H__
#define __MENU_THREAD_H__

#define IN_BUFFER 16
#define LOG_BUFFER 64

#include "server.h"
#include "log.h"

void* menu_thread_function();

#endif