/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import dao.DAO;
import static helpers.Metodos.log_historial;
import java.awt.Dialog;
import java.io.InputStream;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author Arnaldo_Cantero
 */
public class HelpersJasperReports {
        private DAO con = new DAO();
    
        public void GenerarReporteJTABLE(String rutajasper, Map parametros, TableModel elTableModel) {
        try {
            InputStream isRutajasper = Metodos.class.getResourceAsStream(rutajasper);
            if (isRutajasper == null) {
                JOptionPane.showMessageDialog(null, "Archivo jasper no encontrado: " + rutajasper, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                //Carga el archivo jasper
                JasperReport jrReporte = (JasperReport) JRLoader.loadObject(isRutajasper);
                //Carga el modelo de la tabla (Los titulos de la tabla deben coincidir con los fields del jasper)
                JasperPrint jprint;

                if (elTableModel != null) {
                    JRTableModelDataSource jrTableModel = new JRTableModelDataSource(elTableModel);
                    jprint = JasperFillManager.fillReport(jrReporte, parametros, jrTableModel);
                } else {  //Si el tablemodel viene null
                    jprint = JasperFillManager.fillReport(jrReporte, parametros, new JREmptyDataSource());
                }

                //JasperPrintManager.printPages(jprint, 1, 4, true); //Imprimir paginas especificas
                //JasperPrintManager.printReport(jprint, true); //Mandar directamente al dialogo de impresora, si es false imprime directo
                //Ver vista previa del reporte
                JasperViewer jViewer = new JasperViewer(jprint, false);//false para que al cerrar reporte no se cierre el sistema
                //jViewer.setTitle("Reporte de productos"); //Titulo de la ventana
                jViewer.setDefaultCloseOperation(JasperViewer.DISPOSE_ON_CLOSE);
                jViewer.setZoomRatio((float) 0.8); //1 es Zoom al 100%
                jViewer.setExtendedState(JasperViewer.MAXIMIZED_BOTH); //Maximizado
                jViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                jViewer.requestFocus();
                jViewer.setVisible(true);

                //Exportar a pdf
                //JasperExportManager.exportReportToPdfFile(jprint, "C:/ss.pdf"); 
            }
        } catch (JRException e) {
            log_historial.error("Error 1008: " + e);
            e.printStackTrace();
        } catch (JRRuntimeException e) {
            log_historial.error("Error 1085: " + e);
            e.printStackTrace();
        }
    }

    public void GenerarReporteSQL(String rutajasper, Map parametros, String consulta) {
        try {
            InputStream isRutajasper = Metodos.class.getResourceAsStream(rutajasper);
            if (isRutajasper == null) {
                JOptionPane.showMessageDialog(null, "Archivo jasper no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                //Carga el archivo jasper
                JasperReport jrReporte_productos = (JasperReport) JRLoader.loadObject(isRutajasper);

                //LOS TITULOS DE LA Consulta DEBEN COINCIDIR CON LOS FIELDS DEL JASPER
                con = con.ObtenerRSSentencia(consulta);
                JRResultSetDataSource rsLista = new JRResultSetDataSource(con.getResultSet()); //Para sql
                JasperPrint jprint = JasperFillManager.fillReport(jrReporte_productos, parametros, rsLista);

                JasperViewer jViewer = new JasperViewer(jprint, false);//false para que al cerrar reporte no se cierre el sistema
                //jViewer.setTitle("Reporte de productos"); //Titulo de la ventana
                jViewer.setDefaultCloseOperation(JasperViewer.DISPOSE_ON_CLOSE);
                jViewer.setZoomRatio((float) 0.8); //1 es Zoom al 100%
                jViewer.setExtendedState(JasperViewer.MAXIMIZED_BOTH); //Maximizado
                jViewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                jViewer.requestFocus();
                jViewer.setVisible(true);

                con.DesconectarBasedeDatos();
            }
        } catch (JRException e) {
            log_historial.error("Error 1009: consulta:" + consulta + ", rutajasper:" + rutajasper + ", Error:" + e);
            e.printStackTrace();
        } catch (NullPointerException e) {
            log_historial.error("Error 1071: consulta:" + consulta + ", rutajasper:" + rutajasper + ", Error:" + e);
            e.printStackTrace();
        }
    }

}
