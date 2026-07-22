package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Kriteria;
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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog modal untuk Tambah atau Edit data Kandidat.
 *
 * Mode Tambah (initial == null): menampilkan section tambahan "Nilai Kandidat
 * per Kriteria" (combo 1-5 per kriteria) supaya user bisa langsung masukin nilai
 * target saat bikin kandidat baru. Setelah Simpan, caller dapetin
 * HasilForm.nilaiByKriteria untuk di-insertBatch ke tb_nilai_kandidat.
 *
 * Mode Edit (initial != null): hanya form data kandidat — nilai diedit lewat menu
 * Input Nilai supaya tidak ada dua jalan Ubah nilai yang bisa bikin bingung.
 */
public class KandidatFormDialog extends JDialog {

    private static final String[] NILAI_OPTIONS = {"1", "2", "3", "4", "5"};
    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final KriteriaDAO kriteriaDAO = new KriteriaDAO();
    private final Map<Integer, JComboBox<String>> comboByKriteriaId = new LinkedHashMap<>();

    private final JTextField txtNisn = new JTextField();
    private final JTextField txtNama = new JTextField();
    private final JTextField txtTanggalLahir = new JTextField();
    private final JTextArea txtAlamat = new JTextArea(2, 20);
    private final JTextField txtLinkCv = new JTextField();
    private final JTextField txtTahunLulus = new JTextField();

    private final Kandidat initial;
    private Kandidat hasilKandidat;
    private final Map<Integer, Integer> hasilNilai = new LinkedHashMap<>();
    private boolean saved = false;

    public KandidatFormDialog(Frame owner, String title, Kandidat initial) {
        super(owner, title, true);
        this.initial = initial;
        initComponents();
        isiDataAwal();
    }

    private void initComponents() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        root.add(buildKandidatPanel(), BorderLayout.NORTH);

        if (initial == null) {
            root.add(buildNilaiPanel(), BorderLayout.CENTER);
            setSize(540, 700);
        } else {
            setSize(540, 460);
        }

        root.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildKandidatPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Data Kandidat"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JScrollPane scrollAlamat = new JScrollPane(txtAlamat);
        scrollAlamat.setPreferredSize(new java.awt.Dimension(320, 50));

        addRow(form, gbc, 0, "Kode", txtNisn);
        addRow(form, gbc, 1, "Nama", txtNama);
        addRow(form, gbc, 2, "Tgl Lahir", txtTanggalLahir);
        addRow(form, gbc, 3, "Alamat", scrollAlamat);
        addRow(form, gbc, 4, "Link CV", txtLinkCv);
        addRow(form, gbc, 5, "Tahun Lulus", txtTahunLulus);

        txtTahunLulus.setPreferredSize(new java.awt.Dimension(120, 24));
        txtTanggalLahir.setPreferredSize(new java.awt.Dimension(150, 24));
        txtTanggalLahir.putClientProperty("JTextField.placeholderText", "dd-MM-yyyy");

        return form;
    }

    private JPanel buildNilaiPanel() {
        // Wrapper (BorderLayout) -> header label di NORTH + grid panel di CENTER.
        // Header dipisah dari grid supaya gak nabrak dengan row pertama (C1 - ...).
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));

        JLabel sectionTitle = new JLabel("Nilai Kandidat per Kriteria (skala 1-5)");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionTitle.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        wrapper.add(sectionTitle, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(new java.awt.Color(0xF8, 0xBB, 0xD0), 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;

        List<Kriteria> daftar = kriteriaDAO.getAll();
        if (daftar.isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;
            gbc.insets = new Insets(10, 10, 10, 10);
            JLabel kosong = new JLabel("(Belum ada kriteria di DB — buka menu Kriteria dulu.)");
            kosong.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            panel.add(kosong, gbc);
            wrapper.add(panel, BorderLayout.CENTER);
            return wrapper;
        }

        int row = 0;
        for (Kriteria k : daftar) {
            gbc.insets = new Insets(6, 10, 6, 10);

            String label = k.getKodeKriteria() + " - " + k.getNamaKriteria() + "  [" + k.getJenisFaktor() + "]";
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(lbl, gbc);

            JComboBox<String> cb = new JComboBox<>(NILAI_OPTIONS);
            cb.setSelectedItem("3");
            cb.setPreferredSize(new java.awt.Dimension(70, 24));

            gbc.gridx = 1; gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(cb, gbc);

            comboByKriteriaId.put(k.getIdKriteria(), cb);
            row++;
        }
        wrapper.add(panel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnSimpan = new JButton("Simpan");
        JButton btnBatal = new JButton("Batal");
        ButtonStyle.primary(btnSimpan);
        ButtonStyle.secondary(btnBatal);
        btnSimpan.addActionListener(e -> onSimpan());
        btnBatal.addActionListener(e -> dispose());
        buttonPanel.add(btnSimpan);
        buttonPanel.add(btnBatal);
        getRootPane().setDefaultButton(btnSimpan);
        return buttonPanel;
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
        if (initial == null) {
            txtTahunLulus.setText(String.valueOf(java.time.Year.now().getValue()));
            return;
        }
        txtNisn.setText(initial.getNisn());
        txtNama.setText(initial.getNama());
        if (initial.getTanggalLahir() != null) {
            txtTanggalLahir.setText(initial.getTanggalLahir().format(FMT_TGL));
        }
        txtAlamat.setText(initial.getAlamat());
        txtLinkCv.setText(initial.getLinkCv());
        txtTahunLulus.setText(String.valueOf(initial.getTahunLulus()));
    }

    private void onSimpan() {
        String nisn = txtNisn.getText().trim();
        String nama = txtNama.getText().trim();
        String tglStr = txtTanggalLahir.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String linkCv = txtLinkCv.getText().trim();
        String tahunStr = txtTahunLulus.getText().trim();

        if (nisn.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNisn.requestFocusInWindow();
            return;
        }
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocusInWindow();
            return;
        }
        if (tglStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tanggal lahir tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTanggalLahir.requestFocusInWindow();
            return;
        }
        LocalDate tanggalLahir;
        try {
            tanggalLahir = LocalDate.parse(tglStr, FMT_TGL);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal lahir harus dd-MM-yyyy (contoh: 15-03-2005).", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTanggalLahir.requestFocusInWindow();
            return;
        }
        if (alamat.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Alamat tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtAlamat.requestFocusInWindow();
            return;
        }
        if (linkCv.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Link CV tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtLinkCv.requestFocusInWindow();
            return;
        }
        int tahun;
        try {
            tahun = Integer.parseInt(tahunStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun Lulus harus berupa angka.", "Validasi", JOptionPane.WARNING_MESSAGE);
            txtTahunLulus.requestFocusInWindow();
            return;
        }

        hasilKandidat = (initial == null) ? new Kandidat() : initial;
        hasilKandidat.setNisn(nisn);
        hasilKandidat.setNama(nama);
        hasilKandidat.setTanggalLahir(tanggalLahir);
        hasilKandidat.setAlamat(alamat);
        hasilKandidat.setLinkCv(linkCv);
        hasilKandidat.setTahunLulus(tahun);

        hasilNilai.clear();
        for (Map.Entry<Integer, JComboBox<String>> e : comboByKriteriaId.entrySet()) {
            int nilai = Integer.parseInt((String) e.getValue().getSelectedItem());
            hasilNilai.put(e.getKey(), nilai);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Kandidat getHasilKandidat() { return hasilKandidat; }
    public Map<Integer, Integer> getHasilNilai() { return hasilNilai; }

    /**
     * Hasil dialog Tambah/Edit: kandidat + map nilai per kriteria (kosong di mode
     * Edit / kalau tidak ada kriteria). Dipake caller untuk insert kandidat + batch nilai.
     */
    public static class HasilForm {
        private final Kandidat kandidat;
        private final Map<Integer, Integer> nilaiByKriteria;

        public HasilForm(Kandidat kandidat, Map<Integer, Integer> nilaiByKriteria) {
            this.kandidat = kandidat;
            this.nilaiByKriteria = nilaiByKriteria;
        }
        public Kandidat getKandidat() { return kandidat; }
        public Map<Integer, Integer> getNilaiByKriteria() { return nilaiByKriteria; }
    }

    /** Tampilkan dialog modal. Return HasilForm (kandidat + nilai) atau null bila batal. */
    public static HasilForm tampilkan(Frame owner, String title, Kandidat initial) {
        KandidatFormDialog dlg = new KandidatFormDialog(owner, title, initial);
        dlg.setVisible(true);
        return dlg.saved ? new HasilForm(dlg.hasilKandidat, dlg.hasilNilai) : null;
    }

    /** Overload dari komponen Swing manapun — cari root Window-nya otomatis. */
    public static HasilForm tampilkan(java.awt.Component parent, String title, Kandidat initial) {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(parent);
        return tampilkan(owner, title, initial);
    }
}
