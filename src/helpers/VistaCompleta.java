/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package helpers;

/**
 *
 * @author Lic. Arnaldo Cantero
 */
public class VistaCompleta extends javax.swing.JDialog {

    /**
     * Creates new form VistaCompleta
     *
     * @param rutaImagen
     * @param rutaImagenDefecto
     */
    public VistaCompleta(String rutaImagen, String rutaImagenDefecto) {
        initComponents();

        MetodosImagen metodosimagen = new MetodosImagen();
        metodosimagen.LeerImagen(lbImagen, rutaImagen, rutaImagenDefecto);
        System.out.println("Se cargo la imagen a la VistaCompleta " + rutaImagen);

        //this.setSize(this.getToolkit().getScreenSize());
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lbImagen = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Vista Completa");
        setAlwaysOnTop(true);
        setModal(true);
        setName("dgVistaCompleta"); // NOI18N
        setType(java.awt.Window.Type.POPUP);

        lbImagen.setFont(new java.awt.Font("sansserif", 1, 24)); // NOI18N
        lbImagen.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbImagen.setText("SIN IMAGEN");
        jScrollPane1.setViewportView(lbImagen);
        lbImagen.getAccessibleContext().setAccessibleName("");
        lbImagen.getAccessibleContext().setAccessibleDescription("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 947, Short.MAX_VALUE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @SuppressWarnings("override")
            public void run() {
                VistaCompleta dialog = new VistaCompleta(null, null);

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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbImagen;
    // End of variables declaration//GEN-END:variables
}
