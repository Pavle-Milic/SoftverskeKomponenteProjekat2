package org.example.panels;

import org.example.ClientApp;
import org.example.model.CreateSessionDto;
import org.example.model.GameDto;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateSessionPanel extends JPanel {
    private ClientApp app;

    private JComboBox<GameDto> cmbGames = new JComboBox<>();
    private JTextField tfTitle = new JTextField();
    private JTextArea taDesc = new JTextArea(3, 20);
    private JSpinner spPlayers = new JSpinner(new SpinnerNumberModel(5, 2, 100, 1));
    private JComboBox<String> cmbType = new JComboBox<>(new String[]{"OPEN", "CLOSED"});
    // Jednostavan input za vreme (može se unaprediti sa DatePickerom)
    private JTextField tfTime = new JTextField(LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));

    public CreateSessionPanel(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        form.add(new JLabel("Izaberi Igru:"));
        form.add(cmbGames);

        form.add(new JLabel("Naslov sesije:"));
        form.add(tfTitle);

        form.add(new JLabel("Opis:"));
        form.add(new JScrollPane(taDesc));

        form.add(new JLabel("Max igrača:"));
        form.add(spPlayers);

        form.add(new JLabel("Tip sesije:"));
        form.add(cmbType);

        form.add(new JLabel("Vreme (yyyy-MM-ddTHH:mm):"));
        form.add(tfTime);

        JButton btnCreate = new JButton("Napravi Sesiju");
        JButton btnBack = new JButton("Otkaži");

        btnCreate.addActionListener(e -> submit());
        btnBack.addActionListener(e -> app.navigateTo("SESSION_LIST"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnBack);
        btnPanel.add(btnCreate);

        add(new JLabel("NOVA SESIJA", SwingConstants.CENTER), BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void loadGames() {
        cmbGames.removeAllItems();
        java.util.List<GameDto> games = app.getAllGames();
        for(GameDto g : games) {
            cmbGames.addItem(g);
        }
    }

    private void submit() {
        try {
            GameDto selectedGame = (GameDto) cmbGames.getSelectedItem();
            if(selectedGame == null) {
                JOptionPane.showMessageDialog(this, "Moraš izabrati igru!");
                return;
            }

            CreateSessionDto dto = new CreateSessionDto();
            dto.gameId = selectedGame.id;
            dto.title = tfTitle.getText();
            dto.description = taDesc.getText();
            dto.maxPlayers = (Integer) spPlayers.getValue();
            dto.type = (String) cmbType.getSelectedItem();

            // FIX ZA DATUM: Uzimamo tekst iz polja
            String timeText = tfTime.getText().trim();
            // Ako je format yyyy-MM-ddTHH:mm (16 karaktera), dodajemo sekunde
            if (timeText.length() == 16) {
                timeText += ":00";
            }

            dto.time = LocalDateTime.parse(timeText);

            if(app.createSession(dto)) {
                JOptionPane.showMessageDialog(this, "Sesija uspešno kreirana!");
                app.navigateTo("SESSION_LIST");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Greška. Proveri format datuma (yyyy-MM-ddTHH:mm): " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}