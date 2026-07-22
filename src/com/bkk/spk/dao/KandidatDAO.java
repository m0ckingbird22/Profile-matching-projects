package com.bkk.spk.dao;

import com.bkk.spk.model.Kandidat;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) untuk tabel tb_kandidat.
 * Semua query SQL terkait Kandidat taruh di sini -> jangan taruh SQL di class Form/View.
 */
public class KandidatDAO {

    // CREATE - tambah kandidat baru. Id generated key di-set ke objek kandidat supaya
    // caller bisa langsung pakai (mis. untuk insert batch nilai kandidat terkait).
    public boolean insert(Kandidat kandidat) {
        String sql = "INSERT INTO tb_kandidat (nisn, nama, tanggal_lahir, alamat, link_cv, tahun_lulus) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, kandidat.getNisn());
            ps.setString(2, kandidat.getNama());
            ps.setDate(3, kandidat.getTanggalLahir() != null ? Date.valueOf(kandidat.getTanggalLahir()) : null);
            ps.setString(4, kandidat.getAlamat());
            ps.setString(5, kandidat.getLinkCv());
            ps.setInt(6, kandidat.getTahunLulus());

            int result = ps.executeUpdate();
            if (result > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        kandidat.setIdKandidat(keys.getInt(1));
                    }
                }
            }
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // READ - ambil semua data kandidat
    public List<Kandidat> getAll() {
        List<Kandidat> daftarKandidat = new ArrayList<>();
        String sql = "SELECT * FROM tb_kandidat ORDER BY nama ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarKandidat.add(mapResultSetToKandidat(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data kandidat: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarKandidat;
    }

    // READ - ambil satu kandidat berdasarkan id
    public Kandidat getById(int idKandidat) {
        String sql = "SELECT * FROM tb_kandidat WHERE id_kandidat = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKandidat);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKandidat(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil kandidat id " + idKandidat + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // UPDATE - ubah data kandidat yang sudah ada
    public boolean update(Kandidat kandidat) {
        String sql = "UPDATE tb_kandidat SET nisn=?, nama=?, tanggal_lahir=?, alamat=?, link_cv=?, tahun_lulus=? " +
                     "WHERE id_kandidat=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kandidat.getNisn());
            ps.setString(2, kandidat.getNama());
            ps.setDate(3, kandidat.getTanggalLahir() != null ? Date.valueOf(kandidat.getTanggalLahir()) : null);
            ps.setString(4, kandidat.getAlamat());
            ps.setString(5, kandidat.getLinkCv());
            ps.setInt(6, kandidat.getTahunLulus());
            ps.setInt(7, kandidat.getIdKandidat());

            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE - hapus kandidat berdasarkan id
    public boolean delete(int idKandidat) {
        String sql = "DELETE FROM tb_kandidat WHERE id_kandidat = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idKandidat);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus kandidat: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper privat -> hindari duplikasi kode mapping ResultSet ke object Kandidat
    private Kandidat mapResultSetToKandidat(ResultSet rs) throws SQLException {
        Kandidat kandidat = new Kandidat();
        kandidat.setIdKandidat(rs.getInt("id_kandidat"));
        kandidat.setNisn(rs.getString("nisn"));
        kandidat.setNama(rs.getString("nama"));
        Date tgl = rs.getDate("tanggal_lahir");
        kandidat.setTanggalLahir(tgl != null ? tgl.toLocalDate() : null);
        kandidat.setAlamat(rs.getString("alamat"));
        kandidat.setLinkCv(rs.getString("link_cv"));
        kandidat.setTahunLulus(rs.getInt("tahun_lulus"));
        return kandidat;
    }
}
