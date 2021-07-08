package principal;

import conexion.Conexion;

import forms.usuario.ABMPerfil;
import forms.usuario.ABMUsuario;
import forms.usuario.ABMUsuarioRol;
import forms.compra.ABMProveedor;
import forms.compra.RegistrarCompra;
import forms.producto.ABMProducto;

import forms.usuario.ABMModulo;
import forms.venta.ABMCliente;
import forms.venta.RegistrarVenta;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import login.Login;
//Variables globales
import static login.Login.codUsuario;
import static login.Login.alias;
//import reportes.ReportePlanilla;
//

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class Principal extends javax.swing.JFrame implements Runnable {

    private Conexion con = new Conexion();
    private Thread hilo;

    public Principal() {
        initComponents();
        this.setExtendedState(Principal.MAXIMIZED_BOTH);//Maximizar ventana

        ObtenerHorayFecha();

        lbAlias.setText(alias);
        PerfilesUsuario(codUsuario);
        PermisoModulos(codUsuario);

    }

    private void PermisoModulos(String codUsuario) {
        con = con.ObtenerRSSentencia("CALL SP_UsuarioModuloConsulta('" + codUsuario + "')");
        String modulo;
        try {
            while (con.getResultSet().next()) {
                modulo = con.getResultSet().getString("mo_denominacion");
                switch (modulo) {
                    case "PRODUCTO" -> {
                        btnProducto.setEnabled(true);
                        meProducto.setEnabled(true);
                        meitRegistrarActualizacionStock.setEnabled(true);
                    }

                    case "VENTA" -> {
                        meVenta.setEnabled(true);
                        meitRegistrarVenta.setEnabled(true);
                    }

                    case "COMPRA" -> {
                        meCompra.setEnabled(true);
                        meitRegistrarCompra.setEnabled(true);
                    }

                    case "PROVEEDOR" -> {
                        meitProveedor.setEnabled(true);
                    }

                    case "CLIENTE" -> {
                        btnCliente.setEnabled(true);
                    }

                    case "USUARIO" -> {
                        btnUsuario.setEnabled(true);
                        meUsuario.setEnabled(true);
                        meitPerfil.setEnabled(true);
                        meitModulo.setEnabled(true);
                        meitRol.setEnabled(true);
                    }

                    case "REPORTE" -> {
                        meReporte.setEnabled(true);
                        meiReportePlanilla.setEnabled(true);
                    }

                    case "CONFIGURACION" -> {
                        meConfiguracion.setEnabled(true);
                    }
                    default -> {
                        //JOptionPane.showMessageDialog(this, "No se encontró " + modulo, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            con.DesconectarBasedeDatos();
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void PerfilesUsuario(String codUsuario) {
        String consulta = "CALL SP_UsuarioPerfilConsulta(" + codUsuario + ")";
        con = con.ObtenerRSSentencia(consulta);
        try {
            String perfil = "";
            while (con.getResultSet().next()) {
                if (perfil.equals("")) {
                    perfil = con.getResultSet().getString("per_denominacion");
                } else {
                    perfil = perfil + ", " + con.getResultSet().getString("per_denominacion");
                }
            }
            lblPerfil.setText(perfil);
        } catch (SQLException | NullPointerException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        con.DesconectarBasedeDatos();
    }

    private void ObtenerHorayFecha() {
        //Obtener fecha y hora
        hilo = new Thread(this);
        hilo.start();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        piPrincipal = new org.edisoncor.gui.panel.PanelImage();
        btnAplicacion = new javax.swing.JButton();
        btnCliente = new javax.swing.JButton();
        btnUsuario = new javax.swing.JButton();
        btnProducto = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        panelImage1 = new org.edisoncor.gui.panel.PanelImage();
        panelImage2 = new org.edisoncor.gui.panel.PanelImage();
        panelImage3 = new org.edisoncor.gui.panel.PanelImage();
        jLabel7 = new javax.swing.JLabel();
        panel1 = new org.edisoncor.gui.panel.Panel();
        jLabel1 = new javax.swing.JLabel();
        lbAlias = new javax.swing.JLabel();
        lblPerfil = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbFechaTitulo = new javax.swing.JLabel();
        lbFecha = new javax.swing.JLabel();
        lbHoraTitulo = new javax.swing.JLabel();
        lbHora = new javax.swing.JLabel();
        panelImage4 = new org.edisoncor.gui.panel.PanelImage();
        jMenuBar1 = new javax.swing.JMenuBar();
        meVenta = new javax.swing.JMenu();
        meitRegistrarVenta = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        meitDeuda = new javax.swing.JMenuItem();
        meCompra = new javax.swing.JMenu();
        meitRegistrarCompra = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        meitProveedor = new javax.swing.JMenuItem();
        meProducto = new javax.swing.JMenu();
        meiCategoria = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        meitRegistrarActualizacionStock = new javax.swing.JMenuItem();
        meReporte = new javax.swing.JMenu();
        meiReportePlanilla = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        meUsuario = new javax.swing.JMenu();
        meitRol = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItem18 = new javax.swing.JMenuItem();
        meConfiguracion = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        meitPerfil = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        meitModulo = new javax.swing.JMenuItem();
        meSalir = new javax.swing.JMenu();
        jMenuItem19 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Menu Principal");
        setName("Fm_Principal"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        piPrincipal.setFocusable(false);
        piPrincipal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fondo_principal.png"))); // NOI18N
        piPrincipal.setPreferredSize(new java.awt.Dimension(2000, 655));

        btnAplicacion.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnAplicacion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoBuscar40.png"))); // NOI18N
        btnAplicacion.setText("APLICACIONES");
        btnAplicacion.setEnabled(false);
        btnAplicacion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAplicacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAplicacionActionPerformed(evt);
            }
        });

        btnCliente.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoClientes40.png"))); // NOI18N
        btnCliente.setText("CLIENTES");
        btnCliente.setEnabled(false);
        btnCliente.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClienteActionPerformed(evt);
            }
        });

        btnUsuario.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos50x50/IconoUsuario50.png"))); // NOI18N
        btnUsuario.setText("USUARIOS");
        btnUsuario.setEnabled(false);
        btnUsuario.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsuarioActionPerformed(evt);
            }
        });

        btnProducto.setFont(new java.awt.Font("Century Gothic", 1, 14)); // NOI18N
        btnProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoProducto40.png"))); // NOI18N
        btnProducto.setText("PRODUCTOS");
        btnProducto.setEnabled(false);
        btnProducto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnProducto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductoActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 153, 153));

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Desarrollado por Lic. Arnaldo Cantero");

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Contactos");

        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel5.setText("0973-694378");

        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel6.setText("arnaldorcm@hotmail.com");

        panelImage1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo_whatsapp.png"))); // NOI18N

        javax.swing.GroupLayout panelImage1Layout = new javax.swing.GroupLayout(panelImage1);
        panelImage1.setLayout(panelImage1Layout);
        panelImage1Layout.setHorizontalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        panelImage1Layout.setVerticalGroup(
            panelImage1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        panelImage2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo_gmail.png"))); // NOI18N

        javax.swing.GroupLayout panelImage2Layout = new javax.swing.GroupLayout(panelImage2);
        panelImage2.setLayout(panelImage2Layout);
        panelImage2Layout.setHorizontalGroup(
            panelImage2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        panelImage2Layout.setVerticalGroup(
            panelImage2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        panelImage3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo_outlook.png"))); // NOI18N

        javax.swing.GroupLayout panelImage3Layout = new javax.swing.GroupLayout(panelImage3);
        panelImage3.setLayout(panelImage3Layout);
        panelImage3Layout.setHorizontalGroup(
            panelImage3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );
        panelImage3Layout.setVerticalGroup(
            panelImage3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 25, Short.MAX_VALUE)
        );

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel7.setText("arnaldo1ooo95@gmail.com");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(panelImage3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(panelImage2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelImage1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelImage3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelImage2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(64, 64, 64))
        );

        panel1.setColorPrimario(new java.awt.Color(255, 255, 255));
        panel1.setColorSecundario(new java.awt.Color(154, 255, 255));
        panel1.setGradiente(org.edisoncor.gui.panel.Panel.Gradiente.VERTICAL);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoUsuario20.png"))); // NOI18N
        jLabel1.setText("Usuario:");

        lbAlias.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lbAlias.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbAlias.setText("Error de usuario");

        lblPerfil.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lblPerfil.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblPerfil.setText("Error de perfil");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel2.setText("Perfil:");

        lbFechaTitulo.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        lbFechaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbFechaTitulo.setText("Fecha de hoy:");
        lbFechaTitulo.setFocusable(false);
        lbFechaTitulo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbFecha.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lbFecha.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbFecha.setText("00/00/0000");
        lbFecha.setFocusable(false);
        lbFecha.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbHoraTitulo.setFont(new java.awt.Font("Tahoma", 1, 16)); // NOI18N
        lbHoraTitulo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbHoraTitulo.setText("Hora actual:");
        lbHoraTitulo.setFocusable(false);
        lbHoraTitulo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        lbHora.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        lbHora.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lbHora.setText("00:00:00");
        lbHora.setFocusable(false);
        lbHora.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbAlias, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPerfil, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
                .addGap(496, 496, 496)
                .addComponent(lbFechaTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbFecha)
                .addGap(18, 18, 18)
                .addComponent(lbHoraTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbHora, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lbAlias, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbFechaTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbHoraTitulo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbHora, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        lbFechaTitulo.getAccessibleContext().setAccessibleName("");

        panelImage4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/logo_mainumby.png"))); // NOI18N

        javax.swing.GroupLayout panelImage4Layout = new javax.swing.GroupLayout(panelImage4);
        panelImage4.setLayout(panelImage4Layout);
        panelImage4Layout.setHorizontalGroup(
            panelImage4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 221, Short.MAX_VALUE)
        );
        panelImage4Layout.setVerticalGroup(
            panelImage4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 220, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout piPrincipalLayout = new javax.swing.GroupLayout(piPrincipal);
        piPrincipal.setLayout(piPrincipalLayout);
        piPrincipalLayout.setHorizontalGroup(
            piPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(piPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(piPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(piPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnUsuario, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnProducto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAplicacion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panelImage4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        piPrincipalLayout.setVerticalGroup(
            piPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(piPrincipalLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(btnAplicacion, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnProducto, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 219, Short.MAX_VALUE)
                .addGroup(piPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, piPrincipalLayout.createSequentialGroup()
                        .addComponent(panelImage4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        jMenuBar1.setMinimumSize(new java.awt.Dimension(120, 70));
        jMenuBar1.setPreferredSize(new java.awt.Dimension(120, 55));

        meVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoVenta40.png"))); // NOI18N
        meVenta.setText("VENTAS");
        meVenta.setEnabled(false);
        meVenta.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meVenta.setMaximumSize(new java.awt.Dimension(150, 32767));
        meVenta.setMinimumSize(new java.awt.Dimension(150, 70));
        meVenta.setPreferredSize(new java.awt.Dimension(150, 70));

        meitRegistrarVenta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        meitRegistrarVenta.setText("Registrar venta");
        meitRegistrarVenta.setEnabled(false);
        meitRegistrarVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitRegistrarVentaActionPerformed(evt);
            }
        });
        meVenta.add(meitRegistrarVenta);
        meVenta.add(jSeparator19);

        meitDeuda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        meitDeuda.setText("Saldar Deudas");
        meitDeuda.setEnabled(false);
        meitDeuda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitDeudaActionPerformed(evt);
            }
        });
        meVenta.add(meitDeuda);

        jMenuBar1.add(meVenta);

        meCompra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoCompra40.png"))); // NOI18N
        meCompra.setText("COMPRAS");
        meCompra.setEnabled(false);
        meCompra.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meCompra.setMaximumSize(new java.awt.Dimension(150, 32767));
        meCompra.setMinimumSize(new java.awt.Dimension(150, 70));
        meCompra.setPreferredSize(new java.awt.Dimension(150, 70));

        meitRegistrarCompra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        meitRegistrarCompra.setText("Registrar Compra");
        meitRegistrarCompra.setEnabled(false);
        meitRegistrarCompra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitRegistrarCompraActionPerformed(evt);
            }
        });
        meCompra.add(meitRegistrarCompra);
        meCompra.add(jSeparator17);

        meitProveedor.setText("Proveedores");
        meitProveedor.setEnabled(false);
        meitProveedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitProveedorActionPerformed(evt);
            }
        });
        meCompra.add(meitProveedor);

        jMenuBar1.add(meCompra);

        meProducto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoProducto40.png"))); // NOI18N
        meProducto.setText("PRODUCTOS");
        meProducto.setEnabled(false);
        meProducto.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meProducto.setMaximumSize(new java.awt.Dimension(150, 32767));
        meProducto.setMinimumSize(new java.awt.Dimension(150, 70));
        meProducto.setPreferredSize(new java.awt.Dimension(150, 70));

        meiCategoria.setText("Categorias");
        meiCategoria.setEnabled(false);
        meiCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meiCategoriaActionPerformed(evt);
            }
        });
        meProducto.add(meiCategoria);
        meProducto.add(jSeparator14);

        meitRegistrarActualizacionStock.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos20x20/IconoNuevo20.png"))); // NOI18N
        meitRegistrarActualizacionStock.setText("Actualización de Stock");
        meitRegistrarActualizacionStock.setEnabled(false);
        meitRegistrarActualizacionStock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitRegistrarActualizacionStockActionPerformed(evt);
            }
        });
        meProducto.add(meitRegistrarActualizacionStock);

        jMenuBar1.add(meProducto);

        meReporte.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/iconos40x40/IconoReporte40.png"))); // NOI18N
        meReporte.setText("REPORTES");
        meReporte.setEnabled(false);
        meReporte.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meReporte.setPreferredSize(new java.awt.Dimension(130, 70));

        meiReportePlanilla.setText("Reporte planilla");
        meiReportePlanilla.setEnabled(false);
        meiReportePlanilla.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meiReportePlanillaActionPerformed(evt);
            }
        });
        meReporte.add(meiReportePlanilla);
        meReporte.add(jSeparator13);

        jMenuBar1.add(meReporte);

        meUsuario.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos50x50/IconoUsuario50.png"))); // NOI18N
        meUsuario.setText("USUARIOS");
        meUsuario.setEnabled(false);
        meUsuario.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meUsuario.setMaximumSize(new java.awt.Dimension(150, 32767));
        meUsuario.setMinimumSize(new java.awt.Dimension(150, 70));
        meUsuario.setPreferredSize(new java.awt.Dimension(150, 70));

        meitRol.setText("Roles de usuario");
        meitRol.setEnabled(false);
        meitRol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitRolActionPerformed(evt);
            }
        });
        meUsuario.add(meitRol);
        meUsuario.add(jSeparator7);

        jMenuItem18.setText("Cambiar contraseña");
        jMenuItem18.setEnabled(false);
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        meUsuario.add(jMenuItem18);

        jMenuBar1.add(meUsuario);

        meConfiguracion.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos50x50/IconoConfiguracion50.png"))); // NOI18N
        meConfiguracion.setText("CONFIGURACIÓN");
        meConfiguracion.setEnabled(false);
        meConfiguracion.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meConfiguracion.setPreferredSize(new java.awt.Dimension(180, 70));

        jMenuItem6.setText("Configuración principal");
        jMenuItem6.setEnabled(false);
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        meConfiguracion.add(jMenuItem6);
        meConfiguracion.add(jSeparator18);

        meitPerfil.setText("Perfiles");
        meitPerfil.setEnabled(false);
        meitPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitPerfilActionPerformed(evt);
            }
        });
        meConfiguracion.add(meitPerfil);
        meConfiguracion.add(jSeparator6);

        meitModulo.setText("Modulos");
        meitModulo.setEnabled(false);
        meitModulo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meitModuloActionPerformed(evt);
            }
        });
        meConfiguracion.add(meitModulo);

        jMenuBar1.add(meConfiguracion);

        meSalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/Iconos50x50/IconoSalir50.png"))); // NOI18N
        meSalir.setText("DESCONECTAR");
        meSalir.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        meSalir.setPreferredSize(new java.awt.Dimension(160, 70));

        jMenuItem19.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        jMenuItem19.setText("OK");
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        meSalir.add(jMenuItem19);

        jMenuBar1.add(meSalir);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(piPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 1360, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(piPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName("Principal");

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void meitModuloActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitModuloActionPerformed
        ABMModulo abmmodulos = new ABMModulo(this, true);
        abmmodulos.setLocationRelativeTo(this); //Centrar
        abmmodulos.setVisible(true);
    }//GEN-LAST:event_meitModuloActionPerformed

    private void meitPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitPerfilActionPerformed
        ABMPerfil abmperfil = new ABMPerfil(this, true);
        abmperfil.setLocationRelativeTo(this); //Centrar
        abmperfil.setVisible(true);
    }//GEN-LAST:event_meitPerfilActionPerformed


    private void meitRolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitRolActionPerformed
        ABMUsuarioRol abmusuariorol = new ABMUsuarioRol(this, true);
        abmusuariorol.setLocationRelativeTo(this); //Centrar
        abmusuariorol.setVisible(true);
    }//GEN-LAST:event_meitRolActionPerformed

    private void ObtenerFechayHora() {
        Date fecha = new Date();
        //Formateando la fecha:
        DateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        lbFecha.setText(formatoFecha.format(fecha));
        DateFormat formatoHora = new SimpleDateFormat("HH:mm:ss");
        lbHora.setText(formatoHora.format(fecha));
    }


    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        Login login = new Login();
        login.setVisible(true);
        dispose();
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        /*Configuracion conf = new Configuracion(this, true);
        conf.setLocationRelativeTo(this); //Centrar
        conf.setVisible(true);*/
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void btnClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClienteActionPerformed
        ABMCliente abmcliente = new ABMCliente(this, true);
        abmcliente.setLocationRelativeTo(this); //Centrar
        abmcliente.setVisible(true);
    }//GEN-LAST:event_btnClienteActionPerformed

    private void btnAplicacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAplicacionActionPerformed
        /*Aplicacion aplicacion = new Aplicacion(this, true);
        aplicacion.setLocationRelativeTo(this); //Centrar
        aplicacion.setVisible(true);*/
    }//GEN-LAST:event_btnAplicacionActionPerformed

    private void btnUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsuarioActionPerformed
        ABMUsuario abmusuario = new ABMUsuario(this, true);
        abmusuario.setLocationRelativeTo(this); //Centrar
        abmusuario.setVisible(true);
    }//GEN-LAST:event_btnUsuarioActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void btnProductoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductoActionPerformed
        ABMProducto abmproducto = new ABMProducto(this, false);
        abmproducto.setLocationRelativeTo(this); //Centrar
        abmproducto.setVisible(true);
    }//GEN-LAST:event_btnProductoActionPerformed

    private void meiReportePlanillaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meiReportePlanillaActionPerformed
        /*        ReportePlanilla reporteplanilla = new ReportePlanilla(this, true);
        reporteplanilla.setLocationRelativeTo(this);
        reporteplanilla.setVisible(true);*/
    }//GEN-LAST:event_meiReportePlanillaActionPerformed

    private void meitRegistrarVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitRegistrarVentaActionPerformed
        RegistrarVenta registrarventa = new RegistrarVenta(this, true);
        registrarventa.setLocationRelativeTo(this);
        registrarventa.setVisible(true);
    }//GEN-LAST:event_meitRegistrarVentaActionPerformed

    private void meitProveedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitProveedorActionPerformed
        ABMProveedor abmproveedor = new ABMProveedor(this, true);
        abmproveedor.setLocationRelativeTo(this); //Centrar
        abmproveedor.setVisible(true);
    }//GEN-LAST:event_meitProveedorActionPerformed

    private void meiCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meiCategoriaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_meiCategoriaActionPerformed

    private void meitRegistrarCompraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitRegistrarCompraActionPerformed
        RegistrarCompra registrarcompra = new RegistrarCompra(this, true);
        registrarcompra.setLocationRelativeTo(this);
        registrarcompra.setVisible(true);
    }//GEN-LAST:event_meitRegistrarCompraActionPerformed

    private void meitRegistrarActualizacionStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitRegistrarActualizacionStockActionPerformed
        RegistrarCompra registrarcompra = new RegistrarCompra(this, true);
        registrarcompra.setLocationRelativeTo(this);
        registrarcompra.setVisible(true);
    }//GEN-LAST:event_meitRegistrarActualizacionStockActionPerformed

    private void meitDeudaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meitDeudaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_meitDeudaActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Principal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    public void run() {
        Thread current = Thread.currentThread();
        while (current == hilo) {
            ObtenerFechayHora();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAplicacion;
    private javax.swing.JButton btnCliente;
    private javax.swing.JButton btnProducto;
    private javax.swing.JButton btnUsuario;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JLabel lbAlias;
    private javax.swing.JLabel lbFecha;
    private javax.swing.JLabel lbFechaTitulo;
    private javax.swing.JLabel lbHora;
    private javax.swing.JLabel lbHoraTitulo;
    private javax.swing.JLabel lblPerfil;
    private javax.swing.JMenu meCompra;
    private javax.swing.JMenu meConfiguracion;
    private javax.swing.JMenu meProducto;
    private javax.swing.JMenu meReporte;
    private javax.swing.JMenu meSalir;
    private javax.swing.JMenu meUsuario;
    private javax.swing.JMenu meVenta;
    private javax.swing.JMenuItem meiCategoria;
    private javax.swing.JMenuItem meiReportePlanilla;
    private javax.swing.JMenuItem meitDeuda;
    private javax.swing.JMenuItem meitModulo;
    private javax.swing.JMenuItem meitPerfil;
    private javax.swing.JMenuItem meitProveedor;
    private javax.swing.JMenuItem meitRegistrarActualizacionStock;
    private javax.swing.JMenuItem meitRegistrarCompra;
    private javax.swing.JMenuItem meitRegistrarVenta;
    private javax.swing.JMenuItem meitRol;
    private org.edisoncor.gui.panel.Panel panel1;
    private org.edisoncor.gui.panel.PanelImage panelImage1;
    private org.edisoncor.gui.panel.PanelImage panelImage2;
    private org.edisoncor.gui.panel.PanelImage panelImage3;
    private org.edisoncor.gui.panel.PanelImage panelImage4;
    private org.edisoncor.gui.panel.PanelImage piPrincipal;
    // End of variables declaration//GEN-END:variables

}
