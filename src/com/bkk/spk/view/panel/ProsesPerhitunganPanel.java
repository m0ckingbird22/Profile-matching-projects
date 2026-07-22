package com.bkk.spk.view.panel;

import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.model.Admin;
import com.bkk.spk.model.HasilAkhir;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.service.ProfileMatchingService;
import com.bkk.spk.util.Session;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * Panel untuk menjalankan proses Profile Matching per lowongan.
 *
 * Setelah klik "Proses Perhitungan":
 *   1. Panggil ProfileMatchingService.prosesSeleksi(idLowongan, admin).
 *      Service hapus hasil lama -> hitung gap -> hitung NCF/NSF/total -> ranking -> simpan.
 *   2. Tampilkan hasil di tabel (Rank, NISN, Nama, NCF, NSF, Total, Status).
 *   3. Status: ranking <= kuota -> LULUS, selebihnya -> Belum Lulus.
 */
public class ProsesPerhitunganPanel extends JPanel {

    private static final String[] COLUMNS = {
        "Rank", "NISN", "Nama", "NCF", "NSF", "Total", "Status"
    };

    private final LowonganDAO lowonganDAO = new LowonganDAO();
    private final ProfileMatchingService service = new ProfileMatchingService();

    private final JComboBox<Lowongan> cbLowongan = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextArea txtLog = new JTextArea(6, 60);

    public ProsesPerhitunganPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(0xFC, 0xE4, 0xEC));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scrollTable = new JScrollPane(table);
        scrollTable.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xF8, 0xBB, 0xD0)));
        add(scrollTable, BorderLayout.CENTER);

        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBackground(new Color(0xFA, 0xFA, 0xFA));
        txtLog.setText("Log proses perhitungan akan muncul di sini.\n");
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setPreferredSize(new java.awt.Dimension(60, 140));
        scrollLog.setBorder(BorderFactory.createTitledBorder("Log"));
        add(scrollLog, BorderLayout.SOUTH);

        muatLowonganBuka();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTitle = new JLabel("Proses Perhitungan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("Lowongan (BUKA):"));
        cbLowongan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        right.add(cbLowongan);

        JButton btnProses = new JButton("Proses Perhitungan");
        ButtonStyle.primary(btnProses);
        btnProses.addActionListener(e -> onProses());
        right.add(btnProses);

        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private void muatLowonganBuka() {
        cbLowongan.removeAllItems();
        List<Lowongan> daftar = lowonganDAO.getAllBuka();
        for (Lowongan l : daftar) cbLowongan.addItem(l);
        cbLowongan.setSelectedIndex(-1);
        if (!daftar.isEmpty()) cbLowongan.setSelectedIndex(0);
    }

    private void onProses() {
        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih lowongan dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Admin admin = Session.getCurrentAdmin();
        if (admin == null) {
            JOptionPane.showMessageDialog(this, "Sesi admin hilang. Silakan login ulang.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Jalankan proses perhitungan untuk lowongan \"" + selected.getPosisi() + "\"?\n"
                + "Hasil proses lama untuk lowongan ini akan ditimpa.",
            "Konfirmasi Proses",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        appendLog("Memulai proses perhitungan untuk: " + selected);
        appendLog("Kuota: " + selected.getKuota() + " | Admin: " + admin.getNama());

        setEnabledAll(false);

        new Thread(() -> {
            try {
                long t0 = System.currentTimeMillis();
                List<HasilAkhir> hasil = service.prosesSeleksi(selected.getIdLowongan(), admin);
                long ms = System.currentTimeMillis() - t0;

                SwingUtilities.invokeLater(() -> {
                    appendLog("Selesai dalam " + ms + " ms. Total kandidat diproses: " + hasil.size());
                    tampilkanHasil(hasil, selected.getKuota());
                    setEnabledAll(true);
                    JOptionPane.showMessageDialog(
                        this,
                        "Proses perhitungan selesai.\nKandidat diproses: " + hasil.size() + "\nKuota: " + selected.getKuota(),
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendLog("ERROR: " + ex.getMessage());
                    ex.printStackTrace();
                    setEnabledAll(true);
                    JOptionPane.showMessageDialog(
                        this,
                        "Gagal proses perhitungan:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }, "proses-perhitungan").start();
    }

    private void tampilkanHasil(List<HasilAkhir> hasil, int kuota) {
        tableModel.setRowCount(0);
        for (HasilAkhir h : hasil) {
            String status = (h.getRanking() <= kuota) ? "LULUS" : "Belum Lulus";
            tableModel.addRow(new Object[]{
                h.getRanking(),
                h.getKandidat().getNisn(),
                h.getKandidat().getNama(),
                String.format("%.3f", h.getNcf()),
                String.format("%.3f", h.getNsf()),
                String.format("%.3f", h.getNilaiTotal()),
                status
            });
        }
    }

    private void setEnabledAll(boolean enabled) {
        cbLowongan.setEnabled(enabled);
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JPanel) {
                for (java.awt.Component child : ((JPanel) c).getComponents()) {
                    if (child instanceof JButton) child.setEnabled(enabled);
                }
            }
        }
    }

    private void appendLog(String line) {
        txtLog.append(line + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }
}
