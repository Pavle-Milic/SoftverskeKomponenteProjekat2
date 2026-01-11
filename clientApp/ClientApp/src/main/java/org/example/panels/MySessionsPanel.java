package org.example.panels;

import org.example.ClientApp;
import org.example.model.Session;
import org.example.model.SessionPlayer;
import org.example.state.AppState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

// 1. Promena: extends JPanel
public class MySessionsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private ClientApp clientApp;
    private List<Session> currentSessions;

    public MySessionsPanel(ClientApp clientApp) {
        // 2. Uklonjen super(parent, title, modal) jer ovo vise nije dijalog
        this.clientApp = clientApp;
        setLayout(new BorderLayout());

        // --- HEADER (Dugme Nazad) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("<< Nazad na Hub");
        btnBack.addActionListener(e -> clientApp.navigateTo("HUB")); // Povratak na Hub
        topPanel.add(btnBack);
        add(topPanel, BorderLayout.NORTH);

        // --- TABELA ---
        String[] columns = {"ID", "Naslov", "Igra", "Vreme", "Status", "Uloga"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- DUGMICI (AKCIJE) ---
        JPanel buttonPanel = new JPanel();

        JButton btnConclude = new JButton("Conclude Selected");
        btnConclude.setBackground(new Color(144, 238, 144)); // Svetlo zelena

        JButton btnCancel = new JButton("Cancel Selected");
        btnCancel.setBackground(new Color(255, 99, 71)); // Crvena

        JButton btnRefresh = new JButton("Osveži");

        buttonPanel.add(btnConclude);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnRefresh);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- LISTENERS ---

        btnRefresh.addActionListener(e -> loadSessions());

        // LOGIKA ZA CONCLUDE
        btnConclude.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Selektuj sesiju!");
                return;
            }

            Session selectedSession = currentSessions.get(selectedRow);
            Long myId = AppState.getInstance().getCurrentUserId();

            // 1. Provera da li si HOST
            if(!selectedSession.hostId.equals(myId)) {
                JOptionPane.showMessageDialog(this, "Samo HOST može da zaključi sesiju.");
                return;
            }

            // 2. Provera da li je sesija vec gotova
            if("FINISHED".equals(selectedSession.status) || "CANCELED".equals(selectedSession.status)){
                JOptionPane.showMessageDialog(this, "Ova sesija je već završena ili otkazana.");
                return;
            }

            // 3. FETCH IGRACA (LAZY LOADING)
            new SwingWorker<List<SessionPlayer>, Void>() {
                @Override
                protected List<SessionPlayer> doInBackground() throws Exception {
                    return clientApp.getSessionPlayers(selectedSession.id);
                }

                @Override
                protected void done() {
                    try {
                        List<SessionPlayer> players = get();

                        // 4. OTVARANJE DIJALOGA ZA PRISUSTVO (Ovo ostaje Dialog jer je popup)
                        String gameName = (selectedSession.game != null) ? selectedSession.game.getName() : selectedSession.title;

                        // Kada zatvorimo dijalog, zelimo osvezavanje.
                        // Mozemo dodati WindowListener na dijalog ili jednostavno osveziti odmah nakon

                        System.out.println("=== DEBUG PODATAKA ===");
                        if (players != null) {
                            for (SessionPlayer p : players) {
                                System.out.println("ID: " + p.userId + " | Username: " + p.username); // <--- DA LI JE OVO NULL?
                            }
                        } else {
                            System.out.println("Lista igrača je NULL!");
                        }

                        ConcludeDialog dialog = new ConcludeDialog(clientApp, selectedSession.id, gameName, players);
                        dialog.setVisible(true);

                        // Osvezi tabelu nakon sto se dijalog zatvori (jer je modalan, kod staje ovde dok se ne zatvori)
                        loadSessions();

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
            if (selectedRow == -1) return;

            Session session = currentSessions.get(selectedRow);
            int confirm = JOptionPane.showConfirmDialog(this, "Otkaži sesiju?");

            if (confirm == JOptionPane.YES_OPTION) {
                // Pozivamo metodu iz ClientApp (pretpostavka da je imas implementiranu sa SwingWorker-om)
                // Ako nemas u ClientApp, moras ovde implementirati SwingWorker kao za Conclude
                clientApp.cancelSession(session.id);

                // Mali delay pa refresh
                Timer t = new Timer(500, x -> loadSessions());
                t.setRepeats(false);
                t.start();
            }
        });

        // Necemo zvati loadSessions() u konstruktoru, vec eksplicitno kad udjemo na panel
    }

    // 3. Promena: public metoda da bi ClientApp mogao da je pozove pri navigaciji
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
                    // ignore or log
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