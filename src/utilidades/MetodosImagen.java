/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilidades;

import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class MetodosImagen {

    JFileChooser fc;
    Boolean fcEstaCargado = false;

    public void CargarImagenFC(JLabel ElLabelImagen) throws HeadlessException {
        CambiarLookSwing("windows"); //Cambiamos el look a Windows

        //Traducir
        UIManager.put("FileChooser.saveButtonText", "Guardar");
        UIManager.put("FileChooser.openButtonText", "Abrir");
        UIManager.put("FileChooser.cancelButtonText", "Cancelar");
        UIManager.put("FileChooser.updateButtonText", "Actualizar");
        UIManager.put("FileChooser.helpButtonText", "Ayuda");
        UIManager.put("FileChooser.saveButtonToolTipText", "Guardar fichero");

        String userDir = System.getProperty("user.home"); //Directorio
        //JFileChooser fc = new JDirectoryChooser(); //Para directorios
        fc = new JFileChooser(userDir + "/Desktop"); //Para archivos

        //Vista previa de imagenes del Fc
        VistaPreviaEnFC vistapreviaenfc = new VistaPreviaEnFC(); //File Chooser FC
        fc.setAccessory(vistapreviaenfc);
        fc.addPropertyChangeListener(vistapreviaenfc);

        fc.setDialogTitle("Buscar imagen o foto"); //El titulo de la ventana buscador
        fc.setFileFilter(new FileNameExtensionFilter("JPG", "jpg"));
        fc.setFileFilter(new FileNameExtensionFilter("PNG", "png"));
        fc.setFileFilter(new FileNameExtensionFilter("JPG & PNG", "jpg", "png"));

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            EscalarImagen(ElLabelImagen, fc, null);
            ElLabelImagen.setText("");
            fcEstaCargado = true;
        } else {
            System.out.println("Cargar Imagen Cancelado");
            fcEstaCargado = false;
        }
        System.out.println("CargarImagenFC esta cargado " + fcEstaCargado);
        CambiarLookSwing("nimbus"); //Cambiamos el look a Nimbus otra vez
    }

    public void GuardarImagen(String rutadestinoimagen) {
        //Guardar nuevo imagen
        //rutadestinoimagen = System.getProperty("user.dir") + rutadestinoimagen;
        try {
            if (fcEstaCargado == true) { //Si la FileChooser tiene cargado un file
                BufferedImage biImagen = ImageIO.read(fc.getSelectedFile());
                //Obtener extension
                String filenombre = fc.getSelectedFile().getName();
                String fileextension = filenombre.substring(filenombre.lastIndexOf(".") + 1, fc.getSelectedFile().getName().length());

                ImageIcon icon = new ImageIcon(biImagen); //Convierte un BufferedImage a ImageIcon
                Graphics2D g2 = biImagen.createGraphics();
                g2.drawImage(icon.getImage(), 0, 0, icon.getImageObserver());
                g2.dispose();
                // Escribe la imagen
                try {
                    EliminarImagen(rutadestinoimagen + "." + fileextension); //Elimina la imagen por si ya existe, sucede en el caso de modificar imagen
                    System.out.println("Guardando imagen... " + rutadestinoimagen + "." + fileextension);
                    ImageIO.write(biImagen, fileextension, new File(rutadestinoimagen + "." + fileextension));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error al guardar imagen del producto... " + ex, "Error", JOptionPane.ERROR_MESSAGE);
   
                }
            }
        } catch (HeadlessException | IOException e) {
            System.out.println("Error al Guardar Imagen del registro" + e);
        }
    }

    public boolean LeerImagen(JLabel ElLabel, String rutaimagen) {
        rutaimagen = System.getProperty("user.dir") + rutaimagen;
        //ObtenerImagen Escalado al Label
        String ruta;
        File ficheroimagen;

        ruta = rutaimagen + ".png";
        ficheroimagen = new File(ruta);
        if (ficheroimagen.exists()) {
            ElLabel.setText("");
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen: " + ruta);
            return true;
        } else {
            System.out.println("Error al LeerImagen, La imagen solicitada no existe o la ruta esta mal, revise la extension o ruta: " + ficheroimagen.getAbsolutePath());
        }

        ruta = rutaimagen + ".jpg";
        ficheroimagen = new File(ruta);
        if (ficheroimagen.exists()) {
            ElLabel.setText("");
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen: " + ruta);
            return true;
        } else {
            System.out.println("Error al LeerImagen, La imagen solicitada no existe o la ruta esta mal, revise la extension o ruta: " + ficheroimagen.getAbsolutePath());
        }
        return false;
    }

    public void EliminarImagen(String rutaimagen) {
        rutaimagen = System.getProperty("user.dir") + rutaimagen;
        String ruta;
        ruta = rutaimagen + ".png";
        File fichero = new File(ruta);
        if (fichero.exists() == false) {
            ruta = rutaimagen + ".jpg";
            fichero = new File(ruta);
        }
        if (fichero.exists()) { //Si fichero existe
            try {
                if (fichero.delete()) { //Eliminar imagen
                    System.out.println("La imagen ha sido borrado satisfactoriamente");
                }
            } catch (Exception e) {
                System.out.println("Error al querer eliminar imagen " + e);
            }
        }
    }

    public void EscalarImagen(JLabel ElLabel, JFileChooser fc, String UrlImagen) {
        if (fc != null) { //Si la imagen viene desde un File Chooser
            //Si se presiona boton aceptar

            //Escala la imagen al Jlabel sin perder la proporcion
            ImageIcon tmpImagen = new ImageIcon(fc.getSelectedFile().toString());
            float delta = ((ElLabel.getWidth() * 100) / tmpImagen.getIconWidth()) / 100f;
            if (tmpImagen.getIconHeight() > ElLabel.getHeight()) {
                delta = ((ElLabel.getHeight() * 100) / tmpImagen.getIconHeight()) / 100f;
            }
            int ancho = (int) (tmpImagen.getIconWidth() * delta);
            int alto = (int) (tmpImagen.getIconHeight() * delta);
            ElLabel.setIcon(new ImageIcon(tmpImagen.getImage().getScaledInstance(ancho, alto, Image.SCALE_AREA_AVERAGING)));
        } else { //Si la imagen viene desde una URL
            //Escala la imagen al Jlabel sin perder la proporcion
            ImageIcon imicImagen = new ImageIcon(UrlImagen);
            float delta = ((ElLabel.getWidth() * 100) / imicImagen.getIconWidth()) / 100f;
            if (imicImagen.getIconHeight() > ElLabel.getHeight()) {
                delta = ((ElLabel.getHeight() * 100) / imicImagen.getIconHeight()) / 100f;
            }
            int ancho = (int) (imicImagen.getIconWidth() * delta);
            int alto = (int) (imicImagen.getIconHeight() * delta);
            ElLabel.setIcon(new ImageIcon(imicImagen.getImage().getScaledInstance(ancho, alto, Image.SCALE_AREA_AVERAGING)));
        }
    }

    public void CambiarLookSwing(String look) {
        if (look.equals("windows")) {
            look = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        }
        if (look.equals("nimbus")) {
            look = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
        }
        try {
            javax.swing.UIManager.setLookAndFeel(look);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {

        }
    }
}
