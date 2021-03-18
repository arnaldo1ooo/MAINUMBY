package conexion;

import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;

public class Conexion {

    private Conexion con;
    private Connection connection;
    private Statement st;
    private ResultSet rs;
    private static String controlador;
    private static String usuarioDB;
    private static String passDB; //Contrasena de la BD
    private static String nombreBD;
    private static String host;
    private static String puerto;
    private static String servidor;
    static Logger log_historial = Logger.getLogger(Conexion.class.getName());

    public static Connection ConectarBasedeDatos() {
        String tipoHost = "local";
        controlador = "com.mysql.cj.jdbc.Driver";
        switch (tipoHost) {
            case "local" -> {
                //Modo host local
                usuarioDB = "root";
                passDB = "toor5127-"; //Contrasena de la BD
                nombreBD = "mainumby";
                host = "localhost";
                puerto = "3306";
                servidor = "jdbc:mysql://" + host + ":" + puerto + "/" + nombreBD
                        + "?useUnicode=true"
                        + "&useJDBCCompliantTimezoneShift=true"
                        + "&useLegacyDatetimeCode=false"
                        + "&serverTimezone=America/Mexico_City"
                        //+ "&serverTimezone=UTC"
                        + "&useSSL=false"
                        + "&allowPublicKeyRetrieval=true";
                break;
            }
            case "remoto" -> {
                //Modo host remoto
                usuarioDB = "supervisor";
                passDB = "toor5127-"; //Contrasena de la BD
                nombreBD = "syschool";
                host = "192.168.100.234"; //San roque 192.168.1.240
                puerto = "3306";
                servidor = "jdbc:mysql://" + host + ":" + puerto + "/" + nombreBD
                        + "?useUnicode=true"
                        + "&useJDBCCompliantTimezoneShift=true"
                        + "&useLegacyDatetimeCode=false"
                        + "&serverTimezone=UTC"
                        + "&useSSL=false";
                break;
            }
            case "online" -> {
                //Modo host online
                usuarioDB = "root";
                passDB = "toor5127-"; //Contrasena de la BD
                nombreBD = "escuela";
                host = "181.123.175.39";
                puerto = "3306";
                servidor = "jdbc:mysql://" + host + ":" + puerto + "/" + nombreBD
                        + "?useUnicode=true"
                        + "&useJDBCCompliantTimezoneShift=true"
                        + "&useLegacyDatetimeCode=false"
                        + "&serverTimezone=UTC"
                        + "&useSSL=false";
                break;
            }

            default -> {
                JOptionPane.showMessageDialog(null, "Case no se encontro", "Error", JOptionPane.ERROR_MESSAGE);
                break;
            }

        }

        Connection connection;
        try {
            Class.forName(controlador);
            connection = DriverManager.getConnection(servidor, usuarioDB, passDB);
            if (connection != null) {
                System.out.println("\nCONEXIÓN A " + nombreBD + ", EXITOSA..");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            connection = null;
            log_historial.error("Error 1089: " + ex);
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error de conexion a la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return st;
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public void DesconectarBasedeDatos() {
        try {
            if (getConnection() != null) {
                getConnection().close();
            }
            if (getStatement() != null) {
                getStatement().close();
            }
            if (getResultSet() != null) {
                getResultSet().close();
            }
            System.out.println("DESCONEXIÓN DE LA BD (" + nombreBD + ") EXITOSA..");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "ERROR AL INTENTAR DESCONECTAR CONNECTION(" + nombreBD + "), RESULTSET y del STATEMENT", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            log_historial.error("Error 1098: " + ex);
            ex.printStackTrace();
        }
    }

    public int NumColumnsRS() {
        int NumColumnsRS = -1;
        try {
            NumColumnsRS = rs.getMetaData().getColumnCount();

        } catch (SQLException ex) {
            log_historial.error("Error 1090: " + ex);
            ex.printStackTrace();
        }
        return NumColumnsRS;
    }

    DefaultTableModel modelotabla;
    ResultSetMetaData mdrs;
    int numColumns;
    Object[] registro;

    public DefaultTableModel ConsultaTableBD(String sentencia, String titlesJtabla[], JComboBox ElComboCampos) {
        modelotabla = new DefaultTableModel(null, titlesJtabla);
        con = ObtenerRSSentencia(sentencia);
        try {
            mdrs = con.rs.getMetaData();
            numColumns = mdrs.getColumnCount();
            registro = new Object[numColumns]; //el numero es la cantidad de columnas del rs
            while (con.rs.next()) {
                for (int c = 0; c < numColumns; c++) {
                    registro[c] = (con.rs.getString(c + 1));
                }
                modelotabla.addRow(registro);//agrega el registro a la tabla
            }
            //Carga el combobox con los titulos de la tabla, solo si esta vacio
            if (ElComboCampos != null && ElComboCampos.getItemCount() == 0) {
                javax.swing.DefaultComboBoxModel modelCombo = new javax.swing.DefaultComboBoxModel(titlesJtabla);
                ElComboCampos.setModel(modelCombo);
            }
        } catch (SQLException ex) {
            log_historial.error("Error 1091: " + ex);
            ex.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        return modelotabla;
    }

    public Conexion ObtenerRSSentencia(String sentencia) { //con.Desconectar luego de usar el metodo
        System.out.println("ObtenerRSSentencia: " + sentencia);
        con = new Conexion();
        try {
            con.connection = (Connection) Conexion.ConectarBasedeDatos();
            con.st = con.connection.createStatement();
            con.rs = con.st.executeQuery(sentencia);

            con.rs.last(); //Poner el puntero en el ultimo
            System.out.println("ObtenerRSSentencia trajo " + con.rs.getRow() + " resultados, consulta: " + sentencia);
            con.getResultSet().beforeFirst(); //Poner el puntero en el anteprimero
        } catch (SQLException e) {
            log_historial.error("Error 1092: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e);
            if (e.getMessage().equals("Can not issue data manipulation statements with executeQuery().")) {
                System.out.println("Se esta ejecutando un update en ObtenerConsulta, cambie de metodo a EjecutarABM sentencia:" + sentencia + ", Error:" + e);
                JOptionPane.showMessageDialog(null, "Se esta ejecutando un update en ObtenerConsulta, cambie de metodo a EjecutarABM");
            }
        } catch (NullPointerException e) {
            log_historial.error("Error 1026: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
        return con;
    }

    public void EjecutarABM(String sentencia, boolean conAviso) {
        //Ejecuta consultas de Altas, Bajas y Modificaciones
        try {
            System.out.println("EjecutarABM: " + sentencia);
            log_historial.debug("EjecutarABM: " + sentencia);
            connection = Conexion.ConectarBasedeDatos();
            st = connection.createStatement();
            st.executeUpdate(sentencia);
            connection.close();
            st.close();

            if (conAviso == true) {
                Toolkit.getDefaultToolkit().beep(); //BEEP
                JOptionPane.showMessageDialog(null, "La operación se realizó correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            log_historial.error("Error 1093: " + ex);
            ex.printStackTrace();
        }
    }

    public Boolean SiYaExisteEnLaBD(String sentencia) {
        con = new Conexion();
        int cantreg = 0;
        try {
            System.out.println("Comprobar si ya existe en la BD: " + sentencia);
            con.connection = (Connection) Conexion.ConectarBasedeDatos();
            con.st = con.connection.createStatement();
            con.rs = con.st.executeQuery(sentencia);
            while (con.rs.next() && cantreg < 2) { //Revisamos cuantos registro trajo la consulta
                cantreg = cantreg + 1;
            }

        } catch (SQLException | NullPointerException e) {
            log_historial.error("Error 1094: " + e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();

        if (cantreg > 0) {
            return true;
        } else {
            return false;
        }
    }

    public String ObtenerUltimoID(String consultasql) {
        String ultimoId = "";
        Conexion con = new Conexion();
        try {
            con = con.ObtenerRSSentencia(consultasql);
            while (con.getResultSet().next()) {
                ultimoId = con.getResultSet().getString(1);
            }

        } catch (SQLException e) {
            System.out.println("No se pudo obtener el idultimo: " + ultimoId);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        return ultimoId;
    }
}
