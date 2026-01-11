package org.example.panels;

import org.example.ClientApp;
import org.example.utils.Theme;
import javax.swing.*;
import java.awt.*;

public class StartPanel extends JPanel {
    public StartPanel(ClientApp app) {
        setBackground(Theme.BG_COLOR);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("Pro Gamer App");
        title.setFont(new Font("Segoe UI", Font.BOLD, 40));
        title.setForeground(Theme.PRIMARY);

        JButton btnLogin = new JButton("Prijavi se");
        Theme.styleButton(btnLogin);
        btnLogin.setPreferredSize(new Dimension(200, 50));

        JButton btnRegister = new JButton("Registracija");
        Theme.styleButton(btnRegister);
        btnRegister.setBackground(Theme.SECONDARY); // Drugačija boja
        btnRegister.setPreferredSize(new Dimension(200, 50));

        btnLogin.addActionListener(e -> app.navigateTo("LOGIN"));
        btnRegister.addActionListener(e -> app.navigateTo("REGISTER"));

        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 40, 0);
        add(title, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 10, 0);
        add(btnLogin, gbc);

        gbc.gridy = 2;
        add(btnRegister, gbc);
    }
}