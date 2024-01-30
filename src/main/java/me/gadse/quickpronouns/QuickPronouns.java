package me.gadse.quickpronouns;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.gadse.quickpronouns.commands.PronounsCommand;
import me.gadse.quickpronouns.inventories.PronounSelection;
import me.gadse.quickpronouns.listeners.PlayerJoin;
import me.gadse.quickpronouns.messages.Message;
import me.gadse.quickpronouns.messages.Sounds;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuickPronouns extends JavaPlugin {

    private final Map<String, Pronoun> pronounIdToPronoun = new HashMap<>();

    private PronounPlayerData pronounPlayerData;
    private PronounSelection pronounSelection;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().warning("Can not create the plugin folder. Please check read/write permissions.");
        }
        saveDefaultConfig();

        pronounPlayerData = new PronounPlayerData(this);
        this.pronounSelection = new PronounSelection(this, pronounPlayerData);
        new PronounsCommand(this, pronounSelection);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            PlaceholderIntegration placeholderIntegration = new PlaceholderIntegration(this);
            placeholderIntegration.register();
        }

        new PlayerJoin(this, pronounPlayerData);

        reload(null);
    }

    public void reload(CommandSender sender) {
        pronounIdToPronoun.clear();

        ConfigurationSection pronounConfig = getConfig().getConfigurationSection("pronouns");
        if (pronounConfig == null) {
            if (sender != null) {
                sender.sendMessage("'pronouns' was missing in 'config.yml'. The plugin will not work.");
            } else {
                getLogger().warning("'pronouns' was missing in 'config.yml'. The plugin will not work.");
            }
            return;
        }

        pronounConfig.getKeys(false).forEach(pronounId -> {
            try {
                pronounIdToPronoun.put(pronounId, new Pronoun(pronounConfig.getConfigurationSection(pronounId)));
            } catch (IllegalArgumentException exc) {
                if (sender != null) {
                    sender.sendMessage(exc.getMessage());
                } else {
                    getLogger().warning(exc.getMessage());
                }
            }
        });

        for (Message message : Message.values()) {
            message.loadMessage(this);
        }

        for (Sounds sound : Sounds.values()) {
            sound.loadSound(this);
        }

        pronounSelection.reload();

        if (sender != null) {
            Message.RELOAD.send(sender);
        }
    }

    @Override
    public void onDisable() {
        pronounIdToPronoun.clear();
    }

    public Pronoun getPronounById(String pronounId) {
        return pronounIdToPronoun.get(pronounId);
    }

    public String getPronounDisplayForPlayer(Player player) {
        return getPronounDisplayForPlayer(player, "");
    }

    public String getPronounDisplayForPlayer(Player player, String defaultPronounDisplay) {
        Pronoun pronoun = pronounIdToPronoun.get(pronounPlayerData.getPronounId(player));
        if (pronoun == null) {
            return defaultPronounDisplay;
        }

        return pronoun.getDisplay();
    }

    private final Pattern HEX_PATTERN = Pattern.compile("&(#[0-9a-fA-F]{6})");

    public String color(String text) {
        if (text == null || text.isEmpty()) return "";

        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        while (hexMatcher.find()) {
            text = text.replace(
                    hexMatcher.group(0), ChatColor.of(hexMatcher.group(1)).toString());
            hexMatcher = HEX_PATTERN.matcher(text);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
