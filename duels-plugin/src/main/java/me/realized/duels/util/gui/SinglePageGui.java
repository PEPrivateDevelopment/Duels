package me.realized.duels.util.gui;

import me.realized.duels.util.StringUtil;
import me.realized.duels.util.inventory.InventoryBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SinglePageGui<P extends JavaPlugin> extends AbstractGui<P> {

    protected final Inventory inventory;

    public SinglePageGui(final P plugin, final String title, final int rows) {
        super(plugin);
        this.inventory = InventoryBuilder.of(StringUtil.color(title), rows * 9).build();
    }

    protected void set(final int slot, final Button<P> button) {
        set(inventory, slot, button);
    }

    protected void set(final int from, final int to, final int height, final Button<P> button) {
        set(inventory, from, to, height, button);
    }

    @Override
    public void open(final Player... players) {
        for (final Player player : players) {
            update(player);
            player.openInventory(inventory);
        }
    }

    @Override
    public boolean isPart(final Inventory inventory) {
        return inventory.equals(this.inventory);
    }

    @Override
    public boolean hasViewers() {
        return !inventory.getViewers().isEmpty();
    }

    @Override
    public void on(final Player player, final Inventory top, final InventoryClickEvent event) {
        final Inventory clicked = event.getClickedInventory();

        if (clicked == null) {
            return;
        }

        final ItemStack item = event.getCurrentItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (!clicked.equals(top)) {
            return;
        }

        final Button<P> button = get(inventory, event.getSlot());

        if (button == null) {
            return;
        }

        button.onClick(player);
    }
}