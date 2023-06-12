#include "../include/Party.h"
#include "../include/Offer.h"
#include "../include/Simulation.h"
#include "../include/JoinPolicy.h"
#include <utility>
#include <vector>

using std::vector;


Party::Party(int id, string name, int mandates, JoinPolicy *jp) : mId(id), mName(std::move(name)), mMandates(mandates), mJoinPolicy(jp), mState(Waiting), timer(0), offers()
{

}

Party::~Party(){
    if(mJoinPolicy) delete mJoinPolicy;
}

Party::Party(const Party &other) : mId(other.mId), mName(other.mName), mMandates(other.mMandates), mJoinPolicy(nullptr),mState(other.mState), timer(other.timer), offers(){
    mJoinPolicy=other.mJoinPolicy->clone();
    for(int i = 0 ; i < other.getNumberOfOffers() ; i++){
        offers.push_back(Offer(other.getOffer(i).getAgentID(),other.getOffer(i).getCoalitionId()));
    }
}

Party& Party::operator=(const Party &other){
    if(this != &other){
        if(mJoinPolicy) delete mJoinPolicy;
        mId = other.mId;
        mName = other.mName;
        mMandates = other.mMandates;
        mState = other.mState;
        timer = other.timer;
        mJoinPolicy=other.mJoinPolicy->clone();
        for(int i = 0 ; i < other.getNumberOfOffers() ; i++){
            offers.push_back(Offer(other.getOffer(i).getAgentID(),other.getOffer(i).getCoalitionId()));
        }
    }
    return *this;
}

Party::Party(Party &&other) : mId(other.mId), mName(other.mName), mMandates(other.mMandates), mJoinPolicy(other.mJoinPolicy),mState(other.mState), timer(other.timer), offers(other.offers){
    other.mJoinPolicy = nullptr;
}

Party& Party::operator=(Party &&other){
    if(this != &other){
        if(mJoinPolicy) delete mJoinPolicy;
        mId = other.mId;
        mName = other.mName;
        mMandates = other.mMandates;
        mState = other.mState;
        timer = other.timer;
        mJoinPolicy = other.mJoinPolicy;
        offers = other.offers;
        other.mJoinPolicy = nullptr;
    }
    return *this;
}


State Party::getState() const
{
    return mState;
}

void Party::setState(State state)
{
    mState = state;
}

int Party::getMandates() const
{
    return mMandates;
}

const string & Party::getName() const
{
    return mName;
}

void Party::step(Simulation &s)
{
    // TODO: implement this method
    if(getState() == Joined) return;
    if(getState() == CollectingOffers){
        increaseTimer();
    }
    if(getTimer() >= 3){
        mJoinPolicy->join(*this,s);
    }
}


void Party::increaseTimer(){
    timer++;
}

int Party::getNumberOfOffers() const{
    return offers.size();
}

const Offer &Party::getOffer(int offerIndex) const{
    return offers[offerIndex];
}

void Party::receiveOffer(Offer &newOffer){
    if(getState() == Waiting){
        setState(CollectingOffers);
    }
    offers.push_back(newOffer);
}


int Party::getTimer() const {
    return timer;
}

vector<Offer> &Party::getOffers() {
    return offers;
}

int Party::getId() const {
    return mId;
}

bool Party::gotOfferFromCoalition(int coalition) {
    int numberOfOffers = getNumberOfOffers();
    for(int i = 0 ; i < numberOfOffers ; i++){
        if(getOffer(i).getCoalitionId() == coalition){
            return true;
        }
    }
    return false;
}