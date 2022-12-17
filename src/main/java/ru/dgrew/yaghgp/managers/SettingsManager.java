package ru.dgrew.yaghgp.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class SettingsManager {
    final private FileConfiguration config;
    public SettingsManager(FileConfiguration config) {
        this.config = config;
        setUpEntries();
    }
    private World worldobj;
    private String lobby;
    public World getLobbyobj() {
        worldobj = Bukkit.getWorld(lobby);
        return worldobj;
    }
    private String lobbySpawn;
    public Location fetchLobbySpawn() {
        String[] coords = lobbySpawn.split(":");
        return new Location(getLobbyobj(),
                Double.parseDouble(coords[0]),
                Double.parseDouble(coords[1]),
                Double.parseDouble(coords[2]),
                Float.parseFloat(coords[3]),
                Float.parseFloat(coords[4]));
    }
    private String arena;
    public World getArenaobj() {
        worldobj = Bukkit.getWorld(arena);
        return worldobj;
    }
    private List<String> coordinates;
    final private List<Location> correctedCoordinates = new ArrayList<>();
    public List<Location> fetchCorrectedCoordinates() {
        if (correctedCoordinates.size()==0) {
            String[] coords;
            for (String s : coordinates) {
                coords = s.split(":");
                correctedCoordinates.add(new Location(getArenaobj(),
                        Double.parseDouble(coords[0]),
                        Double.parseDouble(coords[1]),
                        Double.parseDouble(coords[2]),
                        Float.parseFloat(coords[3]),
                        Float.parseFloat(coords[4])));
            }
            return correctedCoordinates;
        }
        else return correctedCoordinates;
    }
    private String arenaCenter;
    public Location fetchArenaCenter() {
        String[] coords = arenaCenter.split(":");
        return new Location(getArenaobj(), Double.parseDouble(coords[0]), 0, Double.parseDouble(coords[1]));
    }
    private int borderRadius;
    public Integer getBorderRadius() { return borderRadius; }
    void setUpEntries() {
        lobby = config.getString("settings.lobby");
        lobbySpawn = config.getString("settings.lobby-spawn");
        arena = config.getString("settings.arena");
        coordinates = config.getStringList("settings.arena-spawns");
        arenaCenter = config.getString("settings.arena-center");
        borderRadius = config.getInt("settings.world-border-radius");
    }
}
