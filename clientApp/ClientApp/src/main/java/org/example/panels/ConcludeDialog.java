package org.example.panels;

import org.example.ClientApp;
import org.example.model.SessionPlayer;
import org.example.utils.Theme; // Tvoja Theme klasa

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcludeDialog extends JDialog {

    private final Map<Long, JCheckBox> attendanceMap = new HashMap<>();

    public ConcludeDialog(ClientApp parent, Long sessionId, String gameName, List<SessionPlayer> players) {
        super(parent, "Zaključi sesiju", true);
        setSize(450, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Postavljamo globalnu pozadinu iz teme
        getContentPane().setBackground(Theme.BG_COLOR);

        // ===========================
        // 1. HEADER
        // ===========================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_COLOR);
        headerPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel(gameName);
        lblTitle.setFont(Theme.HEADER_FONT);
        lblTitle.setForeground(Theme.PRIMARY); // Naslov u plavoj boji

        JLabel lblSubtitle = new JLabel("Potvrdite prisustvo igrača:");
        lblSubtitle.setFont(Theme.REGULAR_FONT);
        lblSubtitle.setForeground(Theme.TEXT_DARK);
        lblSubtitle.setBorder(new EmptyBorder(5, 0, 0, 0));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // ===========================
        // 2. LISTA IGRACA
        // ===========================
        // Kontejner koji drži redove
        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(Theme.BG_COLOR);

        // Wrapper da bi se lista lepila za vrh (da ne pluta u sredini)
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(Theme.BG_COLOR);
        wrapperPanel.add(listContainer, BorderLayout.NORTH);

        if (players != null && !players.isEmpty()) {
            for (SessionPlayer player : players) {
                listContainer.add(createPlayerRow(player));
                // Mali razmak između redova (opciono)
                listContainer.add(Box.createVerticalStrut(5));
            }
        } else {
            JLabel noPlayers = new JLabel("Nema prijavljenih igrača.", SwingConstants.CENTER);
            noPlayers.setFont(Theme.REGULAR_FONT);
            noPlayers.setForeground(Theme.TEXT_DARK);
            noPlayers.setBorder(new EmptyBorder(20, 0, 0, 0));
            listContainer.add(noPlayers);
        }

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(new EmptyBorder(10, 0, 10, 0)); // Malo vazduha gore i dole
        scrollPane.getViewport().setBackground(Theme.BG_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // ===========================
        // 3. FOOTER (DUGMIĆI)
        // ===========================
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        footerPanel.setBackground(Theme.WHITE); // Beli footer za kontrast
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton btnCancel = new JButton("Otkaži");
        Theme.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        // Dugme SAČUVAJ (Plavo/Primary)
        JButton btnConfirm = new JButton("Sačuvaj");
        Theme.styleButton(btnConfirm);

        btnConfirm.addActionListener(e -> {
            List<Long> presentUserIds = new ArrayList<>();
            for (Map.Entry<Long, JCheckBox> entry : attendanceMap.entrySet()) {
                if (entry.getValue().isSelected()) {
                    presentUserIds.add(entry.getKey());
                }
            }
            parent.concludeSession(sessionId, presentUserIds);
            dispose();
        });

        footerPanel.add(btnCancel);
        footerPanel.add(btnConfirm);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createPlayerRow(SessionPlayer player) {
        // Koristimo BorderLayout jer je najpouzdaniji za Levo-Desno raspored
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.WHITE); // Beli kartoni na sivoj pozadini
        row.setPreferredSize(new Dimension(0, 50));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Padding unutar reda i tanka siva linija okolo (ili samo dole)
        row.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1), // Tanka linija oko kartice
                new EmptyBorder(0, 15, 0, 15) // Padding unutar kartice
        ));

        // 1. Ime igrača (Levo)
        JLabel nameLabel = new JLabel(player.username);
        nameLabel.setFont(Theme.SUBHEADER_FONT); // Malo veći font (16px bold)
        nameLabel.setForeground(Theme.TEXT_DARK); // OBAVEZNO: Tamna boja teksta

        // 2. Checkbox (Desno)
        JCheckBox cb = new JCheckBox("Prisutan");
        cb.setFont(Theme.REGULAR_FONT);
        cb.setForeground(Theme.SECONDARY);
        cb.setBackground(Theme.WHITE); // Mora da se slaže sa pozadinom reda
        cb.setSelected(true);
        cb.setFocusPainted(false);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));

        attendanceMap.put(player.userId, cb);

        // Dodajemo u red
        row.add(nameLabel, BorderLayout.CENTER); // Ime uzima sav prostor
        row.add(cb, BorderLayout.EAST);          // Checkbox ide desno

        return row;
    }
}