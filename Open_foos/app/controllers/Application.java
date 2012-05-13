package controllers;

import java.util.ArrayList;
import java.util.List;
import models.Game;
import models.Player;
import models.Statistic;
import models.Team;
import play.mvc.Controller;
import repositories.GameRepository;
import repositories.StatisticRepository;
import repositories.TeamRepository;

public class Application extends Controller {

    public static void index() {
       
        render();
    }

    public static void game() {
        render();
    }

    public static void main_page() {
       Player onlinePlayer = null;
       Team team = null;
       Statistic statistic = null;
       if(session.get("login") != null && session.get("pid") != null){
           boolean login = Boolean.parseBoolean(session.get("login"));
           if (login){
               Long id = Long.parseLong(session.get("pid"));
               onlinePlayer = Player.findById(id);
               if ( onlinePlayer != null){
                   team = TeamController.getTeam(onlinePlayer.getId());
                   statistic = StatisticRepository.getStatistics(team.getId());
                 
               }
           }
       }
       List<Game> onGoingGames =  GameRepository.getOngoingGames();
       List<Team> topRanked = TeamController.getTopRanked(5);
       List<Team> topTeams = TeamController.getTopRankedTeams(5);
       List<Team> topPlayer = TeamController.getTopRankedPlayers(5);
       List<Team> biggestWinner = TeamRepository.getBiggestWinner();
       List<Team> biggestLoser = TeamRepository.getBiggestLoser();
       render(topRanked,onGoingGames,biggestWinner,biggestLoser, onlinePlayer,statistic,team,topTeams,topPlayer);
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
        List<Team> teams = Team.find("player1_id != NULL AND player2_id != NULL").fetch();//Team.findAll();
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