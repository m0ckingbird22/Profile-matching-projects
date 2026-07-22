package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KandidatDAO;
import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.dao.NilaiKandidatDAO;
import com.bkk.spk.model.Kandidat;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiKandidat;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel "Data Nilai Kandidat" — matrix view: baris = kandidat, kolom = kriteria,
 * sel = nilai 1-5 (combo box). Satu tombol Simpan menyimpan SEMUA baris yang
 * tampil (insert baru / update yang sudah ada).
 *
 * Save logic: id_nilai sudah ada -> update; belum ada -> insert.
 */
public class InputNilaiPanel extends JPanel {

    private static final String[] NILAI_OPTIONS = {"1", "2", "3", "4", "5"};
    private static final Color BG = new Color(0xFD, 0xEA, 0xF1);
    private static final Color BORDER = new Color(0xF8, 0xBB, 0xD0);
    private static final Color HEADER_BG = new Color(0xFC, 0xE4, 0xEC);

    private final KandidatDAO kandidatDAO = new KandidatDAO();
    private final KriteriaDAO kriteriaDAO = new KriteriaDAO();
    private final NilaiKandidatDAO nilaiDAO = new NilaiKandidatDAO();

    private final JTextField txtCari = new JTextField(18);
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel lblInfo = new JLabel(" ");

    private List<Kriteria> daftarKriteria = new ArrayList<>();
    private List<Kandidat> daftarKandidatTampil = new ArrayList<>();
    private final Map<String, Integer> idNilaiMap = new HashMap<>();

    public InputNilaiPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        tableModel = new DefaultTableModel(new Object[]{"Alternatif"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c > 0; }
        };
        table = new JTable(tableModel);

        add(buildToolbar(), BorderLayout.NORTH);

        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(0xE5, 0xE7, 0xEB));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        add(scroll, BorderLayout.CENTER);

        add(buildSouth(), BorderLayout.SOUTH);

        siapkanStrukturKolom();
        muatBaris();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTitle = new JLabel("Data Nilai Kandidat — Matrix");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("Cari kandidat:"));
        txtCari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { muatBaris(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { muatBaris(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { muatBaris(); }
        });
        right.add(txtCari);

        JButton btnReload = new JButton("Muat Ulang");
        ButtonStyle.secondary(btnReload);
        btnReload.addActionListener(e -> { siapkanStrukturKolom(); muatBaris(); });
        right.add(btnReload);
        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel buildSouth() {
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(lblInfo, BorderLayout.WEST);
        JButton btnSimpan = new JButton("Simpan Semua Nilai");
        ButtonStyle.primary(btnSimpan);
        btnSimpan.addActionListener(e -> onSimpan());
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrap.setOpaque(false);
        btnWrap.add(btnSimpan);
        south.add(btnWrap, BorderLayout.EAST);
        return south;
    }

    /** Bangun ulang kolom (Alternatif + per-kriteria dari DB). Panggil saat kriteria berubah. */
    private void siapkanStrukturKolom() {
        daftarKriteria = kriteriaDAO.getAll();
        String[] cols = new String[1 + daftarKriteria.size()];
        cols[0] = "Alternatif";
        for (int i = 0; i < daftarKriteria.size(); i++) {
            cols[i + 1] = singkatKriteria(daftarKriteria.get(i));
        }
        tableModel.setColumnIdentifiers(cols);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(240);
        for (int i = 1; i <= daftarKriteria.size(); i++) {
            cm.getColumn(i).setCellEditor(new DefaultCellEditor(new JComboBox<>(NILAI_OPTIONS)));
            cm.getColumn(i).setPreferredWidth(70);
            cm.getColumn(i).setMaxWidth(90);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
    }

    /** Isi baris tabel dari daftar kandidat (dengan filter cari). Jangan sentuh struktur kolom. */
    private void muatBaris() {
        if (table.isEditing()) table.getCellEditor().cancelCellEditing();

        String filter = txtCari.getText().trim().toLowerCase();
        daftarKandidatTampil = new ArrayList<>();
        for (Kandidat k : kandidatDAO.getAll()) {
            if (!filter.isEmpty()
                && !k.getNama().toLowerCase().contains(filter)
                && !k.getNisn().toLowerCase().contains(filter)) {
                continue;
            }
            daftarKandidatTampil.add(k);
        }

        tableModel.setRowCount(0);
        idNilaiMap.clear();

        for (int idx = 0; idx < daftarKandidatTampil.size(); idx++) {
            Kandidat k = daftarKandidatTampil.get(idx);
            List<NilaiKandidat> existing = nilaiDAO.getByKandidat(k.getIdKandidat());
            Map<Integer, NilaiKandidat> mapPerKriteria = new HashMap<>();
            for (NilaiKandidat nk : existing) {
                mapPerKriteria.put(nk.getKriteria().getIdKriteria(), nk);
            }

            Object[] row = new Object[1 + daftarKriteria.size()];
            row[0] = "A" + (idx + 1) + " — " + k.getNama();
            for (int j = 0; j < daftarKriteria.size(); j++) {
                Kriteria kr = daftarKriteria.get(j);
                NilaiKandidat nk = mapPerKriteria.get(kr.getIdKriteria());
                int nilai = (nk != null) ? (int) Math.round(nk.getNilaiKandidat()) : 3;
                row[j + 1] = String.valueOf(nilai);
                if (nk != null) idNilaiMap.put(idx + ":" + j, nk.getIdNilai());
            }
            tableModel.addRow(row);
        }

        lblInfo.setText(daftarKandidatTampil.size() + " kandidat • " + daftarKriteria.size() + " kriteria.");
    }

    private void onSimpan() {
        if (daftarKandidatTampil.isEmpty() || daftarKriteria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Simpan semua nilai untuk " + daftarKandidatTampil.size() + " kandidat?",
            "Konfirmasi Simpan",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        int sukses = 0, gagal = 0;
        for (int i = 0; i < daftarKandidatTampil.size(); i++) {
            Kandidat k = daftarKandidatTampil.get(i);
            for (int j = 0; j < daftarKriteria.size(); j++) {
                Kriteria kr = daftarKriteria.get(j);
                Object val = tableModel.getValueAt(i, j + 1);
                int nilai = parseInt(val);
                Integer idNilai = idNilaiMap.get(i + ":" + j);
                if (idNilai == null) idNilai = 0;

                NilaiKandidat nk = new NilaiKandidat();
                nk.setIdNilai(idNilai);
                nk.setKandidat(k);
                nk.setKriteria(kr);
                nk.setNilaiKandidat(nilai);

                boolean ok;
                if (idNilai > 0) ok = nilaiDAO.update(nk);
                else ok = nilaiDAO.insert(nk);
                if (ok) sukses++; else gagal++;
            }
        }
        muatBaris();
        JOptionPane.showMessageDialog(
            this,
            "Simpan selesai. Berhasil: " + sukses + (gagal > 0 ? " | Gagal: " + gagal : ""),
            "Info",
            gagal > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    private int parseInt(Object val) {
        if (val == null) return 3;
        try { return Integer.parseInt(val.toString().trim()); } catch (NumberFormatException e) { return 3; }
    }

    /** Header kolom matrix = kode kriteria (C1..C8) sesuai DB. */
    private String singkatKriteria(Kriteria k) {
        String kode = k.getKodeKriteria() == null ? "??" : k.getKodeKriteria().toUpperCase();
        return kode;
    }
}
