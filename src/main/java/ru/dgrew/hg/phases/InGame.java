package ru.dgrew.hg.phases;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.Phase;
import ru.dgrew.hg.managers.ChatManager;
import ru.dgrew.hg.managers.LootManager;
import ru.dgrew.hg.managers.PlayerManager;

import java.util.ArrayList;
import java.util.List;

public class InGame extends Phase {
    private int timer;
    private ChatManager cm;
    private LootManager lm;
    private PlayerManager pm;
    private BukkitTask gameTimer;
    @Override
    public void onEnable() {
        timer = 900;
        cm = Main.getCm();
        lm = new LootManager();
        pm = Main.getPlm();
        pm.updatePlayersList();
        for (Player p : pm.getRemainingPlayersList()) p.setGameMode(GameMode.SURVIVAL);
        startTimer();
        Bukkit.getLogger().info("InGame phase has started successfully!");
    }

    @Override
    public void onDisable() {
        gameTimer.cancel();
        Bukkit.getLogger().info("Game timer successfully disabled, handing over to Deathmatch...");
    }
    @Override
    public Phase next() {
        return new Deathmatch();
    }
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
        Bukkit.getLogger().info("A player has DC'd! Removing from tributes list, current list size = " + pm.getRemainingPlayersList().size());
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
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null)
        {
            if (e.getAction().isRightClick()) {
                if (e.getClickedBlock().getType().name().startsWith("POTTED_") ||
                        e.getClickedBlock().getType() == Material.FLOWER_POT ||
                        e.getClickedBlock().getType() == Material.ITEM_FRAME ||
                        e.getClickedBlock().getType().name().endsWith("_LOG"))
                    e.setCancelled(true);
            }
            else if (e.getAction().isLeftClick())
                if (e.getClickedBlock().getType() == Material.PAINTING || e.getClickedBlock().getType() == Material.ITEM_FRAME)
                    e.setCancelled(true);
        }
    }
    @EventHandler
    public void onChestOpen(InventoryOpenEvent e) {
        if(e.getInventory().getHolder() instanceof Chest c){
            if (lm.getChestContents(c.getLocation()) == null) {
                c.getInventory().clear();
                lm.createRandomChest(c.getInventory());
                lm.storeChestContents(c.getLocation(), c.getInventory());
            }
        }
        if(e.getInventory().getHolder() instanceof DoubleChest c){
            Chest left = (Chest)c.getLeftSide();
            Chest right = (Chest)c.getRightSide();
            if (lm.getChestContents(c.getLocation()) == null &&
                    (lm.getChestContents(left.getLocation()) == null || lm.getChestContents(right.getLocation()) == null)) {
                c.getInventory().clear();
                left.getInventory().clear();
                right.getInventory().clear();
                lm.createRandomChest(c.getInventory());
                lm.createRandomChest(left.getInventory());
                lm.createRandomChest(right.getInventory());
                lm.storeChestContents(c.getLocation(), c.getInventory());
                lm.storeChestContents(left.getLocation(), left.getInventory());
                lm.storeChestContents(right.getLocation(), right.getInventory());
            }
        }
    }
    @EventHandler
    public void onChestClose(InventoryCloseEvent e){
        if(e.getInventory().getHolder() instanceof Chest c){
            lm.storeChestContents(c.getLocation(), c.getInventory());
        }
        if(e.getInventory().getHolder() instanceof DoubleChest c){
            Chest left = (Chest)c.getLeftSide();
            Chest right = (Chest)c.getRightSide();
            lm.storeChestContents(c.getLocation(), c.getInventory());
            lm.storeChestContents(left.getLocation(), left.getInventory());
            lm.storeChestContents(right.getLocation(), right.getInventory());
        }
    }
    @EventHandler
    public void onKill(PlayerDeathEvent e){
        e.getEntity();
        Player killed = e.getEntity();
        if (e.getEntity().getKiller() != null) {
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
    void startTimer() {
        gameTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (timer > 0) {
                    if (timer == 600 || timer == 540 || timer == 480 || timer == 450)
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getRefill(timer-420))));
                    if (timer == 420){
                        lm.refillAllChests();
                        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getRefillcommencing())));
                    }
                    if (timer == 300 || timer == 180 || timer == 120 || timer == 60)
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatch(timer))));
                    if (timer == 30 || timer == 15 || timer == 10)
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatch(timer))));
                    if (timer <= 5) {
                        for(Player p : Bukkit.getOnlinePlayers()) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                        Bukkit.broadcast(Component.text(cm.getPrefix()).append(Component.text(cm.getDeathmatch(timer))));
                    }
                    timer--;
                } else {
                    Main.getPm().nextPhase();
                }
            }
        }.runTaskTimer(Main.getInstance(),20L, 20L);
    }
    void checkForPlayerCount() {
        if (pm.getRemainingPlayersList().size() == 1) {
            timer = 0;
            return;
        }
        if (pm.getRemainingPlayersList().size() <= 3 && timer >= 30) {
            timer = 60;
            return;
        }
        if (pm.getRemainingPlayersList().size() <= 7) {
            timer = 480;
        }
    }
}
