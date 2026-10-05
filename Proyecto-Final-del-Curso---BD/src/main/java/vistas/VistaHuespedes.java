package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VistaHuespedes extends JFrame {

    private JTable tablaHuespedes;
    private JButton btnNuevoHuesped;
    private JTextField txtFiltro;
    private JPanel panelPrincipal;
    private JButton eliminarHButton;
    private JPanel barraHerramientas;
    private JScrollPane panelTabla;
    private JPanel panelBuscar;

    public VistaHuespedes() {
        setTitle("Gestión de Huéspedes");
        setContentPane(panelPrincipal);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { cargarDatosHuespedes(txtFiltro.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { cargarDatosHuespedes(txtFiltro.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { }
        });

        btnNuevoHuesped.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoNuevoHuesped();
            }
        });

        //Listener para Eliminar Huésped
        eliminarHButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filaSeleccionada = tablaHuespedes.getSelectedRow();

                if (filaSeleccionada == -1) {
                    JOptionPane.showMessageDialog(VistaHuespedes.this,
                            "Debe seleccionar un huésped de la lista para eliminarlo.",
                            "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // El ID está en la columna 0 del modelo de la tabla
                // Aseguramos que se convierta a entero correctamente
                Object valorId = tablaHuespedes.getValueAt(filaSeleccionada, 0);
                int idHuesped = Integer.parseInt(valorId.toString());

                int confirmacion = JOptionPane.showConfirmDialog(
                        VistaHuespedes.this,
                        "¿Está seguro de que desea eliminar el huésped con ID " + idHuesped + "?\nEsta acción no se puede deshacer.",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    eliminarHuespedEnBD(idHuesped);
                }
            }
        });

        btnNuevoHuesped.setBackground(new java.awt.Color(148, 211, 90));
        btnNuevoHuesped.setForeground(java.awt.Color.WHITE);


        eliminarHButton.setBackground(new java.awt.Color(210, 83, 78));
        eliminarHButton.setForeground(java.awt.Color.WHITE);

        cargarDatosHuespedes("");

        // este es el codigo para hacer padding en cada seccion

        try {
            // barraHerramientas (es donde estan botones)
            barraHerramientas.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 0, 20, 0));
        } catch (Exception e) {}

        // panelBuscar (esta el buscador)
        if (panelBuscar != null) {
            panelBuscar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));
        }

        // panelTabla (esta la tabla)
        try {
            panelTabla.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 50, 30, 50));
        } catch (Exception e) {

        }
    }

    private void cargarDatosHuespedes(String filtro) {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nombre Completo");
        modelo.addColumn("Ciudad");
        modelo.addColumn("Dirección");

        Connection con = ConexionDB.getConexion();
        String sql = "SELECT * FROM guests WHERE guest_name ILIKE ? OR guest_city ILIKE ? ORDER BY guest_name";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String busqueda = "%" + filtro + "%";
            ps.setString(1, busqueda);
            ps.setString(2, busqueda);

            ResultSet rs = ps.executeQuery();
            modelo.setRowCount(0);

            while (rs.next()) {
                Object[] fila = new Object[4];
                fila[0] = rs.getInt("guest_number");
                fila[1] = rs.getString("guest_name");
                fila[2] = rs.getString("guest_city");
                fila[3] = rs.getString("guest_address");
                modelo.addRow(fila);
            }

            tablaHuespedes.setModel(modelo);
            tablaBonita2();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    //registra los datos del huesped
    private void mostrarDialogoNuevoHuesped() {
        JTextField nombreField = new JTextField();
        JTextField ciudadField = new JTextField();
        JTextField direccionField = new JTextField();

        Object[] mensaje = {
                "Nombre Completo:", nombreField,
                "Ciudad:", ciudadField,
                "Dirección:", direccionField
        };

        int opcion = JOptionPane.showConfirmDialog(this, mensaje, "Registrar Nuevo Huésped", JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {
            if (nombreField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            guardarHuespedEnBD(nombreField.getText(), ciudadField.getText(), direccionField.getText());
        }
    }

   //inserta al huesped en la base de datos
    private void guardarHuespedEnBD(String nombre, String ciudad, String direccion) {
        Connection con = ConexionDB.getConexion();
        String sql = "INSERT INTO guests (guest_name, guest_city, guest_address) VALUES (?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, ciudad.isEmpty() ? "Desconocido" : ciudad);
            ps.setString(3, direccion);

            int afectados = ps.executeUpdate();

            if (afectados > 0) {
                JOptionPane.showMessageDialog(this, "Huésped registrado exitosamente.");
                cargarDatosHuespedes("");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  Eliminar Huésped
    private void eliminarHuespedEnBD(int idHuesped) {
        Connection con = ConexionDB.getConexion();
        String sql = "DELETE FROM guests WHERE guest_number = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idHuesped);

            int afectados = ps.executeUpdate();

            if (afectados > 0) {
                JOptionPane.showMessageDialog(this, "Huésped eliminado exitosamente.");
                cargarDatosHuespedes(""); // Recargar la tabla para ver los cambios
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el huésped.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            // Manejar la restricción de llave foránea (si el huésped tiene reservas)
            if (ex.getSQLState() != null && ex.getSQLState().equals("23503")) {
                JOptionPane.showMessageDialog(this, "No se puede eliminar el huésped porque tiene reservas activas o historial.", "Error de Integridad", JOptionPane.ERROR_MESSAGE);
            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    public void tablaBonita2() {

        // centrar
        javax.swing.table.DefaultTableCellRenderer centro = new javax.swing.table.DefaultTableCellRenderer();
        centro.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        // centrar solo ID
        // ID es la posicion 0
        tablaHuespedes.getColumnModel().getColumn(0).setCellRenderer(centro);

        javax.swing.table.JTableHeader header = tablaHuespedes.getTableHeader();

        // color de fondo del encabezado
        header.setBackground(new java.awt.Color(33, 37, 41)); //color gris obscuro

        // color blanco letra
        header.setForeground(java.awt.Color.WHITE);

        // fuente letra color negro
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

        // quitar el borde :)
        header.setOpaque(false);
    }
}
