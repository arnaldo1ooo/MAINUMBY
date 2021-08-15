/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import dao.DAO;

public class UsuarioBean {
     private DAO con = new DAO();
    
     public DAO UsuarioConsulta(){
         String sentencia = "SELECT usu_codigo, usu_nombre, usu_apellido, usu_alias, usu_pass, date_format(usu_fechacreacion, '%d/%m/%Y') AS fechacreacion "
                 + "FROM USUARIO";
         con = con.ObtenerRSSentencia(sentencia);
         return con;
    }    
     
    public void UsuarioAlta(String nombre, String apellido, String alias, String pass, String fechacreacion){
        String sentencia = "INSERT INTO usuario VALUES "
                + "(usu_codigo, '"+nombre+"', '"+apellido+"', '"+alias+"', '"+pass+"', '"+fechacreacion+"')";
        
        con.EjecutarABM(sentencia, true);
    }
}
