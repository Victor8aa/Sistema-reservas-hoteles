package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class VistaLogin extends JFrame{
    private JTextField txtHost;
    private JTextField txtPuerto;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnConectar;
    private JButton btnSalir;
    private JPanel panelLogin;
    private JPanel panelBotones;

    public VistaLogin() {
        setTitle("Conexión a Base de Datos");
        setContentPane(panelLogin);
        setSize(400, 250);
        setLocationRelativeTo(null);

        // Conectar: Botón Verde
        btnConectar.setBackground(new java.awt.Color(117, 177, 211));
        btnConectar.setForeground(java.awt.Color.WHITE);

        // Salir: Botón Rojo
        btnSalir.setBackground(new java.awt.Color(220, 53, 69));
        btnSalir.setForeground(java.awt.Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.getRootPane().setDefaultButton(btnConectar);

        btnSalir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        btnConectar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = txtHost.getText();
                String puerto = txtPuerto.getText();
                String usuario = txtUsuario.getText();

                String password = new String(txtPassword.getPassword());

                boolean exito = ConexionDB.probarConexion(host, puerto, usuario, password);

                if (exito) {

                    JOptionPane.showMessageDialog(
                            panelLogin,
                            "¡Conexión exitosa!",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);

                    dispose();

                    VistaHoteles ventanaHoteles = new VistaHoteles();
                    ventanaHoteles.setVisible(true);

                } else {

                    JOptionPane.showMessageDialog(
                            panelLogin, // El panel padre
                            "Error en la conexión. Verifique sus credenciales.", // Mensaje
                            "Error de Conexión", // Título
                            JOptionPane.ERROR_MESSAGE); // Icono de error
                }
            }
        });
    }
}
