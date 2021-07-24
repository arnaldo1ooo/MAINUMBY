/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.usuario;

import dao.DAO;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.sql.SQLException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import helpers.Metodos;
import helpers.HelpersComboBox;
import org.apache.log4j.Logger;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;

/**
 *
 * @author Arnaldo Cantero
 */
public class ABMUsuarioRol extends javax.swing.JDialog {

    private DAO con = new DAO();
    private Metodos metodos = new Metodos();
    private HelpersComboBox metodoscombo = new HelpersComboBox();
    private DefaultTableModel tableModelModulo;

    static Logger log_historial = Logger.getLogger(ABMUsuarioRol.class.getName());

    public ABMUsuarioRol(java.awt.Frame parent, Boolean modal) {
        super(parent, modal);
        initComponents();

        TableRowFilterSupport.forTable(tbModulosUsuario).searchable(true).apply(); //Activar filtrado de tabla click derecho en cabecera
        CargarComboBoxes();
        Limpiar();

    }

    //--------------------------METODOS----------------------------//
    private void CargarComboBoxes() {
        //Carga los combobox con las consultas
        metodoscombo.CargarComboConsulta(cbUsuario, "SELECT usu_codigo, CONCAT(usu_nombre,' ', usu_apellido) AS nomape FROM usuario ORDER BY usu_nombre", -1);
    }

    String sentencia;

    private void ConsultaModulosUsuario(String idusuario) {
        tableModelModulo = (DefaultTableModel) tbModulosUsuario.getModel();
        tableModelModulo.setRowCount(0);

        try {
            sentencia = "CALL SP_UsuarioModuloConsulta(" + idusuario + ")";
            con = con.ObtenerRSSentencia(sentencia);
            int codigo, idalta, idmodificar, idbaja;
            String descripcion;
            String[] idRoles;
            while (con.getResultSet().next()) {
                codigo = con.getResultSet().getInt("mo_codigo");
                descripcion = con.getResultSet().getString("mo_denominacion");
                idRoles = con.getResultSet().getString("idRoles").split(",");
                idalta = Integer.parseInt(idRoles[0]);
                idmodificar = Integer.parseInt(idRoles[1]);
                idbaja = Integer.parseInt(idRoles[2]);

                tableModelModulo.addRow(new Object[]{codigo, descripcion, idalta, idmodificar, idbaja});
            }
            tbModulosUsuario.setModel(tableModelModulo);
        } catch (SQLException e) {
            log_historial.error("Error 1001: " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            log_historial.error("Error 1002: " + e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();

        tbModulosUsuario.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbModulosUsuario.getColumnModel().getColumn(0).setPreferredWidth(60);
        tbModulosUsuario.getColumnModel().getColumn(0).setResizable(false);
        tbModulosUsuario.getColumnModel().getColumn(1).setPreferredWidth(tbModulosUsuario.getWidth() - 60);
        tbModulosUsuario.getColumnModel().getColumn(1).setResizable(false);
        metodos.OcultarColumna(tbModulosUsuario, 2);
        metodos.OcultarColumna(tbModulosUsuario, 3);
        metodos.OcultarColumna(tbModulosUsuario, 4);
    }

    public void RegistroGuardar() {
        String idusuario = metodoscombo.ObtenerIDSelectCombo(cbUsuario) + "";
        String idrolalta = tbModulosUsuario.getValueAt(tbModulosUsuario.getSelectedRow(), 2) + "";
        String idrolmodificar = tbModulosUsuario.getValueAt(tbModulosUsuario.getSelectedRow(), 3) + "";
        String idrolbaja = tbModulosUsuario.getValueAt(tbModulosUsuario.getSelectedRow(), 4) + "";

        int confirmado = JOptionPane.showConfirmDialog(this, "¿Estás seguro de registrar estos roles?", "Confirmación", JOptionPane.YES_OPTION);
        if (JOptionPane.YES_OPTION == confirmado) {
            if (altaExiste == true && chbAlta.isSelected() == false) {
                sentencia = "CALL SP_UsuarioRolEliminar ('" + idusuario + "','" + idrolalta + "')";
            }
            if (altaExiste == false && chbAlta.isSelected() == true) {
                sentencia = "CALL SP_UsuarioRolAlta ('" + idusuario + "','" + idrolalta + "')";
            }
            con.EjecutarABM(sentencia, false);

            if (modificarExiste == true && chbModificar.isSelected() == false) {
                sentencia = "CALL SP_UsuarioRolEliminar ('" + idusuario + "','" + idrolmodificar + "')";
            }
            if (modificarExiste == false && chbModificar.isSelected() == true) {
                sentencia = "CALL SP_UsuarioRolAlta ('" + idusuario + "','" + idrolmodificar + "')";
            }
            con.EjecutarABM(sentencia, false);

            if (bajaExiste == true && chbBaja.isSelected() == false) {
                sentencia = "CALL SP_UsuarioRolEliminar ('" + idusuario + "','" + idrolbaja + "')";
            }
            if (bajaExiste == false && chbBaja.isSelected() == true) {
                sentencia = "CALL SP_UsuarioRolAlta ('" + idusuario + "','" + idrolbaja + "')";
            }
            con.EjecutarABM(sentencia, true);

            ModoEdicion(false);
            Limpiar();
        }
    }

    private void ModoEdicion(boolean valor) {
        cbUsuario.setEnabled(!valor);
        tbModulosUsuario.setEnabled(!valor);
        scPrincipal.setEnabled(!valor);
        chbAlta.setEnabled(valor);
        chbModificar.setEnabled(valor);
        chbBaja.setEnabled(valor);
        btnModificarGuardar.setEnabled(valor);
        btnCancelar.setEnabled(valor);
    }

    private void Limpiar() {
        cbUsuario.setSelectedIndex(-1);

        tableModelModulo = (DefaultTableModel) tbModulosUsuario.getModel();
        tableModelModulo.setRowCount(0);

        btnModificarGuardar.setText("Modificar");

        URL url = this.getClass().getResource("/iconos/Iconos20x20/IconoModificar20.png");
        btnModificarGuardar.setIcon(new ImageIcon(url));

        chbAlta.setSelected(false);
        chbModificar.setSelected(false);
        chbBaja.setSelected(false);

        pnRoles.setBackground(new java.awt.Color(102, 102, 102));
    }

    //--------------------------iniComponent()No tocar----------------------------//
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpPrincipal = new javax.swing.JPanel();
        jpEdicion = new javax.swing.JPanel();
        scPrincipal = new javax.swing.JScrollPane();
        tbModulosUsuario = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        lblBuscarCampo = new javax.swing.JLabel();
        btnModificarGuardar = new javax.swing.JButton();
        pnRoles = new javax.swing.JPanel();
        chbAlta = new javax.swing.JCheckBox();
        chbModificar = new javax.swing.JCheckBox();
        chbBaja = new javax.swing.JCheckBox();
        lblRolesUsuario = new javax.swing.JLabel();
        btnCancelar = new javax.swing.JButton();
        panel2 = new org.edisoncor.gui.panel.Panel();
        labelMetric2 = new org.edisoncor.gui.label.LabelMetric();
        cbUsuario = new javax.swing.JComboBox();
        lblSeleccioneUsuario = new javax.swing.JLabel();

        setTitle("Ventana roles de usuario");
        setBackground(new java.awt.Color(45, 62, 80));
        setResizable(false);

        jpPrincipal.setBackground(new java.awt.Color(233, 255, 255));
        jpPrincipal.setPreferredSize(new java.awt.Dimension(1580, 478));

        jpEdicion.setBackground(new java.awt.Color(233, 255, 255));
        jpEdicion.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        scPrincipal.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tbModulosUsuario.setAutoCreateRowSorter(true);
        tbModulosUsuario.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbModulosUsuario.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbModulosUsuario.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Descripción", "idAlta", "idModificar", "idBaja"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbModulosUsuario.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbModulosUsuario.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbModulosUsuario.setGridColor(new java.awt.Color(0, 153, 204));
        tbModulosUsuario.setOpaque(false);
        tbModulosUsuario.setRowHeight(20);
        tbModulosUsuario.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbModulosUsuario.getTableHeader().setReorderingAllowed(false);
        tbModulosUsuario.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbModulosUsuarioMousePressed(evt);
            }
        });
        tbModulosUsuario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbModulosUsuarioKeyReleased(evt);
            }
        });
        scPrincipal.setViewportView(tbModulosUsuario);

        lblBuscarCampo.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblBuscarCampo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBuscarCampo.setText("Módulos a los que puede acceder el usuario");

        btnModificarGuardar.setBackground(new java.awt.Color(0, 153, 102));
        btnModificarGuardar.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        btnModificarGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificarGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoModificar20.png"))); // NOI18N
        btnModificarGuardar.setText("Modificar");
        btnModificarGuardar.setToolTipText("Inserta el nuevo registro");
        btnModificarGuardar.setEnabled(false);
        btnModificarGuardar.setPreferredSize(new java.awt.Dimension(128, 36));
        btnModificarGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarGuardarActionPerformed(evt);
            }
        });
        btnModificarGuardar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnModificarGuardarKeyPressed(evt);
            }
        });

        pnRoles.setBackground(new java.awt.Color(102, 102, 102));
        pnRoles.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        chbAlta.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        chbAlta.setForeground(new java.awt.Color(255, 255, 255));
        chbAlta.setText("ALTA");
        chbAlta.setEnabled(false);

        chbModificar.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        chbModificar.setForeground(new java.awt.Color(255, 255, 255));
        chbModificar.setText("MODIFICAR");
        chbModificar.setEnabled(false);

        chbBaja.setFont(new java.awt.Font("sansserif", 1, 15)); // NOI18N
        chbBaja.setForeground(new java.awt.Color(255, 255, 255));
        chbBaja.setText("BAJA");
        chbBaja.setEnabled(false);

        javax.swing.GroupLayout pnRolesLayout = new javax.swing.GroupLayout(pnRoles);
        pnRoles.setLayout(pnRolesLayout);
        pnRolesLayout.setHorizontalGroup(
            pnRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnRolesLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(pnRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chbModificar)
                    .addComponent(chbAlta, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chbBaja, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnRolesLayout.setVerticalGroup(
            pnRolesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnRolesLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(chbAlta)
                .addGap(18, 18, 18)
                .addComponent(chbModificar)
                .addGap(18, 18, 18)
                .addComponent(chbBaja)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        lblRolesUsuario.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblRolesUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRolesUsuario.setText("Roles");

        btnCancelar.setBackground(new java.awt.Color(255, 153, 153));
        btnCancelar.setFont(new java.awt.Font("sansserif", 1, 16)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoCancelar20.png"))); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.setToolTipText("Inserta el nuevo registro");
        btnCancelar.setEnabled(false);
        btnCancelar.setPreferredSize(new java.awt.Dimension(128, 36));
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        btnCancelar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnCancelarKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout jpEdicionLayout = new javax.swing.GroupLayout(jpEdicion);
        jpEdicion.setLayout(jpEdicionLayout);
        jpEdicionLayout.setHorizontalGroup(
            jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpEdicionLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBuscarCampo)
                    .addComponent(scPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addComponent(btnModificarGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pnRoles, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRolesUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15))
        );
        jpEdicionLayout.setVerticalGroup(
            jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpEdicionLayout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBuscarCampo)
                    .addComponent(lblRolesUsuario))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addComponent(pnRoles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnModificarGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(scPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18))
        );

        panel2.setColorPrimario(new java.awt.Color(0, 153, 153));
        panel2.setColorSecundario(new java.awt.Color(233, 255, 255));

        labelMetric2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMetric2.setText("ROLES DE USUARIO");
        labelMetric2.setDireccionDeSombra(110);
        labelMetric2.setFont(new java.awt.Font("Cooper Black", 0, 28)); // NOI18N

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        cbUsuario.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbUsuarioItemStateChanged(evt);
            }
        });

        lblSeleccioneUsuario.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblSeleccioneUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSeleccioneUsuario.setText("Seleccione el usuario");

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSeleccioneUsuario)
                    .addComponent(cbUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(361, Short.MAX_VALUE))
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jpEdicion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblSeleccioneUsuario)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jpEdicion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnModificarGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarGuardarActionPerformed
        if (btnModificarGuardar.getText().equals("Modificar")) {
            btnModificarGuardar.setText("Guardar");

            URL url = this.getClass().getResource("/iconos/Iconos20x20/IconoGuardar20.png");
            btnModificarGuardar.setIcon(new ImageIcon(url));

            ModoEdicion(true);
            pnRoles.setBackground(new java.awt.Color(0, 102, 102));
        } else {
            if (btnModificarGuardar.getText().equals("Guardar")) {
                RegistroGuardar();
                ModoEdicion(false);
                Limpiar();
            }
        }


    }//GEN-LAST:event_btnModificarGuardarActionPerformed

    private void btnModificarGuardarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnModificarGuardarKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            btnModificarGuardar.doClick();
        }
    }//GEN-LAST:event_btnModificarGuardarKeyPressed

    private void cbUsuarioItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbUsuarioItemStateChanged
        if (cbUsuario.getSelectedIndex() != -1) {
            ConsultaModulosUsuario(metodoscombo.ObtenerIDSelectCombo(cbUsuario) + "");
        }
    }//GEN-LAST:event_cbUsuarioItemStateChanged

    String moduloselect;
    private void tbModulosUsuarioMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbModulosUsuarioMousePressed
        if (tbModulosUsuario.getSelectedRowCount() > 0 && tbModulosUsuario.isEnabled()) {
            ConsultaRoles();
        }
    }//GEN-LAST:event_tbModulosUsuarioMousePressed

    boolean altaExiste;
    boolean modificarExiste;
    boolean bajaExiste;

    private void ConsultaRoles() {
        lblRolesUsuario.setText("Roles del módulo " + tbModulosUsuario.getValueAt(tbModulosUsuario.getSelectedRow(), 1));
        btnModificarGuardar.setEnabled(true);

        altaExiste = false;
        modificarExiste = false;
        bajaExiste = false;
        try {
            moduloselect = tbModulosUsuario.getValueAt(tbModulosUsuario.getSelectedRow(), 1) + "";
            chbAlta.setSelected(false);
            chbModificar.setSelected(false);
            chbBaja.setSelected(false);
            sentencia = "CALL SP_UsuarioRolConsulta('" + metodoscombo.ObtenerIDSelectCombo(cbUsuario) + "','" + moduloselect + "')";
            con = con.ObtenerRSSentencia(sentencia);

            while (con.getResultSet().next()) {
                switch (con.getResultSet().getString("rol_denominacion")) {
                    case "ALTA" -> {
                        chbAlta.setSelected(true);
                        altaExiste = true;
                    }
                    case "MODIFICAR" -> {
                        chbModificar.setSelected(true);
                        modificarExiste = true;
                    }
                    case "BAJA" -> {
                        chbBaja.setSelected(true);
                        bajaExiste = true;
                    }
                    default ->
                        System.out.println("Error switch " + con.getResultSet().getString("rol_denominacion"));
                }
            }
        } catch (SQLException e) {
            log_historial.error("Error 1003: " + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            log_historial.error("Error 1004: " + e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void tbModulosUsuarioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbModulosUsuarioKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN && tbModulosUsuario.isEnabled()) {
            ConsultaRoles();
        }
    }//GEN-LAST:event_tbModulosUsuarioKeyReleased

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        ModoEdicion(false);
        Limpiar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnCancelarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnCancelarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelarKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnModificarGuardar;
    private javax.swing.JComboBox cbUsuario;
    private javax.swing.JCheckBox chbAlta;
    private javax.swing.JCheckBox chbBaja;
    private javax.swing.JCheckBox chbModificar;
    private javax.swing.JPanel jpEdicion;
    private javax.swing.JPanel jpPrincipal;
    private org.edisoncor.gui.label.LabelMetric labelMetric2;
    private javax.swing.JLabel lblBuscarCampo;
    private javax.swing.JLabel lblRolesUsuario;
    private javax.swing.JLabel lblSeleccioneUsuario;
    private org.edisoncor.gui.panel.Panel panel2;
    private javax.swing.JPanel pnRoles;
    private javax.swing.JScrollPane scPrincipal;
    private javax.swing.JTable tbModulosUsuario;
    // End of variables declaration//GEN-END:variables
}
