package com.bkk.spk.view.panel;

import com.bkk.spk.dao.HasilAkhirDAO;
import com.bkk.spk.dao.HasilGapDAO;
import com.bkk.spk.dao.LowonganDAO;
import com.bkk.spk.dao.NilaiKandidatDAO;
import com.bkk.spk.dao.ProfilIdealDAO;
import com.bkk.spk.model.HasilAkhir;
import com.bkk.spk.model.HasilGap;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.model.NilaiKandidat;
import com.bkk.spk.model.ProfilIdeal;
import com.bkk.spk.service.LaporanPdfExporter;
import com.bkk.spk.view.util.ButtonStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel laporan hasil seleksi: pilih lowongan → lihat ranking lengkap,
 * klik baris kandidat → lihat detail perhitungan (Nilai, Target, GAP, Bobot) per kriteria
 * + label formula NCF/NSF/Nilai Akhir yang dihitung step-by-step.
 */
public class LaporanHasilPanel extends JPanel {

    private static final String[] RANK_COLUMNS = {
        "Rank", "NISN", "Nama", "NCF", "NSF", "Total", "Status"
    };
    private static final String[] GAP_COLUMNS = {
        "Kode", "Nama Kriteria", "Jenis", "Nilai", "Target", "GAP", "Bobot"
    };

    private final LowonganDAO lowonganDAO = new LowonganDAO();
    private final HasilAkhirDAO hasilAkhirDAO = new HasilAkhirDAO();
    private final HasilGapDAO hasilGapDAO = new HasilGapDAO();
    private final NilaiKandidatDAO nilaiKandidatDAO = new NilaiKandidatDAO();
    private final ProfilIdealDAO profilIdealDAO = new ProfilIdealDAO();

    private final JComboBox<Lowongan> cbLowongan = new JComboBox<>();
    private final DefaultTableModel rankModel;
    private final JTable rankTable;
    private final DefaultTableModel gapModel;
    private final JTable gapTable;
    private final JLabel lblInfo = new JLabel(" ");
    private final JLabel lblFormula = new JLabel(" ");
    private JScrollPane scrollGap;

    public LaporanHasilPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(new java.awt.Color(0xFD, 0xEA, 0xF1));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        add(buildToolbar(), BorderLayout.NORTH);

        // Tabel atas: ranking
        rankModel = new DefaultTableModel(RANK_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rankTable = new JTable(rankModel);
        rankTable.setRowHeight(28);
        rankTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rankTable.getTableHeader().setReorderingAllowed(false);
        rankTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        rankTable.getTableHeader().setBackground(new java.awt.Color(0xFC, 0xE4, 0xEC));
        rankTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Tabel bawah: detail perhitungan per kriteria
        gapModel = new DefaultTableModel(GAP_COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        gapTable = new JTable(gapModel);
        gapTable.setRowHeight(24);
        gapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gapTable.getTableHeader().setReorderingAllowed(false);
        gapTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        gapTable.getTableHeader().setBackground(new java.awt.Color(0xFC, 0xE4, 0xEC));
        gapTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JScrollPane scrollRank = new JScrollPane(rankTable);
        scrollRank.setBorder(BorderFactory.createTitledBorder("Ranking Kandidat"));
        scrollGap = new JScrollPane(gapTable);
        scrollGap.setBorder(BorderFactory.createTitledBorder("Detail Perhitungan (klik baris kandidat di atas)"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollRank, scrollGap);
        split.setDividerLocation(320);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // South: formula + info bar
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);

        JPanel formulaBox = new JPanel(new BorderLayout());
        formulaBox.setBackground(new java.awt.Color(0xFF, 0xF6, 0xFA));
        formulaBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(0xF8, 0xBB, 0xD0)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        lblFormula.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFormula.setVerticalAlignment(JLabel.TOP);
        formulaBox.add(lblFormula, BorderLayout.CENTER);
        south.add(formulaBox, BorderLayout.CENTER);

        south.add(lblInfo, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        // Listener: klik baris ranking → muat detail perhitungan
        rankTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRankSelected();
        });

        muatLowongan();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel lblTitle = new JLabel("Laporan Hasil Seleksi");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        toolbar.add(lblTitle, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(new JLabel("Lowongan:"));
        cbLowongan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbLowongan.addActionListener(e -> muatHasilUntukLowonganTerpilih());
        right.add(cbLowongan);

        JButton btnRefresh = new JButton("Refresh");
        ButtonStyle.primary(btnRefresh);
        btnRefresh.addActionListener(e -> {
            muatLowongan();
            muatHasilUntukLowonganTerpilih();
        });
        right.add(btnRefresh);

        JButton btnCetakPdf = new JButton("Cetak PDF");
        ButtonStyle.primary(btnCetakPdf);
        btnCetakPdf.addActionListener(e -> onCetakPdf());
        right.add(btnCetakPdf);

        toolbar.add(right, BorderLayout.EAST);
        return toolbar;
    }

    private void onCetakPdf() {
        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Pilih lowongan dulu sebelum cetak PDF.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<HasilAkhir> hasil = hasilAkhirDAO.getByLowongan(selected.getIdLowongan());
        if (hasil.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Belum ada hasil perhitungan untuk lowongan ini.\nJalankan Proses Perhitungan dulu.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String namaBersih = selected.getPosisi()
            .replaceAll("[^a-zA-Z0-9-_]", "_");
        String tgl = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String namaFileDefault = "Laporan_Hasil_" + namaBersih + "_" + tgl + ".pdf";

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Simpan Laporan PDF");
        fc.setSelectedFile(new File(namaFileDefault));
        fc.setFileFilter(new FileNameExtensionFilter("PDF (*.pdf)", "pdf"));
        int ret = fc.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getParentFile(), file.getName() + ".pdf");
        }
        if (file.exists()) {
            int ovw = JOptionPane.showConfirmDialog(this,
                "File sudah ada. Timpa?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (ovw != JOptionPane.YES_OPTION) return;
        }

        final File target = file;
        setEnabledAll(false);
        new Thread(() -> {
            try {
                long t0 = System.currentTimeMillis();
                new LaporanPdfExporter().export(target, selected);
                long ms = System.currentTimeMillis() - t0;
                SwingUtilities.invokeLater(() -> {
                    setEnabledAll(true);
                    int buka = JOptionPane.showConfirmDialog(this,
                        "PDF berhasil dibuat (" + ms + " ms):\n" + target.getAbsolutePath() +
                            "\n\nBuka sekarang?",
                        "Sukses", JOptionPane.YES_NO_OPTION);
                    if (buka == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                        try { Desktop.getDesktop().open(target); }
                        catch (Exception ex) { ex.printStackTrace(); }
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    setEnabledAll(true);
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                        "Gagal membuat PDF:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "pdf-export").start();
    }

    private void setEnabledAll(boolean enabled) {
        cbLowongan.setEnabled(enabled);
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JPanel) {
                for (java.awt.Component child : ((JPanel) c).getComponents()) {
                    if (child instanceof JButton) child.setEnabled(enabled);
                }
            }
        }
    }

    private void muatLowongan() {
        Lowongan selectedSebelumnya = (Lowongan) cbLowongan.getSelectedItem();
        cbLowongan.removeAllItems();
        List<Lowongan> daftar = lowonganDAO.getAll();
        for (Lowongan l : daftar) cbLowongan.addItem(l);
        if (selectedSebelumnya != null) {
            cbLowongan.setSelectedItem(selectedSebelumnya);
        } else if (!daftar.isEmpty()) {
            cbLowongan.setSelectedIndex(0);
        }
    }

    private void muatHasilUntukLowonganTerpilih() {
        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        rankModel.setRowCount(0);
        gapModel.setRowCount(0);
        lblFormula.setText(" ");
        updateGapBorder(null, 0);

        if (selected == null) {
            lblInfo.setText(" ");
            return;
        }

        List<HasilAkhir> hasil = hasilAkhirDAO.getByLowongan(selected.getIdLowongan());
        int kuota = selected.getKuota();
        int lulus = 0;
        for (HasilAkhir h : hasil) {
            String status = (h.getRanking() <= kuota) ? "LULUS" : "Belum Lulus";
            if (status.equals("LULUS")) lulus++;
            rankModel.addRow(new Object[]{
                h.getRanking(),
                h.getKandidat().getNisn(),
                h.getKandidat().getNama(),
                String.format("%.3f", h.getNcf()),
                String.format("%.3f", h.getNsf()),
                String.format("%.3f", h.getNilaiTotal()),
                status
            });
        }
        lblInfo.setText(
            "Lowongan: " + selected + " — Total diproses: " + hasil.size()
            + " | Kuota: " + kuota + " | Lulus: " + lulus
        );
    }

    private void onRankSelected() {
        gapModel.setRowCount(0);
        lblFormula.setText(" ");
        int viewRow = rankTable.getSelectedRow();
        if (viewRow < 0) return;

        Lowongan selected = (Lowongan) cbLowongan.getSelectedItem();
        if (selected == null) return;

        int rank = (int) rankModel.getValueAt(viewRow, 0);
        String nisn = (String) rankModel.getValueAt(viewRow, 1);

        List<HasilAkhir> hasil = hasilAkhirDAO.getByLowongan(selected.getIdLowongan());
        for (HasilAkhir h : hasil) {
            if (h.getRanking() == rank && h.getKandidat().getNisn().equals(nisn)) {
                tampilkanDetailPerhitungan(h, selected);
                break;
            }
        }
    }

    /** Isi tabel bawah dengan Nilai/Target/GAP/Bobot per kriteria + update label formula. */
    private void tampilkanDetailPerhitungan(HasilAkhir h, Lowongan selected) {
        int idKandidat = h.getKandidat().getIdKandidat();
        int idLowongan = selected.getIdLowongan();

        // Map nilai kandidat & target per kriteria (lookup cepat)
        Map<Integer, Double> mapNilai = new HashMap<>();
        for (NilaiKandidat n : nilaiKandidatDAO.getByKandidat(idKandidat)) {
            mapNilai.put(n.getKriteria().getIdKriteria(), n.getNilaiKandidat());
        }
        Map<Integer, Double> mapTarget = new HashMap<>();
        for (ProfilIdeal p : profilIdealDAO.getByLowongan(idLowongan)) {
            mapTarget.put(p.getKriteria().getIdKriteria(), p.getNilaiTarget());
        }

        List<HasilGap> gaps = hasilGapDAO.getByKandidatDanLowongan(idKandidat, idLowongan);

        // Bangun string formula sekaligus isi tabel
        StringBuilder sbCF = new StringBuilder("(");
        StringBuilder sbSF = new StringBuilder("(");
        int countCF = 0, countSF = 0;

        for (HasilGap g : gaps) {
            int idK = g.getKriteria().getIdKriteria();
            double nilai = mapNilai.getOrDefault(idK, 0.0);
            double target = mapTarget.getOrDefault(idK, 0.0);
            double bobot = g.getBobotNilai();

            gapModel.addRow(new Object[]{
                g.getKriteria().getKodeKriteria(),
                g.getKriteria().getNamaKriteria(),
                g.getKriteria().getJenisFaktor(),
                String.format("%.2f", nilai),
                String.format("%.2f", target),
                g.getNilaiGap(),
                String.format("%.2f", bobot)
            });

            if (g.getKriteria().isCoreFactor()) {
                if (countCF > 0) sbCF.append(" + ");
                sbCF.append(String.format("%.2f", bobot));
                countCF++;
            } else {
                if (countSF > 0) sbSF.append(" + ");
                sbSF.append(String.format("%.2f", bobot));
                countSF++;
            }
        }

        sbCF.append(") / ").append(countCF).append(" = ").append(String.format("%.3f", h.getNcf()));
        sbSF.append(") / ").append(countSF).append(" = ").append(String.format("%.3f", h.getNsf()));

        // Update border title tabel bawah dengan info kandidat
        updateGapBorder(h.getKandidat().getNama(), h.getRanking());

        // Hitung ulang nilai akhir untuk demonstrasi formula
        double ncf = h.getNcf();
        double nsf = h.getNsf();
        double total = h.getNilaiTotal();
        boolean lulus = h.getRanking() <= selected.getKuota();

        String html = String.format(
            "<html>" +
            "<b>NCF</b> (Core Factor) = %s<br>" +
            "<b>NSF</b> (Secondary Factor) = %s<br>" +
            "<b>Nilai Akhir</b> = 60%% &times; NCF + 40%% &times; NSF<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= 60%% &times; %.3f + 40%% &times; %.3f<br>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;= %.3f + %.3f = <b>%.3f</b><br>" +
            "<b>Ranking</b>: #%d &nbsp;<b>%s</b>" +
            "</html>",
            sbCF.toString(),
            sbSF.toString(),
            ncf, nsf,
            ncf * 0.6, nsf * 0.4, total,
            h.getRanking(),
            lulus ? "LULUS" : "Belum Lulus"
        );
        lblFormula.setText(html);
    }

    private void updateGapBorder(String infoKandidat, int rank) {
        String title = (infoKandidat == null)
            ? "Detail Perhitungan (klik baris kandidat di atas)"
            : "Detail Perhitungan: " + infoKandidat + " — Rank #" + rank;
        javax.swing.border.Border current = scrollGap.getBorder();
        if (current instanceof javax.swing.border.TitledBorder) {
            ((javax.swing.border.TitledBorder) current).setTitle(title);
            scrollGap.repaint();
        }
    }
}
