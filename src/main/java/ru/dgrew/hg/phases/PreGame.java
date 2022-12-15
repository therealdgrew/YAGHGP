package ru.dgrew.hg.phases;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.Phase;
import ru.dgrew.hg.managers.ChatManager;
import ru.dgrew.hg.managers.SettingsManager;
import java.util.List;
import java.util.Random;

public class PreGame extends Phase {
    private int timer;
    private ChatManager cm;
    private SettingsManager sm;
    private BukkitTask countdown;
    @Override
    public void onEnable() {
        sm = Main.getSm();
        cm = Main.getCm();
        timer = 15;
        startCountdown();
        scatterPlayers();
        sm.getArenaobj().setAutoSave(false);
        Bukkit.getLogger().info("PreGame phase has started successfully!");
    }
    @Override
    public void onDisable() {
        Bukkit.getLogger().info("Disabling countdown...");
        countdown.cancel();
        Bukkit.getLogger().info("Countdown successfully disabled, handing over to InGame...");
    }
    @Override
    public Phase next() {
        return new InGame();
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(null);
        e.getPlayer().kick(Component.text("Game already started!"));
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.quitMessage(Component.text(e.getPlayer().getName())
                .color(NamedTextColor.GOLD)
                .append(Component.text(" has left."))
                .color(NamedTextColor.YELLOW));
        Main.getPlm().removeOnDC(e.getPlayer());
    }
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) { e.setCancelled(true); }
    @EventHandler
    public void onWorldDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
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
    void startCountdown() {
        countdown = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    if (timer == 15) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getTimer(timer))));
                    if (timer == 10) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getTimer(timer))));
                    if (timer <= 5) {
                        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getTimer(timer))));
                    }

                    timer--;
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                    Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getTimerend())));
                    Main.getPm().nextPhase();
                }
            }
        }.runTaskTimer(Main.getInstance(),20L, 20L);
    }
    void scatterPlayers() {
        Bukkit.getLogger().info("Scattering players...");
        Random random = new Random();
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        List<Location> list = sm.fetchCorrectedCoordinates();
        int var;
        for(int i = 0; i<players.length; i++) {
            var = random.nextInt(list.size());
            players[i].teleport(list.get(var));
            list.remove(var);
            Bukkit.getLogger().info("Scattering player " + players[i].getName() + " to location with index of " + i + ".");
        }
        Bukkit.getLogger().info("All online players should now be scattered!");
    }
}
