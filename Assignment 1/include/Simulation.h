#pragma once

#include <vector>

#include "Graph.h"
#include "Agent.h"
#include "Coalition.h"

using std::string;
using std::vector;

class Simulation
{
public:
    Simulation(Graph g, vector<Agent> agents);

    void step();
    bool shouldTerminate() const;

    const Graph &getGraph() const;
    const vector<Agent> &getAgents() const;
    const Party &getParty(int partyId) const;
    const vector<vector<int>> getPartiesByCoalitions() const;

    vector<Coalition> &getCoalitions() ;
    vector<Agent> &getAgents() ;
    Party &getParty(int partyId);
    vector<Party> &getParties();
    vector<vector<int>> &getEdges();
    void increaseNumberOfJoinedParties();

private:
    int numberOfJoinedParties;
    Graph mGraph;
    vector<Agent> mAgents;
    vector<Coalition> mCoalitions;
};

