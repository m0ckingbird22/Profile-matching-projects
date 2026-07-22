package com.bkk.spk.model;

public class HasilGap {
    private int idHasilGap;
    private Kandidat kandidat;
    private Lowongan lowongan;
    private Kriteria kriteria;
    private int nilaiGap;       // nilai_kandidat - nilai_target
    private double bobotNilai;  // hasil lookup dari tabel bobot gap

    public HasilGap() {}

    public HasilGap(Kandidat kandidat, Lowongan lowongan, Kriteria kriteria, int nilaiGap, double bobotNilai) {
        this.kandidat = kandidat;
        this.lowongan = lowongan;
        this.kriteria = kriteria;
        this.nilaiGap = nilaiGap;
        this.bobotNilai = bobotNilai;
    }

    public int getIdHasilGap() { return idHasilGap; }
    public void setIdHasilGap(int idHasilGap) { this.idHasilGap = idHasilGap; }

    public Kandidat getKandidat() { return kandidat; }
    public void setKandidat(Kandidat kandidat) { this.kandidat = kandidat; }

    public Lowongan getLowongan() { return lowongan; }
    public void setLowongan(Lowongan lowongan) { this.lowongan = lowongan; }

    public Kriteria getKriteria() { return kriteria; }
    public void setKriteria(Kriteria kriteria) { this.kriteria = kriteria; }

    public int getNilaiGap() { return nilaiGap; }
    public void setNilaiGap(int nilaiGap) { this.nilaiGap = nilaiGap; }

    public double getBobotNilai() { return bobotNilai; }
    public void setBobotNilai(double bobotNilai) { this.bobotNilai = bobotNilai; }
}
