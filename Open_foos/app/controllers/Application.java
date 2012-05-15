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
       List<Team> topRanked = TeamController.getTopRanked(6);
       List<Team> topTeams = TeamController.getTopRankedTeams(6);
       List<Team> topPlayer = TeamController.getTopRankedPlayers(6);
       List<Team> biggestWinner = TeamRepository.getBiggestWinner();
       List<Team> biggestLoser = TeamRepository.getBiggestLoser();
       List<Statistic> topRankedStatistics = null;
       List<Statistic> topTeamsStatistics = null;
       List<Statistic> topPlayerStatistics = null;
       if ( topRanked != null && topRanked.size() > 0){
           topRankedStatistics = StatisticRepository.getMoreInfoForTeams(topRanked);
           
       }
       if ( topTeams != null && topTeams.size() > 0 ){
           topTeamsStatistics = StatisticRepository.getMoreInfoForTeams(topTeams);
       }
       if (topPlayer != null && topPlayer.size() > 0){
           topPlayerStatistics = StatisticRepository.getMoreInfoForTeams(topPlayer);
       }
       
       render(topRanked,
               onGoingGames,
               biggestWinner,
               biggestLoser,
               onlinePlayer,
               statistic,team,
               topTeams,
               topPlayer,
               topTeamsStatistics,
               topRankedStatistics,
               topPlayerStatistics);
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
        List<Team> teams = Team.findAll();//Team.find("player1_id != NULL AND player2_id != NULL").fetch();
        List<Player> players = Player.findAll();
        for (int i = 0; i < teams.size(); i++) {
           
            if ( teams.get(i) != null && teams.get(i).memberCount() == 2){
               teamsAndPlayers.add(teams.get(i)); 
            }else if ( teams.get(i) != null && teams.get(i).memberCount() == 1){
                if(!teams.get(i).team_name.equals(teams.get(i).player1.username)){
                    teamsAndPlayers.add(teams.get(i)); 
                }
            }
                
            
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