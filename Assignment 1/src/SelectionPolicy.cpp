#include "../include/SelectionPolicy.h"
#include "../include/Simulation.h"
#include "../include/Offer.h"


using std::string;

SelectionPolicy::~SelectionPolicy() = default;



MandatesSelectionPolicy::~MandatesSelectionPolicy() = default;

void MandatesSelectionPolicy::select(Agent &agent, vector<int> &availableParties, Simulation &s) {
    vector<Party> &parties = s.getParties();
    int chosenParty = parties[availableParties[0]].getId();
    int numberOfAvailableParties = availableParties.size();
    for (int i = 1 ; i < numberOfAvailableParties ; i++){
        int currentParty = availableParties[i];
        if(parties[currentParty].getMandates() > parties[chosenParty].getMandates()){
            chosenParty = currentParty;
        }
    }
    Offer newOffer = Offer(agent.getCoalitionId(),agent.getId());
    parties[chosenParty].receiveOffer(newOffer);
}



EdgeWeightSelectionPolicy::~EdgeWeightSelectionPolicy() = default;

void EdgeWeightSelectionPolicy::select(Agent &agent, vector<int> &availableParties, Simulation &s) {
    vector<Party> &parties = s.getParties();
    vector<vector<int>> &edges = s.getEdges();
    int chosenParty = parties[availableParties[0]].getId();
    int numberOfAvailableParties = availableParties.size();

    for (int i = 1 ; i < numberOfAvailableParties ; i++){
        int currentParty = availableParties[i];
        if(edges[currentParty][agent.getPartyId()] > edges[chosenParty][agent.getPartyId()]){
            chosenParty = currentParty;
        }
    }
    Offer newOffer = Offer(agent.getCoalitionId(),agent.getId());
    parties[chosenParty].receiveOffer(newOffer);
}
SelectionPolicy* EdgeWeightSelectionPolicy::clone(){
    return new EdgeWeightSelectionPolicy;
}
SelectionPolicy* MandatesSelectionPolicy::clone(){
    return new MandatesSelectionPolicy;
}

