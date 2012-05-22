/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package validations;

/**
 *
 * @author Santonas
 */
public class PlayerValidations {
    // Capitalize the first letter and after a whitespace

    public static String capitalize(String s) {
        s.trim();
        char[] chars = s.toLowerCase().toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 0; i < chars.length - 1; i++) {
            if (Character.isWhitespace(chars[i])) {
                chars[i + 1] = Character.toUpperCase(chars[i + 1]);
            }
        }
        return String.valueOf(chars);

    }
}
