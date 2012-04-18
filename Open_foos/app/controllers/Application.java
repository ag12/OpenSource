package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void main_page(){
        render();
    }
     //Player is login
    public static boolean afterLogin(Player exist) {
        
        session.put("login", true);
        session.put("pid", exist.id);
        session.put("pname", exist.username);
        String token = session.getAuthenticityToken();
        session.put("token", token);
        return true;

    }

    public static boolean afterLogout() {
        session.clear();
        return true;
    }
    
    public static boolean auto(Long id){
         return Long.parseLong(session.get("pid")) == id; 
    }
    
    
    public static List<Object> teamsAndPlayers(){
        
        List<Object> teamsAndPlayers = new ArrayList<Object>();
        List<Team> teams = Team.findAll();
        List<Player> players = Player.findAll();
        for( int i = 0; i < teams.size(); i ++){
            teamsAndPlayers.add(teams.get(i));
        }
        for( int i= 0; i < players.size(); i++){
            teamsAndPlayers.add(players.get(i));
        }
        return teamsAndPlayers;    
    }
    public static void autocomplete(){
       
        List<Object> list = teamsAndPlayers();
        renderJSON(list);
    }
   
}