/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Game;
import models.Team;

/**
 *
 * @author Santonas
 */
public class GameRepository {
    
    public static List<Game> getOngoingGames(){
         StringBuilder sqlToQuery = 
                 new StringBuilder("SELECT Game.id FROM Game, Team ");
        sqlToQuery.append("WHERE ((Game.home_team_id = Team.id OR Game.visitor_team_id = Team.id) ");
        sqlToQuery.append("AND (Game.end_time is null) AND ( NOW() - Game.start_time < 10800 )) ");
        sqlToQuery.append("GROUP BY Game.id DESC LIMIT 10;");
        
         ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
         List<Game> games = new ArrayList<Game>();
         try{
             while(resultset.next()){
                Game game = Game.findById(resultset.getLong(1));
                games.add(game);
             }       
         }
         catch(SQLException e){           
               System.out.println(e.toString());
         }catch(Exception e){
              System.out.println(e.toString());
         }
        return games; 
    }
}
