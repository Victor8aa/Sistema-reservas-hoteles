package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VistaReportes extends JFrame {

    private JPanel panelReportes;
    private JTabbedPane tabbedPane;
    private JTable tablaReservas;
    private JTable tablaDistribucion;
    private JTable tablaTarifas;

    public VistaReportes() {
        setTitle("Informes de Gestión Hotelera");
        setContentPane(panelReportes);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        cargarDatosReporte1();
        cargarDatosReporte2();
        cargarDatosReporte3();

        tablaReportes(tablaReservas);
        tablaReportes(tablaDistribucion);
        tablaReportes(tablaTarifas);
    }

    private void cargarDatosReporte1() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        modelo.setColumnIdentifiers(new String[]{"Hotel", "Tipo Habitación", "Total Reservas", "Total Noches"});
        Connection con = ConexionDB.getConexion();
        if (con == null) return;

        String sql = "SELECT h.hotel_name, rb.room_type_code, COUNT(rb.booking_id) AS \"Total Reservas\", " +
                "SUM(rb.date_booking_to - rb.date_booking_from) AS \"Total Noches Reservadas\" " +
                "FROM room_bookings rb JOIN hotels h ON rb.hotel_id = h.hotel_id " +
                "GROUP BY h.hotel_name, rb.room_type_code " +
                "ORDER BY \"Total Reservas\" DESC, h.hotel_name";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Long totalReservas = rs.getObject("Total Reservas", Long.class);
                Long totalNoches = rs.getObject("Total Noches Reservadas", Long.class);

                modelo.addRow(new Object[]{
                        rs.getString("hotel_name"),
                        rs.getString("room_type_code"),
                        totalReservas == null ? 0 : totalReservas.intValue(),
                        totalNoches == null ? 0 : totalNoches.intValue()
                });
            }
            tablaReservas.setModel(modelo);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar Informe 1: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosReporte2() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        modelo.setColumnIdentifiers(new String[]{"Código", "Descripción", "Total Usos"});
        Connection con = ConexionDB.getConexion();
        if (con == null) return;

        String sql = "SELECT r.room_type_code, r.room_type_description, COUNT(rb.room_type_code) AS \"Total Usos\" " +
                "FROM ref_room_types r LEFT JOIN room_bookings rb ON r.room_type_code = rb.room_type_code " +
                "GROUP BY r.room_type_code, r.room_type_description " +
                "ORDER BY \"Total Usos\" DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Long totalUsos = rs.getObject("Total Usos", Long.class);
                modelo.addRow(new Object[]{
                        rs.getString("room_type_code"),
                        rs.getString("room_type_description"),
                        totalUsos == null ? 0 : totalUsos.intValue()
                });
            }
            tablaDistribucion.setModel(modelo);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar Informe 2: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosReporte3() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        modelo.setColumnIdentifiers(new String[]{"Código Hab.", "Descripción", "Tarifa por Período", "Período"});
        Connection con = ConexionDB.getConexion();
        if (con == null) return;

        String sql = "SELECT rrt.room_type_code, rrt.room_type_description, prr.room_rate, rrp.rate_period_description " +
                "FROM period_room_rates prr " +
                "JOIN ref_room_types rrt ON prr.room_type_code = rrt.room_type_code " +
                "JOIN room_rate_periods rrp ON prr.rate_period_code = rrp.rate_period_code " +
                "ORDER BY rrt.room_type_code, prr.room_rate DESC";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getString("room_type_code"),
                        rs.getString("room_type_description"),
                        String.format("$%,.2f", rs.getDouble("room_rate")),
                        rs.getString("rate_period_description")
                });
            }
            tablaTarifas.setModel(modelo);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar Informe 3. Verifique tablas de tarifas: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void tablaReportes(JTable tabla) {

        javax.swing.table.JTableHeader header = tabla.getTableHeader();
        header.setBackground(new java.awt.Color(33, 37, 41));
        header.setForeground(java.awt.Color.WHITE);
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        header.setOpaque(false);

        javax.swing.table.DefaultTableCellRenderer centro = new javax.swing.table.DefaultTableCellRenderer();
        centro.setHorizontalAlignment(javax.swing.JLabel.CENTER);

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centro);
        }

        tabla.setShowGrid(true);
        tabla.setGridColor(new java.awt.Color(200, 200, 200));
    }
}