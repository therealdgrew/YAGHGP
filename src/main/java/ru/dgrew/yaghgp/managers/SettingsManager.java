package ru.dgrew.yaghgp.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        try {
            String[] coords = lobbySpawn.split(":");
            return new Location(getLobbyobj(),
                    Double.parseDouble(coords[0]),
                    Double.parseDouble(coords[1]),
                    Double.parseDouble(coords[2]),
                    Float.parseFloat(coords[3]),
                    Float.parseFloat(coords[4]));
        }
        catch (NumberFormatException e) {
            Bukkit.getLogger().severe("Lobby coordinates are not set up or set up incorrectly in the plugin config! Please set up the coordinates and restart the server! Related stack trace below:");
            return null;
        }
    }
    private String arena;
    public World getArenaobj() {
        worldobj = Bukkit.getWorld(arena);
        return worldobj;
    }
    final private List<Location> spawnLocations = new ArrayList<>();
    public List<Location> fetchSpawnLocations() {
        Random random = new Random();
        try {
            if (spawnLocations.size()==0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    spawnLocations.add(new Location(getArenaobj(),
                            random.nextInt(60)-30,
                            80,
                            random.nextInt(60)-30
                            )
                    );
                }
                return spawnLocations;
            }
            else return spawnLocations;
        }
        catch (Exception e) {
            Bukkit.getLogger().severe("Could not generate spawn locations for players!");
            return null;
        }
    }
    public Location fetchArenaCenter() {
        return new Location(getArenaobj(), 0, 0, 0);
    }
    private int deathmatchBorderRadius;
    public Integer getDeathmatchBorderRadius() { return deathmatchBorderRadius; }
    private int borderRadius;
    public Integer getBorderRadius() { return borderRadius; }
    private boolean updateCheck;
    public boolean getUpdateCheck() { return updateCheck; }
    void setUpEntries() {
        lobby = config.getString("settings.lobby");
        lobbySpawn = config.getString("settings.lobby-spawn");
        arena = config.getString("settings.arena");
        deathmatchBorderRadius = config.getInt("settings.deathmatch-border-radius");
        borderRadius = config.getInt("settings.world-border-radius");
        updateCheck = config.getBoolean("settings.check-for-updates");
    }
}
