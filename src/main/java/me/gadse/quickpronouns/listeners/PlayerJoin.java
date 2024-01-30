package me.gadse.quickpronouns.listeners;

import me.gadse.quickpronouns.PronounPlayerData;
import me.gadse.quickpronouns.QuickPronouns;
import me.gadse.quickpronouns.messages.Message;
import me.gadse.quickpronouns.messages.Placeholder;
import me.gadse.quickpronouns.messages.Replacement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final QuickPronouns plugin;
    private final PronounPlayerData pronounPlayerData;

    public PlayerJoin(QuickPronouns plugin, PronounPlayerData pronounPlayerData) {
        this.plugin = plugin;
        this.pronounPlayerData = pronounPlayerData;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (pronounPlayerData.hasFluidReminderEnabled(event.getPlayer())) {
            plugin.getServer()
                    .getScheduler()
                    .runTaskLater(
                            plugin,
                            () -> {
                                Message.GENDER_FLUID_REMINDER.send(
                                        event.getPlayer(),
                                        new Replacement(
                                                Placeholder.PRONOUNS_DISPLAY,
                                                plugin.getPronounDisplayForPlayer(event.getPlayer(), "none")));
                            },
                            100L);
        }
    }
}
