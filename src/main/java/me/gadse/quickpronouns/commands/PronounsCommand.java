package me.gadse.quickpronouns.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.gadse.quickpronouns.QuickPronouns;
import me.gadse.quickpronouns.inventories.PronounSelection;
import me.gadse.quickpronouns.messages.Message;
import me.gadse.quickpronouns.messages.Placeholder;
import me.gadse.quickpronouns.messages.Replacement;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class PronounsCommand implements CommandExecutor, TabCompleter {

    private final QuickPronouns plugin;
    private final PronounSelection pronounSelection;

    public PronounsCommand(QuickPronouns plugin, PronounSelection pronounSelection) {
        this.plugin = plugin;
        this.pronounSelection = pronounSelection;
        PluginCommand pronounsCommand = plugin.getCommand("pronouns");
        if (pronounsCommand != null) {
            pronounsCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subCommand = args.length > 0 ? args[0].toLowerCase() : "menu";

        switch (subCommand) {
            case "menu" -> {
                if (!sender.hasPermission("pronouns.command.pronouns.menu")) {
                    Message.NO_PERMISSION.send(sender);
                    return true;
                }

                if (args.length > 1 && sender.hasPermission("pronouns.command.pronouns.menu.others")) {
                    Player target = plugin.getServer().getPlayer(args[1]);
                    if (target == null) {
                        Message.TARGET_INVALID.send(sender, new Replacement(Placeholder.TARGET, args[1]));
                        return true;
                    }

                    pronounSelection.openForPlayer(target);
                    Message.MENU_OPENED_OTHER.send(sender);
                    return true;
                }

                if (!(sender instanceof Player player)) {
                    Message.PLAYERS_ONLY.send(sender);
                    return true;
                }
                pronounSelection.openForPlayer(player);
            }
            case "reload" -> {
                if (!sender.hasPermission("pronouns.command.pronouns.reload")) {
                    Message.NO_PERMISSION.send(sender);
                    return true;
                }

                plugin.reloadConfig();
                plugin.reload(sender);
            }
            default -> {
                if (!sender.hasPermission("pronouns.command.pronouns.help")) {
                    Message.NO_PERMISSION.send(sender);
                    return true;
                }

                return false;
            }
        }

        return true;
    }

    private final Set<String> subCommands = Set.of("menu", "reload", "help");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tabCompletions = new ArrayList<>();

        if (args.length == 1) {
            Set<String> subCommandsCopy = new HashSet<>(subCommands);
            subCommandsCopy.removeIf(subCommand -> !sender.hasPermission("pronouns.command.pronouns." + subCommand));
            StringUtil.copyPartialMatches(args[0], subCommandsCopy, tabCompletions);
        }

        if (args.length == 2
                && args[0].equalsIgnoreCase("menu")
                && sender.hasPermission("pronouns.command.pronouns.menu.others")) {
            // If we return null, the server will auto-complete player names.
            return null;
        }

        return tabCompletions;
    }
}
