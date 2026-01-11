package org.example.panels;

import org.example.ClientApp;
import org.example.utils.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {
    public LoginPanel(ClientApp app) {
        setBackground(Theme.BG_COLOR);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel lblTitle = new JLabel("Prijavljivanje");
        lblTitle.setFont(Theme.HEADER_FONT);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField tfEmail = new JTextField("admin@gmail.com");
        Theme.styleTextField(tfEmail);

        JPasswordField tfPassword = new JPasswordField("admin");
        Theme.styleTextField(tfPassword);

        JButton btnLogin = new JButton("Potvrdi");
        Theme.styleButton(btnLogin);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnBack = new JButton("Nazad");
        Theme.styleButton(btnBack);
        btnBack.setBackground(Theme.SECONDARY);
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Akcije
        btnLogin.addActionListener(e -> app.login(tfEmail.getText(), new String(tfPassword.getPassword())));
        btnBack.addActionListener(e -> app.navigateTo("START"));

        // Dodavanje komponenti sa razmacima
        add(lblTitle);
        add(Box.createVerticalStrut(30));
        add(new JLabel("Email:"));
        add(tfEmail);
        add(Box.createVerticalStrut(10));
        add(new JLabel("Šifra:"));
        add(tfPassword);
        add(Box.createVerticalStrut(20));
        add(btnLogin);
        add(Box.createVerticalStrut(10));
        add(btnBack);
    }
}