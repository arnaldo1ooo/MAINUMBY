/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 *
 * @author Arnaldo_Cantero
 */
public class HelpersMoneda {

    private HelpersString helpersString = new HelpersString();

    public static final BigDecimal arredondamientoBigdecimal(BigDecimal valor, int cantidadDecimales) {
        if (valor == null) {
            return new BigDecimal(0);
        }

        double value = valor.doubleValue();
        double unidadDeMil = 1;

        switch (cantidadDecimales) {
            case 1:
                unidadDeMil = 10.0;
                break;
            case 2:
                unidadDeMil = 100.0;
                break;
            case 3:
                unidadDeMil = 1000.0;
                break;
            case 4:
                unidadDeMil = 10000.0;
                break;
            default:
                unidadDeMil = 1.0;
                break;
        }
        return BigDecimal.valueOf(Math.round(value * unidadDeMil) / unidadDeMil);
    }

    public static final BigDecimal arredondamientoDouble(double valor, int cantidadDecimales) {
        /*if (valor == null)
        {
            return new BigDecimal(0);
        }*/

        double value = valor;
        double unidadDeMil = 1;

        switch (cantidadDecimales) {
            case 1:
                unidadDeMil = 10.0;
                break;
            case 2:
                unidadDeMil = 100.0;
                break;
            case 3:
                unidadDeMil = 1000.0;
                break;
            case 4:
                unidadDeMil = 10000.0;
                break;
            default:
                unidadDeMil = 1.0;
                break;
        }
        return BigDecimal.valueOf(Math.round(value * unidadDeMil) / unidadDeMil);
    }

//Poner los puntos de miles
    public String StringPuntosMiles(String elNumString) {
        try {
            if (helpersString.esVacioONulo(elNumString) || elNumString.contains("-")) {
                return elNumString;
            }

            elNumString = elNumString.replace(".", "");
            DecimalFormat formatSudamerica = new DecimalFormat("#,###");
            double elNumeroDouble = Double.parseDouble(elNumString);
            elNumString = formatSudamerica.format(elNumeroDouble);
        } catch (NullPointerException e) {
            return "0";
        }
        return elNumString;
    }

    public String DoublePuntosMiles(double elNumDouble) {
        DecimalFormat formatSudamerica = new DecimalFormat("###,###,###");
        System.out.println("formatSudamerica.format(elNumDoublee) " + formatSudamerica.format(elNumDouble));
        return formatSudamerica.format(elNumDouble);
    }

    public String StringSinPuntosMiles(String elNumString) {
        try {
            if (elNumString.equals("")) {
                return "0";
            }

            elNumString = elNumString.replace(".", "");

            return elNumString;
        } catch (NullPointerException e) {
            return "0";
        }
    }
}
