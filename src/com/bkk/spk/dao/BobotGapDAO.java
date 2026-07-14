package com.bkk.spk.dao;

import com.bkk.spk.util.Koneksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO untuk tabel referensi tb_bobot_gap (selisih_gap -> bobot_nilai).
 * Ini tabel baku/statis, jarang berubah -> gak perlu insert/update/delete dari UI.
 * Dipakai oleh ProfileMatchingService untuk konversi gap ke bobot.
 */
public class BobotGapDAO {

    // Ambil semua mapping gap->bobot sekaligus jadi Map, biar ProfileMatchingService
    // gak perlu query berulang-ulang ke database untuk tiap kriteria/siswa (mahal secara performance).
    public Map<Integer, Double> getAllAsMap() {
        Map<Integer, Double> mapBobot = new HashMap<>();
        String sql = "SELECT selisih_gap, bobot_nilai FROM tb_bobot_gap";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapBobot.put(rs.getInt("selisih_gap"), rs.getDouble("bobot_nilai"));
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil tabel bobot gap: " + e.getMessage());
            e.printStackTrace();
        }
        return mapBobot;
    }

    // Ambil bobot untuk 1 nilai gap tertentu
    public double getBobotByGap(int selisihGap) {
        String sql = "SELECT bobot_nilai FROM tb_bobot_gap WHERE selisih_gap = ?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selisihGap);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("bobot_nilai");
                }
            }

        } catch (SQLException e) {
            System.err.println("Gagal mengambil bobot untuk gap " + selisihGap + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Gap di luar rentang -4 s/d +4 seharusnya tidak terjadi kalau nilai 0-100 & target wajar.
        // Return 0 sebagai fallback aman, tapi ini sebaiknya di-log sebagai anomali.
        System.err.println("PERINGATAN: gap " + selisihGap + " tidak ada di tabel bobot, dikembalikan 0");
        return 0.0;
    }
}
