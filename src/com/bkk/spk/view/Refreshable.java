package com.bkk.spk.view;

/**
 * Penanda panel yang perlu di-refresh ketika ditampilkan ulang.
 *
 * Dipakai MainFrame.navigateTo: tiap kali user pindah halaman, panel yang
 * implements Refreshable akan dipanggil refreshData()-nya supaya data combo/tabel
 * selalu sinkron dengan kondisi DB terbaru (mis. lowongan yang baru di-ubah
 * statusnya dari panel lain).
 */
public interface Refreshable {
    void refreshData();
}
