package com.bkk.spk.dao;

import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel tb_lowongan.
 * Berbeda dari SiswaDAO/KriteriaDAO: setiap query SELECT butuh JOIN ke tb_perusahaan
 * karena Model Lowongan punya field object Perusahaan, bukan cuma id.
 */
public class LowonganDAO {

    private static final String SELECT_BASE =
        "SELECT l.*, p.nama_perusahaan, p.alamat, p.bidang_industri " +
        "FROM tb_lowongan l " +
        "JOIN tb_perusahaan p ON l.id_perusahaan = p.id_perusahaan";

    public boolean insert(Lowongan lowongan) {
        String sql = "INSERT INTO tb_lowongan (id_perusahaan, posisi, deskripsi, kuota, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lowongan.getPerusahaan().getIdPerusahaan());
            ps.setString(2, lowongan.getPosisi());
            ps.setString(3, lowongan.getDeskripsi());
            ps.setInt(4, lowongan.getKuota());
            ps.setString(5, lowongan.getStatus());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert lowongan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Lowongan> getAll() {
        List<Lowongan> daftarLowongan = new ArrayList<>();
        String sql = SELECT_BASE + " ORDER BY l.id_lowongan DESC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarLowongan.add(mapResultSetToLowongan(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data lowongan: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarLowongan;
    }

    // Hanya lowongan yang statusnya masih BUKA -> dipakai di form pemilihan lowongan saat proses seleksi
    public List<Lowongan> getAllBuka() {
        List<Lowongan> daftarLowongan = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE l.status = 'BUKA' ORDER BY l.id_lowongan DESC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarLowongan.add(mapResultSetToLowongan(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data lowongan buka: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarLowongan;
    }

    public Lowongan getById(int idLowongan) {
        String sql = SELECT_BASE + " WHERE l.id_lowongan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToLowongan(rs);
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil lowongan id " + idLowongan + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(Lowongan lowongan) {
        String sql = "UPDATE tb_lowongan SET id_perusahaan=?, posisi=?, deskripsi=?, kuota=?, status=? WHERE id_lowongan=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lowongan.getPerusahaan().getIdPerusahaan());
            ps.setString(2, lowongan.getPosisi());
            ps.setString(3, lowongan.getDeskripsi());
            ps.setInt(4, lowongan.getKuota());
            ps.setString(5, lowongan.getStatus());
            ps.setInt(6, lowongan.getIdLowongan());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update lowongan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idLowongan) {
        String sql = "DELETE FROM tb_lowongan WHERE id_lowongan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus lowongan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Mapping hasil JOIN -> pisahkan dulu jadi object Perusahaan, baru bungkus ke Lowongan
    private Lowongan mapResultSetToLowongan(ResultSet rs) throws SQLException {
        Perusahaan perusahaan = new Perusahaan();
        perusahaan.setIdPerusahaan(rs.getInt("id_perusahaan"));
        perusahaan.setNamaPerusahaan(rs.getString("nama_perusahaan"));
        perusahaan.setAlamat(rs.getString("alamat"));
        perusahaan.setBidangIndustri(rs.getString("bidang_industri"));

        Lowongan lowongan = new Lowongan();
        lowongan.setIdLowongan(rs.getInt("id_lowongan"));
        lowongan.setPerusahaan(perusahaan);
        lowongan.setPosisi(rs.getString("posisi"));
        lowongan.setDeskripsi(rs.getString("deskripsi"));
        lowongan.setKuota(rs.getInt("kuota"));
        lowongan.setStatus(rs.getString("status"));
        return lowongan;
    }
}
