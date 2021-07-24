/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import dao.DAO;
import java.awt.Color;
import java.awt.Graphics;
import java.sql.SQLException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class HelpersComboBox {

    private DAO con = new DAO();
    private int codigo;
    private String descripcion;
    static Logger log_historial = Logger.getLogger(HelpersComboBox.class.getName());

    public HelpersComboBox() { //No borrar
    }

    public HelpersComboBox(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int id) {
        this.codigo = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return this.descripcion;
    }

    public void SetSelectedNombreItem(JComboBox ElCombo, String nombreitem) {
        HelpersComboBox item;
        for (int i = 0; i < ElCombo.getItemCount(); i++) {
            item = (HelpersComboBox) ElCombo.getItemAt(i);
            if (item.getDescripcion().equalsIgnoreCase(nombreitem)) {
                ElCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    public void SetSelectedCodigoItem(JComboBox ElCombo, int codigoitem) {
        HelpersComboBox item;
        for (int i = 0; i < ElCombo.getItemCount(); i++) {
            item = (HelpersComboBox) ElCombo.getItemAt(i);
            if (item.getCodigo() == codigoitem) {
                ElCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    public void CargarComboConsulta(JComboBox elCombo, String consulta, int codItemDefault) {
        System.out.println("Cargar combo (" + elCombo.getName() + "): " + consulta);
        try {
            elCombo.removeAllItems(); //Vaciamos el combo

            con = con.ObtenerRSSentencia(consulta);
            while (con.getResultSet().next()) {
                elCombo.addItem(new HelpersComboBox(con.getResultSet().getInt(1), con.getResultSet().getString(2)));
                
                //Seleccionado por defecto
                try {
                    if (codItemDefault == con.getResultSet().getInt(1) && codItemDefault >= 0) {
                        elCombo.setSelectedItem(con.getResultSet().getString(2));
                    }
                } catch (NullPointerException e) {
                    log_historial.warn("Error 1012: " + e);
                    e.printStackTrace();
                }
            }

            if (elCombo.getItemCount() > 0 || codItemDefault == -1) {
                elCombo.setSelectedIndex(-1);
            }

            //elCombo.setMaximumRowCount(elCombo.getModel().getSize()); //Hace que se despliegue en toda la pantalla vertical el combo
            //AddScrollHorizontalCombo(elCombo);
        } catch (NumberFormatException | SQLException e) {
            log_historial.error("Error al cargar combo: " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("CargarComboConsulta NullPointerException");
        }

        AutoCompleteDecorator.decorate(elCombo);

        CambiarColorDisabledCombo(elCombo, Color.BLACK);
        con.DesconectarBasedeDatos();
    }

    public void CambiarColorDisabledCombo(JComboBox elCombo, Color elColor) {
        //Cambiar color de texto del combo cuando esta disabled
        elCombo.setRenderer(new DefaultListCellRenderer() {
            public void paint(Graphics g) {
                setForeground(elColor);
                super.paint(g);
            }
        });
    }

    public int ObtenerIDSelectCombo(JComboBox<HelpersComboBox> elCombo) {
        int coditemselect = -1;
        try {
            coditemselect = elCombo.getItemAt(elCombo.getSelectedIndex()).getCodigo();
        } catch (NullPointerException e) {
            System.out.println("ObtenerIdCombo: No se selecciono ningun item en el combo: " + e + " codItemSelect:" + coditemselect);
            //log_historial.error("ObtenerIdCombo: No se selecciono ningun item en el combo: " + e + " codItemSelect:" + coditemselect);
        }
        return coditemselect;
    }

    private void AddScrollHorizontalCombo(JComboBox ElCombo) {
        if (ElCombo.getItemCount() == 0) {
            return;
        }
        Object comp = ElCombo.getUI().getAccessibleChild(ElCombo, 0);
        if (!(comp instanceof JPopupMenu)) {
            return;
        }
        JPopupMenu popup = (JPopupMenu) comp;
        JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
        scrollPane.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
}
