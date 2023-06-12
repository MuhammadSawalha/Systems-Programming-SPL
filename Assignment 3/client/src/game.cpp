#include "../include/game.h"
#include <iostream>
#include <string>
#include <map>
using std::string;
using std::map;

game::game(string team_a,string team_b) : team_a_name(team_a), team_b_name(team_b), general_states(), team_a_states(), team_b_states(), game_event_reports() {}
game::~game(){}

void game::update(string event_name,string time,string discription,map<string,string> general_game_update,map<string,string> team_a_update, map<string,string> team_b_update){
  for(map<string,string>::iterator i = general_game_update.begin(); i!= general_game_update.end();++i){
      general_states[i->first]=i->second;
  }
   for(map<string,string>::iterator i= team_a_update.begin(); i != team_a_update.end();++i){
      team_a_states[i->first]=i->second;
  }
    for(map<string,string>::iterator i= team_b_update.begin(); i != team_b_update.end();++i){
      team_b_states[i->first]=i->second;
  }
   string key=time+" - "+event_name;
   game_event_reports[key]=discription;

}

string game::summery(){
    string summery="";
    summery=summery+team_a_name+" vs "+team_b_name+"\n";
    summery=summery+"Game stats:\n";
    for(map<string, string>::iterator a = general_states.begin(); a!= general_states.end();++a){
		summery=summery+a->first.substr(4)+":"+a->second+'\n';
	}
    summery=summery+team_a_name+" states:\n";
    for(map<string, string>::iterator a = team_a_states.begin(); a!= team_a_states.end();++a){
		summery=summery+a->first.substr(4)+":"+a->second+'\n';
	}
    summery=summery+team_b_name+" states:\n";
    for(map<string, string>::iterator a = team_b_states.begin(); a!= team_b_states.end();++a){
		summery=summery+a->first.substr(4)+":"+a->second+'\n';
	}
    summery=summery+"Game event reports:\n";
    for(map<string, string>::iterator a = game_event_reports.begin(); a!= game_event_reports.end();++a){
		summery=summery+a->first.substr(1)+":\n\n "+a->second+"\n\n";
	}
    return summery;
    }





string game::get_game_name(){return team_a_name+"_"+team_b_name;}