package me.gadse.quickpronouns.messages;

import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public enum Sounds {
    GUI_CLICK,
    GUI_CLICK_DENY,
    GUI_OPEN,
    GUI_CLOSE;

    private org.bukkit.Sound sound;
    private SoundCategory soundCategory;
    private float volume;
    private float pitch;

    public void loadSound(JavaPlugin plugin) {
        String configPath = "sounds." + name().toLowerCase();

        String soundName = plugin.getConfig().getString(configPath + ".sound");
        if (soundName == null || soundName.isEmpty()) {
            plugin.getLogger().info("Sound at '%s' is not configured, the sound will not play.".formatted(configPath));
            this.sound = null;
            return;
        }

        try {
            sound = org.bukkit.Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger()
                    .warning("Sound '%s' at '%s' does not exist. Falling back to default."
                            .formatted(soundName, configPath));
            sound = org.bukkit.Sound.UI_BUTTON_CLICK;
        }

        String categoryName = plugin.getConfig().getString(configPath + ".category", "MASTER");
        try {
            soundCategory = SoundCategory.valueOf(categoryName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            plugin.getLogger()
                    .warning("Category '%s' at '%s' does not exist. Falling back to default."
                            .formatted(categoryName, configPath));
            soundCategory = SoundCategory.MASTER;
        }

        volume = (float) plugin.getConfig().getDouble(configPath + ".volume", 1.0);

        pitch = (float) plugin.getConfig().getDouble(configPath + ".volume", 1.0);
    }

    public void playSound(HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player player)) {
            return;
        }

        playSound(player);
    }

    public void playSound(Player player) {
        player.playSound(player.getLocation(), sound, soundCategory, volume, pitch);
    }
}
