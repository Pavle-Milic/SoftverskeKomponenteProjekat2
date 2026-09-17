package org.example.panels;

import org.example.ClientApp;
import org.example.model.Session;
import org.example.model.SessionPlayer;
import org.example.state.AppState;
import org.example.utils.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MySessionsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private ClientApp clientApp;
    private List<Session> currentSessions;

    public MySessionsPanel(ClientApp clientApp) {
        this.clientApp = clientApp;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- HEADER ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("  Moje Sesije");
        lblTitle.setFont(Theme.HEADER_FONT);
        lblTitle.setForeground(Theme.TEXT_DARK);

        JButton btnBack = new JButton("<< Nazad");
        Theme.styleSecondaryButton(btnBack);
        btnBack.addActionListener(e -> clientApp.navigateTo("HUB"));

        topPanel.add(btnBack, BorderLayout.WEST);
        topPanel.add(lblTitle, BorderLayout.CENTER); // Naslov u sredini (opciono)

        // Desno dugme za refresh
        JButton btnRefresh = new JButton("Osveži");
        Theme.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadSessions());
        topPanel.add(btnRefresh, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- TABELA ---
        String[] columns = {"ID", "Naslov", "Igra", "Vreme", "Status", "Uloga"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table); // PRIMENA STILA

        // Centriranje
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        add(scrollPane, BorderLayout.CENTER);

        // --- ACTION PANEL (Dugmici) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        buttonPanel.setOpaque(false);

        JButton btnConclude = new JButton("Zaključi Sesiju (Conclude)");
        Theme.styleSuccessButton(btnConclude); // Koristimo zelenu iz teme

        JButton btnCancel = new JButton("Otkaži Sesiju (Cancel)");
        Theme.styleDangerButton(btnCancel); // Koristimo crvenu iz teme

        buttonPanel.add(btnConclude);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---

        // LOGIKA ZA CONCLUDE
        btnConclude.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Selektuj sesiju koju želiš da zaključiš.");
                return;
            }

            Session selectedSession = currentSessions.get(selectedRow);
            Long myId = AppState.getInstance().getCurrentUserId();

            if(!selectedSession.hostId.equals(myId)) {
                JOptionPane.showMessageDialog(this, "Samo HOST (Organizator) može da zaključi sesiju.");
                return;
            }

            if("FINISHED".equals(selectedSession.status) || "CANCELED".equals(selectedSession.status)){
                JOptionPane.showMessageDialog(this, "Sesija je već završena/otkazana.");
                return;
            }

            // Lazy loading igraca
            new SwingWorker<List<SessionPlayer>, Void>() {
                @Override
                protected List<SessionPlayer> doInBackground() throws Exception {
                    return clientApp.getSessionPlayers(selectedSession.id);
                }
                @Override
                protected void done() {
                    try {
                        List<SessionPlayer> players = get();
                        String gameName = (selectedSession.game != null) ? selectedSession.game.getName() : selectedSession.title;

                        ConcludeDialog dialog = new ConcludeDialog(clientApp, selectedSession.id, gameName, players);
                        dialog.setVisible(true);

                        loadSessions(); // Osvezi nakon zatvaranja dijaloga
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(MySessionsPanel.this, "Greška: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        // LOGIKA ZA CANCEL
        btnCancel.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Selektuj sesiju za otkazivanje.");
                return;
            }

            Session session = currentSessions.get(selectedRow);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Da li ste sigurni da želite da OTKAŽETE sesiju: " + session.title + "?",
                    "Potvrda otkazivanja", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                clientApp.cancelSession(session.id);
                Timer t = new Timer(500, x -> loadSessions());
                t.setRepeats(false);
                t.start();
            }
        });
    }

    public void loadSessions() {
        new SwingWorker<List<Session>, Void>() {
            @Override
            protected List<Session> doInBackground() throws Exception {
                return clientApp.fetchMySessions();
            }
            @Override
            protected void done() {
                try {
                    currentSessions = get();
                    updateTable(currentSessions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateTable(List<Session> sessions) {
        tableModel.setRowCount(0);
        Long myId = AppState.getInstance().getCurrentUserId();

        for (Session s : sessions) {
            String role = (s.hostId.equals(myId)) ? "ORGANIZATOR" : "IGRAČ";
            String gameName = (s.game != null) ? s.game.getName() : "N/A";

            tableModel.addRow(new Object[]{
                    s.id,
                    s.title,
                    gameName,
                    s.time,
                    s.status,
                    role
            });
        }
    }
}