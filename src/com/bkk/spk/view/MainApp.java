package com.bkk.spk.view;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JOptionPane;
import java.io.PrintWriter;
import java.io.StringWriter;
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

        // Pasang handler global untuk exception yang lepas dari EDT (Swing thread).
        // Tanpa ini, kalau MainFrame constructor lempar exception, window gak akan
        // muncul dan app diam-diam nge-hang / ketutup tanpa pesan.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            System.err.println("Uncaught exception di thread " + thread.getName() + ":");
            System.err.println(sw.toString());
            JOptionPane.showMessageDialog(null, sw.toString(),
                "Error tak terduga", JOptionPane.ERROR_MESSAGE);
        });

        SwingUtilities.invokeLater(() -> {
            try {
                boolean sukses = LoginDialog.tampilkanDanTunggu();
                if (!sukses) {
                    System.exit(0);
                }
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Throwable t) {
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                System.err.println("Gagal memulai aplikasi:");
                System.err.println(sw.toString());
                JOptionPane.showMessageDialog(null, sw.toString(),
                    "Gagal Memulai Aplikasi", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
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

