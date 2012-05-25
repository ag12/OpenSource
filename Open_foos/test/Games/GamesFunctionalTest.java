/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Games;

import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 *
 * @author Santonas
 */
public class GamesFunctionalTest extends FunctionalTest {

    @Test
    public void testThatGamePageWorks() {
        Http.Response response = GET("/game");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
}
