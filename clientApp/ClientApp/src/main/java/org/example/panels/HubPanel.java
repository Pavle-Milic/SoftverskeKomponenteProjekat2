package org.example.panels;

import org.example.ClientApp;
import org.example.utils.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HubPanel extends JPanel {
    // Labela i dugmići kojima moramo pristupati kasnije (zbog updateInfo)
    private JLabel lblUser = new JLabel();
    private JLabel lblRank = new JLabel();

    // Admin dugmići (inicijalno ih pravimo, ali ćemo im menjati vidljivost)
    private JButton btnAdminUsers = new JButton("ADMIN: Lista Korisnika");
    private JButton btnAddGame = new JButton("ADMIN: Dodaj Igru");

    public HubPanel(ClientApp app) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);

        // --- 1. Header Panel (Korisnički info) ---
        JPanel header = new JPanel(new GridLayout(3, 1));
        header.setBackground(Theme.SECONDARY);
        header.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Dobrodošli nazad");
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Theme.WHITE);

        lblUser.setFont(Theme.SUBHEADER_FONT);
        lblUser.setForeground(Color.LIGHT_GRAY);

        lblRank.setFont(Theme.SUBHEADER_FONT);
        lblRank.setForeground(new Color(241, 196, 15)); // Zlatna boja za rank

        header.add(title);
        header.add(lblUser);
        header.add(lblRank);
        add(header, BorderLayout.NORTH);

        // --- 2. Center Panel (Dugmići) ---
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Razmak između dugmića
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Dimenzija za sva dugmad
        Dimension btnDim = new Dimension(300, 50);

        // A) Dugme: GEJMING SESIJE (Glavna funkcionalnost)
        JButton btnSessions = new JButton("Gejming Sesije");
        Theme.styleButton(btnSessions);
        btnSessions.setBackground(new Color(52, 152, 219)); // Plava
        btnSessions.setPreferredSize(btnDim);
        btnSessions.addActionListener(e -> app.navigateTo("SESSION_LIST"));

        gbc.gridy = 0;
        center.add(btnSessions, gbc);

        // B) Dugme: AŽURIRAJ PROFIL
        JButton btnUpdate = new JButton("Ažuriraj Profil");
        Theme.styleButton(btnUpdate);
        btnUpdate.setPreferredSize(btnDim);
        btnUpdate.addActionListener(e -> app.navigateTo("UPDATE"));

        gbc.gridy = 1;
        center.add(btnUpdate, gbc);

        // C) Dugme: ADMIN - DODAJ IGRU
        Theme.styleButton(btnAddGame);
        btnAddGame.setBackground(new Color(230, 126, 34)); // Narandžasta
        btnAddGame.setPreferredSize(btnDim);
        btnAddGame.addActionListener(e -> app.navigateTo("ADD_GAME"));

        gbc.gridy = 2;
        center.add(btnAddGame, gbc);

        // D) Dugme: ADMIN - LISTA KORISNIKA
        Theme.styleButton(btnAdminUsers);
        btnAdminUsers.setBackground(new Color(211, 84, 0)); // Tamnija narandžasta
        btnAdminUsers.setPreferredSize(btnDim);
        btnAdminUsers.addActionListener(e -> app.navigateTo("ADMIN_LIST"));

        gbc.gridy = 3;
        center.add(btnAdminUsers, gbc);

        add(center, BorderLayout.CENTER);

        // --- 3. Footer (Logout) ---
        JPanel footer = new JPanel();
        footer.setBackground(Theme.BG_COLOR);
        footer.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton btnLogout = new JButton("Odjavi se");
        Theme.styleDangerButton(btnLogout);
        btnLogout.setPreferredSize(new Dimension(150, 40));
        btnLogout.addActionListener(e -> app.logout());

        footer.add(btnLogout);
        add(footer, BorderLayout.SOUTH);
    }

    /**
     * Poziva se nakon logina da osveži podatke i sakrije/prikaže admin dugmad.
     */
    public void updateInfo(String username, String rank, String role) {
        lblUser.setText("Korisnik: " + username);
        lblRank.setText("Rank: " + rank);

        boolean isAdmin = "ROLE_ADMIN".equals(role);

        // Prikazujemo admin dugmiće samo ako je korisnik admin
        btnAdminUsers.setVisible(isAdmin);
        btnAddGame.setVisible(isAdmin);
    }
}