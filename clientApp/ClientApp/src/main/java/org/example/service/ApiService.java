package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.*;
import org.example.state.AppState;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ApiService {

    // Backend sluša na /api, pa ovo moramo uključiti u osnovni URL
    private static final String SESSION_SERVICE_URL = "http://localhost:8084/session-service/api";
    // UserController je mapiran na /user, pa je puna putanja /api/user
    private static final String USER_SERVICE_URL = "http://localhost:8084/user-service/api/user";

    private final HttpClient client;
    private final ObjectMapper mapper;

    public ApiService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        // BITNO: Backend šalje datume kao niz [2024, 1, 1], ovo ga tera da koristi ISO string
        // Ali pošto smo na backendu stavili write-dates-as-timestamps=false, ovo je ok.
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // --- AUTH ---
    public String login(String email, String password) throws Exception {
        // Backend putanja: /api/user/login
        String json = mapper.writeValueAsString(new LoginRequest(email, password));
        String response = sendPost(USER_SERVICE_URL + "/login", json);
        TokenResponse tokenResp = mapper.readValue(response, TokenResponse.class);
        return tokenResp.token;
    }

    public void register(RegisterDto dto) throws Exception {
        // Backend putanja: /api/user/register
        String json = mapper.writeValueAsString(dto);
        sendPost(USER_SERVICE_URL + "/register", json);
    }

    // --- USER / ADMIN ---

    public List<UserDto> getAllUsers() throws Exception {
        String response = sendGet(USER_SERVICE_URL+ "/all");

        // Backend vraća Page<UserAllDto>, pa moramo izvući "content"
        return mapper.readTree(response).get("content")
                .traverse(mapper).readValueAs(new TypeReference<List<UserDto>>(){});
    }

    public void updateProfile(Long userId, RegisterDto dto) throws Exception {
        // Backend putanja: /api/user/update/{id}
        String json = mapper.writeValueAsString(dto);
        sendPut(USER_SERVICE_URL + "/update/" + userId, json);
    }

    public void blockUser(Long userId) throws Exception {
        // Backend putanja: /api/user/{id}/block
        sendPost(USER_SERVICE_URL + "/" + userId + "/block", "");
    }

    // --- GAMES ---
    public List<GameDto> getAllGames() throws Exception {
        // Backend putanja: /api/game/all
        // Ovo je OK jer u GameController-u piše @RequestMapping("/all")
        String response = sendGet(SESSION_SERVICE_URL + "/game/all");
        return mapper.readValue(response, new TypeReference<List<GameDto>>(){});
    }

    public void createGame(GameDto dto) throws Exception {
        // Backend putanja: /api/game/create
        String json = mapper.writeValueAsString(dto);
        sendPost(SESSION_SERVICE_URL + "/game/create", json);
    }

    // --- SESSIONS ---
    public void createSession(CreateSessionDto dto) throws Exception {
        // Backend putanja: /api/session/create
        String json = mapper.writeValueAsString(dto);
        sendPost(SESSION_SERVICE_URL + "/session/create", json);
    }

    public List<Session> searchSessions(String queryParams) throws Exception {
        String url = SESSION_SERVICE_URL + "/session/search";
        if (queryParams != null && !queryParams.isEmpty()) {
            url += queryParams;
        }

        String response = sendGet(url);

        // Backend vraća Page<Session>, uzimamo content
        return mapper.readTree(response).get("content")
                .traverse(mapper).readValueAs(new TypeReference<List<Session>>(){});
    }

    public void joinSession(Long sessionId, String invitationToken) throws Exception {
        // Backend putanja: /api/session/{id}/join
        String url = SESSION_SERVICE_URL + "/session/" + sessionId + "/join";
        if (invitationToken != null && !invitationToken.isEmpty()) {
            url += "?invitationToken=" + invitationToken;
        }
        sendPost(url, "");
    }

    public String inviteUser(Long sessionId, Long userId) throws Exception {
        // Backend putanja: /api/session/{id}/invite
        String url = SESSION_SERVICE_URL + "/session/" + sessionId + "/invite?userId=" + userId;
        return sendPost(url, "");
    }

    public void concludeSession(Long sessionId, List<Long> presentIds) throws Exception {
        // Backend putanja: /api/session/{id}/conclude
        String json = mapper.writeValueAsString(presentIds);
        sendPost(SESSION_SERVICE_URL + "/session/" + sessionId + "/conclude", json);
    }

    public void cancelSession(Long sessionId) throws Exception {
        // Backend putanja: /api/session/{id}/cancel
        sendPost(SESSION_SERVICE_URL + "/session/" + sessionId + "/cancel", "");
    }

    // --- HTTP HELPERS (Bez izmena) ---
    private String sendGet(String url) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).GET();
        addAuthHeader(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(response);
        return response.body();
    }

    public List<Session> getMySessions() throws Exception {
        // Backend putanja: /api/session/my-sessions
        String response = sendGet(SESSION_SERVICE_URL + "/session/my-sessions");

        // Posto ovde vracamo cistu Listu (a ne Page), mapiranje je jednostavnije:
        return mapper.readValue(response, new TypeReference<List<Session>>(){});
    }

    public List<SessionPlayer> getSessionPlayers(Long sessionId) throws Exception {
        // Backend putanja: /api/session/{id}/players
        String url = SESSION_SERVICE_URL + "/session/" + sessionId + "/players";
        String response = sendGet(url);

        return mapper.readValue(response, new TypeReference<List<SessionPlayer>>(){});
    }

    private String sendPost(String url, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        addAuthHeader(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(response);
        return response.body();
    }

    private String sendPut(String url, String jsonBody) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));
        addAuthHeader(builder);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        checkError(response);
        return response.body();
    }

    private void addAuthHeader(HttpRequest.Builder builder) {
        String token = AppState.getInstance().getToken();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
    }

    private void checkError(HttpResponse<String> response) throws Exception {
        if (response.statusCode() >= 400) {
            throw new Exception("Server Error (" + response.statusCode() + "): " + response.body());
        }
    }
}