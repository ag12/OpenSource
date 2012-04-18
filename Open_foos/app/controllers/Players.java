/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.util.Date;
import java.util.List;
import models.Game;
import models.Player;
import models.Statistic;
import models.Team;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.Images;
import play.mvc.Controller;
import repositories.StatisticRepository;
import validations.PlayerValidations;

/**
 *
 * @author Santonas
 */
public class Players extends Controller {

//    public static void profile() {
//        render();
//    }

    public static void settings() {

        Long id = Long.parseLong(session.get("pid"));
        String username = session.get("pname");
        if (id != null && (username != null && !username.equals(""))) {


            Player player = Player.find("id = ? and username = ?", id, username).first();
            Team team = Team.find("player1_id = ? AND player2_id = NULL", id).first();
            render(player, team);
        } else {
            controllers.Application.error();
        }



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
                            
            List<Game> games = Games.getTeamGames(team.id);
            
            
            render( (player != null ? player : null), 
                    (team != null ? team : null), 
                    (teams != null ? teams : null), 
                    (statistic != null ? statistic : null), 
                    (statistics != null ? statistics : null), 
                    (teams_statistics != null ? teams_statistics : null),
                    (games != null ? games : null));
            
            
        } else if (player == null) {
//            controllers.Application.error("Oooobs");
//            error(666, "Cant find " + username);
        }
    }

    public static void logout_player() {

        controllers.Application.afterLogout();
        controllers.Application.main_page();
    }

    public static void edit_player(Player player, File image) {

        Player existingplayer = null;
        Long id = Long.parseLong(session.get("pid"));
        String username = session.get("pname");
        if (id != null && (username != null && !username.equals(""))) {


            existingplayer = Player.find("id = ? and username = ?", id, username).first();


        }

        //indicates if player have changed any info
        boolean hasChanged = false;
        
        
        
        
         //FIRST_NAME
        if ((!player.first_name.equals("") && player.first_name != null)) {

            if (existingplayer.first_name == null || !existingplayer.first_name.equals(player.first_name)) {

                existingplayer.first_name = player.first_name;
                hasChanged = true;
            }

        }
        
        //LAST_NAME
        if ((!player.last_name.equals("") && player.last_name != null)) {

            if (existingplayer.last_name == null || !existingplayer.last_name.equals(player.last_name)) {

                existingplayer.last_name = player.last_name;
                hasChanged = true;
            }

        }
        
        
        
        
        
        
        
        //BIO
        if ((!player.bio.equals("") && player.bio != null)) {

            if (existingplayer.bio == null || !existingplayer.bio.equals(player.bio)) {

                existingplayer.bio = player.bio;
                hasChanged = true;
            }

        }
        
        
        //EMAIL
        if ((!player.email.equals("") && player.email != null)) {

            if (existingplayer.email == null || !existingplayer.email.equals(player.email)) {

                existingplayer.email = player.email;
                hasChanged = true;
            }

        }

        //RFID
        if (player.rfid != null && player.rfid >= 1) {
            if (existingplayer.rfid == null || existingplayer.rfid != player.rfid) {
                existingplayer.rfid = player.rfid;
                hasChanged = true;
            }
        }

        //IMAGE
        if (image != null) {

            String imageEnd = "";
            if (image.getName().endsWith(".png")) {

                imageEnd = ".png";
            }
            if (image.getName().endsWith(".gif")) {

                imageEnd = ".gif";
            }
            if (image.getName().endsWith(".jpg")) {

                imageEnd = ".jpg";
            }

            String playerImage = "openfoos_player_" + id + imageEnd;
            existingplayer.image = playerImage;
            String main_path = Play.applicationPath + "/public/images/";
            String small_path = main_path + "small/" + playerImage;
            String xsmall_path = main_path + "xsmall/" + playerImage;
            String medium_path = main_path + "medium/" + playerImage;
            String large_path = main_path + "large/" + playerImage;
            
           
            //since the image name i uniqe here, the system wil auto change players image if exists
            //xsmall 72x72       
            Images.resize(image, new File(xsmall_path), 72, 72, false);
            //small 127x80
            Images.resize(image, new File(small_path), 127, 80, false);
            //medium 85x120 
            Images.resize(image, new File(medium_path), 85, 120, false);
            //Large 260x180
            Images.resize(image, new File(large_path), 180, 260, false);


            hasChanged = true;

        }

        if (hasChanged) {

            existingplayer.save();

        }
        settings();
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
