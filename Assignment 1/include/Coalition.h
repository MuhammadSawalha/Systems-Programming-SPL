#pragma once

#include "Agent.h"
#include "Party.h"

#include <vector>
using std::vector;

class Coalition{
public:
    Coalition(int coalID, Party &firstParty, Agent &firstAgent);
    int getNumOfMandates() const;
    void addParty(Party &newParty , Agent &newAgent);
    vector<int> getPartiesID() const;
private:
    int coalID;
    int numOfMandates;
    vector<Party> mParties;
    vector<Agent> mAgents;
};

