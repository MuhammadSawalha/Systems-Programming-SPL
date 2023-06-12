package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConnectionsImpl<T> implements Connections<T>{

    private final HashMap<Integer,ConnectionHandler<T>> Connected;
    private final HashMap<Integer,ConnectionHandler<T>> UnStarted;
    private final HashMap<String,String> LoggedIn;
    private final HashMap<String,String> Accounts;
    private final HashMap<String,HashMap<Integer,String>> Channels;
    private final HashMap<Integer,List<String>> Subscribtions;
    private final Object lock;
    private int messageCounter;

    public ConnectionsImpl(){
        Connected = new HashMap<Integer,ConnectionHandler<T>>();
        UnStarted = new HashMap<Integer,ConnectionHandler<T>>();
        LoggedIn = new HashMap<String,String>();
        Accounts = new HashMap<String,String>();
        Channels = new HashMap<String,HashMap<Integer,String>>();
        Subscribtions = new HashMap<Integer,List<String>>();
        lock = new Object();
        messageCounter = 0;
    }

    
    public boolean send(int connectionId, T msg){
        if(!Connected.containsKey((Integer)connectionId)){
            return false;
        }
        ConnectionHandler<T> client = Connected.get((Integer)connectionId);
        client.send(msg);
        return true;
    }

    public void send(String channel, T msg){
        synchronized(lock){
            HashMap<Integer,String> subscribers = Channels.get(channel);
            for (Integer subscriber : subscribers.keySet()) {
                send(subscriber, msg);               
            }
        } 
    }

    public void disconnect(int connectionId){
        synchronized(lock){
            List<String> channels = Subscribtions.get((Integer)connectionId);
            for (String channel : channels) {
                HashMap<Integer,String> subscribers = Channels.get(channel);
                subscribers.remove((Integer)connectionId);
            }
            Subscribtions.remove((Integer)connectionId);
            Connected.remove((Integer)connectionId);
        }
    }

    public void addUnstartedId(int connectionId, ConnectionHandler<T> connectionHandler){
        UnStarted.put((Integer)connectionId, connectionHandler);
    }

    public void connect(int connectionId){
        ConnectionHandler<T> connectionHandler = UnStarted.get((Integer)connectionId);
        Connected.put((Integer)connectionId, connectionHandler);
        UnStarted.remove(connectionId);
        List<String> channels = new ArrayList<>();
        Subscribtions.put(connectionId, channels);
    }

    public void Login(String userName, String password){
        LoggedIn.put(userName, password);
    }

    public void LogOut(String userName){
        if(LoggedIn.containsKey(userName)){
            LoggedIn.remove(userName);
        }
    }

    public boolean isLoggedIn(String userName){
        if(userName.equals("")) return false;
        if(LoggedIn.containsKey(userName)){
            return true;
        }
        return false;
    }
    public boolean isConnected(int connectionId){
        if(!Connected.containsKey((Integer)connectionId)){
            return false;
        }
        return true;
    }

    public void createAccount(String userName, String password){
        Accounts.put(userName, password);
    }

    public boolean accountExists(String userName){
        if(Accounts.containsKey(userName)){
            return true;
        }
        return false;
    }

    public boolean legalPassword(String userName, String password){
        String legalPassword = Accounts.get(userName);
        if(legalPassword.equals(password)){
            return true;
        }
        return false;
    }

    public boolean channelExists(String channel){
        if(Channels.containsKey(channel)){
            return true;
        }
        return false;
    }

    public void createChannel(String channel){
        HashMap<Integer,String> subscribers = new HashMap<Integer,String>();
        Channels.put(channel, subscribers);
    }
    
    public void subscribe(String channel, int connectionId, String subscribeId){
        synchronized(lock){
            HashMap<Integer,String> subscribers = Channels.get(channel);
            subscribers.put(connectionId, subscribeId);
            List<String> channels = Subscribtions.get((Integer)connectionId);
            channels.add(channel);
        }
    }

    public void unsubscribe(String channel, int connectionId){
        synchronized(lock){
            HashMap<Integer,String> subscribers = Channels.get(channel);
            subscribers.remove((Integer)connectionId);
    
            List<String> channels = Subscribtions.get((Integer)connectionId);
            channels.remove(channel);
        }
    }

    public int messageId(){
        synchronized(lock){
            messageCounter++;
            return messageCounter;
        }
    }

    public HashMap<Integer,String> subscribersByChannel(String channel){
        return Channels.get(channel);
    }

}
