#include "../include/StompProtocol.h"
#include <iostream>
#include <fstream>
using std::cout;
using std::endl;
using std::map;
using std::string;
using std::vector;

StompProtocol::~StompProtocol() {}

StompProtocol::StompProtocol() : receiptId(1), subscribtionId(1), channle_to_id(), username(""), users_report(), connec(false), recipt_ID_TO_action() {}

void StompProtocol::setBol(bool ans)
{
	connec = ans;
}
bool StompProtocol::getConnec()
{
	return connec;
}

string StompProtocol::eventer(Event input)
{
	string msg = "SEND\ndestination:/" + input.get_team_a_name() + "_" + input.get_team_b_name() + "\n\n";
	msg = msg + "user: " + username + '\n';
	msg = msg + "team a: " + input.get_team_a_name() + '\n';
	msg = msg + "team b: " + input.get_team_b_name() + '\n';
	msg = msg + "event name: ";
	msg = msg + input.get_name() + '\n' + "time: " + std::to_string(input.get_time()) + '\n' + "general game updates:" + '\n';
	std::map<std::string, std::string> game_updates = input.get_game_updates();
	for (std::map<std::string, std::string>::iterator a = game_updates.begin(); a != game_updates.end(); ++a)
	{
		msg = msg + "    " + a->first + ": " + a->second + '\n';
	}
	msg = msg + "team a updates:" + '\n';
	std::map<std::string, std::string> teamAupdates = input.get_team_a_updates();
	for (std::map<std::string, std::string>::iterator a = teamAupdates.begin(); a != teamAupdates.end(); ++a)
	{
		msg = msg + "    " + a->first + ": " + a->second + '\n';
	}
	msg = msg + "team b updates:" + '\n';
	std::map<std::string, std::string> teamBupdates = input.get_team_b_updates();
	for (std::map<std::string, std::string>::iterator a = teamBupdates.begin(); a != teamBupdates.end(); ++a)
	{
		msg = msg + "    " + a->first + ": " + a->second + '\n';
	}
	msg = msg + "description:" + '\n' + input.get_discription() + '\n';
	return msg;
}

string StompProtocol::procces(string command, string line, ConnectionHandler &con)
{
	if (command == "login")
	{   
		if(connec==true){
			cout << "The client is already logged in, log out before trying again" << endl;
			return "none";
		}
		else{
		return connect(line);
		}
	}
	else if (command == "join")
	{
		line = line.substr(command.size() + 1);
		return subscribe(line, receiptId++, subscribtionId++);
	}
	else if (command == "exit")
	{
		line = line.substr(command.size() + 1);
		return unsubscribe(line, receiptId++);
	}
	else if (command == "logout")
	{
		return logout(receiptId++);
	}
	else if(command == "report"){
		line = line.substr(command.size() + 1);
		reports(line,con);
		return "none";
	}
	else if (command == "summary")
	{
		line = line.substr(command.size() + 1);
		summary(line);
		return "none";
	}
	return "none";
}
void StompProtocol::reports(string line, ConnectionHandler &con)
{
	string msg;
	std::vector<Event> events(send(line));
	for (auto event = events.begin(); event != events.end(); ++event)
	{
		msg = eventer(*event);
		con.sendLine(msg);
	}
}
string StompProtocol::summary(string line)
{
	int i = line.find(" ");
	string channle = line.substr(0, i);
	line = line.substr(i + 1);
	i = line.find(" ");
	string user = line.substr(0, i);
	line = line.substr(i + 1);
	i = line.find(" ");
	string file = line.substr(0, i);
	string out="0";
	for (unsigned int i = 0; i < users_report[user].size(); i++)
	{
		if (users_report[user][i].get_game_name() == channle)
		{
			out = users_report[user][i].summery();
			break;
		}
	}
	if(out=="0"){
		cout << "no reports found for selected channle or user" << endl;
		return out;
	}
	std::ofstream MyFile(file);
	MyFile << out;
	MyFile.close();
	return out;
}
std::vector<Event> StompProtocol::send(string line)
{
	names_and_events report = parseEventsFile(line);
	return report.events;
}
string StompProtocol::logout(int receipt)
{
	setBol(false);
	string msg = "DISCONNECT";
	msg = msg + '\n' + "receipt:" + std::to_string(receipt) + '\n' + '\n';
	recipt_ID_TO_action[receipt] = "logged out successfully";
	return msg;
}
string StompProtocol::subscribe(string line, int receipt, int subscribe)
{
	string msg = "SUBSCRIBE";
	msg = msg + '\n';
	msg = msg + "destination:/" + line + '\n';
	msg = msg + "id:" + std::to_string(subscribe) + '\n' + "receipt:" + std::to_string(receipt) + '\n' + '\n';
	channle_to_id[line] = subscribe;
	string to_print = "Joined channel ";
	recipt_ID_TO_action[receipt] = to_print + line;
	return msg;
}
string StompProtocol::unsubscribe(string line, int receipt)
{
	string msg = "UNSUBSCRIBE";
	msg = msg + '\n';
	msg = msg + "id:" + std::to_string(channle_to_id[line]) + '\n' + "receipt:" + std::to_string(receipt) + '\n' + '\n';
	string to_print = "Exited channel ";
	recipt_ID_TO_action[receipt] = to_print + line;
	return msg;
}
string StompProtocol::connect(string line)
{
	string msg = "CONNECT";
	msg = msg + '\n' + "accept-version:1.2" + '\n';
	msg = msg + "host" + ':' + "stomp.cs.bgu.ac.il" + '\n';
	string user = "";
	string pass = "";
	bool User = true;
	for (unsigned int i = 1; i < line.size(); i++)
	{
		if (line[i] == ' ')
		{
			User = false;
			continue;
		}
		if (User)
		{
			user = user + line[i];
		}
		else
		{
			pass = pass + line[i];
		}
	}
	msg = msg + "login:" + user + '\n';
	msg = msg + "passcode:" + pass + '\n';
	msg = msg + '\n';
	username = user;
	msg = msg;
	return msg;
}
void StompProtocol::getProcces(string msg)
{
	int i = msg.find("\n");
	if (msg.substr(0, i) == "MESSAGE")
	{
		getreport(msg.substr(msg.substr(0, i).size() + 1));
	}
	if (msg.substr(0, i) == "RECEIPT")
	{
		msg = msg.substr(i + 1);
		i = msg.find(":");
		int j = msg.find("\n");
		string receipt = msg.substr(i + 1, j - i - 1);
		int receipt_id = stoi(receipt);
		cout << recipt_ID_TO_action[receipt_id] << endl;
	}
	if(msg.substr(0, i) == "ERROR"){
		setBol(false);
		int j=msg.find("message");
		msg=msg.substr(j+1);
        i=msg.find(":");
		j=msg.find("\n");
        cout << msg.substr(i+2,j-i-1-1) << endl;
	}
	if(msg.substr(0, i) == "CONNECTED"){
		setBol(true);
		cout << "Login successful" << endl;
	}
}
void StompProtocol::getreport(string report)
{
	int i = report.find("\n");
	string subscription = report.substr(0, i);
	report = report.substr(subscription.size() + 1);
	i = report.find("\n");
	string massegeID = report.substr(0, i);
	report = report.substr(massegeID.size() + 1);
	i = report.find("/");
	report = report.substr(i + 1);
	i = report.find("\n");
	string channle = report.substr(0, i);
	cout << "\nyou got a message from channle :"+ channle+"\n" << endl;
	string report1 = report.substr(i + 2, report.size());
	cout << "the message is:\n" +report1 <<endl;
	report_for_channle(channle, report1);
}
void StompProtocol::report_for_channle(string channle, string report)
{
	int i = report.find("\n");
	int j = report.find(":");
	string user = report.substr(j + 1, i - j - 1);
	report = report.substr(i + 1);
	i = report.find("\n");
	j = report.find(":");
	string a = report.substr(j + 1, i - j - 1);
	report = report.substr(i + 1);
	i = report.find("\n");
	j = report.find(":");
	string b = report.substr(j + 1, i - j - 1);
	report = report.substr(i + 1);
	i = report.find("\n");
	j = report.find(":");
	string game_event = report.substr(j + 1, i - j - 1);
	report = report.substr(i + 1);
	i = report.find("\n");
	j = report.find(":");
	string time = report.substr(j + 1, i - j - 1);
	report = report.substr(i + 1);

	map<string, string> game_updates;
	map<string, string> team_a_updates;
	map<string, string> team_b_updates;
	string description = "";
	bool game_update = true;
	bool team_a = false;
	bool team_b = false;
	while (1)
	{

		i = report.find("\n");
		j = report.find(":");
		string header = report.substr(0, j);
		if (header == "general game updates")
		{
			game_update = true;
		}
		else if (header == "team a updates")
		{
			team_a = true;
			game_update = false;
		}
		else if (header == "team b updates")
		{
			team_b = true;
			team_a = false;
		}
		else if (header == "description")
		{
			team_b = false;
			int x = report.find("\n");
			description = report.substr(x + 1);
			break;
		}
		else
		{
			if (game_update)
			{
				game_updates[report.substr(0, j)] = report.substr(j + 1, i - j - 1);
			}
			else if (team_a)
			{
				team_a_updates[report.substr(0, j)] = report.substr(j + 1, i - j - 1);
			}
			else if (team_b)
			{
				team_b_updates[report.substr(0, j)] = report.substr(j + 1, i - j - 1);
			}
		}
		report = report.substr(i + 1);
	}
	user = user.substr(1);
	if (check_if_first_report(user, channle))
	{
		int q = channle.find("_");
		game a(channle.substr(0, q), channle.substr(q + 1));
		a.update(game_event, time, description, game_updates, team_a_updates, team_b_updates);
		users_report[user].push_back(a);
		return;
	}
	else
	{
		for (unsigned int i = 0; i < users_report[user].size(); i++)
		{
			if (users_report[user][i].get_game_name() == channle)
			{
				users_report[user][i].update(game_event, time, description, game_updates, team_a_updates, team_b_updates);
				break;
			}
		}
	}
}
bool StompProtocol::check_if_first_report(string user, string channle)
{
	vector<game> games = users_report[user];
	for (unsigned int i = 0; i < games.size(); i++)
	{
		if (games[i].get_game_name() == channle)
		{
			return false;
		}
	}
	return true;
}
