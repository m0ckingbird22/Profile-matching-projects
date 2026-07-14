package com.bkk.spk.model;

public class ProfilIdeal {
    private int idProfilIdeal;
    private Lowongan lowongan;
    private Kriteria kriteria;
    private double nilaiTarget;

    public ProfilIdeal() {}

    public ProfilIdeal(int idProfilIdeal, Lowongan lowongan, Kriteria kriteria, double nilaiTarget) {
        this.idProfilIdeal = idProfilIdeal;
        this.lowongan = lowongan;
        this.kriteria = kriteria;
        this.nilaiTarget = nilaiTarget;
    }

    public ProfilIdeal(Lowongan lowongan, Kriteria kriteria, double nilaiTarget) {
        this.lowongan = lowongan;
        this.kriteria = kriteria;
        this.nilaiTarget = nilaiTarget;
    }

    public int getIdProfilIdeal() { return idProfilIdeal; }
    public void setIdProfilIdeal(int idProfilIdeal) { this.idProfilIdeal = idProfilIdeal; }

    public Lowongan getLowongan() { return lowongan; }
    public void setLowongan(Lowongan lowongan) { this.lowongan = lowongan; }

    public Kriteria getKriteria() { return kriteria; }
    public void setKriteria(Kriteria kriteria) { this.kriteria = kriteria; }

    public double getNilaiTarget() { return nilaiTarget; }
    public void setNilaiTarget(double nilaiTarget) { this.nilaiTarget = nilaiTarget; }
}
