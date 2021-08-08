/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Arnaldo_Cantero
 */
public class HelpersDate {
    public Date dateFechaSQLActual(){
        Date fechaActualUtil = new Date();
        
        return new java.sql.Date(fechaActualUtil.getTime());
    }
    
    public String stringFechaHoraActual(){
                DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                String fechaCompra = formatoFecha.format(new Date());
                
                DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                String horaCompra = formatoHora.format(new Date());
                
                return fechaCompra + " " + horaCompra;
    }
}
