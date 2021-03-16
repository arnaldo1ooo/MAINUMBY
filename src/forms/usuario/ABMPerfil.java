/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms.usuario;

import conexion.Conexion;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;
import utilidades.Metodos;
import utilidades.MetodosTXT;
import utilidades.MetodosCombo;

/**
 *
 * @author Arnaldo Cantero
 */
public class ABMPerfil extends javax.swing.JDialog {

    private Conexion con = new Conexion();
    private Metodos metodos = new Metodos();
    private MetodosTXT metodostxt = new MetodosTXT();
    private MetodosCombo metodoscombo = new MetodosCombo();
    private DefaultTableModel tablemodelPerfil;
    private DefaultTableModel tablemodelAllModulos;

    public ABMPerfil(java.awt.Frame parent, Boolean modal) {
        super(parent, modal);
        initComponents();

        TableRowFilterSupport.forTable(tbAllPerfil).searchable(true).apply(); //Activar filtrado de tabla click derecho en cabecera
        TablaConsultaAllPerfil(); //Trae todos los registros
        TableRowFilterSupport.forTable(tbPerfilModulos).searchable(true).apply(); //Activar filtrado de tabla click derecho en cabecera
        TablaConsultaAllModulos();

        OrdenTabulador();
    }

//--------------------------METODOS----------------------------//
    public void RegistroNuevoModificar() {
        try {
            if (ComprobarCampos() == true) {
                String codperfil = txtCodigo.getText();
                String denominacion = txtDenominacion.getText().toUpperCase();
                String descripcion = metodostxt.MayusSoloPrimeraLetra(taDescripcion.getText());

                if (txtCodigo.getText().equals("")) { //NUEVO REGISTRO
                    int confirmado = JOptionPane.showConfirmDialog(this, "¿Estás seguro de registrar este nuevo perfil?", "Confirmación", JOptionPane.YES_OPTION);
                    if (JOptionPane.YES_OPTION == confirmado) {
                        String sentencia = "CALL SP_PerfilAlta ('" + denominacion + "','" + descripcion + "')";
                        con.EjecutarABM(sentencia, true);

                        //Obtener ultimo codigo perfil
                        String ultimoidperfil = "";
                        con = con.ObtenerRSSentencia("SELECT MAX(per_codigo) AS ultimoid FROM perfil");
                        if (con.getResultSet().next()) {
                            ultimoidperfil = con.getResultSet().getString("ultimoid");
                        }

                        //Nuevos Perfil_Modulo
                        String idmodulo;
                        boolean estado;
                        for (int i = 0; i < tbPerfilModulos.getRowCount(); i++) {
                            idmodulo = tbPerfilModulos.getValueAt(i, 0) + "";
                            estado = (Boolean) tbPerfilModulos.getValueAt(i, 2);
                            if (estado == true) {
                                con.EjecutarABM("CALL SP_PerfilModuloAlta('" + ultimoidperfil + "','" + idmodulo + "')", false);
                            }
                        }
                    }
                } else { //MODIFICAR REGISTRO
                    int confirmado = JOptionPane.showConfirmDialog(this, "¿Estás seguro de modificar este regitro?", "Confirmación", JOptionPane.YES_OPTION);
                    if (JOptionPane.YES_OPTION == confirmado) {
                        String sentencia = "CALL SP_PerfilModificar('" + codperfil + "','" + denominacion + "','" + descripcion + "')";
                        con.EjecutarABM(sentencia, true);

                        //Agregar o eliminar nuevo perfil_modulo
                        String codmodulo;
                        boolean estado;
                        for (int i = 0; i < tbPerfilModulos.getRowCount(); i++) {
                            codmodulo = tbPerfilModulos.getValueAt(i, 0) + "";
                            estado = (Boolean) tbPerfilModulos.getValueAt(i, 2);
                            con = con.ObtenerRSSentencia("SELECT permo_codigo FROM perfil_modulo WHERE permo_perfil='" + codperfil + "' AND permo_modulo='" + codmodulo + "'");
                            if (con.getResultSet().next()) {
                                if (estado == false) {
                                    con.EjecutarABM("CALL SP_PerfilModuloEliminar('" + codperfil + "','" + codmodulo + "')", false);
                                }
                            } else {
                                if (estado == true) {
                                    con.EjecutarABM("CALL SP_PerfilModuloAlta('" + codperfil + "','" + codmodulo + "')", false);
                                }
                            }
                        }
                    }
                }
                TablaConsultaAllPerfil(); //Actualizar tabla
                ModoEdicion(false);
                Limpiar();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void RegistroEliminar() {
        int filasel = tbAllPerfil.getSelectedRow();
        if (filasel != -1) {
            int confirmado = JOptionPane.showConfirmDialog(this, "¿Estás seguro eliminar este perfil?", "Confirmación", JOptionPane.YES_OPTION);
            if (JOptionPane.YES_OPTION == confirmado) {
                int codigo = Integer.parseInt(tbAllPerfil.getValueAt(filasel, 0) + "");
                String sentencia = "CALL SP_PerfilEliminar(" + codigo + ")";
                con.EjecutarABM(sentencia, true);

                TablaConsultaAllPerfil(); //Actualizar tabla
                ModoEdicion(false);
                Limpiar();
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "No se ha seleccionado ninguna fila", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void TablaConsultaAllPerfil() {//Realiza la consulta de los productos que tenemos en la base de datos
        tablemodelPerfil = (DefaultTableModel) tbAllPerfil.getModel();//Cargamos campos de jtable al modeltable
        tablemodelPerfil.setRowCount(0); //Vacia la tabla
        try {
            String sentencia = "CALL SP_PerfilConsulta()";
            con = con.ObtenerRSSentencia(sentencia);
            int codigo;
            String denominacion, descripcion;
            while (con.getResultSet().next()) {
                codigo = con.getResultSet().getInt("per_codigo");
                denominacion = con.getResultSet().getString("per_denominacion");
                descripcion = con.getResultSet().getString("per_descripcion");

                tablemodelPerfil.addRow(new Object[]{codigo, denominacion, descripcion});
            }
            tbAllPerfil.setModel(tablemodelPerfil);
            metodos.AnchuraColumna(tbAllPerfil);
        } catch (Exception e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();

        if (tbAllPerfil.getModel().getRowCount() == 1) {
            lbCantRegistros.setText(tbAllPerfil.getModel().getRowCount() + " Registro encontrado");
        } else {
            lbCantRegistros.setText(tbAllPerfil.getModel().getRowCount() + " Registros encontrados");
        }
    }

    private void TablaConsultaAllModulos() {
        tablemodelAllModulos = (DefaultTableModel) tbPerfilModulos.getModel();//Cargamos campos de jtable al modeltable
        tablemodelAllModulos.setRowCount(0); //Vacia la tabla
        try {
            con = con.ObtenerRSSentencia("CALL SP_ModuloConsulta()");
            int codigo;
            String denominacion;
            while (con.getResultSet().next()) {
                codigo = con.getResultSet().getInt("mo_codigo");
                denominacion = con.getResultSet().getString("mo_denominacion");

                tablemodelAllModulos.addRow(new Object[]{codigo, denominacion});
            }
            tbPerfilModulos.setModel(tablemodelAllModulos);
            metodos.AnchuraColumna(tbPerfilModulos);

            /*tbPerfilModulos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            tbPerfilModulos.getColumnModel().getColumn(0).setPreferredWidth(70);
            tbPerfilModulos.getColumnModel().getColumn(1).setPreferredWidth(tbPerfilModulos.getWidth() - 135);
            tbPerfilModulos.getColumnModel().getColumn(2).setPreferredWidth(50);*/
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void ModoVistaPrevia() {
        txtCodigo.setText(metodos.SiStringEsNull(tbAllPerfil.getValueAt(tbAllPerfil.getSelectedRow(), 0) + ""));
        txtDenominacion.setText(metodos.SiStringEsNull(tbAllPerfil.getValueAt(tbAllPerfil.getSelectedRow(), 1) + ""));
        taDescripcion.setText(metodos.SiStringEsNull(tbAllPerfil.getValueAt(tbAllPerfil.getSelectedRow(), 2) + ""));

        try {
            for (int f = 0; f < tbPerfilModulos.getRowCount(); f++) {
                tbPerfilModulos.setValueAt(false, f, 2);
            }

            String idPerfil = tbAllPerfil.getValueAt(tbAllPerfil.getSelectedRow(), 0) + "";
            lblTituloPerfilModulos.setText("Módulos del perfil: " + tbAllPerfil.getValueAt(tbAllPerfil.getSelectedRow(), 1));
            String sentencia = "SELECT mo_codigo, mo_denominacion FROM perfil_modulo, modulo WHERE permo_perfil = '" + idPerfil
                    + "' AND permo_modulo=mo_codigo ORDER BY mo_denominacion";
            con = con.ObtenerRSSentencia(sentencia);
            while (con.getResultSet().next()) {
                for (int f = 0, estado = 0; f < tbPerfilModulos.getRowCount() && estado == 0; f++) {
                    if (con.getResultSet().getInt("mo_codigo") == (int) tbPerfilModulos.getValueAt(f, 0)) {
                        System.out.println("Prueba " + con.getResultSet().getString("mo_codigo"));
                        tbPerfilModulos.setValueAt(true, f, 2);
                        estado = 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();

    }

    private void ModoEdicion(boolean valor) {
        tbAllPerfil.setEnabled(!valor);
        txtDenominacion.setEnabled(valor);
        taDescripcion.setEnabled(valor);
        tbPerfilModulos.setEnabled(valor);

        btnNuevo.setEnabled(!valor);
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnGuardar.setEnabled(valor);
        btnCancelar.setEnabled(valor);

        txtDenominacion.requestFocus();
    }

    private void Limpiar() {
        txtCodigo.setText("");
        lblDescripcion.setForeground(Color.BLACK);
        txtDenominacion.setText("");
        taDescripcion.setText("");
        tbAllPerfil.clearSelection();

        for (int i = 0; i < tbPerfilModulos.getRowCount(); i++) {
            tbPerfilModulos.setValueAt(false, i, 2);
        }
    }

    public boolean ComprobarCampos() {
        if (metodostxt.ValidarCampoVacioTXT(txtDenominacion, lblDenominacion) == false) {
            return false;
        }

        return true;
    }

//--------------------------iniComponent()No tocar----------------------------//
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpPrincipal = new javax.swing.JPanel();
        jpTabla = new javax.swing.JPanel();
        scPrincipal = new javax.swing.JScrollPane();
        tbAllPerfil = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        lbCantRegistros = new javax.swing.JLabel();
        jpBotones = new javax.swing.JPanel();
        btnNuevo = new javax.swing.JButton();
        btnModificar = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        jtpEdicion = new javax.swing.JTabbedPane();
        jpEdicion = new javax.swing.JPanel();
        lblCodigo = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        lblDescripcion = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblDenominacion = new javax.swing.JLabel();
        txtDenominacion = new javax.swing.JTextField();
        jScrollPane3 = new javax.swing.JScrollPane();
        tbPerfilModulos = new javax.swing.JTable();
        lblTituloPerfilModulos = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taDescripcion = new javax.swing.JTextArea();
        jpBotones2 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        panel2 = new org.edisoncor.gui.panel.Panel();
        labelMetric2 = new org.edisoncor.gui.label.LabelMetric();

        setTitle("Ventana perfiles");
        setBackground(new java.awt.Color(45, 62, 80));
        setResizable(false);

        jpPrincipal.setBackground(new java.awt.Color(233, 255, 255));
        jpPrincipal.setPreferredSize(new java.awt.Dimension(1580, 478));

        jpTabla.setBackground(new java.awt.Color(233, 255, 255));
        jpTabla.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        scPrincipal.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tbAllPerfil.setAutoCreateRowSorter(true);
        tbAllPerfil.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbAllPerfil.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbAllPerfil.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Denominación", "Descripción"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbAllPerfil.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbAllPerfil.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbAllPerfil.setGridColor(new java.awt.Color(0, 153, 204));
        tbAllPerfil.setOpaque(false);
        tbAllPerfil.setRowHeight(20);
        tbAllPerfil.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbAllPerfil.getTableHeader().setReorderingAllowed(false);
        tbAllPerfil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbAllPerfilMousePressed(evt);
            }
        });
        tbAllPerfil.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbAllPerfilKeyReleased(evt);
            }
        });
        scPrincipal.setViewportView(tbAllPerfil);

        lbCantRegistros.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lbCantRegistros.setForeground(new java.awt.Color(153, 153, 0));
        lbCantRegistros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbCantRegistros.setText("0 Registros encontrados");
        lbCantRegistros.setPreferredSize(new java.awt.Dimension(57, 25));

        javax.swing.GroupLayout jpTablaLayout = new javax.swing.GroupLayout(jpTabla);
        jpTabla.setLayout(jpTablaLayout);
        jpTablaLayout.setHorizontalGroup(
            jpTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpTablaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lbCantRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jpTablaLayout.setVerticalGroup(
            jpTablaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpTablaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbCantRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jpBotones.setBackground(new java.awt.Color(233, 255, 255));
        jpBotones.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jpBotones.setPreferredSize(new java.awt.Dimension(100, 50));

        btnNuevo.setBackground(new java.awt.Color(14, 154, 153));
        btnNuevo.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnNuevo.setForeground(new java.awt.Color(255, 255, 255));
        btnNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoNuevo40.png"))); // NOI18N
        btnNuevo.setText("NUEVO");
        btnNuevo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnModificar.setBackground(new java.awt.Color(14, 154, 153));
        btnModificar.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnModificar.setForeground(new java.awt.Color(255, 255, 255));
        btnModificar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoModifcar40.png"))); // NOI18N
        btnModificar.setText("MODIFICAR");
        btnModificar.setEnabled(false);
        btnModificar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnModificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnModificarActionPerformed(evt);
            }
        });

        btnEliminar.setBackground(new java.awt.Color(14, 154, 153));
        btnEliminar.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnEliminar.setForeground(new java.awt.Color(255, 255, 255));
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoEliminar40.png"))); // NOI18N
        btnEliminar.setText("ELIMINAR");
        btnEliminar.setEnabled(false);
        btnEliminar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpBotonesLayout = new javax.swing.GroupLayout(jpBotones);
        jpBotones.setLayout(jpBotonesLayout);
        jpBotonesLayout.setHorizontalGroup(
            jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnNuevo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnModificar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpBotonesLayout.setVerticalGroup(
            jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpBotonesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnNuevo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnModificar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEliminar)
                .addGap(25, 25, 25))
        );

        jtpEdicion.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        jtpEdicion.setName(""); // NOI18N

        jpEdicion.setBackground(new java.awt.Color(233, 255, 255));
        jpEdicion.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        lblCodigo.setBackground(new java.awt.Color(0, 0, 0));
        lblCodigo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblCodigo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCodigo.setText("Código:");

        txtCodigo.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtCodigo.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCodigo.setEnabled(false);

        lblDescripcion.setBackground(new java.awt.Color(0, 0, 0));
        lblDescripcion.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDescripcion.setText("Descripción:");

        jLabel2.setForeground(new java.awt.Color(0, 0, 153));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Campos con (*) son obligatorios");

        lblDenominacion.setBackground(new java.awt.Color(0, 0, 0));
        lblDenominacion.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblDenominacion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblDenominacion.setText("Denominación*:");
        lblDenominacion.setToolTipText("");

        txtDenominacion.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtDenominacion.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDenominacion.setEnabled(false);
        txtDenominacion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDenominacionKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDenominacionKeyTyped(evt);
            }
        });

        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tbPerfilModulos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Módulo", "Estado"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbPerfilModulos.setEnabled(false);
        tbPerfilModulos.setRowHeight(20);
        tbPerfilModulos.setRowSelectionAllowed(false);
        tbPerfilModulos.getTableHeader().setReorderingAllowed(false);
        jScrollPane3.setViewportView(tbPerfilModulos);

        lblTituloPerfilModulos.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        lblTituloPerfilModulos.setText("Módulos del perfil:");

        taDescripcion.setColumns(1);
        taDescripcion.setLineWrap(true);
        taDescripcion.setRows(5);
        taDescripcion.setWrapStyleWord(true);
        taDescripcion.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        taDescripcion.setEnabled(false);
        jScrollPane1.setViewportView(taDescripcion);

        javax.swing.GroupLayout jpEdicionLayout = new javax.swing.GroupLayout(jpEdicion);
        jpEdicion.setLayout(jpEdicionLayout);
        jpEdicionLayout.setHorizontalGroup(
            jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpEdicionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblDenominacion, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCodigo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(lblDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addComponent(txtDenominacion)
                    .addComponent(jScrollPane1))
                .addGap(18, 18, 18)
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addComponent(lblTituloPerfilModulos, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 146, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpEdicionLayout.setVerticalGroup(
            jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpEdicionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(txtDenominacion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDenominacion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpEdicionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpEdicionLayout.createSequentialGroup()
                                .addComponent(lblDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
                    .addGroup(jpEdicionLayout.createSequentialGroup()
                        .addComponent(lblTituloPerfilModulos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jtpEdicion.addTab("Edición", jpEdicion);

        jpBotones2.setBackground(new java.awt.Color(233, 255, 255));
        jpBotones2.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));

        btnGuardar.setBackground(new java.awt.Color(0, 153, 255));
        btnGuardar.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoGuardar.png"))); // NOI18N
        btnGuardar.setText("Guardar");
        btnGuardar.setToolTipText("Inserta el nuevo registro");
        btnGuardar.setEnabled(false);
        btnGuardar.setPreferredSize(new java.awt.Dimension(128, 36));
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        btnGuardar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnGuardarKeyPressed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(255, 138, 138));
        btnCancelar.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoCancelar.png"))); // NOI18N
        btnCancelar.setText("Cancelar");
        btnCancelar.setToolTipText("Cancela la acción");
        btnCancelar.setEnabled(false);
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpBotones2Layout = new javax.swing.GroupLayout(jpBotones2);
        jpBotones2.setLayout(jpBotones2Layout);
        jpBotones2Layout.setHorizontalGroup(
            jpBotones2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBotones2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpBotones2Layout.setVerticalGroup(
            jpBotones2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpBotones2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBotones2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panel2.setColorPrimario(new java.awt.Color(0, 153, 153));
        panel2.setColorSecundario(new java.awt.Color(233, 255, 255));

        labelMetric2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMetric2.setText("PERFILES DE USUARIO");
        labelMetric2.setDireccionDeSombra(110);
        labelMetric2.setFont(new java.awt.Font("Cooper Black", 0, 28)); // NOI18N

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpPrincipalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jpBotones2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(216, 216, 216))
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jtpEdicion)
                    .addGroup(jpPrincipalLayout.createSequentialGroup()
                        .addComponent(jpTabla, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jpTabla, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jtpEdicion, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpBotones2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jtpEdicion.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 793, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("Perfiles");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        RegistroNuevoModificar();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        ModoEdicion(false);
        Limpiar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        Limpiar();
        ModoEdicion(true);
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnModificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnModificarActionPerformed
        ModoEdicion(true);
    }//GEN-LAST:event_btnModificarActionPerformed

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        RegistroEliminar();
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void btnGuardarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnGuardarKeyPressed
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            btnGuardar.doClick();
        }
    }//GEN-LAST:event_btnGuardarKeyPressed

    private void tbAllPerfilMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbAllPerfilMousePressed
        if (tbAllPerfil.isEnabled() == true) {
            btnModificar.setEnabled(true);
            btnEliminar.setEnabled(true);

            ModoVistaPrevia();
        }
    }//GEN-LAST:event_tbAllPerfilMousePressed

    private void tbAllPerfilKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbAllPerfilKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_UP || evt.getKeyCode() == KeyEvent.VK_DOWN) {
            ModoVistaPrevia();
        }
    }//GEN-LAST:event_tbAllPerfilKeyReleased

    private void txtDenominacionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDenominacionKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDenominacionKeyReleased

    private void txtDenominacionKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDenominacionKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDenominacionKeyTyped

    List<Component> ordenTabulador;

    private void OrdenTabulador() {
        ordenTabulador = new ArrayList<>();
        ordenTabulador.add(taDescripcion);
        ordenTabulador.add(btnGuardar);
        setFocusTraversalPolicy(new PersonalizadoFocusTraversalPolicy());
    }

    private class PersonalizadoFocusTraversalPolicy extends FocusTraversalPolicy {

        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            int currentPosition = ordenTabulador.indexOf(aComponent);
            currentPosition = (currentPosition + 1) % ordenTabulador.size();
            return (Component) ordenTabulador.get(currentPosition);
        }

        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int currentPosition = ordenTabulador.indexOf(aComponent);
            currentPosition = (ordenTabulador.size() + currentPosition - 1) % ordenTabulador.size();
            return (Component) ordenTabulador.get(currentPosition);
        }

        public Component getFirstComponent(Container cntnr) {
            return (Component) ordenTabulador.get(0);
        }

        public Component getLastComponent(Container cntnr) {
            return (Component) ordenTabulador.get(ordenTabulador.size() - 1);
        }

        public Component getDefaultComponent(Container cntnr) {
            return (Component) ordenTabulador.get(0);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnModificar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel jpBotones;
    private javax.swing.JPanel jpBotones2;
    private javax.swing.JPanel jpEdicion;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpTabla;
    private javax.swing.JTabbedPane jtpEdicion;
    private org.edisoncor.gui.label.LabelMetric labelMetric2;
    private javax.swing.JLabel lbCantRegistros;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDenominacion;
    private javax.swing.JLabel lblDescripcion;
    private javax.swing.JLabel lblTituloPerfilModulos;
    private org.edisoncor.gui.panel.Panel panel2;
    private javax.swing.JScrollPane scPrincipal;
    private javax.swing.JTextArea taDescripcion;
    private javax.swing.JTable tbAllPerfil;
    private javax.swing.JTable tbPerfilModulos;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDenominacion;
    // End of variables declaration//GEN-END:variables
}
