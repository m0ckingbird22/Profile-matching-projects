package com.bkk.spk.dao;

import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.model.ProfilIdeal;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel tb_profil_ideal.
 * Menyimpan nilai target tiap kriteria, per lowongan (bukan global).
 */
public class ProfilIdealDAO {

    private static final String SELECT_BASE =
        "SELECT pi.*, " +
        "  l.posisi, l.deskripsi, l.kuota, l.status, " +
        "  p.id_perusahaan, p.nama_perusahaan, p.alamat, p.bidang_industri, " +
        "  k.kode_kriteria, k.nama_kriteria, k.jenis_faktor " +
        "FROM tb_profil_ideal pi " +
        "JOIN tb_lowongan l ON pi.id_lowongan = l.id_lowongan " +
        "JOIN tb_perusahaan p ON l.id_perusahaan = p.id_perusahaan " +
        "JOIN tb_kriteria k ON pi.id_kriteria = k.id_kriteria";

    public boolean insert(ProfilIdeal profilIdeal) {
        String sql = "INSERT INTO tb_profil_ideal (id_lowongan, id_kriteria, nilai_target) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profilIdeal.getLowongan().getIdLowongan());
            ps.setInt(2, profilIdeal.getKriteria().getIdKriteria());
            ps.setDouble(3, profilIdeal.getNilaiTarget());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert profil ideal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Ambil semua profil ideal untuk 1 lowongan tertentu -> ini yang paling sering dipakai
    public List<ProfilIdeal> getByLowongan(int idLowongan) {
        List<ProfilIdeal> daftar = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE pi.id_lowongan = ? ORDER BY k.kode_kriteria ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftar.add(mapResultSetToProfilIdeal(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil profil ideal lowongan " + idLowongan + ": " + e.getMessage());
            e.printStackTrace();
        }
        return daftar;
    }

    public boolean update(ProfilIdeal profilIdeal) {
        String sql = "UPDATE tb_profil_ideal SET nilai_target=? WHERE id_profil_ideal=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, profilIdeal.getNilaiTarget());
            ps.setInt(2, profilIdeal.getIdProfilIdeal());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update profil ideal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idProfilIdeal) {
        String sql = "DELETE FROM tb_profil_ideal WHERE id_profil_ideal = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProfilIdeal);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus profil ideal: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private ProfilIdeal mapResultSetToProfilIdeal(ResultSet rs) throws SQLException {
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

        Kriteria kriteria = new Kriteria();
        kriteria.setIdKriteria(rs.getInt("id_kriteria"));
        kriteria.setKodeKriteria(rs.getString("kode_kriteria"));
        kriteria.setNamaKriteria(rs.getString("nama_kriteria"));
        kriteria.setJenisFaktor(rs.getString("jenis_faktor"));

        ProfilIdeal profilIdeal = new ProfilIdeal();
        profilIdeal.setIdProfilIdeal(rs.getInt("id_profil_ideal"));
        profilIdeal.setLowongan(lowongan);
        profilIdeal.setKriteria(kriteria);
        profilIdeal.setNilaiTarget(rs.getDouble("nilai_target"));
        return profilIdeal;
    }
}
