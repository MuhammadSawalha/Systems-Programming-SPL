
#include <string>
#include <iostream>
#include <map>
using std::string;
using std::map;
class game{
    private:
    string team_a_name;
    string team_b_name;
    map<string,string> general_states;
    map<string,string> team_a_states;
    map<string,string> team_b_states;
    map<string,string> game_event_reports;
    public:
    game(string team_a,string team_b);
    virtual ~game();
    void update(string event_name,string time,string discription,map<string,string> general_game_update,map<string,string> team_a_update, map<string,string> team_b_update);
    string summery();
    string get_game_name();

};