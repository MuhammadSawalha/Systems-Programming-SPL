#pragma once

#include <vector>
#include "Graph.h"

class SelectionPolicy;


enum agentState
{
    Finished,
    Active
};

class Agent
{
public:
    Agent(int agentId, int partyId, SelectionPolicy *selectionPolicy);

    virtual ~Agent();
    Agent(const Agent &other);
    Agent& operator=(const Agent &other);
    Agent(Agent &&other);
    Agent& operator=(Agent &&other);

    int getPartyId() const;
    int getId() const;
    void step(Simulation &);
    int getCoalitionId() const;
    void setCoalitionId(int numOfCoalition);
    void setAgentId(int agentId);
    void setPartyId(int partyId);
    agentState getState() const;
    void setState(agentState state);

private:
    int mAgentId;
    int mPartyId;
    int mCoalitionId;
    agentState mState;
    SelectionPolicy *mSelectionPolicy;
};