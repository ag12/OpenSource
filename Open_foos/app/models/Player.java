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

/**
 *
 * @author Santonas
 */
@Entity
public class Player extends Model {

    @Column(unique = true)
    @Required
    @MinSize(1)
    public String username = null;
    @Required
    @MinSize(1)
    @Password
    public String password = null;
    @Column(unique = true)
    public Long rfid = null;
    public String first_name = "";
    public String last_name = "";
    @Email
    public String email = "";
    //default "player.png"
    public String image = "player.png";
    public String bio = "";
    public Date registered = new Date();

    @Override
    public String toString() {
        return username;
    }
}
