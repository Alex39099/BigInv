/*
 * Copyright (C) 2019-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.biginv.data;

import com.github.alexqp.biginv.listeners.BigInvHandler;
import com.github.alexqp.biginv.util.ArrayUtils;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.dataHandler.DataHandler;
import com.github.alexqp.commons.dataHandler.LoadSaveException;
import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BigInvContainer implements BigInvHandler {

    private final String activeInvName = "active";
    private final String backupInvName = "backup";

    private final JavaPlugin plugin;
    private DataHandler dataHandler;

    private final Map<UUID, BigInvHolder> invHolders = new HashMap<>();

    public BigInvContainer(JavaPlugin plugin)
        throws IllegalArgumentException {
        this.plugin = plugin;
        try {
            this.dataHandler = new DataHandler(plugin, "playerdata");
            this.loadInventories();
        } catch (LoadSaveException e) {
            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Could not create sub-directory. Please make sure your system allows to save files.");
            e.printStackTrace();
            plugin.onDisable();
        }
    }

    private void addInvHolder(Player owner, BigInvHolder holder) {
        invHolders.put(owner.getUniqueId(), holder);
        ConsoleMessage.debug(this.getClass(), plugin, "Added BigInvHolder for " + ConsoleMessage.getPlayerString(owner));
    }

    private void removeInvHolder(Player owner) {
        UUID ownerUUID = owner.getUniqueId();
        invHolders.remove(ownerUUID);
        ConsoleMessage.debug(this.getClass(), plugin, "Removed BigInvHolder for " + ConsoleMessage.getPlayerString(owner));
    }

    private void loadInventories() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.loadInventory(p);
        }
    }

    public void saveInventories() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.saveInventory(p);
        }
    }

    private void saveYmlFile(Player owner, YamlConfiguration ymlFile) {
        try {
            dataHandler.saveYmlFile(owner.getUniqueId().toString(), ymlFile);
            ConsoleMessage.debug(this.getClass(), plugin, "saved file for " + ConsoleMessage.getPlayerString(owner));
        } catch (LoadSaveException e) {
            ConsoleMessage.send(ConsoleErrorType.ERROR, plugin, "Could not save updated file for " + ConsoleMessage.getPlayerString(owner) + ". Please make sure your directory is writable. Printing stackTrace...");
            e.printStackTrace();
        }
    }


    @Override
    public BigInvHandlerReturnType loadInventory(Player owner) {
        BigInvHolder holder = BigInvHolder.get(owner);
        if (holder == null) {
            ConsoleMessage.debug(this.getClass(), plugin, "No load for " + ConsoleMessage.getPlayerString(owner) + " cause he does not have necessary permissions.");
            return BigInvHandlerReturnType.NO_HOLDER;
        }

        YamlConfiguration ymlFile = dataHandler.loadYmlFile(owner.getUniqueId().toString());
        ConfigChecker configChecker = new ConfigChecker(plugin, ymlFile);
        YmlInventory defYmlInv = new YmlInventory();

        YmlInventory ymlInv = configChecker.checkSerializable(ymlFile, activeInvName, ConsoleErrorType.NONE, defYmlInv, false);
        YmlInventory oldBackupYmlInv = configChecker.checkSerializable(ymlFile, backupInvName, ConsoleErrorType.NONE, new YmlInventory(), false);

        if (owner.hasPermission("biginv.backup.login")) {
            ymlInv.setContents(ArrayUtils.addAll(ymlInv.getContentsClone(), oldBackupYmlInv.clearContents()));
            ConsoleMessage.debug(this.getClass(), plugin, ConsoleMessage.getPlayerString(owner) + " had an old backup and the permission. Trying to add both into the BigInv...");
            // TODO backup should also be restored into the vanilla inventory if possible.
        }

        YmlInventory backupYmlInv = holder.setContents(ymlInv);
        this.addInvHolder(owner, holder);

        if (!backupYmlInv.isEmpty()) {
            if (!oldBackupYmlInv.isEmpty()) {
                backupYmlInv.setContents(ArrayUtils.addAll(backupYmlInv.getContentsClone(), oldBackupYmlInv.getContentsClone()));
                ConsoleMessage.debug(this.getClass(), plugin, ConsoleMessage.getPlayerString(owner) + " had an old backup. Merged both contents.");
            }
            ymlFile.set(activeInvName, new YmlInventory(holder.getInventory().getContents()));
            ymlFile.set(backupInvName, backupYmlInv);
            this.saveYmlFile(owner, ymlFile);
            return BigInvHandlerReturnType.NEEDED_BACKUP;
        }
        ConsoleMessage.debug(this.getClass(), plugin, "loaded BigInvHolder for " + ConsoleMessage.getPlayerString(owner));

        if (ymlInv == defYmlInv)
            return BigInvHandlerReturnType.PARTIAL_SUCCESS;

        return BigInvHandlerReturnType.SUCCESS;
    }

    @Override
    public BigInvHandlerReturnType saveInventory(Player owner) {
        return this.saveInventory(owner, true);
    }

    @Override
    public BigInvHandlerReturnType saveInventory(Player owner, boolean closeInv) {
        BigInvHolder holder = invHolders.get(owner.getUniqueId());

        if (holder == null) {
            ConsoleMessage.debug(this.getClass(), plugin, "No save for " + ConsoleMessage.getPlayerString(owner) + " cause there was no holder found.");
            return BigInvHandlerReturnType.NO_HOLDER;
        }

        if (closeInv && !holder.getInventory().getViewers().isEmpty()) {
            List<HumanEntity> opener = new ArrayList<>(holder.getInventory().getViewers());
            for (HumanEntity ent : opener) {
                ent.closeInventory();
                Bukkit.getPluginManager().callEvent(new BigInvForceCloseEvent(owner, ent));
            }
        }

        if (holder.isChanged()) {
            YamlConfiguration ymlFile = dataHandler.loadYmlFile(owner.getUniqueId().toString());
            ymlFile.set(activeInvName, new YmlInventory(holder.getInventory().getContents()));
            this.saveYmlFile(owner, ymlFile);
        } else {
            ConsoleMessage.debug(this.getClass(), plugin, ConsoleMessage.getPlayerString(owner) + " did not change anything in his inventory. Skipped saving.");
        }

        if (closeInv)
            this.removeInvHolder(owner);

        ConsoleMessage.debug(this.getClass(), plugin, "Saved BigInv for " + ConsoleMessage.getPlayerString(owner) + ", unload/closeInv == " + closeInv);
        return BigInvHandlerReturnType.SUCCESS;
    }

    @Override
    public BigInvHandlerReturnType openInventory(Player owner, Player viewer) {
        BigInvHolder holder = invHolders.get(owner.getUniqueId());
        if (holder == null) {
            return BigInvHandlerReturnType.NO_HOLDER;
        }

        if (!holder.getCanOpen(viewer)) {
            return BigInvHandlerReturnType.NO_PERMISSION;
        }

        if (!holder.hasValidInventory()) {
            return BigInvHandlerReturnType.NO_DATA;
        }
        viewer.openInventory(holder.getInventory());
        return BigInvHandlerReturnType.SUCCESS;
    }

    @Override
    public BigInvHandlerReturnType restoreBackup(Player owner, Player viewer) {
        BigInvHolder holder = invHolders.get(owner.getUniqueId());
        if (holder == null) {
            return BigInvHandlerReturnType.NO_HOLDER;
        }

        if (!holder.getCanRestoreBackup(viewer)) {
            return BigInvHandlerReturnType.NO_PERMISSION;
        }

        YamlConfiguration ymlFile = dataHandler.loadYmlFile(owner.getUniqueId().toString());
        ConfigChecker configChecker = new ConfigChecker(plugin, ymlFile);

        YmlInventory ymlInv = configChecker.checkSerializable(ymlFile, backupInvName, ConsoleErrorType.NONE, new YmlInventory(), false);
        if (ymlInv.isEmpty()) {
            return BigInvHandlerReturnType.NO_DATA;
        }

        BigInvHandlerReturnType returnType;

        List<ItemStack> notNullItems = new ArrayList<>();
        for (ItemStack item : ymlInv.getContentsClone()) {
            if (item != null)
                notNullItems.add(item);
        }

        Map<Integer, ItemStack> notAdded = viewer.getInventory().addItem(notNullItems.toArray(new ItemStack[0]));

        ymlInv.setContents(notAdded.values().toArray(new ItemStack[0]));
        if (ymlInv.isEmpty()) {
            ConsoleMessage.debug(this.getClass(), plugin, "Restoring of backup of " + ConsoleMessage.getPlayerString(owner) + " by " + ConsoleMessage.getPlayerString(viewer) + " was fully successful");
            returnType = BigInvHandlerReturnType.SUCCESS;
        } else {
            ConsoleMessage.debug(this.getClass(), plugin, "Restoring of backup of " + ConsoleMessage.getPlayerString(owner) + " by " + ConsoleMessage.getPlayerString(viewer) + " was partially successful");
            returnType = BigInvHandlerReturnType.PARTIAL_SUCCESS;
        }
        ymlInv.setContents(notAdded.values().toArray(new ItemStack[0]));
        ymlFile.set(backupInvName, ymlInv);
        this.saveYmlFile(owner, ymlFile);
        return returnType;
    }

    @Override
    public List<ItemStack> clearInventory(Player owner) {
        return this.clearInventory(owner, true);
    }

    @SuppressWarnings("WeakerAccess")
    public List<ItemStack> clearInventory(Player owner, boolean saveClear) {
        List<ItemStack> list = new ArrayList<>();
        BigInvHolder holder = invHolders.get(owner.getUniqueId());

        if (holder == null) {
            ConsoleMessage.debug(this.getClass(), plugin, "Could not clear inventory because no holder was found.");
            return list;
        }

        list.addAll(Arrays.asList(holder.getInventory().getContents()));
        holder.clearContents();

        if (saveClear) {
            this.saveInventory(owner, false);
        }

        ConsoleMessage.debug(this.getClass(), plugin, "Cleared inventory for player " + ConsoleMessage.getPlayerString(owner));
        return list;
    }
}