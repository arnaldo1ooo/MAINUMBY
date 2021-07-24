package helpers;

import java.awt.*;
import java.beans.*;
import java.io.File;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class VistaPreviaEnFC extends JPanel
        implements PropertyChangeListener {

    private int ancho, largo;
    private ImageIcon icon;
    private Image image;
    private static final int ACCSIZE = 155;
    private final Color bg;

    public VistaPreviaEnFC() {
        setPreferredSize(new Dimension(ACCSIZE, -1));
        bg = getBackground();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();

        // Make sure we are responding to the right event.
        if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File selection = (File) e.getNewValue();
            String name;

            if (selection == null) {
                return;
            } else {
                name = selection.getAbsolutePath();
            }

            /*
             * Make reasonably sure we have an image format that AWT can
             * handle so we don't try to draw something silly.
             */
            if (name == null
                    || !name.toLowerCase().endsWith(".jpg")
                    && !name.toLowerCase().endsWith(".jpeg")
                    && !name.toLowerCase().endsWith(".gif")
                    && !name.toLowerCase().endsWith(".png")) {
            } else {
                icon = new ImageIcon(name);
                image = icon.getImage();
                scaleImagen();
                repaint();
            }
        }
    }

    private void scaleImagen() {
        ancho = image.getWidth(this);
        largo = image.getHeight(this);
        double ratio = 1.0;

        /* 
        * Determinar cómo escalar la imagen. Dado que el accesorio se puede expandir 
        * verticalmente, asegúrese de no ir más allá de 150 al escalar verticalmente.
         */
        if (ancho >= largo) {
            ratio = (double) (ACCSIZE - 5) / ancho;
            ancho = ACCSIZE - 5;
            largo = (int) (largo * ratio);
        } else {
            if (getHeight() > 150) {
                ratio = (double) (ACCSIZE - 5) / largo;
                largo = ACCSIZE - 5;
                ancho = (int) (ancho * ratio);
            } else {
                ratio = (double) getHeight() / largo;
                largo = getHeight();
                ancho = (int) (ancho * ratio);
            }
        }
        image = image.getScaledInstance(ancho, largo, Image.SCALE_DEFAULT);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(bg);

        /*
         * If we don't do this, we will end up with garbage from previous
         * images if they have larger sizes than the one we are currently
         * drawing. Also, it seems that the file list can paint outside
         * of its rectangle, and will cause odd behavior if we don't clear
         * or fill the rectangle for the accessory before drawing. This might
         * be a bug in JFileChooser.
         */
        g.fillRect(0, 0, ACCSIZE, getHeight());
        g.drawImage(image, getWidth() / 2 - ancho / 2 + 5,
                getHeight() / 2 - largo / 2, this);
    }

}
