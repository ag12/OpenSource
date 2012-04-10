/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import models.Team;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class Teams extends Controller {
    
    public static Team getTeam(Long player_id){
        Team team = Team.find("player1_id = ? AND player2_id = NULL", player_id).first();
        return team;
    }
    
    
}
