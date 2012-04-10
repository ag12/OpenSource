/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package securities;

import controllers.Secure;
import models.Player;
import play.libs.Crypto;

/**
 *
 * @author Santonas
 */
public class Security extends Secure.Security {

    public static boolean authenticate(String username, String password) {

        Player player = Player.find("byUsername", username).first();

        return (player != null && player.password.equals(password));
    }

    public static String enc_password(String password) {

        return Crypto.passwordHash(password);
    }
}
