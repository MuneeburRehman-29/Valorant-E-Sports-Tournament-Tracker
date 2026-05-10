package app;

import java.util.List;

public class MatchResponse {
    public int status;
    public List<MatchData> data;

    public static class MatchData {
        public Metadata metadata;
        public Players players;
    }

    public static class Metadata {
        public String matchid;
        public String map;
        public int rounds_played;
    }

    public static class Players {
        public List<Player> all_players;
    }

    public static class Player {
        public String name;
        public String tag;
        public String team;
        public String character;
        public Stats stats;
    }

    public static class Stats {
        public int score;
        public int kills;
        public int deaths;
        public int assists;
    }
}
