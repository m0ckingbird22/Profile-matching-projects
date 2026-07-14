package com.bkk.spk.model;

public class Lowongan {
    private int idLowongan;
    private Perusahaan perusahaan; // relasi -> hasil JOIN diisi oleh DAO
    private String posisi;
    private String deskripsi;
    private int kuota;
    private String status; // "BUKA" atau "TUTUP"

    public Lowongan() {}

    public Lowongan(int idLowongan, Perusahaan perusahaan, String posisi, String deskripsi, int kuota, String status) {
        this.idLowongan = idLowongan;
        this.perusahaan = perusahaan;
        this.posisi = posisi;
        this.deskripsi = deskripsi;
        this.kuota = kuota;
        this.status = status;
    }

    public Lowongan(Perusahaan perusahaan, String posisi, String deskripsi, int kuota, String status) {
        this.perusahaan = perusahaan;
        this.posisi = posisi;
        this.deskripsi = deskripsi;
        this.kuota = kuota;
        this.status = status;
    }

    public int getIdLowongan() { return idLowongan; }
    public void setIdLowongan(int idLowongan) { this.idLowongan = idLowongan; }

    public Perusahaan getPerusahaan() { return perusahaan; }
    public void setPerusahaan(Perusahaan perusahaan) { this.perusahaan = perusahaan; }

    public String getPosisi() { return posisi; }
    public void setPosisi(String posisi) { this.posisi = posisi; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public int getKuota() { return kuota; }
    public void setKuota(int kuota) { this.kuota = kuota; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        String namaPerusahaan = (perusahaan != null) ? perusahaan.getNamaPerusahaan() : "-";
        return posisi + " - " + namaPerusahaan;
    }
}
