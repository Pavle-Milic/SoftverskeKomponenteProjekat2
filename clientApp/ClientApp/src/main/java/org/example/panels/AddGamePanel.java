package org.example.panels;

import org.example.ClientApp;
import org.example.model.GameDto;

import javax.swing.*;
import java.awt.*;

public class AddGamePanel extends JPanel {
    private ClientApp app;
    private JTextField tfName = new JTextField(20);
    private JTextField tfGenre = new JTextField(20);
    private JTextArea taDescription = new JTextArea(5, 20);

    public AddGamePanel(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        formPanel.add(new JLabel("Naziv igre:"));
        formPanel.add(tfName);
        formPanel.add(new JLabel("Žanr:"));
        formPanel.add(tfGenre);
        formPanel.add(new JLabel("Opis:"));
        formPanel.add(new JScrollPane(taDescription));

        JButton btnAdd = new JButton("Dodaj Igru");
        JButton btnBack = new JButton("Nazad");

        btnAdd.addActionListener(e -> {
            GameDto dto = new GameDto();
            dto.name = tfName.getText();
            dto.genre = tfGenre.getText();
            dto.description = taDescription.getText();

            if(app.createGame(dto)) {
                JOptionPane.showMessageDialog(this, "Igra uspešno dodata!");
                tfName.setText(""); tfGenre.setText(""); taDescription.setText("");
                app.navigateTo("HUB");
            }
        });

        btnBack.addActionListener(e -> app.navigateTo("HUB"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnBack);
        btnPanel.add(btnAdd);

        add(new JLabel("NOVA IGRA", SwingConstants.CENTER), BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }
}