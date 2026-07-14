package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.model.Kriteria;
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
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;

public class KriteriaPanel extends JPanel {

    private static final String[] COLUMNS = {"ID", "Kode", "Nama Kriteria", "Jenis Faktor"};

    private final KriteriaDAO dao = new KriteriaDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField txtCari = new JTextField(16);

    public KriteriaPanel() {
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

        JLabel lblTitle = new JLabel("Data Kriteria");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);

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
        List<Kriteria> daftar = dao.getAll();
        for (Kriteria k : daftar) {
            tableModel.addRow(new Object[]{
                k.getIdKriteria(),
                k.getKodeKriteria(),
                k.getNamaKriteria(),
                k.getJenisFaktor()
            });
        }
        if (selectedId > 0) selectById(selectedId);
    }

    private void onTambah() {
        Kriteria hasil = KriteriaFormDialog.tampilkan(this, "Tambah Kriteria", null);
        if (hasil == null) return;
        if (dao.insert(hasil)) refreshData();
        else JOptionPane.showMessageDialog(this, "Gagal menambah kriteria.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void onEdit() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk diedit.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Kriteria existing = dao.getById(id);
        if (existing == null) { refreshData(); return; }
        Kriteria hasil = KriteriaFormDialog.tampilkan(this, "Edit Kriteria", existing);
        if (hasil == null) return;
        if (dao.update(hasil)) refreshData();
        else JOptionPane.showMessageDialog(this, "Gagal update kriteria.", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void onHapus() {
        int id = getSelectedId();
        if (id <= 0) {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu untuk dihapus.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int row = table.convertRowIndexToModel(table.getSelectedRow());
        String kode = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Hapus kriteria \"" + kode + "\"?\nKriteria yang sudah dipakai di nilai/profil ideal tidak bisa dihapus.",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;
        if (dao.delete(id)) refreshData();
        else JOptionPane.showMessageDialog(
            this,
            "Gagal hapus kriteria. Kemungkinan masih ada data nilai/profil ideal yang memakai kriteria ini.",
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
