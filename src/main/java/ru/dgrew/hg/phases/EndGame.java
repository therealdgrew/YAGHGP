package ru.dgrew.hg.phases;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.Phase;
import ru.dgrew.hg.managers.ChatManager;
import ru.dgrew.hg.managers.SettingsManager;

public class EndGame extends Phase {
    private int timer;
    private SettingsManager sm;
    private ChatManager cm;
    private Player victor;
    @Override
    public void onEnable() {
        timer = 15;
        sm = Main.getSm();
        cm = Main.getCm();
        victor = Main.getPlm().getRemainingPlayersList().get(0);
        victor.sendTitle(cm.getVictorytitle(), cm.getVictory(), 20, 40, 20);
        victor.playSound(victor.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getGlobalvictory().replace("{player}", victor.getName()))));
        startTimer();
        Bukkit.getLogger().info("EndGame phase has started successfully!");
    }
    @Override
    public void onDisable() {
        Bukkit.getServer().unloadWorld(sm.getArenaobj(), true);
        Bukkit.getServer().reload();
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getBlock().getType().name().endsWith("_LEAVES") || !(e.getBlock().getType().name().endsWith("FIRE"))) e.setCancelled(true);
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().equals(new ItemStack(Material.FLINT_AND_STEEL))) e.setCancelled(true);
    }
    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent e) {
        if (!e.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)) e.setCancelled(true);
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
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onWorldDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e){
        e.setCancelled(true);
    }
    void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    Firework fw = (Firework)victor.getWorld().spawnEntity(victor.getLocation(), EntityType.FIREWORK);
                    FireworkMeta fwm = fw.getFireworkMeta();
                    FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(Color.RED).withFade(Color.FUCHSIA).with(FireworkEffect.Type.BALL).trail(true).build();
                    fwm.addEffect(effect);
                    int rp = 3;
                    fwm.setPower(rp);
                    fw.setFireworkMeta(fwm);
                    if (timer == 15) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getEndgame(timer))));
                    if (timer == 10) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getEndgame(timer))));
                    if (timer <= 5) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getEndgame(timer))));
                    timer--;
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()) p.kick(Component.text("Game ended! Restarting server..."));
                    Bukkit.getServer().unloadWorld(sm.getArenaobj(), false);
                    Bukkit.getLogger().info("The arena world should now be unloaded!");
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Bukkit.getServer().shutdown(), 60);
                }
            }
        }.runTaskTimer(Main.getInstance(),20L, 20L);
    }
    @Override
    public Phase next() {
        return null;
    }
}
