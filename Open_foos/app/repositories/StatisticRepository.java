/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Statistic;
import models.Team;

/**
 *
 * @author Santonas
 */
public class StatisticRepository {

    public static Statistic getStatistics(Long team_id) {

        StringBuilder sqlToQuery = new StringBuilder("SELECT (SELECT SUM(home_score) from Game where home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS score_home_for, ");
        sqlToQuery.append("(SELECT SUM( visitor_score ) from Game where visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS score_away_for, ");
        sqlToQuery.append("(SELECT ( score_home_for + score_away_for )) AS score_for, ");
        sqlToQuery.append("(SELECT count(Game.id) FROM Game WHERE home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS home_games, ");
        sqlToQuery.append("(SELECT count(Game.id) FROM Game WHERE visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS away_games, ");
        sqlToQuery.append("(SELECT ( home_games + away_games )) AS games_playd, ");
        sqlToQuery.append("(SELECT count(Game.id) FROM Game WHERE winner_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS winns, ");
        sqlToQuery.append("(SELECT (games_playd-winns)) as losts,  ");
        sqlToQuery.append(" (SELECT SUM(visitor_score) FROM Game WHERE home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) AS score_home_against, ");
        sqlToQuery.append("(SELECT SUM(home_score) FROM Game WHERE visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) as score_away_against, ");
        sqlToQuery.append("(select sum(score_home_against + score_away_against)) as score_against ");

        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        Statistic statistic = new Statistic();
        try {
            while (resultset.next()) {

                statistic.games_playd = resultset.getInt("games_playd");
                statistic.home_games = resultset.getInt("home_games");
                statistic.away_games = resultset.getInt("away_games");
                statistic.winns = resultset.getInt("winns");
                statistic.losts = resultset.getInt("losts");


                statistic.score_for = resultset.getInt("score_for");
                statistic.score_home_for = resultset.getInt("score_home_for");
                statistic.score_away_for = resultset.getInt("score_away_for");
                if (statistic.score_for == 0 && statistic.score_home_for != 0) {
                    statistic.score_for = statistic.score_home_for;
                }
                if (statistic.score_for == 0 && statistic.score_away_for != 0) {
                    statistic.score_for = statistic.score_away_for;
                }
                statistic.score_against = resultset.getInt("score_against");
                statistic.score_home_against = resultset.getInt("score_home_against");
                statistic.score_away_against = resultset.getInt("score_away_against");
                if (statistic.score_against == 0 && statistic.score_home_against != 0) {
                    statistic.score_against = statistic.score_home_against;
                }
                if (statistic.score_against == 0 && statistic.score_away_against != 0) {
                    statistic.score_against = statistic.score_away_against;
                }





            }
            resultset.close();
        } catch (SQLException e) {
        } finally {
        }

        if (statistic.games_playd >= 1) {


            statistic.average_score_for = (statistic.score_for / statistic.games_playd);

            statistic.win_prosent = (statistic.winns * 100) / statistic.games_playd;

            statistic.lost_prosent = (100 - statistic.win_prosent);

            statistic.last_three_games_played = getLastThreeGameResult(team_id);
        }

        return statistic;

    }

    private static String getLastThreeGameResult(Long team_id) {


//        StringBuilder sqlToQuery = new StringBuilder("SELECT id, home_team_id, visitor_team_id, winner_id, case winner_id ");
//        // if you want to se the home or visitor team, and the winner
//        sqlToQuery.append(" when winner_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" and ( home_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" and visitor_team_id != ");
//        sqlToQuery.append(team_id);
//
//        sqlToQuery.append(" ) or ( ");
//        sqlToQuery.append("home_team_id != ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" and visitor_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" ))");
//        
//        sqlToQuery.append(" then 'W' ");
//        sqlToQuery.append(" else 'L' ");
//        sqlToQuery.append(" end FROM Game where home_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" or visitor_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" order by id desc limit 3");




//
//     when ( winner_id !=
//      1 and (home_team_id = 1 and visitor_team_id != 1)  or ( home_team_id != 1 and visitor_team_id = 1)          ) 
//      then 'L' 
//      else 'W' 






//       end FROM Game where  ( (home_team_id = 1 and visitor_team_id != 1)
//                             or ( visitor_team_id = 1 and home_team_id != 1)  ) 
//        order by id desc limit 3


        StringBuilder sqlToQuery = new StringBuilder("SELECT case winner_id ");
        sqlToQuery.append("when ( winner_id != ");
        sqlToQuery.append(team_id);


        sqlToQuery.append(" and ( home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" and visitor_team_id != ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) or ( home_team_id != ");


        sqlToQuery.append(team_id);
        sqlToQuery.append(" and visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ) ");

        sqlToQuery.append(" then 'L' ");
        sqlToQuery.append(" else 'W' ");
        sqlToQuery.append(" end FROM Game where  ( ( home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" and visitor_team_id != ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) or ( visitor_team_id = ");
        sqlToQuery.append(team_id);

        sqlToQuery.append(" and home_team_id != ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ) order by id desc");



        StringBuilder last_three_games_played = new StringBuilder();
        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());

        try {

            while (resultset.next()) {

                last_three_games_played.append(resultset.getString(1));
                last_three_games_played.append(" / ");
            }
            resultset.close();

        } catch (SQLException e) {
        } finally {
        }
        last_three_games_played.replace(last_three_games_played.toString().length() - 2, last_three_games_played.toString().length(), "");
        last_three_games_played.reverse();
        return last_three_games_played.toString();
    }

    private Statistic getMostPlayedAgainst(Long team_id) {

        StringBuilder sqlToQuery =
                new StringBuilder("SELECT DISTINCT Team.id, Team.team_name, Team.image, COUNT(*) AS count_matches from Team, Game ");
        sqlToQuery.append("WHERE ( Team.id = Game.home_team_id AND Game.visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("OR (Game.home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" And Team.id = Game.visitor_team_id ) ");
        sqlToQuery.append("Group BY Team.id ORDER BY count_matches DESC LIMIT 1;");


        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        Team team = new Team();
        Statistic statistic = new Statistic();
        try {
            while (resultset.next()) {

                team.id = resultset.getLong("id");
                team.team_name = resultset.getString("team_name");
                team.image = resultset.getString("image");
                statistic.count_most_played_against = resultset.getInt("count_matches");

            }
            resultset.close();
            if (team.id != null && team.team_name != null) {
                statistic.target_team = team;
            }
        } catch (SQLException e) {
        } finally {
        }

        return statistic;
    }

    private Statistic getMostLostAgainst(Long team_id) {


        StringBuilder sqlToQuery =
                new StringBuilder("SELECT DISTINCT Team.id, Team.team_name, Team.image, COUNT(*) AS count_matches from Team, Game ");
        sqlToQuery.append("Where ( Team.id = Game.home_team_id and Game.visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("OR ( Game.home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" AND Team.id = Game.visitor_team_id ) ");
        sqlToQuery.append("AND winner_id != ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" GROUP BY Team.id ORDER BY count_matches DESC LIMIT 1;");


        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        Team team = new Team();
        Statistic statistic = new Statistic();
        try {
            while (resultset.next()) {

                team.id = resultset.getLong("id");
                team.team_name = resultset.getString("team_name");
                team.image = resultset.getString("image");
                statistic.count_most_lost_against = resultset.getInt("count_matches");

            }
            resultset.close();
            if (team.id != null && team.team_name != null) {
                statistic.target_team = team;
            }
        } catch (SQLException e) {
        } finally {
        }
        return statistic;
    }

    private Statistic getMostWonAgainst(Long team_id) {

        StringBuilder sqlToQuery =
                new StringBuilder("SELECT DISTINCT Team.id, Team.team_name, Team.image,  count(*) AS count_matches FROM Team, Game ");
        sqlToQuery.append("WHERE ( winner_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("AND (( Team.id = Game.home_team_id AND Game.visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("OR ( Game.home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" AND Team.id = Game.visitor_team_id ))");
        sqlToQuery.append("GROUP BY Team.id ORDER BY count_matches DESC LIMIT 1;");


        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        Team team = new Team();
        Statistic statistic = new Statistic();
        try {
            while (resultset.next()) {

                team.id = resultset.getLong("id");
                team.team_name = resultset.getString("team_name");
                team.image = resultset.getString("image");
                statistic.count_most_won_against = resultset.getInt("count_matches");

            }
            resultset.close();
            if (team.id != null && team.team_name != null) {
                statistic.target_team = team;
            }
        } catch (SQLException e) {
        } finally {
        }
        return statistic;
    }

    //the difference is equal to 1. Matches that was played 10-9 or 9-10
    private Statistic getMostRegularAppearances(Long team_id) {

        StringBuilder sqlToQuery =
                new StringBuilder("SELECT DISTINCT Team.id, Team.team_name, Team.image, count(*) AS count_matches FROM Game, Team ");
        sqlToQuery.append("WHERE ( winner_id != ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("AND (( Team.id = Game.home_team_id AND Game.visitor_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" ) ");
        sqlToQuery.append("OR ( Game.home_team_id = ");
        sqlToQuery.append(team_id);
        sqlToQuery.append(" AND Team.id = Game.visitor_team_id ))");
        sqlToQuery.append(" AND (( home_score=10 AND visitor_score=9 ) OR ( home_score=9 AND visitor_score=10 ))");
        sqlToQuery.append("GROUP BY Team.id ORDER BY count_matches DESC LIMIT 1;");


        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        Team team = new Team();
        Statistic statistic = new Statistic();
        try {
            while (resultset.next()) {

                team.id = resultset.getLong("id");
                team.team_name = resultset.getString("team_name");
                team.image = resultset.getString("image");
                statistic.count_most_regular_appearances = resultset.getInt("count_matches");

            }
            resultset.close();
            if (team.id != null && team.team_name != null) {
                statistic.target_team = team;
            }
        } catch (SQLException e) {
        } finally {
        }
        return statistic;
    }
//    private Statistic getMostRegularAppearances(Long team_id, int differenc) {
//
//        StringBuilder sqlToQuery =
//                new StringBuilder("SELECT DISTINCT Team.id, Team.team_name, Team.image, count(*) AS count_matches FROM Game, Team ");
//        sqlToQuery.append("WHERE ( winner_id != ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" ) ");
//        sqlToQuery.append("AND (( Team.id = Game.home_team_id AND Game.visitor_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" ) ");
//        sqlToQuery.append("OR ( Game.home_team_id = ");
//        sqlToQuery.append(team_id);
//        sqlToQuery.append(" AND Team.id = Game.visitor_team_id ))");
//        sqlToQuery.append(" AND (( home_score=10 AND visitor_score=9 ) OR ( home_score=9 AND visitor_score=10 ))");
//        sqlToQuery.append("GROUP BY Team.id ORDER BY count_matches DESC LIMIT 1;");
//
//
//        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
//        Team team = new Team();
//        Statistic statistic = new Statistic();
//        try {
//            while (resultset.next()) {
//
//                team.id = resultset.getLong("id");
//                team.team_name = resultset.getString("team_name");
//                team.image = resultset.getString("image");
//                statistic.count_most_regular_appearances = resultset.getInt("count_matches");
//
//            }
//            resultset.close();
//            statistic.target_team = team;
//        } catch (SQLException e) {
//        } finally {
//        }
//        return statistic;
//    }

    public List<Statistic> getMoreInfo(Long team_id) {

        List<Statistic> statistics = new ArrayList<Statistic>();
        statistics.add(this.getMostPlayedAgainst(team_id));
        statistics.add(this.getMostWonAgainst(team_id));
        statistics.add(this.getMostLostAgainst(team_id));
        statistics.add(this.getMostRegularAppearances(team_id));
        return statistics;
    }

    public static List<Statistic> getMoreInfoForTeams(List<Team> teams) {

        List<Statistic> statistics = new ArrayList<Statistic>();
        for (int i = 0; i < teams.size(); i++) {

            statistics.add(getStatistics(teams.get(i).id));
        }

        return statistics;
    }
}