package vistas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // decirle a Java que pondre bordes personalizados
        // barra de Flatlat (para poder personalizar)
        JFrame.setDefaultLookAndFeelDecorated(true);

        // agrandar letra
        UIManager.put("TitlePane.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 16));

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {

        }

        // ...
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo cargar el diseño. Se usará el por defecto.");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                VistaLogin ventanaLogin = new VistaLogin();
                ventanaLogin.pack();
                ventanaLogin.setLocationRelativeTo(null);
                ventanaLogin.setVisible(true);
            }
        });
    }
}
