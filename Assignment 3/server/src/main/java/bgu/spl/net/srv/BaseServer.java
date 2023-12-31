package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<MessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private final Connections<T> connections;
    private ServerSocket sock;
    

    public BaseServer(
            int port,
            Connections<T> connections,
            Supplier<MessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.connections = connections;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            int counterId = 1;

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();

                MessageEncoderDecoder<T> messageEncoderDecoder = encdecFactory.get();
                MessagingProtocol<T> messagingProtocol = protocolFactory.get();
                //messagingProtocol.start(counterId, connections);
                

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        messageEncoderDecoder,
                        messagingProtocol , connections , counterId);

                connections.addUnstartedId(counterId, handler);
                counterId++;

                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
