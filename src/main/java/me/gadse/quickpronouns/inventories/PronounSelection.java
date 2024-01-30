package me.gadse.quickpronouns.inventories;

import java.util.*;
import java.util.stream.Collectors;
import me.gadse.quickpronouns.Pronoun;
import me.gadse.quickpronouns.PronounPlayerData;
import me.gadse.quickpronouns.QuickPronouns;
import me.gadse.quickpronouns.messages.Message;
import me.gadse.quickpronouns.messages.Placeholder;
import me.gadse.quickpronouns.messages.Replacement;
import me.gadse.quickpronouns.messages.Sounds;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PronounSelection implements Listener {

    private final QuickPronouns plugin;
    private final Set<UUID> inventoryViewers = new HashSet<>();
    private final Map<Integer, Button> slotActions = new HashMap<>();
    private final Map<String, Integer> pronounIdToSlot = new HashMap<>();
    private final PronounPlayerData pronounPlayerData;

    public PronounSelection(QuickPronouns plugin, PronounPlayerData pronounPlayerData) {
        this.plugin = plugin;
        this.pronounPlayerData = pronounPlayerData;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private String inventoryTitle;
    private int inventorySize;

    private List<String> selectedPronounLore;
    private int fluidToggleSlot;
    private String fluidToggleEnabled;
    private String fluidToggleDisabled;

    public void reload() {
        inventoryViewers.forEach((uuid -> {
            Player inventoryViewer = plugin.getServer().getPlayer(uuid);
            if (inventoryViewer != null) {
                inventoryViewer.closeInventory();
            }
        }));
        inventoryViewers.clear();
        slotActions.clear();
        pronounIdToSlot.clear();

        inventoryTitle = plugin.getConfig().getString("gui.title", "Pronouns");
        inventorySize = plugin.getConfig().getInt("gui.size", 45);

        ItemStack closeItem = loadItemFromConfig("gui.close");
        ItemStack fluidToggleItem = loadItemFromConfig("gui.fluid_toggle");
        ItemStack removePronounItem = loadItemFromConfig("gui.none");
        ItemStack pronounItem = loadItemFromConfig("gui.pronoun");

        selectedPronounLore = plugin.getConfig().getStringList("gui.pronoun.selected_lore").stream()
                .map(plugin::color)
                .collect(Collectors.toList());
        fluidToggleEnabled = plugin.color(plugin.getConfig().getString("gui.fluid_toggle.enabled", "&aYes"));
        fluidToggleDisabled = plugin.color(plugin.getConfig().getString("gui.fluid_toggle.disabled", "&cNo"));

        ConfigurationSection layoutConfig = plugin.getConfig().getConfigurationSection("gui.layout");
        if (layoutConfig == null) {
            return;
        }

        layoutConfig.getKeys(false).forEach(configSlot -> {
            int slot;
            try {
                slot = Integer.parseInt(configSlot);
            } catch (NumberFormatException exc) {
                plugin.getLogger().info("'" + configSlot + " under 'gui.layout' is not a number.");
                return;
            }

            String action = layoutConfig.getString(configSlot);
            if (action == null || action.isEmpty()) {
                plugin.getLogger().info("The action under 'gui.layout." + configSlot + "' is not valid.");
                return;
            }

            switch (action) {
                case "none" -> slotActions.put(slot, new Button(removePronounItem, event -> {
                    String pronounId = pronounPlayerData.getPronounId(event.getWhoClicked());
                    if (pronounId.equals("none")) {
                        Sounds.GUI_CLICK_DENY.playSound(event.getWhoClicked());
                        return;
                    }

                    removePlayerPronouns(event.getWhoClicked());
                    updateInventoryItemsWithPlaceholders(event.getWhoClicked());
                    Sounds.GUI_CLICK.playSound(event.getWhoClicked());
                }));
                case "close" -> slotActions.put(slot, new Button(closeItem, event -> event.getWhoClicked()
                        .closeInventory()));
                case "fluid_toggle" -> {
                    slotActions.put(slot, new Button(fluidToggleItem, event -> {
                        toggleFluidReminders(event.getWhoClicked());
                        replaceFluidTogglePlaceholders(
                                event.getWhoClicked(), event.getView().getTopInventory());

                        Sounds.GUI_CLICK.playSound((Player) event.getWhoClicked());
                    }));
                    fluidToggleSlot = slot;
                }
                default -> {
                    Pronoun pronoun = plugin.getPronounById(action);
                    if (pronoun == null) {
                        plugin.getLogger().info("The action at 'gui.layout." + configSlot + "' is not a pronoun ID.");
                        return;
                    }
                    ItemStack pronounClone = pronounItem.clone();
                    pronounClone.setType(pronoun.getIcon());
                    ItemMeta itemMeta = pronounClone.getItemMeta();
                    if (itemMeta != null) {
                        if (itemMeta.hasDisplayName()) {
                            itemMeta.setDisplayName(
                                    itemMeta.getDisplayName().replaceAll("%pronouns_display%", pronoun.getDisplay()));
                        }

                        if (itemMeta.getLore() != null) {
                            itemMeta.setLore(itemMeta.getLore().stream()
                                    .map(line -> line.replaceAll("%pronouns_display%", pronoun.getDisplay()))
                                    .collect(Collectors.toList()));
                        }
                    }
                    pronounClone.setItemMeta(itemMeta);
                    slotActions.put(slot, new Button(pronounClone, event -> {
                        String pronounId = pronounPlayerData.getPronounId(event.getWhoClicked());
                        if (pronounId.equals(action)) {
                            Sounds.GUI_CLICK_DENY.playSound(event.getWhoClicked());
                            return;
                        }

                        setPronouns(event.getWhoClicked(), action);
                        updateInventoryItemsWithPlaceholders(event.getWhoClicked());
                        Sounds.GUI_CLICK.playSound(event.getWhoClicked());
                    }));
                    pronounIdToSlot.put(action, slot);
                }
            }
        });
    }

    public void openForPlayer(Player player) {
        if (inventoryViewers.contains(player.getUniqueId())) {
            return;
        }

        Inventory inventory = plugin.getServer().createInventory(null, inventorySize, inventoryTitle);
        slotActions.forEach((slot, button) -> inventory.setItem(slot, button.itemStack()));

        displayActivePronounOnItem(player, inventory);
        replaceFluidTogglePlaceholders(player, inventory);

        inventoryViewers.add(player.getUniqueId());
        Sounds.GUI_OPEN.playSound(player);
        player.openInventory(inventory);
    }

    public void setPronouns(HumanEntity player, String pronounId) {
        Pronoun pronoun = plugin.getPronounById(pronounId);
        if (pronoun == null) {
            return;
        }

        pronounPlayerData.setPronounId(player, pronounId);
        Message.PRONOUNS_SET.send(player, new Replacement(Placeholder.PRONOUNS_DISPLAY, pronoun.getDisplay()));
    }

    public void removePlayerPronouns(HumanEntity player) {
        pronounPlayerData.removePronouns(player);
        Message.PRONOUNS_REMOVED.send(player);
    }

    private void toggleFluidReminders(HumanEntity player) {
        boolean isFluidReminderEnabled = pronounPlayerData.hasFluidReminderEnabled(player);
        if (!isFluidReminderEnabled) {
            Message.GENDER_FLUID_REMINDER_ENABLE.send(player);
        } else {
            Message.GENDER_FLUID_REMINDER_DISABLE.send(player);
        }
        pronounPlayerData.setFluidReminder(player, !isFluidReminderEnabled);
    }

    private void updateInventoryItemsWithPlaceholders(HumanEntity player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        displayActivePronounOnItem(player, inventory);
        replaceFluidTogglePlaceholders(player, inventory);
    }

    private void displayActivePronounOnItem(HumanEntity player, Inventory inventory) {
        String previousPronounId = pronounPlayerData.getPreviousPronounId(player);
        String pronounId = pronounPlayerData.getPronounId(player);

        Integer previousPronounSlot = pronounIdToSlot.get(previousPronounId);
        if (previousPronounSlot != null) {
            Button previousPronounButton = slotActions.get(previousPronounSlot);
            if (previousPronounButton != null) {
                inventory.setItem(previousPronounSlot, previousPronounButton.itemStack());
            }
        }

        Integer pronounSlot = pronounIdToSlot.get(pronounId);
        if (pronounSlot == null) {
            return;
        }

        ItemStack pronounItem = inventory.getItem(pronounSlot);
        if (pronounItem == null
                || pronounItem.getItemMeta() == null
                || pronounItem.getItemMeta().getLore() == null) {
            return;
        }
        ItemMeta itemMeta = pronounItem.getItemMeta();
        itemMeta.setLore(selectedPronounLore);
        pronounItem.setItemMeta(itemMeta);
    }

    private void replaceFluidTogglePlaceholders(HumanEntity player, Inventory inventory) {
        Pronoun playerPronoun = plugin.getPronounById(pronounPlayerData.getPronounId(player));
        boolean isFluidReminderEnabled = pronounPlayerData.hasFluidReminderEnabled(player);

        Button fluidToggleButton = slotActions.get(fluidToggleSlot);
        if (fluidToggleButton == null) {
            return;
        }

        ItemStack fluidToggleItem = fluidToggleButton.itemStack().clone();
        ItemMeta itemMeta = fluidToggleItem.getItemMeta();
        if (itemMeta == null || itemMeta.getLore() == null) {
            return;
        }

        itemMeta.setLore(itemMeta.getLore().stream()
                .map(line -> line.replaceAll(
                                "%pronouns_display%", playerPronoun != null ? playerPronoun.getDisplay() : "none")
                        .replaceAll(
                                "%fluid_toggle_enabled%",
                                isFluidReminderEnabled ? fluidToggleEnabled : fluidToggleDisabled))
                .collect(Collectors.toList()));

        fluidToggleItem.setItemMeta(itemMeta);
        inventory.setItem(fluidToggleSlot, fluidToggleItem);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!inventoryViewers.contains(event.getWhoClicked().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
            return;
        }

        Button slotButton = slotActions.get(event.getSlot());
        if (slotButton == null) {
            return;
        }
        slotButton.clickConsumer().accept(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (inventoryViewers.contains(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (inventoryViewers.remove(event.getPlayer().getUniqueId())) {
            Sounds.GUI_CLOSE.playSound(event.getPlayer());
        }
    }

    private ItemStack loadItemFromConfig(String configPath) {
        String materialName =
                plugin.getConfig().getString(configPath + ".icon", "dirt").toUpperCase();
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            material = Material.DIRT;
            plugin.getLogger().info("The icon at '" + configPath + "' does not exist.");
        }

        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return itemStack;
        }

        String displayName = plugin.getConfig().getString(configPath + ".name", "");
        if (!displayName.isEmpty()) {
            itemMeta.setDisplayName(plugin.color(displayName));
        }

        itemMeta.setLore(plugin.getConfig().getStringList(configPath + ".lore").stream()
                .map(plugin::color)
                .collect(Collectors.toList()));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
