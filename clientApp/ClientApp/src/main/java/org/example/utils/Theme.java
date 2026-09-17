package org.example.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Theme {
    // Boje
    public static final Color PRIMARY = new Color(41, 128, 185);
    public static final Color SECONDARY = new Color(52, 73, 94);
    public static final Color SUCCESS = new Color(39, 174, 96);
    public static final Color DANGER = new Color(192, 57, 43); // Malo tamnija crvena
    public static final Color BG_COLOR = new Color(236, 240, 241);
    public static final Color TEXT_DARK = new Color(44, 62, 80);
    public static final Color WHITE = Color.WHITE;

    // Fontovi
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void styleButton(JButton btn) { baseButtonStyle(btn, PRIMARY); }
    public static void styleSecondaryButton(JButton btn) { baseButtonStyle(btn, SECONDARY); }
    public static void styleDangerButton(JButton btn) { baseButtonStyle(btn, DANGER); }
    public static void styleSuccessButton(JButton btn) { baseButtonStyle(btn, SUCCESS); }

    private static void baseButtonStyle(JButton btn, Color bg) {
        btn.setFont(SUBHEADER_FONT);
        btn.setBackground(bg);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTextField(JTextField tf) {
        tf.setFont(REGULAR_FONT);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(WHITE);
        tf.setCaretColor(TEXT_DARK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
    }

    // --- POPRAVLJENA METODA ZA TABELU ---
    public static void styleTable(JTable table) {
        table.setFont(REGULAR_FONT);
        table.setRowHeight(35); // Malo visi redovi
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setSelectionForeground(TEXT_DARK);

        JTableHeader header = table.getTableHeader();
        header.setFont(SUBHEADER_FONT);
        header.setReorderingAllowed(false);

        // KREIRAMO CUSTOM RENDERER ZA HEADER DA BI FORSIRALI BOJE
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(SECONDARY);
        headerRenderer.setForeground(WHITE);
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        headerRenderer.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); // Padding u headeru

        // Primenjujemo renderer na svaku kolonu
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }
}