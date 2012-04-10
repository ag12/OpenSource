/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Date;
import java.util.List;
import models.Player;
import models.Statistic;
import models.Team;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;
import repositories.StatisticRepository;
import validations.PlayerValidations;

/**
 *
 * @author Santonas
 */
public class Players extends Controller {

    public static void profile() {
        render();
    }

    public static void settings() {
        render();
    }

    public static void register_player(@Valid Player player) {

        if (Validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            Validation.keep();   // keep the errors for the next request
            controllers.Application.main_page();
        }

        
        Player exist = Player.find("byUsername", player.username).first();
        if (exist != null) {
            Validation.addError("message", "The username have been used.", player.username);
            Validation.keep();
            controllers.Application.main_page();
        } else if (exist == null) {

            player = PlayerValidations.trimAtRegistr(player);
            player.registered = new Date();
            player.password = securities.Security.enc_password(player.password);
            Team team = new Team();
            team.team_name = "Team_" + player.username;
            team.player1 = player;
            team.registered = new Date();
            player.save();
            team.save();
            controllers.Application.afterLogin(player);
            profile(player.username);

        }
    }

    public static void login_player(@Valid Player player) {

        boolean b = Boolean.parseBoolean(session.get("login"));
        if (b) {
            controllers.Application.afterLogout();
        }

        Long rfid = null;
        if (!player.username.equals("") && player.username != null) {
            try {
                rfid = Long.parseLong(player.username);
                System.out.println(rfid);

            } catch (NumberFormatException e) {
            }
            if (rfid != null && rfid > 999999999) {

                Player exist = Player.find("byRfid", rfid).first();

                if (exist != null) {

                    controllers.Application.afterLogin(exist);
                    profile(exist.username);

                } else if (exist == null) {

                    Validation.addError("message", "The system cant find you with rfid.", player.username);
                    Validation.keep();
                    controllers.Application.main_page();

                }
            }
        }

        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            controllers.Application.main_page();
        }

        player.password = securities.Security.enc_password(player.password);
        Player exist = Player.find("byUsernameAndPassword",
                player.username, player.password).first();

        if (exist == null) {
            Validation.addError("message", "The system cant find u, rigister ur self", player.username);
            Validation.keep();
            controllers.Application.main_page();

        } else if (exist != null) {

            controllers.Application.afterLogin(exist);
            profile(player.username);
        }


    }

    public static void profile(String username) {


        username = PlayerValidations.sqlInjection(username);

        Player player = Player.find("byUsername", username).first();
        if (player != null) {
            
            Team team = Teams.getTeam(player.id);
            
            List<Team> teams = Teams.getTeams(team.id);
            
            List<Statistic> teams_statistics = StatisticRepository.getMoreInfoForTeams(teams);
            
            Statistic statistic = StatisticRepository.getStatistics(team.id);
            List<Statistic> statistics = new StatisticRepository().getMoreInfo(team.id);
            render(player 
                    ,(team != null ? team : null)
                    ,(teams != null ? teams : null)
                    ,(statistic != null ? statistic : null)
                    ,(statistics != null ? statistics : null)
                    ,(teams_statistics != null ? teams_statistics : null));
        } else if (player == null) {
//            controllers.Application.error("Oooobs");
//            error(666, "Cant find " + username);
        }
    }

    public static void logout_player() {

        controllers.Application.afterLogout();
        controllers.Application.main_page();
    }
    
    
    
    
    
    
    
    
    
    
    
    public static void allPlayers() {


        renderJSON(getPlayersFromDb(null));
    }

    public static void allPlayersByUsername(String filter) {

        renderJSON(getPlayersFromDb(filter));

    }

    public static List<Player> getPlayersFromDb(String filter) {



        if (filter != null) {
            List<Player> players = Player.find("byUsernameLike", filter + "%").fetch();
            return players;
        } else {
            List<Player> players = Player.findAll();
            return players;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
