package com.bkk.spk.view.panel;

import com.bkk.spk.model.Perusahaan;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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

/** Dialog Tambah/Edit Perusahaan. */
public class PerusahaanFormDialog extends JDialog {

    private final JTextField txtNama = new JTextField();
    private final JTextField txtBidang = new JTextField();
    private final JTextArea txtAlamat = new JTextArea(3, 30);

    private final Perusahaan initial;
    private Perusahaan hasil;
    private boolean saved = false;

    public PerusahaanFormDialog(Frame owner, String title, Perusahaan initial) {
        super(owner, title, true);
        this.initial = initial;
        initComponents();
        isiDataAwal();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(440, 320);
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

        addRow(form, gbc, 0, "Nama Perusahaan", txtNama);
        addRow(form, gbc, 1, "Bidang Industri", txtBidang);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblAlamat = new JLabel("Alamat");
        lblAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblAlamat.setPreferredSize(new Dimension(110, 24));
        form.add(lblAlamat, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtAlamat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);
        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        scrollAlamat.setPreferredSize(new Dimension(280, 70));
        form.add(scrollAlamat, gbc);

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
        if (initial == null) return;
        txtNama.setText(initial.getNamaPerusahaan());
        txtBidang.setText(initial.getBidangIndustri());
        txtAlamat.setText(initial.getAlamat());
    }

    private void onSimpan() {
        String nama = txtNama.getText().trim();
        String bidang = txtBidang.getText().trim();
        String alamat = txtAlamat.getText().trim();

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama perusahaan tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocusInWindow();
            return;
        }

        hasil = (initial == null) ? new Perusahaan() : initial;
        hasil.setNamaPerusahaan(nama);
        hasil.setBidangIndustri(bidang);
        hasil.setAlamat(alamat);

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Perusahaan getHasil() { return hasil; }

    public static Perusahaan tampilkan(Frame owner, String title, Perusahaan initial) {
        PerusahaanFormDialog dlg = new PerusahaanFormDialog(owner, title, initial);
        dlg.setVisible(true);
        return dlg.saved ? dlg.hasil : null;
    }

    public static Perusahaan tampilkan(java.awt.Component parent, String title, Perusahaan initial) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        return tampilkan(owner, title, initial);
    }
}
