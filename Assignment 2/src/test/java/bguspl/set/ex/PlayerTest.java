package bguspl.set.ex;

import bguspl.set.Config;
import bguspl.set.Env;
import bguspl.set.UserInterface;
import bguspl.set.Util;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlayerTest {

    Player player;
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

    void assertInvariants() {
        assertTrue(player.id >= 0);
        assertTrue(player.score() >= 0);
    }

    @BeforeEach
    void setUp() {
        // purposely do not find the configuration files (use defaults here).
        Properties properties = new Properties();
        properties.put("PointFreezeSeconds", "1");
        properties.put("PenaltyFreezeSeconds","3");

        logger = new TableTest.MockLogger();
        Config config = new Config(logger, properties);
        env = new Env(logger,config, ui, util);
        player = new Player(env, dealer, table, 0, false);
        assertInvariants();
    }

    @AfterEach
    void tearDown() {
        assertInvariants();
    }

    @Test
    void point() {

        // force table.countCards to return 3
        //when(table.countCards()).thenReturn(3); // this part is just for demonstration

        // calculate the expected score for later
        int expectedScore = player.score() + 1;

        // call the method we are testing
        player.point();

        // check that the score was inc(long)reased correctly
        assertEquals(expectedScore, player.score());

        // check that ui.setScore was called with the player's id and the correct score
        verify(ui).setScore(eq(player.id), eq(expectedScore));
        
    }
    /**
     * 
     */
    @Test
    void paneltyTests(){
        player.penalty();
        long panTime=env.config.penaltyFreezeMillis;
        while(panTime>=0){
            if (panTime < 1) {
                verify(ui).setFreeze(player.id, panTime);
            }
            verify(ui).setFreeze(player.id, panTime);
            panTime-=1000;
        }

    }
    @Test
    void pointTest(){
        player.point();
        long pointTime=env.config.pointFreezeMillis;
        while(pointTime>=0) {
            if (pointTime < 1) {
                verify(ui).setFreeze(player.id, pointTime);
            }
            verify(ui).setFreeze(player.id, pointTime);
            pointTime -= 1000;
        }

        verify(ui).setScore(player.id, player.score() );
    }


    }
