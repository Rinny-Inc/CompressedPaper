package net.minecraft.server;

import java.util.*;

public class Scoreboard {
	private final Map<String, ScoreboardObjective> objectivesByName = new HashMap<>();
	private final Map<IScoreboardCriteria, List<ScoreboardObjective>> objectivesByCriteria = new HashMap<>();
	private final Map<String, Map<ScoreboardObjective, ScoreboardScore>> playerScores = new HashMap<>();
	private final ScoreboardObjective[] displaySlots = new ScoreboardObjective[3];
	private final Map<String, ScoreboardTeam> teamsByName = new HashMap<>();
	private final Map<String, ScoreboardTeam> teamsByPlayer = new HashMap<>();

	public ScoreboardObjective getObjective(String name) {
		return objectivesByName.get(name);
	}

	public ScoreboardObjective registerObjective(String name, IScoreboardCriteria criteria) {
		ScoreboardObjective objective = getObjective(name);
		if (objective != null) {
			throw new IllegalArgumentException("An objective with the name '" + name + "' already exists!");
		}
		objective = new ScoreboardObjective(this, name, criteria);
		objectivesByCriteria.computeIfAbsent(criteria, k -> new ArrayList<>()).add(objective);
		objectivesByName.put(name, objective);
		handleObjectiveAdded(objective);
		return objective;
	}

	public Collection<ScoreboardObjective> getObjectivesForCriteria(IScoreboardCriteria criteria) {
		Collection<ScoreboardObjective> collection = objectivesByCriteria.get(criteria);
		return collection == null ? new ArrayList<>() : new ArrayList<>(collection);
	}

	public ScoreboardScore getPlayerScoreForObjective(String playerName, ScoreboardObjective objective) {
		return playerScores.computeIfAbsent(playerName, k -> new HashMap<>()).computeIfAbsent(objective, k -> new ScoreboardScore(this, objective, playerName));
	}

	public Collection<ScoreboardScore> getScoresForObjective(ScoreboardObjective objective) {
		List<ScoreboardScore> scores = new ArrayList<>();
		for (Map<ScoreboardObjective, ScoreboardScore> map : playerScores.values()) {
			ScoreboardScore score = map.get(objective);
			if (score != null) {
				scores.add(score);
			}
		}
		scores.sort(ScoreboardScore.a);
		return scores;
	}

	public Collection<ScoreboardObjective> getObjectives() {
		return objectivesByName.values();
	}

	public Collection<String> getPlayers() {
		return playerScores.keySet();
	}

	public void resetPlayerScores(String playerName) {
		Map<ScoreboardObjective, ScoreboardScore> map = playerScores.remove(playerName);
		if (map != null) {
			handlePlayerRemoved(playerName);
		}
	}

	public Collection<ScoreboardScore> getScores() {
		List<ScoreboardScore> scores = new ArrayList<>();
		for (Map<ScoreboardObjective, ScoreboardScore> map : playerScores.values()) {
			scores.addAll(map.values());
		}
		return scores;
	}

	public Map<ScoreboardObjective, ScoreboardScore> getPlayerObjectives(String playerName) {
		return playerScores.getOrDefault(playerName, new HashMap<>());
	}

	public void unregisterObjective(ScoreboardObjective objective) {
		objectivesByName.remove(objective.getName());
		for (int i = 0; i < displaySlots.length; i++) {
			if (displaySlots[i] == objective) {
				setDisplaySlot(i, null);
			}
		}
		List<ScoreboardObjective> list = objectivesByCriteria.get(objective.getCriteria());
		if (list != null) {
			list.remove(objective);
		}
		for (Map<ScoreboardObjective, ScoreboardScore> map : playerScores.values()) {
			map.remove(objective);
		}
		handleObjectiveRemoved(objective);
	}

	public void setDisplaySlot(int slot, ScoreboardObjective objective) {
		displaySlots[slot] = objective;
	}

	public ScoreboardObjective getObjectiveForSlot(int slot) {
		return displaySlots[slot];
	}

	public ScoreboardTeam getTeam(String name) {
		return teamsByName.get(name);
	}

	public ScoreboardTeam createTeam(String name) {
		if (teamsByName.containsKey(name)) {
			throw new IllegalArgumentException("A team with the name '" + name + "' already exists!");
		}
		ScoreboardTeam team = new ScoreboardTeam(this, name);
		teamsByName.put(name, team);
		handleTeamAdded(team);
		return team;
	}

	public void removeTeam(ScoreboardTeam team) {
		teamsByName.remove(team.getName());
		for (String player : team.getPlayerNameSet()) {
			teamsByPlayer.remove(player);
		}
		handleTeamRemoved(team);
	}

	public boolean addPlayerToTeam(String playerName, String teamName) {
		if (!teamsByName.containsKey(teamName)) {
			return false;
		}
		ScoreboardTeam team = getTeam(teamName);
		if (getPlayerTeam(playerName) != null) {
			removePlayerFromTeam(playerName);
		}
		teamsByPlayer.put(playerName, team);
		team.getPlayerNameSet().add(playerName);
		return true;
	}

	public boolean removePlayerFromTeam(String playerName) {
		ScoreboardTeam team = getPlayerTeam(playerName);
		if (team != null) {
			removePlayerFromTeam(playerName, team);
			return true;
		}
		return false;
	}

	public void removePlayerFromTeam(String playerName, ScoreboardTeam team) {
		if (getPlayerTeam(playerName) != team) {
			throw new IllegalStateException(
					"Player is either on another team or not on any team. Cannot remove from team '" + team.getName()
							+ "'.");
		}
		teamsByPlayer.remove(playerName);
		team.getPlayerNameSet().remove(playerName);
	}

	public Collection<String> getTeamNames() {
		return teamsByName.keySet();
	}

	public Collection<ScoreboardTeam> getTeams() {
		return teamsByName.values();
	}

	public ScoreboardTeam getPlayerTeam(String playerName) {
		return teamsByPlayer.get(playerName);
	}

	public void handleObjectiveAdded(ScoreboardObjective objective) {
	}

	public void handleObjectiveChanged(ScoreboardObjective objective) {
	}

	public void handleObjectiveRemoved(ScoreboardObjective objective) {
	}

	public void handleScoreChanged(ScoreboardScore score) {
	}

	public void handlePlayerRemoved(String playerName) {
	}

	public void handleTeamAdded(ScoreboardTeam team) {
	}

	public void handleTeamChanged(ScoreboardTeam team) {
	}

	public void handleTeamRemoved(ScoreboardTeam team) {
	}

	public static String getSlotName(int slot) {
		return switch (slot) {
			case 0 -> "list";
			case 1 -> "sidebar";
			case 2 -> "belowName";
			default -> null;
		};
	}

	public static int getSlotForName(String name) {
		return switch (name) {
			case "list" -> 0;
			case "sidebar" -> 1;
			case "belowName" -> 2;
			default -> -1;
		};
	}
}
