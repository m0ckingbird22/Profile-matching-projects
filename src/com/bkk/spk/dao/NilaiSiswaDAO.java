package com.bkk.spk.dao;

import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiSiswa;
import com.bkk.spk.model.Siswa;
import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO untuk tabel tb_nilai_siswa.
 * Menyimpan nilai asli (bukan gap) tiap siswa per kriteria.
 */
public class NilaiSiswaDAO {

    private static final String SELECT_BASE =
        "SELECT ns.*, " +
        "  s.nisn, s.nama, s.jurusan, s.kelas, s.tahun_lulus, " +
        "  k.kode_kriteria, k.nama_kriteria, k.jenis_faktor " +
        "FROM tb_nilai_siswa ns " +
        "JOIN tb_siswa s ON ns.id_siswa = s.id_siswa " +
        "JOIN tb_kriteria k ON ns.id_kriteria = k.id_kriteria";

    // insert satu nilai. Untuk skenario umum (input semua nilai 1 siswa sekaligus),
    // panggil ini di loop dari Service/Controller, atau pakai insertBatch di bawah.
    public boolean insert(NilaiSiswa nilaiSiswa) {
        String sql = "INSERT INTO tb_nilai_siswa (id_siswa, id_kriteria, nilai_kandidat) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, nilaiSiswa.getSiswa().getIdSiswa());
            ps.setInt(2, nilaiSiswa.getKriteria().getIdKriteria());
            ps.setDouble(3, nilaiSiswa.getNilaiKandidat());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal insert nilai siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Insert banyak nilai sekaligus (1 siswa, 8 kriteria) dalam SATU transaksi.
    // Kalau salah satu gagal, semua di-rollback -> data gak setengah-setengah.
    public boolean insertBatch(List<NilaiSiswa> daftarNilai) {
        String sql = "INSERT INTO tb_nilai_siswa (id_siswa, id_kriteria, nilai_kandidat) VALUES (?, ?, ?)";

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false); // mulai transaksi manual

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (NilaiSiswa nilai : daftarNilai) {
                    ps.setInt(1, nilai.getSiswa().getIdSiswa());
                    ps.setInt(2, nilai.getKriteria().getIdKriteria());
                    ps.setDouble(3, nilai.getNilaiKandidat());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback(); // batalkan semua kalau ada yang gagal di tengah jalan
                System.err.println("Gagal insert batch nilai siswa, rollback: " + e.getMessage());
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

    // Ambil semua nilai milik satu siswa -> dipakai saat mulai proses Profile Matching
    public List<NilaiSiswa> getBySiswa(int idSiswa) {
        List<NilaiSiswa> daftar = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE ns.id_siswa = ? ORDER BY k.kode_kriteria ASC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSiswa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    daftar.add(mapResultSetToNilaiSiswa(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil nilai siswa " + idSiswa + ": " + e.getMessage());
            e.printStackTrace();
        }
        return daftar;
    }

    public boolean update(NilaiSiswa nilaiSiswa) {
        String sql = "UPDATE tb_nilai_siswa SET nilai_kandidat=? WHERE id_nilai=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, nilaiSiswa.getNilaiKandidat());
            ps.setInt(2, nilaiSiswa.getIdNilai());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal update nilai siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int idNilai) {
        String sql = "DELETE FROM tb_nilai_siswa WHERE id_nilai = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idNilai);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Gagal hapus nilai siswa: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private NilaiSiswa mapResultSetToNilaiSiswa(ResultSet rs) throws SQLException {
        Siswa siswa = new Siswa();
        siswa.setIdSiswa(rs.getInt("id_siswa"));
        siswa.setNisn(rs.getString("nisn"));
        siswa.setNama(rs.getString("nama"));
        siswa.setJurusan(rs.getString("jurusan"));
        siswa.setKelas(rs.getString("kelas"));
        siswa.setTahunLulus(rs.getInt("tahun_lulus"));

        Kriteria kriteria = new Kriteria();
        kriteria.setIdKriteria(rs.getInt("id_kriteria"));
        kriteria.setKodeKriteria(rs.getString("kode_kriteria"));
        kriteria.setNamaKriteria(rs.getString("nama_kriteria"));
        kriteria.setJenisFaktor(rs.getString("jenis_faktor"));

        NilaiSiswa nilaiSiswa = new NilaiSiswa();
        nilaiSiswa.setIdNilai(rs.getInt("id_nilai"));
        nilaiSiswa.setSiswa(siswa);
        nilaiSiswa.setKriteria(kriteria);
        nilaiSiswa.setNilaiKandidat(rs.getDouble("nilai_kandidat"));
        return nilaiSiswa;
    }
}
