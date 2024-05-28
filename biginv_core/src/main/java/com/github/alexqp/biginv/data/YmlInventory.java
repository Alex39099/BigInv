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

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConfigurationSerializableCheckable;
import com.github.alexqp.commons.config.ConsoleErrorType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("WeakerAccess")
@SerializableAs("biginv.data.YmlInventory")
public class YmlInventory implements ConfigurationSerializableCheckable {

    private static final String[] configNames = {"contents"};

    private ItemStack[] contents;

    public YmlInventory() {
        this(new ItemStack[0]);
    }

    @Override
    public boolean checkValues(ConfigChecker configChecker, ConfigurationSection section, String path, ConsoleErrorType errorType, boolean overwriteValues) {
        return true;
    }

    public YmlInventory(ItemStack[] contents) {
        this.setContents(contents);
    }

    public ItemStack[] getContentsClone() {
        return contents.clone();
    }

    public ItemStack[] clearContents() {
        ItemStack[] contents = this.getContentsClone();
        this.contents = new ItemStack[0];
        return contents;
    }

    public boolean isEmpty() {
        return this.contents.length == 0;
    }

    /**
     * Sets the contents of the YmlInventory. This will ignore null items!
     * @param contents a set of contents.
     */
    public void setContents(ItemStack[] contents) {
        this.contents = contents.clone();
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put(configNames[0], contents);
        return map;
    }

    @SuppressWarnings("unused")
    public static YmlInventory deserialize(Map<String, Object> map) {
        for (String configName : configNames) {
            if (!map.containsKey(configName))
                return null;
        }
        @SuppressWarnings("unchecked")
        ItemStack[] contents = ((List<ItemStack>) map.get(configNames[0])).toArray(new ItemStack[0]);
        return new YmlInventory(contents);
    }
}
