package bguspl.set.ex;

import bguspl.set.Env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;
    public final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private  List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private boolean terminate;

    private volatile List<Integer> emptySlots;
    public volatile long timer;
    public volatile BlockingQueue<Offer> offersQueue;
    public volatile Thread dealerThread;
    public volatile Object offersLock;
    public volatile boolean inside_his_synchronized;
    public volatile boolean isActive;
    public volatile Object activeLock;
    public volatile Object waiting;
    private final int SetSize;

    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());

        emptySlots = IntStream.range(0, env.config.tableSize).boxed().collect(Collectors.toList());
        shuffle(emptySlots);
        shuffle(deck);
        timer = env.config.turnTimeoutMillis;

        offersQueue = new LinkedBlockingQueue<Offer>();
        SetSize=env.config.featureSize;
        inside_his_synchronized = false;
        isActive = false;
        offersLock = new Object();
        activeLock = new Object();
        terminate=false;
        waiting = new Object();
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        dealerThread = Thread.currentThread();
        for (int i = 0; i < env.config.players; i++) {
            Thread playerThread = new Thread(players[i], env.config.playerNames[i]);
            playerThread.start();
        }

        env.logger.info("Thread " + Thread.currentThread().getName() + " starting.");

        while (!shouldFinish()) {
            synchronized (activeLock) {
                isActive = true;
                setBlockForAll(true);
                removeAllCardsFromTable();
                shuffle(deck);
                putLegalSetInTable();
                placeCardsOnTable();
                if (env.config.hints) {
                    System.out.println("new hent");
                    table.hints();
                }

                isActive = false;
                setBlockForAll(false);
                reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
                updateTimerDisplay(true);

                for (int i = 0; i < env.config.players; i++) {
                    if (players[i].waiting_To_Dealer_Answer)
                        continue;
                    synchronized (players[i]) {
                        players[i].notifyAll();
                    }
                }
            }

            timerLoop();

        }
        synchronized (activeLock) {
            removeAllCardsFromTable();
        }
        terminate();
        announceWinners();
        System.out.println("the dealer is about to finish");
        env.logger.info("Thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did
     * not time out.
     */
    private void timerLoop() {
        while (!terminate && timer >= 0) {
            sleepUntilWokenOrTimeout();
            synchronized(waiting){
                try{
                    waiting.wait(3);
                }catch (InterruptedException ignored) {}
            }
            updateTimerDisplay(false);
            removeCardsFromTable();

        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
        for (int i = env.config.players - 1; i >= 0; i--) {
            players[i].terminate();
        }

    }

    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        if (offersQueue.size() > 0) {
            Offer currentOffer = offersQueue.poll();
            int playerId = currentOffer.getPlayer();
            int[] cards = currentOffer.getCards();
            updateTimerDisplay(false);
            if (!legalOffer(currentOffer)) {

                synchronized (players[playerId]) {
                    players[playerId].notifyAll();
                }
                return;
            }
            if (env.util.testSet(cards)) {

                players[playerId].givePoint();
                synchronized (players[playerId]) {
                    players[playerId].notifyAll();
                }

                synchronized (activeLock) {
                    isActive = true;
                    setBlockForAll(true);
                    for (int i = 0; i < SetSize; i++) {
                        int currentSlot = table.cardToSlot[cards[i]];
                        removeAllTokensOnSlot(currentSlot);
                        emptySlots.add(currentSlot);
                        table.removeCard(currentSlot);
                    }

                    placeCardsOnTable();
                    if (env.config.hints) {
                        System.out.println("new hent");
                        table.hints();
                    }
                    setBlockForAll(false);
                    isActive = false;
                    updateTimerDisplay(true);
                    for (int i = 0; i < env.config.players; i++) {
                        if (players[i].waiting_To_Dealer_Answer)
                            continue;
                        synchronized (players[i]) {
                            players[i].notifyAll();
                        }
                    }
                }

                // break;
            } else {
                players[playerId].givePenalty();
                synchronized (players[playerId]) {
                    players[playerId].notifyAll();
                }
            }

        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    public void placeCardsOnTable() {
        shuffle(emptySlots);
        int numberOfEmptySlots = emptySlots.size();

        for (int i = 0; i < numberOfEmptySlots; i++) {
            if (deck.size() > 0) {
                table.placeCard(removeLastFromTheDeck(), emptySlots.remove(emptySlots.size() - 1));
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some
     * purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        updateTimerDisplay(false);
        if (timer <= env.config.turnTimeoutWarningMillis) {
            return;
        }

        try {
            synchronized (this) {
                synchronized (offersLock) {
                    inside_his_synchronized = true;
                    if (offersQueue.size() > 0) {
                        inside_his_synchronized = false;
                        return;
                    }

                }
                wait(1000);
            }

        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        if (reset) {
            reshuffleTime = System.currentTimeMillis() + env.config.turnTimeoutMillis;
            timer = env.config.turnTimeoutMillis;
            env.ui.setCountdown(timer, false);
        } else {
            long currentTime = System.currentTimeMillis();
            timer = reshuffleTime - currentTime;
            long display = timer + 999;
            if (env.config.turnTimeoutWarningMillis > 0 && timer <= env.config.turnTimeoutWarningMillis) {
                if (timer < 0) {
                    env.ui.setCountdown(0, true);
                } else {
                    env.ui.setCountdown(timer, true);
                }
            } else {
                env.ui.setCountdown(display, false);
            }
        }
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    public void removeAllCardsFromTable() {
        for (int i = 0; i < env.config.players; i++) {
            players[i].clearQueue();
        }
        removeAllTokensFromtable();
        int tableSize = env.config.tableSize;
        List<Integer> currentSlots = IntStream.range(0, tableSize).boxed().collect(Collectors.toList());
        ;
        removeEmptySlots(currentSlots);
        shuffle(currentSlots);
        for (int i = 0; i < currentSlots.size(); i++) {
            int currentSlot = currentSlots.get(i);
            deck.add(table.slotToCard[currentSlot]);
            emptySlots.add(currentSlot);
            table.removeCard(currentSlot);
        }
        shuffle(emptySlots);
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    public void announceWinners() {
        List<Integer> score = new ArrayList<>(players.length);
        int max = players[0].score();
        for (int i = 0; i < players.length; i++) {
            score.add(players[i].score());
            if (players[i].score() >= max) {
                max = players[i].score();
            }
        }
        int sumWhoWon = Collections.frequency(score, max);
        int[] whoWon = new int[sumWhoWon];
        int j = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i].score() == max) {
                whoWon[j] = i;
                j++;
            }
        }
        env.ui.announceWinner(whoWon);
    }

    /* new helper methods */
    private int removeLastFromTheDeck() {
        return deck.remove(deck.size() - 1);
    }

    private void shuffle(List<Integer> torandomize) {
        Collections.shuffle(torandomize);
    }

    private void removeEmptySlots(List<Integer> currentSlots) {
        for (int i = 0; i < emptySlots.size(); i++) {
            currentSlots.remove(emptySlots.get(i));
        }
    }

    public void removeAllTokensOnSlot(int slot) {
        for (int i = 0; i < env.config.players; i++) {
            if (table.tokensOnTable[slot][i] == true) {

                players[i].removeToken(slot);
                players[i].tokenIncreament();
                ;
                table.removeToken(i, slot);
            }
        }
    }

    public void setBlockForAll(boolean status) {
        int numberOfPlayers = env.config.players;
        for (int i = 0; i < numberOfPlayers; i++) {
            players[i].setBlock(status);
        }
    }

    private void removeAllTokensFromtable() {
        int numberOfSlots = env.config.tableSize;
        for (int i = 0; i < numberOfSlots; i++) {
            removeAllTokensOnSlot(i);
        }
    }

    private boolean legalOffer(Offer offer) {
        int playerID = offer.getPlayer();
        int leftTokens = players[playerID].getLeftTokens();
        int[] cards = offer.getCards();
        boolean legalTokens = true;
        boolean legalCards = true;
        if (leftTokens != 0) {
            legalTokens = false;
        }
        for (int i = 0; i < cards.length; i++) {
            if (table.cardToSlot[cards[i]] == null) {
                legalCards = false;
            }
        }
        if (legalCards && legalTokens) {
            return true;
        }
        return false;
    }

    private void putLegalSetInTable() {
        if (deck.size() > 12) {
            List<int[]> legalSet = env.util.findSets(deck, 1);
            int[] set = legalSet.get(0);
            for (int i = 0; i < SetSize; i++) {
                deck.remove((Integer) set[i]);
                deck.add((Integer) set[i]);
            }
        }
    }

    /*func for utest */
    public void clearDeck(){
        deck.clear();
    }
    public boolean giveShouldFinish(){
        return shouldFinish();
    }
    public void setTerminate(boolean terminate){
        this.terminate=terminate;
    }
}
