package ru.dgrew.hg.phases;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.Phase;
import ru.dgrew.hg.managers.ChatManager;
import ru.dgrew.hg.managers.PlayerManager;
import ru.dgrew.hg.managers.SettingsManager;

import java.util.ArrayList;
import java.util.List;

public class Deathmatch extends Phase {
    private int timer;
    private ChatManager cm;
    private PlayerManager pm;
    private SettingsManager sm;
    private boolean prepbool;
    private BukkitTask gracep;
    @Override
    public void onEnable() {
        prepbool = true;
        timer = 10;
        cm = Main.getCm();
        sm = Main.getSm();
        pm = Main.getPlm();
        World arena = sm.getArenaobj();
        arena.getWorldBorder().setCenter(sm.fetchArenaCenter());
        arena.getWorldBorder().setSize(sm.getBorderRadius());
        arena.setTime(18000L);
        scatterPlayers();
        startTimer();
        checkForPlayerCount();
        Bukkit.getLogger().info("Deathmatch phase has started successfully!");
    }

    @Override
    public void onDisable() {
        gracep.cancel();
        Bukkit.getLogger().info("Grace Period timer successfully disabled, handing over to EndGame...");
    }

    @Override
    public Phase next() {
        return new EndGame();
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
    public void onWorldDamage(EntityDamageEvent e) {
        if (prepbool) e.setCancelled(true);
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
        checkForPlayerCount();
    }
    @EventHandler
    public void onKill(PlayerDeathEvent e){
        e.getEntity();
        Player killed = e.getEntity();
        if (e.getEntity().getKiller() instanceof Player) {
            Player killer = e.getEntity().getKiller();
            killer.sendMessage(cm.getPrefix() + cm.getKill().replace("{player}",killed.getName()));
            killed.sendMessage(cm.getPrefix() + cm.getKilled().replace("{player}",killer.getName()));
        }
        else killed.sendMessage(cm.getPrefix() + cm.getKillednat());
        e.setCancelled(true);
        killed.setHealth(20);
        killed.getWorld().strikeLightningEffect(killed.getLocation());
        List<ItemStack> items = new ArrayList<>();
        for(int i = 0; i < killed.getInventory().getSize(); i++) items.add(killed.getInventory().getItem(i));
        killed.getInventory().clear();
        for(ItemStack item : items) if (item != null) killed.getWorld().dropItem(killed.getLocation(), item).setPickupDelay(20);
        items.clear();
        pm.transferToSpectators(killed);
        Bukkit.broadcast(Component.text(cm.getGlobalkill().replace("{players}", String.valueOf(pm.getRemainingPlayersList().size()))));
        checkForPlayerCount();
    }
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) { e.setCancelled(true); }
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }
    @EventHandler
    public void onLeafDecay(LeavesDecayEvent e){
        e.setCancelled(true);
    }
    void startTimer() {
        gracep = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    if (timer == 10) Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatchprep(timer))));
                    if (timer <= 5) {
                        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatchprep(timer))));
                    }
                    timer--;
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                    Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatchstart())));
                    prepbool = false;
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(),20L, 20L);
    }
    void scatterPlayers() {
        System.out.println("Scattering tributes...");
        Player[] players = pm.getRemainingPlayersList().toArray(new Player[0]);
        for(int i = 0; i<players.length; i++) players[i].teleport(sm.fetchCorrectedCoordinates().get(i));
        System.out.println("All tributes should now be scattered!");
    }
    void checkForPlayerCount() {
        if (pm.getRemainingPlayersList().size() == 1) Main.getPm().nextPhase();
    }
}
