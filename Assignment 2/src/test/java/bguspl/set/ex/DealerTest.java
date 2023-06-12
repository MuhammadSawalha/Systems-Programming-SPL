package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class DealerTest {
    @Mock
    Util util;
    @Mock
    private UserInterface ui;
    @Mock
    private Table table;
    @Mock
    private Dealer dealer;
    @Mock
    private Logger logger;
    @Mock
    private Env env;
 
    @BeforeEach
    void setUp() {
        // purposely do not find the configuration files (use defaults here).
        Properties properties = new Properties();
        properties.put("FeatureSize", "3");
        properties.put("FeatureCount", "4");
        properties.put("TableDelaySeconds", "0");
        TableTest.MockLogger logger = new TableTest.MockLogger();
        Config config = new Config(logger, properties);
        env = new Env(logger,config, ui, util);
        table=new Table(env);
        Player p1=new Player(env, dealer, table, 0, true);
        Player p2=new Player(env, dealer, table, 1, true);
        Player[] players={p1,p2};
        dealer=new Dealer(env, table, players);
    }

    
    @Test
    void setBlockForAllTest(){
        dealer.setBlockForAll(true);
        for(int i=0;i<2;i++){
            assertEquals(true, dealer.players[i].blocked);
        }
        dealer.setBlockForAll(false);
        for(int i=0;i<2;i++){
            assertEquals(false, dealer.players[i].blocked);
        }
        
    }

    @Test
    void removeAllTokensOnSlotTest(){
        int slot=2;
        for(int i=0;i<2;i++){
            table.placeToken(i, slot);
            assertEquals(true,table.tokensOnTable[slot][i]);
        }
        dealer.removeAllTokensOnSlot(slot);
        for(int i=0;i<2;i++){
            assertEquals(false,table.tokensOnTable[slot][i]);
        }

    }

     @Test
     void placeCardsoOnTableTest() {
        dealer.placeCardsOnTable();
        for(int i=0;i<env.config.tableSize;i++){
             assertNotNull(table.slotToCard[i]);
         }

     }


     @Test
     void announceWinnersTest(){
        /*player 1 won*/
         dealer.players[0].setScore(0);
         dealer.players[1].setScore(2);
         dealer.announceWinners();
         int[] b={dealer.players[1].id};
         verify(ui).announceWinner(b);
         /*player 0 won*/
         dealer.players[0].setScore(10);
         dealer.players[1].setScore(2);
         dealer.announceWinners();
         int[] z={dealer.players[0].id};
         verify(ui).announceWinner(z);
         /*drew  player 1 and player 2 won*/
         dealer.players[0].setScore(10);
         dealer.players[1].setScore(10);
         dealer.announceWinners();
         int[] e={dealer.players[0].id,dealer.players[1].id};
         verify(ui).announceWinner(e);
     }

     @Test
    void removeAllCardsOnTableTest(){
        dealer.placeCardsOnTable();
         for(int i=0;i<env.config.tableSize;i++){
             assertNotNull(table.slotToCard[i]);
         }
         dealer.removeAllCardsFromTable();
         for(int i=0;i<env.config.tableSize;i++){
             assertNull(table.slotToCard[i]);
         }


     }

    
}
