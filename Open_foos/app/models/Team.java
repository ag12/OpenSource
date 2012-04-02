/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import play.db.jpa.Model;

/**
 *
 * @author Santonas
 */
@Entity
public class Team extends Model {
    
    public String team_name = null;
    
    public String bio = null;
    
    public Date registered = null;
    
    public String image = null;
   
    @ManyToOne
    public Player player1 = null;
    @ManyToOne
    public Player player2 = null;
    
    public int memberCount()
    {
        int count = 0;
        if(this.player1 != null) 
            count++; 
        if(this.player2 != null)
            count++; 
        return count;
    }

}
