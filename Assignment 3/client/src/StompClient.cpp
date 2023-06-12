#include <stdlib.h>
#include <thread>
#include <string>
#include <iostream>
#include <fstream>
#include "event.cpp"
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include <vector>
#include "Reader.h"
using std::cerr;
using std::cin;
using std::cout;
using std::endl;
using std::string;
using std::stringstream;
using namespace std;

string Give_Command(string line)
{
	string command = "";
	for (unsigned int i = 0; i < line.size(); i++)
	{
		if (line[i] != ' ')
		{
			command += line[i];
		}
		if (line[i] == ' ')
		{
			break;
		}
	}
	return command;
}

string getkeyboard()
{
	const short bufsiz = 1024;
	char buff[bufsiz];
	cin.getline(buff, bufsiz);
	string line(buff);
	return line;
}

int main(int argc, char *argv[])
{
	while (1)
	{
		string line = getkeyboard();
		string command = Give_Command(line);line = line.substr(command.size() + 1);
		string host = line.substr(0, line.find(":"));line = line.substr(host.size() + 1);
		string port = line.substr(0, line.find(" "));line = line.substr(port.size());//צמצום
		stringstream geek(port);
		short Port = 0;
		geek >> Port;
		ConnectionHandler connectionHandler(host, Port);
		if (!connectionHandler.connect()){std::cerr << "Could not connect to server " << host << ":" << Port << std::endl;continue;}
		StompProtocol stomp;
		string toSend = stomp.procces(command, line,connectionHandler);
		connectionHandler.sendLine(toSend);
		string answer;
		connectionHandler.getLine(answer);
		stomp.getProcces(answer);
		while (stomp.getConnec())
		{
			Reader reader(&connectionHandler, &stomp);
			thread readerThread(&Reader::run, &reader);
			while (stomp.getConnec())
			{
				line = getkeyboard();
				if (!stomp.getConnec()){break;}
				string command = Give_Command(line);
				string msg;
					msg = stomp.procces(command, line,connectionHandler);
					if(msg != "none"){
					    connectionHandler.sendLine(msg);

				}
			}
			readerThread.join();
			cout << "exiting ...\n" << endl;
		}
		connectionHandler.close();
	}
	return 0;
}