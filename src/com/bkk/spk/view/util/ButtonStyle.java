package com.bkk.spk.view.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Styling tombol pink primary supaya konsisten di semua panel.
 *
 * Pakai: JButton btn = new JButton("Tambah"); ButtonStyle.primary(btn);
 * Atau: JButton btn = ButtonStyle.primary(new JButton("Tambah"));
 */
public final class ButtonStyle {

    public static final Color PINK_PRIMARY = new Color(244, 143, 177);   // #f48fb1
    public static final Color PINK_DARK    = new Color(236, 64, 122);    // #ec407a (hover)
    public static final Color PINK_TINT    = new Color(252, 228, 236);   // #fce4ec
    public static final Color PINK_BORDER  = new Color(248, 187, 208);   // #f8bbd0
    public static final Color PINK_PANEL   = new Color(253, 234, 241);   // #fdeaf1

    private ButtonStyle() {}

    /** Apply pink primary styling ke tombol yang sudah dibuat. Return tombol yg sama buat chaining. */
    public static JButton primary(JButton btn) {
        btn.setBackground(PINK_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        // Beberapa LAF butuh opaque=true biar setBackground kelihatan
        btn.setOpaque(true);
        btn.setBorderPainted(false);

        // Hover feedback → warna lebih gelat
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(PINK_DARK); }
            @Override public void mouseExited(MouseEvent e) { btn.setBackground(PINK_PRIMARY); }
        });
        return btn;
    }

    /** Convenience: buat tombol primary baru dengan text + action. */
    public static JButton primary(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        primary(btn);
        btn.addActionListener(action);
        return btn;
    }

    /** Styling untuk tombol sekunder (outline pink) — buat tombol Batal/Keluar. */
    public static JButton secondary(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(PINK_DARK);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(PINK_PRIMARY, 1));
        // padding supaya height mirip primary
        btn.setMargin(new java.awt.Insets(8, 14, 8, 14));
        return btn;
    }
}
