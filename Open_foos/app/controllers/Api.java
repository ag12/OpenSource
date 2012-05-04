package controllers;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Game;
import models.Player;
import models.Team;
import org.h2.util.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import securities.Security;

public class Api extends Controller
{
    
    /*
      Useful methods: 
      await("5s"); // wait for 5 seconds
     */
    
    public static void autocomplete(String username)
    {
        
       if(StringUtils.isNullOrEmpty(username))
       {
          badRequest();
          return;
       } 
       
       List<Player> players = Player.find("byUsernameLike", username + "%").fetch(10);
       ArrayList<String> usernames = new ArrayList<String>();
       for(Player player : players)
       {
         usernames.add(player.username);    
       }
       
       renderJSON(usernames);
    }
    
    public static void fetchPlayer(String username, String password)
    {
       //await("5s");
       if(StringUtils.isNullOrEmpty(username) || StringUtils.isNullOrEmpty(password))
       {
          error(Http.StatusCode.BAD_REQUEST, "Username or password cannot be empty"); 
       }   
        
       Player player = Player.find("username = ? and password = ?", username, Security.encPassword(password)).first();
       
       if(player == null)
       {
          error(Http.StatusCode.BAD_REQUEST, "Could not locate a player with the given username and password"); 
       }
       
       else
       {
          //Logger.info("I authenticated " + username, null);
          renderJSON(player);
       }
    }
    
    public static void registerPlayer(JsonElement body)
    {
      
       Player player = construct(body, Player.class);  
       
       if(StringUtils.isNullOrEmpty(player.username) || StringUtils.isNullOrEmpty(player.password))
       {
          error(Http.StatusCode.BAD_REQUEST, "Username or password cannot be empty"); 
          return;
       }
       
       // Check if a player with the given username 
       // allready exist
       Player existingPlayer = Player.find("username = ?", player.username).first();
       if(existingPlayer != null)
       {
         error(Http.StatusCode.BAD_REQUEST, "A player allready exsist with this username"); 
         return;
       }
       
       else
       {
          //Logger.info("I registered " + username, null);
          player.password = Security.encPassword(player.password);
          player.save();
          // 
          Team team = TeamController.register_team(player,null);
          team.save();
          renderJSON(player);
       }
    }
    
    
    public static void fetchTeam(JsonElement body)
    {
       Team team = construct(body, Team.class);
       
       if(team == null)
           badRequest();
       
       if(team.player1.id == null)
           team.player1 = null;
       
       if(team.player2.id == null)
           team.player2 = null;
       
       if(team.memberCount() == 0)
           badRequest();
       
       if(team.player1 == null && team.player2 != null)
       {
           team.player1 = team.player2; 
           team.player2 = null;
       }
       
       //Make sure that the players are ordered in an
       //ascending order
       if(team.player2 != null)
       {
           if(team.player2.id < team.player1.id)
           {
               Player temp = team.player1;
               team.player1 = team.player2; 
               team.player2 = temp;
           } 
       }

       Team verifiedTeam = null;
       //Check if the team allready exsists
       if(team.memberCount() == 1)
          verifiedTeam = Team.find("player1_id = ? and player2_id = null", team.player1.id).first(); 
       
       
       else if(team.memberCount() == 2)
          verifiedTeam = Team.find("player1_id = ? and player2_id = ?", team.player1.id, team.player2.id).first();
       
       
       //We tried to look for the team
       //specified, if we coult not find it, then we will create it;
       if(verifiedTeam == null)
       {
         String name = "Team " + team.player1.username; 
         if(team.memberCount() > 1) 
             name += " & " + team.player2.username;
                     
         team.team_name = name;  
         verifiedTeam = team.save();
       }
        
       
       if(verifiedTeam == null)
           error("Could not find or create the team"); 
       else
       {
           Logger.info("i authenticated team: " + verifiedTeam.getId());
           renderJSON(verifiedTeam);
       }
    }
    
    public static void initGame(JsonElement body)
    {
       Game game = construct(body, Game.class);
       if(game == null)
           badRequest();
             
       //TODO: lagene må verifiseres
       game.start_time = new Date();
       Game verifiedGame = game.save();
       Logger.info("Verdified game with id: " + verifiedGame.getId());
       renderJSON(verifiedGame); 
    }

    public static void updateGame(JsonElement body)
    {
       Game game = construct(body, Game.class);
       if(game == null)
           badRequest();

       if(game.getId() < 1)
           badRequest();
       
       Game verified = Game.findById(game.getId());
       if(verified != null)
       {
           //TODO husk å validere disse verdiene
           //At de ikke er null; 
           verified.home_score = game.home_score; 
           verified.visitor_score = game.visitor_score; 
           verified.winner_id = (verified.home_score > verified.visitor_score ? verified.home_team.id : verified.visitor_team.id); 
           
           verified.end_time = new Date();
           verified.save(); 
           renderJSON(verified);
       }
       badRequest();
    }
    
    private static <T extends Object> T construct(JsonElement json, Class<T> classOfT)
    {
       if(json == null)
       return null;
       
       Gson gson = new Gson(); 
       
       try
       {
        return gson.fromJson(json, classOfT);
       }catch(JsonSyntaxException e)
       {
         return null;
       }
    }
    
}
