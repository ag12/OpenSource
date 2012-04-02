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
public class Game extends Model {

    public Long winner_id = null;
    public String state = null;
    public Date start_time = null;
    public Date end_time = null;
   
    
    
    @ManyToOne
    public Team home_team = null;
    @ManyToOne
    public Team visitor_team = null;
    
   
    public int home_score = 0;
    public int visitor_score = 0;
}
