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
import play.libs.Crypto;
import play.libs.Images;
import play.mvc.Controller;
import repositories.StatisticRepository;

public class PlayerController extends Controller {

    public static void getPlayer(String username){
        Player p = Player.find("byUsername", username).first();
        renderJSON(p);
    
    }

    /*
     * Here the player can change settings, the actuale code is edit();
     */
    public static void settings() {

        Long id = Long.parseLong(session.get("pid"));

        String username = session.get("pname");

        if (id != null && (username != null && !username.equals(""))) {

            Player player = Player.find("id = ? and username = ?", id, username).first();
                    //getPlayer(id, username);

            Team team = TeamController.getTeam(id);

            int compeleted = (compeletedProfile(player)
                    + controllers.TeamController.compeletedProfile(team));

            render(player, team, compeleted);

        } else {
            controllers.Application.error();
        }
    }

    public static void registerPlayer(@Valid Player player) {

        if (Validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            Validation.addError("register", "Username and password is missing", player.username);
            Validation.keep();   // keep the errors for the next request
            controllers.Application.register();
        }


        Player exist = Player.find("byUsername", player.username).first();//getPlayer(player);
        if (exist != null) {
            Validation.addError("register", "The username have been used.", player.username);
            Validation.keep();
            controllers.Application.register();
        } else if (exist == null) {

           
            player.username = player.username.trim();
            player.password = player.password.trim();
            player.registered = new Date();
            player.password = Crypto.encryptAES(player.password);


            player.save();
            Team team = TeamController.register_team(player, null);
            team.save();
            controllers.Application.afterLogin(player);
            profile(player.username);

        }
    }

    public static void loginPlayer(@Valid Player player) {

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

                    Validation.addError("login", "The system cant find you with radio frequency identification.", player.username);
                    Validation.keep();
                    controllers.Application.login();

                }
            }
        }

        if (Validation.hasErrors()) {
            params.flash();
            Validation.keep();
            Validation.addError("login", "Username and password is missing", player.username);
            controllers.Application.login();
        }

        //player.password = Crypto.encryptAES(player.password);
        Player exist = Player.find("byUsernameAndPassword",
                player.username, Crypto.encryptAES(player.password)).first();//dosPlayerExist(player);

        if (exist == null) {
            Validation.addError("login", "Wrong username or password.", player.username);
            Validation.keep();
            controllers.Application.login();

        } else if (exist != null) {

            controllers.Application.afterLogin(exist);
            profile(player.username);
        }


    }

    public static void profile(String username) {

        Player player = Player.find("byUsername", username).first();
        if (player != null) {

            Team team = TeamController.getTeam(player.getId());

            Statistic statistic = null;
            List<Team> teams = null;
            List<Statistic> statistics = null;
            List<Game> games = null;
            if ( team != null){
            teams = TeamController.getTeams(player.getId());
             
            statistic = StatisticRepository.getStatistics(team.getId());
            statistics = new StatisticRepository().getMoreInfo(team.getId());
            games = GamesController.getTeamGames(team.getId(),"id desc",20);

            }
            List<Statistic> teams_statistics = null;
            if ( teams != null){
            teams_statistics = StatisticRepository.getMoreInfoForTeams(teams);
            }

            render((player != null ? player : null),
                    (team != null ? team : null),
                    (teams != null ? teams : null),
                    (statistic != null ? statistic : null),
                    (statistics != null ? statistics : null),
                    (teams_statistics != null ? teams_statistics : null),
                    (games != null ? games : null));


        } else if (player == null) {

            controllers.Application.ofError();
        }
    }

    public static void logoutPlayer() {

        controllers.Application.afterLogout();
        controllers.Application.main_page();
    }

    public static void editPlayer(Player player, File image) {

        Player existingplayer = null;
        Long id = null;
        if (session.get("pid") != null) {
            id = Long.parseLong(session.get("pid"));
        }
        String username = null;
        if (session.get("pname") != null) {
            username = session.get("pname");
        }

        if (id != null && (username != null && !username.equals(""))) {


            existingplayer = Player.find("id = ? and username = ?", id, username).first();


        }
        //indicates if player have changed any info
        boolean hasChanged = false;
        if (existingplayer != null) {

            //FIRST_NAME
            if ((/*
                     * !player.first_name.equals("") &&
                     */player.first_name != null)) {

                if (existingplayer.first_name == null || !existingplayer.first_name.equals(player.first_name)) {

                    existingplayer.first_name = player.first_name;
                    hasChanged = true;
                }

            }

            //LAST_NAME
            if ((/*
                     * !player.last_name.equals("") &&
                     */player.last_name != null)) {

                if (existingplayer.last_name == null || !existingplayer.last_name.equals(player.last_name)) {

                    existingplayer.last_name = player.last_name;
                    hasChanged = true;
                }
            }
            //BIO
            if ((/*
                     * !player.bio.equals("") &&
                     */player.bio != null)) {

                if (existingplayer.bio == null || !existingplayer.bio.equals(player.bio)) {

                    existingplayer.bio = player.bio;
                    hasChanged = true;
                }

            }
            //EMAIL
            if ((/*
                     * !player.email.equals("") &&
                     */player.email != null)) {

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
                main_path += "players/" + playerImage;
                Images.resize(image, new File(main_path), 200, 160, true);
           

                hasChanged = true;
            }

        }

        if (hasChanged) {

            existingplayer.save();

        }
        settings();
    }

    public static void changePassword(Player player, String newPassword, String newPassword2) {


        if ((player.password == null || player.password.equals("")) || newPassword == null || newPassword2 == null) {
            Validation.addError("message", "Missing informasjon", player.password);
            params.flash();
            Validation.keep();
            settings();
        }




        if (!newPassword.equals(newPassword2)) {

            Validation.addError("message", "Your passwords must bee the same.", player.password);
            params.flash();
            Validation.keep();
            settings();

        } else if (newPassword.equals(newPassword2) && (player.password != null || !player.password.equals(""))) {

            if (session.get("pname") != null && session.get("pid") != null) {
                //ONLIE

                player.username = session.get("pname");
                player = Player.find("byUsernameAndPassword",
                player.username, Crypto.encryptAES(player.password)).first();
                        //dosPlayerExist(player);
               
                
                if (player != null) {

                    newPassword = Crypto.encryptAES(newPassword);
                    player.password = newPassword;
                    player.save();
                    settings();
                }
                if (player == null) {

                    Validation.addError("message", "Your password is inncoret.", "error");
              
                    params.flash();
                    Validation.keep();
                    settings();
                }
            }
        }
    }

    /*
     * This method returns how much data player have about him/her self the
     * return value wil be used as % Max return value == 60
     *
     * Same method is used for teams with a max return value == 40
     */
    public static int compeletedProfile(Player player) {

        int notNull = 0;
        if (player.rfid != null) {
            notNull += 10;
        }
        if (player.first_name != null && !player.first_name.equals("")) {
            notNull += 10;
        }
        if (player.last_name != null && !player.last_name.equals("")) {
            notNull += 10;
        }
        if (player.bio != null && !player.bio.equals("")) {
            notNull += 10;
        }
        if (player.email != null && !player.email.equals("")) {
            notNull += 10;
        }
        if (!player.image.equalsIgnoreCase("player.png")) {
            notNull += 10;
        }
        return notNull;
    }
}
