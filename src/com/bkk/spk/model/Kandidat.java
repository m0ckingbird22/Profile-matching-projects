package com.bkk.spk.model;

import java.time.LocalDate;

public class Kandidat {
    private int idKandidat;
    private String nisn;
    private String nama;
    private LocalDate tanggalLahir;
    private String alamat;
    private String linkCv;
    private int tahunLulus;

    public Kandidat() {}

    public int getIdKandidat() { return idKandidat; }
    public void setIdKandidat(int idKandidat) { this.idKandidat = idKandidat; }

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

    public int getTahunLulus() { return tahunLulus; }
    public void setTahunLulus(int tahunLulus) { this.tahunLulus = tahunLulus; }

    @Override
    public String toString() {
        return nama + " (" + nisn + ")";
    }
}
