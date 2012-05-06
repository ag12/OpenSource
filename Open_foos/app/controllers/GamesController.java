/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import models.Game;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class GamesController extends Controller {

    public static List<Game> getTeamGames(Long team_id, int limit) {

        List<Game> games = Game.find("(home_team_id = ? or visitor_team_id = ?) and (end_time != 0 )order by id desc", team_id, team_id).fetch(limit);
        return games;

    }
}
