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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BigInvHolder implements InventoryHolder {

    // ALSO USED IN COMMANDS!!!
    private static final String[] permissions = {"biginv.size.", "biginv.see", "biginv.see.other", "biginv.change", "biginv.change.other", "biginv.backup", "biginv.backup.other"};

    public static List<String> getSeePermissions() {
        List<String> list = new ArrayList<>();
        list.add(permissions[1]);
        list.add(permissions[2]);
        return list;
    }

    public static List<String> getBackupPermissions() {
        List<String> list = new ArrayList<>();
        list.add(permissions[5]);
        list.add(permissions[6]);
        return list;
    }

    private static int getAllowedInvSize(Player p) {
        int maxRows = 6;
        if (p.hasPermission(permissions[0] + "*")) {
            return maxRows * 9;
        }
        for (int i = maxRows; i >= 0; i--) {
            if (p.hasPermission(permissions[0] + i)) {
                return i * 9;
            }
        }
        return 0;
    }

    public static String invTitle = "BigInv";

    private final Player owner;
    private final int size;
    private final Inventory inv;

    private boolean isChanged = false;

    private BigInvHolder(Player owner, int allowedSize) throws IllegalArgumentException {
        this.owner = owner;
        this.size = allowedSize;
        if (allowedSize != 0) {
            this.inv = Bukkit.createInventory(this, allowedSize, this.getInvTitle(owner));
        } else {
            throw new IllegalArgumentException("allowedSize must not be 0.");
        }
    }

    @Nullable
    static BigInvHolder get(@NotNull Player owner) {
        int allowedSize = getAllowedInvSize(owner);
        if (allowedSize > 0)
            return new BigInvHolder(owner, allowedSize);
        return null;
    }

    private String getInvTitle(Player owner) {
        return invTitle.replaceAll("%player%", owner.getName());
    }

    @NotNull public Player getOwner() {
        return this.owner;
    }

    public boolean isChanged() {
        return this.isChanged;
    }

    boolean hasValidInventory() {
        return this.size > 0;
    }

    public void setChanged(boolean changed) {
        this.isChanged = changed;
    }

    /**
     * Sets the possible contents into the inventory hold by this BigInvHolder.
     * @param ymlInventory the ymlInventory from which the contents get pasted.
     * @return a new ymlInventory containing the backup contents. (BigInvHolder isChanged for none empty backups)
     */
    @NotNull YmlInventory setContents(final YmlInventory ymlInventory) {
        ItemStack[] backupContents = {};
        if (ymlInventory != null) {
            ItemStack[] contents = ymlInventory.getContentsClone();
            this.inv.setStorageContents(Arrays.copyOfRange(contents, 0, this.inv.getSize()));
            if (contents.length > this.size) {
                backupContents = Arrays.copyOfRange(contents, this.size, contents.length);
                this.isChanged = true;

                List<ItemStack> backupContentsList = new ArrayList<>(Arrays.asList(backupContents));
                backupContentsList.removeIf(Objects::isNull);

                Map<Integer, ItemStack> notAdded = this.inv.addItem(backupContentsList.toArray(new ItemStack[0]));
                backupContents = notAdded.values().toArray(new ItemStack[0]);
            }
        }
        return new YmlInventory(backupContents);
    }

    void clearContents() {
        this.inv.setStorageContents(new ItemStack[0]);
        this.isChanged = true;
    }

    boolean getCanOpen(Player viewer) {
        if (viewer.hasPermission(permissions[1])) {
            return viewer.equals(owner) || viewer.hasPermission(permissions[2]);
        }
        return false;
    }

    public boolean getCanChange(Player viewer) {
        if (viewer.hasPermission(permissions[3])) {
            return viewer.equals(owner) || viewer.hasPermission(permissions[4]);
        }
        return false;
    }

    boolean getCanRestoreBackup(Player viewer) {
        if (viewer.hasPermission(permissions[5])) {
            return viewer.equals(owner) || viewer.hasPermission(permissions[6]);
        }
        return false;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
