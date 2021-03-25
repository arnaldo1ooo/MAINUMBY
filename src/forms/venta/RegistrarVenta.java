/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forms.venta;

import conexion.Conexion;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import utilidades.Metodos;
import utilidades.MetodosCombo;
import utilidades.MetodosImagen;
import utilidades.MetodosTXT;
import utilidades.VistaCompleta;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public final class RegistrarVenta extends javax.swing.JDialog {

    Conexion con = new Conexion();
    Metodos metodos = new Metodos();
    MetodosTXT metodostxt = new MetodosTXT();
    MetodosCombo metodoscombo = new MetodosCombo();
    MetodosImagen metodosimagen = new MetodosImagen();
    private final String rutaFotoProducto = "C:\\MAINUMBY\\productos\\imagenes\\";
    private final String rutaFotoDefault = "/src/images/IconoProductoSinFoto.png";
    private DefaultTableModel tablemodelDetalleVenta;
    private DefaultTableModel tabmodelProductos;
    public static String codUsuario;
    private Color colorGris = Color.GRAY;
    private int advertenciaDeStock = 5;

    public RegistrarVenta(java.awt.Frame parent, Boolean modal) {
        super(parent, modal);
        initComponents();

        GenerarNumVenta();
        tablemodelDetalleVenta = (DefaultTableModel) tbDetalleVenta.getModel();
        CargarComboBoxes();

        //Obtener fecha actual
        dcFechaVenta.setDate(new Date());

        TablaAllProducto();

        //Permiso Roles de usuario
        String permisos = metodos.PermisoRol(codUsuario, "VENTA");
        btnGuardar.setVisible(permisos.contains("A"));
    }

//--------------------------METODOS----------------------------//
    public void CargarComboBoxes() {
        //Carga los combobox con las consultas
        metodoscombo.CargarComboConsulta(cbVendedor, "SELECT fun_codigo, CONCAT(fun_nombre,' ', fun_apellido) AS nomape "
                + "FROM funcionario WHERE fun_cargo = 1 ORDER BY fun_nombre", -1);

        metodoscombo.CargarComboConsulta(cbCliente, "SELECT cli_codigo, CONCAT(cli_nombre,' ', cli_apellido) AS nomape "
                + "FROM cliente ORDER BY cli_nombre", 1);
    }

    public void RegistroNuevo() {
        //Registra la venta
        if (ComprobarCamposVenta() == true) {
            String numVenta, fechaVenta;
            int vendedor, cliente, tipoDoc;
            Double totalVenta;

            numVenta = lblNumVenta.getText();
            vendedor = metodoscombo.ObtenerIDSelectCombo(cbVendedor);
            cliente = metodoscombo.ObtenerIDSelectCombo(cbCliente);
            tipoDoc = cbTipoDocumento.getSelectedIndex();
            DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
            fechaVenta = formatoFecha.format(dcFechaVenta.getDate());
            totalVenta = metodostxt.StringAFormatoAmericano(txtImporte.getText());

            int confirmado = JOptionPane.showConfirmDialog(this, "¿Esta seguro de crear esta nueva venta?", "Confirmación", JOptionPane.YES_OPTION);
            if (JOptionPane.YES_OPTION == confirmado) {
                try {
                    //Registrar nueva venta
                    String sentencia = "CALL SP_VentaAlta('" + numVenta + "','" + vendedor + "','" + cliente + "','"
                            + tipoDoc + "','" + fechaVenta + "','" + totalVenta + "')";
                    con.EjecutarABM(sentencia, false);

                    //Obtener el id de la venta
                    String idultimaventa = con.ObtenerUltimoID("SELECT MAX(ven_codigo) AS ultimoid FROM venta WHERE ven_numventa='" + numVenta + "'");

                    //Registra los productos de la venta                      
                    String idProducto, moneda;
                    int cantidadUnitaria;
                    double precioCompra;
                    double precioVentaBruto;
                    double descuento;

                    int cantfila = tbDetalleVenta.getRowCount();
                    for (int fila = 0; fila < cantfila; fila++) {
                        idProducto = tbDetalleVenta.getValueAt(fila, 0).toString();
                        cantidadUnitaria = Integer.parseInt(tbDetalleVenta.getValueAt(fila, 3).toString());
                        precioCompra = ObtenerUltimoPrecioCompra(idProducto);
                        precioVentaBruto = metodostxt.StringAFormatoAmericano(tbDetalleVenta.getValueAt(fila, 4).toString());
                        descuento = Double.parseDouble(tbDetalleVenta.getValueAt(fila, 5).toString());
                        moneda = tbDetalleVenta.getValueAt(fila, 7).toString();

                        //Se registran los productos de la venta
                        sentencia = "CALL SP_VentaDetalleAlta('" + idultimaventa + "','" + idProducto + "','"
                                + cantidadUnitaria + "','" + precioCompra + "','" + precioVentaBruto + "','" + descuento + "')";
                        con.EjecutarABM(sentencia, false);
                    }

                    Toolkit.getDefaultToolkit().beep(); //BEEP
                    JOptionPane.showMessageDialog(this, "Se agregó correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    Limpiar();
                    GenerarNumVenta();
                } catch (HeadlessException ex) {
                    JOptionPane.showMessageDialog(this, "Ocurrió un Error " + ex.getMessage());
                    Logger.getLogger(RegistrarVenta.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void Limpiar() {
        cbCliente.setSelectedIndex(-1);
        cbTipoDocumento.setSelectedIndex(1);
        Calendar c2 = new GregorianCalendar();
        dcFechaVenta.setCalendar(c2);

        cbMoneda.setSelectedIndex(1);
        cbMoneda.setEnabled(true);

        tablemodelDetalleVenta.setRowCount(0);
        btnEliminar.setEnabled(false);
        txtImporte.setEnabled(false);
        txtImporte.setText("0");
        txtImporte.setForeground(Color.BLACK);
        txtVuelto.setText("0");
        txtTotalVenta.setText("0");
        lblCodigoProducto.setForeground(Color.BLACK);
        lblCantidad.setForeground(Color.BLACK);
        lblDescuento.setForeground(Color.BLACK);
    }

    private boolean ComprobarCamposVenta() {
        if (cbVendedor.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione el vendedor/a", "Advertencia", JOptionPane.WARNING_MESSAGE);
            cbVendedor.requestFocus();
            return false;
        }

        if (dcFechaVenta.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Complete la fecha de venta", "Advertencia", JOptionPane.WARNING_MESSAGE);
            dcFechaVenta.requestFocus();
            return false;
        }

        int cantidadProductos = tbDetalleVenta.getModel().getRowCount();
        if (cantidadProductos <= 0) {
            JOptionPane.showMessageDialog(this, "No se cargó ningún producto", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtImporte.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Ingrese el importe", "Advertencia", JOptionPane.WARNING_MESSAGE);
            txtImporte.requestFocus();
            return false;
        }

        double importe = metodostxt.StringAFormatoAmericano(txtImporte.getText());
        double totalventa = metodostxt.StringAFormatoAmericano(txtTotalVenta.getText());
        if (totalventa > importe || txtImporte.getText().equals("")) {
            JOptionPane.showMessageDialog(null, "El importe debe ser mayor al total de la venta", "Advertencia", JOptionPane.WARNING_MESSAGE);
            txtImporte.requestFocus();
            return false;
        }

        return true;
    }

    private boolean ComprobarCamposProducto() {
        if (metodostxt.ValidarCampoVacioTXT(txtCodigoProducto, lblCodigoProducto) == false) {
            System.out.println("Validar CodigoProducto false");
            return false;
        } else {
            if (ConsultaProducto(txtCodigoProducto.getText()) == false) {
                JOptionPane.showMessageDialog(this, "El Codigo de producto ingresado no existe", "Advertencia", JOptionPane.WARNING_MESSAGE);
                txtCodigoProducto.requestFocus();
                return false;
            }
        }

        String codigoproducto = txtCodigoProducto.getText();
        String filaactual;
        for (int i = 0; i < tbDetalleVenta.getRowCount(); i++) {
            filaactual = tbDetalleVenta.getValueAt(i, 1).toString();
            if (codigoproducto.equals(filaactual) == true) {
                JOptionPane.showMessageDialog(this, "Este producto ya se encuentra cargado", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (metodostxt.ValidarCampoVacioTXT(txtCantidadUnitaria, lblCantidad) == false) {
            System.out.println("Validar Cantidad adquirida false");
            return false;
        }

        if (txtDescuento.getText().equals("")) {
            txtDescuento.setText("0");
        }

        int cantidad = Integer.parseInt(txtCantidadUnitaria.getText());
        if (cantidad <= 0) {
            JOptionPane.showMessageDialog(this, "La cantidad no puede ser menor o igual a 0");
            txtCantidadUnitaria.requestFocus();
            lblCantidad.setForeground(Color.RED);
            return false;
        }

        int stock = Integer.parseInt(txtExistenciaActual.getText());
        if (cantidad > stock) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock, el stock actual del producto es de " + stock);
            txtCantidadUnitaria.requestFocus();
            txtCantidadUnitaria.setForeground(Color.RED);
            return false;
        }

        return true;
    }

//--------------------------iniComponent()No tocar----------------------------//
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BuscadorProducto = new javax.swing.JDialog();
        panel6 = new org.edisoncor.gui.panel.Panel();
        jLabel12 = new javax.swing.JLabel();
        txtBuscarProducto = new javax.swing.JTextField();
        lblBuscarCampoProducto = new javax.swing.JLabel();
        cbCampoBuscarProducto = new javax.swing.JComboBox();
        lbCantRegistrosProducto = new javax.swing.JLabel();
        scProductosBuscadorProductos = new javax.swing.JScrollPane();
        tbProductosBuscadorProductos = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        jpPrincipal = new javax.swing.JPanel();
        jpBotones = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jpDatosVenta = new javax.swing.JPanel();
        lblRucCedula = new javax.swing.JLabel();
        cbCliente = new javax.swing.JComboBox<>();
        btnBuscarCliente = new javax.swing.JButton();
        cbTipoDocumento = new javax.swing.JComboBox<>();
        lblRucCedula1 = new javax.swing.JLabel();
        lblFechaRegistro = new javax.swing.JLabel();
        dcFechaVenta = new com.toedter.calendar.JDateChooser();
        lblRucCedula2 = new javax.swing.JLabel();
        cbVendedor = new javax.swing.JComboBox<>();
        btnABMCliente = new javax.swing.JButton();
        panelDatosProducto = new javax.swing.JPanel();
        lblCodigoProducto = new javax.swing.JLabel();
        txtCodigoProducto = new javax.swing.JTextField();
        lblTituloDescripcion = new javax.swing.JLabel();
        txtDescripcionProducto = new javax.swing.JTextField();
        lblCodigo6 = new javax.swing.JLabel();
        txtExistenciaActual = new javax.swing.JTextField();
        lblCodigo7 = new javax.swing.JLabel();
        txtPrecioGs = new javax.swing.JTextField();
        lblMoneda = new javax.swing.JLabel();
        txtIdProducto = new javax.swing.JTextField();
        lblIDProducto = new javax.swing.JLabel();
        lblImagen = new javax.swing.JLabel();
        btnBuscarProducto = new javax.swing.JButton();
        lblCodigo8 = new javax.swing.JLabel();
        lblMoneda2 = new javax.swing.JLabel();
        txtPrecioGs1 = new javax.swing.JTextField();
        btnPantallaCompleta = new javax.swing.JButton();
        lblCodigo9 = new javax.swing.JLabel();
        jpProductos = new javax.swing.JPanel();
        btnEliminar = new javax.swing.JButton();
        btnAnadir = new javax.swing.JButton();
        txtCantidadUnitaria = new javax.swing.JTextField();
        lblCantidad = new javax.swing.JLabel();
        cbMoneda = new javax.swing.JComboBox<>();
        lblCodigo10 = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        txtDescuento = new javax.swing.JTextField();
        panel2 = new org.edisoncor.gui.panel.Panel();
        labelMetric2 = new org.edisoncor.gui.label.LabelMetric();
        labelMetric1 = new org.edisoncor.gui.label.LabelMetric();
        lblNumVenta = new org.edisoncor.gui.label.LabelMetric();
        jPanel3 = new javax.swing.JPanel();
        lblTituloTotalCompra1 = new javax.swing.JLabel();
        txtImporte = new javax.swing.JTextField();
        txtVuelto = new javax.swing.JTextField();
        lblTituloTotalCompra2 = new javax.swing.JLabel();
        lblTotalMoneda = new javax.swing.JLabel();
        txtTotalVenta = new javax.swing.JTextField();
        lblTituloTotalCompra = new javax.swing.JLabel();
        lblTotalMoneda1 = new javax.swing.JLabel();
        lblTotalMoneda2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        scPrincipal = new javax.swing.JScrollPane();
        tbDetalleVenta = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };

        BuscadorProducto.setTitle("Buscador de apoderados");
        BuscadorProducto.setModal(true);
        BuscadorProducto.setSize(new java.awt.Dimension(760, 310));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoBuscar40.png"))); // NOI18N
        jLabel12.setText("  BUSCAR ");

        txtBuscarProducto.setFont(new java.awt.Font("Tahoma", 1, 17)); // NOI18N
        txtBuscarProducto.setForeground(new java.awt.Color(0, 153, 153));
        txtBuscarProducto.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtBuscarProducto.setCaretColor(new java.awt.Color(0, 204, 204));
        txtBuscarProducto.setDisabledTextColor(new java.awt.Color(0, 204, 204));
        txtBuscarProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarProductoKeyReleased(evt);
            }
        });

        lblBuscarCampoProducto.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblBuscarCampoProducto.setForeground(new java.awt.Color(255, 255, 255));
        lblBuscarCampoProducto.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBuscarCampoProducto.setText("Buscar por:");

        lbCantRegistrosProducto.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lbCantRegistrosProducto.setForeground(new java.awt.Color(153, 153, 0));
        lbCantRegistrosProducto.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbCantRegistrosProducto.setText("0 Registros encontrados");
        lbCantRegistrosProducto.setPreferredSize(new java.awt.Dimension(57, 25));

        tbProductosBuscadorProductos.setAutoCreateRowSorter(true);
        tbProductosBuscadorProductos.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbProductosBuscadorProductos.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbProductosBuscadorProductos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Codigo", "Identificador", "Descripción", "Categoría", "Existencia", "Estado", "Observación"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbProductosBuscadorProductos.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbProductosBuscadorProductos.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbProductosBuscadorProductos.setGridColor(new java.awt.Color(0, 153, 204));
        tbProductosBuscadorProductos.setOpaque(false);
        tbProductosBuscadorProductos.setRowHeight(20);
        tbProductosBuscadorProductos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbProductosBuscadorProductos.getTableHeader().setReorderingAllowed(false);
        tbProductosBuscadorProductos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbProductosBuscadorProductosMousePressed(evt);
            }
        });
        tbProductosBuscadorProductos.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbProductosBuscadorProductosKeyReleased(evt);
            }
        });
        scProductosBuscadorProductos.setViewportView(tbProductosBuscadorProductos);

        javax.swing.GroupLayout panel6Layout = new javax.swing.GroupLayout(panel6);
        panel6.setLayout(panel6Layout);
        panel6Layout.setHorizontalGroup(
            panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(lblBuscarCampoProducto)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbCampoBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(lbCantRegistrosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(scProductosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(44, Short.MAX_VALUE))
        );
        panel6Layout.setVerticalGroup(
            panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblBuscarCampoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbCampoBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scProductosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbCantRegistrosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );

        javax.swing.GroupLayout BuscadorProductoLayout = new javax.swing.GroupLayout(BuscadorProducto.getContentPane());
        BuscadorProducto.getContentPane().setLayout(BuscadorProductoLayout);
        BuscadorProductoLayout.setHorizontalGroup(
            BuscadorProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        BuscadorProductoLayout.setVerticalGroup(
            BuscadorProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setTitle("Ventana Registrar Venta");
        setBackground(new java.awt.Color(45, 62, 80));
        setResizable(false);

        jpPrincipal.setBackground(new java.awt.Color(233, 255, 255));
        jpPrincipal.setPreferredSize(new java.awt.Dimension(1580, 478));

        jpBotones.setBackground(new java.awt.Color(233, 255, 255));
        jpBotones.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder()));
        jpBotones.setPreferredSize(new java.awt.Dimension(100, 50));

        btnGuardar.setBackground(new java.awt.Color(0, 153, 255));
        btnGuardar.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoGuardar20.png"))); // NOI18N
        btnGuardar.setText("Registrar venta");
        btnGuardar.setToolTipText("Inserta el nuevo registro");
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

        btnCancelar.setBackground(new java.awt.Color(255, 101, 101));
        btnCancelar.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoCancelar20.png"))); // NOI18N
        btnCancelar.setText("Limpiar campos");
        btnCancelar.setToolTipText("Cancela la acción");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpBotonesLayout = new javax.swing.GroupLayout(jpBotones);
        jpBotones.setLayout(jpBotonesLayout);
        jpBotonesLayout.setHorizontalGroup(
            jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpBotonesLayout.setVerticalGroup(
            jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpBotonesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBotonesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jpDatosVenta.setBackground(new java.awt.Color(233, 255, 255));
        jpDatosVenta.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos de la venta"));

        lblRucCedula.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula.setText("Cliente");
        lblRucCedula.setToolTipText("");

        btnBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoBuscar20.png"))); // NOI18N
        btnBuscarCliente.setToolTipText("Buscar cliente");
        btnBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteActionPerformed(evt);
            }
        });

        cbTipoDocumento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SIN ESPECIFICAR", "NOTA", "RECIBO", "FACTURA" }));
        cbTipoDocumento.setSelectedIndex(1);

        lblRucCedula1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula1.setText("Tipo de documento");
        lblRucCedula1.setToolTipText("");

        lblFechaRegistro.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblFechaRegistro.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblFechaRegistro.setText("Fecha de venta");
        lblFechaRegistro.setToolTipText("");

        dcFechaVenta.setEnabled(false);
        dcFechaVenta.setMaxSelectableDate(new java.util.Date(4102455600000L));
        dcFechaVenta.setMinSelectableDate(new java.util.Date(631162800000L));

        lblRucCedula2.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula2.setText("Vendedor/a*");
        lblRucCedula2.setToolTipText("");

        btnABMCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        btnABMCliente.setToolTipText("Nuevo proveedor");
        btnABMCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnABMClienteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpDatosVentaLayout = new javax.swing.GroupLayout(jpDatosVenta);
        jpDatosVenta.setLayout(jpDatosVentaLayout);
        jpDatosVentaLayout.setHorizontalGroup(
            jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbVendedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRucCedula2, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jpDatosVentaLayout.createSequentialGroup()
                        .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(btnBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(3, 3, 3)
                .addComponent(btnABMCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblRucCedula1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cbTipoDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcFechaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpDatosVentaLayout.setVerticalGroup(
            jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblRucCedula2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRucCedula1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbVendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcFechaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnABMCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        panelDatosProducto.setBackground(new java.awt.Color(233, 255, 255));
        panelDatosProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos del producto"));

        lblCodigoProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigoProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigoProducto.setText("Código del producto");

        txtCodigoProducto.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtCodigoProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCodigoProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoProductoActionPerformed(evt);
            }
        });
        txtCodigoProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCodigoProductoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodigoProductoKeyTyped(evt);
            }
        });

        lblTituloDescripcion.setFont(new java.awt.Font("Berlin Sans FB Demi", 1, 12)); // NOI18N
        lblTituloDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloDescripcion.setText("Descripción del producto");
        lblTituloDescripcion.setToolTipText("");

        txtDescripcionProducto.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtDescripcionProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescripcionProducto.setEnabled(false);

        lblCodigo6.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo6.setText("Stock actual");

        txtExistenciaActual.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtExistenciaActual.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtExistenciaActual.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtExistenciaActual.setEnabled(false);

        lblCodigo7.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo7.setText("Precio del producto");

        txtPrecioGs.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtPrecioGs.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPrecioGs.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtPrecioGs.setEnabled(false);

        lblMoneda.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblMoneda.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMoneda.setText("Guaraníes");

        txtIdProducto.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        txtIdProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtIdProducto.setEnabled(false);

        lblIDProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblIDProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIDProducto.setText("ID");

        lblImagen.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        lblImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImagen.setText("PRODUCTO SIN FOTO");
        lblImagen.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnBuscarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoBuscar20.png"))); // NOI18N
        btnBuscarProducto.setToolTipText("Buscador de productos");
        btnBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProductoActionPerformed(evt);
            }
        });

        lblCodigo8.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo8.setText("Precio promocional");

        lblMoneda2.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblMoneda2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMoneda2.setText("Guaraníes");

        txtPrecioGs1.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtPrecioGs1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPrecioGs1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtPrecioGs1.setEnabled(false);

        btnPantallaCompleta.setBackground(new java.awt.Color(0, 255, 255));
        btnPantallaCompleta.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnPantallaCompleta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoPantallacompleta20.png"))); // NOI18N
        btnPantallaCompleta.setToolTipText("Ampliar vista de Imagen del producto");
        btnPantallaCompleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPantallaCompletaActionPerformed(evt);
            }
        });

        lblCodigo9.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCodigo9.setText("Cantidad");
        lblCodigo9.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout panelDatosProductoLayout = new javax.swing.GroupLayout(panelDatosProducto);
        panelDatosProducto.setLayout(panelDatosProductoLayout);
        panelDatosProductoLayout.setHorizontalGroup(
            panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDatosProductoLayout.createSequentialGroup()
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblIDProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtIdProducto, javax.swing.GroupLayout.DEFAULT_SIZE, 46, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtCodigoProducto)
                            .addComponent(lblCodigoProducto, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                        .addGap(3, 3, 3)
                        .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblCodigo6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtExistenciaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(txtDescripcionProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPrecioGs)
                    .addComponent(lblCodigo8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCodigo7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDatosProductoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblCodigo9, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtPrecioGs1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMoneda, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                    .addComponent(lblMoneda2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelDatosProductoLayout.setVerticalGroup(
            panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelDatosProductoLayout.createSequentialGroup()
                            .addComponent(lblCodigo7)
                            .addGap(1, 1, 1)
                            .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(panelDatosProductoLayout.createSequentialGroup()
                                    .addGap(46, 46, 46)
                                    .addComponent(lblCodigo8))
                                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtPrecioGs, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(1, 1, 1)
                            .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtPrecioGs1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblCodigo9, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panelDatosProductoLayout.createSequentialGroup()
                            .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(lblIDProducto)
                                .addComponent(lblCodigoProducto)
                                .addComponent(lblCodigo6))
                            .addGap(1, 1, 1)
                            .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                .addComponent(txtIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtCodigoProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtExistenciaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(3, 3, 3)
                            .addComponent(txtDescripcionProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(lblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        jpProductos.setBackground(new java.awt.Color(233, 255, 255));
        jpProductos.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnEliminar.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnEliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoEliminar.png"))); // NOI18N
        btnEliminar.setText("Quitar");
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEliminarActionPerformed(evt);
            }
        });

        btnAnadir.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnAnadir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        btnAnadir.setText("Agregar");
        btnAnadir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAnadirActionPerformed(evt);
            }
        });

        txtCantidadUnitaria.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtCantidadUnitaria.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCantidadUnitaria.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCantidadUnitaria.setEnabled(false);
        txtCantidadUnitaria.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaKeyTyped(evt);
            }
        });

        lblCantidad.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCantidad.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidad.setText("Cantidad (Unidades)*");

        cbMoneda.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Guaranies" }));
        cbMoneda.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbMonedaItemStateChanged(evt);
            }
        });

        lblCodigo10.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo10.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo10.setText("Moneda");

        lblDescuento.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblDescuento.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDescuento.setText("Descuento (Por unidad)");

        txtDescuento.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtDescuento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDescuento.setText("0");
        txtDescuento.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescuento.setEnabled(false);
        txtDescuento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescuentoActionPerformed(evt);
            }
        });
        txtDescuento.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtDescuentoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtDescuentoKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jpProductosLayout = new javax.swing.GroupLayout(jpProductos);
        jpProductos.setLayout(jpProductosLayout);
        jpProductosLayout.setHorizontalGroup(
            jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtCantidadUnitaria)
                    .addComponent(lblCantidad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDescuento)
                    .addComponent(lblDescuento, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpProductosLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(lblCodigo10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(cbMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAnadir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEliminar)
                .addContainerGap())
        );
        jpProductosLayout.setVerticalGroup(
            jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCantidad)
                    .addComponent(lblDescuento)
                    .addComponent(lblCodigo10))
                .addGap(1, 1, 1)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDescuento, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCantidadUnitaria, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnadir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEliminar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel2.setColorPrimario(new java.awt.Color(0, 153, 153));
        panel2.setColorSecundario(new java.awt.Color(233, 255, 255));

        labelMetric2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMetric2.setText("REGISTRAR VENTA");
        labelMetric2.setDireccionDeSombra(110);
        labelMetric2.setFont(new java.awt.Font("Cooper Black", 0, 24)); // NOI18N

        labelMetric1.setText("N° de venta:");
        labelMetric1.setDistanciaDeSombra(2);

        lblNumVenta.setText("00000001");
        lblNumVenta.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelMetric1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(lblNumVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNumVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelMetric1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(233, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblTituloTotalCompra1.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTituloTotalCompra1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloTotalCompra1.setText("TOTAL EFECTIVO");

        txtImporte.setFont(new java.awt.Font("sansserif", 1, 22)); // NOI18N
        txtImporte.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtImporte.setEnabled(false);
        txtImporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtImporteActionPerformed(evt);
            }
        });
        txtImporte.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtImporteKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtImporteKeyTyped(evt);
            }
        });

        txtVuelto.setEditable(false);
        txtVuelto.setBackground(new java.awt.Color(0, 153, 153));
        txtVuelto.setFont(new java.awt.Font("sansserif", 1, 22)); // NOI18N
        txtVuelto.setForeground(new java.awt.Color(255, 255, 255));
        txtVuelto.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtVuelto.setText("0");
        txtVuelto.setDisabledTextColor(new java.awt.Color(0, 51, 153));
        txtVuelto.setFocusable(false);
        txtVuelto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtVueltoActionPerformed(evt);
            }
        });

        lblTituloTotalCompra2.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTituloTotalCompra2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloTotalCompra2.setText("VUELTO");

        lblTotalMoneda.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTotalMoneda.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalMoneda.setText("Guaranies");
        lblTotalMoneda.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        txtTotalVenta.setEditable(false);
        txtTotalVenta.setBackground(new java.awt.Color(0, 0, 0));
        txtTotalVenta.setFont(new java.awt.Font("sansserif", 1, 22)); // NOI18N
        txtTotalVenta.setForeground(new java.awt.Color(0, 154, 0));
        txtTotalVenta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalVenta.setText("0");
        txtTotalVenta.setFocusable(false);
        txtTotalVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalVentaActionPerformed(evt);
            }
        });

        lblTituloTotalCompra.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTituloTotalCompra.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTituloTotalCompra.setText("TOTAL A PAGAR");

        lblTotalMoneda1.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTotalMoneda1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalMoneda1.setText("Guaranies");
        lblTotalMoneda1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        lblTotalMoneda2.setFont(new java.awt.Font("Doppio One", 1, 14)); // NOI18N
        lblTotalMoneda2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalMoneda2.setText("Guaranies");
        lblTotalMoneda2.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtImporte, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(lblTotalMoneda1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblTituloTotalCompra1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloTotalCompra2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtVuelto, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(lblTotalMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(120, 120, 120)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloTotalCompra)
                    .addComponent(txtTotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTotalMoneda)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblTituloTotalCompra1)
                    .addComponent(lblTituloTotalCompra2)
                    .addComponent(lblTituloTotalCompra))
                .addGap(1, 1, 1)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(txtImporte, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalMoneda1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtVuelto, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotalMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(233, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Detalle de la venta"));

        tbDetalleVenta.setAutoCreateRowSorter(true);
        tbDetalleVenta.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbDetalleVenta.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbDetalleVenta.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id del producto", "Descripcion", "Cantidad (Unidades)", "Descuento", "Precio neto", "SubTotal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbDetalleVenta.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbDetalleVenta.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbDetalleVenta.setGridColor(new java.awt.Color(0, 153, 204));
        tbDetalleVenta.setOpaque(false);
        tbDetalleVenta.setRowHeight(20);
        tbDetalleVenta.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbDetalleVenta.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbDetalleVenta.getTableHeader().setReorderingAllowed(false);
        tbDetalleVenta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetalleVentaMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbDetalleVentaMousePressed(evt);
            }
        });
        scPrincipal.setViewportView(tbDetalleVenta);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scPrincipal)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addGap(212, 212, 212)
                .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpDatosVenta, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelDatosProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpProductos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpDatosVenta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelDatosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 875, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 751, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("RegistrarCompra");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void GenerarNumVenta() {
        //Generar numero venta
        try {
            con = con.ObtenerRSSentencia("SELECT MAX(ven_numventa) AS numultimaventa FROM venta");
            String numultimaventa = null;
            while (con.getResultSet().next()) {
                numultimaventa = con.getResultSet().getString("numultimaventa");
            }

            if (numultimaventa == null) {
                numultimaventa = String.format("%8s", String.valueOf(1)).replace(' ', '0');
            } else {
                numultimaventa = String.format("%8s", String.valueOf((Integer.parseInt(numultimaventa) + 1))).replace(' ', '0');
            }
            lblNumVenta.setText(numultimaventa);
        } catch (SQLException e) {
            Logger.getLogger(RegistrarVenta.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void LimpiarProducto() {
        txtIdProducto.setText("");
        txtExistenciaActual.setText("");
        txtDescripcionProducto.setText("");
        lblImagen.setIcon(null);
        txtPrecioGs.setText("");
        txtCantidadUnitaria.setText("");
        txtCantidadUnitaria.setEnabled(false);
        txtDescuento.setText("");
        txtDescuento.setEnabled(false);
    }

    private void tbDetalleVentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetalleVentaMouseClicked

    }//GEN-LAST:event_tbDetalleVentaMouseClicked

    private void tbDetalleVentaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetalleVentaMousePressed
        if (tbDetalleVenta.isEnabled() == true) {
            btnEliminar.setEnabled(true);
        }
    }//GEN-LAST:event_tbDetalleVentaMousePressed

    private void btnAnadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirActionPerformed
        if (ComprobarCamposProducto() == true) {
            try {
                String idproducto = txtIdProducto.getText();
                String codigoproducto = txtCodigoProducto.getText();
                String descripcion = txtDescripcionProducto.getText();
                int cantidad = Integer.parseInt(txtCantidadUnitaria.getText());
                double preciobruto = 0.0;
                double descuento = metodostxt.StringAFormatoAmericano(txtDescuento.getText());
                descuento = metodostxt.DoubleCantidadDecimales(descuento, 2);
                double precioneto;

                if (descuento >= preciobruto) {
                    JOptionPane.showMessageDialog(this, "El descuento no puede ser mayor o igual al precio del producto", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                preciobruto = metodostxt.DoubleCantidadDecimales(preciobruto, 2);
                precioneto = preciobruto - descuento; //Se aplica el descuento
                precioneto = metodostxt.DoubleCantidadDecimales(precioneto, 2);

                double subtotal = cantidad * precioneto;
                subtotal = metodostxt.DoubleCantidadDecimales(subtotal, 2);
                tablemodelDetalleVenta.addRow(new Object[]{idproducto, codigoproducto, descripcion, cantidad, preciobruto, descuento, precioneto, subtotal});

                SumarSubtotal();

                if (tbDetalleVenta.getRowCount() > 0) {
                    txtImporte.setEnabled(true);
                    cbMoneda.setEnabled(false);
                } else {
                    txtImporte.setEnabled(false);
                    cbMoneda.setEnabled(true);
                }
                lblTotalMoneda.setText(cbMoneda.getSelectedItem().toString());
                lblTotalMoneda1.setText(cbMoneda.getSelectedItem().toString());
                lblTotalMoneda2.setText(cbMoneda.getSelectedItem().toString());

                LimpiarProducto();
                txtCodigoProducto.setText("");
                txtCantidadUnitaria.setText("");

                txtCodigoProducto.requestFocus();
            } catch (NumberFormatException e) {
                System.out.println("Error al añadir producto a la tabla " + e);
            }
        }
    }//GEN-LAST:event_btnAnadirActionPerformed

    private void SumarSubtotal() {
        //Suma la colmna subtotal
        double totalventa = metodos.SumarColumnaDouble(tbDetalleVenta, 8);
        totalventa = metodostxt.DoubleCantidadDecimales(totalventa, 2);
        String totalventaString = metodostxt.DoubleAFormatSudamerica(totalventa);
        txtTotalVenta.setText(totalventaString); //El 5 es la columna 5, comienza de 0
    }

    private void cbMonedaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbMonedaItemStateChanged
        lblTotalMoneda.setText(cbMoneda.getSelectedItem().toString());
        lblTotalMoneda1.setText(cbMoneda.getSelectedItem().toString());
        lblTotalMoneda2.setText(cbMoneda.getSelectedItem().toString());
    }//GEN-LAST:event_cbMonedaItemStateChanged

    private void txtCantidadUnitariaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaKeyTyped
        metodostxt.SoloNumeroEnteroKeyTyped(evt);
    }//GEN-LAST:event_txtCantidadUnitariaKeyTyped

    private void txtCantidadUnitariaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaKeyReleased
        metodostxt.TxtColorLabelKeyReleased(txtCantidadUnitaria, lblCantidad, colorGris);

        int cantidad = Integer.parseInt(txtCantidadUnitaria.getText());
        int stock = Integer.parseInt(txtExistenciaActual.getText());
        if (cantidad > stock) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock, el stock actual del producto es de " + stock);
            txtCantidadUnitaria.setForeground(Color.RED);
        } else {
            txtCantidadUnitaria.setForeground(new Color(0, 153, 51)); //Verde
        }
    }//GEN-LAST:event_txtCantidadUnitariaKeyReleased

    private void btnEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEliminarActionPerformed
        tablemodelDetalleVenta.removeRow(tbDetalleVenta.getSelectedRow());
        SumarSubtotal();

        if (tbDetalleVenta.getRowCount() <= 0) {
            txtImporte.setEnabled(false);
            txtImporte.setText("");
            txtVuelto.setText("");
            cbMoneda.setEnabled(true);
            btnEliminar.setEnabled(false);
            txtCodigoProducto.requestFocus();
        }
    }//GEN-LAST:event_btnEliminarActionPerformed

    private void txtCodigoProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProductoKeyTyped
        //Evitar que entre espacio
        if (evt.getKeyChar() == KeyEvent.VK_SPACE) {
            evt.consume();
        }
    }//GEN-LAST:event_txtCodigoProductoKeyTyped

    private void txtCodigoProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoProductoKeyReleased
        metodostxt.TxtColorLabelKeyReleased(txtCodigoProducto, lblCodigoProducto, colorGris);
        txtCodigoProducto.setForeground(Color.RED);
    }//GEN-LAST:event_txtCodigoProductoKeyReleased

    private boolean ConsultaProducto(String codigoproducto) {
        try {
            con = con.ObtenerRSSentencia("SELECT pro_codigo, pro_existencia, pro_descripcion, pro_codigo FROM producto WHERE pro_identificador = '" + codigoproducto + "'");
            if (con.getResultSet().next()) {
                txtCodigoProducto.setForeground(new Color(0, 153, 51)); //Verde

                txtIdProducto.setText(con.getResultSet().getString(1));
                txtExistenciaActual.setText(con.getResultSet().getString(2));
                txtDescripcionProducto.setText(con.getResultSet().getString(3));
                metodosimagen.LeerImagen(lblImagen, rutaFotoProducto + "image_" + con.getResultSet().getString(4) + "_A", rutaFotoDefault);

                double ultimopreciocompra = ObtenerUltimoPrecioCompra(txtIdProducto.getText());
                double porcganancia = PorcGanancia();
                String precioventaString = metodostxt.DoubleAFormatSudamerica(ultimopreciocompra + (ultimopreciocompra * (porcganancia / 100)));

                return true;
            } else {
                txtCodigoProducto.setForeground(Color.RED); //Rojo
                LimpiarProducto();
                return false;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al intentar obtener cambio " + e);
            System.out.println("Error al consultar producto: ");
        }
        con.DesconectarBasedeDatos();
        return false;
    }

    private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed

    }//GEN-LAST:event_btnBuscarClienteActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        RegistroNuevo();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnGuardarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnGuardarKeyPressed


    }//GEN-LAST:event_btnGuardarKeyPressed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        int confirmado = JOptionPane.showConfirmDialog(this, "¿Seguro que desea borrar todos los datos de la venta actual?", "Confirmación", JOptionPane.YES_OPTION);
        if (JOptionPane.YES_OPTION == confirmado) {
            Limpiar();
            txtCodigoProducto.setText("");
            LimpiarProducto();
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void txtDescuentoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescuentoKeyReleased
        txtDescuento.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtDescuento.getText()));
        metodostxt.TxtColorLabelKeyReleased(txtDescuento, lblDescuento, colorGris);
    }//GEN-LAST:event_txtDescuentoKeyReleased

    private void btnPantallaCompletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPantallaCompletaActionPerformed
        VistaCompleta vistacompleta = new VistaCompleta(rutaFotoProducto + "image_" + txtIdProducto.getText() + "_A", rutaFotoDefault);
        vistacompleta.setLocationRelativeTo(this);
        vistacompleta.setVisible(true);
    }//GEN-LAST:event_btnPantallaCompletaActionPerformed

    private void btnABMClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnABMClienteActionPerformed
        /*ABMCliente abmcliente = new ABMCliente(null, true);
        abmcliente.setVisible(true);*/
    }//GEN-LAST:event_btnABMClienteActionPerformed

    private void txtDescuentoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescuentoKeyTyped
        metodostxt.TxtCantidadCaracteresKeyTyped(txtDescuento, 11);
        metodostxt.SoloNumeroDecimalKeyTyped(evt, txtDescuento);
    }//GEN-LAST:event_txtDescuentoKeyTyped

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
        BuscadorProducto.setVisible(true);
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    private void txtVueltoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtVueltoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtVueltoActionPerformed

    private void txtImporteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtImporteKeyTyped
        metodostxt.TxtCantidadCaracteresKeyTyped(txtImporte, 11);

        metodostxt.SoloNumeroDecimalKeyTyped(evt, txtImporte);
    }//GEN-LAST:event_txtImporteKeyTyped

    private void CalcularVuelto() {
        double importe = metodostxt.StringAFormatoAmericano(txtImporte.getText());
        double totalventa = metodostxt.StringAFormatoAmericano(txtTotalVenta.getText());
        if (totalventa > importe) {
            txtImporte.setForeground(Color.RED);
            txtVuelto.setText("0");
        } else {
            txtImporte.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtImporte.getText()));
            double vuelto = importe - totalventa;
            vuelto = metodostxt.DoubleCantidadDecimales(vuelto, 2);
            txtVuelto.setText(metodostxt.DoubleAFormatSudamerica(vuelto));
            txtImporte.setForeground(new Color(0, 153, 51)); //Verde
        }
    }

    private void txtImporteKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtImporteKeyReleased
        if (txtImporte.getText().equals("") == false) {
            txtImporte.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtImporte.getText()));

            CalcularVuelto();
        }

    }//GEN-LAST:event_txtImporteKeyReleased

    private void txtTotalVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalVentaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalVentaActionPerformed

    private void txtImporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtImporteActionPerformed
        CalcularVuelto();
    }//GEN-LAST:event_txtImporteActionPerformed

    private void txtCodigoProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoProductoActionPerformed
        ConsultarProdCotiCant();
    }//GEN-LAST:event_txtCodigoProductoActionPerformed

    private void ConsultarProdCotiCant() throws HeadlessException {
        //Si se oprime ENTER o si el producto ya se encontro y se cambia el codigo de producto, volver a buscar
        if (ConsultaProducto(txtCodigoProducto.getText()) == true) {
            //Solicita la cantidad, y revisa que sea valido
            boolean esNumeroValido = false;
            while (esNumeroValido == false) {
                try {
                    Object cantidadString = JOptionPane.showInputDialog(this, "Ingrese la cantidad",
                            "Cantidad vendida", JOptionPane.INFORMATION_MESSAGE, null, null, "1");

                    int cantidad = Integer.parseInt(cantidadString.toString());
                    int stock = Integer.parseInt(txtExistenciaActual.getText());
                    if (cantidadString == null || cantidad <= 0 || cantidad > stock) {
                        if (null == cantidadString) {
                            System.out.println("Se canceló la operación");
                            return;
                        }

                        if (cantidad <= 0) {
                            JOptionPane.showMessageDialog(this, "La cantidad no puede ser menor o igual a 0");
                        }

                        if (cantidad > stock) {
                            JOptionPane.showMessageDialog(this, "No hay suficiente stock, el stock actual del producto es de " + stock);
                        }
                    } else {
                        txtCantidadUnitaria.setText(cantidadString.toString());
                        lblCantidad.setForeground(new Color(0, 153, 51)); //Verde
                        txtDescuento.setEnabled(true);
                        txtDescuento.requestFocus();
                        esNumeroValido = true;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número entero válido");
                    esNumeroValido = false;
                }
            }
        }
    }

    private void txtDescuentoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescuentoActionPerformed
        btnAnadir.doClick();
    }//GEN-LAST:event_txtDescuentoActionPerformed

    private void txtBuscarProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarProductoKeyReleased
        metodos.FiltroJTable(txtBuscarProducto.getText(), cbCampoBuscarProducto.getSelectedIndex(), tbProductosBuscadorProductos);

        if (tbProductosBuscadorProductos.getRowCount() == 1) {
            lbCantRegistrosProducto.setText(tbProductosBuscadorProductos.getRowCount() + " Registro encontrado");
        } else {
            lbCantRegistrosProducto.setText(tbProductosBuscadorProductos.getRowCount() + " Registros encontrados");
        }
    }//GEN-LAST:event_txtBuscarProductoKeyReleased

    private void tbProductosBuscadorProductosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbProductosBuscadorProductosMousePressed
        if (evt.getClickCount() == 2) {
            SeleccionarProducto();
        }
    }//GEN-LAST:event_tbProductosBuscadorProductosMousePressed

    private void tbProductosBuscadorProductosKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbProductosBuscadorProductosKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            SeleccionarProducto();
        }
    }//GEN-LAST:event_tbProductosBuscadorProductosKeyReleased

    private double ObtenerUltimoPrecioCompra(String codigoproducto) {
        con = con.ObtenerRSSentencia("SELECT compro_preciocompra AS mayorpreciocompra FROM compra_producto "
                + "WHERE compro_producto = '" + codigoproducto + "' ORDER BY compro_preciocompra DESC LIMIT 0,1");
        double precioventa = 0.0;
        try {
            //Obtener el mayor precio de compra de un producto
            if (con.getResultSet().next()) {
                precioventa = con.getResultSet().getDouble("mayorpreciocompra");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RegistrarVenta.class.getName()).log(Level.SEVERE, null, ex);
        }
        con.DesconectarBasedeDatos();
        return precioventa;
    }

    private double PorcGanancia() {
        con = con.ObtenerRSSentencia("SELECT conf_porcganancia FROM configuracion WHERE conf_codigo = '1'");;
        double porcganancia = 0.0;
        try {
            if (con.getResultSet().next()) {
                porcganancia = con.getResultSet().getDouble("conf_porcganancia");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RegistrarVenta.class.getName()).log(Level.SEVERE, null, ex);
        }
        con.DesconectarBasedeDatos();
        return porcganancia;
    }

    public void TablaAllProducto() {//Realiza la consulta de los productos que tenemos en la base de datos
        tabmodelProductos = (DefaultTableModel) tbProductosBuscadorProductos.getModel();
        tabmodelProductos.setRowCount(0);

        if (cbCampoBuscarProducto.getItemCount() == 0) {
            metodos.CargarTitlesaCombo(cbCampoBuscarProducto, tbProductosBuscadorProductos);
        }
        try {
            String sentencia = "CALL SP_ProductoConsulta()";
            con = con.ObtenerRSSentencia(sentencia);
            String identificador, descripcion, categoria, estado, obs;
            int codigo, existencia;

            while (con.getResultSet().next()) {
                codigo = con.getResultSet().getInt("pro_codigo");
                identificador = con.getResultSet().getString("pro_identificador");
                descripcion = con.getResultSet().getString("pro_descripcion");
                categoria = con.getResultSet().getString("cat_descripcion");
                existencia = con.getResultSet().getInt("pro_existencia");
                estado = con.getResultSet().getString("estado");
                obs = con.getResultSet().getString("pro_obs");

                tabmodelProductos.addRow(new Object[]{codigo, identificador, descripcion, categoria, existencia, estado, obs});
            }
            tbProductosBuscadorProductos.setModel(tabmodelProductos);
            //metodos.AnchuraColumna(tbProductosBuscadorProductos);

            if (tbProductosBuscadorProductos.getModel().getRowCount() == 1) {
                lbCantRegistrosProducto.setText(tbProductosBuscadorProductos.getModel().getRowCount() + " Registro encontrado");
            } else {
                lbCantRegistrosProducto.setText(tbProductosBuscadorProductos.getModel().getRowCount() + " Registros encontrados");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void SeleccionarProducto() {
        try {
            int codSelect, existenciaSelect;
            String descripcionSelect;
            codSelect = Integer.parseInt(tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 0) + "");
            descripcionSelect = tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 2) + "";
            existenciaSelect = Integer.parseInt(tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 4) + "");

            metodosimagen.LeerImagen(lblImagen, rutaFotoProducto + "image_" + codSelect + "_A", rutaFotoDefault);

            txtIdProducto.setText(codSelect + "");
            txtDescripcionProducto.setText(descripcionSelect);
            txtExistenciaActual.setText(existenciaSelect + "");
            txtCantidadUnitaria.setEnabled(true);
            txtCantidadUnitaria.requestFocus();
            BuscadorProducto.dispose();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog BuscadorProducto;
    private javax.swing.JButton btnABMCliente;
    private javax.swing.JButton btnAnadir;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnPantallaCompleta;
    private javax.swing.JComboBox cbCampoBuscarProducto;
    private static javax.swing.JComboBox<MetodosCombo> cbCliente;
    private javax.swing.JComboBox<String> cbMoneda;
    private javax.swing.JComboBox<String> cbTipoDocumento;
    private javax.swing.JComboBox<MetodosCombo> cbVendedor;
    private com.toedter.calendar.JDateChooser dcFechaVenta;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jpBotones;
    private javax.swing.JPanel jpDatosVenta;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpProductos;
    private org.edisoncor.gui.label.LabelMetric labelMetric1;
    private org.edisoncor.gui.label.LabelMetric labelMetric2;
    private javax.swing.JLabel lbCantRegistrosProducto;
    private javax.swing.JLabel lblBuscarCampoProducto;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCodigo10;
    private javax.swing.JLabel lblCodigo6;
    private javax.swing.JLabel lblCodigo7;
    private javax.swing.JLabel lblCodigo8;
    private javax.swing.JLabel lblCodigo9;
    private javax.swing.JLabel lblCodigoProducto;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblFechaRegistro;
    private javax.swing.JLabel lblIDProducto;
    private javax.swing.JLabel lblImagen;
    private javax.swing.JLabel lblMoneda;
    private javax.swing.JLabel lblMoneda2;
    private org.edisoncor.gui.label.LabelMetric lblNumVenta;
    private javax.swing.JLabel lblRucCedula;
    private javax.swing.JLabel lblRucCedula1;
    private javax.swing.JLabel lblRucCedula2;
    private javax.swing.JLabel lblTituloDescripcion;
    private javax.swing.JLabel lblTituloTotalCompra;
    private javax.swing.JLabel lblTituloTotalCompra1;
    private javax.swing.JLabel lblTituloTotalCompra2;
    private javax.swing.JLabel lblTotalMoneda;
    private javax.swing.JLabel lblTotalMoneda1;
    private javax.swing.JLabel lblTotalMoneda2;
    private org.edisoncor.gui.panel.Panel panel2;
    private org.edisoncor.gui.panel.Panel panel6;
    private javax.swing.JPanel panelDatosProducto;
    private javax.swing.JScrollPane scPrincipal;
    private javax.swing.JScrollPane scProductosBuscadorProductos;
    private javax.swing.JTable tbDetalleVenta;
    private javax.swing.JTable tbProductosBuscadorProductos;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCantidadUnitaria;
    private static javax.swing.JTextField txtCodigoProducto;
    private javax.swing.JTextField txtDescripcionProducto;
    private javax.swing.JTextField txtDescuento;
    private javax.swing.JTextField txtExistenciaActual;
    private javax.swing.JTextField txtIdProducto;
    private javax.swing.JTextField txtImporte;
    private javax.swing.JTextField txtPrecioGs;
    private javax.swing.JTextField txtPrecioGs1;
    private javax.swing.JTextField txtTotalVenta;
    private javax.swing.JTextField txtVuelto;
    // End of variables declaration//GEN-END:variables
}
