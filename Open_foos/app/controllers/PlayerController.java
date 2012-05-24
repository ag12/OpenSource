/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.util.ArrayList;
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

//    public static void getPlayer(String username) {
//        Player p = Player.find("byUsername", username).first();
//        renderJSON(p);
//
//    }

    /*
     * Here the player can change settings, the actuale code is edit();
     */
    public static void settings(int p) {
        if (p > 3 || p < 1){
            controllers.Application.ofError();
        }

        if (session.get("login") != null && session.get("pid") != null) {

            Long id = Long.parseLong(session.get("pid"));

            String username = session.get("pname");

            if (id != null && (username != null && !username.equals(""))) {

                Player player = Player.find("id = ? and username = ?", id, username).first();

                Team team = TeamController.getTeam(id);

                int compeleted = (compeletedProfile(player)
                        + controllers.TeamController.compeletedProfile(team));
                
                render(player, team, compeleted,p);

            } else {
                controllers.Application.ofError();
            }
        } else {
            controllers.Application.ofError();
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
            player.password = Crypto.encryptAES(player.password);
            player.save();
            Team team = new Team();
            team.player1 = player;
            team.team_name = player.username;
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

            } catch (NumberFormatException e) {
            }
            if (rfid != null && rfid >= 1) {

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

            //players team statistic
            Statistic statistic = null;
            //players teams
            List<Team> teams = null;
            //funn facts
            List<Statistic> statistics = null;
            List<Game> games = null;
            int scoredGoal = GoalController.getIndividualGoalScoreds(player.getId());
            int ownGoal = GoalController.getIndividualOwnGoal(player.getId());

            if (team != null) {
                teams = TeamController.getTeams(player.getId());

                statistic = StatisticRepository.getStatistics(team.getId());
                statistics = new StatisticRepository().getMoreInfoForOneTeam(team.getId());
                games = GamesController.getTeamGames(team.getId(), "id desc", 20);

            }
            List<Statistic> teams_statistics = null;
            if (teams != null) {
                teams_statistics = StatisticRepository.getMoreInfoForTeams(teams);
            }

            render((player != null ? player : null),
                    (team != null ? team : null),
                    (teams != null ? teams : null),
                    (statistic != null ? statistic : null),
                    (statistics != null ? statistics : null),
                    (teams_statistics != null ? teams_statistics : null),
                    (games != null ? games : null), scoredGoal, ownGoal);


        } else if (player == null) {

            controllers.Application.ofError();
        }
    }

    public static void logoutPlayer() {

        controllers.Application.afterLogout();
        controllers.Application.main_page();
    }

    public static void editPlayer(Player player, File image, boolean resetPicture, boolean resetrfid) {

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
        List<String> changes = new ArrayList<String>();
        if (existingplayer != null) {

            //FIRST_NAME
            if ((/*
                     * !player.first_name.equals("") &&
                     */player.first_name != null)) {

                if (existingplayer.first_name == null || !existingplayer.first_name.equals(player.first_name)) {


                    player.first_name = player.first_name.trim();
                    existingplayer.first_name = player.first_name;
                    if (existingplayer.first_name != null && !existingplayer.first_name.equals("")) {
                        existingplayer.first_name = capitalize(existingplayer.first_name);
                    }
                    hasChanged = true;
                    changes.add("Your first name is now changed.");

                }

            }

            //LAST_NAME
            if ((/*
                     * !player.last_name.equals("") &&
                     */player.last_name != null)) {

                if (existingplayer.last_name == null || !existingplayer.last_name.equals(player.last_name)) {
                    player.last_name = player.last_name.trim();
                    existingplayer.last_name = player.last_name;
                    if (existingplayer.last_name != null && !existingplayer.last_name.equals("")) {
                        existingplayer.last_name = capitalize(existingplayer.last_name);
                    }
                    hasChanged = true;
                    changes.add("Your last name is now changed.");
                }
            }
            //BIO
            if ((/*
                     * !player.bio.equals("") &&
                     */player.bio != null)) {

                if (existingplayer.bio == null || !existingplayer.bio.equals(player.bio)) {
                    existingplayer.bio = player.bio;
                    hasChanged = true;
                    changes.add("Your bio is now changed.");

                }

            }
            //EMAIL
            if ((/*
                     * !player.email.equals("") &&
                     */player.email != null)) {

                if (existingplayer.email == null || !existingplayer.email.equals(player.email)) {

                    if (player.email.equals("")) {
                        existingplayer.email = player.email;
                        hasChanged = true;
                        changes.add("Your email is now changed.");

                    } else {
                        validation.required(player.email);
                        validation.email(player.email);
                        if (!Validation.hasErrors()) {
                            existingplayer.email = player.email;
                            hasChanged = true;
                            changes.add("Your email is now changed.");

                        } else if (Validation.hasErrors()) {
                            Validation.addError("settings", "You email is incorrect.", player.username);
                            Validation.keep();
                        }
                    }



                }
            }


            //RFID
            if (player.rfid != null && player.rfid >= 1) {
                if (existingplayer.rfid == null || existingplayer.rfid != player.rfid) {
                    Player byRfid = Player.find("byRfid", player.rfid).first();
                    if (byRfid == null) {
                        existingplayer.rfid = player.rfid;
                        hasChanged = true;
                        changes.add("Your rfid is now changed.");
                    } else if (byRfid != null && !byRfid.equals(existingplayer)) {
                        //OBS
                        Validation.addError("settings", "There is a nother user with the same rfid.", player.username);
                        Validation.keep();
                    }
                }
            }
            if (resetrfid) {
                existingplayer.rfid = null;
                hasChanged = true;
                changes.add("Your rfid is now reseted.");
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
                String main_path = Play.applicationPath + "/public/images/";
                if (!existingplayer.image.equals("player.png") && existingplayer.image != null) {
                    String delete_path = main_path + "players/" + existingplayer.image;
                    File file = new File(delete_path);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
                existingplayer.image = playerImage;

                main_path += "players/" + playerImage;
                Images.resize(image, new File(main_path), 180, 140, true);


                hasChanged = true;
                changes.add("Your picture is now changed.");

            }
            if (resetPicture) {

                if (!existingplayer.image.equals("player.png")) {
                    String main_path = Play.applicationPath + "/public/images/";
                    main_path += "players/" + existingplayer.image;
                    File file = new File(main_path);

                    if (file != null && file.exists()) {
                        file.delete();
                    }

                    existingplayer.image = "player.png";
                    hasChanged = true;
                    changes.add("Your picture is now reseted.");
                }


            }

        }


        if (hasChanged) {

            existingplayer.save();
            for (int i = 0; i < changes.size(); i++) {
                Validation.addError("itsok", changes.get(i));
            }
            params.flash();
            Validation.keep();
        }
        if (!hasChanged) {
            Validation.addError("gay", "You have not changed anything.");
            Validation.keep();
        }

        settings(1);
    }

    public static void changePassword(Player player, String newPassword, String newPassword2) {


        if ((player.password == null || player.password.equals("")) || newPassword == null || newPassword2 == null) {
            Validation.addError("settings", "Missing informasjon", player.password);
            params.flash();
            Validation.keep();
            settings(3);
        }




        if (!newPassword.equals(newPassword2)) {

            Validation.addError("settings", "Your passwords must bee the same.", player.password);
            params.flash();
            Validation.keep();
            settings(3);

        } else if (newPassword.equals(newPassword2) && (player.password != null || !player.password.equals(""))) {

            if (session.get("pname") != null && session.get("pid") != null) {
                //ONLIE

                player.username = session.get("pname");
                player = Player.find("byUsernameAndPassword",
                        player.username, Crypto.encryptAES(player.password)).first();

                if (player != null) {

                    newPassword = Crypto.encryptAES(newPassword);
                    player.password = newPassword;
                    player.save();
                    Validation.addError("itsok", "Your password is now changed", "error");
                    Validation.keep();
                    settings(3);
                }
                if (player == null) {

                    Validation.addError("settings", "Your password is inncoret.", "error");

                    params.flash();
                    Validation.keep();
                    settings(3);
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

    private static String capitalize(String s) {
        s.trim();
        char[] chars = s.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isWhitespace(chars[i])) {
                chars[i + 1] = Character.toUpperCase(chars[i + 1]);
            }
        }
        return String.valueOf(chars);

    }
}
