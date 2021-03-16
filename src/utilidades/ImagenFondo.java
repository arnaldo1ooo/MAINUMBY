package utilidades;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

public class ImagenFondo implements Border {

    public BufferedImage back;

    public ImagenFondo() {
        try {
            URL imagePath = new URL(getClass().getResource("/principal/iconos/fondo1.png").toString());
            back = ImageIO.read(imagePath);
        } catch (Exception ex) {   
            JOptionPane.showMessageDialog(null, "Error","No se encontro la ruta del fondo de pantalla",JOptionPane.ERROR_MESSAGE);
        }
    }
    int PosicionX;
    int PosicionY;
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        PosicionX = 0;//x + (width - back.getWidth()) / 2;
        PosicionY = y + (height - back.getHeight()) / 2;
        g.drawImage(back, (PosicionX), (PosicionY), null);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
    }

    public boolean isBorderOpaque() {
        return false;
    }

}
