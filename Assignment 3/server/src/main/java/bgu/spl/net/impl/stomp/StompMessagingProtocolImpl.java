package bgu.spl.net.impl.stomp;

import java.util.HashMap;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl implements MessagingProtocol<String>{

    private Connections<String> Connections;
    private int connection_Id;
    private String UserName;
    private HashMap<String,String> subscribtions;
    private volatile boolean shouldTerminate = false;
    private volatile boolean loggedIn = false;
    private String[] legalCommands = {"CONNECT","SEND","SUBSCRIBE","UNSUBSCRIBE","DISCONNECT"};

    @Override
    public String process(String msg) {
        HashMap<String,String> headers = new HashMap<String,String>();
        String[] msgSplit = msg.split("\n");
        for(int i = 1 ; i < msgSplit.length ; i++){
            if(msgSplit[i].equals("") || msgSplit[i].equals("\u0000")){
                break;
            }
            if(msgSplit[i].contains(":")){
                String[] lineSplit = msgSplit[i].split(":");
                if(lineSplit.length == 2){
                    headers.put(lineSplit[0], lineSplit[1]);
                }
            }
        }
        // to check the command
        if(!legalCommand(msgSplit[0])){
            disconnect();
            return illegalCommandError(msg, headers);
        }

        switch (msgSplit[0]){
            case "CONNECT" : {
                if(loggedIn){
                    disconnect();
                    return clientLoggedInError(msg);
                }
                if(!headers.containsKey("accept-version")){
                    disconnect();
                    return missingHeaderError(msg, headers, "accept-version");
                }
                if(!headers.containsKey("host")){
                    disconnect();
                    return missingHeaderError(msg, headers, "host");
                }
                if(!headers.containsKey("login")){
                    disconnect();
                    return missingHeaderError(msg, headers, "login");
                }
                if(!headers.containsKey("passcode")){
                    disconnect();
                    return missingHeaderError(msg, headers, "passcode");
                }
                String userName = headers.get("login");
                String password = headers.get("passcode");

                if(Connections.isLoggedIn(userName)){
                    disconnect();
                    return userLoggedInError(msg);
                }
                if(Connections.accountExists(userName)){
                    if(!Connections.legalPassword(userName, password)){
                        disconnect();
                        return passwordError(msg);
                    }
                    this.UserName = userName;
                    Connections.Login(userName, password);
                    loggedIn = true;
                    Connections.connect(connection_Id);
                    return ConnectedFrame();

                }
                else{
                    this.UserName = userName;
                    Connections.createAccount(userName, password);
                    Connections.Login(userName, password);
                    loggedIn = true;
                    Connections.connect(connection_Id);
                    return ConnectedFrame();
                }
            }

            case "SUBSCRIBE" : {
                if(!loggedIn){
                    disconnect();
                    return notLoggedInError(msg,headers);
                }
                if(!headers.containsKey("destination")){
                    disconnect();
                    return missingHeaderError(msg, headers, "destination");
                }
                if(!headers.containsKey("id")){
                    disconnect();
                    return missingHeaderError(msg, headers, "id");
                }
               
                String id = headers.get("id");
                String destination = headers.get("destination");
                if(subscribtions.containsKey(id)){
                    disconnect();
                    return idExistsError(msg, headers);
                }
                if(Connections.channelExists(destination)){
                    subscribtions.put(id, destination);
                    Connections.subscribe(destination, connection_Id, id);
                }else{
                    Connections.createChannel(destination);
                    subscribtions.put(id, destination);
                    Connections.subscribe(destination, connection_Id, id);
                }
                if(headers.containsKey("receipt")){
                   
                    return receiptFrame(headers.get("receipt"));
                }

                break;
            }


            case "UNSUBSCRIBE" : {
                if(!loggedIn){
                    disconnect();
                    return notLoggedInError(msg,headers);
                }
                if(!headers.containsKey("id")){
                    disconnect();
                    return missingHeaderError(msg, headers, "id");
                }
                String id = headers.get("id");
                if(!subscribtions.containsKey(id)){
                    disconnect();
                    return unsubscribeError(msg, headers);
                }
                String channel = subscribtions.get(id);
                Connections.unsubscribe(channel, connection_Id);
                subscribtions.remove(id);
                return receiptFrame(headers.get("receipt"));
            }

            case "SEND" : {
                if(!loggedIn){
                    disconnect();
                    return notLoggedInError(msg,headers);
                }
                if(!headers.containsKey("destination")){
                    disconnect();
                    return missingHeaderError(msg, headers, "destination");
                }
                String destination = headers.get("destination");
                if(!subscribtions.containsValue(destination)){
                    disconnect();
                    return sendError(msg, headers);
                }
                HashMap<Integer,String> subscribers = Connections.subscribersByChannel(destination);
                String[] body = msg.split("\n\n");
                if(body.length == 2){
                    int messageId = Connections.messageId();
                    String messageFrame = sendFrame(body[1], destination, subscribers.get(connection_Id), Integer.toString(messageId));
                    Connections.send(destination, messageFrame);
                }else if(body.length == 1){
                    int messageId = Connections.messageId();
                    String messageFrame = sendFrame("", destination, subscribers.get(connection_Id), Integer.toString(messageId));
                    Connections.send(destination, messageFrame);
                }
                if(headers.containsKey("receipt")){
                    return receiptFrame(headers.get("receipt"));
                }
                break;
            }

            case "DISCONNECT" :{
                if(!loggedIn){
                    disconnect();
                    return notLoggedInError(msg,headers);
                }
                disconnect();
                if(headers.containsKey("receipt")){
                    return receiptFrame(headers.get("receipt"));
                }
                break;
            }
            
        } 
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    //@Override
    public void start(int connectionId, Connections<String> connections) {
        this.connection_Id = connectionId;
        this.Connections = connections;

        //Connections.connect(connection_Id);
        subscribtions = new HashMap<String,String>();
        UserName = "";
        loggedIn = false;
    }

    private boolean legalCommand(String command){
        for (String legalCommand : legalCommands) {
            if(legalCommand.equals(command)){
                return true;
            }
        }
        return false;
    }

    private String illegalCommandError(String msg, HashMap<String,String> headers){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt");
        }
        error = error + "message: illegal command" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg+ "-----" + '\n';
        error = error + "The command is illegal, you can only use CONNECT, SEND, SUBSCRIBE, UNSUBSCRIBE or DISCONNECT !" + '\n';
        return error;
    }

    private String missingHeaderError(String msg, HashMap<String,String> headers, String header){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt");
        }
        error = error + "message: malformed frame received" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg+ "-----" + '\n';
        error = error + "Did not contain a "+ header+ " header, which is REQUIRED for message propagation." + '\n';
        return error;
    }

    private String userLoggedInError(String msg){
        String error = "ERROR"+'\n';
        error = error + "message: User already logged in" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "User already logged in, you have to try another user name to continue." + '\n';
        return error;
    }

    private String notLoggedInError(String msg, HashMap<String,String> headers){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt") + '\n';
        }
        error = error + "message: you are not logged in yet" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "You have to login first before doing this action." + '\n';
        return error;
    }

    private String clientLoggedInError(String msg){
        String error = "ERROR"+'\n';
        error = error + "message: Client already logged in" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg+ "-----" + '\n';
        error = error + "Client already logged in, you can login only with one user." + '\n';
        return error;
    }

    private String passwordError(String msg){
        String error = "ERROR"+'\n';
        error = error + "message: Wrong password" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "Wrong password, try agian with another password that matches the user name." + '\n';
        return error;
    }

    private String ConnectedFrame(){
        String frame = "CONNECTED" + '\n' + "version:1.2" + '\n' + '\n';
        return frame;
    }

    private void disconnect(){
        shouldTerminate = true;
        if(Connections.isLoggedIn(UserName)){
            Connections.LogOut(UserName);
            loggedIn = false;
        }
        subscribtions.clear();
        if(Connections.isConnected(connection_Id)){
            Connections.disconnect(connection_Id);
        }
    }

    private String idExistsError(String msg, HashMap<String,String> headers){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt") + '\n';
        }
        error = error + "message: id is already exists" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "This id number has been chosen, you cant use it twice." + '\n';
        return error;
    }

    private String receiptFrame(String id){
        String frame = "RECEIPT" + '\n' + "receipt-id:" + id + '\n' + '\n';
        return frame;
    }

    private String unsubscribeError(String msg, HashMap<String,String> headers){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt") + '\n';
        }
        error = error + "message: id does not exist" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "This user has not used this id number as an id subscribtion." + '\n';
        return error;
    }

    private String sendError(String msg, HashMap<String,String> headers){
        String error = "ERROR"+'\n';
        if(headers.containsKey("receipt")){
            error = error +"receipt-id: " + headers.get("receipt") + '\n';
        }
        error = error + "message: not subscribed to this topic" + '\n' + "The message:" + '\n' + "-----" + '\n';
        error = error + msg + "-----" + '\n';
        error = error + "This user has not subscribed to this topic." + '\n';
        return error;
    }

    private String sendFrame(String msg, String destination, String id, String messageId){
        String frame = "MESSAGE" + '\n';
        frame = frame + "subscribtion:" + id + '\n';
        frame = frame + "messageId:" + messageId + '\n';
        frame = frame + "destination:" + destination + '\n' + '\n';
        frame = frame + msg + '\n';
        return frame;
    }
}
