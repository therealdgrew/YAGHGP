package ru.dgrew.yaghgp.phases;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.dgrew.yaghgp.Main;
import ru.dgrew.yaghgp.Phase;
import ru.dgrew.yaghgp.managers.ChatManager;
import ru.dgrew.yaghgp.managers.PlayerManager;
import ru.dgrew.yaghgp.managers.SettingsManager;

import java.util.ArrayList;
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
        if (e.getItemInHand() == new ItemStack(Material.FLINT_AND_STEEL)) e.setCancelled(true);
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
                    e.getClickedBlock().getType().name().endsWith("_LOG") ||
                    e.getClickedBlock().getType().name().endsWith("_WOOD"))
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
    public void onDeath(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player killed = (Player)e.getEntity();
            if (killed.getHealth() <= e.getFinalDamage()) {
                e.setCancelled(true);
                onDeath(killed, e.getDamager());
            }
        }
    }
    @EventHandler
    public void onWorldDeath(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player killed = (Player)e.getEntity();
            if (!e.getCause().toString().startsWith("ENTITY_") && !e.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
                if (killed.getHealth() <= e.getFinalDamage()) {
                    e.setCancelled(true);
                    onDeath(killed, null);
                }
            }
        }
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
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldDamage(EntityDamageEvent e) {
        if (prepbool) e.setCancelled(true);
    }
    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent e) { e.setCancelled(true); }
    @EventHandler
    public void onHangingInteract(PlayerInteractEntityEvent e) { e.setCancelled(true); }
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
    void onDeath(Player killed, Entity killerent) {
        Player killer;
        if (killerent instanceof Player) {
            killer = (Player)killerent;
            killer.sendMessage(cm.getPrefix() + cm.getKill().replace("{player}",killed.getName()));
            killed.sendMessage(cm.getPrefix() + cm.getKilled().replace("{player}",killer.getName()));
        }
        else if (killerent instanceof Projectile) {
            Projectile pj = (Projectile)killerent;
            killer = (Player)pj.getShooter();
            killer.sendMessage(cm.getPrefix() + cm.getKill().replace("{player}",killed.getName()));
            killed.sendMessage(cm.getPrefix() + cm.getKilled().replace("{player}",killer.getName()));
        }
        else killed.sendMessage(cm.getPrefix() + cm.getKillednat());
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack i : killed.getInventory()) items.add(i);
        killed.getInventory().clear();
        for (ItemStack i : items) if (i != null) killed.getWorld().dropItem(killed.getLocation(), i).setPickupDelay(20);
        killed.setHealth(20);
        killed.getWorld().strikeLightningEffect(killed.getLocation());
        pm.transferToSpectators(killed);
        checkForPlayerCount();
        Bukkit.broadcastMessage(cm.getGlobalkill().replace("{players}", String.valueOf(pm.getRemainingPlayersList().size())));
    }
}
