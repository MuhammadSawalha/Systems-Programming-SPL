#include "ConnectionHandler.h"
#include "StompProtocol.h"
class Reader{
    public:
        void run();
        Reader(ConnectionHandler *c, StompProtocol *p);
    private:
        ConnectionHandler *handler;
        StompProtocol *protocol;
};