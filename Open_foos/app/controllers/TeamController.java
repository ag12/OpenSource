/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.Game;
import models.Player;
import models.Statistic;
import models.Team;
import play.Play;
import play.libs.Images;
import play.mvc.Controller;
import repositories.StatisticRepository;

/**
 *
 * @author Santonas
 */
public class TeamController extends Controller {

    /*
     *
     *
     */
    public static void profile(String teamname) {

        Team team = Team.find("byTeam_name", teamname).first();
        if (team != null && team.memberCount() == 2) {

            //This teams statistic
            Statistic statistic = StatisticRepository.getStatistics(team.id);


            //This team morestatistic, as most played against and so on
            List<Statistic> statistics = new StatisticRepository().getMoreInfoForOneTeam(team.id);

            //This team have 100% two players, and them have their own teams as well
            //So we get them too
            List<Team> teams = new ArrayList<Team>();
            teams.add(getTeam(team.player1.id));
            teams.add(getTeam(team.player2.id));

            //uses the players to get statistic about them
            List<Statistic> teams_statistics = StatisticRepository.getMoreInfoForTeams(teams);

            //This teams played games
            List<Game> games = GamesController.getTeamGames(team.id, "id desc", 20);


            render((team != null ? team : null),
                    (statistic != null ? statistic : null),
                    (statistics != null ? statistics : null),
                    (games != null ? games : null),
                    (teams_statistics != null ? teams_statistics : null),
                    (teams != null ? teams : null));
        } else if (team != null && team.memberCount() == 1) {

            // send user to player profile site, where the player team info is available
            controllers.PlayerController.profile(team.player1.username);
        } else {
            controllers.Application.ofError();
        }
    }

    //Players own team
    public static Team getTeam(Long player_id) {
        Team team = Team.find("player1_id = ? AND player2_id = NULL", player_id).first();
        return team;
    }

    //Teams where player are a member of
    public static List<Team> getTeams(Long player1_id) {

        List<Team> teams = Team.find("(player1_id = ? or player2_id = ?) and (player1_id != NULL and player2_id != NULL)",
                player1_id, player1_id).fetch();
        return teams;
    }

    /*
     * Remove the comments inside the if's if you dont want the user to sett
     * data to empty
     *
     */
    public static void editTeam(Long id, Team team, File image, boolean reset) {

        System.out.println("START");
        Team existingTeam = Team.findById(id);
        boolean hasChanged = false;

        if (team.team_name != null && !"".equals(team.team_name) && !existingTeam.team_name.equals(team.team_name)) {
            Team t = Team.find("byTeam_name", team.team_name).first();
            if (t == null) {
                existingTeam.team_name = team.team_name;
                hasChanged = true;
            }
        }
        if (team.bio != null /*
                 * && !"".equals(team.bio)
                 */) {

            if (!team.bio.equals(existingTeam.bio)) {
                existingTeam.bio = team.bio;
                hasChanged = true;
            }
        }

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

            String teamImage = "openfoos_team_" + id + imageEnd;
            String main_path = Play.applicationPath + "/public/images/";
            if (!existingTeam.image.equals("team.png") && existingTeam.image != null) {
                String delete_path = main_path + "teams/" + existingTeam.image;
                File file = new File(delete_path);
                if (file != null && file.exists()) {
                    file.delete();
                }
            }
            existingTeam.image = teamImage;
            main_path += "teams/" + teamImage;
            Images.resize(image, new File(main_path), 200, 160, true);
            hasChanged = true;

        }

        if (reset) {

            if (!existingTeam.image.equals("team.png")) {
                String main_path = Play.applicationPath + "/public/images/";
                main_path += "teams/" + existingTeam.image;
                File file = new File(main_path);

                if (file != null && file.exists()) {
                    file.delete();
                }

                existingTeam.image = "team.png";
                hasChanged = true;
            }


        }

        if (team.arch_rival.team_name.equals("") && existingTeam.arch_rival != null) {
            existingTeam.arch_rival = null;
            hasChanged = true;
        }
        //User picks arch rival from the list
        if (team.arch_rival.team_name != null && team.arch_rival.team_name.length() > 1/*
                 * && !team.arch_rival.team_name.equals("")
                 */) {
            System.out.println("arch er ikke null, og større en 1");
            Team arch_rival = Team.find("byTeam_name", team.arch_rival.team_name).first();

            if (arch_rival != null && existingTeam.arch_rival != arch_rival) {
                System.out.println("arch er faktisk et lag som finnes");
                //User cant pick them self
                if (existingTeam.id != arch_rival.id) {

                    System.out.println("lagets id er ikke like arch sin id");
                    if (existingTeam.arch_rival != null) {

                        System.out.println("dette laget har archrival fra før og vil bytte");
                        if (existingTeam.memberCount() == 2) {
                            System.out.println("dette laget har to spillere ");
                            if (existingTeam.arch_rival.id != arch_rival.id
                                    && existingTeam.player1.id != arch_rival.id
                                    && existingTeam.player2.id != arch_rival.id) {


                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;

                            }
                        } else if (existingTeam.memberCount() == 1) {

                            System.out.println("dette laget har kunn en spiller ");
                            if (existingTeam.arch_rival.id != arch_rival.id
                                    && existingTeam.player1.id != arch_rival.id && existingTeam.player1.id != 
                                    arch_rival.player1.id) {

                                System.out.println("arch er en ny arch og player1 id av laget er ikke lig arch heller");
                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;

                            }
                        }

                    }//END existingTeam.arch_rival != null
                    if (existingTeam.arch_rival == null) {

                        System.out.println("laget har ikke arch fra før");
                        if (existingTeam.memberCount() == 2) {
                            System.out.println("to mans lag");
                            if (existingTeam.player1.id != arch_rival.id
                                    && existingTeam.player2.id != arch_rival.id) {


                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;

                            }
                        }
                        if (existingTeam.memberCount() == 1) {
                            System.out.println("en mans lag");
                            if (existingTeam.player1.id != arch_rival.id && existingTeam.player1.id != 
                                    arch_rival.player1.id) {

                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;

                            }
                        }
                    }
                }//END existingTeam.id != arch_rival.id
                else {
                    System.out.println("samme id");
                }
            }//END arch_rival != null && existingTeam.arch_rival != arch_rival
            else {
                System.out.println("men ingen forandring eller at arch ikke finnes");
            }

        }

        if (hasChanged) {
            existingTeam.save();
        }
        if (existingTeam.memberCount() == 1) {
            controllers.PlayerController.settings();
        } else {
            settings(existingTeam.team_name);
        }



    }

    public static void settings(String teamname) {

        if (session.get("login") != null && session.get("pid") != null) {
            Team team = Team.find("byTeam_name", teamname).first();
            if (team != null) {
                if (team.memberCount() == 2) {
                    long pid = Long.parseLong(session.get("pid"));
                    if (team.player1.id == pid || team.player2.id == pid) {
                        int compeleted = (compeletedProfile(team) + 60);
                        render(team, compeleted);
                    } else {
                        controllers.Application.ofError();
                    }
                }
            } else {
                controllers.Application.ofError();
            }

        } else {
            controllers.Application.ofError();
        }


    }

    public static int compeletedProfile(Team team) {

        int notNull = 10;
        if (team.bio != null && !team.bio.equals("")) {
            notNull += 10;
        }
        if (!team.image.equalsIgnoreCase("team.png")) {
            notNull += 10;
        }
        if (team.arch_rival != null) {
            notNull += 10;
        }
        return notNull;
    }

    public static List<Team> getTopRanked(int limit) {
        List<Team> teams = Team.find("ORDER BY rating DESC").fetch(5);
        return teams;
    }

    public static List<Team> getTopRankedTeams(int limit) {
        List<Team> teams = Team.find("player1_id != NULL AND player2_id != NULL ORDER BY rating DESC").fetch(5);
        return teams;
    }

    public static List<Team> getTopRankedPlayers(int limit) {
        List<Team> teams = Team.find("player2_id = NULL ORDER BY rating DESC").fetch(5);
        return teams;
    }
}
