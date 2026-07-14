package com.bkk.spk.service;

import com.bkk.spk.dao.BobotGapDAO;
import com.bkk.spk.dao.HasilAkhirDAO;
import com.bkk.spk.dao.HasilGapDAO;
import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.dao.NilaiSiswaDAO;
import com.bkk.spk.dao.ProfilIdealDAO;
import com.bkk.spk.dao.SiswaDAO;
import com.bkk.spk.model.Admin;
import com.bkk.spk.model.HasilAkhir;
import com.bkk.spk.model.HasilGap;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.NilaiSiswa;
import com.bkk.spk.model.ProfilIdeal;
import com.bkk.spk.model.Siswa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service untuk menjalankan algoritma Profile Matching:
 * GAP -> Bobot -> NCF/NSF -> Nilai Total -> Ranking.
 *
 * PENTING: bobot 60% CF / 40% SF di bawah ini adalah nilai yang UMUM dipakai
 * di literatur Profile Matching. CEK LAGI ke BAB II/III skripsi kamu -
 * kalau dosen pembimbing menetapkan rasio beda (misal 70/30), ganti nilai
 * BOBOT_CF dan BOBOT_SF di bawah supaya konsisten dengan apa yang kamu tulis
 * di laporan. Jangan sampai kode dan laporan tertulis beda angka.
 */
public class ProfileMatchingService {

    private static final double BOBOT_CF = 0.6;
    private static final double BOBOT_SF = 0.4;

    private final SiswaDAO siswaDAO = new SiswaDAO();
    private final LowonganDAO lowonganDAO = new LowonganDAO();
    private final ProfilIdealDAO profilIdealDAO = new ProfilIdealDAO();
    private final NilaiSiswaDAO nilaiSiswaDAO = new NilaiSiswaDAO();
    private final BobotGapDAO bobotGapDAO = new BobotGapDAO();
    private final HasilGapDAO hasilGapDAO = new HasilGapDAO();
    private final HasilAkhirDAO hasilAkhirDAO = new HasilAkhirDAO();

    /**
     * Method utama -> dipanggil dari tombol "Proses Seleksi" di UI.
     * Menghitung Profile Matching untuk SEMUA siswa terhadap 1 lowongan tertentu,
     * lalu menyimpan hasil gap + hasil akhir + ranking ke database.
     *
     * @param idLowongan lowongan yang mau diproses
     * @param adminYangMemproses admin yang sedang login (buat audit trail)
     * @return daftar hasil akhir, sudah terurut dari ranking 1 (terbaik)
     */
    public List<HasilAkhir> prosesSeleksi(int idLowongan, Admin adminYangMemproses) {
        Lowongan lowongan = lowonganDAO.getById(idLowongan);
        if (lowongan == null) {
            throw new IllegalArgumentException("Lowongan dengan id " + idLowongan + " tidak ditemukan.");
        }

        List<ProfilIdeal> profilIdealList = profilIdealDAO.getByLowongan(idLowongan);
        if (profilIdealList.isEmpty()) {
            throw new IllegalStateException("Profil ideal belum diatur untuk lowongan ini. Set dulu nilai target per kriteria.");
        }

        // Hapus hasil proses sebelumnya (kalau ini re-proses / proses ulang) supaya data tidak dobel
        hasilGapDAO.deleteByLowongan(idLowongan);
        hasilAkhirDAO.deleteByLowongan(idLowongan);

        // Ambil tabel bobot gap sekali di awal, dipakai berulang -> hindari query berulang per siswa
        Map<Integer, Double> bobotGapMap = bobotGapDAO.getAllAsMap();

        List<Siswa> semuaSiswa = siswaDAO.getAll();
        List<HasilAkhir> hasilSementara = new ArrayList<>();

        for (Siswa siswa : semuaSiswa) {
            List<NilaiSiswa> nilaiSiswaList = nilaiSiswaDAO.getBySiswa(siswa.getIdSiswa());

            // Skip siswa yang nilainya belum lengkap untuk semua kriteria yang dipakai lowongan ini.
            // Ini validasi penting: jangan proses data yang tidak lengkap, hasilnya bisa menyesatkan.
            if (nilaiSiswaList.size() < profilIdealList.size()) {
                System.out.println("Siswa " + siswa.getNama() + " dilewati: nilai belum lengkap ("
                        + nilaiSiswaList.size() + "/" + profilIdealList.size() + ")");
                continue;
            }

            List<HasilGap> daftarGap = hitungGap(siswa, lowongan, profilIdealList, nilaiSiswaList, bobotGapMap);
            hasilGapDAO.insertBatch(daftarGap);

            double[] ncfNsf = hitungNcfNsf(daftarGap);
            double ncf = ncfNsf[0];
            double nsf = ncfNsf[1];
            double nilaiTotal = (ncf * BOBOT_CF) + (nsf * BOBOT_SF);

            hasilSementara.add(new HasilAkhir(siswa, lowongan, adminYangMemproses, ncf, nsf, nilaiTotal));
        }

        // Ranking: urutkan dari nilai_total TERBESAR -> ranking 1
        hasilSementara.sort((a, b) -> Double.compare(b.getNilaiTotal(), a.getNilaiTotal()));

        int rank = 1;
        for (HasilAkhir hasilAkhir : hasilSementara) {
            hasilAkhir.setRanking(rank++);
            hasilAkhirDAO.insert(hasilAkhir);
        }

        return hasilSementara;
    }

    /**
     * Hitung GAP (nilai kandidat - nilai target) untuk 1 siswa di semua kriteria lowongan tsb,
     * lalu konversi tiap gap ke bobot pakai tabel referensi.
     */
    private List<HasilGap> hitungGap(Siswa siswa, Lowongan lowongan, List<ProfilIdeal> profilIdealList,
                                      List<NilaiSiswa> nilaiSiswaList, Map<Integer, Double> bobotGapMap) {

        List<HasilGap> daftarGap = new ArrayList<>();

        for (ProfilIdeal profilIdeal : profilIdealList) {
            int idKriteria = profilIdeal.getKriteria().getIdKriteria();

            NilaiSiswa nilaiSiswa = cariNilaiByKriteria(nilaiSiswaList, idKriteria);
            if (nilaiSiswa == null) {
                // Seharusnya tidak terjadi karena sudah divalidasi kelengkapan di atas, tapi jaga-jaga
                continue;
            }

            int gap = (int) Math.round(nilaiSiswa.getNilaiKandidat() - profilIdeal.getNilaiTarget());
            double bobot = bobotGapMap.getOrDefault(gap, 0.0);

            daftarGap.add(new HasilGap(siswa, lowongan, profilIdeal.getKriteria(), gap, bobot));
        }
        return daftarGap;
    }

    private NilaiSiswa cariNilaiByKriteria(List<NilaiSiswa> nilaiSiswaList, int idKriteria) {
        for (NilaiSiswa n : nilaiSiswaList) {
            if (n.getKriteria().getIdKriteria() == idKriteria) {
                return n;
            }
        }
        return null;
    }

    /**
     * Hitung NCF (rata-rata bobot kriteria Core Factor) dan NSF (rata-rata bobot Secondary Factor).
     * @return array 2 elemen: [0] = NCF, [1] = NSF
     */
    private double[] hitungNcfNsf(List<HasilGap> daftarGap) {
        double totalCf = 0, totalSf = 0;
        int countCf = 0, countSf = 0;

        for (HasilGap gap : daftarGap) {
            if (gap.getKriteria().isCoreFactor()) {
                totalCf += gap.getBobotNilai();
                countCf++;
            } else {
                totalSf += gap.getBobotNilai();
                countSf++;
            }
        }

        double ncf = countCf > 0 ? totalCf / countCf : 0.0;
        double nsf = countSf > 0 ? totalSf / countSf : 0.0;
        return new double[]{ncf, nsf};
    }
}
