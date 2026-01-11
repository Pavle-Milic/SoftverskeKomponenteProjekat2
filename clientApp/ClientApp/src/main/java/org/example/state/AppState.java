package org.example.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;

public class AppState {
    private static AppState instance;
    private String token;
    private Long currentUserId;
    private String currentUserRole;
    private String currentUsername;
    private String currentUserRank;

    private AppState() {}

    public static synchronized AppState getInstance() {
        if (instance == null) instance = new AppState();
        return instance;
    }

    public void setToken(String token) {
        this.token = token;
        decodeToken(token); // Čim stigne token, izvuci podatke
    }

    public String getToken() { return token; }
    public Long getCurrentUserId() { return currentUserId; }
    public String getCurrentUserRole() { return currentUserRole; }
    public String getCurrentUsername() { return currentUsername; }
    public String getCurrentUserRank() { return currentUserRank; }


    public boolean isAdmin() {
        return "ROLE_ADMIN".equals(currentUserRole);
    }

    public void logout() {
        this.token = null;
        this.currentUserId = null;
        this.currentUserRole = null;
        this.currentUsername = null;
        this.currentUserRank = null;
    }

    private void decodeToken(String token) {
        try {
            // JWT format: Header.Payload.Signature
            String[] chunks = token.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> claims = mapper.readValue(payload, Map.class);

            this.currentUserId = ((Number) claims.get("id")).longValue();
            this.currentUserRole = (String) claims.get("role");
            this.currentUsername = (String) claims.get("username");

            Object rankObj = claims.get("rank");
            this.currentUserRank = rankObj != null ? rankObj.toString() : "N/A";

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to decode token!");
        }
    }
}