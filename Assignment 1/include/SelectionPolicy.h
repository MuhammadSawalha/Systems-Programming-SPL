#pragma once
#include <vector>


using std::vector;

class Simulation;
class Agent;

class SelectionPolicy {
public:
    virtual ~SelectionPolicy();
    virtual SelectionPolicy* clone() = 0 ;
    virtual void select(Agent &agent, vector<int> &availableParties, Simulation &s) = 0;
};

class MandatesSelectionPolicy: public SelectionPolicy{
public:
    ~MandatesSelectionPolicy() override;
    SelectionPolicy* clone();
    void select(Agent &agent, vector<int> &availableParties, Simulation &s) override;
};

class EdgeWeightSelectionPolicy: public SelectionPolicy{
public:
    ~EdgeWeightSelectionPolicy() override;
    SelectionPolicy* clone();
    void select(Agent &agent, vector<int> &availableParties, Simulation &s) override;
};
