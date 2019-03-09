#include <stdlib.h>
#include "queue.h"

queue* initQue() {
  queue* Q = (queue *)malloc(sizeof(queue));
  Q->head = NULL;
  Q->last = NULL;
  return Q;
}

int emptyQue(queue* Q) {
  if(Q->head == NULL) return 0;
  else return -1;
}

void enque(queue* Q, void* data) {
  queNode* node = (queNode *)malloc(sizeof(queNode));
  node->data = data;
  node->next = NULL;
  Q->last = node;
  if(Q->head == NULL) Q->head = node;
}

void* deque(queue* Q) {
  if(Q->last == NULL) return NULL;
  queNode* node = Q->head;
  void* data = node->data;
  Q->head = node->next;
  free(node);
  return data;
}