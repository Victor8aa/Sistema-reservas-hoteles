package com.hotelReservations.datos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static Connection conexion = null;

    private static final String DB_NAME = "hotel_reservations";

    public static boolean probarConexion(String host, String puerto, String usuario, String password) {

        String url = "jdbc:postgresql://" + host + ":" + puerto + "/" + DB_NAME;

        try {
            Connection connPrueba = DriverManager.getConnection(url, usuario, password);

            conexion = connPrueba;
            return true;

        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos.");
            e.printStackTrace();
            return false;
        }
    }


    public static Connection getConexion() {
        try {

            if (conexion == null || conexion.isClosed()) {
                System.err.println("Error: La conexión no ha sido establecida por el Login.");
                return null; // O lanzar una excepción
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return conexion;
    }


    public static void cerrarConexion() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
