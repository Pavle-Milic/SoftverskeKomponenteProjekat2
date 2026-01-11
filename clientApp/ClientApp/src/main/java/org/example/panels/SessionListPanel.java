package org.example.panels;

import org.example.ClientApp;
import org.example.model.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionListPanel extends JPanel {
    private ClientApp app;
    private JTable table;
    private DefaultTableModel tableModel;

    public SessionListPanel(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Nazad");
        JButton btnCreate = new JButton("Kreiraj Sesiju");
        JButton btnRefresh = new JButton("Osveži");
        JButton btnMySessions = new JButton("Moje Sesije");

        btnBack.addActionListener(e -> app.navigateTo("HUB"));
        btnCreate.addActionListener(e -> app.navigateTo("CREATE_SESSION"));
        btnRefresh.addActionListener(e -> refreshSessions());
        btnMySessions.addActionListener(e -> app.navigateTo("MY_SESSIONS"));

        topPanel.add(btnBack);
        topPanel.add(btnRefresh);
        topPanel.add(btnCreate);
        topPanel.add(btnMySessions);

        // Tabela
        String[] columns = {"ID", "Naslov", "Igra", "Vreme", "Max Igrača", "Tip", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Join Dugme
        JButton btnJoin = new JButton("Pridruži se (Join)");
        btnJoin.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Long sessionId = (Long) tableModel.getValueAt(row, 0);
                String type = (String) tableModel.getValueAt(row, 5);

                if("CLOSED".equals(type)) {
                    JOptionPane.showMessageDialog(this, "Ovo je zatvorena sesija, potreban je link iz mejla.");
                    return;
                }

                if (app.joinSession(sessionId)) {
                    JOptionPane.showMessageDialog(this, "Uspešno ste se prijavili!");
                    refreshSessions();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Izaberite sesiju.");
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnJoin, BorderLayout.SOUTH);
    }

    public void refreshSessions() {
        app.fetchSessions("?size=100&sort=time,asc");
    }

    public void updateTable(List<Session> sessions) {
        tableModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        for (Session s : sessions) {
            Object[] row = {
                    s.id,
                    s.title,
                    (s.game != null ? s.game.name : "N/A"),
                    (s.time != null ? s.time.format(formatter) : "N/A"),
                    s.maxPlayers,
                    s.type,
                    s.status
            };
            tableModel.addRow(row);
        }
    }
}