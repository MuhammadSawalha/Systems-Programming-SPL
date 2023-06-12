package bguspl.set.ex;

public class Action {
    private int slot;
    private int card;


    public Action(int slot, int card){
        this.slot = slot;
        this.card = card;
    }

    public int getSlot(){
        return slot;
    }
    public int getCard(){
        return card;
    }
}
