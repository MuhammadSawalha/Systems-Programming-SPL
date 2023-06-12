package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        String serverType = args[1];
        if(serverType.equals("tpc")){
            TPC_Server(port);
        }else if(serverType.equals("reactor")){
            Reactor_Server(port);
        }
        
    }
    private static void TPC_Server(int port){
        Server.threadPerClient(
                port, //port
                new ConnectionsImpl<String>(),
                () -> new StompMessagingProtocolImpl(), //protocol factory
                StompMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

    }
    private static void Reactor_Server(int port){
        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                port, //port
                new ConnectionsImpl<String>(),
                () -> new StompMessagingProtocolImpl(), //protocol factory
                StompMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();
        

    }
}
