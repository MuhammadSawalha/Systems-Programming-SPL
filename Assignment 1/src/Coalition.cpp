#include "../include/Coalition.h"

Coalition::Coalition(int coalID, Party &firstParty, Agent &firstAgent) : coalID(coalID), numOfMandates(0), mParties(),mAgents(){
    numOfMandates = numOfMandates + firstParty.getMandates();
    mParties.push_back(firstParty);
    mAgents.push_back(firstAgent);
}

int Coalition::getNumOfMandates() const{
    return numOfMandates;
}

void Coalition::addParty(Party &newParty, Agent &newAgent) {
    mParties.push_back(newParty);
    mAgents.push_back(newAgent);
    numOfMandates = numOfMandates + newParty.getMandates();
}

vector<int> Coalition::getPartiesID() const{
    vector<int> partiesID = vector<int>();
    int numberOfParties = mParties.size();
    for(int i = 0 ; i < numberOfParties ; i++){
        partiesID.push_back(mParties[i].getId());
    }
    return partiesID;
}