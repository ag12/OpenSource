/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import models.Team;

/**
 *
 * @author Santonas
 */
public class TeamRepository {

    public static List<Team> getBiggestWinner() {
        StringBuilder sqlToQuery = new StringBuilder("SELECT home_team_id as team_id, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE home_team_id = temp_game.home_team_id and Game.end_time != 0) AS home_games, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE home_team_id = temp_game.visitor_team_id and Game.end_time != 0) AS away_games, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE (home_team_id = temp_game.home_team_id OR home_team_id = temp_game.visitor_team_id) AND winner_id= team_id and Game.end_time != 0) AS win, ");
        sqlToQuery.append("(SELECT sum(home_games+away_games)) as total ");
        sqlToQuery.append("FROM (SELECT * FROM Game where Game.end_time != 0) AS temp_game ");
        sqlToQuery.append("GROUP BY home_team_id ORDER BY win DESC");

        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        List<Team> teams = new ArrayList<Team>();
        try {
            while (resultset.next()) {
                Team team = Team.findById(resultset.getLong(1));
                team.won = resultset.getInt("win");
                if (team.won == 0) {
                    break;
                }
                team.lost = resultset.getInt("total") - team.won;
                teams.add(team);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return teams;
    }

    public static List<Team> getBiggestLoser() {
        
        StringBuilder sqlToQuery = new StringBuilder("SELECT home_team_id as team_id, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE home_team_id = temp_game.home_team_id and Game.end_time != 0) AS home_games, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE home_team_id = temp_game.visitor_team_id and Game.end_time != 0) AS away_games, ");
        sqlToQuery.append("(SELECT count(*) FROM Game WHERE (home_team_id = temp_game.home_team_id OR home_team_id = temp_game.visitor_team_id) and winner_id!=team_id and Game.end_time != 0) as lost, ");
        sqlToQuery.append("(SELECT sum(home_games+away_games)) as total ");
        sqlToQuery.append("FROM (SELECT * FROM Game where Game.end_time != 0) AS temp_game ");
        sqlToQuery.append("GROUP BY home_team_id ORDER BY lost DESC");

        ResultSet resultset = OpenFoosDatabase.executeQueryToFoosBase(sqlToQuery.toString());
        List<Team> teams = new ArrayList<Team>();
        try {
            while (resultset.next()) {
                Team team = Team.findById(resultset.getLong(1));
                team.lost = resultset.getInt("lost");
                if (team.lost == 0) {
                    break;
                }
                team.won = resultset.getInt("total") - team.lost;
                teams.add(team);
            }
        } catch (SQLException e) {
            System.out.println(e.toString());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return teams;
    }
}
