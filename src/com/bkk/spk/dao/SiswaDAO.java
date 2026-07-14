package com.bkk.spk.dao;

import com.bkk.spk.model.Siswa;
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
 * DAO (Data Access Object) untuk tabel tb_siswa.
 * Semua query SQL terkait Siswa taruh di sini -> jangan taruh SQL di class Form/View.
 */
public class SiswaDAO {

    // CREATE - tambah siswa baru. Id generated key di-set ke objek siswa supaya
    // caller bisa langsung pakai (mis. untuk insert batch nilai siswa terkait).
    public boolean insert(Siswa siswa) {
        String sql = "INSERT INTO tb_siswa (nisn, nama, tanggal_lahir, alamat, link_cv, jurusan, kelas, tahun_lulus) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // try-with-resources -> Connection & PreparedStatement otomatis ke-close walau error
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, siswa.getNisn());
            ps.setString(2, siswa.getNama());
            ps.setDate(3, siswa.getTanggalLahir() != null ? Date.valueOf(siswa.getTanggalLahir()) : null);
            ps.setString(4, siswa.getAlamat());
            ps.setString(5, siswa.getLinkCv());
            ps.setString(6, siswa.getJurusan());
            ps.setString(7, siswa.getKelas());
            ps.setInt(8, siswa.getTahunLulus());

            int result = ps.executeUpdate();
            if (result > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        siswa.setIdSiswa(keys.getInt(1));
                    }
                }
            }
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // READ - ambil semua data siswa
    public List<Siswa> getAll() {
        List<Siswa> daftarSiswa = new ArrayList<>();
        String sql = "SELECT * FROM tb_siswa ORDER BY nama ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                daftarSiswa.add(mapResultSetToSiswa(rs));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil data siswa: " + e.getMessage());
            e.printStackTrace();
        }
        return daftarSiswa;
    }

    // READ - ambil satu siswa berdasarkan id
    public Siswa getById(int idSiswa) {
        String sql = "SELECT * FROM tb_siswa WHERE id_siswa = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSiswa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSiswa(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil siswa id " + idSiswa + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null; // gak ketemu / error
    }

    // UPDATE - ubah data siswa yang sudah ada
    public boolean update(Siswa siswa) {
        String sql = "UPDATE tb_siswa SET nisn=?, nama=?, tanggal_lahir=?, alamat=?, link_cv=?, jurusan=?, kelas=?, tahun_lulus=? " +
                     "WHERE id_siswa=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, siswa.getNisn());
            ps.setString(2, siswa.getNama());
            ps.setDate(3, siswa.getTanggalLahir() != null ? Date.valueOf(siswa.getTanggalLahir()) : null);
            ps.setString(4, siswa.getAlamat());
            ps.setString(5, siswa.getLinkCv());
            ps.setString(6, siswa.getJurusan());
            ps.setString(7, siswa.getKelas());
            ps.setInt(8, siswa.getTahunLulus());
            ps.setInt(9, siswa.getIdSiswa());

            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // DELETE - hapus siswa berdasarkan id
    public boolean delete(int idSiswa) {
        String sql = "DELETE FROM tb_siswa WHERE id_siswa = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSiswa);
            int result = ps.executeUpdate();
            return result > 0;

        } catch (SQLException e) {
            // Kemungkinan error krn FK constraint (siswa ini sudah punya nilai/hasil terkait)
            System.err.println("Gagal hapus siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Helper privat -> hindari duplikasi kode mapping ResultSet ke object Siswa
    private Siswa mapResultSetToSiswa(ResultSet rs) throws SQLException {
        Siswa siswa = new Siswa();
        siswa.setIdSiswa(rs.getInt("id_siswa"));
        siswa.setNisn(rs.getString("nisn"));
        siswa.setNama(rs.getString("nama"));
        Date tgl = rs.getDate("tanggal_lahir");
        siswa.setTanggalLahir(tgl != null ? tgl.toLocalDate() : null);
        siswa.setAlamat(rs.getString("alamat"));
        siswa.setLinkCv(rs.getString("link_cv"));
        siswa.setJurusan(rs.getString("jurusan"));
        siswa.setKelas(rs.getString("kelas"));
        siswa.setTahunLulus(rs.getInt("tahun_lulus"));
        return siswa;
    }
}
