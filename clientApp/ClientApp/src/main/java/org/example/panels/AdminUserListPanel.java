package org.example.panels;

import org.example.ClientApp;
import org.example.utils.Theme;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.example.model.UserDto;

public class AdminUserListPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private ClientApp app;
    private JButton btnBlock;

    public AdminUserListPanel(ClientApp app) {
        this.app = app;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_COLOR);

        JLabel title = new JLabel("Admin Panel - Upravljanje Korisnicima", SwingConstants.CENTER);
        title.setFont(Theme.HEADER_FONT);
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        add(title, BorderLayout.NORTH);

        // Kolone
        String[] columns = {"ID", "Username", "Email", "Rank", "Blokiran"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            // Osigurava da kolona "Blokiran" vraca Boolean klasu radi lakseg rendera
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Boolean.class : String.class;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.SUBHEADER_FONT);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(Theme.BG_COLOR);

        btnBlock = new JButton("Promeni Status (Block)");
        Theme.styleDangerButton(btnBlock);

        JButton btnBack = new JButton("Nazad");
        Theme.styleSecondaryButton(btnBack);

        // --- GLAVNA LOGIKA NA KLIK ---
        btnBlock.addActionListener(e -> performBlockAction());

        btnBack.addActionListener(e -> app.navigateTo("HUB"));
        footer.add(btnBack);
        footer.add(btnBlock);
        add(footer, BorderLayout.SOUTH);
    }

    private void performBlockAction() {
        int[] rows = table.getSelectedRows();
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Niste selektovali nijednog korisnika.");
            return;
        }

        List<Long> idsToBlock = new ArrayList<>();
        for (int r : rows) {
            idsToBlock.add((Long) model.getValueAt(r, 0));
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                btnBlock.setEnabled(false);

                for (Long id : idsToBlock) {
                    app.blockUser(id); // Menja stanje na backendu
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnBlock.setEnabled(true);
                app.fetchUsersForAdmin();
                table.revalidate();
                table.repaint();

                JOptionPane.showMessageDialog(AdminUserListPanel.this,
                        "Status uspešno promenjen za selektovane korisnike.");
            }
        }.execute();
    }

    public void updateTable(List<UserDto> users) {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (UserDto u : users) {
                model.addRow(new Object[]{
                        u.id,
                        u.username,
                        u.email,
                        u.rank,
                        u.blocked
                });
            }
            model.fireTableDataChanged();
        });
    }
}