/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import play.db.jpa.Model;

/**
 *
 * @author Santonas
 */
@Entity
public class Team extends Model {

    //@Column(unique=true)
    public String team_name = null;
    public String bio = "";
    public Date registered = new Date();
    public String image = "team.png";
    public int won = 0;
    public int lost = 0;
    @ManyToOne
    public Player player1 = null;
    @ManyToOne
    public Player player2 = null;
    @OneToOne
    public Team arch_rival = null;
    
    public double rating = 1500;

    public int memberCount() {
        int count = 0;
        if (this.player1 != null) {
            count++;
        }
        if (this.player2 != null) {
            count++;
        }
        return count;
    }

    @Override
    public String toString() {
        return team_name;
    }
}
