/*
 * Copyright 2013 Alan Kristensen. All rights reserved.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  See LICENSE in the jar file. If not, 
 *   see {http://www.gnu.org/licenses/}.
 */
package ws.kristensen.HopperFilterSimplified;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This class listens to and handles the commands:<br/>
 * <br/> 
 *      - hopperfiltersimplifiedclearcache<br/>
 *      - hfsclearcache<br/>
 * <br/>
 * If a player issues the command, and they do not have permission, a message is displayed stating such.<br/>
 * Upon a successful or unsuccessful cache clear, a message is displayed stating the fact.
 * 
 */
public class HfsCommandListenerClearCache implements CommandExecutor {
    private final HopperFilterSimplified plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin HopperFilterSimplified class so we can point back to the base class at protected functions.
     */
    public HfsCommandListenerClearCache(HopperFilterSimplified plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!player.hasPermission("hopperfiltersimplified.clearcache")) {
                plugin.sendMessageInfo(sender, "You do not have hopperfiltersimplified.clearcache permission needed to clear that.");
                return true;
            }
        }
        
        //try to clear the cache and handle the response
        if (plugin.knownHoppersCache_Clear()) {
            plugin.sendMessageInfo(sender, "Hopper filter cache cleared.");
        } else {
            plugin.sendMessageInfo(sender, "Hopper filter cache clear failed.");
        }
        return true;
    }
}
