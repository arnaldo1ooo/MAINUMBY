/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vista.venta;

import dao.DAO;
import helpers.HelpersComboBox;
import helpers.HelpersJasperReports;
import helpers.HelpersTable;
import helpers.HelpersTextField;
import java.awt.Toolkit;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ReporteVentas extends javax.swing.JDialog {
    private HelpersComboBox helpersComboBox = new HelpersComboBox();
    private HelpersTextField helpersTextField = new HelpersTextField();
    private HelpersTable helpersTable = new HelpersTable();
    private HelpersJasperReports helpersJasperReports = new HelpersJasperReports();
    private DAO con = new DAO();
    private DefaultTableModel modelTablePagos;
    private static org.apache.log4j.Logger log_historial = org.apache.log4j.Logger.getLogger(ReporteVentas.class.getName());

    public ReporteVentas(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        CargarComboBoxes();

        lblTituloFecha.setVisible(false);

        //Obtener Fechas
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1); //Primer dia del mes seleccionado
        dcDesde.setCalendar(cal);
        
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.getActualMaximum(Calendar.DAY_OF_MONTH)); //Ultimo dia del mes seleccionado
        dcHasta.setCalendar(cal);
    }

    //--------------------------METODOS----------------------------//
    private void CargarComboBoxes() {
        //Carga los combobox con las consultas
        helpersComboBox.CargarComboConsulta(cbConcepto, "SELECT con_codigo, con_descripcion FROM concepto ORDER BY con_descripcion", 1);
        //cbConcepto.addItem(new MetodosCombo(-1, "TODOS"));
        cbConcepto.setMaximumRowCount(cbConcepto.getModel().getSize()); //Tamaño del combo
        cbConcepto.setSelectedItem("TODOS");
    }

    private void ConsultaPagos() {
        modelTablePagos = (DefaultTableModel) tbPrincipal.getModel();//Cargamos campos de jtable al modeltable
        modelTablePagos.setRowCount(0); //Vacia la tabla

        //Carga el combobox con los titulos de la tabla, solo si esta vacio
        if (cbOrdenar != null && cbOrdenar.getItemCount() == 0) {
            for (int i = 0; i < tbPrincipal.getColumnCount(); i++) {
                cbOrdenar.addItem(tbPrincipal.getColumnName(i));
            }
            cbOrdenar.setSelectedIndex(0);
        }
        SimpleDateFormat formatofecha = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatofecha2 = new SimpleDateFormat("dd/MM/yyyy");
        String sentencia;
        if (cbConcepto.getSelectedItem().toString().equals("TODOS")) {
            sentencia = "CALL SP_ReportePagos('" + formatofecha.format(dcDesde.getDate()) + "','" + formatofecha.format(dcHasta.getDate()) + "','-1')";
        } else {
            sentencia = "CALL SP_ReportePagos('" + formatofecha.format(dcDesde.getDate()) + "','" + formatofecha.format(dcHasta.getDate()) + "','"
                    +helpersComboBox.ObtenerIDSelectCombo(cbConcepto) + "')";
        }

        try {
            con = con.ObtenerRSSentencia(sentencia);
            String numpago, fechapago, concepto;
            double monto, total = 0;
            while (con.getResultSet().next()) {
                numpago = con.getResultSet().getString("pag_numpago");
                concepto = con.getResultSet().getString("con_descripcion");
                fechapago = formatofecha2.format(con.getResultSet().getDate("pag_fechapago"));
                monto = con.getResultSet().getDouble("totalpago");
                total = total + monto;
                modelTablePagos.addRow(new Object[]{numpago, concepto, fechapago, monto});

                lblTotal.setText(helpersTextField.DoubleAFormatSudamerica(total) + " Gs.");
            }
        } catch (SQLException e) {
            log_historial.error("Error 10218: " + e);
            e.printStackTrace();
        }
        con.DesconectarBasedeDatos();
        tbPrincipal.setModel(modelTablePagos);
        helpersTable.AnchuraColumna(tbPrincipal);
        helpersTable.OrdenarColumna(tbPrincipal, cbOrdenar.getSelectedIndex());
        lbCantRegistros.setText(tbPrincipal.getRowCount() + " Registros encontrados");

        String[] meses = {"Ene.", "Feb.", "Mar.", "Abr.", "May.", "Jun.", "Jul.", "Ago.", "Sep.", "Oct.", "Nov.", "Dic"}; // primero creas el String array
        // la inicializas en idioma español, país México:
        DateFormatSymbols simbols = new DateFormatSymbols(new Locale("es", "MX"));  // con MX es sep, con ES (España) es sept.
        simbols.setShortMonths(meses);// lo configuras así, después de inicializar symbols:
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", simbols);
        lblTituloFecha.setVisible(true);
        lblTituloFecha.setText("Pagos del " + dateFormat.format(dcDesde.getDate()) + " al " + dateFormat.format(dcHasta.getDate()));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpPrincipal = new javax.swing.JPanel();
        panel3 = new org.edisoncor.gui.panel.Panel();
        labelMetric2 = new org.edisoncor.gui.label.LabelMetric();
        panel1 = new org.edisoncor.gui.panel.Panel();
        lblFechaPago = new javax.swing.JLabel();
        lblHasta = new javax.swing.JLabel();
        lblDesde = new javax.swing.JLabel();
        dcDesde = new com.toedter.calendar.JDateChooser();
        dcHasta = new com.toedter.calendar.JDateChooser();
        btnFiltrar = new org.edisoncor.gui.button.ButtonSeven();
        cbConcepto = new javax.swing.JComboBox<>();
        lblConcepto = new javax.swing.JLabel();
        panel4 = new org.edisoncor.gui.panel.Panel();
        lblBuscarCampo = new javax.swing.JLabel();
        cbOrdenar = new javax.swing.JComboBox();
        scPrincipal = new javax.swing.JScrollPane();
        tbPrincipal = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        lbCantRegistros = new javax.swing.JLabel();
        lblTituloFecha = new javax.swing.JLabel();
        lbCantRegistros1 = new javax.swing.JLabel();
        lblTotal = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnGenerarReporte = new org.edisoncor.gui.button.ButtonSeven();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ventana reporte de pagos");

        jpPrincipal.setBackground(new java.awt.Color(233, 255, 255));
        jpPrincipal.setPreferredSize(new java.awt.Dimension(1580, 478));

        panel3.setColorPrimario(new java.awt.Color(0, 153, 153));
        panel3.setColorSecundario(new java.awt.Color(233, 255, 255));

        labelMetric2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelMetric2.setText("REPORTE DE VENTAS");
        labelMetric2.setDireccionDeSombra(110);
        labelMetric2.setFont(new java.awt.Font("Cooper Black", 0, 22)); // NOI18N

        javax.swing.GroupLayout panel3Layout = new javax.swing.GroupLayout(panel3);
        panel3.setLayout(panel3Layout);
        panel3Layout.setHorizontalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel3Layout.setVerticalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelMetric2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(7, Short.MAX_VALUE))
        );

        panel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panel1.setColorPrimario(new java.awt.Color(233, 255, 255));
        panel1.setColorSecundario(new java.awt.Color(255, 255, 255));

        lblFechaPago.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        lblFechaPago.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblFechaPago.setText("Fecha de pago");

        lblHasta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblHasta.setText("Hasta");

        lblDesde.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblDesde.setText("Desde");

        btnFiltrar.setBackground(new java.awt.Color(0, 153, 153));
        btnFiltrar.setText("Filtrar");
        btnFiltrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFiltrarActionPerformed(evt);
            }
        });

        cbConcepto.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbConceptoItemStateChanged(evt);
            }
        });

        lblConcepto.setFont(new java.awt.Font("sansserif", 0, 14)); // NOI18N
        lblConcepto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblConcepto.setText("Concepto");

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblFechaPago)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblDesde, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(lblHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblConcepto)
                .addGap(2, 2, 2)
                .addComponent(cbConcepto, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(298, 298, 298)
                .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFechaPago, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblConcepto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(cbConcepto, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dcDesde, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(dcHasta, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnFiltrar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panel4.setColorPrimario(new java.awt.Color(255, 255, 255));
        panel4.setColorSecundario(new java.awt.Color(233, 255, 255));

        lblBuscarCampo.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblBuscarCampo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblBuscarCampo.setText("Ordenar por:");

        cbOrdenar.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbOrdenarItemStateChanged(evt);
            }
        });

        scPrincipal.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        tbPrincipal.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tbPrincipal.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        tbPrincipal.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "N° de pago", "Concepto", "Fecha de pago", "Monto"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tbPrincipal.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tbPrincipal.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tbPrincipal.setEnabled(false);
        tbPrincipal.setGridColor(new java.awt.Color(0, 153, 204));
        tbPrincipal.setOpaque(false);
        tbPrincipal.setRowHeight(20);
        tbPrincipal.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbPrincipal.getTableHeader().setReorderingAllowed(false);
        tbPrincipal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tbPrincipalMousePressed(evt);
            }
        });
        tbPrincipal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbPrincipalKeyReleased(evt);
            }
        });
        scPrincipal.setViewportView(tbPrincipal);

        lbCantRegistros.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lbCantRegistros.setForeground(new java.awt.Color(153, 153, 0));
        lbCantRegistros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbCantRegistros.setText("0 Registros encontrados");
        lbCantRegistros.setPreferredSize(new java.awt.Dimension(57, 25));

        lblTituloFecha.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        lblTituloFecha.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTituloFecha.setText("Pagos del xx/xx/xx al xx/xx/xx");

        lbCantRegistros1.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        lbCantRegistros1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lbCantRegistros1.setText("TOTAL:");
        lbCantRegistros1.setPreferredSize(new java.awt.Dimension(57, 25));

        lblTotal.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTotal.setText("0 Gs.");
        lblTotal.setPreferredSize(new java.awt.Dimension(57, 25));

        javax.swing.GroupLayout panel4Layout = new javax.swing.GroupLayout(panel4);
        panel4.setLayout(panel4Layout);
        panel4Layout.setHorizontalGroup(
            panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                        .addComponent(lbCantRegistros1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(lblTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbCantRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scPrincipal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 727, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel4Layout.createSequentialGroup()
                                .addComponent(lblBuscarCampo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbOrdenar, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panel4Layout.createSequentialGroup()
                        .addComponent(lblTituloFecha, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panel4Layout.setVerticalGroup(
            panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbOrdenar, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBuscarCampo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblTituloFecha, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addComponent(scPrincipal, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbCantRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbCantRegistros1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );

        jLabel11.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel11.setText("FILTRAR POR:");

        btnGenerarReporte.setBackground(new java.awt.Color(0, 153, 153));
        btnGenerarReporte.setText("Generar reporte");
        btnGenerarReporte.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGenerarReporteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jpPrincipalLayout = new javax.swing.GroupLayout(jpPrincipal);
        jpPrincipal.setLayout(jpPrincipalLayout);
        jpPrincipalLayout.setHorizontalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jpPrincipalLayout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpPrincipalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGenerarReporte, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(308, 308, 308))
        );
        jpPrincipalLayout.setVerticalGroup(
            jpPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpPrincipalLayout.createSequentialGroup()
                .addComponent(panel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnGenerarReporte, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 609, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGenerarReporteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporteActionPerformed
        if (tbPrincipal.getRowCount() <= 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "No existe ningún registro en la tabla", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map parametros;
        String rutajasper;
        InputStream logo = this.getClass().getResourceAsStream("/reportes/images/logo_ace.jpg");
        InputStream logo2 = this.getClass().getResourceAsStream("/reportes/images/logo_sanroque.png");

        //Parametros
        parametros = new HashMap();
        parametros.clear();
        parametros.put("LOGO", logo);
        parametros.put("LOGO2", logo2);
        parametros.put("FECHADESDEHASTA", "(" + lblTituloFecha.getText() + ")");

        parametros.put("ORDENADOPOR", cbOrdenar.getSelectedItem() + "");
        parametros.put("CONCEPTO", cbConcepto.getSelectedItem().toString());
        String[] totalsplit = lblTotal.getText().split(" ");
        parametros.put("TOTAL", totalsplit[0] + " Gs");
        parametros.put("CANTREGISTROS", lbCantRegistros.getText());

        rutajasper = "/reportes/reporte_pagos.jasper";

        //Crea una tabla auxiliar en donde se carga los registros ordenados por cierta en la tabla principal
        JTable tableAuxiliar = new JTable(); //tabla auxiliar
        DefaultTableModel tablemodelAuxiliar = new DefaultTableModel(); //tablemodel de la tabla auxiliar
        tablemodelAuxiliar.setRowCount(0); // Vaciar filas del modelo
        //Establecer el número y el nombre de las columnas en el tablemodel auxiliar
        for (int i = 0; i < tbPrincipal.getColumnCount(); i++) {
            tablemodelAuxiliar.addColumn(tbPrincipal.getColumnName(i));
        }
        //Cargamos los registros ordenados al modelo auxiliar
        for (int f = 0; f < tbPrincipal.getRowCount(); f++) {
            Object fila[] = new Object[tbPrincipal.getColumnCount()];
            for (int c = 0; c < tbPrincipal.getColumnCount(); c++) {
                if (tbPrincipal.getColumnName(c).equals("Monto")) {
                    fila[c] = helpersTextField.DoubleAFormatSudamerica(Double.parseDouble(tbPrincipal.getValueAt(f, c) + ""));
                } else {
                    fila[c] = tbPrincipal.getValueAt(f, c);
                }
            }
            tablemodelAuxiliar.addRow(fila);
        }
        tableAuxiliar.setModel(tablemodelAuxiliar); // Asignamos el tablemodel auxiliar a la tabla auxiliar

        helpersJasperReports.GenerarReporteJTABLE(rutajasper, parametros, tableAuxiliar.getModel());
    }//GEN-LAST:event_btnGenerarReporteActionPerformed

    private void cbOrdenarItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbOrdenarItemStateChanged
        helpersTable.OrdenarColumna(tbPrincipal, cbOrdenar.getSelectedIndex());
    }//GEN-LAST:event_cbOrdenarItemStateChanged

    private void tbPrincipalMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbPrincipalMousePressed

    }//GEN-LAST:event_tbPrincipalMousePressed

    private void tbPrincipalKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbPrincipalKeyReleased

    }//GEN-LAST:event_tbPrincipalKeyReleased

    private void btnFiltrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFiltrarActionPerformed
        if (dcHasta.getDate().after(dcDesde.getDate())) {
            ConsultaPagos();
        } else {
            JOptionPane.showMessageDialog(this, "La fecha DESDE no puede ser mayor a la fecha HASTA", "Advertencia", JOptionPane.WARNING_MESSAGE);
            dcDesde.requestFocus();
        }
    }//GEN-LAST:event_btnFiltrarActionPerformed

    private void cbConceptoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbConceptoItemStateChanged
        modelTablePagos = (DefaultTableModel) tbPrincipal.getModel();
        modelTablePagos.setRowCount(0);
    }//GEN-LAST:event_cbConceptoItemStateChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException e) {
            log_historial.error("Error 10221: " + e);
            e.printStackTrace();
        }

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                ReporteVentas dialog = new ReporteVentas(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.edisoncor.gui.button.ButtonSeven btnFiltrar;
    private org.edisoncor.gui.button.ButtonSeven btnGenerarReporte;
    private javax.swing.JComboBox<HelpersComboBox> cbConcepto;
    private javax.swing.JComboBox cbOrdenar;
    private com.toedter.calendar.JDateChooser dcDesde;
    private com.toedter.calendar.JDateChooser dcHasta;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JPanel jpPrincipal;
    private org.edisoncor.gui.label.LabelMetric labelMetric2;
    private javax.swing.JLabel lbCantRegistros;
    private javax.swing.JLabel lbCantRegistros1;
    private javax.swing.JLabel lblBuscarCampo;
    private javax.swing.JLabel lblConcepto;
    private javax.swing.JLabel lblDesde;
    private javax.swing.JLabel lblFechaPago;
    private javax.swing.JLabel lblHasta;
    private javax.swing.JLabel lblTituloFecha;
    private javax.swing.JLabel lblTotal;
    private org.edisoncor.gui.panel.Panel panel1;
    private org.edisoncor.gui.panel.Panel panel3;
    private org.edisoncor.gui.panel.Panel panel4;
    private javax.swing.JScrollPane scPrincipal;
    private javax.swing.JTable tbPrincipal;
    // End of variables declaration//GEN-END:variables
}
