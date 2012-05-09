package controllers;

import java.util.ArrayList;
import java.util.List;
import models.Game;
import models.Player;
import models.Team;
import play.mvc.Controller;
import repositories.GameRepository;

public class Application extends Controller {

    public static void index() {
       
        render();
    }

    public static void game() {
        render();
    }

    public static void main_page() {
       List<Game> games =  GameRepository.getOngoingGames();
       List<Team> teams = TeamController.getTopRankedTeams(5);
       render();
    }

    public static void login() {
        render();
    }
    public static void register() {
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
    
    public static String redirectToProfile(String who){
       
        Player player = Player.find("byUsername", who).first();
        if (player != null) {
            return "/players/profile/" + who;
        }
        Team team = Team.find("byTeam_name", who).first();
        if (team != null) {
            return "/teams/profile/" + who;
        }
        if (team == null && player == null) {
            return "/error";
        }
        return "/";     
    }
}