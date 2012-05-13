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
    
    
    public static int getIndividualGoalScoreds(Long player_id){
        
        List<Goal> goal = Goal.find("player_id = ? AND backfire != 1", player_id).fetch();
        return goal.size();
        
    }
    
    public static int getIndividualOwnGoal(Long player_id){
        
        List<Goal> goal = Goal.find("player_id = ? AND backfire = 1", player_id).fetch();
        System.out.println(goal.size());
        return goal.size();
        
    }

}
