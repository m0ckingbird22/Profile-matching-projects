package com.bkk.spk.util;

import com.bkk.spk.model.Admin;

/**
 * State global buat simpan admin yang lagi login.
 * Diisi saat LoginDialog sukses, dibersihkan saat logout.
 * Dipakai terutama oleh ProsesSeleksiPanel untuk audit id_admin di tb_hasil_akhir.
 */
public final class Session {

    private static Admin currentAdmin;

    private Session() {}

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }

    public static void setCurrentAdmin(Admin admin) {
        currentAdmin = admin;
    }

    public static boolean isLoggedIn() {
        return currentAdmin != null;
    }

    public static void clear() {
        currentAdmin = null;
    }
}
