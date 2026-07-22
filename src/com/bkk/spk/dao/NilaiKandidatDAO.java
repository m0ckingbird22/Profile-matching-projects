package com.bkk.spk.dao;

import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiKandidat;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel tb_nilai_kandidat.
 * Menyimpan nilai asli (bukan gap) tiap kandidat per kriteria.
 */
public class NilaiKandidatDAO {

    private static final String SELECT_BASE =
        "SELECT nk.*, " +
        "  knd.nisn, knd.nama, knd.tahun_lulus, " +
        "  k.kode_kriteria, k.nama_kriteria, k.jenis_faktor " +
        "FROM tb_nilai_kandidat nk " +
        "JOIN tb_kandidat knd ON nk.id_kandidat = knd.id_kandidat " +
        "JOIN tb_kriteria k ON nk.id_kriteria = k.id_kriteria";

    public boolean insert(NilaiKandidat nilaiKandidat) {
        String sql = "INSERT INTO tb_nilai_kandidat (id_kandidat, id_kriteria, nilai_kandidat) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nilaiKandidat.getKandidat().getIdKandidat());
            ps.setInt(2, nilaiKandidat.getKriteria().getIdKriteria());
            ps.setDouble(3, nilaiKandidat.getNilaiKandidat());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert nilai kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Insert banyak nilai sekaligus (1 kandidat, semua kriteria) dalam SATU transaksi.
    public boolean insertBatch(List<NilaiKandidat> daftarNilai) {
        String sql = "INSERT INTO tb_nilai_kandidat (id_kandidat, id_kriteria, nilai_kandidat) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (NilaiKandidat nilai : daftarNilai) {
                    ps.setInt(1, nilai.getKandidat().getIdKandidat());
                    ps.setInt(2, nilai.getKriteria().getIdKriteria());
                    ps.setDouble(3, nilai.getNilaiKandidat());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Gagal insert batch nilai kandidat, rollback: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Gagal membuka koneksi untuk batch insert: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Ambil semua nilai milik satu kandidat -> dipakai saat mulai proses Profile Matching
    public List<NilaiKandidat> getByKandidat(int idKandidat) {
        List<NilaiKandidat> daftar = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE nk.id_kandidat = ? ORDER BY k.kode_kriteria ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKandidat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftar.add(mapResultSetToNilaiKandidat(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil nilai kandidat " + idKandidat + ": " + e.getMessage());
            e.printStackTrace();
        }
        return daftar;
    }

    public boolean update(NilaiKandidat nilaiKandidat) {
        String sql = "UPDATE tb_nilai_kandidat SET nilai_kandidat=? WHERE id_nilai=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, nilaiKandidat.getNilaiKandidat());
            ps.setInt(2, nilaiKandidat.getIdNilai());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update nilai kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idNilai) {
        String sql = "DELETE FROM tb_nilai_kandidat WHERE id_nilai = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idNilai);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus nilai kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private NilaiKandidat mapResultSetToNilaiKandidat(ResultSet rs) throws SQLException {
        Kandidat kandidat = new Kandidat();
        kandidat.setIdKandidat(rs.getInt("id_kandidat"));
        kandidat.setNisn(rs.getString("nisn"));
        kandidat.setNama(rs.getString("nama"));
        kandidat.setTahunLulus(rs.getInt("tahun_lulus"));

        Kriteria kriteria = new Kriteria();
        kriteria.setIdKriteria(rs.getInt("id_kriteria"));
        kriteria.setKodeKriteria(rs.getString("kode_kriteria"));
        kriteria.setNamaKriteria(rs.getString("nama_kriteria"));
        kriteria.setJenisFaktor(rs.getString("jenis_faktor"));

        NilaiKandidat nilaiKandidat = new NilaiKandidat();
        nilaiKandidat.setIdNilai(rs.getInt("id_nilai"));
        nilaiKandidat.setKandidat(kandidat);
        nilaiKandidat.setKriteria(kriteria);
        nilaiKandidat.setNilaiKandidat(rs.getDouble("nilai_kandidat"));
        return nilaiKandidat;
    }
}
