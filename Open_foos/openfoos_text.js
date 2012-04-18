/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

        $('input:text').focus(
        function(){
            $(this).css({'background-color' : '#FFFFCC'});
        });

        $('input:text').blur(
        function(){
            $(this).css({'background-color' : '#FFFFFF'});
        });
            
        $('textarea').focus(
        function(){
            $(this).css({'background-color' : '#FFFFCC'});
        });

        $('textarea').blur(
        function(){
            $(this).css({'background-color' : '#FFFFFF'});
        });

