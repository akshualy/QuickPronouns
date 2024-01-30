package me.gadse.quickpronouns.inventories;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record Button(ItemStack itemStack, Consumer<InventoryClickEvent> clickConsumer) {}
