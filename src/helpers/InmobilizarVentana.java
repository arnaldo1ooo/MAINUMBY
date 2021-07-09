/*
 * Evita que el form se pueda mover en la pantalla
 * se lo invoca con
 * 
 */
package utilidades;

import javax.swing.JInternalFrame;

/**
 *
 * @author Arnaldo Cantero
 */
public class InmobilizarVentana extends JInternalFrame {

    private boolean inmobilizar = false;

    public void reshape(int x, int y, int width, int height) {
        if (!inmobilizar) {
            super.reshape(x, y, width, height);
        }
    }

    public boolean isInmobilizar() {
        return inmobilizar;
    }

    public void setInmobilizar(boolean inmobilizar) {
        this.inmobilizar = inmobilizar;
    }
}
