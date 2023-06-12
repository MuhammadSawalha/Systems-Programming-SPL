#pragma once
#include <string>
#include <vector>

using std::string;
using std::vector;

class JoinPolicy;
class Simulation;
class Offer;


enum State
{
    Waiting,
    CollectingOffers,
    Joined
};

class Party
{
public:
    Party(int id, string name, int mandates, JoinPolicy *jp);

    virtual ~Party();
    Party(const Party& other);
    Party& operator=(const Party &other);
    Party(Party &&other);
    Party& operator=(Party &&other);

    State getState() const;
    void setState(State state);
    int getMandates() const;
    void step(Simulation &s);
    const string &getName() const;

    void increaseTimer();
    int getNumberOfOffers() const;
    const Offer &getOffer(int offerIndex) const;
    void receiveOffer(Offer &newOffer);
    int getTimer() const;
    vector<Offer> &getOffers();
    int getId() const;
    bool gotOfferFromCoalition(int coalition);
private:
    int mId;
    string mName;
    int mMandates;
    JoinPolicy *mJoinPolicy;
    State mState;

    int timer;
    vector<Offer> offers;
};
