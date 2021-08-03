/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.util.Date;

/**
 *
 * @author Arnaldo_Cantero
 */
public class HelpersDate {
    public Date fechaSQLActual(){
        Date fechaActualUtil = new Date();
        
        return new java.sql.Date(fechaActualUtil.getTime());
    }
}
