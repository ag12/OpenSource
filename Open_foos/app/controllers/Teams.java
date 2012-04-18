/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import models.Game;
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
public class Teams extends Controller {

    public static void oko(){
        
        long id = 1;
        long id2 = 2;
        long id3 = 3;
        long id4 = 4;
        long id5 = 5;
        
       Statistic statistic = StatisticRepository.getStatistics(id);
       System.out.println(statistic.last_three_games_played + " id 1");
       
       statistic = StatisticRepository.getStatistics(id2);
       System.out.println(statistic.last_three_games_played + " id 2");
       statistic = StatisticRepository.getStatistics(id3);
       System.out.println(statistic.last_three_games_played + " id 3 ");
       statistic = StatisticRepository.getStatistics(id4);
       System.out.println(statistic.last_three_games_played + " id 4");
        
         statistic = StatisticRepository.getStatistics(id5);
       System.out.println(statistic.last_three_games_played + " id 5");
    }
    
    public static void profile(String teamname){
       
        Team team = Team.find("byTeam_name", teamname).first();
        Statistic statistic = StatisticRepository.getStatistics(team.id);
        
        List<Statistic> statistics = new StatisticRepository().getMoreInfo(team.id);
                            
        List<Game> games = Games.getTeamGames(team.id);
        
        
        render((team != null ? team : null),
                (statistic != null ? statistic : null),
                (statistics != null ? statistics : null), 
                (games != null ? games : null));
    }
    
    
    
    //Players own team
    public static Team getTeam(Long player_id) {
        Team team = Team.find("player1_id = ? AND player2_id = NULL", player_id).first();
        return team;
    }

    //Teams where player are a member of
    public static List<Team> getTeams(Long player1_id) {

        List<Team> teams = Team.find("(player1_id = ? or player2_id = ?) and (player2_id != NULL)",
                player1_id, player1_id).fetch();
        return teams;
    }

    public static void edit_Team(Long id, Team team, File image) {



        Team existingTeam = Team.findById(id);
        boolean hasChanged = false;


        if (team.team_name != null && !"".equals(team.team_name)) {


            if (!existingTeam.team_name.equals(team.team_name)) {

                Team t = Team.find("byTeam_name", team.team_name).first();
                if (t == null) {
                    existingTeam.team_name = team.team_name;
                    hasChanged = true;
                }
            }


        }
        if (team.bio != null && !"".equals(team.bio)) {


            if (existingTeam.bio == null || !existingTeam.bio.equals(team.bio)) {

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
            existingTeam.image = teamImage;
            String main_path = Play.applicationPath + "/public/images/";
            String xsmall_path = main_path + "xsmall/" + teamImage;
            String small_path = main_path + "small/" + teamImage;
            String medium_path = main_path + "medium/" + teamImage;
            String large_path = main_path + "large/" + teamImage;


            //since the image name i uniqe here, the system wil auto change players image if exists
            //xsmall 72x72       
            Images.resize(image, new File(xsmall_path), 72, 72, false);
            //small 127x80
            Images.resize(image, new File(small_path), 127, 80, false);
            //medium 85x120 
            Images.resize(image, new File(medium_path), 85, 120, false);
            //Large 
            Images.resize(image, new File(large_path), 600, 600, false);
            hasChanged = true;


        }



        //User picks arch rival from the list
        if (team.arch_rival.team_name != null && !team.arch_rival.team_name.equals("")) {
            Team arch_rival = Team.find("byTeam_name", team.arch_rival.team_name).first();



            if (existingTeam.id != arch_rival.id) {

                if (arch_rival != null) {
                    if (existingTeam.arch_rival != null) {


                        if (existingTeam.memberCount() == 2) {
                            if (existingTeam.arch_rival.id != arch_rival.id
                                    && existingTeam.player1.id != arch_rival.id
                                    && existingTeam.player2.id != arch_rival.id) {


                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;
                            }
                        } else if (existingTeam.memberCount() == 1) {
                            if (existingTeam.arch_rival.id != arch_rival.id
                                    && existingTeam.player1.id != arch_rival.id) {


                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;
                            }
                        }

                    }
                    if (existingTeam.arch_rival == null) {


                        if (existingTeam.memberCount() == 2) {
                            if (existingTeam.player1.id != arch_rival.id
                                    && existingTeam.player2.id != arch_rival.id) {


                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;
                            }
                        }
                        if (existingTeam.memberCount() == 1) {
                            if (existingTeam.player1.id != arch_rival.id) {

                                existingTeam.arch_rival = arch_rival;
                                hasChanged = true;
                            }
                        }
                    }

                }
            }

        }

        if (hasChanged) {

            existingTeam.save();

        }

        controllers.Players.settings();


    }

    public static List<Team> getTeamsFromDb(String filter) {

        List<Team> teams;
        if (filter != null) {
            teams = Team.find("byTeam_nameLike", "%" + filter + "%").fetch();
            return teams;
        } else {
            teams = Team.findAll();
            return teams;
        }
    }

    public static void allTeamsByTeam_name(String filter) {
        renderJSON(getTeamsFromDb(filter));
    }

    public static void getTeamsForPlayer(Long team_id, String filter) {

        List<Team> teams = Team.find("player1_id = ? AND player2_id != NULL", team_id).fetch();
        renderJSON(teams);
    }
}
