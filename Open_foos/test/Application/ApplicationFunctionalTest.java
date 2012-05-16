/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;

import org.junit.Test;
import play.mvc.Http;
import play.test.FunctionalTest;

/**
 *
 * @author Santonas
 */
public class ApplicationFunctionalTest extends FunctionalTest {
     
    @Test
    public void testThatErrorPageWorks() {
        Http.Response response = GET("/error");
        //assertStatus(200, response);
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatMainPageWorks() {
        Http.Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatGamePageWorks() {
        Http.Response response = GET("/game");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatLoginPageWorks() {
        Http.Response response = GET("/login");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatRegisterPageWorks() {
        Http.Response response = GET("/register");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatAdminPageWorks() {
        Http.Response response = GET("/admin");
        assertStatus(302, response);
        assertHeaderEquals("Location", "/secure/login", response);

    }

    @Test
    public void testThatEverybodyJsonWorks() {
        Http.Response response = GET("/application/everybody");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }

    @Test
    public void testThatAutocompleteWorks() {
        Http.Response response = GET("/api/player/autocomplete/neberd");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
    @Test
    public void testThatRfidJsonWorks() {
        Http.Response response = GET("/api/player/login/rfid/1");
        assertIsOk(response);
        assertContentType("application/json", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    
    @Test
    public void testThatPlayerProfileWorks() {
        Http.Response response = GET("/players/profile/neberd");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
    @Test
    public void testThatTeamProfileWorks() {
        Http.Response response = GET("/teams/profile/neberd1");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset(play.Play.defaultWebEncoding, response);
    }
}
