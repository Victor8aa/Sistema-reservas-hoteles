package vistas;

import com.hotelReservations.datos.ConexionDB;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class DialogoReserva extends JDialog {
    private JPanel panelDialogoReserva;
    private JComboBox comboTipoHabitacion;
    private JComboBox comboHuesped;
    private JTextField txtHotelSeleccionado;
    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JTextField txtCantidad;
    private JButton btnConfirmar;
    private JButton btnCancelar;
    private JComboBox comboHotel;


    public DialogoReserva(java.awt.Window parent) {
        super(parent, "Crear Nueva Reserva", ModalityType.APPLICATION_MODAL);

        setContentPane(panelDialogoReserva);
        setSize(500, 450);
        setLocationRelativeTo(parent);

        txtCantidad.setText("1");

        cargarComboHuespedes();
        cargarComboHabitaciones();
        cargarComboHoteles();

        btnCancelar.addActionListener(e -> dispose());

        btnConfirmar.addActionListener(e -> confirmarReservaConTransaccion());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    }

    private void cargarComboHoteles() {
        Connection con = ConexionDB.getConexion();
        String sql = "SELECT hotel_id, hotel_name FROM hotels ORDER BY hotel_name";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String item = rs.getInt("hotel_id") + " - " + rs.getString("hotel_name");
                comboHotel.addItem(item);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cargarComboHuespedes() {
        Connection con = ConexionDB.getConexion();
        String sql = "SELECT guest_name FROM guests ORDER BY guest_name";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboHuesped.addItem(rs.getString("guest_name"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void cargarComboHabitaciones() {
        Connection con = ConexionDB.getConexion();
        String sql = "SELECT room_type_code FROM ref_room_types ORDER BY room_type_code";
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                comboTipoHabitacion.addItem(rs.getString("room_type_code"));
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void confirmarReservaConTransaccion() {
        if (comboHotel.getSelectedItem() == null || comboHuesped.getSelectedItem() == null) return;

        String nombreHuesped = (String) comboHuesped.getSelectedItem();
        String tipoHabitacion = (String) comboTipoHabitacion.getSelectedItem();
        String fechaDesde = txtFechaDesde.getText();
        String fechaHasta = txtFechaHasta.getText();
        int cantidad = Integer.parseInt(txtCantidad.getText());

        String hotelSeleccionado = (String) comboHotel.getSelectedItem();
        int hotelId = Integer.parseInt(hotelSeleccionado.split(" - ")[0]);

        Connection con = ConexionDB.getConexion();
        PreparedStatement psBooking = null, psRoomBooking = null, psGuestId = null;
        ResultSet rs = null;

        try {
            con.setAutoCommit(false);

            psGuestId = con.prepareStatement("SELECT guest_number FROM guests WHERE guest_name = ?");
            psGuestId.setString(1, nombreHuesped);
            rs = psGuestId.executeQuery();
            int guestId = rs.next() ? rs.getInt("guest_number") : -1;
            if (guestId == -1) throw new SQLException("Huésped no encontrado");

            psBooking = con.prepareStatement("INSERT INTO bookings (guest_number, date_from, date_to) VALUES (?, ?, ?) RETURNING booking_id");
            psBooking.setInt(1, guestId);
            psBooking.setDate(2, Date.valueOf(fechaDesde));
            psBooking.setDate(3, Date.valueOf(fechaHasta));
            rs = psBooking.executeQuery();

            int bookingId = rs.next() ? rs.getInt(1) : -1;

            psRoomBooking = con.prepareStatement("INSERT INTO room_bookings (booking_id, hotel_id, room_type_code, date_booking_from, date_booking_to, room_count) VALUES (?, ?, ?, ?, ?, ?)");
            psRoomBooking.setInt(1, bookingId);
            psRoomBooking.setInt(2, hotelId);
            psRoomBooking.setString(3, tipoHabitacion);
            psRoomBooking.setDate(4, Date.valueOf(fechaDesde));
            psRoomBooking.setDate(5, Date.valueOf(fechaHasta));
            psRoomBooking.setInt(6, cantidad);
            psRoomBooking.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "¡Reserva creada!");
            dispose();

        } catch (Exception e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}