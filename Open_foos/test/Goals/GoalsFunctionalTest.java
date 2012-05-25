/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Goals;

import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 *
 * @author Santonas
 */
public class GoalsFunctionalTest extends FunctionalTest {

    @Test
    public void testThatGoalsJsonWorks() {
        Http.Response response = GET("/application/goals");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
}
