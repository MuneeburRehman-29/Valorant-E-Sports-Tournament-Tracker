-- Schema and sample data for Valorant Tracker
-- Database: valorant_esports

CREATE DATABASE IF NOT EXISTS valorant_esports;
USE valorant_esports;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS player_match_stats;
DROP TABLE IF EXISTS match_rounds;
DROP TABLE IF EXISTS matches;
DROP TABLE IF EXISTS tournaments;
DROP TABLE IF EXISTS maps;
DROP TABLE IF EXISTS agents;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS organizations;
SET FOREIGN_KEY_CHECKS = 1;

-- Organizations
CREATE TABLE IF NOT EXISTS organizations (
  org_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  region VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Teams
CREATE TABLE IF NOT EXISTS teams (
  team_id INT AUTO_INCREMENT PRIMARY KEY,
  org_id INT,
  name VARCHAR(100) NOT NULL,
  CONSTRAINT fk_teams_organization FOREIGN KEY (org_id) REFERENCES organizations(org_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Players
CREATE TABLE IF NOT EXISTS players (
  player_id INT AUTO_INCREMENT PRIMARY KEY,
  team_id INT,
  riot_id VARCHAR(100) UNIQUE,
  is_active BOOLEAN DEFAULT TRUE,
  CONSTRAINT fk_players_team FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Agents
CREATE TABLE IF NOT EXISTS agents (
  agent_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  role VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Maps
CREATE TABLE IF NOT EXISTS maps (
  map_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tournaments
CREATE TABLE IF NOT EXISTS tournaments (
  tournament_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  start_date DATE,
  end_date DATE,
  prize_pool DECIMAL(12,2) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Matches
CREATE TABLE IF NOT EXISTS matches (
  match_id INT AUTO_INCREMENT PRIMARY KEY,
  tournament_id INT,
  map_id INT,
  stage VARCHAR(100),
  match_date DATETIME,
  team_a_id INT,
  team_b_id INT,
  winner_team_id INT,
  CONSTRAINT fk_matches_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(tournament_id) ON DELETE SET NULL,
  CONSTRAINT fk_matches_map FOREIGN KEY (map_id) REFERENCES maps(map_id) ON DELETE SET NULL,
  CONSTRAINT fk_matches_team_a FOREIGN KEY (team_a_id) REFERENCES teams(team_id) ON DELETE SET NULL,
  CONSTRAINT fk_matches_team_b FOREIGN KEY (team_b_id) REFERENCES teams(team_id) ON DELETE SET NULL,
  CONSTRAINT fk_matches_winner_team FOREIGN KEY (winner_team_id) REFERENCES teams(team_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Match rounds
CREATE TABLE IF NOT EXISTS match_rounds (
  round_id INT AUTO_INCREMENT PRIMARY KEY,
  match_id INT,
  round_number INT,
  winning_team_id INT,
  winning_side VARCHAR(20),
  win_type VARCHAR(50),
  CONSTRAINT fk_match_rounds_match FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
  CONSTRAINT fk_match_rounds_team FOREIGN KEY (winning_team_id) REFERENCES teams(team_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Player match stats
CREATE TABLE IF NOT EXISTS player_match_stats (
  stat_id INT AUTO_INCREMENT PRIMARY KEY,
  match_id INT,
  player_id INT,
  agent_id INT,
  kills INT DEFAULT 0,
  deaths INT DEFAULT 0,
  assists INT DEFAULT 0,
  combat_score INT DEFAULT 0,
  CONSTRAINT fk_player_match_stats_match FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
  CONSTRAINT fk_player_match_stats_player FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE,
  CONSTRAINT fk_player_match_stats_agent FOREIGN KEY (agent_id) REFERENCES agents(agent_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample data
INSERT INTO organizations (name, region) VALUES
  ('Radiant Esports', 'NA'),
  ('Vanguard Gaming', 'EU');

INSERT INTO teams (org_id, name) VALUES
  (1, 'Radiant Alpha'),
  (1, 'Radiant Beta'),
  (2, 'Vanguard One');

INSERT INTO players (team_id, riot_id, is_active) VALUES
  (1, 'Player#NA1', TRUE),
  (1, 'Player#NA2', TRUE),
  (2, 'Player#NA3', FALSE),
  (3, 'Player#EU1', TRUE);

INSERT INTO agents (name, role) VALUES
  ('Jett','Duelist'),
  ('Sage','Sentinel'),
  ('Sova','Initiator');

INSERT INTO maps (name) VALUES
  ('Ascent'),
  ('Bind'),
  ('Haven');

INSERT INTO tournaments (name, start_date, end_date, prize_pool) VALUES
  ('Spring Invitational', '2026-03-01', '2026-03-10', 50000.00),
  ('Regional Cup', '2026-04-05', '2026-04-12', 20000.00);

INSERT INTO matches (tournament_id, map_id, stage, match_date, team_a_id, team_b_id, winner_team_id) VALUES
  (1, 1, 'Group', '2026-03-02 18:00:00', 1, 2, 1),
  (1, 2, 'Group', '2026-03-03 20:00:00', 2, 3, 3);

INSERT INTO match_rounds (match_id, round_number, winning_team_id, winning_side, win_type) VALUES
  (1, 1, 1, 'Attack', 'Elimination'),
  (1, 2, 2, 'Defense', 'Bomb Defuse');

INSERT INTO player_match_stats (match_id, player_id, agent_id, kills, deaths, assists, combat_score) VALUES
  (1, 1, 1, 18, 12, 4, 250),
  (1, 2, 2, 10, 15, 6, 150),
  (2, 4, 3, 22, 8, 5, 300);

-- End of schema_and_sample_data.sql
