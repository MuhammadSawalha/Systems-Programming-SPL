#include "Reader.h"
using namespace std;

void Reader::run(){
       while(protocol->getConnec()){
    	string answer;
		handler->getLine(answer);
		protocol->getProcces(answer);
    }
}

Reader::Reader(ConnectionHandler *c, StompProtocol *p): handler(c), protocol(p){}
