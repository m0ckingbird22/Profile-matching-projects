package com.bkk.spk.model;

import java.time.LocalDateTime;

public class HasilAkhir {
    private int idHasilAkhir;
    private Kandidat kandidat;
    private Lowongan lowongan;
    private Admin admin;
    private double ncf;         // Nilai rata-rata Core Factor
    private double nsf;         // Nilai rata-rata Secondary Factor
    private double nilaiTotal;  // (ncf * 0.6) + (nsf * 0.4) -> bobot standar Profile Matching
    private int ranking;
    private LocalDateTime tanggalProses;

    public HasilAkhir() {}

    public HasilAkhir(Kandidat kandidat, Lowongan lowongan, Admin admin, double ncf, double nsf, double nilaiTotal) {
        this.kandidat = kandidat;
        this.lowongan = lowongan;
        this.admin = admin;
        this.ncf = ncf;
        this.nsf = nsf;
        this.nilaiTotal = nilaiTotal;
    }

    public int getIdHasilAkhir() { return idHasilAkhir; }
    public void setIdHasilAkhir(int idHasilAkhir) { this.idHasilAkhir = idHasilAkhir; }

    public Kandidat getKandidat() { return kandidat; }
    public void setKandidat(Kandidat kandidat) { this.kandidat = kandidat; }

    public Lowongan getLowongan() { return lowongan; }
    public void setLowongan(Lowongan lowongan) { this.lowongan = lowongan; }

    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }

    public double getNcf() { return ncf; }
    public void setNcf(double ncf) { this.ncf = ncf; }

    public double getNsf() { return nsf; }
    public void setNsf(double nsf) { this.nsf = nsf; }

    public double getNilaiTotal() { return nilaiTotal; }
    public void setNilaiTotal(double nilaiTotal) { this.nilaiTotal = nilaiTotal; }

    public int getRanking() { return ranking; }
    public void setRanking(int ranking) { this.ranking = ranking; }

    public LocalDateTime getTanggalProses() { return tanggalProses; }
    public void setTanggalProses(LocalDateTime tanggalProses) { this.tanggalProses = tanggalProses; }
}
