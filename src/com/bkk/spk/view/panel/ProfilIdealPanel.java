package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.dao.ProfilIdealDAO;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.ProfilIdeal;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel input Profil Ideal: pilih lowongan → atur nilai target tiap kriteria (skala 1-5).
 *
 * Disimpan per-row: id_profil_ideal sudah ada → update; belum ada → insert.
 * Tracking id lewat kolom tersembunyi di tabel (kolom 0).
 */
public class ProfilIdealPanel extends JPanel {

    private static final String[] COLUMNS = {"ID Ideal", "Kode", "Nama Kriteria", "Jenis", "Nilai Target"};
    private static final String[] NILAI_OPTIONS = {"1", "2", "3", "4", "5"};

    private final LowonganDAO lowonganDAO = new LowonganDAO();
    private final KriteriaDAO kriteriaDAO = new KriteriaDAO();
    private final ProfilIdealDAO profilIdealDAO = new ProfilIdealDAO();

    private final JComboBox<Lowongan> cbLowongan = new JComboBox<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblInfo = new JLabel(" ");

    public ProfilIdealPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Editor kolom Nilai Target -> combo 1-5
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JComboBox<>(NILAI_OPTIONS)));

        // Hide kolom ID Ideal
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new java.awt.Color(0xFC, 0xE4, 0xEC));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xF8, 0xBB, 0xD0)));
        add(scroll, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(lblInfo, BorderLayout.WEST);
        JButton btnSimpan = new JButton("Simpan Nilai Target");
        ButtonStyle.primary(btnSimpan);
        btnSimpan.addActionListener(e -> onSimpan());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnSimpan);
        south.add(btnWrap, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        muatLowongan();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTitle = new JLabel("Profil Ideal Lowongan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("Lowongan:"));
        cbLowongan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLowongan.addActionListener(e -> muatTabelUntukLowonganTerpilih());
        right.add(cbLowongan);
        toolbar.add(right, BorderLayout.EAST);

        return toolbar;
    }

    private void muatLowongan() {
        cbLowongan.removeAllItems();
        List<Lowongan> daftar = lowonganDAO.getAll();
        for (Lowongan l : daftar) cbLowongan.addItem(l);
        cbLowongan.setSelectedIndex(-1);
        if (!daftar.isEmpty()) cbLowongan.setSelectedIndex(0);
    }

    private void muatTabelUntukLowonganTerpilih() {
        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        tableModel.setRowCount(0);
        if (selected == null) {
            lblInfo.setText(" ");
            return;
        }

        List<Kriteria> semuaKriteria = kriteriaDAO.getAll();
        List<ProfilIdeal> existing = profilIdealDAO.getByLowongan(selected.getIdLowongan());
        Map<Integer, ProfilIdeal> mapExisting = new HashMap<>();
        for (ProfilIdeal pi : existing) mapExisting.put(pi.getKriteria().getIdKriteria(), pi);

        for (Kriteria k : semuaKriteria) {
            ProfilIdeal pi = mapExisting.get(k.getIdKriteria());
            int idIdeal = (pi != null) ? pi.getIdProfilIdeal() : 0;
            int nilai = (pi != null) ? (int) Math.round(pi.getNilaiTarget()) : 3;
            tableModel.addRow(new Object[]{
                idIdeal,
                k.getKodeKriteria(),
                k.getNamaKriteria(),
                k.getJenisFaktor(),
                String.valueOf(nilai)
            });
        }

        lblInfo.setText("Total " + semuaKriteria.size() + " kriteria untuk lowongan terpilih.");
    }

    private void onSimpan() {
        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih lowongan dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada kriteria untuk disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Simpan nilai target untuk lowongan \"" + selected.getPosisi() + "\"?",
            "Konfirmasi Simpan",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        // Map kriteria by kode untuk lookup nama & jenis
        List<Kriteria> semuaKriteria = kriteriaDAO.getAll();
        Map<String, Kriteria> byKode = new HashMap<>();
        for (Kriteria k : semuaKriteria) byKode.put(k.getKodeKriteria(), k);

        int sukses = 0, gagal = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int idIdeal = parseIntCell(tableModel.getValueAt(i, 0));
            String kode = (String) tableModel.getValueAt(i, 1);
            int nilai = parseIntCell(tableModel.getValueAt(i, 4));

            Kriteria k = byKode.get(kode);
            if (k == null) continue;

            ProfilIdeal pi = new ProfilIdeal();
            pi.setIdProfilIdeal(idIdeal);
            pi.setLowongan(selected);
            pi.setKriteria(k);
            pi.setNilaiTarget(nilai);

            boolean ok;
            if (idIdeal > 0) {
                ok = profilIdealDAO.update(pi);
            } else {
                ok = profilIdealDAO.insert(pi);
            }
            if (ok) sukses++; else gagal++;
        }

        muatTabelUntukLowonganTerpilih();
        JOptionPane.showMessageDialog(
            this,
            "Simpan selesai. Berhasil: " + sukses + (gagal > 0 ? " | Gagal: " + gagal : ""),
            "Info",
            gagal > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    private int parseIntCell(Object val) {
        if (val == null) return 0;
        try { return Integer.parseInt(val.toString().trim()); } catch (NumberFormatException e) { return 0; }
    }
}
