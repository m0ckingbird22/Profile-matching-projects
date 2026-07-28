-- ------------------------------------------------------------------
-- Update nama kriteria (CF & SF) sesuai permintaan user
-- Jalankan sekali di database yang sudah ada data kandidat/nilainya.
-- Hanya mengubah nama_kriteria, id & kode (C1..C8) tetap -> data
-- di tb_nilai_kandidat, tb_profil_ideal, tb_hasil_gap TIDAK terpengaruh.
-- ------------------------------------------------------------------

UPDATE tb_kriteria SET nama_kriteria = 'Kompetensi Siswa'        WHERE kode_kriteria = 'C1';
UPDATE tb_kriteria SET nama_kriteria = 'Kesesuaian Bidang Kerja' WHERE kode_kriteria = 'C2';
UPDATE tb_kriteria SET nama_kriteria = 'Kemampuan Komunikasi'    WHERE kode_kriteria = 'C3';
UPDATE tb_kriteria SET nama_kriteria = 'Sikap dan Etika'         WHERE kode_kriteria = 'C4';
UPDATE tb_kriteria SET nama_kriteria = 'Nilai Akademik'          WHERE kode_kriteria = 'C5';
UPDATE tb_kriteria SET nama_kriteria = 'Minat Siswa'             WHERE kode_kriteria = 'C6';
UPDATE tb_kriteria SET nama_kriteria = 'Pengalaman PKL'          WHERE kode_kriteria = 'C7';
UPDATE tb_kriteria SET nama_kriteria = 'Kedisiplinan'            WHERE kode_kriteria = 'C8';

-- Verifikasi (harus tampil 8 baris dengan nama baru):
-- SELECT kode_kriteria, nama_kriteria, jenis_faktor FROM tb_kriteria ORDER BY kode_kriteria;
