package org.example.panels;

import org.example.ClientApp;
import org.example.model.RegisterDto;
import org.example.utils.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;

public class RegisterPanel extends JPanel {
    private JTextField tfEmail = new JTextField();
    private JTextField tfUsername = new JTextField();
    private JPasswordField tfPassword = new JPasswordField();
    private JTextField tfName = new JTextField();
    private JTextField tfSurname = new JTextField();
    private JTextField tfBirth = new JTextField(); // Sklonjen fiksni tekst iz konstruktora

    public RegisterPanel(ClientApp app) {
        setBackground(Theme.BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel lblTitle = new JLabel("Registracija", SwingConstants.CENTER);
        lblTitle.setFont(Theme.HEADER_FONT);
        lblTitle.setForeground(Theme.PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 40, 0)); // 40px razmaka ispod naslova
        add(lblTitle, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBackground(Theme.BG_COLOR);

        styleRow(form, "Email:", tfEmail);
        styleRow(form, "Korisničko ime:", tfUsername);
        styleRow(form, "Šifra:", tfPassword);
        styleRow(form, "Ime:", tfName);
        styleRow(form, "Prezime:", tfSurname);

        // Transparentni template
        setupPlaceholder(tfBirth, "YYYY-MM-DD");
        styleRow(form, "Datum rođenja:", tfBirth);

        add(form, BorderLayout.CENTER);

        // Dugmad
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttons.setBackground(Theme.BG_COLOR);
        buttons.setBorder(new EmptyBorder(20, 0, 0, 0)); // Razmak iznad dugmadi

        JButton btnRegister = new JButton("Registruj se");
        Theme.styleButton(btnRegister);

        JButton btnBack = new JButton("Nazad");
        Theme.styleButton(btnBack);
        btnBack.setBackground(Theme.SECONDARY);

        btnRegister.addActionListener(e -> {
            try {
                RegisterDto dto = new RegisterDto();
                dto.email = tfEmail.getText();
                dto.username = tfUsername.getText();
                dto.password = new String(tfPassword.getPassword());
                dto.firstName = tfName.getText();
                dto.lastName = tfSurname.getText();
                dto.birthDate = LocalDate.parse(tfBirth.getText());

                app.register(dto);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Neispravan format datuma (YYYY-MM-DD)!");
            }
        });

        btnBack.addActionListener(e -> app.navigateTo("START"));

        buttons.add(btnBack);
        buttons.add(btnRegister);
        add(buttons, BorderLayout.SOUTH);
    }

    private void setupPlaceholder(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });
    }

    private void styleRow(JPanel panel, String labelText, JTextField textField) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.REGULAR_FONT);
        Theme.styleTextField(textField);
        panel.add(label);
        panel.add(textField);
    }
}