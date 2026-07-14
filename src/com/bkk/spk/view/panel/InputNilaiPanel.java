package com.bkk.spk.view.panel;

import com.bkk.spk.dao.KriteriaDAO;
import com.bkk.spk.dao.NilaiSiswaDAO;
import com.bkk.spk.dao.SiswaDAO;
import com.bkk.spk.model.Kriteria;
import com.bkk.spk.model.NilaiSiswa;
import com.bkk.spk.model.Siswa;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
 * Panel input Nilai Siswa dengan 2 tab:
 *   1. "Per Siswa"  — pilih 1 siswa, isi nilai per kriteria (mode lama).
 *   2. "Matrix"     — semua siswa (baris) × semua kriteria (kolom),
 *                     nilai 1-5 di-edit langsung di sel. Satu tombol Simpan
 *                     untuk seluruh baris yang tampil.
 *
 * Save logic sama: id_nilai sudah ada → update, belum ada → insert.
 */
public class InputNilaiPanel extends JPanel {

    private static final String[] NILAI_OPTIONS = {"1", "2", "3", "4", "5"};
    private static final Color BG = new Color(0xFD, 0xEA, 0xF1);
    private static final Color BORDER = new Color(0xF8, 0xBB, 0xD0);
    private static final Color HEADER_BG = new Color(0xFC, 0xE4, 0xEC);

    private final SiswaDAO siswaDAO = new SiswaDAO();
    private final KriteriaDAO kriteriaDAO = new KriteriaDAO();
    private final NilaiSiswaDAO nilaiDAO = new NilaiSiswaDAO();

    public InputNilaiPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.setBackground(BG);
        tabs.setOpaque(true);

        tabs.addTab("Per Siswa", buildPerSiswaTab());
        tabs.addTab("Matrix (Semua Siswa)", buildMatrixTab());

        add(tabs, BorderLayout.CENTER);
    }

    // =================== TAB 1: PER SISWA (mode lama) ===================

    private JPanel buildPerSiswaTab() {
        JPanel host = new JPanel(new BorderLayout(0, 12));
        host.setBackground(BG);
        new PerSiswaController(host).init();
        return host;
    }

    /** Logika form per-siswa di-encapsulate supaya tidak bercampur dengan matrix. */
    private class PerSiswaController {
        private static final String[] COLUMNS = {"ID Nilai", "Kode", "Nama Kriteria", "Jenis", "Nilai"};

        private final JPanel host;
        private final JComboBox<Siswa> cbSiswa = new JComboBox<>();
        private final JTextField txtCariSiswa = new JTextField(14);
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final JLabel lblInfo = new JLabel(" ");

        PerSiswaController(JPanel host) {
            this.host = host;
            tableModel = new DefaultTableModel(COLUMNS, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c == 4; }
            };
            table = new JTable(tableModel);
        }

        void init() {
            host.add(buildToolbar(), BorderLayout.NORTH);

            table.setRowHeight(28);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.getTableHeader().setReorderingAllowed(false);
            table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JComboBox<>(NILAI_OPTIONS)));
            // kolom ID Nilai disembunyikan
            table.getColumnModel().getColumn(0).setMinWidth(0);
            table.getColumnModel().getColumn(0).setMaxWidth(0);
            table.getColumnModel().getColumn(0).setWidth(0);
            JTableHeader header = table.getTableHeader();
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            header.setBackground(HEADER_BG);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createLineBorder(BORDER));
            host.add(scroll, BorderLayout.CENTER);

            host.add(buildSouth(), BorderLayout.SOUTH);

            muatComboSiswa();
        }

        private JPanel buildToolbar() {
            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setOpaque(false);

            JLabel lblTitle = new JLabel("Input Nilai — Per Siswa");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            toolbar.add(lblTitle, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);
            right.add(new JLabel("Cari:"));
            txtCariSiswa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            txtCariSiswa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { muatComboSiswa(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { muatComboSiswa(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { muatComboSiswa(); }
            });
            right.add(txtCariSiswa);

            right.add(new JLabel("  Siswa:"));
            cbSiswa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            cbSiswa.setRenderer(new javax.swing.ListCellRenderer<Siswa>() {
                private final javax.swing.DefaultListCellRenderer delegate = new javax.swing.DefaultListCellRenderer();
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<? extends Siswa> list, Siswa value, int index, boolean isSelected, boolean cellHasFocus) {
                    String label = (value == null) ? " " : value.getNisn() + " - " + value.getNama() + " (" + value.getJurusan() + ")";
                    return delegate.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
                }
            });
            cbSiswa.addActionListener(e -> muatTabelUntukSiswaTerpilih());
            right.add(cbSiswa);
            toolbar.add(right, BorderLayout.EAST);
            return toolbar;
        }

        private JPanel buildSouth() {
            JPanel south = new JPanel(new BorderLayout());
            south.setOpaque(false);
            south.add(lblInfo, BorderLayout.WEST);
            JButton btnSimpan = new JButton("Simpan Nilai");
            ButtonStyle.primary(btnSimpan);
            btnSimpan.addActionListener(e -> onSimpan());
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            btnWrap.setOpaque(false);
            btnWrap.add(btnSimpan);
            south.add(btnWrap, BorderLayout.EAST);
            return south;
        }

        private void muatComboSiswa() {
            String filter = txtCariSiswa.getText().trim().toLowerCase();
            Siswa selectedSebelumnya = (Siswa) cbSiswa.getSelectedItem();

            cbSiswa.removeAllItems();
            List<Siswa> daftar = siswaDAO.getAll();
            boolean selectedMasihAda = false;
            for (Siswa s : daftar) {
                if (!filter.isEmpty()
                    && !s.getNama().toLowerCase().contains(filter)
                    && !s.getNisn().toLowerCase().contains(filter)) {
                    continue;
                }
                cbSiswa.addItem(s);
                if (selectedSebelumnya != null && s.getIdSiswa() == selectedSebelumnya.getIdSiswa()) {
                    selectedMasihAda = true;
                }
            }
            if (selectedMasihAda) cbSiswa.setSelectedItem(selectedSebelumnya);
            else if (cbSiswa.getItemCount() > 0) cbSiswa.setSelectedIndex(0);
        }

        private void muatTabelUntukSiswaTerpilih() {
            Siswa selected = (Siswa) cbSiswa.getSelectedItem();
            tableModel.setRowCount(0);
            if (selected == null) {
                lblInfo.setText(" ");
                return;
            }

            List<Kriteria> semuaKriteria = kriteriaDAO.getAll();
            List<NilaiSiswa> existing = nilaiDAO.getBySiswa(selected.getIdSiswa());
            Map<Integer, NilaiSiswa> mapExisting = new HashMap<>();
            for (NilaiSiswa ns : existing) mapExisting.put(ns.getKriteria().getIdKriteria(), ns);

            for (Kriteria k : semuaKriteria) {
                NilaiSiswa ns = mapExisting.get(k.getIdKriteria());
                int idNilai = (ns != null) ? ns.getIdNilai() : 0;
                int nilai = (ns != null) ? (int) Math.round(ns.getNilaiKandidat()) : 3;
                tableModel.addRow(new Object[]{
                    idNilai,
                    k.getKodeKriteria(),
                    k.getNamaKriteria(),
                    k.getJenisFaktor(),
                    String.valueOf(nilai)
                });
            }

            lblInfo.setText("Mengisi nilai untuk: " + selected.getNama() + " — " + semuaKriteria.size() + " kriteria.");
        }

        private void onSimpan() {
            Siswa selected = (Siswa) cbSiswa.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(host, "Pilih siswa dulu.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(host, "Tidak ada kriteria untuk disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                host,
                "Simpan nilai untuk siswa \"" + selected.getNama() + "\"?",
                "Konfirmasi Simpan",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            List<Kriteria> semuaKriteria = kriteriaDAO.getAll();
            Map<String, Kriteria> byKode = new HashMap<>();
            for (Kriteria k : semuaKriteria) byKode.put(k.getKodeKriteria(), k);

            int sukses = 0, gagal = 0;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                int idNilai = parseIntCell(tableModel.getValueAt(i, 0));
                String kode = (String) tableModel.getValueAt(i, 1);
                int nilai = parseIntCell(tableModel.getValueAt(i, 4));

                Kriteria k = byKode.get(kode);
                if (k == null) continue;

                NilaiSiswa ns = new NilaiSiswa();
                ns.setIdNilai(idNilai);
                ns.setSiswa(selected);
                ns.setKriteria(k);
                ns.setNilaiKandidat(nilai);

                boolean ok;
                if (idNilai > 0) ok = nilaiDAO.update(ns);
                else ok = nilaiDAO.insert(ns);
                if (ok) sukses++; else gagal++;
            }

            muatTabelUntukSiswaTerpilih();
            JOptionPane.showMessageDialog(
                host,
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

    // =================== TAB 2: MATRIX VIEW ===================

    private JPanel buildMatrixTab() {
        JPanel host = new JPanel(new BorderLayout(0, 12));
        host.setBackground(BG);
        new MatrixController(host).init();
        return host;
    }

    /**
     * Tabel matrix: baris = siswa (label "A1 — Nama"), kolom = kode kriteria,
     * sel = nilai 1-5 (editable via combobox). Satu klik Simpan menyimpan
     * SEMUA baris yang tampil.
     */
    private class MatrixController {
        private final JPanel host;
        private final JTextField txtCari = new JTextField(18);
        private final DefaultTableModel tableModel;
        private final JTable table;
        private final JLabel lblInfo = new JLabel(" ");

        private List<Kriteria> daftarKriteria = new ArrayList<>();
        private List<Siswa> daftarSiswaTampil = new ArrayList<>();
        private final Map<String, Integer> idNilaiMap = new HashMap<>();

        MatrixController(JPanel host) {
            this.host = host;
            tableModel = new DefaultTableModel(new Object[]{"Alternatif"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return c > 0; }
            };
            table = new JTable(tableModel);
        }

        void init() {
            host.add(buildToolbar(), BorderLayout.NORTH);

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
            host.add(scroll, BorderLayout.CENTER);

            host.add(buildSouth(), BorderLayout.SOUTH);

            siapkanStrukturKolom();
            muatBaris();
        }

        private JPanel buildToolbar() {
            JPanel toolbar = new JPanel(new BorderLayout());
            toolbar.setOpaque(false);

            JLabel lblTitle = new JLabel("Input Nilai — Matrix");
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            toolbar.add(lblTitle, BorderLayout.WEST);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            right.setOpaque(false);
            right.add(new JLabel("Cari siswa:"));
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

        /** Isi baris tabel dari daftar siswa (dengan filter cari). Jangan sentuh struktur kolom. */
        private void muatBaris() {
            if (table.isEditing()) table.getCellEditor().cancelCellEditing();

            String filter = txtCari.getText().trim().toLowerCase();
            daftarSiswaTampil = new ArrayList<>();
            for (Siswa s : siswaDAO.getAll()) {
                if (!filter.isEmpty()
                    && !s.getNama().toLowerCase().contains(filter)
                    && !s.getNisn().toLowerCase().contains(filter)) {
                    continue;
                }
                daftarSiswaTampil.add(s);
            }

            tableModel.setRowCount(0);
            idNilaiMap.clear();

            for (int idx = 0; idx < daftarSiswaTampil.size(); idx++) {
                Siswa s = daftarSiswaTampil.get(idx);
                List<NilaiSiswa> existing = nilaiDAO.getBySiswa(s.getIdSiswa());
                Map<Integer, NilaiSiswa> mapPerKriteria = new HashMap<>();
                for (NilaiSiswa ns : existing) {
                    mapPerKriteria.put(ns.getKriteria().getIdKriteria(), ns);
                }

                Object[] row = new Object[1 + daftarKriteria.size()];
                row[0] = "A" + (idx + 1) + " — " + s.getNama();
                for (int j = 0; j < daftarKriteria.size(); j++) {
                    Kriteria k = daftarKriteria.get(j);
                    NilaiSiswa ns = mapPerKriteria.get(k.getIdKriteria());
                    int nilai = (ns != null) ? (int) Math.round(ns.getNilaiKandidat()) : 3;
                    row[j + 1] = String.valueOf(nilai);
                    if (ns != null) idNilaiMap.put(idx + ":" + j, ns.getIdNilai());
                }
                tableModel.addRow(row);
            }

            lblInfo.setText(daftarSiswaTampil.size() + " siswa • " + daftarKriteria.size() + " kriteria.");
        }

        private void onSimpan() {
            if (daftarSiswaTampil.isEmpty() || daftarKriteria.isEmpty()) {
                JOptionPane.showMessageDialog(host, "Tidak ada data untuk disimpan.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(
                host,
                "Simpan semua nilai untuk " + daftarSiswaTampil.size() + " siswa?",
                "Konfirmasi Simpan",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            int sukses = 0, gagal = 0;
            for (int i = 0; i < daftarSiswaTampil.size(); i++) {
                Siswa s = daftarSiswaTampil.get(i);
                for (int j = 0; j < daftarKriteria.size(); j++) {
                    Kriteria k = daftarKriteria.get(j);
                    Object val = tableModel.getValueAt(i, j + 1);
                    int nilai = parseInt(val);
                    Integer idNilai = idNilaiMap.get(i + ":" + j);
                    if (idNilai == null) idNilai = 0;

                    NilaiSiswa ns = new NilaiSiswa();
                    ns.setIdNilai(idNilai);
                    ns.setSiswa(s);
                    ns.setKriteria(k);
                    ns.setNilaiKandidat(nilai);

                    boolean ok;
                    if (idNilai > 0) ok = nilaiDAO.update(ns);
                    else ok = nilaiDAO.insert(ns);
                    if (ok) sukses++; else gagal++;
                }
            }
            muatBaris();
            JOptionPane.showMessageDialog(
                host,
                "Simpan selesai. Berhasil: " + sukses + (gagal > 0 ? " | Gagal: " + gagal : ""),
                "Info",
                gagal > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE
            );
        }

        private int parseInt(Object val) {
            if (val == null) return 3;
            try { return Integer.parseInt(val.toString().trim()); } catch (NumberFormatException e) { return 3; }
        }

        /** Singkat nama kriteria jadi 2 huruf biar header kolom matrix ringkas. */
        private String singkatKriteria(Kriteria k) {
            String nama = k.getNamaKriteria() == null ? "" : k.getNamaKriteria().toLowerCase().trim();
            if (nama.contains("kompetensi")) return "KS";
            if (nama.contains("kesesuaian")) return "KB";
            if (nama.contains("akademik")) return "NA";
            if (nama.contains("minat")) return "MS";
            if (nama.contains("pkl") || nama.contains("pengalaman")) return "PK";
            if (nama.contains("disiplin")) return "KD";
            if (nama.contains("komunikasi")) return "KK";
            if (nama.contains("sikap") || nama.contains("etika")) return "SE";
            String kode = k.getKodeKriteria() == null ? "??" : k.getKodeKriteria().toUpperCase();
            return kode.length() >= 2 ? kode.substring(0, 2) : kode;
        }
    }
}
