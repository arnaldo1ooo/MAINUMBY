/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

/**
 *
 * @author Arnaldo_Cantero
 */
public class HelpersString {
    
    public boolean esVacioONulo(String valor){
        if (valor.equals("") || valor.equals(null) || valor.isEmpty()) {
            return true;
        }else{
            return false;
        }
    }
    
    public Boolean esNulo(String laCadena) {
        if (laCadena.equals("null") || laCadena.equals(null)) {
            laCadena = "";
            return true;
        }
        
        return false;
    }
    
        public String nuloAVacio(String laCadena) {
        if (laCadena.equals("null")) {
            laCadena = "";
        }
        return laCadena;
    }
    
}
