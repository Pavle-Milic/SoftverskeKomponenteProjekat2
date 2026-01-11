package org.example.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Theme {
    public static final Color PRIMARY = new Color(41, 128, 185);    // Plava
    public static final Color SECONDARY = new Color(52, 73, 94);    // Tamno siva
    public static final Color BG_COLOR = new Color(245, 245, 245);  // Svetlo siva
    public static final Color TEXT_DARK = new Color(33, 33, 33);
    public static final Color WHITE = Color.WHITE;
    public static final Color DANGER = new Color(231, 76, 60);      // Crvena

    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public static void styleButton(JButton btn) {
        btn.setFont(SUBHEADER_FONT);
        btn.setBackground(PRIMARY);
        btn.setForeground(WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleSecondaryButton(JButton btn) {
        styleButton(btn);
        btn.setBackground(SECONDARY);
    }

    public static void styleDangerButton(JButton btn) {
        styleButton(btn);
        btn.setBackground(DANGER);
    }

    public static void styleTextField(JTextField tf) {
        tf.setFont(REGULAR_FONT);
        tf.setForeground(TEXT_DARK);
        tf.setBackground(WHITE);
        tf.setCaretColor(TEXT_DARK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }
}