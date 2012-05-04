/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.lang.reflect.Constructor;
import java.util.Date;
import models.Player;
import models.Team;
import play.data.binding.Binder;
import play.db.Model;
import play.exceptions.TemplateNotFoundException;
import play.libs.Crypto;

/**
 *
 * @author Santonas
 */
public class Players extends CRUD {

    public static void create() throws Exception {


        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Model object = (Model) constructor.newInstance();
        Binder.bindBean(params.getRootParamNode(), "object", object);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/blank.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/blank.html", type, object);
            }
        }
        
        //HER MÅ VI OGSÅ BRUKE GENEREL METODE 
        //MÅ SJEKKE OM BRUKERNAVNET ER UNIQT
        Player player = (Player) object;
        player.password = Crypto.encryptAES(player.password);
        player.registered = new Date();
        
        
        //TODO
        //DETTE ER TEMP TIL VI FINNER EN GENERELL METODE FOR Å REGISTRERE LAG
        Team team = new Team();
        team.registered = player.registered;
        team.team_name = "Team " + player.username;
        team.player1 = player;
        
        //TEMP KODE SLUTTER
        object = player;
        Player chekPlayer = Player.find("byUsername", player.username).first();
        if ( chekPlayer == null){
        object._save();
        
        //OG HER. PLAYER MÅ "SAVES" først
        team.save();
        flash.success(play.i18n.Messages.get("crud.created", type.modelName));
        }if( chekPlayer != null){
        flash.error(type.modelName + " Username have been used, and it's not uniq", "PROBLEM");}
        
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        if (params.get("_saveAndAddAnother") != null) {
            redirect(request.controller + ".blank");
        }
        redirect(request.controller + ".show", object._key());

    }

    public static void save(String id, String encryptedPassword) throws Exception {

        ObjectType type = ObjectType.get(getControllerClass());
        notFoundIfNull(type);
        Model object = type.findById(id);
        notFoundIfNull(object);
        Binder.bindBean(params.getRootParamNode(), "object", object);
        validation.valid(object);
        if (validation.hasErrors()) {
            renderArgs.put("error", play.i18n.Messages.get("crud.hasErrors"));
            try {
                render(request.controller.replace(".", "/") + "/show.html", type, object);
            } catch (TemplateNotFoundException e) {
                render("CRUD/show.html", type, object);
            }
        }
  
      

        Player player = (Player) object;
        //Admin whants to change players password
        if (!encryptedPassword.equals(player.password)){    
           String temp = Crypto.encryptAES(player.password);
           player.password = temp;
           
        }else if( encryptedPassword.equals(player.password)){
            player.password = Crypto.decryptAES(player.password);
            player.password = Crypto.encryptAES(player.password);
        }
       
        object = player;

        object._save();
        flash.success(play.i18n.Messages.get("crud.saved", type.modelName));
        if (params.get("_save") != null) {
            redirect(request.controller + ".list");
        }
        redirect(request.controller + ".show", object._key());
    }
}
