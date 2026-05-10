package app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;

import app.dao.PlayerDAO;
import models.Player;
import java.sql.SQLException;
import app.AccountResponse;

public class ValorantApiService {

    private static final String BASE_URL = "https://api.henrikdev.xyz/valorant";

    private final PlayerDAO playerDAO = new PlayerDAO();

    public String getAccountDetails(String riotId, String tagLine) {
        // Keep original behavior: fetch and save in one call.
        try {
            AccountResponse accountInfo = fetchAccountInfo(riotId, tagLine);
            if (accountInfo == null || accountInfo.data == null) {
                return "Failed to parse account information.";
            }

            String fullRiotId = accountInfo.data.name + "#" + accountInfo.data.tag;
            StringBuilder result = new StringBuilder();
            result.append("Successfully parsed player: ").append(fullRiotId).append("\n");
            result.append("Account Level: ").append(accountInfo.data.account_level).append("\n");
            result.append("Region: ").append(accountInfo.data.region).append("\n");

            Player newPlayer = new Player();
            newPlayer.setRiotId(fullRiotId);
            newPlayer.setActive(true);

            try {
                java.util.Optional<Player> existing = playerDAO.findByRiotId(fullRiotId);
                if (existing.isEmpty()) {
                    playerDAO.insert(newPlayer);
                    result.append("Player saved to database successfully.\n");
                } else {
                    result.append("Player already exists in database.\n");
                }
            } catch (SQLException e) {
                result.append("Failed to save player to database: ").append(e.getMessage()).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            return "Something went wrong with the fetch: " + e.getMessage();
        }
    }

    /**
     * Fetches account information from Henrik API without saving to the database.
     * Returns an AccountResponse on success or throws an Exception on error.
     */
    public AccountResponse fetchAccountInfo(String riotId, String tagLine) throws Exception {
        String apiKey = System.getenv("HENRIK_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("HENRIK_API_KEY environment variable is not set!");
        }

        String endpoint = BASE_URL + "/v1/account/" + riotId + "/" + tagLine;
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("API Error. Status Code: " + response.statusCode());
        }

        Gson gson = new Gson();
        return gson.fromJson(response.body(), AccountResponse.class);
    }

    public void getMatchHistory(String region, String riotId, String tagLine) {
        String apiKey = System.getenv("HENRIK_API_KEY");

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("Error: HENRIK_API_KEY environment variable is not set!");
            return;
        }

        String endpoint = BASE_URL + "/v3/matches/" + region + "/" + riotId + "/" + tagLine;
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Authorization", apiKey)
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Gson gson = new Gson();
                MatchResponse matchHistory = gson.fromJson(response.body(), MatchResponse.class);

                if (matchHistory.data != null && !matchHistory.data.isEmpty()) {
                    MatchResponse.MatchData latestMatch = matchHistory.data.get(0);
                    System.out.println("\n--- LATEST MATCH DATA ---");
                    System.out.println("Map: " + latestMatch.metadata.map);
                    System.out.println("Rounds Played: " + latestMatch.metadata.rounds_played);

                    for (MatchResponse.Player p : latestMatch.players.all_players) {
                        if (p.name.equalsIgnoreCase(riotId) && p.tag.equalsIgnoreCase(tagLine)) {
                            System.out.println("Agent: " + p.character);
                            System.out.println("K/D/A: " + p.stats.kills + "/" + p.stats.deaths + "/" + p.stats.assists);
                            System.out.println("ACS: " + (p.stats.score / latestMatch.metadata.rounds_played));
                            break;
                        }
                    }
                }
            } else {
                System.out.println("API Error. Status Code: " + response.statusCode());
            }

        } catch (Exception e) {
            System.out.println("Something went wrong with the match fetch: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ValorantApiService api = new ValorantApiService();
        System.out.println("Fetching Account...");
        String res = api.getAccountDetails("TenZ", "SEN");
        System.out.println(res);

        System.out.println("\nFetching Matches...");
        api.getMatchHistory("na", "TenZ", "SEN");
    }
}
