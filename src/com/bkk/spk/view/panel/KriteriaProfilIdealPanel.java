package com.bkk.spk.view.panel;

import com.bkk.spk.view.Refreshable;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Wrapper 1 halaman untuk Data Kriteria & Profil Ideal.
 *
 * Struktur: 2 tab dalam 1 halaman, menu sidebar cukup 1 ("Data Kriteria").
 */
public class KriteriaProfilIdealPanel extends JPanel implements Refreshable {

    private static final Color BG = new Color(0xFD, 0xEA, 0xF1);

    public KriteriaProfilIdealPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(BG);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabs.addTab("Data Kriteria", new KriteriaPanel());
        tabs.addTab("Profil Ideal", new ProfilIdealPanel());

        add(tabs, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        for (int i = 0; i < getComponentCount(); i++) {
            forwardRefresh(getComponent(i));
        }
    }

    private void forwardRefresh(Component c) {
        if (c instanceof Refreshable) {
            ((Refreshable) c).refreshData();
        } else if (c instanceof java.awt.Container) {
            for (Component child : ((java.awt.Container) c).getComponents()) {
                forwardRefresh(child);
            }
        }
    }
}
