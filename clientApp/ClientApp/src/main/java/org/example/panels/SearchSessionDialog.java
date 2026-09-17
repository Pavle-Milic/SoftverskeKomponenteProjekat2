package org.example.panels;

import org.example.ClientApp;
import org.example.model.GameDto;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SearchSessionDialog extends JDialog {

    private JComboBox<GameItem> cmbGames;
    private JComboBox<String> cmbType;
    private JSpinner spinMaxPlayers;
    private JTextField txtDescription;
    private ClientApp app;

    public SearchSessionDialog(Frame parent, ClientApp app, List<GameDto> games) {
        super(parent, "Pretraga Sesija", true);
        this.app = app;
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- FORM PANEL ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Game Selector
        formPanel.add(new JLabel("Igra:"));
        cmbGames = new JComboBox<>();
        cmbGames.addItem(new GameItem(null, "Sve igre"));
        if (games != null) {
            for (GameDto g : games) {
                cmbGames.addItem(new GameItem(g.getId(), g.getName()));
            }
        }
        formPanel.add(cmbGames);

        // 2. Type Selector
        formPanel.add(new JLabel("Tip sesije:"));
        cmbType = new JComboBox<>();
        // Dodajemo String opcije
        cmbType.addItem("Svi tipovi"); // Default opcija (null na backendu)
        cmbType.addItem("OPEN");
        cmbType.addItem("CLOSED");
        formPanel.add(cmbType);

        // 3. Max Players
        formPanel.add(new JLabel("Max Igrača (do):"));
        spinMaxPlayers = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        formPanel.add(spinMaxPlayers);

        // 4. Description
        formPanel.add(new JLabel("Opis sadrži:"));
        txtDescription = new JTextField();
        formPanel.add(txtDescription);

        add(formPanel, BorderLayout.CENTER);

        // --- BUTTON PANEL ---
        JPanel btnPanel = new JPanel();
        JButton btnSearch = new JButton("Pretraži");
        JButton btnCancel = new JButton("Otkaži");

        btnSearch.addActionListener(e -> performSearch());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnSearch);
        btnPanel.add(btnCancel);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void performSearch() {
        StringBuilder sb = new StringBuilder("?size=100");

        // Game ID
        GameItem selectedGame = (GameItem) cmbGames.getSelectedItem();
        if (selectedGame != null && selectedGame.id != null) {
            sb.append("&gameId=").append(selectedGame.id);
        }

        // Type Logic (String version)
        String selectedType = (String) cmbType.getSelectedItem();
        if (selectedType != null && !selectedType.equals("Svi tipovi")) {
            sb.append("&type=").append(selectedType);
        }

        // Max Players
        int maxP = (Integer) spinMaxPlayers.getValue();
        if (maxP > 0) {
            sb.append("&maxPlayers=").append(maxP);
        }

        // Description
        String desc = txtDescription.getText().trim();
        if (!desc.isEmpty()) {
            sb.append("&description=").append(desc);
        }

        // Pozivamo App
        app.fetchSessions(sb.toString());
        dispose();
    }

    private static class GameItem {
        Long id;
        String name;

        public GameItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}