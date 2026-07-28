package com.bkk.spk.view.util;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

/**
 * Cell renderer zebra-striping (baris genap/ganjil beda warna) supaya tabel
 * lebih mudah dibaca baris-per-baris. Satu instance per kolom, masing-masing
 * dengan alignment sendiri (LEFT / CENTER / RIGHT).
 *
 * Dipakai untuk isi cell (bukan header). Untuk header pakai {@link #applyHeaderAlign}.
 */
public class ZebraTableRenderer extends DefaultTableCellRenderer {

    public static final Color EVEN_BG = Color.WHITE;
    public static final Color ODD_BG  = new Color(0xFF, 0xF5, 0xF9); // pink super pucat

    private final int alignment;

    public ZebraTableRenderer(int alignment) {
        this.alignment = alignment;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        setHorizontalAlignment(alignment);
        // Row di renderer = VIEW row (post-sort), jadi zebra mengikuti urutan yang user lihat.
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? EVEN_BG : ODD_BG);
        }
        return c;
    }

    /**
     * Pasang alignment per kolom HEADER (pink pastel, bold), tanpa merusak
     * styling LAF secara keseluruhan. Dipanggil sekali setelah tabel dibangun.
     *
     * @param table       target tabel
     * @param alignments  array sepanjang jumlah kolom, isi SwingConstants.LEFT/CENTER/RIGHT.
     *                    Index mengikuti index kolom di TableColumnModel (urutan tampil).
     */
    public static void applyHeaderAlign(JTable table, int[] alignments) {
        JTableHeader header = table.getTableHeader();
        Font headerFont = header.getFont().deriveFont(Font.BOLD);
        Color headerBg = new Color(0xFC, 0xE4, 0xEC); // PINK_TINT
        TableColumnModel cm = table.getColumnModel();

        for (int i = 0; i < cm.getColumnCount() && i < alignments.length; i++) {
            final int align = alignments[i];
            TableColumn col = cm.getColumn(i);
            TableCellRenderer current = col.getHeaderRenderer();
            // Hindari overwrite kalau sudah pernah dipasang renderer alignment kita
            if (current instanceof AlignHeaderRenderer) {
                ((AlignHeaderRenderer) current).setAlignment(align);
                continue;
            }
            AlignHeaderRenderer r = new AlignHeaderRenderer(align, headerFont, headerBg);
            col.setHeaderRenderer(r);
        }
    }

    /**
     * One-shot styling: zebra striping + alignment per kolom (isi + header)
     * + grid pink tipis + font header bold. Cukup panggil sekali setelah tabel
     * dan semua kolom selesai dibentuk (termasuk operasi hide-ID).
     *
     * @param table     target tabel
     * @param colAlign  alignment per index kolom di TableColumnModel.
     */
    public static void apply(JTable table, int[] colAlign) {
        table.setShowGrid(true);
        table.setGridColor(new Color(0xF8, 0xBB, 0xD0));
        table.setIntercellSpacing(new java.awt.Dimension(1, 1));

        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount() && i < colAlign.length; i++) {
            cm.getColumn(i).setCellRenderer(new ZebraTableRenderer(colAlign[i]));
        }
        applyHeaderAlign(table, colAlign);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setBackground(new Color(0xFC, 0xE4, 0xEC));
    }

    /** Header renderer minimal: pink pastel + bold + alignment per kolom. */
    private static class AlignHeaderRenderer extends DefaultTableCellRenderer {
        private final Font font;
        private final Color bg;
        private int alignment;

        AlignHeaderRenderer(int alignment, Font font, Color bg) {
            this.alignment = alignment;
            this.font = font;
            this.bg = bg;
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        }

        void setAlignment(int a) { this.alignment = a; }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(alignment);
            setFont(font);
            setBackground(bg);
            setForeground(new Color(60, 60, 60));
            return c;
        }
    }
}
