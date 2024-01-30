package me.gadse.quickpronouns;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class PronounPlayerData {

    private final NamespacedKey pronounNamespace;
    private final NamespacedKey previousPronounNamespace;
    private final NamespacedKey fluidReminderNamespace;

    public PronounPlayerData(JavaPlugin plugin) {
        this.pronounNamespace = new NamespacedKey(plugin, "pronoun-id");
        this.previousPronounNamespace = new NamespacedKey(plugin, "previous-pronoun-id");
        this.fluidReminderNamespace = new NamespacedKey(plugin, "fluid-reminder");
    }

    public String getPronounId(HumanEntity player) {
        return player.getPersistentDataContainer().getOrDefault(pronounNamespace, PersistentDataType.STRING, "none");
    }

    public void setPronounId(HumanEntity player, String pronounId) {
        player.getPersistentDataContainer()
                .set(previousPronounNamespace, PersistentDataType.STRING, getPronounId(player));
        player.getPersistentDataContainer().set(pronounNamespace, PersistentDataType.STRING, pronounId);
    }

    public void removePronouns(HumanEntity player) {
        player.getPersistentDataContainer()
                .set(previousPronounNamespace, PersistentDataType.STRING, getPronounId(player));
        player.getPersistentDataContainer().remove(pronounNamespace);
    }

    public String getPreviousPronounId(HumanEntity player) {
        return player.getPersistentDataContainer()
                .getOrDefault(previousPronounNamespace, PersistentDataType.STRING, "none");
    }

    public boolean hasFluidReminderEnabled(HumanEntity player) {
        return player.getPersistentDataContainer()
                .getOrDefault(fluidReminderNamespace, PersistentDataType.BOOLEAN, false);
    }

    public void setFluidReminder(HumanEntity player, boolean enabled) {
        player.getPersistentDataContainer().set(fluidReminderNamespace, PersistentDataType.BOOLEAN, enabled);
    }
}
