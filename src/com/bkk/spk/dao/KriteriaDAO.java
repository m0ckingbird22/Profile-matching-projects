package com.bkk.spk.dao;

import com.bkk.spk.model.Kriteria;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) untuk tabel tb_kriteria.
 */
public class KriteriaDAO {

    // CREATE - tambah kriteria baru
    public boolean insert(Kriteria kriteria) {
        String sql = "INSERT INTO tb_kriteria (kode_kriteria, nama_kriteria, jenis_faktor) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kriteria.getKodeKriteria());
            ps.setString(2, kriteria.getNamaKriteria());
            ps.setString(3, kriteria.getJenisFaktor());

            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert kriteria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // READ - ambil semua data kriteria
    public List<Kriteria> getAll() {
        List<Kriteria> daftarKriteria = new ArrayList<>();
        String sql = "SELECT * FROM tb_kriteria ORDER BY kode_kriteria ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarKriteria.add(mapResultSetToKriteria(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data kriteria: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarKriteria;
    }

    // READ - ambil satu kriteria berdasarkan id
    public Kriteria getById(int idKriteria) {
        String sql = "SELECT * FROM tb_kriteria WHERE id_kriteria = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKriteria);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKriteria(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil kriteria id " + idKriteria + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - ubah data kriteria yang sudah ada
    public boolean update(Kriteria kriteria) {
        String sql = "UPDATE tb_kriteria SET kode_kriteria=?, nama_kriteria=?, jenis_faktor=? WHERE id_kriteria=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kriteria.getKodeKriteria());
            ps.setString(2, kriteria.getNamaKriteria());
            ps.setString(3, kriteria.getJenisFaktor());
            ps.setInt(4, kriteria.getIdKriteria());

            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update kriteria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE - hapus kriteria berdasarkan id
    public boolean delete(int idKriteria) {
        String sql = "DELETE FROM tb_kriteria WHERE id_kriteria = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKriteria);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            // Kemungkinan gagal krn FK constraint (kriteria ini sudah dipakai di nilai_siswa/profil_ideal)
            System.err.println("Gagal hapus kriteria: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper privat -> mapping ResultSet ke object Kriteria
    private Kriteria mapResultSetToKriteria(ResultSet rs) throws SQLException {
        Kriteria kriteria = new Kriteria();
        kriteria.setIdKriteria(rs.getInt("id_kriteria"));
        kriteria.setKodeKriteria(rs.getString("kode_kriteria"));
        kriteria.setNamaKriteria(rs.getString("nama_kriteria"));
        kriteria.setJenisFaktor(rs.getString("jenis_faktor"));
        return kriteria;
    }
}
