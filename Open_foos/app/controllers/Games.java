/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Date;
import java.util.List;
import models.Game;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class Games extends Controller {
    
    
    
    public static List<Game> getTeamGames(Long team_id){
        
        List<Game> games = Game.find("(home_team_id = ? or visitor_team_id = ?) and (end_time != 0 )order by id desc", team_id,team_id).fetch();
        for ( int i  =0; i < games.size(); i++)
        {
            if ( games.get(i).end_time != null){
                 coutGameDiff(games.get(i));
            }
           
        }
        return games;
        
    }
    
    public static int coutGameDiff(Game game){
        
        
        long diff = game.start_time.getTime() - game.end_time.getTime();
        System.out.println("DIFF " + diff);
        System.out.println("DATE " + new Date(diff));
        return 0;
    }
    
}
