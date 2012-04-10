/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.List;
import models.Team;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class Teams extends Controller {
    
    //Players own team
    public static Team getTeam(Long player_id){
        Team team = Team.find("player1_id = ? AND player2_id = NULL", player_id).first();
        return team;
    }
    
    //Teams where player are a member of
    public static List<Team> getTeams(Long player1_id) {

        List<Team> teams = Team.find("(player1_id = ? or player2_id = ?) and (player2_id != NULL)",
                player1_id, player1_id).fetch();
        return teams;
    }
    
    
}
