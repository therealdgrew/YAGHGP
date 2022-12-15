package ru.dgrew.yaghgp.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    final private List<Player> tributes = new ArrayList<>();
    final private List<Player> spectators = new ArrayList<>();
    public PlayerManager() {
        tributes.addAll(Bukkit.getOnlinePlayers());
    }
    public void transferToSpectators(Player p) {
        tributes.remove(p);
        spectators.add(p);
        p.setGameMode(GameMode.SPECTATOR);
    }
    public List<Player> getRemainingPlayersList() {
        return tributes;
    }
    public void updatePlayersList() {
        spectators.clear();
        tributes.clear();
        tributes.addAll(Bukkit.getOnlinePlayers());
    }
    public void removeOnDC(Player p){
        tributes.remove(p);
    }
}
