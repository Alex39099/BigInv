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

package com.github.alexqp.biginv.main;

import com.github.alexqp.biginv.command.BigInvCommand;
import com.github.alexqp.biginv.data.BigInvHolder;
import com.github.alexqp.biginv.listeners.*;
import com.github.alexqp.biginv.data.BigInvContainer;
import com.github.alexqp.biginv.data.YmlInventory;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.commons.messages.Debugable;
import com.github.alexqp.commons.bstats.bukkit.Metrics;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class BigInv extends JavaPlugin implements Debugable {

    private static final String defaultInternalsVersion = "1_20_R3";

    @Override
    public boolean getDebug() {
        return false;
    }

    private static BigInv instance;

    public static BigInv getInstance() {
        return instance;
    }

    private static InternalsProvider internals;
    static {
        try {
            String packageName = BigInv.class.getPackage().getName();
            String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            if (defaultInternalsVersion.equals(internalsName)) {
                Bukkit.getLogger().log(Level.INFO, BigInv.class.getSimpleName() + " is using the latest implementation (last tested for " + defaultInternalsVersion + ").");
                internals = new InternalsProvider();
            } else {
                Bukkit.getLogger().log(Level.INFO, BigInv.class.getSimpleName() + " is using the implementation for version " + internalsName + ".");
                internals = (InternalsProvider) Class.forName(packageName + "." + internalsName).getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException exception) {
            Bukkit.getLogger().log(Level.WARNING, BigInv.class.getSimpleName() + " could not find an updated implementation for this server version. " +
                    "However the plugin is trying to use the latest implementation which should work if Minecraft did not change drastically (last tested version: " + defaultInternalsVersion + ").");
            internals = new InternalsProvider();
        }
    }

    /**
     *
     * @param internalsName the current NMS version used by the server
     * @return the internals version name for the given NMS version. Returns defaultInternalsVersion for newer versions by default.
     */
    private static String getInternalsName(String internalsName) {
        return defaultInternalsVersion;
    }

    static {
        ConfigurationSerialization.registerClass(YmlInventory.class, "YmlInventory");
    }

    private final String saveOnInvCloseOptionConfigName = "save_on_inventory_close";
    private final String[] messagesSectionConfigNames = {"messages", "backup_needed_login", "logoutClose"};

    private BigInvContainer invContainer;

    @Override
    public void onEnable() {
        new Metrics(this, 5077);
        this.saveDefaultConfig();
        this.getLogger().info("This plugin was made by alex_qp");
        this.updateChecker();
        instance = this;
        ConfigChecker configChecker = new ConfigChecker(this);

        BigInvHolder.invTitle = configChecker.checkString(this.getConfig(), "inventory_title", ConsoleErrorType.WARN, "BigInv (%player%)");
        invContainer = new BigInvContainer(this);

        String neededBackupMsg = "&4Your old BigInv was to big to fit all items in the new one. Use /biginv backup to restore all left items in your player inventory.";
        String forceClose = "&6BigInv has been closed because the owner left the server.";
        ConfigurationSection msgSection = configChecker.checkConfigSection(this.getConfig(), messagesSectionConfigNames[0], ConsoleErrorType.ERROR);
        if (msgSection != null) {
            BigInvCommand cmd = new BigInvCommand(this, invContainer, msgSection);

            neededBackupMsg = cmd.getPrefix() + configChecker.checkString(msgSection, messagesSectionConfigNames[1], ConsoleErrorType.WARN, neededBackupMsg);
            forceClose = cmd.getPrefix() + configChecker.checkString(msgSection, messagesSectionConfigNames[2], ConsoleErrorType.WARN, forceClose);
        }

        PlayerConnectionListener connectionListener = new PlayerConnectionListener(this, invContainer, neededBackupMsg);
        BigInvClickEventListener invClickEventListener = new BigInvClickEventListener(this, invContainer);
        BigInvForceCloseEventListener invForceCloseEventListener = new BigInvForceCloseEventListener(forceClose);

        Bukkit.getPluginManager().registerEvents(connectionListener, this);
        Bukkit.getPluginManager().registerEvents(invClickEventListener, this);
        Bukkit.getPluginManager().registerEvents(invForceCloseEventListener, this);

        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(invContainer), this);

        if (configChecker.checkBoolean(this.getConfig(), saveOnInvCloseOptionConfigName, ConsoleErrorType.WARN, true)) {
            Bukkit.getPluginManager().registerEvents(new BigInvBackupOnCloseEventListener(invContainer), this);
        }
    }

    @Override
    public void onDisable() {
        invContainer.saveInventories();
    }

    private void updateChecker() {
        int spigotResourceID = 69257;
        ConfigChecker configChecker = new ConfigChecker(this);
        ConfigurationSection updateCheckerSection = configChecker.checkConfigSection(this.getConfig(), "updatechecker", ConsoleErrorType.ERROR);
        if (updateCheckerSection != null && configChecker.checkBoolean(updateCheckerSection, "enable", ConsoleErrorType.WARN, true)) {
            ConsoleMessage.debug((Debugable) this, "enabled UpdateChecker");

            new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(spigotResourceID))
                    .setDownloadLink(spigotResourceID)
                    .setChangelogLink("https://www.spigotmc.org/resources/" + spigotResourceID + "/updates")
                    .setDonationLink("https://paypal.me/alexqpplugins")
                    .setNotifyOpsOnJoin(configChecker.checkBoolean(updateCheckerSection, "notify_op_on_login", ConsoleErrorType.WARN, true))
                    .setNotifyByPermissionOnJoin("biginv.updatechecker")
                    .checkEveryXHours(24).checkNow();
        }
    }
}
