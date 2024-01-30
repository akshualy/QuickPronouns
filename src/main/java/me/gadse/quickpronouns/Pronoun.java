package me.gadse.quickpronouns;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class Pronoun {

    private final String display;
    private final Material icon;

    public Pronoun(ConfigurationSection config) {
        if (config == null) {
            throw new IllegalArgumentException("A path under 'pronuns' was not a pronoun config.");
        }

        this.display = config.getString("display", "");
        if (display.isEmpty()) {
            throw new IllegalArgumentException("'display' is missing in '" + config.getCurrentPath() + "'");
        }

        String iconMaterial = config.getString("icon", "name_tag");
        this.icon = Material.getMaterial(iconMaterial.toUpperCase());
        if (this.icon == null) {
            throw new IllegalArgumentException("'icon' is invalid in '" + config.getCurrentPath() + "'");
        }
    }

    public String getDisplay() {
        return this.display;
    }

    public Material getIcon() {
        return this.icon;
    }
}
