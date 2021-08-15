/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.venta;

import dao.DAO;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import helpers.Metodos;
import helpers.HelpersComboBox;
import helpers.HelpersImagen;
import helpers.HelpersTextField;
import helpers.VistaCompleta;
import static login.Login.codUsuario;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public final class RegistrarVentaVista extends javax.swing.JDialog {

    DAO con = new DAO();
    Metodos metodos = new Metodos();
    HelpersTextField metodostxt = new HelpersTextField();
    HelpersComboBox metodoscombo = new HelpersComboBox();
    HelpersImagen metodosimagen = new HelpersImagen();
    HelpersComboBox helpersComboBox = new HelpersComboBox();
    private final String rutaFotoProducto = "C:\\MAINUMBY\\productos\\imagenes\\";
    private final String rutaFotoDefault = "/src/images/IconoProductoSinFoto.png";
    private DefaultTableModel tabmodelDetalleVenta;
    private DefaultTableModel tabmodelProductos;
    private final Color colorAdvertencia = Color.RED;
    private final Color colorTitulos = Color.BLACK;
    private final int advertenciaDeStock = 5;

    public RegistrarVentaVista(java.awt.Frame parent, Boolean modal) {
        super(parent, modal);
        initComponents();

        GenerarNumVenta();
        tabmodelDetalleVenta = (DefaultTableModel) tbDetalleVenta.getModel();
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
                + "FROM funcionario WHERE fun_cargo = 1 ORDER BY fun_nombre", 1);
        cbVendedor.setSelectedIndex(0);

        metodoscombo.CargarComboConsulta(cbCliente, "SELECT cli_codigo, CONCAT(cli_nombre,' ', cli_apellido) AS nomape "
                + "FROM cliente ORDER BY cli_nombre", 1);
        cbCliente.setSelectedIndex(0);
    }

    public void RegistroNuevo() {
        //Registra la venta
        if (ComprobarCamposVenta() == true) {
            String numVenta, fechaVenta, horaVenta, numDoc, moneda, obs, deuda;
            int idvendedor, idcliente, tipoDoc, idusuario;
            Double importe, totalVenta, montoDeuda;

            numVenta = lblNumVenta.getText();

            DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
            fechaVenta = formatoFecha.format(dcFechaVenta.getDate());
            DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
            horaVenta = formatoHora.format(new Date());
            fechaVenta = fechaVenta + " " + horaVenta;

            idvendedor = metodoscombo.ObtenerIDSelectCombo(cbVendedor);
            idcliente = metodoscombo.ObtenerIDSelectCombo(cbCliente);
            tipoDoc = cbTipoDocumento.getSelectedIndex();
            numDoc = txtNumDoc.getText();
            moneda = cbMoneda.getSelectedItem().toString();
            importe = metodostxt.StringAFormatoAmericano(txtImporte.getText());
            totalVenta = metodostxt.StringAFormatoAmericano(txtTotalVenta.getText());
            idusuario = Integer.parseInt(codUsuario);
            obs = taObs.getText();

            if (importe < totalVenta) {
                deuda = "S";
                montoDeuda = totalVenta - importe;
            } else {
                deuda = "N";
                montoDeuda = 0.0;
            }

            int confirmado = JOptionPane.showConfirmDialog(this, "¿Esta seguro de crear esta nueva venta?", "Confirmación", JOptionPane.YES_OPTION);
            if (JOptionPane.YES_OPTION == confirmado) {
                try {
                    //Registrar nueva venta
                    String sentencia = "CALL SP_VentaAlta('" + numVenta + "','" + fechaVenta + "','" + idvendedor + "','" + idcliente
                            + "','" + tipoDoc + "','" + numDoc + "','" + moneda + "','" + importe + "','" + totalVenta
                            + "','" + idusuario + "','" + obs + "','" + deuda + "','" + montoDeuda + "')";
                    con.EjecutarABM(sentencia, false);

                    //Obtener el id de la venta
                    String idultimaventa = con.ObtenerUltimoID("SELECT MAX(ven_codigo) AS ultimoid FROM venta WHERE ven_numventa='" + numVenta + "'");

                    //Registra los productos de la venta                      
                    String idProducto;
                    int cantidadUnitaria;
                    double precioCompra;
                    double precioVentaBruto;
                    double descuento;

                    int cantfila = tbDetalleVenta.getRowCount();
                    for (int fila = 0; fila < cantfila; fila++) {
                        idProducto = tbDetalleVenta.getValueAt(fila, 0).toString();
                        cantidadUnitaria = Integer.parseInt(tbDetalleVenta.getValueAt(fila, 2).toString());
                        precioCompra = ObtenerUltimoPrecioCompra(idProducto);
                        descuento = Double.parseDouble(tbDetalleVenta.getValueAt(fila, 3).toString());
                        precioVentaBruto = metodostxt.StringAFormatoAmericano(tbDetalleVenta.getValueAt(fila, 4).toString());

                        //Se registran los productos de la venta
                        sentencia = "CALL SP_VentaDetalleAlta('" + idultimaventa + "','" + idProducto + "','"
                                + cantidadUnitaria + "','" + precioCompra + "','" + descuento + "','" + precioVentaBruto + "')";
                        con.EjecutarABM(sentencia, false);
                    }

                    Toolkit.getDefaultToolkit().beep(); //BEEP
                    JOptionPane.showMessageDialog(this, "Se agregó correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);

                    GenerarNumVenta();
                    Limpiar();
                } catch (HeadlessException ex) {
                    JOptionPane.showMessageDialog(this, "Ocurrió un Error " + ex.getMessage());
                    Logger.getLogger(RegistrarVentaVista.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void Limpiar() {
        cbVendedor.setSelectedIndex(-1);
        cbCliente.setSelectedItem("CLIENTE OCASIONAL");
        cbTipoDocumento.setSelectedItem("SIN ESPECIFICAR");
        txtNumDoc.setText("");
        dcFechaVenta.setDate(new Date());
        taObs.setText("");

        txtCodProducto.setText("");
        txtCodIdProducto.setText("");
        txtExistenciaActual.setText("");
        txtExistenciaActual.setForeground(Color.GRAY);
        txtDescripcionProducto.setText("");

        lblImagen.setIcon(null);

        txtCantidadUnitaria.setText("");
        cbMoneda.setSelectedItem("GUARANIES");
        txtTotalVenta.setText("0");
        txtImporte.setText("");
        txtVuelto.setText("");
        lblCantRegistrosDetalleVenta.setText("0 Item seleccionado");

        tabmodelDetalleVenta.setRowCount(0);
        tbDetalleVenta.setModel(tabmodelDetalleVenta);

        btnQuitar.setEnabled(false);
    }

    private boolean ComprobarCamposVenta() {
        double importe = metodostxt.StringAFormatoAmericano(txtImporte.getText());
        double totalventa = metodostxt.StringAFormatoAmericano(txtTotalVenta.getText());
        int idcliente = metodoscombo.ObtenerIDSelectCombo(cbCliente);

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

        if (txtImporte.getText().equals("") || importe < 0) {
            JOptionPane.showMessageDialog(this, "El importe no puede ser menor a 0 o vacio", "Advertencia", JOptionPane.WARNING_MESSAGE);
            txtImporte.requestFocus();
            return false;
        }

        if (idcliente == 1 && importe < totalventa || importe == 0) { //Si va a realizar una venta con deuda y se selecciono cliente ocasional
            JOptionPane.showMessageDialog(this, "Se va a realizar una venta con deuda, especifique el cliente", "Advertencia", JOptionPane.WARNING_MESSAGE);
            cbCliente.requestFocus();
            return false;
        }

        return true;
    }

    private boolean ComprobarCamposProducto() {
        if (metodostxt.CampoNoNulo(txtCodIdProducto, lblCodIdProducto) == false) {
            System.out.println("Validar CodigoProducto false");
            return false;
        } else {
            if (ConsultaProducto(txtCodIdProducto.getText()) == false) {
                JOptionPane.showMessageDialog(this, "El Codigo de producto ingresado no existe", "Advertencia", JOptionPane.WARNING_MESSAGE);
                txtCodIdProducto.requestFocus();
                return false;
            }
        }

        String codigoproducto = txtCodIdProducto.getText();
        String filaactual;
        for (int i = 0; i < tbDetalleVenta.getRowCount(); i++) {
            filaactual = tbDetalleVenta.getValueAt(i, 1).toString();
            if (codigoproducto.equals(filaactual) == true) {
                JOptionPane.showMessageDialog(this, "Este producto ya se encuentra cargado", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (metodostxt.CampoNoNulo(txtCantidadUnitaria, lblCantidad) == false) {
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
            lblCantidad.setForeground(colorAdvertencia);
            return false;
        }

        int stock = Integer.parseInt(txtExistenciaActual.getText());
        if (cantidad > stock) {
            JOptionPane.showMessageDialog(this, "No hay suficiente stock, el stock actual del producto es de " + stock);
            txtCantidadUnitaria.requestFocus();
            txtCantidadUnitaria.setForeground(colorAdvertencia);
            return false;
        } else {
            txtCantidadUnitaria.setForeground(colorTitulos);
        }

        /*double descuento = metodostxt.StringAFormatoAmericano(txtDescuento.getText());
        double subtotal = metodostxt.StringAFormatoAmericano(txtSubtotal.getText());
        if (descuento > subtotal) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "El descuento no puede ser mayor al subtotal del producto", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }*/

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
        txtNumDoc = new javax.swing.JTextField();
        lblNumDoc = new javax.swing.JLabel();
        lblFechaRegistro1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taObs = new javax.swing.JTextArea();
        panelDatosProducto = new javax.swing.JPanel();
        lblCodIdProducto = new javax.swing.JLabel();
        txtCodIdProducto = new javax.swing.JTextField();
        lblTituloDescripcion = new javax.swing.JLabel();
        txtDescripcionProducto = new javax.swing.JTextField();
        lblExistencia = new javax.swing.JLabel();
        txtExistenciaActual = new javax.swing.JTextField();
        lblCodigo7 = new javax.swing.JLabel();
        txtPrecioUnitarioVenta = new javax.swing.JTextField();
        lblMoneda = new javax.swing.JLabel();
        txtCodProducto = new javax.swing.JTextField();
        lblIDProducto = new javax.swing.JLabel();
        lblImagen = new javax.swing.JLabel();
        btnBuscarProducto = new javax.swing.JButton();
        lblCodigo8 = new javax.swing.JLabel();
        lblMoneda2 = new javax.swing.JLabel();
        txtPromocion = new javax.swing.JTextField();
        btnPantallaCompleta = new javax.swing.JButton();
        jpProductos = new javax.swing.JPanel();
        btnQuitar = new javax.swing.JButton();
        btnAnadir = new javax.swing.JButton();
        txtCantidadUnitaria = new javax.swing.JTextField();
        lblCantidad = new javax.swing.JLabel();
        cbMoneda = new javax.swing.JComboBox<>();
        lblCodigo10 = new javax.swing.JLabel();
        lblDescuento = new javax.swing.JLabel();
        txtDescuento = new javax.swing.JTextField();
        lblDescuento1 = new javax.swing.JLabel();
        txtSubtotal = new javax.swing.JTextField();
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
        lblCantRegistrosDetalleVenta = new javax.swing.JLabel();

        BuscadorProducto.setTitle("Buscador de apoderados");
        BuscadorProducto.setModal(true);
        BuscadorProducto.setSize(new java.awt.Dimension(746, 286));

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
                "Codigo", "Identificador", "Descripción", "Categoría", "Existencia", "Precio", "Precio promocional", "Estado", "Observación"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
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
                .addContainerGap(30, Short.MAX_VALUE))
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
                .addComponent(lbCantRegistrosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
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
        lblRucCedula.setText("Cliente*");
        lblRucCedula.setToolTipText("");

        btnBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoBuscar20.png"))); // NOI18N
        btnBuscarCliente.setToolTipText("Buscar cliente");
        btnBuscarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarClienteActionPerformed(evt);
            }
        });

        cbTipoDocumento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SIN ESPECIFICAR", "NOTA", "RECIBO", "FACTURA" }));

        lblRucCedula1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula1.setText("Tipo de documento");
        lblRucCedula1.setToolTipText("");

        lblFechaRegistro.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblFechaRegistro.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblFechaRegistro.setText("Fecha de venta*");
        lblFechaRegistro.setToolTipText("");

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

        txtNumDoc.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtNumDoc.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtNumDoc.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        lblNumDoc.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblNumDoc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNumDoc.setText("N° de documento");

        lblFechaRegistro1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblFechaRegistro1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblFechaRegistro1.setText("Observación");
        lblFechaRegistro1.setToolTipText("");

        taObs.setColumns(20);
        taObs.setRows(5);
        jScrollPane1.setViewportView(taObs);

        javax.swing.GroupLayout jpDatosVentaLayout = new javax.swing.GroupLayout(jpDatosVenta);
        jpDatosVenta.setLayout(jpDatosVentaLayout);
        jpDatosVentaLayout.setHorizontalGroup(
            jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpDatosVentaLayout.createSequentialGroup()
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                                .addComponent(lblRucCedula1, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                                .addComponent(cbTipoDocumento, 0, 222, Short.MAX_VALUE)
                                .addGap(21, 21, 21)))
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dcFechaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jpDatosVentaLayout.createSequentialGroup()
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbVendedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblRucCedula2, javax.swing.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                                .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 227, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(btnABMCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(18, 18, 18)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                    .addComponent(lblFechaRegistro1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpDatosVentaLayout.setVerticalGroup(
            jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosVentaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblRucCedula2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblFechaRegistro1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDatosVentaLayout.createSequentialGroup()
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(btnBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnABMCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbVendedor, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(lblRucCedula1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblNumDoc)
                            .addComponent(lblFechaRegistro, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addGroup(jpDatosVentaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(dcFechaVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelDatosProducto.setBackground(new java.awt.Color(233, 255, 255));
        panelDatosProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos del producto"));

        lblCodIdProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodIdProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodIdProducto.setText("Codigo Id del producto");

        txtCodIdProducto.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtCodIdProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCodIdProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodIdProductoActionPerformed(evt);
            }
        });
        txtCodIdProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCodIdProductoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCodIdProductoKeyTyped(evt);
            }
        });

        lblTituloDescripcion.setFont(new java.awt.Font("Berlin Sans FB Demi", 1, 12)); // NOI18N
        lblTituloDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloDescripcion.setText("Descripción del producto");
        lblTituloDescripcion.setToolTipText("");

        txtDescripcionProducto.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtDescripcionProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescripcionProducto.setEnabled(false);

        lblExistencia.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblExistencia.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblExistencia.setText("Stock actual");

        txtExistenciaActual.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtExistenciaActual.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtExistenciaActual.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtExistenciaActual.setEnabled(false);

        lblCodigo7.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo7.setText("Precio del producto (Unidad)");

        txtPrecioUnitarioVenta.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtPrecioUnitarioVenta.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPrecioUnitarioVenta.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtPrecioUnitarioVenta.setEnabled(false);

        lblMoneda.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblMoneda.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMoneda.setText("Guaraníes");

        txtCodProducto.setFont(new java.awt.Font("SansSerif", 0, 10)); // NOI18N
        txtCodProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCodProducto.setEnabled(false);

        lblIDProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblIDProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIDProducto.setText("Codigo");

        lblImagen.setFont(new java.awt.Font("Segoe UI Black", 1, 9)); // NOI18N
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

        txtPromocion.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtPromocion.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtPromocion.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtPromocion.setEnabled(false);

        btnPantallaCompleta.setBackground(new java.awt.Color(0, 255, 255));
        btnPantallaCompleta.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnPantallaCompleta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoPantallacompleta20.png"))); // NOI18N
        btnPantallaCompleta.setToolTipText("Ampliar vista de Imagen del producto");
        btnPantallaCompleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPantallaCompletaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelDatosProductoLayout = new javax.swing.GroupLayout(panelDatosProducto);
        panelDatosProducto.setLayout(panelDatosProductoLayout);
        panelDatosProductoLayout.setHorizontalGroup(
            panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDatosProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panelDatosProductoLayout.createSequentialGroup()
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblIDProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCodProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelDatosProductoLayout.createSequentialGroup()
                                .addComponent(txtCodIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblCodIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtExistenciaActual)
                            .addComponent(lblExistencia, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)))
                    .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtDescripcionProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 383, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(lblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtPromocion)
                    .addComponent(lblCodigo8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtPrecioUnitarioVenta)
                    .addComponent(lblCodigo7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(1, 1, 1)
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblMoneda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(48, 48, 48))
        );
        panelDatosProductoLayout.setVerticalGroup(
            panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelDatosProductoLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelDatosProductoLayout.createSequentialGroup()
                        .addComponent(lblCodigo7)
                        .addGap(1, 1, 1)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPrecioUnitarioVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCodigo8)
                        .addGap(1, 1, 1)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtPromocion, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelDatosProductoLayout.createSequentialGroup()
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(lblIDProducto)
                            .addComponent(lblCodIdProducto)
                            .addComponent(lblExistencia))
                        .addGap(1, 1, 1)
                        .addGroup(panelDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(txtCodProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCodIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtExistenciaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(txtDescripcionProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblImagen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jpProductos.setBackground(new java.awt.Color(233, 255, 255));
        jpProductos.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnQuitar.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnQuitar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoEliminar20.png"))); // NOI18N
        btnQuitar.setText("Quitar");
        btnQuitar.setEnabled(false);
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
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
        cbMoneda.setEnabled(false);
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
        lblDescuento.setText("Descuento");

        txtDescuento.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtDescuento.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtDescuento.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescuento.setEnabled(false);
        txtDescuento.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtDescuentoFocusLost(evt);
            }
        });
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

        lblDescuento1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblDescuento1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDescuento1.setText("Subtotal Neto");

        txtSubtotal.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtSubtotal.setText("0");
        txtSubtotal.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtSubtotal.setEnabled(false);
        txtSubtotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSubtotalActionPerformed(evt);
            }
        });
        txtSubtotal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtSubtotalKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtSubtotalKeyTyped(evt);
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtDescuento)
                    .addComponent(lblDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtSubtotal)
                    .addComponent(lblDescuento1, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbMoneda, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblCodigo10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 157, Short.MAX_VALUE)
                .addComponent(btnAnadir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuitar)
                .addGap(25, 25, 25))
        );
        jpProductosLayout.setVerticalGroup(
            jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jpProductosLayout.createSequentialGroup()
                        .addComponent(lblCantidad)
                        .addGap(1, 1, 1)
                        .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(cbMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCantidadUnitaria, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAnadir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnQuitar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jpProductosLayout.createSequentialGroup()
                        .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jpProductosLayout.createSequentialGroup()
                                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(lblDescuento1)
                                    .addComponent(lblCodigo10))
                                .addGap(1, 1, 1)
                                .addComponent(txtSubtotal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jpProductosLayout.createSequentialGroup()
                                .addComponent(lblDescuento)
                                .addGap(1, 1, 1)
                                .addComponent(txtDescuento, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(2, 2, 2)))
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
        lblTituloTotalCompra1.setText("TOTAL RECIBIDO");

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
        lblTituloTotalCompra.setText("TOTAL NETO A PAGAR");

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
                .addGap(43, 43, 43)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTituloTotalCompra2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(txtVuelto, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(lblTotalMoneda2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
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
                "Codigo", "Descripcion", "Cantidad", "Descuento", "Subtotal Bruto", "SubTotal Neto"
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

        lblCantRegistrosDetalleVenta.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        lblCantRegistrosDetalleVenta.setForeground(new java.awt.Color(153, 153, 0));
        lblCantRegistrosDetalleVenta.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblCantRegistrosDetalleVenta.setText("0 Item seleccionado");
        lblCantRegistrosDetalleVenta.setPreferredSize(new java.awt.Dimension(57, 25));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCantRegistrosDetalleVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(scPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 887, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(scPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCantRegistrosDetalleVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpPrincipalLayout.createSequentialGroup()
                        .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jpPrincipalLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(panelDatosProducto, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jpDatosVenta, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jpProductos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jpPrincipalLayout.createSequentialGroup()
                                .addGap(239, 239, 239)
                                .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpPrincipalLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpDatosVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panelDatosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jpBotones, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 911, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
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
            Logger.getLogger(RegistrarVentaVista.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    private void LimpiarProducto() {
        txtCodProducto.setText("");
        txtExistenciaActual.setText("");
        txtDescripcionProducto.setText("");
        lblImagen.setIcon(null);
        txtPrecioUnitarioVenta.setText("");
        txtPromocion.setText("");
        txtCantidadUnitaria.setText("");
        txtCantidadUnitaria.setEnabled(false);
        txtDescuento.setText("");
        txtDescuento.setEnabled(false);
        txtSubtotal.setText("");
    }

    private void tbDetalleVentaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetalleVentaMouseClicked

    }//GEN-LAST:event_tbDetalleVentaMouseClicked

    private void tbDetalleVentaMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetalleVentaMousePressed
        if (tbDetalleVenta.isEnabled() == true) {
            btnQuitar.setEnabled(true);
        }
    }//GEN-LAST:event_tbDetalleVentaMousePressed

    private void btnAnadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirActionPerformed
        if (ComprobarCamposProducto() == true) {
            if (txtDescuento.getText().equals("")) {
                txtDescuento.setText("0");
            }

            try {
                String codProducto, descripcion;
                int cantidad;
                double descuento, subTotalNeto, SubTotalBruto;

                codProducto = txtCodProducto.getText();
                descripcion = txtDescripcionProducto.getText();
                cantidad = Integer.parseInt(txtCantidadUnitaria.getText());
                descuento = metodostxt.StringAFormatoAmericano(txtDescuento.getText());
                descuento = metodostxt.arredondamientoDouble(descuento, 2);
                subTotalNeto = metodostxt.StringAFormatoAmericano(txtSubtotal.getText());
                subTotalNeto = metodostxt.arredondamientoDouble(subTotalNeto, 2);

                SubTotalBruto = descuento + subTotalNeto;

                tabmodelDetalleVenta.addRow(new Object[]{codProducto, descripcion, cantidad, descuento, SubTotalBruto, subTotalNeto});

                SumarSubtotalNeto();

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
                txtCodIdProducto.setText("");
                txtCantidadUnitaria.setText("");

                if (tbDetalleVenta.getRowCount() <= 1) {
                    lblCantRegistrosDetalleVenta.setText(tbDetalleVenta.getRowCount() + " Item seleccionado");
                } else {
                    lblCantRegistrosDetalleVenta.setText(tbDetalleVenta.getRowCount() + " Items seleccionados");
                }

                txtCodIdProducto.requestFocus();
            } catch (NumberFormatException e) {
                System.out.println("Error al añadir producto a la tabla " + e);
            }
        }
    }//GEN-LAST:event_btnAnadirActionPerformed

    private void SumarSubtotalNeto() {
        //Suma la colmna subtotal
        double totalventa = metodos.SumarColumnaDouble(tbDetalleVenta, 5); //El 6 es la columna 5, comienza de 0
        totalventa = metodostxt.arredondamientoDouble(totalventa, 2);
        txtTotalVenta.setText(metodostxt.DoubleAFormatSudamerica(totalventa));
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
        metodostxt.TxtColorLabelKeyReleased(txtCantidadUnitaria, lblCantidad, colorTitulos);

        CalculoSubtotal();
    }//GEN-LAST:event_txtCantidadUnitariaKeyReleased

    private void CalculoSubtotal() throws NumberFormatException {
        if (txtCantidadUnitaria.getText().equals("")) {
            txtSubtotal.setText("0");
            return;
        }
        int cantidad = Integer.parseInt(txtCantidadUnitaria.getText());
        double precioUnitario = metodostxt.StringAFormatoAmericano(txtPrecioUnitarioVenta.getText());
        double subtotal;
        if (txtPromocion.getText().equals("-")) {
            subtotal = cantidad * precioUnitario;
            txtSubtotal.setText(metodostxt.DoubleAFormatSudamerica(subtotal));
        } else {
            String[] promocion = (txtPromocion.getText()).replaceAll(" ", "").split("x");
            int cantidadPromocion = Integer.parseInt(promocion[0]);
            double precioPromocion = metodostxt.StringAFormatoAmericano(promocion[1]);
            int cantDivision = cantidad / cantidadPromocion;

            if (cantidad % cantidadPromocion == 0) { //Si es division exacta
                subtotal = cantDivision * precioPromocion;
                txtSubtotal.setText(metodostxt.DoubleAFormatSudamerica(subtotal));
            } else {
                if (cantidad < cantidadPromocion) { //Si cantidad es menor a la cantidad por promocion
                    txtSubtotal.setText(metodostxt.DoubleAFormatSudamerica((cantidad * precioUnitario)));
                }
                if (cantidad > cantidadPromocion) { //Si cantidad es mayor a la cantidad por promocion
                    cantDivision = cantidad;
                    while (cantDivision % cantidadPromocion != 0) {
                        cantDivision = cantDivision - 1;
                    }
                    subtotal = (cantDivision / cantidadPromocion) * precioPromocion;
                    cantDivision = cantidad - cantDivision; //El resto ej: total 7, ya se calculo 6 con precio promocion, faltaria 1 que seria a precio unitario
                    subtotal = subtotal + (cantDivision * precioUnitario);
                    txtSubtotal.setText(metodostxt.DoubleAFormatSudamerica(subtotal));
                }
            }
        }
    }

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        tabmodelDetalleVenta.removeRow(tbDetalleVenta.getSelectedRow());
        SumarSubtotalNeto();

        if (tbDetalleVenta.getRowCount() <= 0) {
            txtImporte.setEnabled(false);
            txtImporte.setText("");
            txtVuelto.setText("");
            cbMoneda.setEnabled(true);
            btnQuitar.setEnabled(false);
            txtCodIdProducto.requestFocus();
        }
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void txtCodIdProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodIdProductoKeyTyped
        //Evitar que entre espacio
        if (evt.getKeyChar() == KeyEvent.VK_SPACE) {
            evt.consume();
        }
    }//GEN-LAST:event_txtCodIdProductoKeyTyped

    private void txtCodIdProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodIdProductoKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            ConsultaProducto(txtCodIdProducto.getText());
        }
    }//GEN-LAST:event_txtCodIdProductoKeyReleased

    private boolean ConsultaProducto(String idProducto) {
        try {
            con = con.ObtenerRSSentencia("SELECT pro_codigo, pro_existencia, pro_descripcion FROM producto "
                    + "WHERE pro_identificador = '" + idProducto + "'");
            if (con.getResultSet().next()) {
                txtCodProducto.setText(con.getResultSet().getInt("pro_codigo") + "");

                txtExistenciaActual.setText(con.getResultSet().getInt("pro_existencia") + "");
                if (con.getResultSet().getInt("pro_existencia") <= advertenciaDeStock) {
                    lblExistencia.setForeground(colorAdvertencia);
                } else {
                    lblExistencia.setForeground(colorTitulos);
                }

                txtDescripcionProducto.setText(con.getResultSet().getString("pro_descripcion"));
                CalculoPrecioVentaPromocional(Integer.parseInt(txtCodProducto.getText()));

                metodosimagen.LeerImagen(lblImagen, rutaFotoProducto + "image_" + txtCodProducto.getText() + "_A", rutaFotoDefault);

                txtCantidadUnitaria.setEnabled(true);
                txtDescuento.setEnabled(true);
                txtCantidadUnitaria.requestFocus();

                lblCodIdProducto.setForeground(colorTitulos);
                lblExistencia.setForeground(colorTitulos);

                return true;
            } else {
                LimpiarProducto();
                txtCodIdProducto.setForeground(colorAdvertencia);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        return false;
    }

    private void btnBuscarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarClienteActionPerformed

    }//GEN-LAST:event_btnBuscarClienteActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        RegistroNuevo();
        
        TablaAllProducto(); //Actualiza el stock en busqueda producto
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnGuardarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnGuardarKeyPressed


    }//GEN-LAST:event_btnGuardarKeyPressed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        int confirmado = JOptionPane.showConfirmDialog(this, "¿Seguro que desea borrar todos los datos de la venta actual?", "Confirmación", JOptionPane.YES_OPTION);
        if (JOptionPane.YES_OPTION == confirmado) {
            Limpiar();
            txtCodIdProducto.setText("");
            LimpiarProducto();
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void txtDescuentoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDescuentoKeyReleased
        txtDescuento.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtDescuento.getText()));
        metodostxt.TxtColorLabelKeyReleased(txtDescuento, lblDescuento, colorTitulos);

        if (txtDescuento.getText().equals("") == false) {
            double descuento = metodostxt.StringAFormatoAmericano(txtDescuento.getText());
            CalculoSubtotal();
            double subtotal = metodostxt.StringAFormatoAmericano(txtSubtotal.getText());
            txtSubtotal.setText(metodostxt.DoubleAFormatSudamerica(subtotal - descuento));
        } else {
            CalculoSubtotal();
        }
    }//GEN-LAST:event_txtDescuentoKeyReleased

    private void btnPantallaCompletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPantallaCompletaActionPerformed
        VistaCompleta vistacompleta = new VistaCompleta(rutaFotoProducto + "image_" + txtCodProducto.getText() + "_A", rutaFotoDefault);
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
        BuscadorProducto.setLocationRelativeTo(this);
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
            vuelto = metodostxt.arredondamientoDouble(vuelto, 2);
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

    private void txtCodIdProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodIdProductoActionPerformed

    }//GEN-LAST:event_txtCodIdProductoActionPerformed

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

    private void txtSubtotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSubtotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSubtotalActionPerformed

    private void txtSubtotalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSubtotalKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSubtotalKeyReleased

    private void txtSubtotalKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSubtotalKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSubtotalKeyTyped

    private void txtDescuentoFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtDescuentoFocusLost

    }//GEN-LAST:event_txtDescuentoFocusLost

    private double ObtenerUltimoPrecioCompra(String codigoproducto) {
        con = con.ObtenerRSSentencia("SELECT compro_costounitario AS ultimacostounitariocompra FROM compra_producto "
                + "WHERE compro_producto = '" + codigoproducto + "' ORDER BY compro_costounitario DESC LIMIT 0,1");
        double ultimoCostoCompra = 0.0;
        try {
            //Obtener el mayor precio de compra de un producto
            if (con.getResultSet().next()) {
                ultimoCostoCompra = con.getResultSet().getDouble("ultimacostounitariocompra");
            }
        } catch (SQLException ex) {
            Logger.getLogger(RegistrarVentaVista.class.getName()).log(Level.SEVERE, null, ex);
        }
        con.DesconectarBasedeDatos();
        return ultimoCostoCompra;
    }

    public void TablaAllProducto() {//Realiza la consulta de los productos que tenemos en la base de datos
        tabmodelProductos = (DefaultTableModel) tbProductosBuscadorProductos.getModel();
        tabmodelProductos.setRowCount(0);

        if (cbCampoBuscarProducto.getItemCount() == 0) {
            helpersComboBox.CargarTitlesaCombo(cbCampoBuscarProducto, tbProductosBuscadorProductos);
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
            int idSelect, existenciaSelect;
            String descripcionSelect, identificadorSelect;
            idSelect = Integer.parseInt(tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 0) + "");
            identificadorSelect = tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 1) + "";
            descripcionSelect = tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 2) + "";
            existenciaSelect = Integer.parseInt(tbProductosBuscadorProductos.getValueAt(tbProductosBuscadorProductos.getSelectedRow(), 4) + "");

            metodosimagen.LeerImagen(lblImagen, rutaFotoProducto + "image_" + idSelect + "_A", rutaFotoDefault);

            txtCodProducto.setText(idSelect + "");
            txtCodIdProducto.setText(identificadorSelect);
            txtCodIdProducto.setForeground(colorTitulos);
            txtDescripcionProducto.setText(descripcionSelect);
            txtExistenciaActual.setText(existenciaSelect + "");

            CalculoPrecioVentaPromocional(idSelect);

            txtCantidadUnitaria.setEnabled(true);
            txtDescuento.setEnabled(true);
            txtCantidadUnitaria.requestFocus();
            BuscadorProducto.dispose();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void CalculoPrecioVentaPromocional(int codSelect) {
        try { //Calculo precio venta y promocion
            con = con.ObtenerRSSentencia("SELECT prom_cantidad, prom_precio, propreve_precioventa FROM producto_precioventa, promocion "
                    + "WHERE propreve_promocion=prom_codigo AND propreve_producto='" + codSelect + "' ORDER BY propreve_codigo DESC LIMIT 1");
            if (con.getResultSet().next()) {
                double precioVenta = con.getResultSet().getDouble("propreve_precioventa");
                int cantidadPromocion = con.getResultSet().getInt("prom_cantidad");
                double precioPromocion = con.getResultSet().getDouble("prom_precio");
                txtPrecioUnitarioVenta.setText(metodostxt.DoubleAFormatSudamerica(precioVenta));
                txtPromocion.setText(cantidadPromocion + " x " + metodostxt.DoubleAFormatSudamerica(precioPromocion));

                if (cantidadPromocion == 0 && precioPromocion == 0) {
                    txtPromocion.setText("-");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog BuscadorProducto;
    private javax.swing.JButton btnABMCliente;
    private javax.swing.JButton btnAnadir;
    private javax.swing.JButton btnBuscarCliente;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnPantallaCompleta;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JComboBox cbCampoBuscarProducto;
    private static javax.swing.JComboBox<helpers.HelpersComboBox> cbCliente;
    private javax.swing.JComboBox<String> cbMoneda;
    private javax.swing.JComboBox<String> cbTipoDocumento;
    private javax.swing.JComboBox<helpers.HelpersComboBox> cbVendedor;
    private com.toedter.calendar.JDateChooser dcFechaVenta;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jpBotones;
    private javax.swing.JPanel jpDatosVenta;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpProductos;
    private org.edisoncor.gui.label.LabelMetric labelMetric1;
    private org.edisoncor.gui.label.LabelMetric labelMetric2;
    private javax.swing.JLabel lbCantRegistrosProducto;
    private javax.swing.JLabel lblBuscarCampoProducto;
    private javax.swing.JLabel lblCantRegistrosDetalleVenta;
    private javax.swing.JLabel lblCantidad;
    private javax.swing.JLabel lblCodIdProducto;
    private javax.swing.JLabel lblCodigo10;
    private javax.swing.JLabel lblCodigo7;
    private javax.swing.JLabel lblCodigo8;
    private javax.swing.JLabel lblDescuento;
    private javax.swing.JLabel lblDescuento1;
    private javax.swing.JLabel lblExistencia;
    private javax.swing.JLabel lblFechaRegistro;
    private javax.swing.JLabel lblFechaRegistro1;
    private javax.swing.JLabel lblIDProducto;
    private javax.swing.JLabel lblImagen;
    private javax.swing.JLabel lblMoneda;
    private javax.swing.JLabel lblMoneda2;
    private javax.swing.JLabel lblNumDoc;
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
    private javax.swing.JTextArea taObs;
    private javax.swing.JTable tbDetalleVenta;
    private javax.swing.JTable tbProductosBuscadorProductos;
    private javax.swing.JTextField txtBuscarProducto;
    private javax.swing.JTextField txtCantidadUnitaria;
    private static javax.swing.JTextField txtCodIdProducto;
    private javax.swing.JTextField txtCodProducto;
    private javax.swing.JTextField txtDescripcionProducto;
    private javax.swing.JTextField txtDescuento;
    private javax.swing.JTextField txtExistenciaActual;
    private javax.swing.JTextField txtImporte;
    private javax.swing.JTextField txtNumDoc;
    private javax.swing.JTextField txtPrecioUnitarioVenta;
    private javax.swing.JTextField txtPromocion;
    private javax.swing.JTextField txtSubtotal;
    private javax.swing.JTextField txtTotalVenta;
    private javax.swing.JTextField txtVuelto;
    // End of variables declaration//GEN-END:variables
}
