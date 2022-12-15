package ru.dgrew.hg.phases;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.Phase;
import ru.dgrew.hg.managers.SettingsManager;

public class Lobby extends Phase {
    private SettingsManager sm;
    @Override
    public void onEnable() {
        sm = new SettingsManager(Main.getInstance().getConfig());
        System.out.println("Lobby phase has started successfully!");
    }
    @Override
    public void onDisable() {
    }
    @Override
    public Phase next() {
        return new PreGame();
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.joinMessage(Component.text(p.getName())
                        .color(NamedTextColor.GOLD)
                        .append(Component.text(" has joined!"))
                            .color(NamedTextColor.YELLOW));
        p.setGameMode(GameMode.ADVENTURE);
        p.setExp(0);
        p.setLevel(0);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(1.0f);
        p.setTotalExperience(0);
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[]{null, null, null, null});
        p.getActivePotionEffects().forEach(effect -> p.removePotionEffect(effect.getType()));
        p.teleport(sm.fetchLobbySpawn());
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.quitMessage(Component.text(e.getPlayer().getName())
                .color(NamedTextColor.GOLD)
                .append(Component.text(" has left."))
                .color(NamedTextColor.YELLOW));
    }
    @EventHandler
    public void onWorldDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e){
        e.setCancelled(true);
    }
}
