/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import play.data.validation.Email;
import play.db.jpa.Model;

/**
 *
 * @author Santonas
 */
@Entity
public class Player extends Model {

    
    public String username = null;
    public String password = null;
    public Long rfid = null;
    
    @Email
    public String email = null;
    public String image = null;
    public String bio = null;
    public Date registered = null;
    
    @OneToOne
    public Player arch_rival = null;
}
