/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import dao.DAO;
import static helpers.Metodos.log_historial;
import java.sql.SQLException;

public class HelpersUsuarioRol {
        private DAO con = new DAO();
    
        public String PermisoRol(String codUsuario, String modulo) {
        String permisos = "";
        String roldenominacion;
        String aliasusuario;
        try {
            con = con.ObtenerRSSentencia("CALL SP_UsuarioRolConsulta('" + codUsuario + "','" + modulo + "')");
            while (con.getResultSet().next()) {
                roldenominacion = con.getResultSet().getString("rol_denominacion");
                aliasusuario = con.getResultSet().getString("usu_alias");
                switch (roldenominacion) {
                    case "ALTA" -> {
                        permisos = permisos.concat("A");
                        System.out.println("El usuario " + aliasusuario + " en el modulo " + modulo + " tiene permiso para Alta");
                    }
                    case "BAJA" -> {
                        permisos = permisos.concat("B");
                        System.out.println("El usuario " + aliasusuario + " en el modulo " + modulo + " tiene permiso para Baja");
                    }
                    case "MODIFICAR" -> {
                        permisos = permisos.concat("M");
                        System.out.println("El usuario " + aliasusuario + " en el modulo " + modulo + " tiene permiso para Modificar");
                    }
                    default ->
                        System.out.println("No coincide ninguno: " + roldenominacion);
                }
            }
        } catch (SQLException e) {
            log_historial.error("Error 1010: " + e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        return permisos;
    }
}
