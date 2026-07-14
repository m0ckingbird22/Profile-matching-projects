package com.bkk.spk.model;

public class Kriteria {

    // Konstanta jenis faktor -> hindari typo string "CF"/"SF" tersebar di banyak file
    public static final String CORE_FACTOR = "CF";
    public static final String SECONDARY_FACTOR = "SF";

    private int idKriteria;
    private String kodeKriteria;   // C1, C2, ..., C8
    private String namaKriteria;
    private String jenisFaktor;    // CORE_FACTOR atau SECONDARY_FACTOR

    public Kriteria() {}

    public Kriteria(int idKriteria, String kodeKriteria, String namaKriteria, String jenisFaktor) {
        this.idKriteria = idKriteria;
        this.kodeKriteria = kodeKriteria;
        this.namaKriteria = namaKriteria;
        this.jenisFaktor = jenisFaktor;
    }

    public int getIdKriteria() { return idKriteria; }
    public void setIdKriteria(int idKriteria) { this.idKriteria = idKriteria; }

    public String getKodeKriteria() { return kodeKriteria; }
    public void setKodeKriteria(String kodeKriteria) { this.kodeKriteria = kodeKriteria; }

    public String getNamaKriteria() { return namaKriteria; }
    public void setNamaKriteria(String namaKriteria) { this.namaKriteria = namaKriteria; }

    public String getJenisFaktor() { return jenisFaktor; }
    public void setJenisFaktor(String jenisFaktor) { this.jenisFaktor = jenisFaktor; }

    public boolean isCoreFactor() { return CORE_FACTOR.equals(jenisFaktor); }
    public boolean isSecondaryFactor() { return SECONDARY_FACTOR.equals(jenisFaktor); }

    @Override
    public String toString() {
        return kodeKriteria + " - " + namaKriteria;
    }
}
