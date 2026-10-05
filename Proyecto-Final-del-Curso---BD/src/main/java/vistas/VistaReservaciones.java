package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VistaReservaciones extends JFrame {
    private JPanel panelReservas;
    private JTable tablaReservas;
    private JButton btnRefrescar;
    private JButton btnNuevaReserva;
    private JButton btnCancelar;

    public VistaReservaciones() {
        setTitle("Historial de Reservaciones");
        setContentPane(panelReservas);
        setSize(900, 600);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //cierra solo la ventana no la app entera


        btnNuevaReserva.setBackground(new java.awt.Color(148, 211, 90));
        btnNuevaReserva.setForeground(java.awt.Color.WHITE);

        btnCancelar.setBackground(new java.awt.Color(210, 83, 78));
        btnCancelar.setForeground(java.awt.Color.WHITE);

        btnRefrescar.setBackground(new java.awt.Color(117, 177, 211));
        btnRefrescar.setForeground(java.awt.Color.WHITE);


        btnRefrescar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarDatosReservaciones();
            }
        });

        btnNuevaReserva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DialogoReserva dialogo = new DialogoReserva(VistaReservaciones.this);
                dialogo.setVisible(true);

                cargarDatosReservaciones();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int fila = tablaReservas.getSelectedRow();
                if (fila == -1) {
                    JOptionPane.showMessageDialog(VistaReservaciones.this, "Seleccione una reserva para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int bookingId = (int) tablaReservas.getValueAt(fila, 0);

                int confirm = JOptionPane.showConfirmDialog(VistaReservaciones.this,
                        "¿Está seguro de cancelar la reserva #" + bookingId + "?\nEsta acción no se puede deshacer.",
                        "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) return;

                Connection con = null;
                PreparedStatement psRoom = null;
                PreparedStatement psBooking = null;

                try {
                    con = ConexionDB.getConexion();
                    con.setAutoCommit(false);


                    String sqlRoom = "DELETE FROM room_bookings WHERE booking_id = ?";
                    psRoom = con.prepareStatement(sqlRoom);
                    psRoom.setInt(1, bookingId);
                    psRoom.executeUpdate();

                    String sqlBooking = "DELETE FROM bookings WHERE booking_id = ?";
                    psBooking = con.prepareStatement(sqlBooking);
                    psBooking.setInt(1, bookingId);
                    int afectados = psBooking.executeUpdate();

                    if (afectados > 0) {
                        con.commit();
                        JOptionPane.showMessageDialog(VistaReservaciones.this, "Reserva cancelada exitosamente.");
                        cargarDatosReservaciones();
                    } else {
                        con.rollback();
                        JOptionPane.showMessageDialog(VistaReservaciones.this, "No se encontró la reserva.", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SQLException ex) {
                    try {
                        if (con != null) con.rollback();
                    } catch (SQLException exRollback) { exRollback.printStackTrace(); }

                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(VistaReservaciones.this, "Error crítico al cancelar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        if (con != null) con.setAutoCommit(true);
                        if (psRoom != null) psRoom.close();
                        if (psBooking != null) psBooking.close();
                    } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        });

        cargarDatosReservaciones();
    }

   //Reporte
    private void cargarDatosReservaciones() {
        DefaultTableModel modelo = new DefaultTableModel();
        modelo.addColumn("ID Reserva");
        modelo.addColumn("Huésped");
        modelo.addColumn("Hotel");
        modelo.addColumn("Habitación");
        modelo.addColumn("Desde");
        modelo.addColumn("Hasta");
        modelo.addColumn("Noches");

        Connection con = ConexionDB.getConexion();
        PreparedStatement ps = null;
        ResultSet rs = null;

        // --- ¡LA CONSULTA MÁGICA (JOIN)! ---
        String sql = "SELECT " +
                "    b.booking_id, " +
                "    g.guest_name, " +
                "    h.hotel_name, " +
                "    rb.room_type_code, " +
                "    b.date_from, " +
                "    b.date_to, " +
                "    (b.date_to - b.date_from) AS noches " +
                "FROM bookings b " +
                "JOIN guests g ON b.guest_number = g.guest_number " +
                "JOIN room_bookings rb ON b.booking_id = rb.booking_id " +
                "JOIN hotels h ON rb.hotel_id = h.hotel_id " +
                "ORDER BY b.date_from DESC"; // muestra las más nuevas primero

        try {
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            modelo.setRowCount(0);

            while (rs.next()) {
                Object[] fila = new Object[7];
                fila[0] = rs.getInt("booking_id");
                fila[1] = rs.getString("guest_name");
                fila[2] = rs.getString("hotel_name");
                fila[3] = rs.getString("room_type_code");
                fila[4] = rs.getDate("date_from").toString();
                fila[5] = rs.getDate("date_to").toString();
                fila[6] = rs.getInt("noches");

                modelo.addRow(fila);
            }

            tablaReservas.setModel(modelo);
            tablaBonita1();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar las reservaciones", "Error SQL", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void tablaBonita1() {

        // centrar
        javax.swing.table.DefaultTableCellRenderer centro = new javax.swing.table.DefaultTableCellRenderer();
        centro.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        // centrar solo ID
        // ID es la posicion 0
        tablaReservas.getColumnModel().getColumn(0).setCellRenderer(centro);

        javax.swing.table.JTableHeader header = tablaReservas.getTableHeader();

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
