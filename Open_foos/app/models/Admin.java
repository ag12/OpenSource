/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Date;
import javax.persistence.Entity;
import play.data.validation.Email;
import play.data.validation.Password;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 *
 * @author Santonas
 */
@Entity
public class Admin extends Model{
    
    @Required
    public String username;
    @Required
    @Email
    public String email;
    @Required
    @Password
    public String password; 
    public Date registered = new Date();
    @Required
    public boolean activ = true;
    
}
