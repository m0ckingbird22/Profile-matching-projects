  package com.bkk.spk.model;

public class NilaiSiswa {
    private int idNilai;
    private Siswa siswa;
    private Kriteria kriteria;
    private double nilaiKandidat;

    public NilaiSiswa() {}

    public NilaiSiswa(int idNilai, Siswa siswa, Kriteria kriteria, double nilaiKandidat) {
        this.idNilai = idNilai;
        this.siswa = siswa;
        this.kriteria = kriteria;
        this.nilaiKandidat = nilaiKandidat;
    }

    public NilaiSiswa(Siswa siswa, Kriteria kriteria, double nilaiKandidat) {
        this.siswa = siswa;
        this.kriteria = kriteria;
        this.nilaiKandidat = nilaiKandidat;
    }

    public int getIdNilai() { return idNilai; }
    public void setIdNilai(int idNilai) { this.idNilai = idNilai; }

    public Siswa getSiswa() { return siswa; }
    public void setSiswa(Siswa siswa) { this.siswa = siswa; }

    public Kriteria getKriteria() { return kriteria; }
    public void setKriteria(Kriteria kriteria) { this.kriteria = kriteria; }

    public double getNilaiKandidat() { return nilaiKandidat; }
    public void setNilaiKandidat(double nilaiKandidat) { this.nilaiKandidat = nilaiKandidat; }
}
