package com.bkk.spk.view.panel;

import com.bkk.spk.model.Kriteria;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Dialog Tambah/Edit Kriteria. */
public class KriteriaFormDialog extends JDialog {

    private final JTextField txtKode = new JTextField();
    private final JTextField txtNama = new JTextField();
    private final JComboBox<String> cbJenis = new JComboBox<>(new String[]{Kriteria.CORE_FACTOR, Kriteria.SECONDARY_FACTOR});

    private final Kriteria initial;
    private Kriteria hasil;
    private boolean saved = false;

    public KriteriaFormDialog(Frame owner, String title, Kriteria initial) {
        super(owner, title, true);
        this.initial = initial;
        initComponents();
        isiDataAwal();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 220);
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

        addRow(form, gbc, 0, "Kode Kriteria", txtKode);
        addRow(form, gbc, 1, "Nama Kriteria", txtNama);
        addRow(form, gbc, 2, "Jenis Faktor", cbJenis);

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
        lbl.setPreferredSize(new java.awt.Dimension(110, 24));
        form.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        form.add(field, gbc);
    }

    private void isiDataAwal() {
        if (initial == null) return;
        txtKode.setText(initial.getKodeKriteria());
        txtNama.setText(initial.getNamaKriteria());
        cbJenis.setSelectedItem(initial.getJenisFaktor());
    }

    private void onSimpan() {
        String kode = txtKode.getText().trim();
        String nama = txtNama.getText().trim();
        String jenis = (String) cbJenis.getSelectedItem();

        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode kriteria tidak boleh kosong (contoh: C1).", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtKode.requestFocusInWindow();
            return;
        }
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kriteria tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocusInWindow();
            return;
        }

        hasil = (initial == null) ? new Kriteria() : initial;
        hasil.setKodeKriteria(kode);
        hasil.setNamaKriteria(nama);
        hasil.setJenisFaktor(jenis);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Kriteria getHasil() { return hasil; }

    public static Kriteria tampilkan(Frame owner, String title, Kriteria initial) {
        KriteriaFormDialog dlg = new KriteriaFormDialog(owner, title, initial);
        dlg.setVisible(true);
        return dlg.saved ? dlg.hasil : null;
    }

    public static Kriteria tampilkan(java.awt.Component parent, String title, Kriteria initial) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        return tampilkan(owner, title, initial);
    }
}
