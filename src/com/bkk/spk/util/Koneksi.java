package com.bkk.spk.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton koneksi database MySQL.
 * Dipanggil dari class DAO manapun yang butuh akses ke database.
 */
public class Koneksi {

    private static final String URL = "jdbc:mysql://localhost:3306/db_spk_bkk?useSSL=false&serverTimezone=Asia/Jakarta&zeroDateTimeBehavior=convertToNull";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // default XAMPP kosong, ganti kalau kamu set password

    private static Connection connection = null;

    // Constructor private -> cegah instansiasi langsung
    private Koneksi() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Koneksi database berhasil.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver MySQL tidak ditemukan. Cek apakah JAR sudah ditambahkan ke Libraries.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Gagal konek ke database. Cek apakah MySQL/XAMPP sudah running.");
            e.printStackTrace();
        }
        return connection;
    }

    // Testing manual: klik kanan file ini -> Run File
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("Status: Terhubung ke database db_spk_bkk");
        } else {
            System.out.println("Status: Gagal terhubung");
        }
    }
}
