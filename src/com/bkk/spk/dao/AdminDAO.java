package com.bkk.spk.dao;

import com.bkk.spk.model.Admin;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    public boolean insert(Admin admin) {
        String sql = "INSERT INTO tb_admin (username, password, nama) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPassword()); // pastikan sudah di-hash BCrypt sebelum masuk sini
            ps.setString(3, admin.getNama());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Admin> getAll() {
        List<Admin> daftarAdmin = new ArrayList<>();
        String sql = "SELECT * FROM tb_admin ORDER BY nama ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarAdmin.add(mapResultSetToAdmin(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data admin: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarAdmin;
    }

    public Admin getById(int idAdmin) {
        String sql = "SELECT * FROM tb_admin WHERE id_admin = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAdmin);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAdmin(rs);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil admin id " + idAdmin + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Dipakai khusus untuk proses LOGIN -> cari admin berdasarkan username
    public Admin getByUsername(String username) {
        String sql = "SELECT * FROM tb_admin WHERE username = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToAdmin(rs);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil admin username " + username + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Admin admin) {
        String sql = "UPDATE tb_admin SET username=?, password=?, nama=? WHERE id_admin=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, admin.getUsername());
            ps.setString(2, admin.getPassword());
            ps.setString(3, admin.getNama());
            ps.setInt(4, admin.getIdAdmin());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idAdmin) {
        String sql = "DELETE FROM tb_admin WHERE id_admin = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idAdmin);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setIdAdmin(rs.getInt("id_admin"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setNama(rs.getString("nama"));
        return admin;
    }
}
