/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.ArrayList;
import java.util.List;
import models.Goal;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class GoalController extends Controller {

    public static int getIndividualGoalScoreds(Long player_id) {
       
        //ALT ONE  
        //List<Goal> goal = Goal.find("player_id = ? AND backfire != 1", player_id).fetch();
        //return goal.size();

        //ALT TWO
        List<Goal> allGoals = Goal.find("player_id = ?", player_id).fetch();
        List<Goal> goals = new ArrayList<Goal>();
        for (int i = 0; i < allGoals.size(); i++) {
            if (!allGoals.get(i).backfire) {
                goals.add(allGoals.get(i));
            }
        }
        return goals.size();

    }

    public static int getIndividualOwnGoal(Long player_id) {
          
        //ALT ONE
        //List<Goal> goal = Goal.find("player_id = ? AND backfire = 1", player_id).fetch();
        //return goal.size();
       
        //ALT TWO      
        List<Goal> allGoals = Goal.find("player_id = ?", player_id).fetch();
        List<Goal> goals = new ArrayList<Goal>();
        for (int i = 0; i < allGoals.size(); i++) {
            if (allGoals.get(i).backfire) {
                goals.add(allGoals.get(i));
            }
        }
        return goals.size();

    }
}
