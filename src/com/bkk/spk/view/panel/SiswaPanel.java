package com.bkk.spk.view.panel;

import com.bkk.spk.dao.NilaiSiswaDAO;
import com.bkk.spk.dao.SiswaDAO;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiSiswa;
import com.bkk.spk.model.Siswa;
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
 * Panel master data Siswa: tabel + tombol Tambah / Edit / Hapus + filter pencarian.
 *
 * Data dimuat tiap kali panel tampil (lihat refreshData()). ID siswa disimpan
 * di kolom 0 tapi di-hidden supaya UI bersih — tetap bisa diambil untuk operasi Edit/Hapus.
 */
public class SiswaPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "NISN", "Nama", "Jurusan", "Kelas", "Th. Lulus", "Tgl Lahir", "Alamat"};

    private final SiswaDAO dao = new SiswaDAO();
    private final NilaiSiswaDAO nilaiDAO = new NilaiSiswaDAO();

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField txtCari = new JTextField(16);

    public SiswaPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 5 ? Integer.class : Object.class;
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

        // Kiri: judul
        JLabel lblTitle = new JLabel("Data Siswa");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        // Tengah-kanan: search + tombol CRUD
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCari.putClientProperty("JTextField.placeholderText", "Cari nama / NISN...");
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
        List<Siswa> daftar = dao.getAll();
        for (Siswa s : daftar) {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
            tableModel.addRow(new Object[]{
                s.getIdSiswa(),
                s.getNisn(),
                s.getNama(),
                s.getJurusan(),
                s.getKelas(),
                s.getTahunLulus(),
                s.getTanggalLahir() != null ? s.getTanggalLahir().format(fmt) : "",
                s.getAlamat() != null ? s.getAlamat() : ""
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
        // Cari di kolom NISN(1) dan Nama(2)
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2));
    }

    private void onTambah() {
        SiswaFormDialog.HasilForm hasil = SiswaFormDialog.tampilkan(this, "Tambah Siswa", null);
        if (hasil == null) return;
        Siswa s = hasil.getSiswa();
        if (!dao.insert(s)) {
            JOptionPane.showMessageDialog(this, "Gagal menambah siswa. Cek log / konsol.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Insert siswa sukses → id_siswa baru udah diset oleh DAO. Sekarang masukin
        // nilai per kriteria (kalau user ngisi). Pakai batch biara atomik.
        if (!hasil.getNilaiByKriteria().isEmpty() && s.getIdSiswa() > 0) {
            List<NilaiSiswa> daftarNilai = new java.util.ArrayList<>();
            for (Map.Entry<Integer, Integer> e : hasil.getNilaiByKriteria().entrySet()) {
                Kriteria k = new Kriteria();
                k.setIdKriteria(e.getKey());
                NilaiSiswa ns = new NilaiSiswa();
                ns.setSiswa(s);
                ns.setKriteria(k);
                ns.setNilaiKandidat(e.getValue());
                daftarNilai.add(ns);
            }
            if (!nilaiDAO.insertBatch(daftarNilai)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Siswa tersimpan, tapi sebagian/tidak ada nilai yang tersimpan.\n" +
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
        Siswa existing = dao.getById(id);
        if (existing == null) {
            JOptionPane.showMessageDialog(this, "Data siswa tidak ditemukan (mungkin sudah dihapus).", "Error", JOptionPane.ERROR_MESSAGE);
            refreshData();
            return;
        }
        SiswaFormDialog.HasilForm hasil = SiswaFormDialog.tampilkan(this, "Edit Siswa", existing);
        if (hasil == null) return;
        boolean ok = dao.update(hasil.getSiswa());
        if (ok) {
            refreshData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal update siswa.", "Error", JOptionPane.ERROR_MESSAGE);
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
            "Hapus siswa \"" + nama + "\"?\nData nilai & hasil terkait juga bisa ikut terhapus.",
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
                "Gagal hapus siswa. Kemungkinan masih ada data nilai/hasil yang mereferensikan siswa ini.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /** ID siswa dari baris yang dipilih (lihat baris di model lewat convertRowIndexToModel). */
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
