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

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class BigInvForceCloseEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final HumanEntity viewer;

    public BigInvForceCloseEvent(@NotNull Player owner, @NotNull HumanEntity viewer) {
        super(owner);
        Objects.requireNonNull(viewer, "viewer must not be null for BigInvForceCloseEvent");
        this.viewer = viewer;
    }

    public @NotNull HumanEntity getViewer() {
        return viewer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
