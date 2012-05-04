package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;
import play.libs.Crypto;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void game() {
        render();
    }

    public static void main_page() {
        render();
    }

    public static void login() {
        System.out.println("OK");
        render();
    }
    
    public static void ofError(){
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

    public static boolean auto(Long id) {
        return Long.parseLong(session.get("pid")) == id;
    }

    public static List<Object> teamsAndPlayers() {

        List<Object> teamsAndPlayers = new ArrayList<Object>();
        List<Team> teams = Team.findAll();
        List<Player> players = Player.findAll();
        for (int i = 0; i < teams.size(); i++) {
            teamsAndPlayers.add(teams.get(i));
        }
        for (int i = 0; i < players.size(); i++) {
            teamsAndPlayers.add(players.get(i));
        }
        return teamsAndPlayers;
    }

    public static void autocomplete() {

        List<Object> list = teamsAndPlayers();
        renderJSON(list);
    }
    
    public static String whoIsIt(String who){
        
        System.out.println("Inside who");
        Player player = Player.find("byUsername", who).first();
        if (player != null) {
            System.out.println("Inside player != null");
            //Players.profile(who);
            return "/players/profile/" + who;
        }
        System.out.println("outside first if");
        Team team = Team.find("byTeam_name", who).first();
        if (team != null) {
            System.out.println("Inside team != null");
           // Teams.profile(who);
            return "/teams/profile/" + who;
        }
        System.out.println("Outside 2.if");
        if (team == null && player == null) {
            System.out.println("team == player == null");
            ofError();
            return "";
        }
        return "";     
    }
}