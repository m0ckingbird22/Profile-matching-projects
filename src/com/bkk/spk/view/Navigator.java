package com.bkk.spk.view;

/**
 * Kontrak navigasi antar card di MainFrame.
 *
 * Kenapa pakai interface? Supaya sub-panel (SiswaPanel, LowonganPanel, dst.)
 * gak hard-reference MainFrame -> coupling longgar, panel bisa di-test
 * terpisah, dan gampang ditukar implementasinya.
 *
 * Konstanta string di sini dipakai sebagai key CardLayout -> satu sumber
 * kebenaran, gak ada typo "SISWA" vs "siswa" tersebar di banyak file.
 */
public interface Navigator {

    String LOGIN = "LOGIN";
    String DASHBOARD = "DASHBOARD";
    String SISWA = "SISWA";
    String PERUSAHAAN = "PERUSAHAAN";
    String LOWONGAN = "LOWONGAN";
    String KRITERIA = "KRITERIA";
    String PROFIL_IDEAL = "PROFIL_IDEAL";
    String INPUT_NILAI = "INPUT_NILAI";
    String PROSES_SELEKSI = "PROSES_SELEKSI";
    String LAPORAN_HASIL = "LAPORAN_HASIL";

    /** Pindah card ke key tertentu (lihat konstanta di interface ini). */
    void show(String cardName);

    /** Logout: clear Session + balik ke login dialog. */
    void logout();
}
