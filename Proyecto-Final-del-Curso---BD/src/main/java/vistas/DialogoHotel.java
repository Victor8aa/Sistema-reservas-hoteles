package vistas;

import com.hotelReservations.datos.ConexionDB;

import javax.swing.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DialogoHotel extends JDialog {
    private JPanel panelDialogo;
    private JTextField txtCodigo;
    private JTextField txtDireccion;
    private JTextField txtCiudad;
    private JTextField txtUrl;
    private JComboBox comboCadena;
    private JComboBox comboPais;
    private JComboBox comboEstrellas;
    private JTextField txtNombre;
    private JButton btnGuardar;
    private JButton btnCancelar;

    private Integer hotelIdParaEditar = null;

    public DialogoHotel(JFrame parent) {
        super(parent, "Nuevo Hotel", true);

        setContentPane(panelDialogo);
        setSize(500, 450);
        setLocationRelativeTo(parent);

        cargarCombos();

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarHotel();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void cargarCombos() {
        Connection con = ConexionDB.getConexion();

        try {
            // Cargar Cadenas (hotel_chains)
            PreparedStatement psCadenas = con.prepareStatement("SELECT hotel_chain_code FROM hotel_chains");
            ResultSet rsCadenas = psCadenas.executeQuery();
            while (rsCadenas.next()) {
                comboCadena.addItem(rsCadenas.getString("hotel_chain_code"));
            }

            // Cargar Países (ref_countries)
            PreparedStatement psPaises = con.prepareStatement("SELECT country_code FROM ref_countries");
            ResultSet rsPaises = psPaises.executeQuery();
            while (rsPaises.next()) {
                comboPais.addItem(rsPaises.getString("country_code"));
            }

            // Cargar Estrellas (ref_star_ratings)
            PreparedStatement psEstrellas = con.prepareStatement("SELECT star_rating_id, star_rating_code FROM ref_star_ratings");
            ResultSet rsEstrellas = psEstrellas.executeQuery();
            while (rsEstrellas.next()) {
                comboEstrellas.addItem(rsEstrellas.getString("star_rating_code"));
            }

            rsCadenas.close();
            psCadenas.close();
            rsPaises.close();
            psPaises.close();
            rsEstrellas.close();
            psEstrellas.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los datos iniciales.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarHotel() {
        if (txtNombre.getText().isEmpty() || txtCodigo.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El Nombre y el Código son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Connection con = ConexionDB.getConexion();
        PreparedStatement ps = null;
        String sql; // La consulta SQL cambiará

        String cadenaSel = (String) comboCadena.getSelectedItem();
        String paisSel = (String) comboPais.getSelectedItem();
        String estrellaSel = (String) comboEstrellas.getSelectedItem();

        try {
            con = ConexionDB.getConexion();

            if (hotelIdParaEditar == null) {
                sql = "INSERT INTO hotels (hotel_chain_code, country_code, star_rating_id, hotel_code, hotel_name, hotel_address, hotel_city, hotel_url) " +
                        "VALUES (?, ?, (SELECT star_rating_id FROM ref_star_ratings WHERE star_rating_code = ?), ?, ?, ?, ?, ?)";

                ps = con.prepareStatement(sql);

                ps.setString(1, cadenaSel);
                ps.setString(2, paisSel);
                ps.setString(3, estrellaSel);
                ps.setString(4, txtCodigo.getText());
                ps.setString(5, txtNombre.getText());
                ps.setString(6, txtDireccion.getText());
                ps.setString(7, txtCiudad.getText());
                ps.setString(8, txtUrl.getText());

            } else {
                // 2. MODO EDICIÓN (UPDATE)
                sql = "UPDATE hotels SET " +
                        "  hotel_chain_code = ?, " +
                        "  country_code = ?, " +
                        "  star_rating_id = (SELECT star_rating_id FROM ref_star_ratings WHERE star_rating_code = ?), " +
                        "  hotel_code = ?, " +
                        "  hotel_name = ?, " +
                        "  hotel_address = ?, " +
                        "  hotel_city = ?, " +
                        "  hotel_url = ? " +
                        "WHERE hotel_id = ?";

                ps = con.prepareStatement(sql);

                ps.setString(1, cadenaSel);
                ps.setString(2, paisSel);
                ps.setString(3, estrellaSel);
                ps.setString(4, txtCodigo.getText());
                ps.setString(5, txtNombre.getText());
                ps.setString(6, txtDireccion.getText());
                ps.setString(7, txtCiudad.getText());
                ps.setString(8, txtUrl.getText());
                ps.setInt(9, hotelIdParaEditar);
            }

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Hotel guardado exitosamente.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo guardar el hotel.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public DialogoHotel(JFrame parent, int hotelId) {
        this(parent);

        setTitle("Modificar Hotel");
        this.hotelIdParaEditar = hotelId;

        cargarDatosDelHotel(hotelId);
    }


    private void cargarDatosDelHotel(int hotelId) {
        Connection con = ConexionDB.getConexion();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sql = "SELECT " +
                "    h.hotel_code, h.hotel_name, h.hotel_address, h.hotel_city, h.hotel_url, " +
                "    hc.hotel_chain_code, " +
                "    rc.country_code, " +
                "    rs.star_rating_code " +
                "FROM hotels h " +
                "LEFT JOIN hotel_chains hc ON h.hotel_chain_code = hc.hotel_chain_code " +
                "LEFT JOIN ref_countries rc ON h.country_code = rc.country_code " +
                "LEFT JOIN ref_star_ratings rs ON h.star_rating_id = rs.star_rating_id " +
                "WHERE h.hotel_id = ?";

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, hotelId);
            rs = ps.executeQuery();

            if (rs.next()) {
                // Rellenamos los campos de texto
                txtNombre.setText(rs.getString("hotel_name"));
                txtCodigo.setText(rs.getString("hotel_code"));
                txtDireccion.setText(rs.getString("hotel_address"));
                txtCiudad.setText(rs.getString("hotel_city"));
                txtUrl.setText(rs.getString("hotel_url"));

                // Rellenamos los JComboBox
                comboCadena.setSelectedItem(rs.getString("hotel_chain_code"));
                comboPais.setSelectedItem(rs.getString("country_code"));
                comboEstrellas.setSelectedItem(rs.getString("star_rating_code"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los datos del hotel.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}
