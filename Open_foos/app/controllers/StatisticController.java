/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import models.Player;
import models.Statistic;
import models.Team;
import play.mvc.Controller;
import repositories.StatisticRepository;

/**
 *
 * @author Santonas
 */
public class StatisticController extends Controller {
    
    public static void data(Long id) {

        Statistic statistic = StatisticRepository.getStatistics(id);
        renderJSON(statistic);
    }
    
    public static void statisticForWho(String name){
        
        Player player = Player.find("byUsername", name).first();
        if(player!= null){
            Team team = statisticTeamForPlayer(player.getId());
            if ( team != null){
                 renderJSON(team);
            }
           
        }else 
        {
            Team team = Team.find("byTeam_name", name).first();
            if ( team != null){
                renderJSON(team);
            }
        }  
    }
    
    public static Team statisticTeamForPlayer(Long player1_id){
        return controllers.TeamController.getTeam(player1_id);
    }
}
