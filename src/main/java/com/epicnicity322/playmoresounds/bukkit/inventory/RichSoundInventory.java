package com.epicnicity322.playmoresounds.bukkit.inventory;

import com.epicnicity322.epicpluginlib.bukkit.lang.MessageSender;
import com.epicnicity322.epicpluginlib.core.config.PluginConfig;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.sound.RichSound;
import com.epicnicity322.playmoresounds.bukkit.sound.Sound;
import com.epicnicity322.playmoresounds.bukkit.util.VersionUtils;
import com.epicnicity322.playmoresounds.core.config.Configurations;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

public class RichSoundInventory implements PMSInventory, Listener
{
    private static final @NotNull PluginConfig config = Configurations.CONFIG.getPluginConfig();
    private static final @NotNull MessageSender lang = PlayMoreSounds.getMessageSender();
    private static final @NotNull Pattern spaceRegex = Pattern.compile(" ");
    private static NamespacedKey type;
    private final @NotNull RichSound richSound;
    private final @NotNull Inventory inventory;
    private final @NotNull HashSet<HumanEntity> openInventories = new HashSet<>();

    public RichSoundInventory()
    {
        this(new RichSound(lang.get("Rich Sound.Default Name"), true, false, null));
    }

    public RichSoundInventory(@NotNull RichSound richSound)
    {
        if (!VersionUtils.hasPersistentData())
            throw new UnsupportedOperationException("This class only works with bukkit 1.14+.");

        if (PlayMoreSounds.getInstance() == null)
            throw new IllegalStateException("PlayMoreSounds is not loaded.");

        if (type == null)
            type = new NamespacedKey(PlayMoreSounds.getInstance(), "type");

        PlayMoreSounds.addOnDisableRunnable(() -> {
            openInventories.forEach(HumanEntity::closeInventory);
            openInventories.clear();
        });

        this.richSound = richSound;

        inventory = Bukkit.createInventory(null, 54, lang.getColored("Editor.GUI.Rich Sound.Title")
                .replace("<name>", richSound.getName()));

        if (richSound.isEnabled())
            inventory.setItem(1, Items.ENABLED.getItemStack());
        else
            inventory.setItem(1, Items.DISABLED.getItemStack());

        inventory.setItem(4, Items.NAME.getItemStack());
        inventory.setItem(7, Items.CANCELLABLE.getItemStack());
        inventory.setItem(48, Items.CANCEL.getItemStack());
        inventory.setItem(50, Items.DONE.getItemStack());

        Collection<Sound> sounds = richSound.getChildSounds();

        if (sounds.isEmpty()) {
            inventory.setItem(31, Items.ADD_NEW_SOUND.getItemStack());
        } else {
            inventory.setItem(22, Items.ADD_NEW_SOUND.getItemStack());
            boolean moreThanOnePage = sounds.size() > 9;

            if (moreThanOnePage)
                inventory.setItem(35, Items.NEXT_PAGE.getItemStack());

            int count = 1;
            int end = moreThanOnePage ? 35 : 36;

            for (int i = 27; i < end; ++i)
                if (count++ <= sounds.size())
                    inventory.setItem(i, Items.SOUND.getItemStack());
                else
                    break;
        }
    }

    public @NotNull RichSound getRichSound()
    {
        return richSound;
    }

    private void animate()
    {

    }

    @Override
    public @NotNull PMSInventoryItem[] getItems()
    {
        return Items.values();
    }

    @Override
    public @NotNull Inventory getInventory()
    {
        return inventory;
    }

    @Override
    public void openInventory(@NotNull HumanEntity humanEntity)
    {
        Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
        humanEntity.openInventory(inventory);
        openInventories.add(humanEntity);
    }

    @EventHandler
    public final void onInventoryClick(InventoryClickEvent event)
    {
        ItemStack itemStack = event.getCurrentItem();

        if (itemStack != null) {
            HumanEntity humanEntity = event.getWhoClicked();

            if (openInventories.contains(humanEntity)) {
                event.setResult(Event.Result.DENY);

                ItemMeta meta = itemStack.getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();

                Items.valueOf(container.get(type, PersistentDataType.STRING)).onClick(this, event);
            }
        }
    }

    @EventHandler
    public final void onInventoryClose(InventoryCloseEvent event)
    {
        openInventories.remove(event.getPlayer());

        if (openInventories.isEmpty())
            HandlerList.unregisterAll(this);
    }

    public enum Items implements PMSInventoryItem
    {
        ADD_NEW_SOUND("Add New Sound"),//, (inventory, event) -> new SoundInventory(inventory, event.getCurrentItem().getType()).openInventory(event.getWhoClicked())),
        CANCEL("Cancel", (inventory, event) -> event.getWhoClicked().closeInventory()),
        CANCELLABLE("Cancellable", (inventory, event) -> inventory.getRichSound().setCancellable(
                !inventory.getRichSound().isCancellable())),
        DELAY("Loop Start Delay"),
        DISABLED("Disabled"),
        DONE("Done"),
        ENABLED("Enabled"),
        NAME("Name"),
        NEXT_PAGE("Next Page"),
        PERIOD("Loop Period"),
        PREVIOUS_PAGE("Previous Page"),
        SEPARATOR("Separator"),
        SOUND("Sound");

        private final @NotNull String name;
        private final @NotNull String configPath;
        private final @NotNull String langPath;
        private final @NotNull String id = name();
        private final @Nullable OnClickRunnable onClickRunnable;

        Items(@NotNull String name)
        {
            this(name, null);
        }

        Items(@NotNull String name, @Nullable OnClickRunnable onClickRunnable)
        {
            this.name = name;
            configPath = "Inventories.List." + name + " Item";
            langPath = "List.GUI." + name;
            this.onClickRunnable = onClickRunnable;
        }

        @Override
        public @NotNull String getName()
        {
            return name;
        }

        @Override
        public @NotNull String getConfigPath()
        {
            return configPath;
        }

        public @NotNull String getLangPath()
        {
            return langPath;
        }

        @Override
        public @NotNull String getId()
        {
            return id;
        }

        @Override
        public @NotNull ItemStack getItemStack()
        {
            return null;
        }

        private void onClick(RichSoundInventory richSoundInventory, InventoryClickEvent event)
        {
            if (onClickRunnable != null) {
                onClickRunnable.run(richSoundInventory, event);
            }
        }
    }

    private interface OnClickRunnable
    {
        void run(RichSoundInventory richSoundInventory, InventoryClickEvent event);
    }
}
