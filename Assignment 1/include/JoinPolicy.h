

#pragma once
#include <vector>

using std::vector;

class Party;
class Simulation;

class JoinPolicy {
public:
    virtual ~JoinPolicy();
    virtual JoinPolicy* clone() = 0;
    virtual void join(Party &party, Simulation &s) = 0;
};

class MandatesJoinPolicy : public JoinPolicy {
public:
    ~MandatesJoinPolicy() override;
    JoinPolicy* clone();
    void join(Party &party, Simulation &s) override;
};

class LastOfferJoinPolicy : public JoinPolicy {
public:
    ~LastOfferJoinPolicy() override;
    JoinPolicy* clone();
    void join(Party &party, Simulation &s) override;
};

