/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

/**
 *
 * @author Santonas
 */
public class Statistic {
    
    public Long game_id;
    public Long player_id;   
    public Long player2_id;
    
    
    
    
    public int games_playd;
    public int home_games;
    public int away_games;
    public int winns;
    public int losts;
    
    
    public int score_for;   
    public int score_home_for;   
    public int score_away_for;
    
    
    
    public int score_against;
    public int score_home_against;   
    public int score_away_against;
    
    
    
    public double average_score_for;
    public double average_score_against;
    
    
    public double win_prosent;
    public double lost_prosent;
    
    
    
    public String last_three_games_played = null;
    
    public Team target_team = null;
    
    
    
    
    public int count_most_played_against;
    public int count_most_lost_against;
    public int count_most_won_against;  
    public int count_most_regular_appearances;
    
}
