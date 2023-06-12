#include "../include/JoinPolicy.h"
#include "../include/Party.h"
#include "../include/Simulation.h"
#include "../include/Offer.h"


JoinPolicy::~JoinPolicy()= default;

MandatesJoinPolicy::~MandatesJoinPolicy()= default;

void MandatesJoinPolicy::join(Party &party, Simulation &s) {
   vector<Coalition> &coalitions = s.getCoalitions();
   vector<Agent> &agents = s.getAgents();
   vector<Offer> &offers = party.getOffers();

   int senderAgent = offers[0].getAgentID();
   int chosenCoalition = agents[senderAgent].getCoalitionId();
   int numberOfOffers = offers.size();
   int numberOfAgents = agents.size();

   for(int i = 0 ; i < numberOfOffers ; i++){
       int numOfCoalition = offers[i].getCoalitionId();
       if(coalitions[numOfCoalition].getNumOfMandates() > coalitions[chosenCoalition].getNumOfMandates()){
           chosenCoalition = numOfCoalition;
           senderAgent = offers[i].getAgentID();
       }
   }
   Agent newAgent = Agent(agents[senderAgent]);
   newAgent.setCoalitionId(chosenCoalition);
   newAgent.setAgentId(numberOfAgents);
   newAgent.setPartyId(party.getId());
   newAgent.setState(Active);
   agents.push_back(newAgent);

   party.setState(Joined);
   s.increaseNumberOfJoinedParties();
   coalitions[chosenCoalition].addParty(party,newAgent);
}
JoinPolicy* LastOfferJoinPolicy::clone(){
    return new LastOfferJoinPolicy;
}
JoinPolicy* MandatesJoinPolicy::clone(){
    return new MandatesJoinPolicy;
}
LastOfferJoinPolicy::~LastOfferJoinPolicy()= default;

void LastOfferJoinPolicy::join(Party &party, Simulation &s) {
    vector<Coalition> &coalitions = s.getCoalitions();
    vector<Agent> &agents = s.getAgents();
    vector<Offer> &offers = party.getOffers();

    int numberOfOffers = offers.size();
    int numberOfAgents = agents.size();
    int senderAgent = offers[numberOfOffers - 1].getAgentID();
    int chosenCoalition = offers[numberOfOffers - 1].getCoalitionId();
    Agent newAgent = Agent(agents[senderAgent]);
    newAgent.setCoalitionId(chosenCoalition);
    newAgent.setAgentId(numberOfAgents);
    newAgent.setPartyId(party.getId());
    newAgent.setState(Active);
    agents.push_back(newAgent);

    party.setState(Joined);
    s.increaseNumberOfJoinedParties();
    coalitions[chosenCoalition].addParty(party,newAgent);
}