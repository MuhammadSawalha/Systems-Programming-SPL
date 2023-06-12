package bguspl.set.ex;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import bguspl.set.Env;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    private final Dealer dealer;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private  boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    private int remain_Tokens;

    private  List<Integer> tokensPlace;

    private  BlockingQueue<Action> actionsQueue;

    public  boolean blocked;

    public  boolean to_Get_Point;

    public  boolean to_Get_Penalty;

    public volatile boolean waiting_To_Dealer_Answer;

    private  Object aiLock;

    private final int SetSize;


    public volatile boolean dealerIsInside;


    public volatile boolean playerIsInside;
    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.dealer = dealer; 
        
        SetSize=env.config.featureSize;
        remain_Tokens = SetSize;
        actionsQueue = new ArrayBlockingQueue<Action>(SetSize);
        tokensPlace = new ArrayList<Integer>(SetSize);
        blocked = false;
        to_Get_Point = false;
        to_Get_Penalty = false;
        waiting_To_Dealer_Answer = false;
        dealerIsInside = false;
        aiLock = new Object();
        playerIsInside = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence();

        while (!terminate) {
            try{
                synchronized(this){
                    playerIsInside = true;
                    while((actionsQueue.size() == 0 | dealer.isActive) && !terminate){
                        synchronized(aiLock){
                            aiLock.notifyAll();
                        }
                        wait();
                    } 
                    playerIsInside = false;
                    if(terminate) continue;
                }
            }catch(InterruptedException e){}
          
            if(actionsQueue.size()==0)continue;
            Action currenAction = actionsQueue.poll();

            int slot = currenAction.getSlot();
            int card = currenAction.getCard();
            //if(table.slotToCard[slot] == null) continue;
            if(remain_Tokens < SetSize && tokensPlace.contains((Integer)slot) && table.tokensOnTable[slot][id] == true){
                if(dealer.isActive) continue;
                synchronized(dealer.activeLock){
                    if(terminate) continue;
                    if(table.tokensOnTable[slot][id] == true && tokensPlace.contains((Integer)slot)){
                        table.removeToken(id, slot);
                        tokensPlace.remove((Integer)slot);
                        remain_Tokens++;
                    }else{
                        continue;
                    }
                } 
            }else if(remain_Tokens > 0 && table.slotToCard[slot] != null && card == table.slotToCard[slot] && table.tokensOnTable[slot][id] == false){
                if(dealer.isActive) continue;
                synchronized(dealer.activeLock){
                    if(terminate) continue;
                    if(table.slotToCard[slot] != null && card == table.slotToCard[slot]){
                        table.placeToken(id, slot);
                        tokensPlace.add(slot);
                        remain_Tokens--;
                    }else{
                        continue;
                    }
                }
                if(remain_Tokens == 0){
                    int[] cards = tokensToCards();
                    Offer offer = new Offer(cards, id);
                    try{
                        synchronized(this){
                            synchronized(dealer.offersLock){
                                dealer.offersQueue.put(offer);
                                dealerIsInside = dealer.inside_his_synchronized;
                                if(dealerIsInside){
                                    synchronized(dealer){
                                        dealer.inside_his_synchronized = false;
                                        dealer.notifyAll();
                                    }
                                }
                                
                            } 
                            if(terminate) continue;

                            waiting_To_Dealer_Answer = true;
                            wait();
                            waiting_To_Dealer_Answer = false;
                            if(terminate) continue;
                        }
                    }catch(InterruptedException e){}
                }
            }
            if(to_Get_Point){
                point();
                
            }
            if(to_Get_Penalty){
                penalty();
  
            }
        }
        if (!human) try { aiThread.join();System.out.println("Ai for player "+id+" has terminated"); } catch (InterruptedException ignored) {}
        System.out.println("player "+id+" is about to finish");
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }


    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");
            while (!terminate) {
                while(actionsQueue.size() < SetSize && !blocked && !terminate){
                    int slot = (int)(Math.random()*env.config.tableSize);
                     
                    if(table.slotToCard[slot] == null){
                        continue;
                    }
                    
                    synchronized(this){
                        keyPressed(slot); 
                    } 
                }
                if(terminate) continue;
                try {
                    synchronized (aiLock) {
                        aiLock.wait(); 
                        if(terminate) continue;
                    }
                } catch (InterruptedException ignored) {}
            }
            env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        try{
            if(aiThread != null){
                synchronized(aiLock){
                    aiLock.notifyAll();
                }
                aiThread.join();
            }
            
            synchronized(this){
                notifyAll();
            }
            playerThread.join();
        }catch(InterruptedException e){}
        
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        if(blocked){
            return;
        }
        int card = 0;
        if(table.slotToCard[slot] != null){
            card = table.slotToCard[slot];
        }else{
            return;
        }
        if(actionsQueue.size() < SetSize){
            Action currentAction = new Action(slot, card);
            actionsQueue.add(currentAction);
            if(playerIsInside){
                synchronized(this){ notifyAll();}
            }
        }
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        blocked = true;
        long pointTime = env.config.pointFreezeMillis;
        while(pointTime > 1000){
            env.ui.setFreeze(id, pointTime);
            try {
                synchronized (this) { Thread.sleep(1000); }
            } catch (InterruptedException ignored) {}
            pointTime = pointTime - 1000;
        }
        env.ui.setFreeze(id, pointTime);
        try {
            synchronized (this) { Thread.sleep(pointTime); }
        } catch (InterruptedException ignored) {}
        env.ui.setFreeze(id, 0);    
        if(dealer.timer != 0){
            blocked = false;
        }
        to_Get_Point = false;
        actionsQueue.clear();
        env.ui.setScore(id, ++score);
    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        blocked = true;
        long penaltyTime = env.config.penaltyFreezeMillis;
        while(penaltyTime > 1000){
            env.ui.setFreeze(id, penaltyTime);
            try {
                synchronized (this) { Thread.sleep(1000); }
            } catch (InterruptedException ignored) {}
            penaltyTime = penaltyTime - 1000;
        }
        env.ui.setFreeze(id, penaltyTime);  
        try {
            synchronized (this) { Thread.sleep(penaltyTime); }
        } catch (InterruptedException ignored) {}
        env.ui.setFreeze(id, 0);    
        if(dealer.timer != 0){
            blocked = false;
        }
        to_Get_Penalty = false;
        actionsQueue.clear();
    }

    public int score() {
        return score;
    }
    private int[] tokensToCards(){
        int[] cards = new int[SetSize];
        for(int i = 0 ; i < SetSize ; i++){
            cards[i] = table.slotToCard[tokensPlace.get(i)];
        }
        return cards;
    }
    
    public void setBlock(boolean status){
        blocked = status;
    }
    
    public void removeToken(int slot){
        tokensPlace.remove((Integer)slot);
    }
    
    public void tokenIncreament(){
        remain_Tokens++;
    }
    
    public void givePoint(){
        to_Get_Point = true;
    }
    
    public void givePenalty(){
        to_Get_Penalty = true;
    }
    
    public int getLeftTokens(){
        return remain_Tokens;
    }
    
    public void clearQueue(){
        actionsQueue.clear();
    }
    public  void setScore(int s){score=s;}
}




