#include "../include/Offer.h"

Offer::Offer(int coalitionId, int agentId) : mCoalitionId(coalitionId), mAgentId(agentId)
{

}
int Offer::getCoalitionId() const{
    return mCoalitionId;
}
int Offer::getAgentID() const{
    return mAgentId;
}

