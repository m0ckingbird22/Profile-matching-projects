package com.bkk.spk.view;

/**
 * Kontrak navigasi antar card di MainFrame.
 *
 * Kenapa pakai interface? Supaya sub-panel (KandidatPanel, LowonganPanel, dst.)
 * gak hard-reference MainFrame -> coupling longgar, panel bisa di-test
 * terpisah, dan gampang ditukar implementasinya.
 *
 * Konstanta string di sini dipakai sebagai key CardLayout -> satu sumber
 * kebenaran.
 */
public interface Navigator {

    String LOGIN = "LOGIN";
    String DASHBOARD = "DASHBOARD";
    String KANDIDAT = "KANDIDAT";
    String PERUSAHAAN_LOWONGAN = "PERUSAHAAN_LOWONGAN";
    String KRITERIA = "KRITERIA";
    String INPUT_NILAI = "INPUT_NILAI";
    String PROSES_PERHITUNGAN = "PROSES_PERHITUNGAN";
    String LAPORAN_HASIL = "LAPORAN_HASIL";

    /** Pindah card ke key tertentu (lihat konstanta di interface ini). */
    void show(String cardName);

    /** Logout: clear Session + balik ke login dialog. */
    void logout();
}
