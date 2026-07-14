package com.bkk.spk.dao;

import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PerusahaanDAO {

    public boolean insert(Perusahaan perusahaan) {
        String sql = "INSERT INTO tb_perusahaan (nama_perusahaan, alamat, bidang_industri) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, perusahaan.getNamaPerusahaan());
            ps.setString(2, perusahaan.getAlamat());
            ps.setString(3, perusahaan.getBidangIndustri());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert perusahaan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Perusahaan> getAll() {
        List<Perusahaan> daftarPerusahaan = new ArrayList<>();
        String sql = "SELECT * FROM tb_perusahaan ORDER BY nama_perusahaan ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarPerusahaan.add(mapResultSetToPerusahaan(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data perusahaan: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarPerusahaan;
    }

    public Perusahaan getById(int idPerusahaan) {
        String sql = "SELECT * FROM tb_perusahaan WHERE id_perusahaan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPerusahaan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToPerusahaan(rs);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil perusahaan id " + idPerusahaan + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Perusahaan perusahaan) {
        String sql = "UPDATE tb_perusahaan SET nama_perusahaan=?, alamat=?, bidang_industri=? WHERE id_perusahaan=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, perusahaan.getNamaPerusahaan());
            ps.setString(2, perusahaan.getAlamat());
            ps.setString(3, perusahaan.getBidangIndustri());
            ps.setInt(4, perusahaan.getIdPerusahaan());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update perusahaan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idPerusahaan) {
        String sql = "DELETE FROM tb_perusahaan WHERE id_perusahaan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idPerusahaan);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Gagal kalau perusahaan ini masih punya lowongan aktif (FK constraint)
            System.err.println("Gagal hapus perusahaan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Perusahaan mapResultSetToPerusahaan(ResultSet rs) throws SQLException {
        Perusahaan perusahaan = new Perusahaan();
        perusahaan.setIdPerusahaan(rs.getInt("id_perusahaan"));
        perusahaan.setNamaPerusahaan(rs.getString("nama_perusahaan"));
        perusahaan.setAlamat(rs.getString("alamat"));
        perusahaan.setBidangIndustri(rs.getString("bidang_industri"));
        return perusahaan;
    }
}
