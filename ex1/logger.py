from socket import *
from ctypes import *
import struct

PACKET_SIZE = 68
MULT_IP = '224.0.0.1'
MULT_PORT = 5001

class MsgStruct(Structure):
  _fields_ = [
    ('author', c_char * 16),
    ('content', c_char * 32)
  ]

class TokenStruct(Structure):
  _fields_ = [
    ('id', c_char * 16),
    ('msg', MsgStruct)
  ]

class PacketStruct(Structure):
  _fields_ = [
    ('type', c_int),
    #('sender_addr', c_char * 16),
    #('sender_port', c_int),
    ('token', TokenStruct)
  ]

if __name__ == '__main__':
  sock = socket(AF_INET, SOCK_DGRAM) #, IPPROTO_UDP)
  #sock.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
  sock.bind(('', MULT_PORT))
  #group = inet_aton(MULT_IP)
  #mreq = struct.pack('4sL', group, INADDR_ANY)
  #sock.setsockopt(IPPROTO_IP, IP_ADD_MEMBERSHIP, mreq)

  while True:
    packet, addr = sock.recvfrom(PACKET_SIZE)
    p = PacketStruct.from_buffer(bytearray(packet))
    print('Client ' + p.token.msg.author.decode('utf-8') + ' has acquired token: ' + p.token.id.decode('utf-8'))