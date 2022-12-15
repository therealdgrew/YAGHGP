package ru.dgrew.hg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.dgrew.hg.Main;
import ru.dgrew.hg.managers.ChatManager;

public class Start implements CommandExecutor {
    ChatManager cm;
    public Start() {
        cm = Main.getCm();
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("hg.start")) sender.sendMessage(cm.getPerm());
        else {
            Main.getPm().nextPhase();
            sender.sendMessage(cm.getPrefix() + cm.getStarted());
            return true;
        }
        return false;
    }
}
