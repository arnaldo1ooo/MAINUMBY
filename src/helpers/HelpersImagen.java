/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class HelpersImagen {

    public File CargarImagenFC(JLabel ElLabelImagen) throws HeadlessException {
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
        JFileChooser fc = new JFileChooser(userDir + "/Desktop"); //Para archivos

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
        } else {
            System.out.println("Cargar Imagen Cancelado");
        }
        CambiarLookSwing("nimbus"); //Cambiamos el look a Nimbus otra vez
        return fc.getSelectedFile();
    }

    public void GuardarImagen(String rutaDestinoImagen, File elFichero) {
        //Guardar nuevo imagen
        //rutaDestinoImagen = System.getProperty("user.dir") + rutaDestinoImagen; Para Guardar imagen interna
        try {
            if (elFichero != null) { //Si el fichero no es vacio
                BufferedImage biImagen;
                ImageIcon icon;
                Graphics2D g2;
                String ficheroNombre, ficheroExtension;
                biImagen = ImageIO.read(elFichero);
                ficheroNombre = elFichero.getName();
                ficheroExtension = ficheroNombre.substring(ficheroNombre.lastIndexOf(".") + 1, ficheroNombre.length()); //Obtener extension

                icon = new ImageIcon(biImagen); //Convierte un BufferedImage a ImageIcon
                g2 = biImagen.createGraphics();
                g2.drawImage(icon.getImage(), 0, 0, icon.getImageObserver());
                g2.dispose();

                // Escribe la imagen
                EliminarImagen(rutaDestinoImagen + "." + ficheroExtension); //Elimina la imagen por si ya existe, sucede en el caso de modificar imagen
                System.out.println("Guardando imagen... " + rutaDestinoImagen + "." + ficheroExtension);
                ImageIO.write(biImagen, ficheroExtension, new File(rutaDestinoImagen + "." + ficheroExtension)); //Guarda la imagen
            }
        } catch (HeadlessException | IOException e) {
            System.out.println("Error al Guardar Imagen del registro" + e);
            e.printStackTrace();
        }
    }

    public boolean LeerImagen(JLabel ElLabel, String rutaImagen, String rutaPorDefault) {
        String ruta;
        File fileImagen;
        ElLabel.setText("");

        //Probar si es ruta interna PNG
        ruta = System.getProperty("user.dir") + rutaImagen + ".png";
        fileImagen = new File(ruta);
        if (fileImagen.exists()) {
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen: " + ruta);
            return true;
        }

        //Probar si es ruta interna JPG
        ruta = System.getProperty("user.dir") + rutaImagen + ".jpg";
        fileImagen = new File(ruta);
        if (fileImagen.exists()) {
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen: " + ruta);
            return true;
        }

        //Probar si es ruta externa PNG
        ruta = rutaImagen + ".png";
        fileImagen = new File(ruta);
        if (fileImagen.exists()) {
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen externa: " + ruta);
            return true;
        }

        //Probar si es ruta externa JPG
        ruta = rutaImagen + ".jpg";
        fileImagen = new File(ruta);
        if (fileImagen.exists()) {
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen externa: " + ruta);
            return true;
        }

        //Cargar imagen por Default
        ruta = System.getProperty("user.dir") + rutaPorDefault;
        fileImagen = new File(ruta);
        if (fileImagen.exists()) {
            EscalarImagen(ElLabel, null, ruta);
            System.out.println("Se cargó la imagen por Default: " + ruta);
            return false;
        } else {
            System.out.println("No se encontró imagen por defecto: " + rutaPorDefault);
        }

        System.out.println("No se encontró LeerImagen: " + rutaImagen);
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
