package com.bkk.spk.model;

public class Admin {
    private int idAdmin;
    private String username;
    private String password; // simpan hash BCrypt, bukan plaintext
    private String nama;

    public Admin() {}

    public Admin(int idAdmin, String username, String password, String nama) {
        this.idAdmin = idAdmin;
        this.username = username;
        this.password = password;
        this.nama = nama;
    }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    @Override
    public String toString() {
        return nama; // dipakai kalau object ini di-render langsung di JComboBox/JList
    }
}
