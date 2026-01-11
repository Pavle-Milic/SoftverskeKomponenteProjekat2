package org.example;

import lombok.Getter;
import org.example.model.*;
import org.example.panels.*;
import org.example.service.ApiService;
import org.example.state.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientApp extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);
    private final ApiService apiService = new ApiService();

    // Reference ka panelima koji se moraju ažurirati
    private HubPanel hubPanel;
    private SessionListPanel sessionListPanel;
    private AdminUserListPanel adminUserListPanel;
    private CreateSessionPanel createSessionPanel;
    private MySessionsPanel mySessionsPanel;

    public ClientApp() {
        setTitle("RAF Gaming Centar");
        setSize(1200, 800); // Malo veći prozor
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicijalizacija panela
        initPanels();

        add(mainContainer);

        // Početni ekran
        navigateTo("START");
    }

    private void initPanels() {
        // Kreiranje instanci panela i prosleđivanje 'this' (ClientApp)
        hubPanel = new HubPanel(this);
        sessionListPanel = new SessionListPanel(this);
        adminUserListPanel = new AdminUserListPanel(this);
        createSessionPanel = new CreateSessionPanel(this);
        mySessionsPanel = new MySessionsPanel(this);

        // Dodavanje u CardLayout sa jedinstvenim ključevima
        mainContainer.add(new StartPanel(this), "START");
        mainContainer.add(new LoginPanel(this), "LOGIN");
        mainContainer.add(new RegisterPanel(this), "REGISTER");
        mainContainer.add(hubPanel, "HUB");
        mainContainer.add(new UpdatePanel(this), "UPDATE");
        mainContainer.add(sessionListPanel, "SESSION_LIST");
        mainContainer.add(createSessionPanel, "CREATE_SESSION");
        mainContainer.add(new AddGamePanel(this), "ADD_GAME");
        mainContainer.add(adminUserListPanel, "ADMIN_LIST");
        mainContainer.add(mySessionsPanel, "MY_SESSIONS");
    }

    // --- NAVIGACIJA ---

    public void navigateTo(String key) {
        // Logika pre prikaza određenih panela
        if ("HUB".equals(key)) {
            refreshHub();
        }
        else if ("SESSION_LIST".equals(key)) {
            fetchSessions(null);
        }
        else if ("CREATE_SESSION".equals(key)) {
            createSessionPanel.loadGames();
        }
        else if ("ADMIN_LIST".equals(key)) {
            fetchUsersForAdmin();
        }
        else if ("MY_SESSIONS".equals(key)) {
            mySessionsPanel.loadSessions();
        }

        cardLayout.show(mainContainer, key);
    }

    // --- AUTH ACTIONS ---

    public void login(String email, String password) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return apiService.login(email, password);
            }

            @Override
            protected void done() {
                try {
                    String token = get();
                    AppState.getInstance().setToken(token);
                    navigateTo("HUB");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ClientApp.this, "Login failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void register(RegisterDto dto) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.register(dto);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ClientApp.this, "Registracija uspešna! Prijavite se.");
                    navigateTo("LOGIN");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ClientApp.this, "Greška: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void logout() {
        AppState.getInstance().logout();
        navigateTo("START");
    }

    public void updateProfile(RegisterDto dto) {
        Long userId = AppState.getInstance().getCurrentUserId();
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.updateProfile(userId, dto);
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(ClientApp.this, "Profil ažuriran.");
                    navigateTo("HUB");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ClientApp.this, "Greška: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void refreshHub() {
        AppState state = AppState.getInstance();

        // Sada koristimo prave podatke iz tokena!
        hubPanel.updateInfo(
                state.getCurrentUsername(),
                state.getCurrentUserRank(),
                state.getCurrentUserRole()
        );
    }

    // --- SESSION ACTIONS ---

    public boolean createSession(CreateSessionDto dto) {
        try {
            apiService.createSession(dto);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
            return false;
        }
    }

    public void fetchSessions(String query) {
        new SwingWorker<List<Session>, Void>() {
            @Override
            protected List<Session> doInBackground() throws Exception {
                return apiService.searchSessions();
            }
            @Override
            protected void done() {
                try {
                    sessionListPanel.updateTable(get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    public boolean joinSession(Long sessionId) {
        try {
            // Ako treba token za zatvorenu sesiju, apiService to hendluje
            // Ovde pojednostavljeno za OPEN sesije
            apiService.joinSession(sessionId, null);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška pri ulasku: " + e.getMessage());
            return false;
        }
    }

    // --- GAME ACTIONS ---

    public boolean createGame(GameDto dto) {
        try {
            apiService.createGame(dto);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
            return false;
        }
    }

    public List<GameDto> getAllGames() {
        try {
            return apiService.getAllGames();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Session> fetchMySessions() {
        try {
            return apiService.getMySessions();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ClientApp.this, "Greška: " + e.getMessage());
        }
        return List.of();
    }

    public void concludeSession(Long sessionId, List<Long> presentUserIds) {

        try {
            apiService.concludeSession(sessionId, presentUserIds);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
        }

    }

    public void cancelSession(Long id) {
        try {
            apiService.cancelSession(id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
        }
    }

    public List<SessionPlayer> getSessionPlayers(Long id) {
        try {
            return apiService.getSessionPlayers(id);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
        }
        return List.of();
    }

    // --- ADMIN ACTIONS ---

    public void fetchUsersForAdmin() {
        new SwingWorker<List<UserDto>, Void>() {
            @Override
            protected List<UserDto> doInBackground() throws Exception {
                return apiService.getAllUsers();
            }
            @Override
            protected void done() {
                try {
                    adminUserListPanel.updateTable(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ClientApp.this, "Greška pri učitavanju korisnika.");
                }
            }
        }.execute();
    }

    public void blockUser(Long userId) {
        try {
            apiService.blockUser(userId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Greška: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new ClientApp().setVisible(true));
    }
}