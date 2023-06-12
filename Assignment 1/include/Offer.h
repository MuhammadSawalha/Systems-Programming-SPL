#pragma once

class Offer{
public:
    Offer(int coalitionId, int agentId);
    int getCoalitionId() const;
    int getAgentID() const;

private:
    int mCoalitionId;
    int mAgentId;
};


