package ru.dgrew.yaghgp.phases;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.dgrew.yaghgp.Main;
import ru.dgrew.yaghgp.Phase;
import ru.dgrew.yaghgp.managers.ChatManager;
import ru.dgrew.yaghgp.managers.PlayerManager;
import ru.dgrew.yaghgp.managers.SettingsManager;
import java.util.List;
import java.util.Random;

public class Deathmatch extends Phase {
    private int timer;
    private ChatManager cm;
    private PlayerManager pm;
    private SettingsManager sm;
    private boolean prepbool;
    private BukkitTask gracep;
    //region Phase Methods
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
        System.out.println("Deathmatch phase has started successfully!");
    }
    @Override
    public void onDisable() {
        gracep.cancel();
    }
    @Override
    public Phase next() {
        return new EndGame();
    }
    //endregion
    //region Phase Listeners
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (!e.getBlock().getType().name().endsWith("_LEAVES") && !(e.getBlock().getType().name().endsWith("FIRE")) && !(e.getBlock().getType().name().endsWith("GRASS"))) e.setCancelled(true);
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
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK)
            if (e.getClickedBlock().getType().name().startsWith("POTTED_") ||
                    e.getClickedBlock().getType() == Material.FLOWER_POT ||
                    e.getClickedBlock().getType().name().endsWith("_LOG"))
                e.setCancelled(true);
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        e.getPlayer().kickPlayer("Game already started!");
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        e.setQuitMessage(ChatColor.YELLOW + e.getPlayer().getName() + " has left!");
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
        killed.setHealth(20);
        killed.getWorld().strikeLightningEffect(killed.getLocation());
        pm.transferToSpectators(killed);
        e.setDeathMessage(cm.getGlobalkill().replace("{players}", String.valueOf(pm.getRemainingPlayersList().size())));
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
    //endregion
    //region Runnables
    void startTimer() {
        gracep = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    if (timer == 10) Bukkit.broadcastMessage(cm.getPrefix() + (cm.getDeathmatchprep(timer)));
                    if (timer <= 5) {
                        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        Bukkit.broadcastMessage(cm.getPrefix() + cm.getDeathmatchprep(timer));
                    }
                    timer--;
                } else {
                    for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
                    Bukkit.broadcastMessage(cm.getPrefix() + cm.getDeathmatchstart());
                    prepbool = false;
                    cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(),20L, 20L);
    }
    //endregion
    void scatterPlayers() {
        System.out.println("Scattering players...");
        Random random = new Random();
        List<Location> list = sm.fetchCorrectedCoordinates();
        int var;
        for (Player player : pm.getRemainingPlayersList()) {
            var = random.nextInt(list.size());
            player.teleport(list.get(var));
            list.remove(var);
        }
        System.out.println("All tributes should now be scattered!");
    }
    void checkForPlayerCount() {
        if (pm.getRemainingPlayersList().size() == 1) Main.getPm().nextPhase();
    }
}
