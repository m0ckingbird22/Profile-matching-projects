package com.bkk.spk.view.panel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

/**
 * Wrapper 1 halaman untuk Data Perusahaan & Data Lowongan.
 *
 * Dipilih struktur tab (pilihan user saat brainstorm): 2 tab dalam 1 halaman,
 * menu sidebar cukup 1 ("Data Perusahaan & Lowongan").
 */
public class PerusahaanLowonganPanel extends JPanel {

    private static final Color BG = new Color(0xFD, 0xEA, 0xF1);

    public PerusahaanLowonganPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(BG);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabs.addTab("Data Perusahaan", new PerusahaanPanel());
        tabs.addTab("Data Lowongan", new LowonganPanel());

        add(tabs, BorderLayout.CENTER);
    }
}
