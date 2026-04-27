package models;

import java.time.LocalDate;

public class Tournament {
    private int tournamentId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private double prizePool;

    public Tournament() {}

    public Tournament(int tournamentId, String name, LocalDate startDate, LocalDate endDate, double prizePool) {
        this.tournamentId = tournamentId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.prizePool = prizePool;
    }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public double getPrizePool() { return prizePool; }
    public void setPrizePool(double prizePool) { this.prizePool = prizePool; }
}