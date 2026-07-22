package com.bkk.spk.view;

import com.bkk.spk.model.Admin;
import com.bkk.spk.util.Session;
import com.bkk.spk.view.panel.DashboardPanel;
import com.bkk.spk.view.panel.KandidatPanel;
import com.bkk.spk.view.panel.PerusahaanLowonganPanel;
import com.bkk.spk.view.panel.KriteriaProfilIdealPanel;
import com.bkk.spk.view.panel.InputNilaiPanel;
import com.bkk.spk.view.panel.ProsesPerhitunganPanel;
import com.bkk.spk.view.panel.LaporanHasilPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.awt.event.ActionEvent;

/**
 * Window utama aplikasi. Berisi:
 *   - Sidebar kiri: tombol menu (Dashboard, master data, transaksi, laporan, logout)
 *   - Header atas: judul halaman aktif + nama admin yang login
 *   - Content: JPanel dengan CardLayout -> ganti panel sesuai menu yang dipilih
 *
 * Implement Navigator supaya sub-panel bisa pindah halaman tanpa hard-reference MainFrame.
 */
public class MainFrame extends javax.swing.JFrame implements Navigator {

    private static final Color SIDEBAR_BG = new Color(244, 143, 177);   // #f48fb1
    private static final Color SIDEBAR_FG = Color.WHITE;
    private static final Color SIDEBAR_HOVER = new Color(236, 64, 122); // #ec407a
    private static final Color HEADER_BG = Color.WHITE;

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JLabel lblHalaman;
    private final JLabel lblAdmin;

    public MainFrame() {
        super("SPK Profile Matching — Bursa Kerja Khusus");

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        lblHalaman = new JLabel("Dashboard");
        lblHalaman.setFont(new Font("Segoe UI", Font.BOLD, 18));

        lblAdmin = new JLabel();
        lblAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAdmin.setForeground(new Color(100, 100, 100));

        initComponents();
        refreshAdminLabel();
        navigateTo(DASHBOARD);
    }

    private void initComponents() {
        // DO_NOTHING + WindowListener biar bisa konfirmasi dulu sebelum keluar.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new Dimension(1100, 700));
        setMinimumSize(new Dimension(900, 600));
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onTutupAplikasi();
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private void onTutupAplikasi() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin keluar dari aplikasi?",
            "Konfirmasi Keluar",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private Component buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel logo = new JLabel("SMK Widya Nusantara");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        sidebar.add(logo);

        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));
        sidebar.add(menuSeparator());

        // Menu utama
        sidebar.add(menuButton("Dashboard", DASHBOARD));
        sidebar.add(menuButton("Data Kandidat", KANDIDAT));
        sidebar.add(menuButton("Data Perusahaan", PERUSAHAAN_LOWONGAN));
        sidebar.add(menuButton("Data Kriteria", KRITERIA));
        sidebar.add(menuButton("Data Nilai Kandidat", INPUT_NILAI));
        sidebar.add(menuButton("Proses Perhitungan", PROSES_PERHITUNGAN));
        sidebar.add(menuButton("Laporan Hasil", LAPORAN_HASIL));

        // Dorong tombol logout ke bawah
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(menuSeparator());
        sidebar.add(menuButton("Logout", "LOGOUT"));

        return sidebar;
    }

    private Component buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        header.add(lblHalaman, BorderLayout.WEST);
        header.add(lblAdmin, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(230, 230, 230));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(HEADER_BG);
        wrapper.add(header, BorderLayout.NORTH);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    private Component buildContent() {
        // Daftarin SEMUA card di awal biar show() gak error.
        // Card yang belum ada panel-nya pakai placeholder dulu.
        cardPanel.add(new DashboardPanel(), DASHBOARD);
        cardPanel.add(new KandidatPanel(), KANDIDAT);
        cardPanel.add(new PerusahaanLowonganPanel(), PERUSAHAAN_LOWONGAN);
        cardPanel.add(new KriteriaProfilIdealPanel(), KRITERIA);
        cardPanel.add(new InputNilaiPanel(), INPUT_NILAI);
        cardPanel.add(new ProsesPerhitunganPanel(), PROSES_PERHITUNGAN);
        cardPanel.add(new LaporanHasilPanel(), LAPORAN_HASIL);

        return cardPanel;
    }

    // --- Navigator implementation ---

    @Override
    public void show(String cardName) {
        if ("LOGOUT".equals(cardName)) {
            logout();
            return;
        }
        navigateTo(cardName);
    }

    @Override
    public void logout() {
        Session.clear();
        dispose();
        // Buka ulang dialog login tanpa keluar aplikasi
        SwingUtilities.invokeLater(() -> {
            boolean sukses = LoginDialog.tampilkanDanTunggu();
            if (!sukses) {
                System.exit(0);
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    private void navigateTo(String cardName) {
        cardLayout.show(cardPanel, cardName);
        lblHalaman.setText(prettyTitle(cardName));
    }

    // --- Builders ---

    private JButton menuButton(String label, String cardKey) {
        JButton btn = new JButton(label);
        btn.setBackground(SIDEBAR_BG);
        btn.setForeground(SIDEBAR_FG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener((ActionEvent e) -> show(cardKey));
        // Hover sederhana -> feedback visual
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_HOVER); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(SIDEBAR_BG); }
        });
        return btn;
    }

    private Component menuSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(236, 64, 122));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private JPanel placeholder(String pesan) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel lbl = new JLabel(pesan, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lbl.setForeground(new Color(140, 140, 140));
        panel.add(lbl, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAdminLabel() {
        Admin admin = Session.getCurrentAdmin();
        if (admin != null) {
            lblAdmin.setText("Login sebagai: " + admin.getNama() + "  •  @" + admin.getUsername());
        } else {
            lblAdmin.setText("");
        }
    }

    private String prettyTitle(String cardName) {
        switch (cardName) {
            case DASHBOARD: return "Dashboard";
            case KANDIDAT: return "Data Kandidat";
            case PERUSAHAAN_LOWONGAN: return "Data Perusahaan";
            case KRITERIA: return "Data Kriteria";
            case INPUT_NILAI: return "Data Nilai Kandidat";
            case PROSES_PERHITUNGAN: return "Proses Perhitungan";
            case LAPORAN_HASIL: return "Laporan Hasil";
            default: return "SPK Profile Matching";
        }
    }
}
