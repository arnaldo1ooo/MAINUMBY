/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import dao.DAO;
import static helpers.Metodos.log_historial;
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
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;


public class HelpersTable {
        public int CantRegistros = 0;
    
    
        public void AnchuraColumna(JTable laTabla) {
        TableColumnModel tbColumnModel = laTabla.getColumnModel();
        int anchoAcumulado = 0;
        int anchoExtra = 20;
        int cantColumns = laTabla.getColumnCount();
        int cantFilas = laTabla.getRowCount();
        String nomheader; //Header = Cabecera
        TableColumn columnactual;
        Component componente;
        TableCellRenderer renderer;
        int anchoheaderenpixel;

        //Obtener tamano de String en pixeles
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2d = img.createGraphics();
        Font font = laTabla.getFont(); //Poner la fuente tipo y tamano que se usa en la tabla
        FontMetrics fontmetrics = graphics2d.getFontMetrics(font);
        graphics2d.dispose();

        for (int columnIndex = 0; columnIndex < cantColumns; columnIndex++) {
            int anchomax = 50; // Min width 
            columnactual = tbColumnModel.getColumn(columnIndex);
            for (int fila = 0; fila < cantFilas; fila++) {
                renderer = laTabla.getCellRenderer(fila, columnIndex);
                componente = laTabla.prepareRenderer(renderer, fila, columnIndex);
                nomheader = columnactual.getHeaderValue().toString(); //Header es cabecera de la columna
                anchoheaderenpixel = fontmetrics.stringWidth(nomheader);
                anchomax = Math.max(componente.getPreferredSize().width + anchoExtra, anchomax);
                if (anchomax <= anchoheaderenpixel || cantFilas == 0) { //Si el ancho del registtro mas largo de la columna es menor a la cabecera 
                    anchomax = anchoheaderenpixel;
                }
            }
            if (cantFilas == 0) { //Si no hay ningun registro
                nomheader = columnactual.getHeaderValue().toString();
                anchoheaderenpixel = fontmetrics.stringWidth(nomheader);
                anchomax = anchoheaderenpixel + anchoExtra;
            }
            if (columnIndex < (cantColumns - 1)) { //Si no es la ultima columna
                columnactual.setPreferredWidth(anchomax); //Asigna a la columna el ancho del registro mas largo de la columna 
                anchoAcumulado = anchoAcumulado + anchomax; //Acumula el ancho de las columnas excepto el ultimo
            } else { //Ultima columna
                int anchototal = (int) laTabla.getParent().getSize().getWidth(); //Tamano total del scroll que contiene a la tabla
                if ((anchoAcumulado + anchomax) <= anchototal) {//Si la suma de la anchura de todas las columnas es menor o igual al ancho del scroll
                    int resta = anchototal - anchoAcumulado; //Resta entre el ancho total del scroll y el ancho sumado de las columnas anteriores
                    columnactual.setPreferredWidth(resta);
                } else { //Si es mayor asigna el ancho del registro mas largo de la columna
                    columnactual.setPreferredWidth(anchomax);
                }
            }
        }

        laTabla.getTableHeader().setResizingAllowed(false); //Bloquear cambio de tamaño manual de columnas
        laTabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); //Desactiva el autoresize
    }

    public void CambiarColorAlternadoTabla(JTable LaTabla, final Color colorback1, final Color colorback2) {
        LaTabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? colorback1 : colorback2);
                return c;
            }
        }
        );
    }

    public void FiltroJTable(String cadenaABuscar, int columnaABuscar, JTable ElJTable) {
        //System.out.println("Metodo FiltroJTable:  cadena: " + cadenaABuscar + ", columna: " + columnaABuscar);

        TableRowSorter modelFiltrado = new TableRowSorter<>(ElJTable.getModel());
        if (columnaABuscar <= (ElJTable.getColumnCount() - 1)) {
            modelFiltrado.setRowFilter(RowFilter.regexFilter("(?i)" + cadenaABuscar, columnaABuscar));
            ElJTable.setRowSorter(modelFiltrado);
            //ElJTable.repaint();
        } else { //Buscar por todas las columnas
            for (int i = 0; i < ElJTable.getColumnCount(); i++) {
                modelFiltrado.setRowFilter(RowFilter.regexFilter("(?i)" + cadenaABuscar, i));
                ElJTable.setRowSorter(modelFiltrado);
                if (ElJTable.getRowCount() > 0) {
                    i = ElJTable.getColumnCount();
                }
            }
        }
    }

    public void ConsultaFiltroTablaBD(JTable LaTabla, String titlesJtabla[], String campoconsulta[], String nombresp, String filtro, JComboBox cbCampoBuscar) {
        String sentencia;
        DefaultTableModel modelotabla = new DefaultTableModel(null, titlesJtabla);

        if (cbCampoBuscar.getItemCount() == 0) {//Si combo esta vacio
            for (int i = 0; i < titlesJtabla.length; i++) {
                cbCampoBuscar.addItem(titlesJtabla[i]);
            }
            cbCampoBuscar.addItem("Todos");
            cbCampoBuscar.setSelectedIndex(1);
        }

        if (cbCampoBuscar.getSelectedItem() == "Todos") {
            String todoscamposconsulta = campoconsulta[0]; //Cargar el combo campobuscar
            //Cargamos todos los titulos en un String separado por comas
            for (int i = 1; i < campoconsulta.length; i++) {
                todoscamposconsulta = todoscamposconsulta + ", " + campoconsulta[i];
            }
            sentencia = "CALL " + nombresp + " ('" + todoscamposconsulta + "', '" + filtro + "');";
        } else {
            sentencia = "CALL " + nombresp + " ('" + campoconsulta[cbCampoBuscar.getSelectedIndex()] + "', '" + filtro + "');";
        }

        cbCampoBuscar.setEnabled(true);

        System.out.println("sentencia filtro tabla BD: " + sentencia);

        Connection connection;
        Statement st;
        ResultSet rs;
        try {
            connection = (Connection) DAO.ConectarBasedeDatos();
            st = connection.createStatement();
            rs = st.executeQuery(sentencia);
            ResultSetMetaData mdrs = rs.getMetaData();
            int numColumns = mdrs.getColumnCount();
            Object[] registro = new Object[numColumns]; //el numero es la cantidad de columnas del rs
            CantRegistros = 0;
            while (rs.next()) {
                for (int j = 0; j < numColumns; j++) {
                    registro[j] = (rs.getString(j + 1));
                }
                modelotabla.addRow(registro);//agrega el registro a la tabla
                CantRegistros = CantRegistros + 1;
            }
            LaTabla.setModel(modelotabla);//asigna a la tabla el modelo creado

            connection.close();
            st.close();
            rs.close();
        } catch (SQLException e) {
            log_historial.error("Error 1005: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public double SumarColumnaDouble(JTable LaTabla, int LaColumna) {
        double valor;
        double totalDouble = 0;

        try {
            if (LaTabla.getRowCount() > 0) {
                for (int i = 0; i < LaTabla.getRowCount(); i++) {
                    valor = Double.parseDouble(LaTabla.getValueAt(i, LaColumna) + "");
                    totalDouble = totalDouble + valor;
                }
            }
        } catch (NumberFormatException e) {
            log_historial.error("Error 1007: " + e);
            e.printStackTrace();
        }
        return totalDouble;
    }
    
        public void OcultarColumna(JTable laTabla, int numColumna) {
        if (laTabla.getColumnCount() >= numColumna) {
            laTabla.getColumnModel().getColumn(numColumna).setMaxWidth(0);
            laTabla.getColumnModel().getColumn(numColumna).setMinWidth(0);
            laTabla.getColumnModel().getColumn(numColumna).setPreferredWidth(0);
        }
    }

    public void OrdenarColumna(JTable laTabla, int numColumna) {
        RowSorter<TableModel> sorter = new TableRowSorter<>(laTabla.getModel());
        laTabla.setRowSorter(sorter);
        laTabla.getRowSorter().toggleSortOrder(numColumna);
    }
}
