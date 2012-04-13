package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }
    
    public static void main_page(){
        render();
    }
     //Player is login
    public static boolean afterLogin(Player exist) {
        
        session.put("login", true);
        session.put("pid", exist.id);
        session.put("pname", exist.username);
        String token = session.getAuthenticityToken();
        session.put("token", token);
        return true;

    }

    public static boolean afterLogout() {
        session.clear();
        return true;
    }
    
    public static boolean auto(Long id){
         return Long.parseLong(session.get("pid")) == id; 
    }
   
}