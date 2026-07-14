package com.bkk.spk.model;

public class Perusahaan {
    private int idPerusahaan;
    private String namaPerusahaan;
    private String alamat;
    private String bidangIndustri;

    public Perusahaan() {}

    public Perusahaan(int idPerusahaan, String namaPerusahaan, String alamat, String bidangIndustri) {
        this.idPerusahaan = idPerusahaan;
        this.namaPerusahaan = namaPerusahaan;
        this.alamat = alamat;
        this.bidangIndustri = bidangIndustri;
    }

    public Perusahaan(String namaPerusahaan, String alamat, String bidangIndustri) {
        this.namaPerusahaan = namaPerusahaan;
        this.alamat = alamat;
        this.bidangIndustri = bidangIndustri;
    }

    public int getIdPerusahaan() { return idPerusahaan; }
    public void setIdPerusahaan(int idPerusahaan) { this.idPerusahaan = idPerusahaan; }

    public String getNamaPerusahaan() { return namaPerusahaan; }
    public void setNamaPerusahaan(String namaPerusahaan) { this.namaPerusahaan = namaPerusahaan; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getBidangIndustri() { return bidangIndustri; }
    public void setBidangIndustri(String bidangIndustri) { this.bidangIndustri = bidangIndustri; }

    @Override
    public String toString() {
        return namaPerusahaan;
    }
}
