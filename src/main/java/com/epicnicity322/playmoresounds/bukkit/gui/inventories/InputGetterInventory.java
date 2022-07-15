/*
 * PlayMoreSounds - A bukkit plugin that manages and plays sounds.
 * Copyright (C) 2022 Christiano Rangel
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.epicnicity322.playmoresounds.bukkit.gui.inventories;

import com.epicnicity322.epicpluginlib.bukkit.reflection.ReflectionUtil;
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.PackageType;
import com.epicnicity322.epicpluginlib.bukkit.reflection.type.SubPackageType;
import com.epicnicity322.playmoresounds.bukkit.PlayMoreSounds;
import com.epicnicity322.playmoresounds.bukkit.gui.InventoryUtils;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.ContainerAccess;
import net.minecraft.world.inventory.ContainerAnvil;
import net.minecraft.world.inventory.Containers;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class InputGetterInventory implements Listener {
    private static final @NotNull Method method_Container_getBukkitView = Objects.requireNonNull(ReflectionUtil.getMethod(Container.class, "getBukkitView"));
    private static final @NotNull Class<?> class_CraftPlayer = Objects.requireNonNull(ReflectionUtil.getClass("CraftPlayer", SubPackageType.ENTITY));
    private static final @NotNull Method method_CraftPlayer_getHandle = Objects.requireNonNull(ReflectionUtil.getMethod(class_CraftPlayer, "getHandle"));
    private static final @NotNull Class<?> class_CraftWorld = Objects.requireNonNull(ReflectionUtil.getClass("CraftWorld", PackageType.CRAFTBUKKIT));
    private static final @NotNull Method method_CraftWorld_getHandle = Objects.requireNonNull(ReflectionUtil.getMethod(class_CraftWorld, "getHandle"));
    private static final @NotNull Class<?> class_CraftInventoryPlayer = Objects.requireNonNull(ReflectionUtil.getClass("CraftInventoryPlayer", SubPackageType.INVENTORY));
    private static final @NotNull Method method_CraftInventoryPlayer_getInventory = Objects.requireNonNull(ReflectionUtil.getMethod(class_CraftInventoryPlayer, "getInventory"));
    private static final @NotNull Field field_EntityPlayer_playerConnection = Objects.requireNonNull(ReflectionUtil.findFieldByType(EntityPlayer.class, PlayerConnection.class));
    private static final @NotNull Method method_PlayerConnection_sendPacket = Objects.requireNonNull(ReflectionUtil.findMethodByParameterTypes(PlayerConnection.class, Packet.class));
    private static final @NotNull Method method_EntityPlayer_addSlotListener = Objects.requireNonNull(ReflectionUtil.findMethodByParameterTypes(EntityPlayer.class, Container.class));
    private static final @NotNull Field field_EntityHuman_activeContainer = Objects.requireNonNull(ReflectionUtil.findFieldByType(EntityHuman.class, Container.class));
    private static final @NotNull Containers<?> containerAnvilType = Objects.requireNonNull(findContainersAnvilType());

    private final @NotNull UUID playerId;
    private final @NotNull AnvilContainer inventory;
    private final @NotNull Consumer<String> inputConsumer;
    private final @NotNull AtomicBoolean open = new AtomicBoolean(false);

    public InputGetterInventory(@NotNull Player player, @NotNull String title, @NotNull Consumer<String> inputConsumer) {
        try {
            this.inventory = new AnvilContainer(entityPlayer(player).nextContainerCounter(), player, title);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.playerId = player.getUniqueId();
        this.inputConsumer = inputConsumer;

        try {
            Inventory bukkitInventory = ((InventoryView) method_Container_getBukkitView.invoke(inventory)).getTopInventory();
            bukkitInventory.setItem(0, InventoryUtils.getItemStack("Input Getter Inventory.Input Item"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Containers<?> findContainersAnvilType() {
        for (Field f : Containers.class.getFields()) {
            if (f.getGenericType().getTypeName().equals("net.minecraft.world.inventory.Containers<net.minecraft.world.inventory.ContainerAnvil>")) {
                try {
                    return (Containers<?>) f.get(null);
                } catch (Exception ignored) {
                }
                break;
            }
        }
        return null;
    }

    private static EntityPlayer entityPlayer(Player player) {
        if (player == null) return null;
        try {
            return (EntityPlayer) method_CraftPlayer_getHandle.invoke(player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static net.minecraft.world.level.World nmsWorld(World world) {
        try {
            return (net.minecraft.world.level.World) method_CraftWorld_getHandle.invoke(world);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static net.minecraft.world.entity.player.PlayerInventory nmsInventory(PlayerInventory inventory) {
        try {
            return (net.minecraft.world.entity.player.PlayerInventory) method_CraftInventoryPlayer_getInventory.invoke(inventory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void openInventory() {
        if (PlayMoreSounds.getInstance() == null) throw new IllegalStateException("PlayMoreSounds is not loaded.");

        Player bukkitPlayer = Bukkit.getPlayer(playerId);

        if (bukkitPlayer == null || !bukkitPlayer.isOnline()) {
            open.set(false);
            return;
        }

        if (open.get()) {
            return;
        } else {
            open.set(true);
            // Checking if an inventory was opened before and should be closed.
            if (bukkitPlayer.getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
                bukkitPlayer.closeInventory();
            }
        }

        EntityPlayer player = entityPlayer(bukkitPlayer);

        // Sending open inventory packet
        try {
            method_PlayerConnection_sendPacket.invoke(
                    field_EntityPlayer_playerConnection.get(player)
                    , new PacketPlayOutOpenWindow(inventory.containerId, containerAnvilType, inventory.title));
            field_EntityHuman_activeContainer.set(player, inventory);
            // Registering slot listener and events
            method_EntityPlayer_addSlotListener.invoke(player, inventory);
            Bukkit.getPluginManager().registerEvents(this, PlayMoreSounds.getInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void closeInventory() {
        Player player = Bukkit.getPlayer(playerId);

        if (player == null || !player.isOnline()) {
            HandlerList.unregisterAll(this);
            open.set(false);
        } else {
            player.closeInventory();
        }
    }

    public boolean isOpen() {
        return open.get();
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    private void onInventoryClick(InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();

        if (!player.getUniqueId().equals(playerId)) return;

        event.setCancelled(true);

        if (event.getRawSlot() != 2) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        player.closeInventory();
        inputConsumer.accept(clicked.getItemMeta().getDisplayName());
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getPlayer().getUniqueId().equals(playerId)) return;
        HandlerList.unregisterAll(this);
        open.set(false);
    }

    private static final class AnvilContainer extends ContainerAnvil {
        private static final @NotNull Method method_ContainerAccess_at = Objects.requireNonNull(ReflectionUtil.findMethodByParameterTypes(ContainerAccess.class, net.minecraft.world.level.World.class, BlockPosition.class));
        private static final @NotNull Method method_IChatBaseComponent_string = Objects.requireNonNull(ReflectionUtil.findMethodByParameterTypes(IChatBaseComponent.class, true, String.class));
        private final int containerId;
        private final @NotNull IChatBaseComponent title;

        private AnvilContainer(int containerId, HumanEntity entity, String title) throws InvocationTargetException, IllegalAccessException {
            super(containerId
                    , nmsInventory(entity.getInventory())
                    , (ContainerAccess) method_ContainerAccess_at.invoke(null,
                            nmsWorld(entity.getWorld())
                            , new BlockPosition(0, 0, 0)));

            super.checkReachable = false;
            this.containerId = containerId;
            this.title = (IChatBaseComponent) method_IChatBaseComponent_string.invoke(null, title);
            setTitle(this.title);
        }

        @Override
        protected void a(@NotNull EntityHuman player, @NotNull IInventory container) {
        }

        @Override
        public void b(@NotNull EntityHuman player) {
        }
    }
}
