#ifndef __MY_QUEUE__
#define __MY_QUEUE__

typedef struct queNode queNode;

struct queNode {
  void* data;
  queNode* next; 
};

typedef struct queue {
  queNode* head;
  queNode* last;
} queue;

queue* initQue();
int emptyQue(queue* Q);
void enque(queue* Q, void* data);
void* deque(queue* Q);

#endif