/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package repositories;

import java.sql.ResultSet;
import play.db.DB;

/**
 *
 * @author Santonas
 */
public class OpenFoosDatabase extends DB {

    public static ResultSet executeQueryToFoosBase(String sql) {

        return executeQuery(sql);
    }
}
