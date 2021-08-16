/*
 * 
 * Carga una consulta realizada a un combobox
 * 
 */
package helpers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import dao.DAO;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.log4j.Logger;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class Metodos {

    private DAO con = new DAO();
    public int CantRegistros = 0;
    static Logger log_historial = Logger.getLogger(Metodos.class.getName());



    public Icon AjustarIconoAButton(Icon icono, int largo) {
        ImageIcon imageicono = (ImageIcon) icono;
        Image img = imageicono.getImage();
        Image resizedImage = img.getScaledInstance(largo, largo, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }






    public void AbrirFichero(String ruta) {
        File elFile = new File(ruta);
        Desktop ficheroAEjecutar = Desktop.getDesktop();
        try {
            ficheroAEjecutar.open(elFile);
        } catch (IOException e) {
            log_historial.error("Error 1011: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    /*public void CentrarventanaJInternalFrame(JInternalFrame LaVentana) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - LaVentana.getWidth()) / 2);
        int y = 0; //(int) ((dimension.getHeight() - LaVentana.getHeight()) / 2);
        LaVentana.setLocation(x, y);
    }
    public void CentrarVentanaJDialog(JDialog LaVentana) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - LaVentana.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - LaVentana.getHeight()) / 2);
        LaVentana.setLocation(x, y);
    }*/

 /*public String ObtenerCotizacion(String de, String a) {
        String valor = "";
        try {
            DecimalFormat df = new DecimalFormat("#.###");
            Conexion con = new Conexion();
            con = con.ObtenerRSSentencia("SELECT coti_valor FROM cambio WHERE cam_de = '" + de + "' AND cam_a = '" + a + "'");
            if (con.rs.next() == true) {
                valor = df.format(Double.parseDouble(con.rs.getString(1)));
                valor = valor.replace(".", ",");
            }
            con.DesconectarBasedeDatos();
        } catch (SQLException e) {
            log_historial.error("Error 1006: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al intentar obtener cambio " + e);
        }
        return valor;
    }*/
}
