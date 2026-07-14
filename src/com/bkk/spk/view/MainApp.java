package com.bkk.spk.view;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.lang.reflect.Method;

/**
 * Entry point aplikasi.
 *
 * Alur:
 *   1. Setup Look & Feel (FlatLaf kalau JAR-nya ada, fallback Nimbus kalau gak ada).
 *   2. Tampilkan LoginDialog (modal, blocking).
 *   3. Kalau login sukses -> buka MainFrame. Kalau batal -> app exit.
 *
 * Kenapa reflection buat FlatLaf? Supaya project tetep jalan walau
 * flatlaf.jar belum di-add ke Libraries. Begitu JAR ditambahkan,
 * FlatLaf otomatis aktif tanpa perlu ubah kode.
 */
public class MainApp {

    public static void main(String[] args) {
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            boolean sukses = LoginDialog.tampilkanDanTunggu();
            if (!sukses) {
                System.exit(0);
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        // Coba FlatLaf dulu (IntelliJ theme) via reflection -> gak import langsung
        try {
            Class<?> lafClass = Class.forName("com.formdev.flatlaf.FlatIntelliJLaf");
            Method setup = lafClass.getMethod("setup");
            setup.invoke(null);
            return;
        } catch (ClassNotFoundException ignored) {
            // flatlaf.jar belum di-add -> lanjut ke fallback
        } catch (Exception e) {
            System.err.println("FlatLaf gagal load, pakai fallback: " + e.getMessage());
        }

        // Fallback: Nimbus (bawaan JDK, kelihatan lebih modern dari Metal default)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus gagal load: " + e.getMessage());
        }
        // Kalau Nimbus juga gak ada, biarkan default LAF sistem
    }
}
