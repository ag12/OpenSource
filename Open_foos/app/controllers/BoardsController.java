/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.List;
import models.Board;
import play.mvc.Controller;

/**
 *
 * @author Santonas
 */
public class BoardsController extends Controller {
    
    public static void getAllBoards(){
        List<Board> boards = Board.findAll();
        renderJSON(boards);
    }
}
