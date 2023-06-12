#include "../include/Agent.h"
#include "../include/SelectionPolicy.h"
#include "../include/Simulation.h"
#include <iostream>

Agent::Agent(int agentId, int partyId, SelectionPolicy *selectionPolicy) : mAgentId(agentId), mPartyId(partyId),mCoalitionId(-1), mState(Active), mSelectionPolicy(selectionPolicy)
{

}

Agent::~Agent(){
    if(mSelectionPolicy){
        delete mSelectionPolicy;
    }
}

Agent::Agent(const Agent &other) : mAgentId(other.mAgentId), mPartyId(other.mPartyId), mCoalitionId(other.mCoalitionId), mState(other.mState), mSelectionPolicy(nullptr){
    mSelectionPolicy=other.mSelectionPolicy->clone();

}

Agent& Agent::operator=(const Agent &other){
    if(this != &other){
        mAgentId = other.mAgentId;
        mPartyId = other.mPartyId;
        mCoalitionId = other.mCoalitionId;
        mState = other.mState;
        if(mSelectionPolicy){
            delete mSelectionPolicy;
        }
        mSelectionPolicy=other.mSelectionPolicy->clone();
    }
    return *this;
}

Agent::Agent(Agent &&other) : mAgentId(other.mAgentId), mPartyId(other.mPartyId), mCoalitionId(other.mCoalitionId), mState(other.mState), mSelectionPolicy(other.mSelectionPolicy){
    other.mSelectionPolicy = nullptr;
}

Agent& Agent::operator=(Agent &&other){
    if(this != &other){
        if(mSelectionPolicy){
            delete mSelectionPolicy;
        }
        mAgentId = other.mAgentId;
        mPartyId = other.mPartyId;
        mCoalitionId = other.mCoalitionId;
        mState = other.mState;
        mSelectionPolicy = other.mSelectionPolicy;
        other.mSelectionPolicy = nullptr;
    }
    return *this;
}

int Agent::getId() const
{
    return mAgentId;
}

int Agent::getPartyId() const
{
    return mPartyId;
}


void Agent::step(Simulation &sim)
{
    // TODO: implement this method
    if(getState() == Finished) return;

    vector<int> availableParties = vector<int>();
    vector<Party> &parties = sim.getParties();
    vector<vector<int>> &edges = sim.getEdges();
    int numberOfParties = parties.size();


    for(int i = 0 ; i < numberOfParties ; i++){
        if(parties[i].getState() == Joined){
            continue;
        }
        if(edges[getPartyId()][parties[i].getId()] == 0){
            continue;
        }
        if(parties[i].gotOfferFromCoalition(getCoalitionId())){
            continue;
        }
        availableParties.push_back(parties[i].getId());
    }
    if(availableParties.empty()){
        setState(Finished);
        return;
    }
    mSelectionPolicy->select(*this, availableParties,sim);
}

int Agent::getCoalitionId() const {
    return mCoalitionId;
}

void Agent::setCoalitionId(int numOfCoalition) {
    mCoalitionId = numOfCoalition;
}

void Agent::setAgentId(int agentId) {
    mAgentId = agentId;
}

void Agent::setPartyId(int partyId) {
    mPartyId = partyId;
}

agentState Agent::getState() const {
    return mState;
}

void Agent::setState(agentState state) {
    mState = state;
}