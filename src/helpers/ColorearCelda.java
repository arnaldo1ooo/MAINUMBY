/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Arnaldo_Cantero
 */
public class ColorearCelda extends DefaultTableCellRenderer {

    private MetodosTXT metodostxt = new MetodosTXT();

    public Component getTableCellRendererComponent(JTable table, Object valor, boolean isSelected, boolean hasFocus, int fila, int columna) {
        //super.getTableCellRendererComponent(table, valor, isSelected, hasFocus, fila, columna);
        Component celda = super.getTableCellRendererComponent(table, valor, isSelected, hasFocus, fila, columna);
        setBackground(Color.WHITE);
        setForeground(Color.BLACK);

        if (columna == 2) {
            if (valor.equals("NO MATRICULADO")) {
                celda.setBackground(Color.RED);
                celda.setForeground(Color.WHITE);
            }
        }

        if (columna == 6) {
            if (String.valueOf(valor).contains("CANCELADO")) {
                celda.setBackground(new java.awt.Color(0, 147, 22));
                celda.setForeground(Color.WHITE);
            } else {
                celda.setBackground(Color.RED);
                celda.setForeground(Color.WHITE);
            }
        }
        return celda;
    }
}
