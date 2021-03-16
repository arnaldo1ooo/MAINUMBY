/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package login;

import conexion.Conexion;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import principal.Principal;

/**
 *
 * @author Arnaldo_Cantero
 */
public class SplashScreen extends javax.swing.JFrame implements Runnable {

    private Conexion con = new Conexion();

    public SplashScreen(java.awt.Frame parent, boolean modal) {
        initComponents();
    }

    public void run() {
        try {
            this.setLocationRelativeTo(null);
            this.setVisible(true);

            lblVersionBD.setText(ObtenerVersionBD());

            Thread.sleep(3000); //Esta en pantalla por 3 segundos
            this.dispose(); //Desaparece
            Principal principal = new Principal();
            principal.setVisible(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String ObtenerVersionBD() {
        String versionBD = "Error";
        try {
            con = con.ObtenerRSSentencia("SELECT conf_valor FROM configuracion WHERE conf_codigo = '2'");
            while (con.getResultSet().next()) {
                versionBD = con.getResultSet().getString("conf_valor");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();

        return versionBD;
    }

    private Date FechaFinPeriodo() {
        Date fechaFinPeriodo = new Date();
        String[] diaMes;
        String diaFinPeriodo;
        String mesFinPeriodo;
        String añoActual;
        Calendar calendar = new java.util.GregorianCalendar();
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        try {
            con = con.ObtenerRSSentencia("SELECT conf_valor FROM configuracion WHERE conf_codigo = '3'");
            while (con.getResultSet().next()) {
                diaMes = con.getResultSet().getString("conf_valor").split("/");
                diaFinPeriodo = diaMes[0];
                mesFinPeriodo = diaMes[1];
                añoActual = calendar.get(Calendar.YEAR) + "";
                fechaFinPeriodo = formatoFecha.parse(diaFinPeriodo + "/" + mesFinPeriodo + "/" + añoActual);
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        return fechaFinPeriodo;
    }



    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnPrincipal = new org.edisoncor.gui.panel.Panel();
        panelCurves1 = new org.edisoncor.gui.panel.PanelCurves();
        lmCargando = new org.edisoncor.gui.label.LabelMetric();
        panelImage1 = new org.edisoncor.gui.panel.PanelImage();
        rSProgressMaterial1 = new rojerusan.componentes.RSProgressMaterial();
        lbHora1 = new javax.swing.JLabel();
        lbHora3 = new javax.swing.JLabel();
        lblVersionBD = new javax.swing.JLabel();
        lbHora2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Cargando sistema...");
        setUndecorated(true);
        setResizable(false);

        pnPrincipal.setColorPrimario(new java.awt.Color(0, 51, 102));

        lmCargando.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lmCargando.setText("Cargando sistema...");
        lmCargando.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N

        panelImage1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo_syschool.png"))); // NOI18N

        javax.swing.GroupLayout panelImage1Layout = new javax.swing.GroupLayout(panelImage1);
        panelImage1.setLayout(panelImage1Layout);
        panelImage1Layout.setHorizontalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 377, Short.MAX_VALUE)
        );
        panelImage1Layout.setVerticalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 162, Short.MAX_VALUE)
        );

        rSProgressMaterial1.setForeground(new java.awt.Color(0, 204, 204));
        rSProgressMaterial1.setAnchoProgress(7);

        lbHora1.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lbHora1.setForeground(new java.awt.Color(255, 255, 255));
        lbHora1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbHora1.setText("Versión del sistema:");
        lbHora1.setFocusable(false);
        lbHora1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbHora3.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lbHora3.setForeground(new java.awt.Color(255, 255, 255));
        lbHora3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbHora3.setText("Versión de la BD:");
        lbHora3.setFocusable(false);
        lbHora3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lblVersionBD.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lblVersionBD.setForeground(new java.awt.Color(255, 255, 255));
        lblVersionBD.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblVersionBD.setText("0");
        lblVersionBD.setFocusable(false);
        lblVersionBD.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbHora2.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        lbHora2.setForeground(new java.awt.Color(255, 255, 255));
        lbHora2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbHora2.setText("1.1.");
        lbHora2.setFocusable(false);
        lbHora2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout panelCurves1Layout = new javax.swing.GroupLayout(panelCurves1);
        panelCurves1.setLayout(panelCurves1Layout);
        panelCurves1Layout.setHorizontalGroup(
            panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurves1Layout.createSequentialGroup()
                .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelCurves1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lmCargando, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(panelCurves1Layout.createSequentialGroup()
                        .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelCurves1Layout.createSequentialGroup()
                                .addGap(251, 251, 251)
                                .addComponent(rSProgressMaterial1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelCurves1Layout.createSequentialGroup()
                                .addGap(79, 79, 79)
                                .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 92, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(panelCurves1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbHora1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbHora3, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lbHora2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblVersionBD, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelCurves1Layout.setVerticalGroup(
            panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelCurves1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(lmCargando, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rSProgressMaterial1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbHora2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lbHora1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelCurves1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbHora3)
                    .addComponent(lblVersionBD))
                .addGap(43, 43, 43))
        );

        javax.swing.GroupLayout pnPrincipalLayout = new javax.swing.GroupLayout(pnPrincipal);
        pnPrincipal.setLayout(pnPrincipalLayout);
        pnPrincipalLayout.setHorizontalGroup(
            pnPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnPrincipalLayout.createSequentialGroup()
                .addComponent(panelCurves1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnPrincipalLayout.setVerticalGroup(
            pnPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnPrincipalLayout.createSequentialGroup()
                .addComponent(panelCurves1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lbHora1;
    private javax.swing.JLabel lbHora2;
    private javax.swing.JLabel lbHora3;
    private javax.swing.JLabel lblVersionBD;
    private org.edisoncor.gui.label.LabelMetric lmCargando;
    private org.edisoncor.gui.panel.PanelCurves panelCurves1;
    private org.edisoncor.gui.panel.PanelImage panelImage1;
    private org.edisoncor.gui.panel.Panel pnPrincipal;
    private rojerusan.componentes.RSProgressMaterial rSProgressMaterial1;
    // End of variables declaration//GEN-END:variables
}
