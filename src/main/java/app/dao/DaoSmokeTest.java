package app.dao;

public class DaoSmokeTest {

    public static void main(String[] args) throws Exception {
        TeamDAO teamDAO = new TeamDAO();
        PlayerDAO playerDAO = new PlayerDAO();
        MatchDAO matchDAO = new MatchDAO();

        System.out.println("Teams: " + teamDAO.findAll().size());
        System.out.println("Players: " + playerDAO.findAll().size());
        System.out.println("Matches: " + matchDAO.findAll().size());
    }
}
