#include "../include/Simulation.h"
#include <iostream>

Simulation::Simulation(Graph graph, vector<Agent> agents) : numberOfJoinedParties(0), mGraph(graph), mAgents(agents), mCoalitions()
{
    int NumberOfAgents = mAgents.size();
    numberOfJoinedParties = NumberOfAgents;
    for(int i = 0 ; i < NumberOfAgents ; i++){
        int PartyID = mAgents[i].getPartyId();
        mAgents[i].setCoalitionId(i);
        mCoalitions.push_back(Coalition(i,mGraph.getParty(PartyID),mAgents[i]));
    }
}

void Simulation::step()
{
    // TODO: implement this method
    int NumberOfParties = mGraph.getNumVertices();
    for(int i = 0 ; i < NumberOfParties ; i++){
        mGraph.getParty(i).step(*this);
    }
    int NumberOfAgents = mAgents.size();  
    for(int i = 0 ; i < NumberOfAgents ; i++){
        mAgents[i].step(*this);
    }
}

bool Simulation::shouldTerminate() const
{
    // TODO implement this method
    int numberOfCoalitions = mCoalitions.size();
    int numberOfParties = getGraph().getNumVertices();
    for(int i = 0 ; i < numberOfCoalitions ; i++){
        if(mCoalitions[i].getNumOfMandates() >= 61){
            return true;
        }
    }
    if(numberOfJoinedParties == numberOfParties){
        return true;
    }
    return false;
}

const Graph &Simulation::getGraph() const
{
    return mGraph;
}

const vector<Agent> &Simulation::getAgents() const
{
    return mAgents;
}

const Party &Simulation::getParty(int partyId) const
{
    return mGraph.getParty(partyId);
}

vector<Coalition> &Simulation::getCoalitions(){
    return mCoalitions;
}

vector<Agent> &Simulation::getAgents(){
    return mAgents;
}

Party &Simulation::getParty(int partyId)
{
    return mGraph.getParty(partyId);
}

vector<Party> &Simulation::getParties(){
    return mGraph.getVertices();
}

vector<vector<int>> &Simulation::getEdges() {
    return mGraph.getEdges();
}

void Simulation::increaseNumberOfJoinedParties(){
    numberOfJoinedParties++;
}

/// This method returns a "coalition" vector, where each element is a vector of party IDs in the coalition.
/// At the simulation initialization - the result will be [[agent0.partyId], [agent1.partyId], ...]
const vector<vector<int>> Simulation::getPartiesByCoalitions() const
{
    // TODO: you MUST implement this method for getting proper output, read the documentation above.
    int numberOfCoalitions = mCoalitions.size();
    vector<vector<int>> partiesByCoalitions = vector<vector<int>>();
    for(int i = 0 ; i < numberOfCoalitions ; i++){
        vector<int> partiesID = mCoalitions[i].getPartiesID();
        int numberOfParties = partiesID.size();
        vector<int> coalition = vector<int>();
        for(int j = 0 ; j < numberOfParties ; j++){
            coalition.push_back(partiesID[j]);
        }
        partiesByCoalitions.push_back(coalition);
    }
    return partiesByCoalitions;
}
