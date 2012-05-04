/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import models.Statistic;
import play.mvc.Controller;
import repositories.StatisticRepository;

/**
 *
 * @author Santonas
 */
public class StatisticController extends Controller {
    
    public static void data(Long id) {

        Statistic statistic = StatisticRepository.getStatistics(id);
        renderJSON(statistic);
    }
}
