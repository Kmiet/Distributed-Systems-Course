#ifndef __PROTOCOL_H__
#define __PROTOCOL_H__
#include "queue.h"

// Multicast
#define MULTICAST_ADDR "224.3.2.1"
#define MULTICAST_PORT 12345

// Client
#define CLIENT_NAME_LEN 16

// Host address length
#define INET_ADDR_LEN 16
#define INET6_ADDR_LEN 46
// Port numbers
#define DEFAULT_PORT 4000
// Conn limit
#define CONN_LIMIT 3

// Msg
#define MSG_LENGTH 32

typedef struct msg {
  char author[CLIENT_NAME_LEN];
  char content[MSG_LENGTH];
} msg;

// Token
#define TOKEN_LENGTH 16
#define TOKEN_LEASE_TIME 2

typedef struct token {
  char id[TOKEN_LENGTH];
  msg msg;
} token;

// Packet
#define PCK_DISCONNECT 0x00
#define PCK_CONNECT 0x01
#define PCK_TOKEN_PASS 0x02
#define PCK_TOKEN_MSG 0x03
#define PCK_TOKEN_ACQ 0x04

#define PACKET_SIZE 68

typedef struct packet {
  int type;
  token token;
} packet;

// TCP

// UDP


#endif