-- =====================================================================
-- DATABASE: db_spk_bkk
-- SPK Profile Matching - BKK (Bursa Kerja Khusus) Sekolah
-- Engine : MySQL 5.7+ / MariaDB 10.4+ (XAMPP default)
-- Cara pakai:
--   1. Buka phpMyAdmin  -> tab Import -> pilih file ini -> Go
--   ATAU
--   2. CLI: mysql -u root < db_spk_bkk.sql
-- Konfigurasi koneksi ada di src/com/bkk/spk/util/Koneksi.java
--   URL  : jdbc:mysql://localhost:3306/db_spk_bkk
--   USER : root   PASS: (kosong)
-- =====================================================================

DROP DATABASE IF EXISTS db_spk_bkk;
CREATE DATABASE db_spk_bkk
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
USE db_spk_bkk;

SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================================
-- 1. tb_admin  -> user yang boleh login ke aplikasi (admin BKK)
--    Password disimpan plaintext sesuai kondisi kode saat ini.
--    Lihat catatan di LoginDialog.java & model/Admin.java rencana BCrypt.
-- =====================================================================
CREATE TABLE tb_admin (
    id_admin   INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nama       VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 2. tb_perusahaan  -> mitra yang membuka lowongan
-- =====================================================================
CREATE TABLE tb_perusahaan (
    id_perusahaan   INT AUTO_INCREMENT PRIMARY KEY,
    nama_perusahaan VARCHAR(100) NOT NULL,
    alamat          VARCHAR(255),
    bidang_industri VARCHAR(100)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. tb_lowongan  -> lowongan pekerjaan dari suatu perusahaan
--    status: 'BUKA' / 'TUTUP' (dipakai di LowonganDAO.getAllBuka)
-- =====================================================================
CREATE TABLE tb_lowongan (
    id_lowongan  INT AUTO_INCREMENT PRIMARY KEY,
    id_perusahaan INT NOT NULL,
    posisi       VARCHAR(100) NOT NULL,
    deskripsi    TEXT,
    kuota        INT NOT NULL DEFAULT 1,
    status       VARCHAR(10) NOT NULL DEFAULT 'BUKA',
    CONSTRAINT fk_lowongan_perusahaan
        FOREIGN KEY (id_perusahaan) REFERENCES tb_perusahaan(id_perusahaan)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_lowongan_status (status)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. tb_kriteria  -> kriteria penilaian (C1..C8)
--    jenis_faktor: 'CF' (Core Factor) / 'SF' (Secondary Factor)
-- =====================================================================
-- =====================================================================
CREATE TABLE tb_kriteria (
    id_kriteria   INT AUTO_INCREMENT PRIMARY KEY,
    kode_kriteria VARCHAR(10)  NOT NULL UNIQUE,
    nama_kriteria VARCHAR(100) NOT NULL,
    jenis_faktor  VARCHAR(2)   NOT NULL,
    CONSTRAINT chk_jenis_faktor CHECK (jenis_faktor IN ('CF','SF'))
) ENGINE=InnoDB;

-- =====================================================================
-- 5. tb_bobot_gap  -> tabel referensi baku Profile Matching
--    Selisih gap = nilai_kandidat - nilai_target
--    Dipakai oleh BobotGapDAO.getAllAsMap()
-- =====================================================================
CREATE TABLE tb_bobot_gap (
    selisih_gap INT PRIMARY KEY,
    bobot_nilai DECIMAL(3,1) NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 6. tb_profil_ideal  -> nilai target per lowongan per kriteria
-- =====================================================================
CREATE TABLE tb_profil_ideal (
    id_profil_ideal INT AUTO_INCREMENT PRIMARY KEY,
    id_lowongan     INT NOT NULL,
    id_kriteria     INT NOT NULL,
    nilai_target    DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_pi_lowongan
        FOREIGN KEY (id_lowongan) REFERENCES tb_lowongan(id_lowongan)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_pi_kriteria
        FOREIGN KEY (id_kriteria) REFERENCES tb_kriteria(id_kriteria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_pi_lowongan_kriteria (id_lowongan, id_kriteria)
) ENGINE=InnoDB;

-- =====================================================================
-- 7. tb_kandidat  -> kandidat yang akan dinilai
-- =====================================================================
CREATE TABLE tb_kandidat (
    id_kandidat   INT AUTO_INCREMENT PRIMARY KEY,
    nisn          VARCHAR(20)  NOT NULL UNIQUE,
    nama          VARCHAR(100) NOT NULL,
    tanggal_lahir DATE,
    alamat        VARCHAR(255),
    link_cv       VARCHAR(255),
    tahun_lulus   INT,
    INDEX idx_kandidat_nama (nama)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. tb_nilai_kandidat  -> nilai asli tiap kandidat per kriteria
--    (bukan nilai gap; gap dihitung on-the-fly di ProfileMatchingService)
-- =====================================================================
CREATE TABLE tb_nilai_kandidat (
    id_nilai        INT AUTO_INCREMENT PRIMARY KEY,
    id_kandidat     INT NOT NULL,
    id_kriteria     INT NOT NULL,
    nilai_kandidat  DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_nk_kandidat
        FOREIGN KEY (id_kandidat) REFERENCES tb_kandidat(id_kandidat)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_nk_kriteria
        FOREIGN KEY (id_kriteria) REFERENCES tb_kriteria(id_kriteria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_nk_kandidat_kriteria (id_kandidat, id_kriteria)
) ENGINE=InnoDB;

-- =====================================================================
-- 9. tb_hasil_gap  -> output hitungan Service: gap & bobot per kandidat per lowongan
--    Diisi oleh ProfileMatchingService (lewat HasilGapDAO.insertBatch),
--    BUKAN input manual user.
-- =====================================================================
CREATE TABLE tb_hasil_gap (
    id_hasil_gap INT AUTO_INCREMENT PRIMARY KEY,
    id_kandidat  INT NOT NULL,
    id_lowongan  INT NOT NULL,
    id_kriteria  INT NOT NULL,
    nilai_gap    INT NOT NULL,
    bobot_nilai  DECIMAL(4,2) NOT NULL,
    CONSTRAINT fk_hg_kandidat
        FOREIGN KEY (id_kandidat) REFERENCES tb_kandidat(id_kandidat)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_hg_lowongan
        FOREIGN KEY (id_lowongan) REFERENCES tb_lowongan(id_lowongan)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_hg_kriteria
        FOREIGN KEY (id_kriteria) REFERENCES tb_kriteria(id_kriteria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_hg_kandidat_lowongan (id_kandidat, id_lowongan)
) ENGINE=InnoDB;

-- =====================================================================
-- 10. tb_hasil_akhir  -> NCF, NSF, nilai total, ranking; tanggal otomatis
-- =====================================================================
CREATE TABLE tb_hasil_akhir (
    id_hasil_akhir INT AUTO_INCREMENT PRIMARY KEY,
    id_kandidat    INT NOT NULL,
    id_lowongan    INT NOT NULL,
    id_admin       INT NOT NULL,
    ncf            DECIMAL(5,2) NOT NULL,
    nsf            DECIMAL(5,2) NOT NULL,
    nilai_total    DECIMAL(5,2) NOT NULL,
    ranking        INT NOT NULL,
    tanggal_proses DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ha_kandidat
        FOREIGN KEY (id_kandidat) REFERENCES tb_kandidat(id_kandidat)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ha_lowongan
        FOREIGN KEY (id_lowongan) REFERENCES tb_lowongan(id_lowongan)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ha_admin
        FOREIGN KEY (id_admin) REFERENCES tb_admin(id_admin)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_ha_lowongan_ranking (id_lowongan, ranking)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;


-- =====================================================================
-- DATA DUMMY
-- Urutan insert menghormati FK: admin -> perusahaan -> lowongan ->
-- kriteria -> profil_ideal -> kandidat -> nilai_kandidat -> bobot_gap.
-- tb_hasil_gap & tb_hasil_akhir sengaja dikosongkan; keduanya diisi
-- otomatis oleh ProfileMatchingService saat user klik "Proses Seleksi".
-- =====================================================================

-- ---------------------- ADMIN ----------------------------------------
-- Login default: username=admin  password=admin123
INSERT INTO tb_admin (username, password, nama) VALUES
('admin', 'admin123', 'Administrator BKK'),
('bk1',   'bk12345',  'Bapak Surya, Pembina BKK'),
('bk2',   'bk12345',  'Ibu Hartuti, Pembina BKK');

-- ---------------------- PERUSAHAAN -----------------------------------
INSERT INTO tb_perusahaan (nama_perusahaan, alamat, bidang_industri) VALUES
('PT Indomarco prismatama',          'Jl. Gaya Motor Raya No. 40, Jakarta Utara',  'Otomotif'),
('PT Jane intan pratama',       'Jl. Japati No. 1, Bandung',                   'Telekomunikasi'),
('PT JFE shoji steel Indonesia',        'Jl. Medan Merdeka Timur 1A, Jakarta Pusat',   'Energi'),
('PT Remala abadi TBK', 'Jl. Soekarno Hatta No. 452, Malang',          'Jasa IT'),
('PT GCKN counsulting', 'Jl. Soekarno Hatta No. 454, Malang',          'Jasa IT');

-- ---------------------- LOWONGAN -------------------------------------
-- id_perusahaan: 1..4
INSERT INTO tb_lowongan (id_perusahaan, posisi, deskripsi, kuota, status) VALUES
(1, 'Staff administrasi',  'Mengoperasikan mesin perakitan sepeda motor. Jam kerja shift pagi/sore.', 5, 'BUKA'),
(2, 'Gudang', 'Instalasi & maintenance jaringan kabel fiber optik pelanggan.',     3, 'BUKA'),
(3, 'Packing', 'Melayani pelanggan di SPBU retail. Komunikatif & ramah.',  4, 'BUKA'),
(4, 'Security', 'Membantu tim mengembangkan aplikasi internal perusahaan (PHP/MySQL).',   2, 'BUKA'),
(2, 'Driver & Kurir',    'Input data pelanggan ke sistem CRM. Teliti & disiplin.',                   2, 'TUTUP');

-- ---------------------- KRITERIA (C1..C8) ----------------------------
-- 4 CF (Core Factor) + 4 SF (Secondary Factor)
INSERT INTO tb_kriteria (kode_kriteria, nama_kriteria, jenis_faktor) VALUES
('C1', 'Rata-rata Nilai Raport',  'CF'),
('C2', 'Kompetensi Keahlian',     'CF'),
('C3', 'Kedisiplinan',            'CF'),
('C4', 'Tes Wawancara',           'CF'),
('C5', 'Prestasi Akademik/Non',   'SF'),
('C6', 'Pengalaman Prakerin',     'SF'),
('C7', 'Kemampuan Komunikasi',    'SF'),
('C8', 'Kejujuran & Sikap',       'SF');

-- ---------------------- BOBOT GAP (tabel baku) -----------------------
-- Standar Profile Matching (gap -4 .. +4)
INSERT INTO tb_bobot_gap (selisih_gap, bobot_nilai) VALUES
( 0, 5.0),
( 1, 4.5),
(-1, 4.0),
( 2, 3.5),
(-2, 3.0),
( 3, 2.5),
(-3, 2.0),
( 4, 1.5),
(-4, 1.0);

-- ---------------------- PROFIL IDEAL ---------------------------------
-- Nilai target per lowongan per kriteria (skala 1..5).
-- Diisi utk SEMUA lowongan (1..5).
-- Format: (id_lowongan, id_kriteria, nilai_target)
-- id_lowongan, C1, C2, C3, C4, C5, C6, C7, C8
--   1          4   2   5   5   3   2   2   4
--   2          4   3   4   5   1   1   2   5
--   3          3   4   5   5   5   1   1   4
--   4          1   5   2   5   4   4   3   4
--   5          1   1   1   3   1   3   5   4
INSERT INTO tb_profil_ideal (id_lowongan, id_kriteria, nilai_target) VALUES
(1, 1, 4), (1, 2, 2), (1, 3, 5), (1, 4, 5), (1, 5, 3), (1, 6, 2), (1, 7, 2), (1, 8, 4),
(2, 1, 4), (2, 2, 3), (2, 3, 4), (2, 4, 5), (2, 5, 1), (2, 6, 1), (2, 7, 2), (2, 8, 5),
(3, 1, 3), (3, 2, 4), (3, 3, 5), (3, 4, 5), (3, 5, 5), (3, 6, 1), (3, 7, 1), (3, 8, 4),
(4, 1, 1), (4, 2, 5), (4, 3, 2), (4, 4, 5), (4, 5, 4), (4, 6, 4), (4, 7, 3), (4, 8, 4),
(5, 1, 1), (5, 2, 1), (5, 3, 1), (5, 4, 3), (5, 5, 1), (5, 6, 3), (5, 7, 5), (5, 8, 4);

-- ---------------------- KANDIDAT -------------------------------------
INSERT INTO tb_kandidat (nisn, nama, tanggal_lahir, alamat, link_cv, tahun_lulus) VALUES
('A1', 'Feby Fitriani', '2004-05-15', 'Jl. Mawar Merah No. 12, Bojong Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1aIlOMllrPllfnJD1vz2vC5SFTfZnEsrE', 2022),
('A2', 'Affania Maghfiroh', '2001-08-10', 'Jl. Melati Indah No. 8, Pondok Gede, Bekasi', 'https://drive.google.com/open?id=1vqqHqt0KbZlEyYsm7Qxp3qCHwwQnJu8k', 2019),
('A3', 'INNES ALPIYANI', '2002-08-24', 'Jl. Anggrek Blok C-15, Jatiasih, Bekasi', 'https://drive.google.com/open?id=1_qiniDsSuIsXJUEdwWUdQv5pxZDxqjBK', 2020),
('A4', 'Shinta avelya setiyowati', '1999-08-20', 'Jl. Kenanga Raya No. 22, Bekasi Barat, Bekasi', 'https://drive.google.com/open?id=1p78Tgtla5JXJn8XskWGFmGdO1mZQ5I7Q', 2017),
('A5', 'Zahra Octaviani', '2003-10-03', 'Jl. Flamboyan No. 5, Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1rK0NDf7Id8nMo8uSDoMh96YbHYZxnvkK', 2021),
('A6', 'Novia', '1997-11-09', 'Jl. Cendana No. 10, Bekasi Selatan, Bekasi', 'https://drive.google.com/open?id=1MyIfAnpPvNFaM-X8ckX_bl2Z1PRSxi1q', 2015),
('A7', 'Agisna Hidayat', '2003-10-12', 'Jl. Diponegoro No. 17, Cakung, Jakarta Timur', 'https://drive.google.com/open?id=1qGPyIkEbhO-BkduXdyu_UmpMV1z9mC5I', 2021),
('A8', 'Salsabila Aulia Nazma', '2003-07-21', 'Jl. Sudirman Kav. 3, Jatinegara, Jakarta Timur', 'https://drive.google.com/open?id=1k1WPAekbhXM6WpokZebvFYTs9fO4KA5z', 2021),
('A9', 'Leily Nurfadhliyah', '2002-12-01', 'Jl. Imam Bonjol No. 9, Matraman, Jakarta Timur', 'https://drive.google.com/open?id=1NKBL6aH_qRry-sLNwxBaFkTXMhUaQrQU', 2020),
('A10', 'Ria Hermawati', '2001-09-23', 'Jl. Kartini No. 14, Kelapa Gading, Jakarta Utara', 'https://drive.google.com/open?id=1noiibebGWg2pSo9KnXnyxzO_2MsMb_-b', 2019),
('A11', 'Ria Hermawati', '2001-09-23', 'Jl. Pahlawan Revolusi No. 7, Cibinong, Bogor', 'https://drive.google.com/open?id=1ARi3vWaO5HRgXOjqv3aMK6Nui8IJcg2t', 2019),
('A12', 'Siti fauziah', '2001-11-17', 'Jl. Cempaka Putih No. 11, Beji, Depok', 'https://drive.google.com/open?id=19kMFj8b-iX5kkat-uU2Hzc0vGX4BvmC4', 2019),
('A13', 'Shella Risky Amanda Putri', '2003-04-03', 'Jl. Dahlia No. 4, Sukmajaya, Depok', 'https://drive.google.com/open?id=1f8gMj0Uw-nZhVa87L-Oy8wBjwlG5_eRb', 2021),
('A14', 'Tania murniawati', '2000-12-13', 'Jl. Tulip No. 19, Bantar Gebang, Bekasi', 'https://drive.google.com/open?id=1skb6dzOp13RrgRY89lScSE_dy-zfpKc2', 2018),
('A15', 'Windy isni rani', '1995-04-14', 'Jl. Bougenville No. 6, Jatisampurna, Bekasi', 'https://drive.google.com/open?id=1wlFWbPE1V8r2GddO1tPdBZqljMhTYP6I', 2013),
('A16', 'Endar Tri Aditia', '2003-01-28', 'Jl. Seroja No. 13, Cimanggis, Depok', 'https://drive.google.com/open?id=1qRIn6IVB_-BczJcQ85dCmzI__Uzhber2', 2021),
('A17', 'Citra Octaviani', '1997-10-13', 'Jl. Wijaya Kusuma No. 21, Cilincing, Jakarta Utara', 'https://drive.google.com/open?id=1vsywqhCumvZzW2dyluJuOS9wriVz4SNP', 2015),
('A18', 'Firda Natasya', '2004-01-10', 'Jl. Kamboja No. 18, Pondok Melati, Bekasi', 'https://drive.google.com/open?id=1ZeY065jAsQP8bwzwcb-3L-vtUstUoi0i', 2022),
('A19', 'Nursaidah', '2001-12-12', 'Jl. Teratai No. 25, Duren Sawit, Jakarta Timur', 'https://drive.google.com/open?id=1pmyM5UfhwDpiFPy8l0ba7axfJNnzLGrZ', 2019),
('A20', 'Ainaia Syawalina novianti', '2004-11-22', 'Jl. Sakura No. 2, Mustika Jaya, Bekasi', 'https://drive.google.com/open?id=1qKnYHqNLIol_qH4KYkQw45jCQ79rd0dk', 2022),
('A21', 'Camelia', '2002-09-02', 'Jl. Mawar Merah No. 12, Bojong Rawalumbu, Bekasi', 'https://drive.google.com/open?id=11V1BDrRmzjQZLRCFi_76uvAHyV4cjBpq', 2020),
('A22', 'Dini Dwi Anjani', '2001-03-30', 'Jl. Melati Indah No. 8, Pondok Gede, Bekasi', 'https://drive.google.com/open?id=1YM2QCnSWGgc_xzlLWEzraQfb-nRvXZdw', 2019),
('A23', 'Putri Safitri', '2001-09-28', 'Jl. Anggrek Blok C-15, Jatiasih, Bekasi', 'https://drive.google.com/open?id=12kpxEvflX5IIka50QsZ7jHIGbIti0C8d', 2019),
('A24', 'Dila septia', '2003-03-02', 'Jl. Kenanga Raya No. 22, Bekasi Barat, Bekasi', 'https://drive.google.com/open?id=1LX9mXB-rUO4liTUKGBXb32MJk9VDZhH2', 2021),
('A25', 'kristin marda', '1999-01-24', 'Jl. Flamboyan No. 5, Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1CpVHA5qvWHMhN0amE7EeFNmEEgGUIDvm', 2017),
('A26', 'Citra Octaviani', '1997-10-13', 'Jl. Cendana No. 10, Bekasi Selatan, Bekasi', 'https://drive.google.com/open?id=1Zlhhv22h2ngjqscD1pHYnmTHdX20LbK1', 2015),
('A27', 'Pramestika endra putri', '1997-10-21', 'Jl. Diponegoro No. 17, Cakung, Jakarta Timur', 'https://drive.google.com/open?id=1BG23jUZ8U-KrNarrXZUmUUhXnjKvaw7G', 2015),
('A28', 'Rifa siti noer afifah', '2002-04-27', 'Jl. Sudirman Kav. 3, Jatinegara, Jakarta Timur', 'https://drive.google.com/open?id=1PUXUuZkgG0E-iASiATeexpxYZYvx65f-', 2020),
('A29', 'Marsha Mutiara Atmaja', '2004-01-06', 'Jl. Imam Bonjol No. 9, Matraman, Jakarta Timur', 'https://drive.google.com/open?id=1vARXpr0LBJYQdZdq0VQR-RfGqbc4ZGE6', 2022),
('A30', 'Mila Asmara', '2001-09-04', 'Jl. Kartini No. 14, Kelapa Gading, Jakarta Utara', 'https://drive.google.com/open?id=1i_IcWS4ACQ-6LaJfzVL3JGh-LnJy3YJC', 2019),
('A31', 'Nurul tri oktavia', '2003-10-21', 'Jl. Pahlawan Revolusi No. 7, Cibinong, Bogor', 'https://drive.google.com/open?id=1Ce3-zJjGnyOEJshqPTAmOqru3CJKpVAB', 2021),
('A32', 'Ayu Adi Soraya', '1999-04-24', 'Jl. Cempaka Putih No. 11, Beji, Depok', 'https://drive.google.com/open?id=1-oX7YYfp2ocH2hIe_Ivj8_4HphM8pbGv', 2017),
('A33', 'Mutiara Milikami', '1999-11-17', 'Jl. Dahlia No. 4, Sukmajaya, Depok', 'https://drive.google.com/open?id=1vfG989xKJwOIaEUw-QsEX0jLlNXEemcL', 2017),
('A34', 'Imelda Putri Sakinah Silalahi', '2004-05-05', 'Jl. Tulip No. 19, Bantar Gebang, Bekasi', 'https://drive.google.com/open?id=17mCPLLdOXGQ6mXDzW0yKJDFgRNEEXZv4', 2022),
('A35', 'Anisa Anggraini', '2003-09-04', 'Jl. Bougenville No. 6, Jatisampurna, Bekasi', 'https://drive.google.com/open?id=1GamxCWK-ucUzpqbq7P2pr3Spj6eUZmeu', 2021),
('A36', 'Fitria Damayanti', '2002-12-26', 'Jl. Seroja No. 13, Cimanggis, Depok', 'https://drive.google.com/open?id=1fhPDqEtp7lLaMsT37if154_osMZFYoiq', 2020),
('A37', 'Desi puspita sari', '2004-12-06', 'Jl. Wijaya Kusuma No. 21, Cilincing, Jakarta Utara', 'https://drive.google.com/open?id=1KKDMnN5rBLafKqpyJvGodxFI5ImmEO-I', 2022),
('A38', 'Inul Nurhasanah', '2002-03-13', 'Jl. Kamboja No. 18, Pondok Melati, Bekasi', 'https://drive.google.com/open?id=1SII7bg7p5Wk7Mjyof2_nS84L0lKFyAlr', 2020),
('A39', 'Mila Asmara', '2001-09-04', 'Jl. Teratai No. 25, Duren Sawit, Jakarta Timur', 'https://drive.google.com/open?id=1kRg3cn6gMW67b0dyDdZHUl3kBiH2HDot', 2019),
('A40', 'Frida Afriliani', '2004-04-17', 'Jl. Sakura No. 2, Mustika Jaya, Bekasi', 'https://drive.google.com/open?id=1xrXGmDorvQQb6B4VV3t5fG5VtqqE8U8Q', 2022),
('A41', 'Tiantri', '2002-06-26', 'Jl. Mawar Merah No. 12, Bojong Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1jQfqKSsd2xzY3W8lvqTtmlJxHYyWc-4H', 2020),
('A42', 'Risma jihan', '1999-07-25', 'Jl. Melati Indah No. 8, Pondok Gede, Bekasi', 'https://drive.google.com/open?id=1RnaSaowhRIKBnt3DcuVB39NGZSwrVTSQ', 2017),
('A43', 'Nural Karina Allamda', '2004-10-12', 'Jl. Anggrek Blok C-15, Jatiasih, Bekasi', 'https://drive.google.com/open?id=1_b2qnvdtCkqp-kvn20PD0PzPKsr9GuTw', 2022),
('A44', 'Sri Ranty', '2000-11-27', 'Jl. Kenanga Raya No. 22, Bekasi Barat, Bekasi', 'https://drive.google.com/open?id=1SGII47wezA8peJlWBjK5ohVw_lDVDn9j', 2018),
('A45', 'Nika Fatmi', '1999-05-18', 'Jl. Flamboyan No. 5, Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1rB2FteClQXAuSbUQOJwgydhFpHTM6ca6', 2017),
('A46', 'Revita Lina', '2004-01-11', 'Jl. Cendana No. 10, Bekasi Selatan, Bekasi', 'https://drive.google.com/open?id=1JrIyV7P-SU3BrjqNzMqe0eOs9GntAJAc', 2022),
('A47', 'Winda Prasiska', '1994-01-09', 'Jl. Diponegoro No. 17, Cakung, Jakarta Timur', 'https://drive.google.com/open?id=1RHxScdK819Q7Vg0PPOC1pxGy1QXW6_3Q', 2012),
('A48', 'Nadia Sarah Cahyanda', '2000-05-09', 'Jl. Sudirman Kav. 3, Jatinegara, Jakarta Timur', 'https://drive.google.com/open?id=1Hvl_y1JZlHOkUEHHlJ_-b10KaHfj1zbP', 2018),
('A49', 'Suci Indriati Mokodongan', '1999-08-21', 'Jl. Imam Bonjol No. 9, Matraman, Jakarta Timur', 'https://drive.google.com/open?id=177ZBnbft3Kvm_9AUdZE-w8wA_ORAyToo', 2017),
('A50', 'HUSNUL KHOTIMAH', '2001-08-28', 'Jl. Kartini No. 14, Kelapa Gading, Jakarta Utara', 'https://drive.google.com/open?id=1ri2VdM1xFLliKtH1wTQ4PIvjJD55i7no', 2019),
('A51', 'Ira Septiyani', '1997-09-03', 'Jl. Pahlawan Revolusi No. 7, Cibinong, Bogor', 'https://drive.google.com/open?id=1luVIZoaCXwO0cOv2HsPXpEhfU5of1a6X', 2015),
('A52', 'Dea Febriyanti', '2003-02-16', 'Jl. Cempaka Putih No. 11, Beji, Depok', 'https://drive.google.com/open?id=1GYwnyCAkQKyvyFR_b2XOkrkI72SEW3cq', 2021),
('A53', 'SriAyu', '2004-10-03', 'Jl. Dahlia No. 4, Sukmajaya, Depok', 'https://drive.google.com/open?id=1bCpqjZxa7hqtjq5KTayrzgkiykiqpSR0', 2022),
('A54', 'AULIA MEILINA', '2003-05-03', 'Jl. Tulip No. 19, Bantar Gebang, Bekasi', 'https://drive.google.com/open?id=1R5KE44gPKhVP5zXEDMIYVDDOFIpwMNpQ', 2021),
('A55', 'Yuningsih', '2003-10-22', 'Jl. Bougenville No. 6, Jatisampurna, Bekasi', 'https://drive.google.com/open?id=1wFDBkwbH9RCYFzo7YskrzDpz4yTpLFtq', 2021),
('A56', 'Syafi''na Haula Noza Jumirah', '2003-12-28', 'Jl. Seroja No. 13, Cimanggis, Depok', 'https://drive.google.com/open?id=1BPKAe1cdo6mOEPJtcIWXXVkEO9Az4uj3', 2021),
('A57', 'Nur Afiani', '2001-01-15', 'Jl. Wijaya Kusuma No. 21, Cilincing, Jakarta Utara', 'https://drive.google.com/open?id=1b7MAmLrikaN3-P3uy8hp7q-XC4gnQ7Nj', 2019),
('A58', 'Nabila Aryani Saputri', '2003-06-09', 'Jl. Kamboja No. 18, Pondok Melati, Bekasi', 'https://drive.google.com/open?id=1Q56OyBaMGkbL4BGhwqVSJVr6c4q9VzVp', 2021),
('A59', 'Susilowati Wibowo', '1998-04-20', 'Jl. Teratai No. 25, Duren Sawit, Jakarta Timur', 'https://drive.google.com/open?id=1KJX4QZ9vNOJ7MfIIGlA4ZNz8J6AuRQyr', 2016),
('A60', 'Dania febriati', '2004-02-06', 'Jl. Sakura No. 2, Mustika Jaya, Bekasi', 'https://drive.google.com/open?id=18A38ZR8iPzXyhTXNBqRs4DqIy68qsWoK', 2022),
('A61', 'Syifa Fauzia Amima', '1999-03-25', 'Jl. Mawar Merah No. 12, Bojong Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1g1cUxD8YIZesr7lD1-mHB905P50sHYRD', 2017),
('A62', 'Angelita Putri', '2003-07-17', 'Jl. Melati Indah No. 8, Pondok Gede, Bekasi', 'https://drive.google.com/open?id=1_hFf9e6ja51oyFINNt5xreScoFXahiBY', 2021),
('A63', 'Indy Mawarni Putri', '2004-03-15', 'Jl. Anggrek Blok C-15, Jatiasih, Bekasi', 'https://drive.google.com/open?id=1TNq6ilMh53ql_iLXzMg4iRiKzRaHkayh', 2022),
('A64', 'Vidya azzahra isdjianto', '2003-10-03', 'Jl. Kenanga Raya No. 22, Bekasi Barat, Bekasi', 'https://drive.google.com/open?id=1szN02Vf7aYXetpi2EtBfbnAi9R9e8abF', 2021),
('A65', 'PUTRI AYU MARIAM', '2003-03-19', 'Jl. Flamboyan No. 5, Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1liFRQpJ_lfISCBGT66vvyEdA8yhGLxGC', 2021),
('A66', 'Anggi Lestari', '2004-08-08', 'Jl. Cendana No. 10, Bekasi Selatan, Bekasi', 'https://drive.google.com/open?id=1FKZs3wBwYVNrMmGjkAtSuv7qNP59GCep', 2022),
('A67', 'Fatma Ayu Oktaviany', '1998-09-30', 'Jl. Diponegoro No. 17, Cakung, Jakarta Timur', 'https://drive.google.com/open?id=1-2QwEN3kglJWh3g1lKSA6Rxe5GzROejA', 2016),
('A68', 'Siti maysaroh', '2001-05-17', 'Jl. Sudirman Kav. 3, Jatinegara, Jakarta Timur', 'https://drive.google.com/open?id=1Q4WQAGbtf5wtedRQau5b1SShmQZd5KDA', 2019),
('A69', 'Nurul azizah', '2003-03-07', 'Jl. Imam Bonjol No. 9, Matraman, Jakarta Timur', 'https://drive.google.com/open?id=1TVRiBq5-rkydBI1XYZfCvzRf-o1WXOPk', 2021),
('A70', 'Devi Ismayanti Prasetio', '2002-06-07', 'Jl. Kartini No. 14, Kelapa Gading, Jakarta Utara', 'https://drive.google.com/open?id=1Zm5_BiN0IQlFfgtQ2jGc5wEyVgm8G6mE', 2020),
('A71', 'Putri Rahmawati', '2004-03-22', 'Jl. Pahlawan Revolusi No. 7, Cibinong, Bogor', 'https://drive.google.com/open?id=1BFn2XAEsdz9WifVPwNq1Z4Gdzn6esSyJ', 2022),
('A72', 'Nazila Umar Badjrie', '1998-01-26', 'Jl. Cempaka Putih No. 11, Beji, Depok', 'https://drive.google.com/open?id=1jXTDEoZ-a1J4eCXORestmku6KP6U7xPu', 2016),
('A73', 'Shidqiyyah Febriani', '2004-02-21', 'Jl. Dahlia No. 4, Sukmajaya, Depok', 'https://drive.google.com/open?id=1edUG93cYCdHr9xVJ21YiozPMiY6OSMGb', 2022),
('A74', 'Dahlia Hosana Maail', '2004-01-26', 'Jl. Tulip No. 19, Bantar Gebang, Bekasi', 'https://drive.google.com/open?id=13LpXtcreMrdnc6S2y68X_iYu0-GS6580', 2022),
('A75', 'Saharani azzahra', '2004-06-07', 'Jl. Bougenville No. 6, Jatisampurna, Bekasi', 'https://drive.google.com/open?id=1CHw4YcCC848869Ji7xJrktSqXpToym7-', 2022),
('A76', 'Sella Marchelina', '2002-06-20', 'Jl. Seroja No. 13, Cimanggis, Depok', 'https://drive.google.com/open?id=1ATdta-46XiuySVXGPnTP4t8ekNTRGGEx', 2020),
('A77', 'Irma yohanah', '2001-05-08', 'Jl. Wijaya Kusuma No. 21, Cilincing, Jakarta Utara', 'https://drive.google.com/open?id=1liqYHYs81WzX9qG1A8ibDj1L5XztCW35', 2019),
('A78', 'Shinta Nuraini', '2005-03-21', 'Jl. Kamboja No. 18, Pondok Melati, Bekasi', 'https://drive.google.com/open?id=1qzPIjfiKHcfco9yqyfgRyXzJQLa7cHx1', 2023),
('A79', 'CAHYA WAHYUNI', '2000-06-22', 'Jl. Teratai No. 25, Duren Sawit, Jakarta Timur', 'https://drive.google.com/open?id=1mwwAA2TjAhaHacMzTj-ZjMiVxX3s4T-x', 2018),
('A80', 'Sifa Fauziah', '2000-11-02', 'Jl. Sakura No. 2, Mustika Jaya, Bekasi', 'https://drive.google.com/open?id=1vPNIrcEAW3LbFRuXTL3B6GekdmwQyc25', 2018),
('A81', 'Rizka Sofia Nurhayati', '2004-10-05', 'Jl. Mawar Merah No. 12, Bojong Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1ZJu39uIIMzcdMOYVf6lLFRJLA7tE39Kr', 2022),
('A82', 'Adjani Rahmmadina', '2000-12-19', 'Jl. Melati Indah No. 8, Pondok Gede, Bekasi', 'https://drive.google.com/open?id=15fnd-m0uiIlVUH2qOMHLSwm-tBdvCC2b', 2018),
('A83', 'Eka citra anggraeni', '1995-08-29', 'Jl. Anggrek Blok C-15, Jatiasih, Bekasi', 'https://drive.google.com/open?id=1HG5Wt7t5Dnlt_xA4iMHrxHHq_CSj70hz', 2013),
('A84', 'WULAN PURNAMA SARI', '2004-11-08', 'Jl. Kenanga Raya No. 22, Bekasi Barat, Bekasi', 'https://drive.google.com/open?id=1V9pnTw02mrIKirqIXWSnF5LChqksZgG3', 2022),
('A85', 'Novitri Irawati', '2002-11-30', 'Jl. Flamboyan No. 5, Rawalumbu, Bekasi', 'https://drive.google.com/open?id=1tb0EecGW54zBALux3gjh66DObJvCu_gc', 2020),
('A86', 'Nova irviyanti', '2000-11-09', 'Jl. Cendana No. 10, Bekasi Selatan, Bekasi', 'https://drive.google.com/open?id=1zbgYtM659jqmyTfJIYBsZ5LpmqVhMRg9', 2018),
('A87', 'Intan Pratiwi', '2002-08-06', 'Jl. Diponegoro No. 17, Cakung, Jakarta Timur', 'https://drive.google.com/open?id=11QtoaLQDV_A_FkjLDvWmNhtJCsS84SU0', 2020),
('A88', 'Fenny Arsiaty', '2001-02-16', 'Jl. Sudirman Kav. 3, Jatinegara, Jakarta Timur', 'https://drive.google.com/open?id=15dMxDXvH1RvA4pPXtJSxzEirFm5wyHgE', 2019),
('A89', 'Siti Nuralifah', '2002-06-19', 'Jl. Imam Bonjol No. 9, Matraman, Jakarta Timur', 'https://drive.google.com/open?id=1dX9HPkgnBvi3kVAeUf-3cSHUQV5lEjYB', 2020),
('A90', 'Devi Al Fadila', '2003-03-23', 'Jl. Kartini No. 14, Kelapa Gading, Jakarta Utara', 'https://drive.google.com/open?id=17AGlktmyw9KguokybQpjU_k7ayx6c9fi', 2021),
('A91', 'Christina Putri Handayani Mouw', '2003-12-17', 'Jl. Pahlawan Revolusi No. 7, Cibinong, Bogor', 'https://drive.google.com/open?id=1faFbQjktqO5qFR-JxVbQbAUuZg7NKl9w', 2021),
('A92', 'Keira inara gandey', '2004-05-21', 'Jl. Cempaka Putih No. 11, Beji, Depok', 'https://drive.google.com/open?id=1Okvz9iK1LiexyP6lmUH3XmjPgcrWZvGa', 2022),
('A93', 'Lusi', '2004-06-03', 'Jl. Dahlia No. 4, Sukmajaya, Depok', 'https://drive.google.com/open?id=12Ax60P4NyfoEiHPfge6kfTyH7BtFZMkq', 2022),
('A94', 'Lusi', '2004-06-03', 'Jl. Tulip No. 19, Bantar Gebang, Bekasi', 'https://drive.google.com/open?id=17bYToJkt1Y4E8xa1TlGCx4utI8JcSXBx', 2022),
('A95', 'Tuti Nuraeni', '2003-08-28', 'Jl. Bougenville No. 6, Jatisampurna, Bekasi', 'https://drive.google.com/open?id=1nAzf_42cOtH-WB41yT58sbpSayY-wN1u', 2021),
('A96', 'Dela Puspita', '2003-02-28', 'Jl. Seroja No. 13, Cimanggis, Depok', 'https://drive.google.com/open?id=1IEuqXcFOX6dqOUzYIShuu4PiRpTANcfJ', 2021),
('A97', 'Muthia Nur', '2002-01-02', 'Jl. Wijaya Kusuma No. 21, Cilincing, Jakarta Utara', 'https://drive.google.com/open?id=1TwCrw3POt8h9zZYJgx3_ctOjM0o2t6En', 2020),
('A98', 'Devia Rachma Suci', '2002-12-04', 'Jl. Kamboja No. 18, Pondok Melati, Bekasi', 'https://drive.google.com/open?id=1o83q9RdSGNPqbCUsLbWsikPpjSNVmVN4', 2020),
('A99', 'Cantika Falentina', '2004-02-14', 'Jl. Teratai No. 25, Duren Sawit, Jakarta Timur', 'https://drive.google.com/open?id=14E40NS-Tu78NlPShbJss8ar93cWoGQ3k', 2022),
('A100', 'Shavira Putri Lembayung', '2004-04-16', 'Jl. Sakura No. 2, Mustika Jaya, Bekasi', 'https://drive.google.com/open?id=19mmberbgm9bwQeDP8eTclhdg3KanGPVl', 2022);

-- ---------------------- NILAI KANDIDAT --------------------------------
-- 100 kandidat x 8 kriteria = 800 baris. Skala 1..5 (integer).
INSERT INTO tb_nilai_kandidat (id_kandidat, id_kriteria, nilai_kandidat) VALUES
(1,1,3),(1,2,3),(1,3,5),(1,4,3),(1,5,3),(1,6,5),(1,7,5),(1,8,4),
(2,1,3),(2,2,4),(2,3,3),(2,4,3),(2,5,5),(2,6,3),(2,7,3),(2,8,5),
(3,1,3),(3,2,5),(3,3,4),(3,4,3),(3,5,3),(3,6,4),(3,7,3),(3,8,3),
(4,1,3),(4,2,4),(4,3,5),(4,4,3),(4,5,3),(4,6,3),(4,7,3),(4,8,3),
(5,1,5),(5,2,5),(5,3,3),(5,4,3),(5,5,4),(5,6,3),(5,7,3),(5,8,3),
(6,1,3),(6,2,5),(6,3,3),(6,4,3),(6,5,3),(6,6,3),(6,7,3),(6,8,4),
(7,1,3),(7,2,3),(7,3,4),(7,4,3),(7,5,3),(7,6,3),(7,7,3),(7,8,5),
(8,1,3),(8,2,3),(8,3,5),(8,4,5),(8,5,3),(8,6,5),(8,7,3),(8,8,3),
(9,1,3),(9,2,5),(9,3,3),(9,4,3),(9,5,3),(9,6,4),(9,7,3),(9,8,3),
(10,1,4),(10,2,4),(10,3,5),(10,4,5),(10,5,5),(10,6,3),(10,7,5),(10,8,3),
(11,1,3),(11,2,3),(11,3,3),(11,4,3),(11,5,4),(11,6,3),(11,7,3),(11,8,3),
(12,1,3),(12,2,3),(12,3,5),(12,4,4),(12,5,5),(12,6,3),(12,7,3),(12,8,3),
(13,1,4),(13,2,3),(13,3,3),(13,4,3),(13,5,3),(13,6,5),(13,7,3),(13,8,3),
(14,1,4),(14,2,4),(14,3,3),(14,4,4),(14,5,3),(14,6,3),(14,7,3),(14,8,3),
(15,1,5),(15,2,3),(15,3,3),(15,4,4),(15,5,5),(15,6,3),(15,7,4),(15,8,5),
(16,1,5),(16,2,3),(16,3,5),(16,4,3),(16,5,3),(16,6,3),(16,7,3),(16,8,3),
(17,1,3),(17,2,3),(17,3,3),(17,4,3),(17,5,3),(17,6,3),(17,7,4),(17,8,5),
(18,1,3),(18,2,5),(18,3,5),(18,4,3),(18,5,3),(18,6,3),(18,7,3),(18,8,3),
(19,1,3),(19,2,3),(19,3,4),(19,4,3),(19,5,3),(19,6,3),(19,7,3),(19,8,3),
(20,1,5),(20,2,3),(20,3,5),(20,4,4),(20,5,3),(20,6,3),(20,7,4),(20,8,5),
(21,1,3),(21,2,3),(21,3,3),(21,4,5),(21,5,3),(21,6,3),(21,7,3),(21,8,3),
(22,1,5),(22,2,3),(22,3,3),(22,4,3),(22,5,3),(22,6,3),(22,7,5),(22,8,5),
(23,1,3),(23,2,3),(23,3,3),(23,4,5),(23,5,3),(23,6,3),(23,7,3),(23,8,3),
(24,1,3),(24,2,3),(24,3,5),(24,4,4),(24,5,3),(24,6,5),(24,7,5),(24,8,5),
(25,1,3),(25,2,5),(25,3,3),(25,4,3),(25,5,4),(25,6,4),(25,7,3),(25,8,3),
(26,1,3),(26,2,4),(26,3,5),(26,4,3),(26,5,3),(26,6,3),(26,7,3),(26,8,4),
(27,1,3),(27,2,3),(27,3,3),(27,4,3),(27,5,5),(27,6,3),(27,7,3),(27,8,5),
(28,1,3),(28,2,3),(28,3,4),(28,4,3),(28,5,5),(28,6,5),(28,7,3),(28,8,4),
(29,1,3),(29,2,4),(29,3,3),(29,4,3),(29,5,4),(29,6,4),(29,7,3),(29,8,3),
(30,1,5),(30,2,3),(30,3,3),(30,4,5),(30,5,4),(30,6,4),(30,7,3),(30,8,5),
(31,1,3),(31,2,3),(31,3,5),(31,4,3),(31,5,3),(31,6,4),(31,7,3),(31,8,3),
(32,1,5),(32,2,3),(32,3,3),(32,4,4),(32,5,5),(32,6,5),(32,7,3),(32,8,5),
(33,1,3),(33,2,3),(33,3,3),(33,4,5),(33,5,3),(33,6,3),(33,7,3),(33,8,3),
(34,1,3),(34,2,3),(34,3,3),(34,4,5),(34,5,3),(34,6,4),(34,7,3),(34,8,3),
(35,1,3),(35,2,5),(35,3,3),(35,4,5),(35,5,5),(35,6,5),(35,7,3),(35,8,3),
(36,1,5),(36,2,3),(36,3,5),(36,4,3),(36,5,4),(36,6,3),(36,7,3),(36,8,5),
(37,1,3),(37,2,5),(37,3,3),(37,4,4),(37,5,3),(37,6,4),(37,7,3),(37,8,5),
(38,1,3),(38,2,4),(38,3,5),(38,4,5),(38,5,3),(38,6,3),(38,7,3),(38,8,3),
(39,1,3),(39,2,5),(39,3,5),(39,4,4),(39,5,3),(39,6,5),(39,7,3),(39,8,3),
(40,1,5),(40,2,4),(40,3,3),(40,4,3),(40,5,3),(40,6,3),(40,7,3),(40,8,5),
(41,1,3),(41,2,3),(41,3,3),(41,4,3),(41,5,3),(41,6,3),(41,7,3),(41,8,3),
(42,1,3),(42,2,5),(42,3,5),(42,4,3),(42,5,3),(42,6,3),(42,7,4),(42,8,4),
(43,1,3),(43,2,3),(43,3,5),(43,4,3),(43,5,3),(43,6,4),(43,7,3),(43,8,5),
(44,1,5),(44,2,4),(44,3,5),(44,4,3),(44,5,4),(44,6,3),(44,7,4),(44,8,4),
(45,1,3),(45,2,3),(45,3,3),(45,4,5),(45,5,3),(45,6,4),(45,7,3),(45,8,5),
(46,1,5),(46,2,5),(46,3,3),(46,4,5),(46,5,3),(46,6,3),(46,7,3),(46,8,4),
(47,1,3),(47,2,3),(47,3,3),(47,4,3),(47,5,3),(47,6,3),(47,7,3),(47,8,5),
(48,1,5),(48,2,4),(48,3,5),(48,4,3),(48,5,3),(48,6,3),(48,7,3),(48,8,4),
(49,1,3),(49,2,4),(49,3,5),(49,4,4),(49,5,3),(49,6,3),(49,7,3),(49,8,3),
(50,1,3),(50,2,5),(50,3,3),(50,4,5),(50,5,4),(50,6,5),(50,7,3),(50,8,3),
(51,1,4),(51,2,3),(51,3,4),(51,4,4),(51,5,4),(51,6,4),(51,7,3),(51,8,3),
(52,1,4),(52,2,3),(52,3,3),(52,4,3),(52,5,4),(52,6,5),(52,7,3),(52,8,3),
(53,1,3),(53,2,3),(53,3,3),(53,4,3),(53,5,5),(53,6,5),(53,7,5),(53,8,5),
(54,1,3),(54,2,3),(54,3,3),(54,4,3),(54,5,3),(54,6,3),(54,7,3),(54,8,3),
(55,1,3),(55,2,5),(55,3,4),(55,4,3),(55,5,3),(55,6,3),(55,7,4),(55,8,3),
(56,1,4),(56,2,3),(56,3,4),(56,4,3),(56,5,5),(56,6,4),(56,7,3),(56,8,4),
(57,1,3),(57,2,4),(57,3,4),(57,4,3),(57,5,3),(57,6,3),(57,7,3),(57,8,3),
(58,1,4),(58,2,3),(58,3,4),(58,4,3),(58,5,5),(58,6,5),(58,7,3),(58,8,5),
(59,1,3),(59,2,4),(59,3,5),(59,4,5),(59,5,3),(59,6,3),(59,7,3),(59,8,3),
(60,1,3),(60,2,3),(60,3,3),(60,4,3),(60,5,4),(60,6,3),(60,7,3),(60,8,3),
(61,1,5),(61,2,3),(61,3,3),(61,4,3),(61,5,5),(61,6,3),(61,7,5),(61,8,3),
(62,1,4),(62,2,3),(62,3,4),(62,4,3),(62,5,3),(62,6,3),(62,7,3),(62,8,4),
(63,1,3),(63,2,5),(63,3,3),(63,4,3),(63,5,4),(63,6,5),(63,7,5),(63,8,3),
(64,1,5),(64,2,3),(64,3,3),(64,4,3),(64,5,3),(64,6,4),(64,7,3),(64,8,3),
(65,1,5),(65,2,4),(65,3,3),(65,4,3),(65,5,4),(65,6,4),(65,7,3),(65,8,5),
(66,1,3),(66,2,3),(66,3,3),(66,4,5),(66,5,3),(66,6,3),(66,7,3),(66,8,3),
(67,1,3),(67,2,3),(67,3,3),(67,4,4),(67,5,3),(67,6,3),(67,7,3),(67,8,3),
(68,1,4),(68,2,3),(68,3,5),(68,4,3),(68,5,3),(68,6,3),(68,7,3),(68,8,3),
(69,1,3),(69,2,3),(69,3,3),(69,4,5),(69,5,4),(69,6,3),(69,7,3),(69,8,3),
(70,1,3),(70,2,5),(70,3,5),(70,4,3),(70,5,4),(70,6,3),(70,7,3),(70,8,5),
(71,1,5),(71,2,3),(71,3,3),(71,4,3),(71,5,4),(71,6,3),(71,7,4),(71,8,3),
(72,1,3),(72,2,5),(72,3,4),(72,4,3),(72,5,4),(72,6,4),(72,7,3),(72,8,3),
(73,1,4),(73,2,3),(73,3,5),(73,4,4),(73,5,3),(73,6,4),(73,7,3),(73,8,3),
(74,1,3),(74,2,3),(74,3,3),(74,4,3),(74,5,3),(74,6,3),(74,7,3),(74,8,5),
(75,1,3),(75,2,5),(75,3,3),(75,4,4),(75,5,5),(75,6,3),(75,7,4),(75,8,3),
(76,1,4),(76,2,3),(76,3,3),(76,4,4),(76,5,3),(76,6,3),(76,7,4),(76,8,4),
(77,1,3),(77,2,5),(77,3,5),(77,4,3),(77,5,3),(77,6,4),(77,7,3),(77,8,4),
(78,1,3),(78,2,5),(78,3,3),(78,4,3),(78,5,5),(78,6,5),(78,7,4),(78,8,5),
(79,1,3),(79,2,3),(79,3,3),(79,4,4),(79,5,3),(79,6,3),(79,7,3),(79,8,5),
(80,1,5),(80,2,4),(80,3,4),(80,4,4),(80,5,3),(80,6,4),(80,7,4),(80,8,4),
(81,1,3),(81,2,3),(81,3,4),(81,4,3),(81,5,3),(81,6,3),(81,7,3),(81,8,4),
(82,1,4),(82,2,5),(82,3,3),(82,4,3),(82,5,3),(82,6,4),(82,7,3),(82,8,5),
(83,1,3),(83,2,3),(83,3,4),(83,4,3),(83,5,3),(83,6,3),(83,7,4),(83,8,5),
(84,1,3),(84,2,5),(84,3,3),(84,4,3),(84,5,3),(84,6,5),(84,7,4),(84,8,5),
(85,1,3),(85,2,4),(85,3,3),(85,4,3),(85,5,3),(85,6,3),(85,7,3),(85,8,3),
(86,1,5),(86,2,5),(86,3,4),(86,4,5),(86,5,3),(86,6,3),(86,7,3),(86,8,3),
(87,1,4),(87,2,3),(87,3,3),(87,4,5),(87,5,4),(87,6,5),(87,7,4),(87,8,3),
(88,1,3),(88,2,3),(88,3,5),(88,4,3),(88,5,4),(88,6,3),(88,7,3),(88,8,5),
(89,1,3),(89,2,3),(89,3,5),(89,4,4),(89,5,5),(89,6,4),(89,7,5),(89,8,3),
(90,1,4),(90,2,5),(90,3,4),(90,4,4),(90,5,3),(90,6,3),(90,7,3),(90,8,4),
(91,1,3),(91,2,3),(91,3,3),(91,4,3),(91,5,3),(91,6,4),(91,7,3),(91,8,3),
(92,1,5),(92,2,3),(92,3,3),(92,4,4),(92,5,4),(92,6,3),(92,7,3),(92,8,3),
(93,1,3),(93,2,3),(93,3,3),(93,4,3),(93,5,3),(93,6,3),(93,7,4),(93,8,3),
(94,1,3),(94,2,5),(94,3,3),(94,4,3),(94,5,5),(94,6,3),(94,7,3),(94,8,4),
(95,1,5),(95,2,5),(95,3,3),(95,4,3),(95,5,3),(95,6,3),(95,7,4),(95,8,5),
(96,1,4),(96,2,3),(96,3,3),(96,4,5),(96,5,4),(96,6,3),(96,7,5),(96,8,4),
(97,1,3),(97,2,3),(97,3,5),(97,4,3),(97,5,3),(97,6,4),(97,7,3),(97,8,3),
(98,1,4),(98,2,3),(98,3,5),(98,4,4),(98,5,3),(98,6,5),(98,7,3),(98,8,5),
(99,1,3),(99,2,5),(99,3,5),(99,4,4),(99,5,3),(99,6,3),(99,7,4),(99,8,4),
(100,1,3),(100,2,4),(100,3,3),(100,4,3),(100,5,5),(100,6,3),(100,7,3),(100,8,4);

-- =====================================================================
-- VERIFIKASI (opsional, jalankan setelah import):
--   SELECT COUNT(*) FROM tb_kandidat;        -- 100
--   SELECT COUNT(*) FROM tb_nilai_kandidat;  -- 800
--   SELECT COUNT(*) FROM tb_kriteria;        -- 8
--   SELECT COUNT(*) FROM tb_profil_ideal;    -- 40  (5 lowongan x 8 kriteria)
-- =====================================================================
