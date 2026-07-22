package com.bkk.spk.model;

public class NilaiKandidat {
    private int idNilai;
    private Kandidat kandidat;
    private Kriteria kriteria;
    private double nilaiKandidat;

    public NilaiKandidat() {}

    public NilaiKandidat(int idNilai, Kandidat kandidat, Kriteria kriteria, double nilaiKandidat) {
        this.idNilai = idNilai;
        this.kandidat = kandidat;
        this.kriteria = kriteria;
        this.nilaiKandidat = nilaiKandidat;
    }

    public NilaiKandidat(Kandidat kandidat, Kriteria kriteria, double nilaiKandidat) {
        this.kandidat = kandidat;
        this.kriteria = kriteria;
        this.nilaiKandidat = nilaiKandidat;
    }

    public int getIdNilai() { return idNilai; }
    public void setIdNilai(int idNilai) { this.idNilai = idNilai; }

    public Kandidat getKandidat() { return kandidat; }
    public void setKandidat(Kandidat kandidat) { this.kandidat = kandidat; }

    public Kriteria getKriteria() { return kriteria; }
    public void setKriteria(Kriteria kriteria) { this.kriteria = kriteria; }

    public double getNilaiKandidat() { return nilaiKandidat; }
    public void setNilaiKandidat(double nilaiKandidat) { this.nilaiKandidat = nilaiKandidat; }
}
