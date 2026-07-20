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
-- 7. tb_siswa  -> kandidat (siswa SMK) yang akan dinilai
-- =====================================================================
CREATE TABLE tb_siswa (
    id_siswa     INT AUTO_INCREMENT PRIMARY KEY,
    nisn         VARCHAR(20)  NOT NULL UNIQUE,
    nama         VARCHAR(100) NOT NULL,
    tanggal_lahir DATE,
    alamat       VARCHAR(255),
    link_cv      VARCHAR(255),
    jurusan      VARCHAR(50),
    kelas        VARCHAR(20),
    tahun_lulus  INT,
    INDEX idx_siswa_nama (nama)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. tb_nilai_siswa  -> nilai asli tiap siswa per kriteria
--    (bukan nilai gap; gap dihitung on-the-fly di ProfileMatchingService)
-- =====================================================================
CREATE TABLE tb_nilai_siswa (
    id_nilai        INT AUTO_INCREMENT PRIMARY KEY,
    id_siswa        INT NOT NULL,
    id_kriteria     INT NOT NULL,
    nilai_kandidat  DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_ns_siswa
        FOREIGN KEY (id_siswa) REFERENCES tb_siswa(id_siswa)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ns_kriteria
        FOREIGN KEY (id_kriteria) REFERENCES tb_kriteria(id_kriteria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    UNIQUE KEY uq_ns_siswa_kriteria (id_siswa, id_kriteria)
) ENGINE=InnoDB;

-- =====================================================================
-- 9. tb_hasil_gap  -> output hitungan Service: gap & bobot per siswa per lowongan
--    Diisi oleh ProfileMatchingService (lewat HasilGapDAO.insertBatch),
--    BUKAN input manual user.
-- =====================================================================
CREATE TABLE tb_hasil_gap (
    id_hasil_gap INT AUTO_INCREMENT PRIMARY KEY,
    id_siswa     INT NOT NULL,
    id_lowongan  INT NOT NULL,
    id_kriteria  INT NOT NULL,
    nilai_gap    INT NOT NULL,
    bobot_nilai  DECIMAL(4,2) NOT NULL,
    CONSTRAINT fk_hg_siswa
        FOREIGN KEY (id_siswa) REFERENCES tb_siswa(id_siswa)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_hg_lowongan
        FOREIGN KEY (id_lowongan) REFERENCES tb_lowongan(id_lowongan)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_hg_kriteria
        FOREIGN KEY (id_kriteria) REFERENCES tb_kriteria(id_kriteria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_hg_siswa_lowongan (id_siswa, id_lowongan)
) ENGINE=InnoDB;

-- =====================================================================
-- 10. tb_hasil_akhir  -> NCF, NSF, nilai total, ranking; tanggal otomatis
-- =====================================================================
CREATE TABLE tb_hasil_akhir (
    id_hasil_akhir INT AUTO_INCREMENT PRIMARY KEY,
    id_siswa       INT NOT NULL,
    id_lowongan    INT NOT NULL,
    id_admin       INT NOT NULL,
    ncf            DECIMAL(5,2) NOT NULL,
    nsf            DECIMAL(5,2) NOT NULL,
    nilai_total    DECIMAL(5,2) NOT NULL,
    ranking        INT NOT NULL,
    tanggal_proses DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ha_siswa
        FOREIGN KEY (id_siswa) REFERENCES tb_siswa(id_siswa)
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
-- kriteria -> profil_ideal -> siswa -> nilai_siswa -> bobot_gap.
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
('PT Astra Honda Motor',          'Jl. Gaya Motor Raya No. 40, Jakarta Utara',  'Otomotif'),
('PT Telkom Indonesia Tbk',       'Jl. Japati No. 1, Bandung',                   'Telekomunikasi'),
('PT Pertamina (Persero)',        'Jl. Medan Merdeka Timur 1A, Jakarta Pusat',   'Energi'),
('CV Berkah Teknologi Nusantara', 'Jl. Soekarno Hatta No. 452, Malang',          'Jasa IT');

-- ---------------------- LOWONGAN -------------------------------------
-- id_perusahaan: 1..4
INSERT INTO tb_lowongan (id_perusahaan, posisi, deskripsi, kuota, status) VALUES
(1, 'Operator Produksi',  'Mengoperasikan mesin perakitan sepeda motor. Jam kerja shift pagi/sore.', 5, 'BUKA'),
(2, 'Junior Network Technician', 'Instalasi & maintenance jaringan kabel fiber optik pelanggan.',     3, 'BUKA'),
(3, 'Customer Service Stasiun Pengisian', 'Melayani pelanggan di SPBU retail. Komunikatif & ramah.',  4, 'BUKA'),
(4, 'Web Developer Intern', 'Membantu tim mengembangkan aplikasi internal perusahaan (PHP/MySQL).',   2, 'BUKA'),
(2, 'Admin Data Entry',    'Input data pelanggan ke sistem CRM. Teliti & disiplin.',                   2, 'TUTUP');

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
-- Nilai target per lowongan per kriteria (skala 0..100).
-- Diisi utk lowongan 1 & 2 (lowongan lain bisa ditambah user via UI).
-- id_lowongan, C1,   C2,   C3,   C4,   C5,   C6,   C7,   C8
--   1         80    85    80    80    75    80    75    80
--   2         75    85    85    80    75    85    80    80
INSERT INTO tb_profil_ideal (id_lowongan, id_kriteria, nilai_target) VALUES
(1, 1, 80), (1, 2, 85), (1, 3, 80), (1, 4, 80),
(1, 5, 75), (1, 6, 80), (1, 7, 75), (1, 8, 80),
(2, 1, 75), (2, 2, 85), (2, 3, 85), (2, 4, 80),
(2, 5, 75), (2, 6, 85), (2, 7, 80), (2, 8, 80);

-- ---------------------- SISWA ----------------------------------------
INSERT INTO tb_siswa (nisn, nama, tanggal_lahir, alamat, link_cv, jurusan, kelas, tahun_lulus) VALUES
('0051234567', 'Ahmad Fauzan',       '2006-03-12', 'Jl. Melati No. 10, Malang',   'drive.google.com/cv/ahmad',    'Teknik Kendaraan Ringan', 'XII TKR 1', 2025),
('0051234568', 'Siti Nuraini',       '2006-05-22', 'Jl. Kenanga No. 5, Malang',   'drive.google.com/cv/siti',     'Rekayasa Perangkat Lunak', 'XII RPL 2', 2025),
('0051234569', 'Bagas Dwi Pratama',  '2006-01-08', 'Jl. Mawar No. 17, Batu',      'drive.google.com/cv/bagas',    'Teknik Komputer Jaringan', 'XII TKJ 1', 2025),
('0051234570', 'Dewi Lestari',       '2006-07-30', 'Jl. Anggrek No. 3, Malang',   'drive.google.com/cv/dewi',     'Akuntansi dan Keuangan',   'XII AKL 1', 2025),
('0051234571', 'Rizky Ramadhan',     '2006-02-14', 'Jl. Flamboyan No. 8, Malang', 'drive.google.com/cv/rizky',    'Teknik Kendaraan Ringan',  'XII TKR 2', 2025),
('0051234572', 'Putri Ayu Ningrum',  '2006-09-19', 'Jl. Dahlia No. 22, Singosari','drive.google.com/cv/putri',    'Rekayasa Perangkat Lunak', 'XII RPL 1', 2025),
('0051234573', 'Fajar Nugroho',      '2006-04-03', 'Jl. Seroja No. 14, Malang',   'drive.google.com/cv/fajar',    'Teknik Komputer Jaringan', 'XII TKJ 2', 2025),
('0051234574', 'Nadia Safitri',      '2006-11-25', 'Jl. Cempaka No. 9, Malang',   'drive.google.com/cv/nadia',    'Akuntansi dan Keuangan',   'XII AKL 2', 2025);

-- ---------------------- NILAI SISWA ----------------------------------
-- 8 siswa x 8 kriteria = 64 baris. Skala 0..100.
-- Sengaja dibuat bervariasi supaya ranking yang muncul nanti tidak seri.
INSERT INTO tb_nilai_siswa (id_siswa, id_kriteria, nilai_kandidat) VALUES
-- Ahmad Fauzan (id_siswa=1) - rata-rata tinggi
(1,1,85),(1,2,90),(1,3,80),(1,4,82),(1,5,78),(1,6,88),(1,7,76),(1,8,80),
-- Siti Nuraini (2) - sangat tinggi
(2,1,90),(2,2,95),(2,3,92),(2,4,85),(2,5,88),(2,6,93),(2,7,90),(2,8,87),
-- Bagas Dwi Pratama (3)
(3,1,75),(3,2,80),(3,3,78),(3,4,77),(3,5,72),(3,6,82),(3,7,74),(3,8,75),
-- Dewi Lestari (4)
(4,1,82),(4,2,78),(4,3,85),(4,4,80),(4,5,80),(4,6,76),(4,7,82),(4,8,84),
-- Rizky Ramadhan (5)
(5,1,78),(5,2,85),(5,3,72),(5,4,75),(5,5,70),(5,6,82),(5,7,73),(5,8,77),
-- Putri Ayu Ningrum (6) - tinggi
(6,1,88),(6,2,92),(6,3,86),(6,4,84),(6,5,85),(6,6,90),(6,7,88),(6,8,86),
-- Fajar Nugroho (7) - menengah
(7,1,72),(7,2,75),(7,3,78),(7,4,76),(7,5,68),(7,6,74),(7,7,72),(7,8,75),
-- Nadia Safitri (8) - menengah-atas
(8,1,80),(8,2,82),(8,3,85),(8,4,83),(8,5,78),(8,6,80),(8,7,82),(8,8,85);

-- =====================================================================
-- VERIFIKASI (opsional, jalankan setelah import):
--   SELECT COUNT(*) FROM tb_siswa;          -- 8
--   SELECT COUNT(*) FROM tb_nilai_siswa;    -- 64
--   SELECT COUNT(*) FROM tb_kriteria;       -- 8
--   SELECT COUNT(*) FROM tb_profil_ideal;   -- 16
-- =====================================================================
