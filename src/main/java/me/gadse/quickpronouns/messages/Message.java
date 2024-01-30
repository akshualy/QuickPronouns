package me.gadse.quickpronouns.messages;

import me.gadse.quickpronouns.QuickPronouns;
import org.bukkit.command.CommandSender;

public enum Message {
    PREFIX,
    RELOAD,
    NO_PERMISSION,
    PLAYERS_ONLY,
    TARGET_INVALID,
    MENU_OPENED_OTHER,
    PRONOUNS_SET,
    PRONOUNS_REMOVED,
    PRONOUNS_REMOVED_OTHER,
    GENDER_FLUID_REMINDER,
    GENDER_FLUID_REMINDER_ENABLE,
    GENDER_FLUID_REMINDER_DISABLE;

    private String message;
    private boolean isEmpty = false;

    public void loadMessage(QuickPronouns plugin) {
        String message = plugin.getConfig().getString("messages." + name().toLowerCase());
        if (message == null || message.isEmpty()) {
            isEmpty = true;
            return;
        }

        this.message = plugin.color(message);
    }

    public void send(CommandSender target, Replacement... placeholders) {
        if (isEmpty) {
            return;
        }

        String messageCopy = message;
        for (Replacement placeholder : placeholders) {
            messageCopy = placeholder.placeholder().apply(messageCopy, placeholder.replacement());
        }

        target.sendMessage(PREFIX.message + messageCopy);
    }
}
