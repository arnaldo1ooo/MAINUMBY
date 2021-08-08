/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package converters;

import helpers.HelpersValoresUniversales;

/**
 *
 * @author Arnaldo_Cantero
 */
public class ConvertersSioNo {
    private HelpersValoresUniversales helpersValoresUniversales = new HelpersValoresUniversales();
    
    public String converterSioNo(String valor){
        if (valor.equals(helpersValoresUniversales.siBd())) {
            return "SI";
        } else {
            if (valor.equals(helpersValoresUniversales.noBd())) {
                return "NO";
            }else{
                System.out.println("Sin seleccion converterSioNo " + valor);
                return null;
            }
        }
    }
    
        public String converterSioNoBD(String valor){
        switch (valor) {
            case "SI":
                return helpersValoresUniversales.siBd();
            case "NO":
                return helpersValoresUniversales.noBd();
            default:
                System.out.println("Error en switch converterSioNo " + valor);
                return null;
        }
    }
    
    
}
