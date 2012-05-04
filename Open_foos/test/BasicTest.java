import controllers.PlayerController;
import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {
    
    @Before
public void setup() {
    Fixtures.deleteDatabase();
    //Fixtures.loadYaml("data.yml");
}

    @Test
    public void aVeryImportantThingToTest() {
        assertEquals(2, 1 + 1);
        
        Player p = new Player();
        Player pp = new Player();
        p.username = "jan";
        //Fixtures.loadModels("start-data.yml");
        Fixtures.loadYaml("data.yml");
        
        //assertEquals(5, Player.count());
        assertNotNull(PlayerController.getPlayersFromDb("barbaqu"));
        
    }

}
