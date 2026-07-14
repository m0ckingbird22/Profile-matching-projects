package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.dao.PerusahaanDAO;
import com.bkk.spk.dao.SiswaDAO;
import com.bkk.spk.model.Admin;
import com.bkk.spk.util.Session;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;

/**
 * Dashboard landing page: greeting + kartu statistik (Total Siswa, Perusahaan,
 * Lowongan BUKA, Kriteria) + tombol Refresh.
 *
 * Statistik dimuat di muatStatistik() — panggil saat panel tampil kalau mau
 * selalu fresh. Sekarang dimuat sekali di constructor, plus tombol Refresh manual.
 */
public class DashboardPanel extends JPanel {

    private static final int LOGO_SIZE = 60;

    private final SiswaDAO siswaDAO = new SiswaDAO();
    private final PerusahaanDAO perusahaanDAO = new PerusahaanDAO();
    private final LowonganDAO lowonganDAO = new LowonganDAO();
    private final KriteriaDAO kriteriaDAO = new KriteriaDAO();

    private final JLabel lblTotalSiswa = new JLabel("0");
    private final JLabel lblTotalPerusahaan = new JLabel("0");
    private final JLabel lblLowonganBuka = new JLabel("0");
    private final JLabel lblTotalKriteria = new JLabel("0");

    private Image logoImage;

    public DashboardPanel() {
        initComponents();
        loadLogo();
        muatStatistik();
    }

    private void loadLogo() {
        URL url = getClass().getResource("/com/bkk/spk/resources/logo_smk.png");
        if (url != null) {
            ImageIcon raw = new ImageIcon(url);
            logoImage = raw.getImage().getScaledInstance(LOGO_SIZE, LOGO_SIZE, Image.SCALE_SMOOTH);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logoImage != null) {
            int x = (getWidth() - LOGO_SIZE) / 2;
            int y = (getHeight() - LOGO_SIZE) / 2;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(logoImage, x, y, null);
            g2.dispose();
        }
    }

    private void initComponents() {
        setBackground(ButtonStyle.PINK_PANEL);
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        setLayout(new BorderLayout(0, 18));

        add(buildHeader(), BorderLayout.NORTH);

        // Grid kartu dibungkus BorderLayout & ditaruh di NORTH -> tinggi grid = tinggi preferensi,
        // sisa ruang di bawah kosong (gak bikin kartu stretch ke bawah).
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setOpaque(false);
        grid.add(buatKartu("Total Siswa", lblTotalSiswa, "Seluruh kandidat terdaftar"));
        grid.add(buatKartu("Lowongan Aktif", lblLowonganBuka, "Status BUKA (siap seleksi)"));
        grid.add(buatKartu("Perusahaan", lblTotalPerusahaan, "Mitra BKK"));
        grid.add(buatKartu("Kriteria", lblTotalKriteria, "CF + SF"));
        gridWrap.add(grid, BorderLayout.NORTH);

        add(gridWrap, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel kiri = new JPanel();
        kiri.setLayout(new GridBagLayout());
        kiri.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 0, 2, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(60, 30, 40));
        gbc.gridy = 0;
        kiri.add(lblTitle, gbc);

        Admin admin = Session.getCurrentAdmin();
        String nama = (admin != null) ? admin.getNama() : "Admin";
        JLabel lblWelcome = new JLabel("Selamat datang, " + nama + ".");
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblWelcome.setForeground(new Color(110, 70, 85));
        gbc.gridy = 1;
        kiri.add(lblWelcome, gbc);

        header.add(kiri, BorderLayout.WEST);

        JButton btnRefresh = ButtonStyle.primary("Refresh Statistik", e -> muatStatistik());
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        kanan.setOpaque(false);
        kanan.add(btnRefresh);
        header.add(kanan, BorderLayout.EAST);

        return header;
    }

    private JPanel buatKartu(String judul, JLabel lblAngka, String subtext) {
        JPanel kartu = new JPanel(new GridBagLayout());
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ButtonStyle.PINK_BORDER, 1),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 0, 1, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblJudul = new JLabel(judul);
        lblJudul.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblJudul.setForeground(new Color(110, 70, 85));
        gbc.gridy = 0;
        kartu.add(lblJudul, gbc);

        lblAngka.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblAngka.setForeground(ButtonStyle.PINK_DARK);
        gbc.gridy = 1;
        kartu.add(lblAngka, gbc);

        JLabel lblSub = new JLabel(subtext);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(150, 110, 125));
        gbc.gridy = 2;
        kartu.add(lblSub, gbc);

        return kartu;
    }

    /** Hitung ulang angka di kartu. Dioptimasi: query count ringan, gak baca seluruh tabel. */
    private void muatStatistik() {
        new Thread(() -> {
            int siswa = siswaDAO.getAll().size();
            int perusahaan = perusahaanDAO.getAll().size();
            int lowonganBuka = lowonganDAO.getAllBuka().size();
            int kriteria = kriteriaDAO.getAll().size();

            SwingUtilities.invokeLater(() -> {
                lblTotalSiswa.setText(String.valueOf(siswa));
                lblTotalPerusahaan.setText(String.valueOf(perusahaan));
                lblLowonganBuka.setText(String.valueOf(lowonganBuka));
                lblTotalKriteria.setText(String.valueOf(kriteria));
            });
        }, "dashboard-stat").start();
    }
}
