# Analisis Struktur Project SPK Profile Matching (BKK SMK)

> Project ini adalah aplikasi desktop Java Swing untuk **Bursa Kerja Khusus (BKK)** sebuah SMK.
> Tujuannya: membantu panitia BKK memilih kandidat lulusan mana yang paling cocok
> dialokasikan ke lowongan tertentu, pakai algoritma **Profile Matching** (hitung selisih
> nilai kandidat vs nilai target → lalu ranking).
>
> File ini menjelaskan tiap folder **dari yang paling dasar (gudang data) sampai yang
> paling depan (yang dilihat user)**, lengkap dengan analogi sederhana, penjelasan teknis,
> dan hubungan antar folder.

---

## Daftar Isi

1. [Root folder & file konfigurasi](#1-root-folder--file-konfigurasi)
2. `database/` — skema + data awal
3. `lib/` — pustaka luar (JAR)
4. `nbproject/`, `build.xml`, `manifest.mf` — konfigurasi build NetBeans
5. `src/com/bkk/spk/util/` — koneksi DB & sesi login
6. `src/com/bkk/spk/model/` — wadah data (POJO)
7. `src/com/bkk/spk/dao/` — jembatan ke database
8. `src/com/bkk/spk/service/` — otak algoritma + cetak PDF
9. `src/com/bkk/spk/resources/` — aset gambar
10. `src/com/bkk/spk/view/util/` — helper styling UI
11. `src/com/bkk/spk/view/` — pintu masuk, frame utama, login
12. `src/com/bkk/spk/view/panel/` — halaman-halaman aplikasi
13. [Diagram alur besar](#diagram-alur-besar)
14. [Ringkasan: project sebagai satu tubuh](#ringkasan-project-sebagai-satu-tubuh)

---

## 1. Root folder & file konfigurasi

**Analoginya:**
Bayangkan kamu mau buka restoran. Sebelum restoran jalan, kamu butuh
**AKTA pendirian**, **logo**, **denah bangunan**, dan **kontrak sewa**. Itulah
file-file yang ada di root folder project ini — bukan bagian "masakan"-nya,
tapi tanpa mereka restoran nggak bisa dibuka.

**Fungsi teknisnya:**
File-file di root yang perlu kamu tahu:

- `build.xml` — script Ant bawaan NetBeans untuk compile & package JAR.
  Di dalamnya cuma `import` `nbproject/build-impl.xml`; kamu nyaris nggak perlu sentuh ini.
- `manifest.mf` — file manifest JAR. Sekarang isinya kosong (Main-Class diisi
  otomatis oleh NetBeans saat build). Kelak JAR dipakai untuk distribusi.
- `nbproject/` — folder konfigurasi project NetBeans (`project.properties`,
  `project.xml`, `genfiles.properties`, `private/`). Isinya: daftar JAR yang
  jadi classpath, platform Java yang dipakai, dst.
- `lib/` — pustaka luar (JAR) — dibahas terpisah di bawah.
- `database/` — script SQL — dibahas terpisah di bawah.
- `src/` — source code Java — **ini inti project**, dibahas di poin-poin berikutnya.
- `.gitignore` — daftar file yang sengaja diabaikan Git (misal `build/`, `dist/`).

**Nyambung ke mana:**
File konfigurasi ini dipakai NetBeans waktu kamu klik **Clean and Build** atau
**Run**. Output build (JAR) akan masuk folder `dist/`. Tanpa file-file ini,
NetBeans nggak tahu classpath mana yang dipakai, dan JAR mysql-connector nggak
akan ke-detect.

---

## 2. `database/` — Skema & data awal

**Analoginya:**
Bayangkan ini **gudung bahan baku restoran + buku resep**. Sebelum masakan
bisa dibuat, kamu harus tahu:
- Bahan apa saja yang harus disiapkan (tabel),
- Bahan itu bentuknya gimana (kolom),
- Dan isi awalnya apa (data dummy).

File `db_spk_bkk.sql` itu ibarat **buku resep lengkap**: bikin gudang,
bikin rak-raknya, lalu ngisi stok awal supaya restoran langsung bisa buka.

**Fungsi teknisnya:**
Isinya 3 file SQL:

- `db_spk_bkk.sql` (file utama) — membuat database `db_spk_bkk` di MySQL/MariaDB
  lengkap dengan **10 tabel** + data dummy:
  - `tb_admin` — user yang bisa login (admin BKK). Default: `admin` / `admin123`.
  - `tb_perusahaan` — mitra yang buka lowongan.
  - `tb_lowongan` — posisi yang dibuka per perusahaan (status `BUKA`/`TUTUP`).
  - `tb_kriteria` — kriteria penilaian C1..C8, diberi label **CF** (Core Factor) atau **SF** (Secondary Factor).
  - `tb_bobot_gap` — **tabel baku Profile Matching**: selisih gap → bobot (5.0, 4.5, …, 1.0).
  - `tb_profil_ideal` — nilai target tiap kriteria per lowongan.
  - `tb_kandidat` — data siswa lulusan (NISN, nama, alamat, link CV, dll).
  - `tb_nilai_kandidat` — nilai asli (skala 1-5) tiap kandidat per kriteria.
  - `tb_hasil_gap` — OUTPUT perhitungan: gap & bobot. **Dikosongkan**, diisi otomatis oleh Service.
  - `tb_hasil_akhir` — OUTPUT akhir: NCF, NSF, nilai total, ranking. Juga otomatis.
- `update_kandidat_data.sql` & `update_nama_kriteria.sql` — script patch
  untuk mengubah data kandidat/nama kriteria setelah database dipakai.

**Nyambung ke mana:**
Script `db_spk_bkk.sql` itu sumber kebenaran struktur data. Class
`util/Koneksi.java` mengacu ke database `db_spk_bkk` (URL-nya literal di kode).
Semua class di package `dao/` mengeksekusi query ke tabel-tabel yang
didefinisikan di sini. Alurnya:

```
db_spk_bkk.sql (dijalankan di phpMyAdmin) → database db_spk_bkk ada di MySQL
                                            ↓
                         Koneksi.java nyambungkan ke sini
                                            ↓
                          DAO-DAO baca/tulis tabel-tabelnya
```

---

## 3. `lib/` — Pustaka luar (JAR)

**Analoginya:**
Bayangkan kamu butuh **mesin penggiling, oven microwave, dan printer struk**
di restoran — kamu nggak bikin sendiri alat-alat itu, kamu beli jadi dari pabrik.
Di project ini, "alat jadi" itu adalah file JAR di folder `lib/`.

**Fungsi teknisnya:**
3 pustaka yang dipakai:

- `mysql-connector-j-9.7.0.jar` — driver JDBC supaya Java bisa ngobrol dengan MySQL.
  Tanpa ini, `DriverManager.getConnection(...)` di `Koneksi.java` akan throw
  `ClassNotFoundException`.
- `flatlaf-3.7.2.jar` — library Look & Feel modern buatan pihak ketiga
  (FlatLaf). Di code dipanggil pakai **reflection** (`Class.forName("com.formdev.flatlaf.FlatIntelliJLaf")`)
  di `MainApp.setupLookAndFeel()` — artinya kalau JAR belum di-add, aplikasi
  tetap jalan, cuma fallback ke Nimbus (bawaan JDK).
- `pdfbox-2.0.30/` — 3 JAR (`pdfbox`, `fontbox`, `commons-logging`) dipakai
  `LaporanPdfExporter` untuk generate laporan PDF (header, kop, tabel ranking, dll).

**Nyambung ke mana:**
NetBeans membaca file-file ini lewat konfigurasi di `nbproject/project.properties`
(jar.classpath). Saat compile → masuk ke classpath. Saat build JAR → ikut
di-bundle di folder `dist/lib/`.

---

## 4. `nbproject/`, `build.xml`, `manifest.mf` — Konfigurasi build NetBeans

**Analoginya:**
Bayangkan ini **perizinan & SOP restoran**. Kamu sebagai pemilik restoran
nggak menyentuh ini tiap hari, tapi saat ada orang baru masuk (IDE baru dibuka),
mereka baca ini dulu supaya tahu aturan mainnya.

**Fungsi teknisnya:**
- `nbproject/project.xml` & `project.properties` — deklarasi:
  - Main-Class: `com.bkk.spk.view.MainApp` (kelas yang punya `public static void main`).
  - Source root: folder `src/`.
  - Library yang direferensikan (lihat poin 3 di atas).
  - Target platform Java.
- `nbproject/build-impl.xml` — Ant script bawaan NetBeans (jangan diedit manual).
- `nbproject/private/` — preferensi personal (lokasi XAMPP, JVM args, dll) —
  biasanya **jangan** di-commit ke repo (sudah masuk `.gitignore`).
- `build.xml` — file tempat kamu bisa nambah custom Ant task kalau perlu
  (sekarang kosong, cuma import `build-impl.xml`).
- `manifest.mf` — manifest JAR (sekarang kosong, Main-Class otomatis).

**Nyambung ke mana:**
Dipakai oleh NetBeans IDE untuk: Build → Clean → Run → Debug. Tanpa folder
ini, project nggak bisa dibuka sebagai "NetBeans Project".

---

## 5. `src/com/bkk/spk/util/` — Koneksi & Session

**Analoginya:**
Bayangkan dua hal:
- **Koneksi** = **pipa air utama** yang nyambungin rumah ke PDAM. Tiap keran
  (DAO) butuh air, tinggal buka keran, air mengalir dari sumber yang sama.
- **Session** = **ID card karyawan** yang dipakai sepanjang shift. Begitu
  kamu tap kartu di gerbang (login), semua orang di restoran tahu kamu
  "Pramugari bernama X". Kalau kamu clock-out (logout), kartu dilupakan.

**Fungsi teknisnya:**
2 file kecil tapi critical:

- `Koneksi.java` — **Singleton** koneksi MySQL. Method `getConnection()`:
  - Cek apakah `connection` masih null atau sudah ditutup.
  - Kalau iya, load driver `com.mysql.cj.jdbc.Driver` lalu
    `DriverManager.getConnection(URL, USER, PASSWORD)`.
  - URL, USER (`root`), PASSWORD (`""`) di-hardcode sebagai `static final`.
  - Dipanggil oleh **setiap** method di setiap DAO.
- `Session.java` — kelas global yang nyimpen object `Admin` yang lagi login.
  - Saat login sukses di `LoginDialog`, `Session.setCurrentAdmin(admin)` dipanggil.
  - Saat logout di `MainFrame.logout()`, `Session.clear()`.
  - Yang paling sering baca: `ProsesPerhitunganPanel` — butuh `id_admin`
    untuk audit di tabel `tb_hasil_akhir` (siapa yang memproses?).

**Nyambung ke mana:**
`Koneksi` dipanggil dari semua DAO. `Session` diisi oleh `LoginDialog`,
dibaca oleh `MainFrame` (buat label "Login sebagai: ...") dan
`ProsesPerhitunganPanel` (buat id_admin saat simpan hasil).

Alur singkat login:
```
User ketik username+password di LoginDialog
        ↓
LoginDialog.doLogin() → AdminDAO.getByUsername(username)
        ↓
Bandingkan password (masih PLAINTEXT — ada TODO buat BCrypt)
        ↓
Session.setCurrentAdmin(admin) → simpan object Admin global
        ↓
LoginDialog.dispose() → MainApp lanjut bikin MainFrame
```

---

## 6. `src/com/bkk/spk/model/` — Wadah data (POJO)

**Analoginya:**
Bayangkan kamu pesan makanan di restoran. Pelayan nggak bawa "nasi goreng"
ditangan kosong — dia bawa pakai **piring**. Tiap jenis makanan punya piring
sendiri: sop di mangkuk, kopi di gelas, nasi di piring datar.
Di project ini, **class-class di `model/` adalah piring-piring tersebut** —
wadah berisi data yang dipindahkan antar bagian aplikasi.

Yang penting: piring **nggak tahu** dari dapur atau ke meja mana. Dia cuma
wadah. DAO yang tahu cara ngisi piring itu dari gudang, dan Panel yang
tahu cara menampilkan isinya.

**Fungsi teknisnya:**
9 class POJO (Plain Old Java Object) — masing-masing punya field privat +
getter/setter, **tanpa logika bisnis**. Beberapa punya `toString()` biar
enak ditampilkan di JComboBox/JList.

| Model | Tabel DB | Field penting | Catatan |
|---|---|---|---|
| `Admin` | `tb_admin` | idAdmin, username, password, nama | `toString()` → nama (untuk JComboBox) |
| `Perusahaan` | `tb_perusahaan` | idPerusahaan, namaPerusahaan, alamat, bidangIndustri | — |
| `Lowongan` | `tb_lowongan` | idLowongan, **perusahaan** (object!), posisi, kuota, status | Punya relasi ke `Perusahaan` |
| `Kriteria` | `tb_kriteria` | idKriteria, kodeKriteria (C1..C8), namaKriteria, jenisFaktor | Konstanta `CORE_FACTOR="CF"` / `SECONDARY_FACTOR="SF"` |
| `Kandidat` | `tb_kandidat` | idKandidat, nisn, nama, tanggalLahir, alamat, linkCv, tahunLulus | `toString()` → "Nama (NISN)" |
| `NilaiKandidat` | `tb_nilai_kandidat` | idNilai, **kandidat**, **kriteria**, nilaiKandidat | Relasi ke Kandidat + Kriteria |
| `ProfilIdeal` | `tb_profil_ideal` | idProfilIdeal, **lowongan**, **kriteria**, nilaiTarget | Relasi ke Lowongan + Kriteria |
| `HasilGap` | `tb_hasil_gap` | idHasilGap, kandidat, lowongan, kriteria, nilaiGap, bobotNilai | OUTPUT hitungan |
| `HasilAkhir` | `tb_hasil_akhir` | idHasilAkhir, kandidat, lowongan, admin, ncf, nsf, nilaiTotal, ranking, tanggalProses | OUTPUT final |

Perhatikan: beberapa model punya **field berupa object model lain**
(bukan cuma id). Contoh: `Lowongan` punya `Perusahaan perusahaan`, bukan
`int idPerusahaan`. Ini disengaja — saat ditampilkan di UI, kita langsung
bisa akses `lowongan.getPerusahaan().getNamaPerusahaan()` tanpa query lagi.
Konsekuensinya: DAO yang handle JOIN SQL untuk ngisi object relasi ini.

**Nyambung ke mana:**
Model dipakai oleh **semua layer**: DAO mengisi-nya dari ResultSet, Service
mengoper-nya antar method, View menampilkan isinya. Model adalah "lingua franca"
(bahasa perantara) antar layer.

```
DAO.baca()  →  Model terisi  →  Service proses  →  Model hasil  →  View tampilkan
```

---

## 7. `src/com/bkk/spk/dao/` — Data Access Object

**Analoginya:**
Bayangkan **staf gudang restoran**. Koki di dapur nggak mau repot ke gudang
ambil bahan sendiri — dia cuma teriak "Bawakan 5 kg beras!". Staf gudang
yang tau persis di rak mana beras disimpan, cara ambilnya, dan cara nyatetnya.
Tiap tabel di database punya "staf gudang"-nya sendiri-supaya spesialisasi jelas.

**Fungsi teknisnya:**
DAO = **Data Access Object**. Pattern ini mengisolasi semua kode SQL ke
satu tempat supaya View/Service nggak perlu tahu SQL. Tiap DAO biasanya
punya method CRUD: `insert`, `getAll`, `getById`, `update`, `delete`.

10 class DAO (urut by kompleksitas):

| DAO | Method kunci | Catatan teknis |
|---|---|---|
| `AdminDAO` | insert, getAll, getById, **getByUsername**, update, delete | `getByUsername` dipakai untuk login |
| `PerusahaanDAO` | CRUD standar | — |
| `KriteriaDAO` | CRUD standar | — |
| `KandidatDAO` | insert + **RETURN_GENERATED_KEYS** (ambil id baru), update, delete | Id baru di-set balik ke object supaya caller bisa dipakai untuk insert nilai |
| `LowonganDAO` | CRUD + **getAllBuka()** (filter `status='BUKA'`) | Semua SELECT JOIN ke `tb_perusahaan` karena model Lowongan simpan object Perusahaan |
| `NilaiKandidatDAO` | insert, **insertBatch** (transaksi), getByKandidat, update, delete | `insertBatch` pakai `setAutoCommit(false)` + `executeBatch()` + `commit()` — efisien & atomik |
| `ProfilIdealDAO` | insert, **getByLowongan(idLowongan)**, update, delete | Yang paling sering dipanggil: ambil target per lowongan |
| `BobotGapDAO` | **getAllAsMap()**, getBobotByGap | Ambil tabel referensi baku → Map<Integer,Double> biar nggak bolak-balik query |
| `HasilGapDAO` | **insertBatch**, getByKandidatDanLowongan, **deleteByLowongan** | Dipakai sebelum re-proses: hapus hasil gap lama biar nggak duplikat |
| `HasilAkhirDAO` | insert, **getByLowongan** (JOIN 4 tabel), deleteByLowongan | Query SELECT_BASE JOIN ke kandidat+lowongan+perusahaan+admin sekaligus |

**Pola yang berulang di semua DAO:**
1. Ambil `Connection` dari `Koneksi.getConnection()` (try-with-resources).
2. Bikin `PreparedStatement` dengan SQL.
3. Isi parameter (`ps.setInt(...)`, `ps.setString(...)`).
4. Eksekusi (`executeUpdate` untuk insert/update/delete, `executeQuery` untuk SELECT).
5. Untuk SELECT: map `ResultSet` ke object model via helper `mapResultSetToX(...)`.
6. Tangani `SQLException` — print stack trace + return null/false (jangan throw ke UI).

**Nyambung ke mana:**
DAO dipanggil oleh Service dan langsung oleh Panel (untuk operasi master data
seperti Kandidat, Perusahaan, Kriteria). Untuk operasi yang butuh logika
(sparing seleksi, batch), Panel manggil Service yang di dalamnya manggil DAO.

Contoh alur Tambah Kandidat:
```
User isi form di KandidatFormDialog → klik Simpan
        ↓
KandidatPanel.klikSimpan() → bikin object Kandidat dari form
        ↓
KandidatDAO.insert(kandidat) → SQL INSERT ke tb_kandidat
        ↓
Kembali ke Panel → refresh tabel
```

Contoh alur yang lebih kompleks (InputNilai simpan batch):
```
User edit beberapa sel nilai di tabel → klik Simpan
        ↓
InputNilaiPanel → kumpulin semua baris yang berubah → bikin List<NilaiKandidat>
        ↓
NilaiKandidatDAO.insertBatch(list) → transaksi: setAutoCommit(false) → executeBatch → commit
        ↓
Kalau ada error → rollback() (data nggak setengah-setengah)
```

---

## 8. `src/com/bkk/spk/service/` — Otak aplikasi

**Analogiya:**
Bayangkan **Kepala Koki**. Dia bukan sekadar mengambil bahan dari gudang (itu
tugas staf gudang/DAO), tapi dia tahu **resep**: bahan A dan B dicampur pakai
rasio tertentu, dimasak berapa menit, baru jadi masakan siap saji.
Di project ini, **Service adalah tempat algoritma hidup**. Kode SQL tidak
boleh masuk sini — Service cuma ngerti "langkah-langkah masak" pakai bahan
yang disediain DAO.

**Fungsi teknisnya:**
2 file:

### a. `ProfileMatchingService.java` — algoritma Profile Matching

Ini inti ilmu project ini. Method utama `prosesSeleksi(idLowongan, admin)`:

```
1. Ambil Lowongan by id (validasi)
2. Ambil ProfilIdeal untuk lowongan itu (target per kriteria)
3. Hapus hasil proses lama: hasilGapDAO.deleteByLowongan + hasilAkhirDAO.deleteByLowongan
   (supaya re-proses tidak duplikat data)
4. Ambil tabel bobot gap SEKALI → Map<Integer,Double> (efisiensi: hindari query per kandidat)
5. Loop semua Kandidat:
   a. Ambil NilaiKandidat milik kandidat itu
   b. Skip kalau nilai belum lengkap (size < target size)
   c. Hitung GAP tiap kriteria: gap = nilai_kandidat - nilai_target
   d. Konversi gap → bobot pakai map dari poin 4
   e. Insert batch hasil gap ke tb_hasil_gap
   f. Hitung NCF = rata-rata bobot kriteria CF (Core Factor)
   g. Hitung NSF = rata-rata bobot kriteria SF (Secondary Factor)
   h. Nilai Total = (NCF * 0.6) + (NSF * 0.4)
   i. Simpan ke list sementara
6. Sort list sementara by nilaiTotal DESC
7. Beri ranking (1, 2, 3, ...) + insert ke tb_hasil_akhir
8. Return list ke pemanggil
```

Konstanta penting yang HARUS cocok sama laporan skripsi:
- `BOBOT_CF = 0.6` (60%)
- `BOBOT_SF = 0.4` (40%)

Comment di kode eksplisit ngingetin: "CEK LAGI ke BAB II/III skripsi kamu".
Kalau pembimbing minta rasio beda (misal 70/30), ubah 2 baris ini.

### b. `LaporanPdfExporter.java` — generate laporan PDF

Pakai **PDFBox 2.0.x**. Bikin PDF per lowongan dengan struktur:
1. **Kop surat** — logo SMK (dari resources), nama sekolah, alamat.
2. **Header** — strip pink + judul "HASIL RANKING [NAMA PT]".
3. **Box info** — posisi, perusahaan, kuota, status.
4. **Tabel ranking** — Peringkat, Kode (NISN), Nama Kandidat, Nilai Akhir, Status (LULUS/Belum Lulus, batasnya = kuota).
5. **Tanda tangan** — kota + tanggal + nama admin.
6. **Footer** — nomor halaman.

Class-nya cukup panjang (~430 baris) karena PDFBox itu **low-level**: kamu
harus atur posisi X/Y tiap teks, gambar kotak manual, hitung lebar kolom,
kalau melebihi batas halaman harus `newPage()`. Ada inner class `Ctx`
(context) untuk encapsulasi state (page aktif, content stream, posisi Y).

**Nyambung ke mana:**
- `ProfileMatchingService` dipanggil dari **`ProsesPerhitunganPanel`** saat
  user klik "Proses Perhitungan". Dijalankan di thread terpisah supaya UI
  tidak freeze.
- `LaporanPdfExporter` dipanggil dari **`LaporanHasilPanel`** saat user klik
  "Cetak PDF" → JFileChooser → simpan ke disk → optional buka otomatis.

Alur lengkap proses seleksi:
```
User pilih lowongan di ProsesPerhitunganPanel → klik "Proses Perhitungan"
        ↓
Panel panggil ProfileMatchingService.prosesSeleksi(idLowongan, Session.admin)
        ↓
Service ambil data lewat 5 DAO berbeda
        ↓
Service hapus hasil lama → hitung gap → hitung NCF/NSF/Total → ranking
        ↓
Service simpan hasil lewat HasilGapDAO + HasilAkhirDAO
        ↓
Return List<HasilAkhir> → Panel tampilkan di JTable
```

---

## 9. `src/com/bkk/spk/resources/` — Aset statis

**Analoginya:**
Bayangkan **logo restoran yang dipajang di dinding** — bukan bagian masakan,
tapi tanpa logo, restoran kelihatan nggak profesional.

**Fungsi teknisnya:**
Isinya cuma 1 file: `logo_smk.png`. Di-akses lewat classpath:

```java
getClass().getResource("/com/bkk/spk/resources/logo_smk.png")
```

Dipakai di 3 tempat:
1. `LoginDialog` — logo di atas form login.
2. `DashboardPanel` — watermark tengah.
3. `LaporanPdfExporter` — di kop surat PDF.

**Nyambung ke mana:**
Dipakai langsung oleh View (LoginDialog, DashboardPanel) dan Service
(LaporanPdfExporter). Karena di-embed di classpath (`src/com/bkk/spk/resources/`),
file ini otomatis masuk ke JAR saat build.

---

## 10. `src/com/bkk/spk/view/util/` — Helper styling UI

**Analogiya:**
Bayangkan **seragam karyawan + alat decorator**. Daripada tiap koki / pelayan
harus mikirin "pake baju warna apa hari ini", kamu bikin satu standar:
"semua pelayan pakai seragam pink". Cukup panggil sekali, semua tombol /
tabel langsung rapi. Ini nggak ada hubungannya sama masakan — cuma
**presentasi**.

**Fungsi teknisnya:**
2 file helper:

- `ButtonStyle.java` — konstanta warna pink (tema aplikasi) + method:
  - `primary(JButton)` — tombol solid pink + hover effect (warna lebih gelap saat mouse di atas).
  - `secondary(JButton)` — tombol outline pink (untuk tombol Batal/Keluar).
  - Variants: `primary(String text, ActionListener)` untuk bikin tombol sekalian.
- `ZebraTableRenderer.java` — custom cell renderer untuk JTable:
  - **Zebra striping**: baris genap putih, baris ganjil pink pucat — lebih mudah dibaca.
  - **Alignment per kolom**: LEFT/CENTER/RIGHT, lewat method `apply(table, int[] alignments)`.
  - **Header styling**: pink pastel + bold.
  - Dipakai di semua panel yang punya JTable (ProsesPerhitunganPanel, LaporanHasilPanel, KandidatPanel, dll).

**Nyambung ke mana:**
Dipanggil dari **hampir semua panel** di `view/panel/`. Alasan dipisah:
biarkan satu orang ubah tema warna di `ButtonStyle.java` → semua tombol
langsung ikut berubah, nggak perlu ubah 100 file panel.

---

## 11. `src/com/bkk/spk/view/` — Pintu masuk & kerangka utama

**Analogiya:**
Bayangkan ini **denah restoran + pintu masuk + manager**.
- `MainApp` = **pemilik restoran** yang pertama kali buka kunci pintu pagi-pagi.
- `LoginDialog` = **gerbang keamanan** — tamu harus tunjukin ID dulu sebelum masuk.
- `MainFrame` = **denah ruangan**: kiri ada koridor menu (sidebar), kanan
  ada area konten yang isinya ganti-ganti tergantung menu yang dipilih.
- `Navigator` = **peta ruangan** — interface yang tetapkan nama ruangan
  ("DASHBOARD", "KANDIDAT", dst.) supaya panel nggak perlu ingat letak fisik.

**Fungsi teknisnya:**

### a. `MainApp.java` (entry point)
Method `main()`:
1. Setup Look & Feel: coba **FlatLaf** dulu lewat reflection (supaya aplikasi
   tetap jalan walau JAR belum di-add). Kalau gagal → fallback ke **Nimbus**
   (bawaan JDK).
2. Buka `LoginDialog.tampilkanDanTunggu()` (modal, blocking).
3. Kalau login sukses → instantiate `MainFrame` dan `setVisible(true)`.
   Kalau batal → `System.exit(0)`.

### b. `LoginDialog.java`
- Form username + password + logo + tombol Login/Keluar.
- `doLogin()` → `AdminDAO.getByUsername(username)` → bandingkan password
  (**masih plaintext**, ada TODO untuk BCrypt).
- Kalau sukses → `Session.setCurrentAdmin(admin)` + `dispose()`.
- Method static `tampilkanDanTunggu()` = convenience: bikin dialog, pack,
  tampilkan modal, return status login.

### c. `MainFrame.java` (kerangka utama)
- **Implement `Navigator`** (interface — lihat d).
- Layout: `BorderLayout` → sidebar kiri (220px), header atas, content tengah.
- Sidebar: 7 menu (Dashboard, Kandidat, Perusahaan, Kriteria, Nilai, Proses,
  Laporan) + Logout di bawah. Tiap tombol punya hover effect.
- Content: `CardLayout` → 7 kartu didaftarkan di constructor (tiap kartu
  = satu panel di `view/panel/`).
- Header: judul halaman aktif + label "Login sebagai: ...".
- Konfirmasi sebelum keluar (`windowClosing` → `JOptionPane`).
- Logout: clear Session → dispose → buka login dialog lagi.

### d. `Navigator.java` (interface)
Kontrak + konstanta string:
```java
interface Navigator {
    String DASHBOARD = "DASHBOARD";
    String KANDIDAT = "KANDIDAT";
    // ...
    void show(String cardName);
    void logout();
}
```
**Kenapa interface?** Supaya panel bisa "minta pindah halaman" tanpa
hard-reference ke `MainFrame`. Coupling longgar → panel bisa di-test terpisah.

**Nyambung ke mana:**
`MainApp` → `LoginDialog` → `MainFrame` → 7 panel di `view/panel/`.

Alur startup:
```
JVM jalankan MainApp.main()
        ↓
Setup Look & Feel (FlatLaf atau Nimbus)
        ↓
LoginDialog.tampilkanDanTunggu() — BLOCKING sampai user login/keluar
        ↓ (login OK)
Session.setCurrentAdmin(admin)
        ↓
new MainFrame().setVisible(true)
        ↓
MainFrame constructor: daftarin 7 panel ke CardLayout, tampilkan DASHBOARD
        ↓
User klik menu di sidebar → Navigator.show(cardName) → CardLayout.switch
```

---

## 12. `src/com/bkk/spk/view/panel/` — Halaman aplikasi

**Analogiya:**
Bayangkan **ruangan-ruangan di restoran**: ruang makan utama, area kasir,
dapur, area VIP. Tiap ruangan punya fungsi spesifik dan stafnya sendiri.
Di project ini, **tiap panel = satu halaman** yang user lihat di konten
tengah aplikasi.

**Fungsi teknisnya:**
14 file panel + 4 dialog form. Aku kelompokin berdasarkan fungsinya:

### Panel wrapper (tab container)
- `PerusahaanLowonganPanel` — halaman "Data Perusahaan" yang isinya 2 tab:
  Data Perusahaan + Data Lowongan.
- `KriteriaProfilIdealPanel` — halaman "Data Kriteria" yang isinya 2 tab:
  Data Kriteria + Profil Ideal.

Alasan dipisah jadi tab: pilihan UX supaya sidebar nggak kepanjangan, tapi
data yang berhubungan tetep dekat.

### Panel master data (CRUD)
- `DashboardPanel` — landing page: greeting + 4 kartu statistik (Total
  Kandidat, Perusahaan, Lowongan BUKA, Kriteria). Pakai `Thread` terpisah
  untuk load statistik supaya UI nggak lag.
- `KandidatPanel` — tabel kandidat + filter pencarian + tombol Tambah/Edit/Hapus.
  Kolom ID di-hide (width=0) supaya UI bersih tapi tetep bisa dipakai untuk
  operasi edit/hapus.
- `PerusahaanPanel`, `LowonganPanel`, `KriteriaPanel`, `ProfilIdealPanel` —
  pattern sama, masing-masing untuk tabel terkait.
- `InputNilaiPanel` — **matrix view**: baris = kandidat, kolom = kriteria,
  sel = combo box 1-5. Satu tombol Simpan menyimpan SEMUA baris (pakai
  `NilaiKandidatDAO.insertBatch`).

### Dialog form (modal popup)
- `KandidatFormDialog`, `PerusahaanFormDialog`, `LowonganFormDialog`,
  `KriteriaFormDialog` — dialog modal untuk Tambah/Edit.
  - `KandidatFormDialog` unik: di **mode Tambah**, dia juga nampilkan
    combo nilai per kriteria (1-5) supaya user bisa langsung input nilai
    saat bikin kandidat baru. Setelah Simpan, nilai dikirim balik untuk
    di-insertBatch.

### Panel transaksi (algoritma)
- `ProsesPerhitunganPanel` — halaman "Proses Perhitungan":
  - Pilih lowongan BUKA → klik "Proses Perhitungan".
  - Pakai `Thread` terpisah (`new Thread(() -> service.prosesSeleksi(...))`)
    supaya UI nggak freeze saat ngitung 100 kandidat.
  - Tabel hasil: Rank, NISN, Nama, NCF, NSF, Total, Status.
  - Ada text area "Log" untuk trace proses.

### Panel laporan
- `LaporanHasilPanel` — halaman "Laporan Hasil":
  - ComboBox pilih lowongan → tabel ranking.
  - Klik baris kandidat → tabel bawah nampilkan **detail perhitungan per
    kriteria** (Nilai, Target, GAP, Bobot) + label formula step-by-step.
  - Tombol "Cetak PDF" → `JFileChooser` → `LaporanPdfExporter.export(file, lowongan)`
    di thread terpisah → buka file otomatis opsional.

**Pola yang sama di semua panel:**
1. Deklarasi DAO yang dipakai sebagai `private final`.
2. `initComponents()` — bangun UI (toolbar, tabel, button).
3. `refreshData()` atau `muatXxx()` — reload data dari DAO, isi tabel.
4. Handler tombol → panggil DAO → refresh tabel.

**Nyambung ke mana:**
- Panel master data → langsung manggil DAO.
- Panel transaksi (`ProsesPerhitunganPanel`) → manggil Service.
- Panel laporan → manggil DAO untuk baca, + manggil Service untuk PDF.
- Dialog form → manggil DAO (insert/update), dilempar dari panel master data.

---

## Diagram Alur Besar

Diagram ini nunjukin **alur lengkap dari user buka aplikasi sampai data
kesimpan di database dan dicetak jadi PDF**:

```
                              [MYSQL DATABASE: db_spk_bkk]
                                  ↑               ↓
                                  |               |
                          (baca/tulis via JDBC)
                                  |               |
                                  ↓               ↑
                    ┌─────────────────────────────────────────┐
                    │  util/Koneksi.java  ← pipa utama         │
                    │  util/Session.java  ← state login        │
                    └─────────────────────────────────────────┘
                                  ↑               ↑
                                  |               |
                                  ↓               ↓
                    ┌─────────────────────────────────────────┐
                    │  dao/  (10 class DAO — staf gudang)      │
                    │  Admin, Perusahaan, Lowongan, Kriteria,  │
                    │  Kandidat, NilaiKandidat, ProfilIdeal,   │
                    │  BobotGap, HasilGap, HasilAkhir          │
                    └─────────────────────────────────────────┘
                          ↑                       ↑          ↑
                          |                       |          |
                          ↓                       ↓          ↓
        ┌─────────────────────┐    ┌─────────────────────────────┐
        │  service/           │    │  view/panel/ (UI panel)      │
        │  ProfileMatching    │←───│  ProsesPerhitunganPanel      │
        │  Service            │    │  LaporanHasilPanel           │
        │  (algoritma)        │    │  KandidatPanel, dll          │
        │                     │    │                              │
        │  LaporanPdfExporter │───→│  LaporanHasilPanel (cetak)   │
        └─────────────────────┘    └─────────────────────────────┘
                                          ↑               ↑
                                          |               |
                                          ↓               ↓
                                ┌──────────────────────────────┐
                                │  view/  (kerangka utama)      │
                                │  MainApp → entry point        │
                                │     ↓                         │
                                │  LoginDialog (modal)          │
                                │     ↓ (login OK)              │
                                │  MainFrame (CardLayout +      │
                                │             sidebar menu)     │
                                │     ↓                         │
                                │  Navigator (interface)        │
                                └──────────────────────────────┘
                                          ↑
                                          |
                                ┌─────────┴──────────┐
                                │  view/util/         │
                                │  ButtonStyle        │
                                │  ZebraTableRenderer │
                                │  (styling helper)   │
                                └────────────────────┘
                                          ↑
                                          |
                                ┌─────────┴──────────┐
                                │  resources/         │
                                │  logo_smk.png       │
                                └────────────────────┘
```

### Contoh alur konkret: User login → proses seleksi → cetak PDF

```
[1] User double-click JAR
        ↓
[2] MainApp.main() → setup Look & Feel (FlatLaf)
        ↓
[3] LoginDialog muncul → user ketik "admin"/"admin123"
        ↓
[4] AdminDAO.getByUsername("admin") → query SELECT ke tb_admin
        ↓
[5] Password cocok → Session.setCurrentAdmin(admin)
        ↓
[6] MainFrame tampil → 7 menu di sidebar
        ↓
[7] User klik "Proses Perhitungan"
        ↓
[8] ProsesPerhitunganPanel dimuat → LowonganDAO.getAllBuka() isi combo
        ↓
[9] User pilih lowongan → klik "Proses Perhitungan"
        ↓
[10] Panel panggil ProfileMatchingService.prosesSeleksi(idLowongan, admin)
        di Thread terpisah (UI nggak freeze)
        ↓
[11] Service:
     - LowonganDAO.getById(idLowongan)
     - ProfilIdealDAO.getByLowongan(idLowongan) → target per kriteria
     - HasilGapDAO.deleteByLowongan(idLowongan)     ┐ bersihkan hasil lama
     - HasilAkhirDAO.deleteByLowongan(idLowongan)   ┘
     - BobotGapDAO.getAllAsMap() → tabel bobot (efisiensi)
     - KandidatDAO.getAll() → 100 kandidat
     - Loop tiap kandidat:
         • NilaiKandidatDAO.getByKandidat(id)
         • hitungGap → HasilGapDAO.insertBatch
         • hitungNcfNsf → nilai total = 0.6*NCF + 0.4*NSF
     - Sort by nilaiTotal DESC → beri ranking
     - Loop → HasilAkhirDAO.insert per kandidat
        ↓
[12] Service return List<HasilAkhir> → Panel tampilkan di JTable
        ↓
[13] User klik "Laporan Hasil" di sidebar
        ↓
[14] LaporanHasilPanel dimuat → HasilAkhirDAO.getByLowongan(id) isi tabel ranking
        ↓
[15] User klik baris kandidat → detail perhitungan tampil di tabel bawah
        ↓
[16] User klik "Cetak PDF" → JFileChooser → user pilih lokasi
        ↓
[17] LaporanPdfExporter.export(file, lowongan) di Thread terpisah
        ↓
[18] PDF tersimpan → opsi "Buka sekarang?" → Desktop.open(file)
```

---

## Ringkasan: Project sebagai Satu Tubuh

Bayangkan project ini adalah **satu tubuh manusia yang lagi kerja sebagai
panitia seleksi**. Tiap folder punya peran spesifik:

| Folder | Peran tubuh | Analogi kerja |
|---|---|---|
| `database/` | **Gudang memory** | Catatan permanen: data kandidat, lowongan, hasil seleksi. Disimpan rapi di rak-rak (tabel). |
| `util/Koneksi` | **Pembuluh darah utama** | Jalan tempat darah (data) mengalir antara otak dan gudang. Putus = aplikasi mati. |
| `util/Session` | **Gelang ID** | Tanda "siapa yang lagi shift sekarang". Dilepas pas logout. |
| `model/` | **Piring & nampan** | Wadah bawa data. Nggak punya otak, cuma penampung. |
| `dao/` | **Tangan & kaki** | Eksekutor fisik: ambil bahan dari gudang, simpan balik. Tiap tangan spesialisasi pegang satu jenis piring. |
| `service/ProfileMatchingService` | **Otak** | Tempat algoritma Profile Matching dijalankan. Hanya dia yang paham logika NCF/NSF/ranking. |
| `service/LaporanPdfExporter` | **Tangan kanan spesialis** | Tangan khusus nulis laporan PDF rapi dengan kop sekolah. |
| `resources/` | **Pakaian seragam** | Logo sekolah yang dipajang supaya kelihatan resmi. |
| `view/util/` | **Tata rias & dekor** | Tema warna pink, font, zebra-stripe tabel — bikin penampilan enak dilihat. |
| `view/MainApp` | **Pemilik tubuh** | Yang pertama bangun dan nyalain mesin. |
| `view/LoginDialog` | **Pintu gerbang** | Pintu keamanan: cek ID dulu sebelum boleh masuk. |
| `view/MainFrame` | **Kerangka tubuh** | Struktur tulang: sidebar kiri, header atas, area perut (konten tengah). |
| `view/Navigator` | **Sistem saraf pusat** | Kontrak antar bagian supaya bisa koordinasi pindah-pindah tugas. |
| `view/panel/*` | **Organ-organ kerja** | Tiap organ punya fungsi spesifik: Panel Kandidat itu "tangan data siswa", Panel Proses itu "otak eksekusi", Panel Laporan itu "alat cetak". |

### One-liner peran folder:

- **`database/`** → gudang
- **`util/`** → pipa + ID card
- **`model/`** → piring
- **`dao/`** → tangan yang ambil dari gudang
- **`service/`** → otak algoritma
- **`resources/`** → logo / pakaian
- **`view/util/`** → tata rias
- **`view/`** → kerangka tubuh + pintu gerbang
- **`view/panel/`** → organ-organ yang dilihat user

### Urutan belajar yang disarankan

Kalau kamu baru pertama kali buka project ini, baca folder dalam urutan ini
supaya otakmu ngegampang:

1. `database/db_spk_bkk.sql` — pahami DULU struktur tabel + hubungan antar tabel.
2. `model/` — lihat class-class Java yang mirror tabel-tabel itu.
3. `util/Koneksi.java` — pahami gimana Java nyambung ke database.
4. `dao/AdminDAO.java` — pahami satu DAO sampai tuntas, sisanya pattern sama.
5. `service/ProfileMatchingService.java` — pahami algoritma (ini inti skripsi).
6. `view/MainApp.java` → `LoginDialog.java` → `MainFrame.java` — pahami startup flow.
7. `view/panel/ProsesPerhitunganPanel.java` — lihat gimana UI manggil Service.
8. `view/panel/LaporanHasilPanel.java` — lihat gimana hasil dibaca + dicetak PDF.

Sisanya (panel CRUD, dialog form, helper styling) adalah variasi dari pattern yang
sama — begitu kamu paham 8 poin di atas, sisanya gampang dibaca.
