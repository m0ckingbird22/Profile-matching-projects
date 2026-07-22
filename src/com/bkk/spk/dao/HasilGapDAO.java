package com.bkk.spk.dao;

import com.bkk.spk.model.HasilGap;
import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Kriteria;
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
 * DAO untuk tabel tb_hasil_gap.
 * Data di tabel ini SELALU digenerate oleh ProfileMatchingService, bukan input manual user.
 */
public class HasilGapDAO {

    private static final String SELECT_BASE =
        "SELECT hg.*, " +
        "  knd.nisn, knd.nama AS nama_kandidat, knd.tahun_lulus, " +
        "  l.posisi, l.deskripsi, l.kuota, l.status, " +
        "  p.id_perusahaan, p.nama_perusahaan, p.alamat, p.bidang_industri, " +
        "  k.kode_kriteria, k.nama_kriteria, k.jenis_faktor " +
        "FROM tb_hasil_gap hg " +
        "JOIN tb_kandidat knd ON hg.id_kandidat = knd.id_kandidat " +
        "JOIN tb_lowongan l ON hg.id_lowongan = l.id_lowongan " +
        "JOIN tb_perusahaan p ON l.id_perusahaan = p.id_perusahaan " +
        "JOIN tb_kriteria k ON hg.id_kriteria = k.id_kriteria";

    // Insert banyak baris gap sekaligus (1 kandidat x semua kriteria) dalam 1 transaksi
    public boolean insertBatch(List<HasilGap> daftarGap) {
        String sql = "INSERT INTO tb_hasil_gap (id_kandidat, id_lowongan, id_kriteria, nilai_gap, bobot_nilai) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (HasilGap gap : daftarGap) {
                    ps.setInt(1, gap.getKandidat().getIdKandidat());
                    ps.setInt(2, gap.getLowongan().getIdLowongan());
                    ps.setInt(3, gap.getKriteria().getIdKriteria());
                    ps.setInt(4, gap.getNilaiGap());
                    ps.setDouble(5, gap.getBobotNilai());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Gagal insert batch hasil gap, rollback: " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.err.println("Gagal membuka koneksi untuk batch insert gap: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<HasilGap> getByKandidatDanLowongan(int idKandidat, int idLowongan) {
        List<HasilGap> daftar = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE hg.id_kandidat = ? AND hg.id_lowongan = ? ORDER BY k.kode_kriteria ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKandidat);
            ps.setInt(2, idLowongan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftar.add(mapResultSetToHasilGap(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil hasil gap: " + e.getMessage());
            e.printStackTrace();
        }
        return daftar;
    }

    // Dipanggil sebelum proses ulang seleksi -> hindari data gap lama menumpuk/duplikat
    public boolean deleteByLowongan(int idLowongan) {
        String sql = "DELETE FROM tb_hasil_gap WHERE id_lowongan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Gagal hapus hasil gap lama untuk lowongan " + idLowongan + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private HasilGap mapResultSetToHasilGap(ResultSet rs) throws SQLException {
        Kandidat kandidat = new Kandidat();
        kandidat.setIdKandidat(rs.getInt("id_kandidat"));
        kandidat.setNisn(rs.getString("nisn"));
        kandidat.setNama(rs.getString("nama_kandidat"));
        kandidat.setTahunLulus(rs.getInt("tahun_lulus"));

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

        HasilGap hasilGap = new HasilGap();
        hasilGap.setIdHasilGap(rs.getInt("id_hasil_gap"));
        hasilGap.setKandidat(kandidat);
        hasilGap.setLowongan(lowongan);
        hasilGap.setKriteria(kriteria);
        hasilGap.setNilaiGap(rs.getInt("nilai_gap"));
        hasilGap.setBobotNilai(rs.getDouble("bobot_nilai"));
        return hasilGap;
    }
}
