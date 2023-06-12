#pragma once
#include <vector>
#include <map>
#include "../include/ConnectionHandler.h"
#include "../include/event.h"
#include "game.h"
using std::string;
// TODO: implement the STOMP protocol
class StompProtocol
{
private:
  int receiptId;
  int subscribtionId;
  std::map<string,int> channle_to_id; 
  string username; 
  std::map<string,std::vector<game>> users_report;
  bool connec=false;
  std::map<int,string> recipt_ID_TO_action;
public:
  void setBol(bool ans);
  bool getConnec();
  virtual ~StompProtocol();
  StompProtocol();
  string procces(string command,string line,ConnectionHandler &con);
  string connect(string line);
  string logout(int receipt);
  string subscribe(string line,int sunscribtionId,int receipt);
  string unsubscribe(string line,int receipt);
  string eventer(Event update);
  std::vector<Event> send(string line);
  string summary(string line);
  void reports(string line,ConnectionHandler &con);
  void getreport(string report);
  void report_for_channle(string channle, string report);
  void getProcces(string msg);
  bool check_if_first_report(string user,string channle);
};
