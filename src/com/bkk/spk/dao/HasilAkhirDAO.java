package com.bkk.spk.dao;

import com.bkk.spk.model.Admin;
import com.bkk.spk.model.HasilAkhir;
import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel tb_hasil_akhir.
 * Ini tabel output akhir Profile Matching -> NCF, NSF, nilai total, dan ranking per kandidat per lowongan.
 */
public class HasilAkhirDAO {

    private static final String SELECT_BASE =
        "SELECT ha.*, " +
        "  knd.nisn, knd.nama AS nama_kandidat, knd.tahun_lulus, " +
        "  l.posisi, l.deskripsi, l.kuota, l.status, " +
        "  p.id_perusahaan, p.nama_perusahaan, p.alamat, p.bidang_industri, " +
        "  a.username, a.nama AS nama_admin " +
        "FROM tb_hasil_akhir ha " +
        "JOIN tb_kandidat knd ON ha.id_kandidat = knd.id_kandidat " +
        "JOIN tb_lowongan l ON ha.id_lowongan = l.id_lowongan " +
        "JOIN tb_perusahaan p ON l.id_perusahaan = p.id_perusahaan " +
        "JOIN tb_admin a ON ha.id_admin = a.id_admin";

    public boolean insert(HasilAkhir hasilAkhir) {
        String sql = "INSERT INTO tb_hasil_akhir (id_kandidat, id_lowongan, id_admin, ncf, nsf, nilai_total, ranking) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, hasilAkhir.getKandidat().getIdKandidat());
            ps.setInt(2, hasilAkhir.getLowongan().getIdLowongan());
            ps.setInt(3, hasilAkhir.getAdmin().getIdAdmin());
            ps.setDouble(4, hasilAkhir.getNcf());
            ps.setDouble(5, hasilAkhir.getNsf());
            ps.setDouble(6, hasilAkhir.getNilaiTotal());
            ps.setInt(7, hasilAkhir.getRanking());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert hasil akhir: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Ambil hasil ranking lengkap untuk 1 lowongan, urut dari ranking terbaik -> dipakai untuk tampilan laporan
    public List<HasilAkhir> getByLowongan(int idLowongan) {
        List<HasilAkhir> daftar = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE ha.id_lowongan = ? ORDER BY ha.ranking ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftar.add(mapResultSetToHasilAkhir(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil hasil akhir lowongan " + idLowongan + ": " + e.getMessage());
            e.printStackTrace();
        }
        return daftar;
    }

    public boolean deleteByLowongan(int idLowongan) {
        String sql = "DELETE FROM tb_hasil_akhir WHERE id_lowongan = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLowongan);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Gagal hapus hasil akhir lama untuk lowongan " + idLowongan + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private HasilAkhir mapResultSetToHasilAkhir(ResultSet rs) throws SQLException {
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

        Admin admin = new Admin();
        admin.setIdAdmin(rs.getInt("id_admin"));
        admin.setUsername(rs.getString("username"));
        admin.setNama(rs.getString("nama_admin"));

        HasilAkhir hasilAkhir = new HasilAkhir();
        hasilAkhir.setIdHasilAkhir(rs.getInt("id_hasil_akhir"));
        hasilAkhir.setKandidat(kandidat);
        hasilAkhir.setLowongan(lowongan);
        hasilAkhir.setAdmin(admin);
        hasilAkhir.setNcf(rs.getDouble("ncf"));
        hasilAkhir.setNsf(rs.getDouble("nsf"));
        hasilAkhir.setNilaiTotal(rs.getDouble("nilai_total"));
        hasilAkhir.setRanking(rs.getInt("ranking"));

        Timestamp ts = rs.getTimestamp("tanggal_proses");
        if (ts != null) {
            hasilAkhir.setTanggalProses(ts.toLocalDateTime());
        }
        return hasilAkhir;
    }
}
