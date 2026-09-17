package org.example.panels;

import org.example.ClientApp;
import org.example.model.GameDto;
import org.example.model.Session;
import org.example.state.AppState;
import org.example.utils.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionListPanel extends JPanel {
    private ClientApp app;
    private JTable table;
    private DefaultTableModel tableModel;

    // 1. Definisemo dugme kao polje klase da bi mu pristarali iz drugih metoda
    private JButton btnAdminCancel;

    public SessionListPanel(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TOP PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // LEVO
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);
        JButton btnBack = new JButton("Nazad");
        Theme.styleSecondaryButton(btnBack);
        JButton btnRefresh = new JButton("Osveži");
        Theme.styleSecondaryButton(btnRefresh);
        leftActions.add(btnBack);
        leftActions.add(btnRefresh);

        // DESNO
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton btnSearch = new JButton("Pretraga");
        Theme.styleButton(btnSearch);
        JButton btnMySessions = new JButton("Moje Sesije");
        Theme.styleButton(btnMySessions);
        JButton btnCreate = new JButton("+ Kreiraj Sesiju");
        Theme.styleSuccessButton(btnCreate);

        rightActions.add(btnSearch);
        rightActions.add(btnMySessions);
        rightActions.add(btnCreate);

        topPanel.add(leftActions, BorderLayout.WEST);
        topPanel.add(rightActions, BorderLayout.EAST);

        // Listeners
        btnBack.addActionListener(e -> app.navigateTo("HUB"));
        btnCreate.addActionListener(e -> app.navigateTo("CREATE_SESSION"));
        btnRefresh.addActionListener(e -> refreshSessions()); // Pozivamo lokalnu metodu
        btnMySessions.addActionListener(e -> app.navigateTo("MY_SESSIONS"));

        btnSearch.addActionListener(e -> {
            new SwingWorker<List<GameDto>, Void>() {
                @Override
                protected List<GameDto> doInBackground() throws Exception {
                    return app.getAllGames();
                }
                @Override
                protected void done() {
                    try {
                        List<GameDto> games = get();
                        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(SessionListPanel.this);
                        new SearchSessionDialog(parentFrame, app, games).setVisible(true);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SessionListPanel.this, "Greska: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        // --- TABELA ---
        String[] columns = {"ID", "Naslov", "Igra", "Vreme", "Max", "Tip", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        Theme.styleTable(table);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0; i<table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // --- BOTTOM PANEL ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        bottomPanel.setOpaque(false);

        JButton btnJoin = new JButton("Pridruži se Sesiji (JOIN)");
        Theme.styleButton(btnJoin);
        btnJoin.setPreferredSize(new Dimension(250, 50));

        btnJoin.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Long sessionId = (Long) tableModel.getValueAt(row, 0);
                String type = (String) tableModel.getValueAt(row, 5);

                if("CLOSED".equals(type)) {
                    JOptionPane.showMessageDialog(this, "Ovo je zatvorena sesija, potreban je link.");
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

        bottomPanel.add(btnJoin);

        // --- ADMIN DUGME (Kreiramo ga UVEK, ali cemo menjati vidljivost) ---
        btnAdminCancel = new JButton("ADMIN: Otkaži Sesiju");
        Theme.styleDangerButton(btnAdminCancel);
        btnAdminCancel.setPreferredSize(new Dimension(250, 50));

        // Po defaultu je sakriveno (dok ne proverimo ulogu)
        btnAdminCancel.setVisible(false);

        btnAdminCancel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Selektuj sesiju za brisanje.");
                return;
            }

            Long sessionId = (Long) tableModel.getValueAt(row, 0);
            String title = (String) tableModel.getValueAt(row, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "ADMIN AKCIJA: Da li sigurno želiš da otkažeš sesiju '" + title + "'?",
                    "Admin Potvrda", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                app.cancelSession(sessionId);
                Timer t = new Timer(500, x -> refreshSessions());
                t.setRepeats(false);
                t.start();
            }
        });

        bottomPanel.add(btnAdminCancel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshSessions() {
        // Svaki put kad osvezimo sesije, proverimo da li je korisnik admin
        checkAdminStatus();
        app.fetchSessions("?size=100");
    }

    // Nova metoda koja proverava stanje
    private void checkAdminStatus() {
        boolean isAdmin = AppState.getInstance().isAdmin();
        // Ako je btnAdminCancel inicijalizovan, postavi vidljivost
        if (btnAdminCancel != null) {
            btnAdminCancel.setVisible(isAdmin);
            // Ponovno iscrtavanje panela ako se dugme pojavi/nestane
            btnAdminCancel.getParent().revalidate();
            btnAdminCancel.getParent().repaint();
        }
    }

    public void updateTable(List<Session> sessions) {
        // Takodje proverimo status ovde, za svaki slucaj (kad podaci stignu)
        checkAdminStatus();

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