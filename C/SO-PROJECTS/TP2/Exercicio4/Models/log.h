#ifndef __LOG_H__
#define __LOG_H__

typedef enum { INFO, ERROR, DEBUG } LOG_LEVEL;

int log_init(const char *pathname);
int log_message(LOG_LEVEL level, const char *msg);
int log_message_with_end_point(LOG_LEVEL level, const char *msg, int sock);
int log_close();

#endif