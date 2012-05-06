/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import play.data.validation.Email;
import play.data.validation.MinSize;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.libs.Crypto;

/**
 *
 * @author Santonas
 */
@Entity
public class Player extends Model {

    @Column(unique = true)
    @Required(message = "Username must be uniq")
    @MinSize(3)
    public String username = null;
    @Required(message = "")
    @MinSize(6)
    @Password
    public String password = null;
    @Column(unique = true)
    public Long rfid = null;
    public String first_name = null;
    public String last_name = null;
    @Email
    public String email = null;
    //default "player.png"
    public String image = "player.png";
    public String bio = null;
    public Date registered = null;

    @Override
    public String toString() {



        return username;
    }
}
