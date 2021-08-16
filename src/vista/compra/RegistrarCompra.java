/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.compra;

import dao.DAO;
import vista.producto.ProductoVista;
import helpers.Metodos;
import helpers.HelpersComboBox;
import helpers.HelpersImagen;
import helpers.HelpersTable;
import helpers.HelpersTextField;
import helpers.HelpersUsuarioRol;
import helpers.VistaCompleta;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import static login.Login.codUsuario;


/*
 * @author Lic. Arnaldo Cantero
 */
public final class RegistrarCompra extends javax.swing.JDialog {

    private HelpersTextField metodostxt = new HelpersTextField();
    private DAO con = new DAO();
    private HelpersUsuarioRol helpersUsuarioRol = new HelpersUsuarioRol();
    private HelpersImagen metodosimagen = new HelpersImagen();
    private HelpersTable helpersTable = new HelpersTable();
    private DefaultTableModel tabmodelDetalleCompra;
    private DefaultTableModel tabmodelProductos;
    private final Color colorVerde = new Color(6, 147, 27);
    private final Color colorAdvertencia = new Color(206, 16, 45);
    private final Color colorTitulos = Color.BLACK;
    private final String rutaFotoProducto = "C:\\MAINUMBY\\productos\\imagenes\\";
    private final String rutaFotoDefault = "/src/images/IconoProductoSinFoto.png";
    private HelpersComboBox helpersComboBox = new HelpersComboBox();

    public RegistrarCompra(java.awt.Frame parent, Boolean modal) {
        super(parent, modal);
        initComponents();

        GenerarNumCompra();
        tabmodelDetalleCompra = (DefaultTableModel) tbDetalleCompra.getModel();

        CargarComboBoxes();

        //Obtener fecha actual
        dcFechaCompra.setDate(
                new Date());

        //Permiso Roles de usuario
        String permisos = helpersUsuarioRol.PermisoRol(codUsuario, "COMPRA");

        btnGuardar.setVisible(permisos.contains("A"));
    }

    //--------------------------METODOS----------------------------//
    public void CargarComboBoxes() {
        //Carga los combobox con las consultas
        helpersComboBox.CargarComboConsulta(cbTipoDocumento, "SELECT tidoc_codigo, tidoc_descripcion FROM tipo_documento ORDER BY tidoc_codigo", 1);
        helpersComboBox.CargarComboConsulta(cbProveedor, "SELECT prov_codigo, prov_descripcion FROM proveedor ORDER BY prov_descripcion", 1);
    }

    public void RegistroNuevo() {
        //Registra la compra
        int cantidadDetalleCompra = tbDetalleCompra.getModel().getRowCount();
        if (cantidadDetalleCompra <= 0) {
            JOptionPane.showMessageDialog(this, "No se cargó ningún producto", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String numcompra, numdoc, fechaCompra, horaCompra, moneda, obs;
        double costoTotalCompra;
        int idtipodoc, idproveedor, idusuario;
        if (ComprobarCamposCompra() == true) {
            int confirmado = JOptionPane.showConfirmDialog(this, "¿Esta seguro crear esta nueva compra?", "Confirmación", JOptionPane.YES_OPTION);
            if (JOptionPane.YES_OPTION == confirmado) {
                numcompra = lblNumCompra.getText();

                DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
                fechaCompra = formatoFecha.format(dcFechaCompra.getDate());
                DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
                horaCompra = formatoHora.format(new Date());
                fechaCompra = fechaCompra + " " + horaCompra;

                idtipodoc = helpersComboBox.ObtenerIDSelectCombo(cbTipoDocumento);
                numdoc = txtNumDoc.getText();
                idproveedor = helpersComboBox.ObtenerIDSelectCombo(cbProveedor);
                moneda = cbMoneda.getSelectedItem().toString();
                costoTotalCompra = metodostxt.StringAFormatoAmericano(txtTotalCompra.getText());
                idusuario = Integer.parseInt(codUsuario);
                obs = taObs.getText();

                try {
                    //Registrar nueva compra
                    String sentencia = "CALL SP_CompraAlta('" + numcompra + "','" + fechaCompra + "','" + idtipodoc + "','" + numdoc + "','" + idproveedor + "','"
                            + moneda + "','" + costoTotalCompra + "','" + idusuario + "','" + obs + "')";
                    con.EjecutarABM(sentencia, false);

                    //Obtener el id de la compra
                    String ultimoIdCompra = con.ObtenerUltimoID("SELECT MAX(com_codigo) AS ultimoidcompra FROM compra WHERE com_numcompra='" + numcompra + "'");

                    //Registra los productos de la compra                      
                    String idProducto, stringFechaVencimiento;
                    int cantidadAdquirida, cantfila;
                    double costoUnitario;
                    Date fechaVencimiento;

                    cantfila = tbDetalleCompra.getRowCount();
                    for (int fila = 0; fila < cantfila; fila++) {
                        idProducto = tbDetalleCompra.getValueAt(fila, 0).toString();
                        cantidadAdquirida = Integer.parseInt(tbDetalleCompra.getValueAt(fila, 2).toString());
                        costoUnitario = Double.parseDouble(tbDetalleCompra.getValueAt(fila, 3).toString());

                        stringFechaVencimiento = tbDetalleCompra.getValueAt(fila, 5).toString();
                        if (stringFechaVencimiento.equals("") == false) {
                            try {
                                SimpleDateFormat formatosuda = new SimpleDateFormat("dd/MM/yyyy");
                                fechaVencimiento = formatosuda.parse(stringFechaVencimiento);
                                SimpleDateFormat formatoamericano = new SimpleDateFormat("yyyy-MM-dd");
                                stringFechaVencimiento = "'" + formatoamericano.format(fechaVencimiento) + "'";
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            stringFechaVencimiento = "null";
                        }

                        //Se registran los productos de la compra
                        sentencia = "CALL SP_CompraDetalleAlta('" + ultimoIdCompra + "','" + idProducto + "','" + cantidadAdquirida
                                + "','" + costoUnitario + "'," + stringFechaVencimiento + ")";
                        con.EjecutarABM(sentencia, false);
                    }
                    Toolkit.getDefaultToolkit().beep(); //BEEP
                    JOptionPane.showMessageDialog(this, "Se agregó correctamente", "Información", JOptionPane.INFORMATION_MESSAGE);
                    GenerarNumCompra();
                    Limpiar();
                } catch (HeadlessException ex) {
                    JOptionPane.showMessageDialog(this, "Ocurrió un Error " + ex.getMessage());
                    Logger.getLogger(RegistrarCompra.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void Limpiar() {
        dcFechaCompra.setDate(new Date());
        cbTipoDocumento.setSelectedItem("SIN ESPECIFICAR");
        txtNumDoc.setText("");
        helpersComboBox.SetSelectedCodigoItem(cbProveedor, -1);
        taObs.setText("");

        txtIdProducto.setText("");
        txtIdentificadorProducto.setText("");
        txtExistenciaActual.setText("");
        txtExistenciaActual.setForeground(Color.GRAY);
        txtDescripcionProducto.setText("");

        lblImagen.setIcon(null);

        txtCantidadUnitaria.setText("");
        txtCostoUnitario.setText("");
        cbMoneda.setSelectedItem("GUARANIES");
        txtTotalCompra.setText("0");

        tabmodelDetalleCompra.setRowCount(0);
        tbDetalleCompra.setModel(tabmodelDetalleCompra);

        btnQuitar.setEnabled(false);
    }

    public boolean ComprobarCamposCompra() {
        if (cbTipoDocumento.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un tipo de documento", "Error", JOptionPane.ERROR_MESSAGE);
            cbTipoDocumento.requestFocus();
            return false;
        }

        if (cbProveedor.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un proveedor", "Error", JOptionPane.ERROR_MESSAGE);
            cbProveedor.requestFocus();
            return false;
        }

        if (dcFechaCompra.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Complete la fecha de compra", "Error", JOptionPane.ERROR_MESSAGE);
            dcFechaCompra.requestFocus();
            return false;
        }

        return true;
    }

    public boolean ComprobarCamposProducto() {
        if (txtIdProducto.getText().equals("") == true) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto", "Error", JOptionPane.ERROR_MESSAGE);
            txtIdentificadorProducto.requestFocus();
            return false;
        }
        String filaActual;

        for (int i = 0; i < tbDetalleCompra.getRowCount(); i++) {
            filaActual = tbDetalleCompra.getValueAt(i, 1).toString();
            if (txtIdProducto.getText().equals(filaActual) == true) {
                JOptionPane.showMessageDialog(this, "Este producto ya se encuentra cargado", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        if (metodostxt.CampoNoNulo(txtCantidadUnitaria, lblCantidadAdquirida) == false) {
            System.out.println("Validar Cantidad adquirida false");
            return false;
        }

        if (metodostxt.ValidarDoubleTXT(txtCostoUnitario, lblCostoUnitario) == false) {
            System.out.println("Validar Double Precio unitario false");
            return false;
        }
        return true;
    }

//--------------------------iniComponent()No tocar----------------------------//
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CalculoPacks = new javax.swing.JDialog();
        panel6 = new org.edisoncor.gui.panel.Panel();
        btnAgregar = new javax.swing.JButton();
        lblPrecioUnitario1 = new javax.swing.JLabel();
        lblCantidadAdquirida1 = new javax.swing.JLabel();
        lblCantidadAdquirida2 = new javax.swing.JLabel();
        lblCantidadAdquirida3 = new javax.swing.JLabel();
        spUnidadPorPackCalculoPacks = new javax.swing.JSpinner();
        txtCantidadUnitariaCalculoPacks = new javax.swing.JTextField();
        lblCantidadAdquirida4 = new javax.swing.JLabel();
        lblPrecioUnitario3 = new javax.swing.JLabel();
        spCantidadPackCalculoPacks = new javax.swing.JSpinner();
        txtCostoPackCalculoPacks = new javax.swing.JTextField();
        txtCostoUnitarioCalculoPacks = new javax.swing.JTextField();
        lblPrecioUnitario4 = new javax.swing.JLabel();
        lblCantidadAdquirida5 = new javax.swing.JLabel();
        txtCostoTotalPacksCalculoPacks = new javax.swing.JTextField();
        lblPrecioUnitario5 = new javax.swing.JLabel();
        labelMetric3 = new org.edisoncor.gui.label.LabelMetric();
        BuscadorProductos = new javax.swing.JDialog();
        panel7 = new org.edisoncor.gui.panel.Panel();
        jLabel12 = new javax.swing.JLabel();
        txtBuscarApoderado = new javax.swing.JTextField();
        lblBuscarCampoApoderado = new javax.swing.JLabel();
        cbCampoBuscarBuscadorProductos = new javax.swing.JComboBox();
        scProductosBuscadorProductos = new javax.swing.JScrollPane();
        tbProductosBuscadorProductos = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        lbCantRegistrosBuscadorProductos = new javax.swing.JLabel();
        jpPrincipal = new javax.swing.JPanel();
        jpDatosCompra = new javax.swing.JPanel();
        lblRucCedula = new javax.swing.JLabel();
        cbProveedor = new javax.swing.JComboBox<>();
        dcFechaCompra = new com.toedter.calendar.JDateChooser();
        lblFechaCompra = new javax.swing.JLabel();
        txtNumDoc = new javax.swing.JTextField();
        lblIDProducto1 = new javax.swing.JLabel();
        btnProveedor1 = new javax.swing.JButton();
        cbTipoDocumento = new javax.swing.JComboBox<>();
        lblRucCedula1 = new javax.swing.JLabel();
        lblIDProducto2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taObs = new javax.swing.JTextArea();
        jpDatosProducto = new javax.swing.JPanel();
        lblCodigoProducto = new javax.swing.JLabel();
        txtIdentificadorProducto = new javax.swing.JTextField();
        lblTituloDescripcion = new javax.swing.JLabel();
        txtDescripcionProducto = new javax.swing.JTextField();
        lblCodigo6 = new javax.swing.JLabel();
        txtExistenciaActual = new javax.swing.JTextField();
        txtIdProducto = new javax.swing.JTextField();
        lblIDProducto = new javax.swing.JLabel();
        btnBuscarProducto = new javax.swing.JButton();
        btnPantallaCompleta = new javax.swing.JButton();
        lblImagen = new javax.swing.JLabel();
        btnABMProducto = new javax.swing.JButton();
        dcFechaVencimiento = new com.toedter.calendar.JDateChooser();
        lblTituloDescripcion1 = new javax.swing.JLabel();
        jpProductos = new javax.swing.JPanel();
        btnQuitar = new javax.swing.JButton();
        btnAnadir = new javax.swing.JButton();
        txtCostoUnitario = new javax.swing.JTextField();
        lblCostoUnitario = new javax.swing.JLabel();
        txtCantidadUnitaria = new javax.swing.JTextField();
        lblCantidadAdquirida = new javax.swing.JLabel();
        cbMoneda = new javax.swing.JComboBox<>();
        lblPrecioUnitario2 = new javax.swing.JLabel();
        btnPack = new javax.swing.JButton();
        panel2 = new org.edisoncor.gui.panel.Panel();
        lblmTitulo = new org.edisoncor.gui.label.LabelMetric();
        lblNumCompra = new org.edisoncor.gui.label.LabelMetric();
        labelMetric1 = new org.edisoncor.gui.label.LabelMetric();
        jpBotones1 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        lblTituloTotalCompra1 = new javax.swing.JLabel();
        txtTotalCompra = new javax.swing.JTextField();
        lblTotalMoneda = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        scDetalleCompra = new javax.swing.JScrollPane();
        tbDetalleCompra = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };

        CalculoPacks.setTitle("Calculo de Pack");
        CalculoPacks.setModal(true);
        CalculoPacks.setPreferredSize(new java.awt.Dimension(700, 350));
        CalculoPacks.setSize(new java.awt.Dimension(700, 350));

        panel6.setPreferredSize(new java.awt.Dimension(656, 350));

        btnAgregar.setBackground(new java.awt.Color(0, 153, 255));
        btnAgregar.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        btnAgregar.setForeground(new java.awt.Color(255, 255, 255));
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoOk20.png"))); // NOI18N
        btnAgregar.setText("AGREGAR");
        btnAgregar.setToolTipText("Inserta el nuevo registro");
        btnAgregar.setPreferredSize(new java.awt.Dimension(128, 36));
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });
        btnAgregar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnAgregarKeyPressed(evt);
            }
        });

        lblPrecioUnitario1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblPrecioUnitario1.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecioUnitario1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPrecioUnitario1.setText("Costo (Unidad)");

        lblCantidadAdquirida1.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblCantidadAdquirida1.setForeground(new java.awt.Color(255, 255, 255));
        lblCantidadAdquirida1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida1.setText("Cantidad (Unidades)");

        lblCantidadAdquirida2.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblCantidadAdquirida2.setForeground(new java.awt.Color(255, 255, 255));
        lblCantidadAdquirida2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida2.setText("Cantidad de Packs");

        lblCantidadAdquirida3.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblCantidadAdquirida3.setForeground(new java.awt.Color(255, 255, 255));
        lblCantidadAdquirida3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida3.setText("Unidades por Pack");

        spUnidadPorPackCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        spUnidadPorPackCalculoPacks.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spUnidadPorPackCalculoPacksStateChanged(evt);
            }
        });

        txtCantidadUnitariaCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtCantidadUnitariaCalculoPacks.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCantidadUnitariaCalculoPacks.setText("0");
        txtCantidadUnitariaCalculoPacks.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCantidadUnitariaCalculoPacks.setEnabled(false);
        txtCantidadUnitariaCalculoPacks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaCalculoPacksKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaCalculoPacksKeyTyped(evt);
            }
        });

        lblCantidadAdquirida4.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblCantidadAdquirida4.setForeground(new java.awt.Color(255, 255, 255));
        lblCantidadAdquirida4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida4.setText("Costo por Pack");
        lblCantidadAdquirida4.setToolTipText("");

        lblPrecioUnitario3.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblPrecioUnitario3.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecioUnitario3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPrecioUnitario3.setText("Gs.");

        spCantidadPackCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        spCantidadPackCalculoPacks.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spCantidadPackCalculoPacksStateChanged(evt);
            }
        });

        txtCostoPackCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtCostoPackCalculoPacks.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCostoPackCalculoPacks.setText("0");
        txtCostoPackCalculoPacks.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCostoPackCalculoPacks.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCostoPackCalculoPacksFocusLost(evt);
            }
        });
        txtCostoPackCalculoPacks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCostoPackCalculoPacksKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCostoPackCalculoPacksKeyTyped(evt);
            }
        });

        txtCostoUnitarioCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtCostoUnitarioCalculoPacks.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCostoUnitarioCalculoPacks.setText("0");
        txtCostoUnitarioCalculoPacks.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCostoUnitarioCalculoPacks.setEnabled(false);
        txtCostoUnitarioCalculoPacks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCostoUnitarioCalculoPacksKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCostoUnitarioCalculoPacksKeyTyped(evt);
            }
        });

        lblPrecioUnitario4.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblPrecioUnitario4.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecioUnitario4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPrecioUnitario4.setText("Gs.");

        lblCantidadAdquirida5.setFont(new java.awt.Font("SansSerif", 1, 12)); // NOI18N
        lblCantidadAdquirida5.setForeground(new java.awt.Color(255, 255, 255));
        lblCantidadAdquirida5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida5.setText("Costo total de Packs");
        lblCantidadAdquirida5.setToolTipText("");

        txtCostoTotalPacksCalculoPacks.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        txtCostoTotalPacksCalculoPacks.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCostoTotalPacksCalculoPacks.setText("0");
        txtCostoTotalPacksCalculoPacks.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCostoTotalPacksCalculoPacks.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCostoTotalPacksCalculoPacksFocusLost(evt);
            }
        });
        txtCostoTotalPacksCalculoPacks.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCostoTotalPacksCalculoPacksKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCostoTotalPacksCalculoPacksKeyTyped(evt);
            }
        });

        lblPrecioUnitario5.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        lblPrecioUnitario5.setForeground(new java.awt.Color(255, 255, 255));
        lblPrecioUnitario5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblPrecioUnitario5.setText("Gs.");

        labelMetric3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMetric3.setText("CALCULO DE PACKS");
        labelMetric3.setDireccionDeSombra(110);
        labelMetric3.setFont(new java.awt.Font("Cooper Black", 0, 28)); // NOI18N

        javax.swing.GroupLayout panel6Layout = new javax.swing.GroupLayout(panel6);
        panel6.setLayout(panel6Layout);
        panel6Layout.setHorizontalGroup(
            panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel6Layout.createSequentialGroup()
                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelMetric3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panel6Layout.createSequentialGroup()
                                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblCantidadAdquirida2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(spCantidadPackCalculoPacks))
                                .addGap(18, 18, 18)
                                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtCantidadUnitariaCalculoPacks)
                                    .addComponent(lblCantidadAdquirida1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblCantidadAdquirida3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(spUnidadPorPackCalculoPacks))
                                .addGap(18, 18, 18)
                                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblPrecioUnitario1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(panel6Layout.createSequentialGroup()
                                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtCostoPackCalculoPacks)
                                            .addComponent(lblCantidadAdquirida4, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblPrecioUnitario3)
                                        .addGap(18, 18, 18)
                                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblCantidadAdquirida5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(txtCostoTotalPacksCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblPrecioUnitario5))
                                    .addGroup(panel6Layout.createSequentialGroup()
                                        .addComponent(txtCostoUnitarioCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblPrecioUnitario4))))))
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        panel6Layout.setVerticalGroup(
            panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel6Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(labelMetric3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(lblCantidadAdquirida2)
                            .addComponent(lblCantidadAdquirida3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(spUnidadPorPackCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spCantidadPackCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(33, 33, 33)
                        .addComponent(lblCantidadAdquirida1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCantidadUnitariaCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panel6Layout.createSequentialGroup()
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(lblCantidadAdquirida4)
                            .addComponent(lblCantidadAdquirida5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCostoPackCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPrecioUnitario3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtCostoTotalPacksCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblPrecioUnitario5, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(33, 33, 33)
                        .addComponent(lblPrecioUnitario1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtCostoUnitarioCalculoPacks, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPrecioUnitario4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(32, 32, 32)
                .addComponent(btnAgregar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout CalculoPacksLayout = new javax.swing.GroupLayout(CalculoPacks.getContentPane());
        CalculoPacks.getContentPane().setLayout(CalculoPacksLayout);
        CalculoPacksLayout.setHorizontalGroup(
            CalculoPacksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel6, javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
        );
        CalculoPacksLayout.setVerticalGroup(
            CalculoPacksLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        CalculoPacks.getAccessibleContext().setAccessibleName("Calculo de Packs");

        BuscadorProductos.setTitle("Buscador de productos");
        BuscadorProductos.setModal(true);
        BuscadorProductos.setSize(new java.awt.Dimension(760, 310));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoBuscar40.png"))); // NOI18N
        jLabel12.setText("  BUSCAR ");

        txtBuscarApoderado.setFont(new java.awt.Font("Tahoma", 1, 17)); // NOI18N
        txtBuscarApoderado.setForeground(new java.awt.Color(0, 153, 153));
        txtBuscarApoderado.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        txtBuscarApoderado.setCaretColor(new java.awt.Color(0, 204, 204));
        txtBuscarApoderado.setDisabledTextColor(new java.awt.Color(0, 204, 204));
        txtBuscarApoderado.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarApoderadoKeyReleased(evt);
            }
        });

        lblBuscarCampoApoderado.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblBuscarCampoApoderado.setForeground(new java.awt.Color(255, 255, 255));
        lblBuscarCampoApoderado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBuscarCampoApoderado.setText("Buscar por:");

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

        lbCantRegistrosBuscadorProductos.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lbCantRegistrosBuscadorProductos.setForeground(new java.awt.Color(153, 153, 0));
        lbCantRegistrosBuscadorProductos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbCantRegistrosBuscadorProductos.setText("0 Registros encontrados");
        lbCantRegistrosBuscadorProductos.setPreferredSize(new java.awt.Dimension(57, 25));

        javax.swing.GroupLayout panel7Layout = new javax.swing.GroupLayout(panel7);
        panel7.setLayout(panel7Layout);
        panel7Layout.setHorizontalGroup(
            panel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel7Layout.createSequentialGroup()
                .addGroup(panel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(panel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbCantRegistrosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 359, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel7Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(panel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(panel7Layout.createSequentialGroup()
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtBuscarApoderado, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(lblBuscarCampoApoderado)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbCampoBuscarBuscadorProductos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(scProductosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(14, 14, 14))
        );
        panel7Layout.setVerticalGroup(
            panel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel7Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(panel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(cbCampoBuscarBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBuscarCampoApoderado, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtBuscarApoderado, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(scProductosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lbCantRegistrosBuscadorProductos, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout BuscadorProductosLayout = new javax.swing.GroupLayout(BuscadorProductos.getContentPane());
        BuscadorProductos.getContentPane().setLayout(BuscadorProductosLayout);
        BuscadorProductosLayout.setHorizontalGroup(
            BuscadorProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        BuscadorProductosLayout.setVerticalGroup(
            BuscadorProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setTitle("Ventana Registrar Compra");
        setBackground(new java.awt.Color(45, 62, 80));
        setResizable(false);

        jpPrincipal.setBackground(new java.awt.Color(233, 255, 255));
        jpPrincipal.setPreferredSize(new java.awt.Dimension(1580, 478));

        jpDatosCompra.setBackground(new java.awt.Color(233, 255, 255));
        jpDatosCompra.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos de la compra"));

        lblRucCedula.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula.setText("Proveedor*");
        lblRucCedula.setToolTipText("");

        dcFechaCompra.setMaxSelectableDate(new java.util.Date(4102455600000L));
        dcFechaCompra.setMinSelectableDate(new java.util.Date(631162800000L));

        lblFechaCompra.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblFechaCompra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblFechaCompra.setText("Fecha de compra*");
        lblFechaCompra.setToolTipText("");

        txtNumDoc.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtNumDoc.setDisabledTextColor(new java.awt.Color(0, 0, 0));

        lblIDProducto1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblIDProducto1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIDProducto1.setText("N° del documento");

        btnProveedor1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        btnProveedor1.setToolTipText("Nuevo proveedor");
        btnProveedor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProveedor1ActionPerformed(evt);
            }
        });

        lblRucCedula1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblRucCedula1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblRucCedula1.setText("Tipo de documento*");
        lblRucCedula1.setToolTipText("");

        lblIDProducto2.setFont(new java.awt.Font("Berlin Sans FB", 1, 12)); // NOI18N
        lblIDProducto2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIDProducto2.setText("Observación");

        taObs.setColumns(20);
        taObs.setRows(5);
        jScrollPane1.setViewportView(taObs);

        javax.swing.GroupLayout jpDatosCompraLayout = new javax.swing.GroupLayout(jpDatosCompra);
        jpDatosCompra.setLayout(jpDatosCompraLayout);
        jpDatosCompraLayout.setHorizontalGroup(
            jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosCompraLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIDProducto2, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(jpDatosCompraLayout.createSequentialGroup()
                            .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtNumDoc)
                                .addComponent(lblIDProducto1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(cbTipoDocumento, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblRucCedula1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(jpDatosCompraLayout.createSequentialGroup()
                                    .addComponent(cbProveedor, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnProveedor1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(18, 18, 18)
                            .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(dcFechaCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblFechaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 154, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 787, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jpDatosCompraLayout.setVerticalGroup(
            jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosCompraLayout.createSequentialGroup()
                .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblIDProducto1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRucCedula1, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRucCedula, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFechaCompra))
                .addGap(1, 1, 1)
                .addGroup(jpDatosCompraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(txtNumDoc, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTipoDocumento, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbProveedor, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnProveedor1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcFechaCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblIDProducto2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        jpDatosProducto.setBackground(new java.awt.Color(233, 255, 255));
        jpDatosProducto.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Datos del producto"));

        lblCodigoProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigoProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigoProducto.setText("Id del producto");

        txtIdentificadorProducto.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        txtIdentificadorProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtIdentificadorProducto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtIdentificadorProductoKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtIdentificadorProductoKeyTyped(evt);
            }
        });

        lblTituloDescripcion.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblTituloDescripcion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloDescripcion.setText("Fecha de vencimiento");
        lblTituloDescripcion.setToolTipText("");

        txtDescripcionProducto.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtDescripcionProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescripcionProducto.setEnabled(false);

        lblCodigo6.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCodigo6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCodigo6.setText("Stock actual");

        txtExistenciaActual.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        txtExistenciaActual.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtExistenciaActual.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtExistenciaActual.setEnabled(false);

        txtIdProducto.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N
        txtIdProducto.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtIdProducto.setEnabled(false);

        lblIDProducto.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblIDProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIDProducto.setText("Código");

        btnBuscarProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoBuscar20.png"))); // NOI18N
        btnBuscarProducto.setToolTipText("Buscador de productos");
        btnBuscarProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarProductoActionPerformed(evt);
            }
        });

        btnPantallaCompleta.setBackground(new java.awt.Color(0, 255, 255));
        btnPantallaCompleta.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnPantallaCompleta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoPantallacompleta20.png"))); // NOI18N
        btnPantallaCompleta.setToolTipText("Ampliar vista de Imagen del producto");
        btnPantallaCompleta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPantallaCompletaActionPerformed(evt);
            }
        });

        lblImagen.setFont(new java.awt.Font("Segoe UI Black", 1, 10)); // NOI18N
        lblImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblImagen.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED, new java.awt.Color(204, 204, 204), new java.awt.Color(153, 153, 153)));

        btnABMProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        btnABMProducto.setToolTipText("Abre la ventana de productos");
        btnABMProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnABMProductoActionPerformed(evt);
            }
        });

        dcFechaVencimiento.setMaxSelectableDate(new java.util.Date(4102455600000L));
        dcFechaVencimiento.setMinSelectableDate(new java.util.Date(631162800000L));

        lblTituloDescripcion1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblTituloDescripcion1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloDescripcion1.setText("Descripción del producto");
        lblTituloDescripcion1.setToolTipText("");

        javax.swing.GroupLayout jpDatosProductoLayout = new javax.swing.GroupLayout(jpDatosProducto);
        jpDatosProducto.setLayout(jpDatosProductoLayout);
        jpDatosProductoLayout.setHorizontalGroup(
            jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosProductoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDatosProductoLayout.createSequentialGroup()
                        .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jpDatosProductoLayout.createSequentialGroup()
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblIDProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(28, 28, 28)
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblCodigoProducto, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                                    .addComponent(txtIdentificadorProducto))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lblTituloDescripcion1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(2, 2, 2)
                        .addComponent(btnABMProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(txtDescripcionProducto))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDatosProductoLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblCodigo6, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtExistenciaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dcFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(59, 59, 59)
                .addComponent(lblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );
        jpDatosProductoLayout.setVerticalGroup(
            jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpDatosProductoLayout.createSequentialGroup()
                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpDatosProductoLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnPantallaCompleta, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jpDatosProductoLayout.createSequentialGroup()
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(lblIDProducto)
                                    .addComponent(lblCodigoProducto)
                                    .addComponent(lblCodigo6))
                                .addGap(1, 1, 1)
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(txtExistenciaActual, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnABMProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btnBuscarProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtIdentificadorProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtIdProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(lblTituloDescripcion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblTituloDescripcion1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(1, 1, 1)
                                .addGroup(jpDatosProductoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(dcFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtDescripcionProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(13, 13, 13))))
                    .addGroup(jpDatosProductoLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblImagen, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        btnAnadir.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btnAnadirKeyReleased(evt);
            }
        });

        txtCostoUnitario.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtCostoUnitario.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCostoUnitario.setToolTipText("");
        txtCostoUnitario.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCostoUnitario.setEnabled(false);
        txtCostoUnitario.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCostoUnitarioKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCostoUnitarioKeyTyped(evt);
            }
        });

        lblCostoUnitario.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCostoUnitario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCostoUnitario.setText("Costo (Unidad)*");

        txtCantidadUnitaria.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        txtCantidadUnitaria.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCantidadUnitaria.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtCantidadUnitaria.setEnabled(false);
        txtCantidadUnitaria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadUnitariaActionPerformed(evt);
            }
        });
        txtCantidadUnitaria.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadUnitariaKeyTyped(evt);
            }
        });

        lblCantidadAdquirida.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblCantidadAdquirida.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadAdquirida.setText("Cantidad (Unidades)*");

        cbMoneda.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "GUARANIES" }));
        cbMoneda.setEnabled(false);

        lblPrecioUnitario2.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblPrecioUnitario2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPrecioUnitario2.setText("Moneda");

        btnPack.setFont(new java.awt.Font("sansserif", 1, 12)); // NOI18N
        btnPack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        btnPack.setText("Pack");
        btnPack.setEnabled(false);
        btnPack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPackActionPerformed(evt);
            }
        });
        btnPack.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                btnPackKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jpProductosLayout = new javax.swing.GroupLayout(jpProductos);
        jpProductos.setLayout(jpProductosLayout);
        jpProductosLayout.setHorizontalGroup(
            jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCantidadAdquirida, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtCantidadUnitaria))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPack, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblCostoUnitario, javax.swing.GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)
                    .addComponent(txtCostoUnitario))
                .addGap(12, 12, 12)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPrecioUnitario2)
                    .addComponent(cbMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAnadir)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnQuitar)
                .addContainerGap())
        );
        jpProductosLayout.setVerticalGroup(
            jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpProductosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lblCantidadAdquirida)
                    .addComponent(lblCostoUnitario)
                    .addComponent(lblPrecioUnitario2))
                .addGap(1, 1, 1)
                .addGroup(jpProductosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(txtCantidadUnitaria, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPack, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCostoUnitario, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAnadir, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnQuitar, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel2.setColorPrimario(new java.awt.Color(0, 153, 153));
        panel2.setColorSecundario(new java.awt.Color(233, 255, 255));

        lblmTitulo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblmTitulo.setText("REGISTRAR COMPRA");
        lblmTitulo.setDireccionDeSombra(110);
        lblmTitulo.setFont(new java.awt.Font("Cooper Black", 0, 24)); // NOI18N

        lblNumCompra.setText("00000001");
        lblNumCompra.setFont(new java.awt.Font("Arial", 1, 20)); // NOI18N

        labelMetric1.setText("N° de compra:");
        labelMetric1.setDistanciaDeSombra(2);

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblmTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 573, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(labelMetric1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(lblNumCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNumCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(labelMetric1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblmTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpBotones1.setBackground(new java.awt.Color(233, 255, 255));
        jpBotones1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder()));
        jpBotones1.setPreferredSize(new java.awt.Dimension(100, 50));

        btnGuardar.setBackground(new java.awt.Color(0, 153, 255));
        btnGuardar.setFont(new java.awt.Font("sansserif", 1, 18)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 255, 255));
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoGuardar20.png"))); // NOI18N
        btnGuardar.setText("Registrar compra");
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

        javax.swing.GroupLayout jpBotones1Layout = new javax.swing.GroupLayout(jpBotones1);
        jpBotones1.setLayout(jpBotones1Layout);
        jpBotones1Layout.setHorizontalGroup(
            jpBotones1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpBotones1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnCancelar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpBotones1Layout.setVerticalGroup(
            jpBotones1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpBotones1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpBotones1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btnGuardar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        lblTituloTotalCompra1.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblTituloTotalCompra1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloTotalCompra1.setText("TOTAL DE LA COMPRA");

        txtTotalCompra.setEditable(false);
        txtTotalCompra.setBackground(new java.awt.Color(0, 0, 0));
        txtTotalCompra.setFont(new java.awt.Font("sansserif", 1, 22)); // NOI18N
        txtTotalCompra.setForeground(new java.awt.Color(0, 154, 0));
        txtTotalCompra.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtTotalCompra.setText("0");
        txtTotalCompra.setFocusable(false);

        lblTotalMoneda.setFont(new java.awt.Font("Doppio One", 1, 12)); // NOI18N
        lblTotalMoneda.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotalMoneda.setText("Gs.");
        lblTotalMoneda.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jPanel1.setBackground(new java.awt.Color(233, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Detalle de la compra"));

        tbDetalleCompra.setAutoCreateRowSorter(true);
        tbDetalleCompra.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbDetalleCompra.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbDetalleCompra.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID producto", "Descripcion", "Cantidad (Unidades)", "Costo (Unidad)", "SubTotal", "Fecha de venc."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class, java.lang.String.class
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
        tbDetalleCompra.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbDetalleCompra.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbDetalleCompra.setGridColor(new java.awt.Color(0, 153, 204));
        tbDetalleCompra.setOpaque(false);
        tbDetalleCompra.setRowHeight(20);
        tbDetalleCompra.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbDetalleCompra.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbDetalleCompra.getTableHeader().setReorderingAllowed(false);
        tbDetalleCompra.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbDetalleCompraMousePressed(evt);
            }
        });
        tbDetalleCompra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDetalleCompraKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbDetalleCompraKeyReleased(evt);
            }
        });
        scDetalleCompra.setViewportView(tbDetalleCompra);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scDetalleCompra, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scDetalleCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jpPrincipalLayout.createSequentialGroup()
                        .addGap(195, 195, 195)
                        .addComponent(jpBotones1, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblTituloTotalCompra1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtTotalCompra, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTotalMoneda))
                    .addComponent(jpDatosCompra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jpDatosProducto, javax.swing.GroupLayout.DEFAULT_SIZE, 843, Short.MAX_VALUE)
                    .addComponent(jpProductos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpDatosCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpDatosProducto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(jpProductos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTituloTotalCompra1)
                .addGap(1, 1, 1)
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTotalMoneda, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalCompra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jpBotones1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 860, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 680, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        getAccessibleContext().setAccessibleName("RegistrarCompra");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void LimpiarProducto() {
        txtIdProducto.setText("");
        txtExistenciaActual.setText("");
        dcFechaVencimiento.setDate(null);
        txtDescripcionProducto.setText("");
        lblImagen.setIcon(null);
    }

    private void tbDetalleCompraMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetalleCompraMousePressed
        if (tbDetalleCompra.isEnabled() == true) {
            Integer filaSelect = tbDetalleCompra.rowAtPoint(evt.getPoint()); //num de fila seleccionada
            Integer columnSelect = tbDetalleCompra.columnAtPoint(evt.getPoint()); //Columna seleccionada
            if (evt.getClickCount() == 2 && columnSelect == 3) { //Si se hace doble click
                double costounitario = metodostxt.StringAFormatoAmericano(tbDetalleCompra.getValueAt(filaSelect, 4).toString());
                System.out.println("precio " + costounitario);
                //Validar que sea numero valido
                int cantidad = 0;
                boolean esNumeroValido = false;

                while (esNumeroValido == false) {
                    try {
                        String cantidadString = JOptionPane.showInputDialog(this, "Ingrese la nueva cantidad: ", "Modificar cantidad", JOptionPane.INFORMATION_MESSAGE);
                        if (cantidadString == null) {
                            System.out.println("Se canceló la operación");
                            return;
                        }

                        cantidad = Integer.parseInt(cantidadString);
                        if (cantidad <= 0) {
                            JOptionPane.showMessageDialog(this, "La cantidad no puede ser menor o igual a 0");
                            return;
                        }
                        esNumeroValido = true;
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Ingrese un número entero válido");
                        esNumeroValido = false;
                    }
                }

                double subtotal = cantidad * costounitario;
                tbDetalleCompra.setValueAt(cantidad, filaSelect, columnSelect);
                tbDetalleCompra.setValueAt(subtotal, filaSelect, 5);
                SumarSubtotal();
                JOptionPane.showMessageDialog(this, "Cantidad modificada con éxito!");

            }
            btnQuitar.setEnabled(true);
        }
    }//GEN-LAST:event_tbDetalleCompraMousePressed

    private void btnAnadirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAnadirActionPerformed
        if (ComprobarCamposProducto() == true) {
            try {
                for (int i = 0; i < tbDetalleCompra.getRowCount(); i++) { //Si ya se cargo producto
                    if (txtIdProducto.getText().equals(tbDetalleCompra.getValueAt(i, 0))) {
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(this, "Este producto ya se cargó");
                        return;
                    }
                }

                String idProducto, descriProducto;
                int cantidadUnitaria;
                double costoUnitario, subtotal;
                String fechaVencimiento = "";

                idProducto = txtIdProducto.getText();
                descriProducto = txtDescripcionProducto.getText();
                cantidadUnitaria = Integer.parseInt(txtCantidadUnitaria.getText());
                costoUnitario = metodostxt.StringAFormatoAmericano(txtCostoUnitario.getText());
                costoUnitario = metodostxt.arredondamientoDouble(costoUnitario, 2);
                subtotal = cantidadUnitaria * costoUnitario;
                SimpleDateFormat formatosuda = new SimpleDateFormat("dd/MM/yyyy");
                if (dcFechaVencimiento.getDate() != null) {
                    fechaVencimiento = formatosuda.format(dcFechaVencimiento.getDate());
                }

                tabmodelDetalleCompra.addRow(new Object[]{idProducto, descriProducto, cantidadUnitaria, costoUnitario, subtotal, fechaVencimiento});

                SumarSubtotal();

                //Limpiar
                txtIdentificadorProducto.setText("");
                LimpiarProducto();
                txtCantidadUnitaria.setText("");
                txtCostoUnitario.setText("");

                txtIdentificadorProducto.requestFocus();
            } catch (NumberFormatException e) {
                System.out.println("Error al añadir producto a la tabla " + e);
            }
        }
    }//GEN-LAST:event_btnAnadirActionPerformed

    private void SumarSubtotal() {
        //Suma la colmna subtotal
        double totalcompra = helpersTable.SumarColumnaDouble(tbDetalleCompra, 4);
        totalcompra = metodostxt.arredondamientoDouble(totalcompra, 2);
        String totalcompraString = metodostxt.DoubleAFormatSudamerica(totalcompra);
        txtTotalCompra.setText(totalcompraString);
    }

    private void GenerarNumCompra() {
        //Generar numero compra
        try {
            con = con.ObtenerRSSentencia("SELECT MAX(com_numcompra) AS numultimacompra FROM compra");
            String numultimacompra = null;
            while (con.getResultSet().next()) {
                numultimacompra = con.getResultSet().getString("numultimacompra");
            }

            if (numultimacompra == null) {
                numultimacompra = String.format("%8s", String.valueOf(1)).replace(' ', '0');
            } else {
                numultimacompra = String.format("%8s", String.valueOf((Integer.parseInt(numultimacompra) + 1))).replace(' ', '0');
            }
            lblNumCompra.setText(numultimacompra);

        } catch (SQLException e) {
            System.out.println("Error GenerarNumCompra " + e);
        }
        con.DesconectarBasedeDatos();
    }

    private void txtCantidadUnitariaKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaKeyTyped
        metodostxt.SoloNumeroEnteroKeyTyped(evt);
    }//GEN-LAST:event_txtCantidadUnitariaKeyTyped

    private void txtCantidadUnitariaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaKeyReleased
        metodostxt.TxtColorLabelKeyReleased(txtCantidadUnitaria, lblCantidadAdquirida, colorTitulos);
    }//GEN-LAST:event_txtCantidadUnitariaKeyReleased

    private void txtCostoUnitarioKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoUnitarioKeyReleased
        txtCostoUnitario.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtCostoUnitario.getText()));
        metodostxt.TxtColorLabelKeyReleased(txtCostoUnitario, lblCostoUnitario, colorTitulos);

    }//GEN-LAST:event_txtCostoUnitarioKeyReleased

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        tabmodelDetalleCompra.removeRow(tbDetalleCompra.getSelectedRow());
        SumarSubtotal();

        if (tbDetalleCompra.getRowCount() > 0) {
            cbMoneda.setEnabled(false);
        } else {
            cbMoneda.setEnabled(true);
            btnQuitar.setEnabled(false);
        }
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void txtCostoUnitarioKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoUnitarioKeyTyped
        metodostxt.TxtCantidadCaracteresKeyTyped(txtCostoUnitario, 11);
        metodostxt.SoloNumeroDecimalKeyTyped(evt, txtCostoUnitario);
    }//GEN-LAST:event_txtCostoUnitarioKeyTyped

    private void txtIdentificadorProductoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIdentificadorProductoKeyTyped
        //Evitar que entre espacio
        if (evt.getKeyChar() == KeyEvent.VK_SPACE) {
            evt.consume();
        }
    }//GEN-LAST:event_txtIdentificadorProductoKeyTyped

    private void txtIdentificadorProductoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIdentificadorProductoKeyReleased
        metodostxt.TxtColorLabelKeyReleased(txtIdentificadorProducto, lblCodigoProducto, colorTitulos);

        //Si se oprime espacio se entrara en la ventana de productos en donde se debe seleccionar el  producto
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BuscadorProductos.setLocationRelativeTo(this);
            txtBuscarApoderado.setText(txtIdentificadorProducto.getText());
            BuscadorProductos.setVisible(true);
        }

//Si se oprime ENTER o si el producto ya se encontro y se cambia el codigo de producto, volver a buscar
        /*if (evt.getKeyCode() == KeyEvent.VK_ENTER || txtIdProducto.getText().equals("") == false) {
            if (ConsultaProducto() == true) {
                txtCantidadUnitaria.requestFocus();
            } else {
                LimpiarProducto();
            }
        }*/
    }//GEN-LAST:event_txtIdentificadorProductoKeyReleased

    private void tbDetalleCompraKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetalleCompraKeyReleased

    }//GEN-LAST:event_tbDetalleCompraKeyReleased

    private void btnBuscarProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarProductoActionPerformed
        ConsultaAllProducto();

        BuscadorProductos.setLocationRelativeTo(this);
        BuscadorProductos.setVisible(true);
    }//GEN-LAST:event_btnBuscarProductoActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        RegistroNuevo();
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnGuardarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnGuardarKeyPressed

    }//GEN-LAST:event_btnGuardarKeyPressed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        int confirmado = JOptionPane.showConfirmDialog(this, "¿Seguro que desea borrar todos los datos de la compra actual?", "Confirmación", JOptionPane.YES_OPTION);
        if (JOptionPane.YES_OPTION == confirmado) {
            Limpiar();
        }
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnPantallaCompletaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPantallaCompletaActionPerformed
        VistaCompleta vistacompleta = new VistaCompleta(rutaFotoProducto + "image_" + txtIdProducto.getText() + "_A", rutaFotoDefault);
        vistacompleta.setLocationRelativeTo(this);
        vistacompleta.setVisible(true);
    }//GEN-LAST:event_btnPantallaCompletaActionPerformed

    private void tbDetalleCompraKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDetalleCompraKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbDetalleCompraKeyPressed

    private void btnProveedor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProveedor1ActionPerformed
        String provnombre = "";
        while (provnombre.equals("")) {
            provnombre = JOptionPane.showInputDialog(this, "Ingrese el nombre del proveedor(*)", "Nuevo proveedor", JOptionPane.INFORMATION_MESSAGE);
            if (provnombre == null) {
                System.out.println("Se cancelo");
                return;
            }
        }
        String provdireccion = "";
        while (provdireccion.equals("")) {
            provdireccion = JOptionPane.showInputDialog(this, "Ingrese la dirección del proveedor(*)", "Nuevo proveedor", JOptionPane.INFORMATION_MESSAGE);
            if (provdireccion == null) {
                System.out.println("Se cancelo");
                return;
            }
        }
        String provcel = JOptionPane.showInputDialog(this, "Ingrese el celular del proveedor", "Nuevo proveedor", JOptionPane.INFORMATION_MESSAGE);
        if (provcel == null) {
            System.out.println("Se cancelo");
            return;
        }
        String provemail = JOptionPane.showInputDialog(this, "Ingrese el email del proveedor", "Nuevo proveedor", JOptionPane.INFORMATION_MESSAGE);
        if (provemail == null) {
            System.out.println("Se cancelo");
            return;
        }

        String sentencia = "CALL SP_ProveedorAlta('" + provnombre.toUpperCase() + "','" + provcel + "','" + provemail + "','" + provdireccion + "')";
        con.EjecutarABM(sentencia, true);

        CargarComboBoxes();
    }//GEN-LAST:event_btnProveedor1ActionPerformed

    private void btnABMProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnABMProductoActionPerformed
        ProductoVista abmproducto = new ProductoVista(null, true);
        abmproducto.setLocationRelativeTo(this);
        abmproducto.setVisible(true);
    }//GEN-LAST:event_btnABMProductoActionPerformed

    private void btnAnadirKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnAnadirKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            btnAnadir.doClick();
        }
    }//GEN-LAST:event_btnAnadirKeyReleased

    private void btnPackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPackActionPerformed
        spCantidadPackCalculoPacks.setValue(0);
        spUnidadPorPackCalculoPacks.setValue(12);
        txtCostoPackCalculoPacks.setText("0");
        txtCostoTotalPacksCalculoPacks.setText("0");
        txtCostoUnitarioCalculoPacks.setText("0");

        CalculoPacks.setLocationRelativeTo(this);
        CalculoPacks.setVisible(true);
    }//GEN-LAST:event_btnPackActionPerformed

    private void btnPackKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPackKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPackKeyReleased

    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        txtCantidadUnitaria.setText(txtCantidadUnitariaCalculoPacks.getText());
        txtCostoUnitario.setText(txtCostoUnitarioCalculoPacks.getText());

        CalculoPacks.dispose();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnAgregarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnAgregarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAgregarKeyPressed

    private void txtCantidadUnitariaCalculoPacksKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaCalculoPacksKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadUnitariaCalculoPacksKeyReleased

    private void txtCantidadUnitariaCalculoPacksKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaCalculoPacksKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadUnitariaCalculoPacksKeyTyped

    private void txtCostoPackCalculoPacksKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoPackCalculoPacksKeyReleased
        txtCostoPackCalculoPacks.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtCostoPackCalculoPacks.getText()));

        int cantidadpacks = Integer.parseInt(spCantidadPackCalculoPacks.getValue() + "");
        int unidadporpack = Integer.parseInt(spUnidadPorPackCalculoPacks.getValue() + "");
        double costoporpack = metodostxt.StringAFormatoAmericano(txtCostoPackCalculoPacks.getText());
        double costototalpacks = metodostxt.arredondamientoDouble(cantidadpacks * costoporpack, 2);
        double costounitario = metodostxt.arredondamientoDouble(costoporpack / unidadporpack, 2);

        txtCostoTotalPacksCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costototalpacks));
        txtCostoUnitarioCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costounitario));
    }//GEN-LAST:event_txtCostoPackCalculoPacksKeyReleased

    private void txtCostoPackCalculoPacksKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoPackCalculoPacksKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostoPackCalculoPacksKeyTyped

    private void txtCostoUnitarioCalculoPacksKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoUnitarioCalculoPacksKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostoUnitarioCalculoPacksKeyReleased

    private void txtCostoUnitarioCalculoPacksKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoUnitarioCalculoPacksKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostoUnitarioCalculoPacksKeyTyped

    private void txtCostoTotalPacksCalculoPacksKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoTotalPacksCalculoPacksKeyReleased
        txtCostoTotalPacksCalculoPacks.setText(metodostxt.StringAFormatSudamericaKeyRelease(txtCostoTotalPacksCalculoPacks.getText()));

        int cantidadpacks = Integer.parseInt(spCantidadPackCalculoPacks.getValue() + "");
        int unidadporpack = Integer.parseInt(spUnidadPorPackCalculoPacks.getValue() + "");
        double costototalpacks = metodostxt.StringAFormatoAmericano(txtCostoTotalPacksCalculoPacks.getText());
        double costoporpack = metodostxt.arredondamientoDouble(costototalpacks / cantidadpacks, 2);
        double costounitario = metodostxt.arredondamientoDouble(costoporpack / unidadporpack, 2);

        txtCostoPackCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costoporpack));
        txtCostoUnitarioCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costounitario));
    }//GEN-LAST:event_txtCostoTotalPacksCalculoPacksKeyReleased

    private void txtCostoTotalPacksCalculoPacksKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCostoTotalPacksCalculoPacksKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCostoTotalPacksCalculoPacksKeyTyped

    private void spCantidadPackCalculoPacksStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spCantidadPackCalculoPacksStateChanged
        CalculoDePacks();
    }//GEN-LAST:event_spCantidadPackCalculoPacksStateChanged

    private void CalculoDePacks() throws NumberFormatException {
        int cantidadpacks = Integer.parseInt(spCantidadPackCalculoPacks.getValue().toString());
        int unidadporpack = Integer.parseInt(spUnidadPorPackCalculoPacks.getValue().toString());
        int cantidadunitaria = cantidadpacks * unidadporpack;
        txtCantidadUnitariaCalculoPacks.setText(cantidadunitaria + "");

        double costoporpack = metodostxt.StringAFormatoAmericano(txtCostoPackCalculoPacks.getText());
        double costototalpacks = metodostxt.arredondamientoDouble(cantidadpacks * costoporpack, 2);
        double costounitario = metodostxt.arredondamientoDouble(costoporpack / unidadporpack, 2);

        txtCostoTotalPacksCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costototalpacks));
        txtCostoUnitarioCalculoPacks.setText(metodostxt.DoubleAFormatSudamerica(costounitario));
    }

    private void txtCostoPackCalculoPacksFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCostoPackCalculoPacksFocusLost
        if (txtCostoPackCalculoPacks.getText().equals("")) {
            txtCostoPackCalculoPacks.setText("0");
        }
    }//GEN-LAST:event_txtCostoPackCalculoPacksFocusLost

    private void txtCostoTotalPacksCalculoPacksFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCostoTotalPacksCalculoPacksFocusLost
        if (txtCostoTotalPacksCalculoPacks.getText().equals("")) {
            txtCostoTotalPacksCalculoPacks.setText("0");
        }
    }//GEN-LAST:event_txtCostoTotalPacksCalculoPacksFocusLost

    private void spUnidadPorPackCalculoPacksStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spUnidadPorPackCalculoPacksStateChanged
        CalculoDePacks();
    }//GEN-LAST:event_spUnidadPorPackCalculoPacksStateChanged

    private void txtBuscarApoderadoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarApoderadoKeyReleased
        helpersTable.FiltroJTable(txtBuscarApoderado.getText(), cbCampoBuscarBuscadorProductos.getSelectedIndex(), tbProductosBuscadorProductos);

        if (tbProductosBuscadorProductos.getRowCount() == 1) {
            lbCantRegistrosBuscadorProductos.setText(tbProductosBuscadorProductos.getRowCount() + " Registro encontrado");
        } else {
            lbCantRegistrosBuscadorProductos.setText(tbProductosBuscadorProductos.getRowCount() + " Registros encontrados");
        }
    }//GEN-LAST:event_txtBuscarApoderadoKeyReleased

    private void tbProductosBuscadorProductosMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbProductosBuscadorProductosMousePressed
        if (evt.getClickCount() == 2) {
            SeleccionarProducto();
        }
    }//GEN-LAST:event_tbProductosBuscadorProductosMousePressed

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
            btnPack.setEnabled(true);
            txtCostoUnitario.setEnabled(true);
            txtCantidadUnitaria.requestFocus();
            BuscadorProductos.dispose();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void tbProductosBuscadorProductosKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbProductosBuscadorProductosKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            SeleccionarProducto();
        }
    }//GEN-LAST:event_tbProductosBuscadorProductosKeyReleased

    private void txtCantidadUnitariaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCantidadUnitariaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCantidadUnitariaActionPerformed

    private void ConsultaAllProducto() {//Realiza la consulta de los productos que tenemos en la base de datos
        tabmodelProductos = (DefaultTableModel) tbProductosBuscadorProductos.getModel();
        tabmodelProductos.setRowCount(0);

        if (cbCampoBuscarBuscadorProductos.getItemCount() == 0) {
            helpersComboBox.CargarTitlesaCombo(cbCampoBuscarBuscadorProductos, tbProductosBuscadorProductos);
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

            if (tbDetalleCompra.getModel().getRowCount() == 1) {
                lbCantRegistrosBuscadorProductos.setText(tbProductosBuscadorProductos.getModel().getRowCount() + " Registro encontrado");
            } else {
                lbCantRegistrosBuscadorProductos.setText(tbProductosBuscadorProductos.getModel().getRowCount() + " Registros encontrados");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
    }

    static void PonerProductoSeleccionado(String codigoproducto) {
        txtIdentificadorProducto.setText(codigoproducto + "");
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog BuscadorProductos;
    private javax.swing.JDialog CalculoPacks;
    private javax.swing.JButton btnABMProducto;
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnAnadir;
    private javax.swing.JButton btnBuscarProducto;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnPack;
    private javax.swing.JButton btnPantallaCompleta;
    private javax.swing.JButton btnProveedor1;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JComboBox cbCampoBuscarBuscadorProductos;
    private javax.swing.JComboBox<String> cbMoneda;
    private javax.swing.JComboBox<HelpersComboBox> cbProveedor;
    private javax.swing.JComboBox<HelpersComboBox> cbTipoDocumento;
    private com.toedter.calendar.JDateChooser dcFechaCompra;
    private com.toedter.calendar.JDateChooser dcFechaVencimiento;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jpBotones1;
    private javax.swing.JPanel jpDatosCompra;
    private javax.swing.JPanel jpDatosProducto;
    private javax.swing.JPanel jpPrincipal;
    private javax.swing.JPanel jpProductos;
    private org.edisoncor.gui.label.LabelMetric labelMetric1;
    private org.edisoncor.gui.label.LabelMetric labelMetric3;
    private javax.swing.JLabel lbCantRegistrosBuscadorProductos;
    private javax.swing.JLabel lblBuscarCampoApoderado;
    private javax.swing.JLabel lblCantidadAdquirida;
    private javax.swing.JLabel lblCantidadAdquirida1;
    private javax.swing.JLabel lblCantidadAdquirida2;
    private javax.swing.JLabel lblCantidadAdquirida3;
    private javax.swing.JLabel lblCantidadAdquirida4;
    private javax.swing.JLabel lblCantidadAdquirida5;
    private javax.swing.JLabel lblCodigo6;
    private javax.swing.JLabel lblCodigoProducto;
    private javax.swing.JLabel lblCostoUnitario;
    private javax.swing.JLabel lblFechaCompra;
    private javax.swing.JLabel lblIDProducto;
    private javax.swing.JLabel lblIDProducto1;
    private javax.swing.JLabel lblIDProducto2;
    private javax.swing.JLabel lblImagen;
    private org.edisoncor.gui.label.LabelMetric lblNumCompra;
    private javax.swing.JLabel lblPrecioUnitario1;
    private javax.swing.JLabel lblPrecioUnitario2;
    private javax.swing.JLabel lblPrecioUnitario3;
    private javax.swing.JLabel lblPrecioUnitario4;
    private javax.swing.JLabel lblPrecioUnitario5;
    private javax.swing.JLabel lblRucCedula;
    private javax.swing.JLabel lblRucCedula1;
    private javax.swing.JLabel lblTituloDescripcion;
    private javax.swing.JLabel lblTituloDescripcion1;
    private javax.swing.JLabel lblTituloTotalCompra1;
    private javax.swing.JLabel lblTotalMoneda;
    private org.edisoncor.gui.label.LabelMetric lblmTitulo;
    private org.edisoncor.gui.panel.Panel panel2;
    private org.edisoncor.gui.panel.Panel panel6;
    private org.edisoncor.gui.panel.Panel panel7;
    private javax.swing.JScrollPane scDetalleCompra;
    private javax.swing.JScrollPane scProductosBuscadorProductos;
    private javax.swing.JSpinner spCantidadPackCalculoPacks;
    private javax.swing.JSpinner spUnidadPorPackCalculoPacks;
    private javax.swing.JTextArea taObs;
    private javax.swing.JTable tbDetalleCompra;
    private javax.swing.JTable tbProductosBuscadorProductos;
    private javax.swing.JTextField txtBuscarApoderado;
    private javax.swing.JTextField txtCantidadUnitaria;
    private javax.swing.JTextField txtCantidadUnitariaCalculoPacks;
    private javax.swing.JTextField txtCostoPackCalculoPacks;
    private javax.swing.JTextField txtCostoTotalPacksCalculoPacks;
    private javax.swing.JTextField txtCostoUnitario;
    private javax.swing.JTextField txtCostoUnitarioCalculoPacks;
    private javax.swing.JTextField txtDescripcionProducto;
    private javax.swing.JTextField txtExistenciaActual;
    private javax.swing.JTextField txtIdProducto;
    private static javax.swing.JTextField txtIdentificadorProducto;
    private javax.swing.JTextField txtNumDoc;
    private javax.swing.JTextField txtTotalCompra;
    // End of variables declaration//GEN-END:variables
}
