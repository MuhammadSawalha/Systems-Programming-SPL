package bguspl.set.ex;

public class Offer {
    private int[] cards;
    private int player;
    public Offer(int[] cards, int player){
        this.cards=cards;
        this.player=player;
    }
    public int[] getCards(){
        return cards;
    }
    public int getPlayer(){
        return player;
    }
}
