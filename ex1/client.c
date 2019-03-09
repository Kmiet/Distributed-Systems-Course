#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <pthread.h>
#include "protocol.h"
#include "queue.h"

pthread_mutex_t input_mutex;
pthread_mutex_t token_mutex;

pthread_t inet_in_thread;
pthread_t inet_out_thread;
pthread_t input_thread;

struct sockaddr_in out_address;
char host[INET_ADDR_LEN];
int out_port;
int out_socket;

struct sockaddr_in in_address;
int in_port;
int in_socket;

int mode;

char username[CLIENT_NAME_LEN];
int hasToken;

token *tkn;

queue* forwardQue;
queue* inputQue;

void* inet_in_handler(void* args) {
  int in_fd = in_socket;
  struct sockaddr_in peer_addr;
  int peer_addr_len = sizeof(peer_addr);
  int in_addr_len = sizeof(in_address);
  if(mode == SOCK_STREAM) {
    in_fd = accept(in_socket, (struct sockaddr *) &peer_addr, &peer_addr_len);
  }  
  char buffer[MSG_LENGTH];
  int bytes_read;
  
  packet p;
  
  while(1) {
    //bytes_read = recv(in_fd, buffer, MSG_LENGTH, MSG_WAITALL);
    bytes_read = recvfrom(in_fd, &p, PACKET_SIZE, 0, NULL, NULL);
    //bytes_read = recvfrom(in_fd, &p, PACKET_SIZE, MSG_WAITALL, (struct sockaddr *) &peer_addr, &peer_addr_len);
    if(bytes_read > 0) {
      printf("RECV \r\n");
      if(p.type == PCK_TOKEN_MSG) {
        if(strcmp(p.token.msg.author, username) != 0) {
          msg *m = malloc(sizeof(msg));
          strcpy(m->author, p.token.msg.author);
          strcpy(m->content, p.token.msg.content);
          enque(forwardQue, m);
          printf("%s: %s", m->author, m->content);
        }
      } else if (p.type == PCK_TOKEN_PASS) {
        pthread_mutex_lock(&token_mutex);
        strcpy(tkn->id, p.token.id);
        hasToken = 1;
        pthread_mutex_unlock(&token_mutex);
      }
    }
  }
}

void* inet_out_handler(void* args) {
  if(mode == SOCK_STREAM) {
    int connected = -1;
    while(connected == -1) {
      sleep(3);
      connected = connect(out_socket, (const struct sockaddr *) &out_address, sizeof(struct sockaddr_in));
    } 
    printf("Connected\r\n");
  }
  
  time_t token_time;
  clock_t begin, end;
  token_time = 0;

  int out_addr_len = sizeof(out_address);
  packet *p = malloc(sizeof(packet));

  while(1) {
    pthread_mutex_lock(&token_mutex);
    p->type = PCK_TOKEN_MSG;
    begin = clock();
    while(hasToken == 1) {
      while(emptyQue(forwardQue) != 0) {
        msg* m = (msg *) deque(forwardQue);
        tkn->msg = *m;
        p->token = *tkn;
        sendto(out_socket, p, PACKET_SIZE, 0, (const struct sockaddr *) &out_address, out_addr_len);
        free(m);
      }
      while(emptyQue(inputQue) != 0) {
        pthread_mutex_lock(&input_mutex);
        msg* m = (msg *) deque(inputQue);
        pthread_mutex_unlock(&input_mutex);
        tkn->msg = *m;
        p->token = *tkn;
        sendto(out_socket, p, PACKET_SIZE, 0, (const struct sockaddr *) &out_address, out_addr_len);
        printf("Me: %d", sizeof(p));
        free(m);
      }
      end = clock();
      token_time = (end - begin) / CLOCKS_PER_SEC;
      if(token_time > TOKEN_LEASE_TIME) {
        hasToken = 0;
        p->type = PCK_TOKEN_PASS;
        int x = sendto(out_socket, p, PACKET_SIZE, 0, (const struct sockaddr *) &out_address, out_addr_len);
        printf("tk: %d", x);
      }
    }
    pthread_mutex_unlock(&token_mutex);
  }
}

void* input_handler(void* args) {
  char* buffer = NULL;
  size_t buffer_size = 0;
  int bytes_read;
  int out_addr_len = sizeof(out_address);

  while(1) {
    bytes_read = getline(&buffer, &buffer_size, stdin);
    if(bytes_read != 0 && bytes_read < MSG_LENGTH) {
      /**/
      msg *m = malloc(sizeof(msg));
      strcpy(m->author, username);
      strcpy(m->content, buffer);
      pthread_mutex_lock(&input_mutex);
      enque(inputQue, m);
      pthread_mutex_unlock(&input_mutex);
      /*
      int x = sendto(out_socket, buffer, bytes_read, 0, (const struct sockaddr *) &out_address, out_addr_len);
      if(x == -1) {
        printf("Nothing sent\r\n");
        exit(1);
      }
      printf("X: %d", x);
      */
    }
  }
}

void exit_handler() {
  shutdown(in_socket, SHUT_RDWR);
  close(out_socket);
  close(in_socket);
  pthread_mutex_destroy(&token_mutex);
  pthread_mutex_destroy(&input_mutex);
  free(inputQue);
  free(forwardQue);
}

int main(int argc, char** argv) {
  if(argc < 6 ||  argc > 7) {
    printf("Usage: ./client <name> <local_port> <peer_client_address> <peer_client_port> <protocol> [hasToken 0|1] \r\n");
    exit(1);
  }

  if(strlen(argv[1]) > CLIENT_NAME_LEN) {
    printf("Username is too long \r\n");
    exit(1);
  }

  strcpy(username, argv[1]);
  in_port = atoi(argv[2]);
  in_address.sin_family = AF_INET; 
	in_address.sin_addr.s_addr = htonl(INADDR_ANY);
	in_address.sin_port = htons(in_port);

  strcpy(host, argv[3]);
  out_port = atoi(argv[4]);
  out_address.sin_family = AF_INET; 
  out_address.sin_port = htons(out_port);
  out_address.sin_addr.s_addr = htonl(INADDR_ANY);

  if(strcmp(argv[5], "TCP") == 0 || strcmp(argv[5], "tcp") == 0) {
    mode = SOCK_STREAM;
  } else if(strcmp(argv[5], "UDP") == 0 || strcmp(argv[5], "udp") == 0) {
    mode = SOCK_DGRAM;
  } else {
    printf("Could not recognize requested protocol. Choose between TCP or UDP \r\n");
    exit(1);
  }

  if(mode == SOCK_STREAM) {
    if(inet_pton(AF_INET, host, &out_address.sin_addr)==0){
      printf("Wrong host address \r\n");
      exit(1);
    }
  }

  tkn = malloc(sizeof(token));
  if(argc == 7 && atoi(argv[6]) == 1) {
    hasToken = 1;
    strcpy(tkn->id, "ALAMAKOTA");
  }

  if(atexit(exit_handler) != 0) {
    printf("Could not set up an exit handler \r\n");
    exit(1);
  }

  in_socket = socket(AF_INET, mode, 0);
  if(in_socket == -1) {
    printf("Could not create in_socket \r\n");
    exit(1);
  }

  if(bind(in_socket, (const struct sockaddr *) &in_address, sizeof(in_address)) < 0) {
    printf("Could not bind in_socket \r\n");
    exit(1);
  };

  if(mode == SOCK_STREAM) {
    if(listen(in_socket, CONN_LIMIT) != 0) {
      printf("Server cannot listen on inet socket \r\n");
      exit(1);
    }
  }

  out_socket = socket(AF_INET, mode, 0);
  if(out_socket == -1){
    printf("Could not create out_socket \r\n");
    exit(1);
  }

  forwardQue = initQue();
  inputQue = initQue();

  pthread_mutex_init(&input_mutex, NULL);
  pthread_mutex_init(&token_mutex, NULL);

  pthread_create(&inet_in_thread, NULL, inet_in_handler, NULL);
  pthread_create(&inet_out_thread, NULL, inet_out_handler, NULL);
  pthread_create(&input_thread, NULL, input_handler, NULL);

  pthread_join(input_thread, NULL);
  pthread_join(inet_out_thread, NULL);
  pthread_join(inet_in_thread, NULL);

  exit(0);
}