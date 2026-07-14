package com.bkk.spk.model;

import java.time.LocalDate;

public class Siswa {
    private int idSiswa;
    private String nisn;
    private String nama;
    private LocalDate tanggalLahir;
    private String alamat;
    private String linkCv;
    private String jurusan;
    private String kelas;
    private int tahunLulus;

    public Siswa() {}

    public int getIdSiswa() { return idSiswa; }
    public void setIdSiswa(int idSiswa) { this.idSiswa = idSiswa; }

    public String getNisn() { return nisn; }
    public void setNisn(String nisn) { this.nisn = nisn; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public LocalDate getTanggalLahir() { return tanggalLahir; }
    public void setTanggalLahir(LocalDate tanggalLahir) { this.tanggalLahir = tanggalLahir; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getLinkCv() { return linkCv; }
    public void setLinkCv(String linkCv) { this.linkCv = linkCv; }

    public String getJurusan() { return jurusan; }
    public void setJurusan(String jurusan) { this.jurusan = jurusan; }

    public String getKelas() { return kelas; }
    public void setKelas(String kelas) { this.kelas = kelas; }

    public int getTahunLulus() { return tahunLulus; }
    public void setTahunLulus(int tahunLulus) { this.tahunLulus = tahunLulus; }

    @Override
    public String toString() {
        return nama + " (" + nisn + ")";
    }
}
