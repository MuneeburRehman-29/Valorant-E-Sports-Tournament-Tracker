package app;

public class AccountResponse {
    public int status;
    public AccountData data;

    // Inner class representing the "data" object in the JSON
    public static class AccountData {
        public String puuid;
        public String region;
        public int account_level;
        public String name;
        public String tag;
    }
}
