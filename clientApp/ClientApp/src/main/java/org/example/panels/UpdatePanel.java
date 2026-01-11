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

public class UpdatePanel extends JPanel {
    private JTextField tfEmail = new JTextField();
    private JTextField tfUsername = new JTextField();
    private JPasswordField tfPassword = new JPasswordField();
    private JTextField tfName = new JTextField();
    private JTextField tfSurname = new JTextField();
    private JTextField tfBirth = new JTextField();

    public UpdatePanel(ClientApp app) {
        setBackground(Theme.BG_COLOR);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 50, 30, 50));

        // Naslov sa razmakom ispod (Border)
        JLabel lblTitle = new JLabel("Ažuriraj Profil", SwingConstants.CENTER);
        lblTitle.setFont(Theme.HEADER_FONT);
        lblTitle.setForeground(Theme.PRIMARY);
        lblTitle.setBorder(new EmptyBorder(0, 0, 40, 0)); // Razmak od 40px ispod naslova
        add(lblTitle, BorderLayout.NORTH);

        // Forma
        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setBackground(Theme.BG_COLOR);

        addFormField(form, "Novi Email:", tfEmail);
        addFormField(form, "Novo Korisničko ime:", tfUsername);
        addFormField(form, "Nova Šifra:", tfPassword);
        addFormField(form, "Ime:", tfName);
        addFormField(form, "Prezime:", tfSurname);

        // Dodavanje prompta/template-a za datum
        setupPlaceholder(tfBirth, "YYYY-MM-DD");
        addFormField(form, "Datum rođenja:", tfBirth);

        add(form, BorderLayout.CENTER);

        // Dugmad sa razmakom iznad
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttons.setBackground(Theme.BG_COLOR);
        buttons.setBorder(new EmptyBorder(20, 0, 0, 0)); // Razmak iznad dugmića

        JButton btnSave = new JButton("Sačuvaj izmene");
        Theme.styleButton(btnSave);
        btnSave.setBackground(new Color(46, 204, 113));

        JButton btnBack = new JButton("Nazad");
        Theme.styleButton(btnBack);
        btnBack.setBackground(Theme.SECONDARY);

        btnSave.addActionListener(e -> {
            try {
                RegisterDto dto = new RegisterDto();
                dto.email = tfEmail.getText();
                dto.username = tfUsername.getText();
                dto.password = new String(tfPassword.getPassword());
                dto.firstName = tfName.getText();
                dto.lastName = tfSurname.getText();
                dto.birthDate = LocalDate.parse(tfBirth.getText());

                app.updateProfile(dto);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Greška: Koristite format YYYY-MM-DD!");
            }
        });

        btnBack.addActionListener(e -> app.navigateTo("HUB"));

        buttons.add(btnBack);
        buttons.add(btnSave);
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

    private void addFormField(JPanel panel, String labelText, JTextField textField) {
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.REGULAR_FONT);
        Theme.styleTextField(textField);
        panel.add(label);
        panel.add(textField);
    }
}