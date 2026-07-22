package com.bkk.spk.service;

import com.bkk.spk.dao.HasilAkhirDAO;
import com.bkk.spk.model.Admin;
import com.bkk.spk.model.HasilAkhir;
import com.bkk.spk.model.Lowongan;
import com.bkk.spk.util.Session;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Cetak laporan hasil seleksi (Profile Matching) ke PDF memakai PDFBox 2.0.x.
 *
 * Struktur PDF (per lowongan terpilih):
 *   1. Header / cover — judul + info lowongan + tanggal cetak
 *   2. Hasil Ranking <nama PT> — Peringkat, Kode, Nama Kandidat, Nilai Akhir, Status
 *   3. Footer — kota+tanggal + nama admin
 */
public class LaporanPdfExporter {

    private static final PDRectangle PAGE = PDRectangle.A4;
    private static final float PAGE_W = PAGE.getWidth();
    private static final float PAGE_H = PAGE.getHeight();
    private static final float MARGIN_X = 40f;
    private static final float MARGIN_TOP = 56f;
    private static final float MARGIN_BOTTOM = 50f;

    private static final PDFont F_BOLD = PDType1Font.HELVETICA_BOLD;
    private static final PDFont F_REG = PDType1Font.HELVETICA;
    private static final PDFont F_ITALIC = PDType1Font.HELVETICA_OBLIQUE;

    private static final Color PINK_DARK = new Color(0xC2, 0x1F, 0x4A);
    private static final Color PINK = new Color(0xF4, 0x8F, 0xB1);
    private static final Color PINK_LIGHT = new Color(0xFC, 0xE4, 0xEC);
    private static final Color GREY_TEXT = new Color(0x55, 0x55, 0x55);
    private static final Color LINE = new Color(0xCC, 0xCC, 0xCC);

    private final HasilAkhirDAO hasilAkhirDAO = new HasilAkhirDAO();

    public File export(File outFile, Lowongan lowongan) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            Ctx ctx = new Ctx(doc);
            drawHeader(ctx, lowongan);
            sectionHasilRanking(ctx, lowongan);
            drawSignature(ctx);

            if (ctx.cs != null) ctx.cs.close();
            if (ctx.page != null) drawPageNumber(ctx);
            ctx.cs = null;
            ctx.page = null;

            doc.save(outFile);
            return outFile;
        }
    }

    // ===== Header / cover =====
    private void drawHeader(Ctx ctx, Lowongan lowongan) throws IOException {
        ctx.newPage();
        float y = PAGE_H - 60;
        float cx = PAGE_W / 2f;

        ctx.cs.setNonStrokingColor(PINK_DARK);
        ctx.cs.addRect(0, PAGE_H - 8, PAGE_W, 8);
        ctx.cs.fill();

        ctx.textCentered(F_BOLD, 18, PINK_DARK, cx, y, "SMK Widya Nusantara");
        y -= 22;
        ctx.textCentered(F_REG, 11, GREY_TEXT, cx, y,
            "Bursa Kerja Khusus (BKK) — Sistem Pendukung Keputusan");
        y -= 16;
        ctx.textCentered(F_REG, 11, GREY_TEXT, cx, y,
            "Metode Profile Matching");
        y -= 22;

        ctx.cs.setStrokingColor(PINK);
        ctx.cs.setLineWidth(1.5f);
        ctx.cs.moveTo(MARGIN_X, y);
        ctx.cs.lineTo(PAGE_W - MARGIN_X, y);
        ctx.cs.stroke();
        y -= 28;

        String namaPerusahaan = (lowongan.getPerusahaan() != null)
            ? lowongan.getPerusahaan().getNamaPerusahaan() : "-";

        ctx.textCentered(F_BOLD, 18, PINK_DARK, cx, y,
            "HASIL RANKING " + namaPerusahaan.toUpperCase());
        y -= 22;
        ctx.textCentered(F_REG, 12, GREY_TEXT, cx, y,
            "Penempatan Kerja Lulusan");
        y -= 30;

        float boxW = PAGE_W - 2 * MARGIN_X;
        float boxH = 92;
        ctx.cs.setNonStrokingColor(PINK_LIGHT);
        ctx.cs.addRect(MARGIN_X, y - boxH, boxW, boxH);
        ctx.cs.fill();
        ctx.cs.setStrokingColor(PINK);
        ctx.cs.setLineWidth(0.8f);
        ctx.cs.addRect(MARGIN_X, y - boxH, boxW, boxH);
        ctx.cs.stroke();

        float rowY = y - 22;
        ctx.textCentered(F_BOLD, 11, PINK_DARK, cx, rowY,
            "Lowongan   :   " + safe(lowongan.getPosisi())); rowY -= 18;
        ctx.textCentered(F_BOLD, 11, PINK_DARK, cx, rowY,
            "Perusahaan   :   " + namaPerusahaan); rowY -= 18;
        ctx.textCentered(F_REG, 10, GREY_TEXT, cx, rowY,
            "Kuota   :   " + lowongan.getKuota()
                + " orang      •      Status   :   " + safe(lowongan.getStatus()));

        ctx.y = y - boxH - 18;
    }

    // ===== Section: Hasil Ranking =====
    private void sectionHasilRanking(Ctx ctx, Lowongan lowongan) throws IOException {
        String namaPerusahaan = (lowongan.getPerusahaan() != null)
            ? safe(lowongan.getPerusahaan().getNamaPerusahaan()) : "-";
        ctx.sectionTitle("Hasil Ranking " + namaPerusahaan);
        ctx.spaced(6);

        List<HasilAkhir> hasil = hasilAkhirDAO.getByLowongan(lowongan.getIdLowongan());
        int kuota = lowongan.getKuota();

        String[] headers = {"Peringkat", "Kode", "Nama Kandidat", "Nilai Akhir", "Status"};
        float[] widths = {80, 80, 240, 110, 90};

        drawTable(ctx, headers, widths, hasil.size(), (i, c) -> {
            HasilAkhir h = hasil.get(i);
            String status = (h.getRanking() <= kuota) ? "LULUS" : "Belum Lulus";
            return new String[]{
                String.valueOf(h.getRanking()),
                safe(h.getKandidat().getNisn()),
                truncate(safe(h.getKandidat().getNama()), 32),
                String.format("%.3f", h.getNilaiTotal()),
                status
            };
        });

        ctx.spaced(10);
        int lulus = 0;
        for (HasilAkhir h : hasil) if (h.getRanking() <= kuota) lulus++;
        ctx.textCentered(F_BOLD, 10, GREY_TEXT, PAGE_W / 2, ctx.y,
            "Total diproses: " + hasil.size()
                + "   •   Kuota: " + kuota
                + "   •   Lulus: " + lulus
                + "   •   Belum Lulus: " + Math.max(0, hasil.size() - lulus));
        ctx.spaced(18);
    }

    // ===== Signature =====
    private void drawSignature(Ctx ctx) throws IOException {
        ctx.ensureSpace(120);
        ctx.spaced(30);

        Admin admin = Session.getCurrentAdmin();
        String namaAdmin = (admin != null) ? safe(admin.getNama()) : "Admin";
        String userAdmin = (admin != null) ? "@" + safe(admin.getUsername()) : "@admin";
        String kotaTanggal = "Cianjur, " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        float x = PAGE_W - MARGIN_X - 180;
        ctx.text(F_REG, 11, GREY_TEXT, x, ctx.y, kotaTanggal);
        ctx.y -= 18;
        ctx.text(F_REG, 11, GREY_TEXT, x, ctx.y, "Admin BKK,");
        ctx.y -= 56;
        ctx.text(F_BOLD, 11, PINK_DARK, x, ctx.y, namaAdmin);
        ctx.y -= 14;
        ctx.text(F_ITALIC, 9, GREY_TEXT, x, ctx.y, userAdmin);
    }

    // ===== Generic table renderer =====
    private interface RowSource {
        String[] row(int i, int colCount);
    }

    private void drawTable(Ctx ctx, String[] headers, float[] widths,
                           int rowCount, RowSource src) throws IOException {
        int colCount = headers.length;
        float avail = PAGE_W - 2 * MARGIN_X;

        float totalW = 0;
        for (float w : widths) totalW += w;
        if (totalW > avail) {
            float scale = avail / totalW;
            for (int i = 0; i < widths.length; i++) widths[i] *= scale;
            totalW = avail;
        }
        float x0 = MARGIN_X + (avail - totalW) / 2f;
        final float contentW = totalW;

        float rowH = 18f;
        float headerH = 22f;

        ctx.ensureSpace(headerH + rowH);
        float y = ctx.y;

        drawTableHeader(ctx, headers, widths, x0, contentW, headerH);
        y -= headerH;

        for (int r = 0; r < rowCount; r++) {
            if (y - rowH < MARGIN_BOTTOM) {
                ctx.y = y;
                ctx.newPage();
                y = ctx.y;
                drawTableHeader(ctx, headers, widths, x0, contentW, headerH);
                y -= headerH;
            }

            String[] row = src.row(r, colCount);

            // Zebra
            if (r % 2 == 1) {
                ctx.cs.setNonStrokingColor(new Color(0xFA, 0xF6, 0xF8));
                ctx.cs.addRect(x0, y - rowH, contentW, rowH);
                ctx.cs.fill();
            }

            float x = x0;
            for (int c = 0; c < colCount; c++) {
                String cellText = (c < row.length && row[c] != null) ? row[c] : "";
                cellText = truncateToWidth(cellText, widths[c] - 6, F_REG, 9f);
                float tw = getStringWidth(F_REG, 9f, cellText);
                float cellX = x + (widths[c] - tw) / 2f;
                cellX = Math.max(cellX, x + 2);
                ctx.text(F_REG, 9f, Color.BLACK, cellX, y - rowH + 5.5f, cellText);
                x += widths[c];
            }

            ctx.cs.setStrokingColor(LINE);
            ctx.cs.setLineWidth(0.4f);
            ctx.cs.moveTo(x0, y - rowH);
            ctx.cs.lineTo(x0 + contentW, y - rowH);
            ctx.cs.stroke();

            y -= rowH;
        }

        ctx.cs.setStrokingColor(PINK);
        ctx.cs.setLineWidth(0.8f);
        ctx.cs.addRect(x0, y, contentW, ctx.y - y);
        ctx.cs.stroke();

        ctx.y = y - 4;
    }

    private void drawTableHeader(Ctx ctx, String[] headers, float[] widths,
                                 float x0, float contentW, float headerH) throws IOException {
        ctx.cs.setNonStrokingColor(PINK_LIGHT);
        ctx.cs.addRect(x0, ctx.y - headerH, contentW, headerH);
        ctx.cs.fill();
        ctx.cs.setStrokingColor(PINK);
        ctx.cs.setLineWidth(0.6f);
        ctx.cs.addRect(x0, ctx.y - headerH, contentW, headerH);
        ctx.cs.stroke();

        float x = x0;
        for (int c = 0; c < headers.length; c++) {
            String h = headers[c];
            float fs = 9.5f;
            float tw = getStringWidth(F_BOLD, fs, h);
            float cellX = x + (widths[c] - tw) / 2f;
            cellX = Math.max(cellX, x + 3);
            ctx.text(F_BOLD, fs, PINK_DARK, cellX, ctx.y - headerH + 7, h);
            x += widths[c];
        }
    }

    // ===== Helpers =====
    private static float getStringWidth(PDFont font, float fontSize, String text) {
        try {
            return font.getStringWidth(text) / 1000f * fontSize;
        } catch (IOException e) {
            return text.length() * fontSize * 0.5f;
        }
    }

    private static String truncateToWidth(String text, float maxWidth, PDFont font, float fontSize) {
        if (text == null) return "";
        try {
            float w = font.getStringWidth(text) / 1000f * fontSize;
            if (w <= maxWidth) return text;
            String s = text;
            while (s.length() > 1) {
                s = s.substring(0, s.length() - 1);
                if (font.getStringWidth(s + "...") / 1000f * fontSize <= maxWidth) {
                    return s + "...";
                }
            }
            return s;
        } catch (IOException e) {
            return text;
        }
    }

    private static String safe(String s) { return s == null ? "" : s; }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    // ===== Context wrapper =====
    private static class Ctx {
        final PDDocument doc;
        PDPage page;
        PDPageContentStream cs;
        float y;

        Ctx(PDDocument doc) { this.doc = doc; }

        void newPage() throws IOException {
            if (cs != null) cs.close();
            if (page != null) drawPageNumber(this);
            page = new PDPage(PAGE);
            doc.addPage(page);
            cs = new PDPageContentStream(doc, page);
            y = PAGE_H - MARGIN_TOP;
        }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN_BOTTOM) newPage();
        }

        void spaced(float dy) throws IOException {
            y -= dy;
            if (y < MARGIN_BOTTOM) newPage();
        }

        void sectionTitle(String title) throws IOException {
            ensureSpace(40);
            float tw = getStringWidth(F_BOLD, 13, title);
            float cx = PAGE_W / 2f;
            float gap = 12;
            cs.setStrokingColor(PINK);
            cs.setLineWidth(1f);
            cs.moveTo(MARGIN_X, y - 7);
            cs.lineTo(cx - tw / 2f - gap, y - 7);
            cs.moveTo(cx + tw / 2f + gap, y - 7);
            cs.lineTo(PAGE_W - MARGIN_X, y - 7);
            cs.stroke();
            textCentered(F_BOLD, 13, PINK_DARK, cx, y - 12, title);
            y -= 22;
        }

        void text(PDFont font, float size, Color color,
                  float x, float yPos, String s) throws IOException {
            if (s == null) s = "";
            cs.beginText();
            cs.setFont(font, size);
            cs.setNonStrokingColor(color);
            cs.newLineAtOffset(x, yPos);
            cs.showText(s);
            cs.endText();
        }

        void textCentered(PDFont font, float size, Color color,
                          float cx, float yPos, String s) throws IOException {
            if (s == null) s = "";
            float w = getStringWidth(font, size, s);
            text(font, size, color, cx - w / 2f, yPos, s);
        }
    }

    private static void drawPageNumber(Ctx ctx) {
        try {
            PDPageContentStream f = new PDPageContentStream(
                ctx.doc, ctx.page, PDPageContentStream.AppendMode.APPEND, true, true);
            int pageNumber = ctx.doc.getNumberOfPages();
            String s = "Halaman " + pageNumber;
            float w = getStringWidth(F_REG, 9, s);
            f.beginText();
            f.setFont(F_REG, 9);
            f.setNonStrokingColor(GREY_TEXT);
            f.newLineAtOffset((PAGE_W - w) / 2f, 28);
            f.showText(s);
            f.endText();
            f.close();
        } catch (IOException ignored) { /* best-effort */ }
    }
}
