package com.bkk.spk.view.panel;

import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

public class LowonganPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Perusahaan", "Posisi", "Kuota", "Status", "Deskripsi"};

    private final LowonganDAO dao = new LowonganDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final javax.swing.JTextField txtCari = new javax.swing.JTextField(16);

    public LowonganPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

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

        JLabel lblTitle = new JLabel("Data Lowongan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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

    public void refreshData() {
        int selectedId = getSelectedId();
        tableModel.setRowCount(0);
        List<Lowongan> daftar = dao.getAll();
        for (Lowongan l : daftar) {
            String perusahaanNama = (l.getPerusahaan() != null) ? l.getPerusahaan().getNamaPerusahaan() : "-";
            tableModel.addRow(new Object[]{
                l.getIdLowongan(),
                perusahaanNama,
                l.getPosisi(),
                l.getKuota(),
                l.getStatus(),
                l.getDeskripsi() == null ? "" : l.getDeskripsi()
            });
        }
        if (selectedId > 0) selectById(selectedId);
        applyFilter();
    }

    private void applyFilter() {
        String text = txtCari.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text), 1, 2, 5));
    }

    private void onTambah() {
        Lowongan hasil = LowonganFormDialog.tampilkan(this, "Tambah Lowongan", null);
        if (hasil == null) return;
        if (dao.insert(hasil)) refreshData();
        else JOptionPane.showMessageDialog(this, "Gagal menambah lowongan.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void onEdit() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk diedit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Lowongan existing = dao.getById(id);
        if (existing == null) { refreshData(); return; }
        Lowongan hasil = LowonganFormDialog.tampilkan(this, "Edit Lowongan", existing);
        if (hasil == null) return;
        if (dao.update(hasil)) refreshData();
        else JOptionPane.showMessageDialog(this, "Gagal update lowongan.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void onHapus() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String posisi = (String) tableModel.getValueAt(row, 2);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Hapus lowongan \"" + posisi + "\"?\nLowongan yang sudah diproses seleksi tidak bisa dihapus.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        if (dao.delete(id)) refreshData();
        else JOptionPane.showMessageDialog(
            this,
            "Gagal hapus lowongan. Kemungkinan masih ada data profil ideal / hasil yang mereferensikan lowongan ini.",
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

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
