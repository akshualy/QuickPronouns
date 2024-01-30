package me.gadse.quickpronouns;

import java.util.Set;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderIntegration extends PlaceholderExpansion {

    private final QuickPronouns plugin;

    public PlaceholderIntegration(QuickPronouns plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pronouns";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        Set<String> pronounParameters = Set.of(params.split("_"));

        StringBuilder fullPronounDisplay = new StringBuilder();
        if (pronounParameters.contains("display")) {
            String pronounDisplay = plugin.getPronounDisplayForPlayer(player);

            if (pronounDisplay.isEmpty()) {
                return "";
            }

            String[] displayParts = pronounDisplay.split("/");
            if (pronounParameters.contains("capitalized")) {
                for (int i = 0; i < displayParts.length; i++) {
                    displayParts[i] = displayParts[i].substring(0, 1).toUpperCase() + displayParts[i].substring(1);
                }
            } else if (pronounParameters.contains("lowercase")) {
                for (int i = 0; i < displayParts.length; i++) {
                    displayParts[i] = displayParts[i].toLowerCase();
                }
            }
            pronounDisplay = String.join("/", displayParts);

            fullPronounDisplay.append(pronounDisplay);
        } else {
            return "";
        }

        if (pronounParameters.contains("parentheses")) {
            fullPronounDisplay.insert(0, "(");
            fullPronounDisplay.append(")");
        }

        if (pronounParameters.contains("front-spaced")) {
            fullPronounDisplay.insert(0, " ");
        }

        if (pronounParameters.contains("back-spaced")) {
            fullPronounDisplay.append(" ");
        }

        return fullPronounDisplay.toString();
    }
}
