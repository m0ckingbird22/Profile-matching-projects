package com.bkk.spk.view.panel;

import com.bkk.spk.dao.PerusahaanDAO;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/** Dialog Tambah/Edit Lowongan. Combo perusahaan dimuat dari DB. */
public class LowonganFormDialog extends JDialog {

    private final JComboBox<Perusahaan> cbPerusahaan = new JComboBox<>();
    private final JTextField txtPosisi = new JTextField();
    private final JTextField txtKuota = new JTextField();
    private final JComboBox<String> cbStatus = new JComboBox<>(new String[]{"BUKA", "TUTUP"});
    private final JTextArea txtDeskripsi = new JTextArea(3, 30);

    private final Lowongan initial;
    private Lowongan hasil;
    private boolean saved = false;

    public LowonganFormDialog(Frame owner, String title, Lowongan initial) {
        super(owner, title, true);
        this.initial = initial;
        muatPerusahaan();
        initComponents();
        isiDataAwal();
    }

    private void muatPerusahaan() {
        cbPerusahaan.removeAllItems();
        List<Perusahaan> daftar = new PerusahaanDAO().getAll();
        for (Perusahaan p : daftar) cbPerusahaan.addItem(p);
        cbPerusahaan.setSelectedIndex(-1);
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(480, 420);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        addRow(form, gbc, 0, "Perusahaan", cbPerusahaan);
        addRow(form, gbc, 1, "Posisi", txtPosisi);
        addRow(form, gbc, 2, "Kuota", txtKuota);
        addRow(form, gbc, 3, "Status", cbStatus);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDesk = new JLabel("Deskripsi");
        lblDesk.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesk.setPreferredSize(new Dimension(110, 24));
        form.add(lblDesk, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDeskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        JScrollPane scrollDesk = new JScrollPane(txtDeskripsi);
        scrollDesk.setPreferredSize(new Dimension(300, 80));
        form.add(scrollDesk, gbc);

        txtKuota.setPreferredSize(new Dimension(80, 24));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        ButtonStyle.primary(btnSimpan);
        ButtonStyle.secondary(btnBatal);
        btnSimpan.addActionListener(e -> onSimpan());
        btnBatal.addActionListener(e -> dispose());
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnBatal);

        root.add(form, BorderLayout.CENTER);
        root.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(btnSimpan);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(110, 24));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(field, gbc);
    }

    private void isiDataAwal() {
        if (initial == null) {
            txtKuota.setText("1");
            return;
        }
        // pilih perusahaan yang cocok di combo
        for (int i = 0; i < cbPerusahaan.getItemCount(); i++) {
            Perusahaan p = cbPerusahaan.getItemAt(i);
            if (p.getIdPerusahaan() == initial.getPerusahaan().getIdPerusahaan()) {
                cbPerusahaan.setSelectedIndex(i);
                break;
            }
        }
        txtPosisi.setText(initial.getPosisi());
        txtKuota.setText(String.valueOf(initial.getKuota()));
        cbStatus.setSelectedItem(initial.getStatus());
        txtDeskripsi.setText(initial.getDeskripsi());
    }

    private void onSimpan() {
        Perusahaan selected = (Perusahaan) cbPerusahaan.getSelectedItem();
        String posisi = txtPosisi.getText().trim();
        String kuotaStr = txtKuota.getText().trim();
        String status = (String) cbStatus.getSelectedItem();
        String deskripsi = txtDeskripsi.getText().trim();

        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih perusahaan dulu.", "Validasi", JOptionPane.WARNING_MESSAGE);
            cbPerusahaan.requestFocusInWindow();
            return;
        }
        if (posisi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Posisi tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtPosisi.requestFocusInWindow();
            return;
        }
        int kuota;
        try {
            kuota = Integer.parseInt(kuotaStr);
            if (kuota < 1) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Kuota harus angka > 0.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtKuota.requestFocusInWindow();
            return;
        }

        hasil = (initial == null) ? new Lowongan() : initial;
        hasil.setPerusahaan(selected);
        hasil.setPosisi(posisi);
        hasil.setKuota(kuota);
        hasil.setStatus(status);
        hasil.setDeskripsi(deskripsi);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Lowongan getHasil() { return hasil; }

    public static Lowongan tampilkan(Frame owner, String title, Lowongan initial) {
        LowonganFormDialog dlg = new LowonganFormDialog(owner, title, initial);
        dlg.setVisible(true);
        return dlg.saved ? dlg.hasil : null;
    }

    public static Lowongan tampilkan(java.awt.Component parent, String title, Lowongan initial) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        return tampilkan(owner, title, initial);
    }
}
