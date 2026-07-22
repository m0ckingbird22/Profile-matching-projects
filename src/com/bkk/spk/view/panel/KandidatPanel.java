package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KandidatDAO;
import com.bkk.spk.dao.NilaiKandidatDAO;
import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiKandidat;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

/**
 * Panel master data Kandidat: tabel + tombol Tambah / Edit / Hapus + filter pencarian.
 *
 * Data dimuat tiap kali panel tampil (lihat refreshData()). ID kandidat disimpan
 * di kolom 0 tapi di-hidden supaya UI bersih — tetap bisa diambil untuk operasi Edit/Hapus.
 */
public class KandidatPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Kode", "Nama", "Th. Lulus", "Tgl Lahir", "Alamat"};

    private final KandidatDAO dao = new KandidatDAO();
    private final NilaiKandidatDAO nilaiDAO = new NilaiKandidatDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtCari = new JTextField(16);

    public KandidatPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : Object.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoCreateRowSorter(false);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Hide kolom ID (kolom 0) — width 0
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(0xFC, 0xE4, 0xEC));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xF8, 0xBB, 0xD0)));
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTitle = new JLabel("Data Kandidat");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCari.putClientProperty("JTextField.placeholderText", "Cari nama / Kode...");
        txtCari.addActionListener((ActionEvent e) -> applyFilter());
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        right.add(txtCari);

        right.add(button("Tambah", e -> onTambah()));
        right.add(button("Edit", e -> onEdit()));
        right.add(button("Hapus", e -> onHapus()));
        right.add(button("Refresh", e -> refreshData()));

        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private JButton button(String text, java.awt.event.ActionListener action) {
        return ButtonStyle.primary(text, action);
    }

    /** Reload tabel dari DB + re-apply filter saat ini. */
    public void refreshData() {
        int selectedId = getSelectedId();
        tableModel.setRowCount(0);
        List<Kandidat> daftar = dao.getAll();
        for (Kandidat k : daftar) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            tableModel.addRow(new Object[]{
                k.getIdKandidat(),
                k.getNisn(),
                k.getNama(),
                k.getTahunLulus(),
                k.getTanggalLahir() != null ? k.getTanggalLahir().format(fmt) : "",
                k.getAlamat() != null ? k.getAlamat() : ""
            });
        }
        if (selectedId > 0) {
            selectById(selectedId);
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = txtCari.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        // Cari di kolom Kode(1) dan Nama(2)
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
    }

    private void onTambah() {
        KandidatFormDialog.HasilForm hasil = KandidatFormDialog.tampilkan(this, "Tambah Kandidat", null);
        if (hasil == null) return;
        Kandidat k = hasil.getKandidat();
        if (!dao.insert(k)) {
            JOptionPane.showMessageDialog(this, "Gagal menambah kandidat. Cek log / konsol.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Insert kandidat sukses → id_kandidat baru udah diset oleh DAO. Sekarang masukin
        // nilai per kriteria (kalau user ngisi). Pakai batch biara atomik.
        if (!hasil.getNilaiByKriteria().isEmpty() && k.getIdKandidat() > 0) {
            List<NilaiKandidat> daftarNilai = new java.util.ArrayList<>();
            for (Map.Entry<Integer, Integer> e : hasil.getNilaiByKriteria().entrySet()) {
                Kriteria kr = new Kriteria();
                kr.setIdKriteria(e.getKey());
                NilaiKandidat nk = new NilaiKandidat();
                nk.setKandidat(k);
                nk.setKriteria(kr);
                nk.setNilaiKandidat(e.getValue());
                daftarNilai.add(nk);
            }
            if (!nilaiDAO.insertBatch(daftarNilai)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Kandidat tersimpan, tapi sebagian/tidak ada nilai yang tersimpan.\n" +
                    "Bisa set manual lewat menu Input Nilai.",
                    "Peringatan",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        }
        refreshData();
    }

    private void onEdit() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk diedit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Kandidat existing = dao.getById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Data kandidat tidak ditemukan (mungkin sudah dihapus).", "Error", JOptionPane.ERROR_MESSAGE);
            refreshData();
            return;
        }
        KandidatFormDialog.HasilForm hasil = KandidatFormDialog.tampilkan(this, "Edit Kandidat", existing);
        if (hasil == null) return;
        boolean ok = dao.update(hasil.getKandidat());
        if (ok) {
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal update kandidat.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onHapus() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String nama = (String) tableModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Hapus kandidat \"" + nama + "\"?\nData nilai & hasil terkait juga bisa ikut terhapus.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        boolean ok = dao.delete(id);
        if (ok) {
            refreshData();
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Gagal hapus kandidat. Kemungkinan masih ada data nilai/hasil yang mereferensikan kandidat ini.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /** ID kandidat dari baris yang dipilih (lihat baris di model lewat convertRowIndexToModel). */
    private int getSelectedId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return 0;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = tableModel.getValueAt(modelRow, 0);
        return (val instanceof Number) ? ((Number) val).intValue() : 0;
    }

    private void selectById(int id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 0);
            int rowId = (val instanceof Number) ? ((Number) val).intValue() : 0;
            if (rowId == id) {
                int viewRow = table.convertRowIndexToView(i);
                table.setRowSelectionInterval(viewRow, viewRow);
                break;
            }
        }
    }
}
