package bgu.spl.net.srv;

import java.util.HashMap;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    void addUnstartedId(int connectionId, ConnectionHandler<T> connectionHandler);

    void connect(int connection_Id);

    void Login(String userName, String password);

    void LogOut(String userName);

    boolean isLoggedIn(String userName);

    void createAccount(String userName, String password);

    boolean accountExists(String userName);

    boolean legalPassword(String userName, String password);

    boolean channelExists(String channel);
    boolean isConnected(int connectionId);

    void createChannel(String channel);

    void subscribe(String channel, int connectionId, String subscribeId);

    void unsubscribe(String channel, int connectionId);

    int messageId();

    HashMap<Integer,String> subscribersByChannel(String channel);
}
