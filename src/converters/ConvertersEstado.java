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
public class ConvertersEstado {

    private HelpersValoresUniversales helpersValoresUniversales = new HelpersValoresUniversales();

    public String converterEstado(String valor) {
        if (valor.equals(helpersValoresUniversales.activoBd())) {
            return "ACTIVO";
        } else {
            if (valor.equals(helpersValoresUniversales.inactivoBd())) {
                return "INACTIVO";
            } else {
                System.out.println("Sin seleccion converterEstado " + valor);
                return null;
            }
        }
    }

    public String converterEstadoBD(String estado) {
        switch (estado) {
            case "ACTIVO":
                return helpersValoresUniversales.activoBd();
            case "INACTIVO":
                return helpersValoresUniversales.inactivoBd();
            default:
                System.out.println("Error en switch converterEstadoBD " + estado);
                return null;
        }
    }

}
