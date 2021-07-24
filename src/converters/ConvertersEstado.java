/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converters;

/**
 *
 * @author Arnaldo_Cantero
 */
public class ConvertersEstado {
    public String converterEstado(String estado){
        switch (estado) {
            case "ACTIVO":
                return "A";
            case "INACTIVO":
                return "I";
            default:
                System.out.println("Error en switch converterestado " + estado);
                return null;
        }
    }
    
    
}
