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
 *      - hopperfiltersimplifiedallowchestfilters<br/>
 *      - hfsallowchestfilters<br/>
 * <br/>
 * If a player issues the command, and they do not have permission, a message is displayed stating such.<br/>
 * If true or false was not supplied as a parameter, a message will displayed and a false returned so usage from plugin.yml is displayed as well.<br/>
 * Upon a successful set, a message is displayed stating the flag was set to what they requested.
 * 
 */
public class HfsCommandListenerAllowChestFilters implements CommandExecutor {
    private final HopperFilterSimplified plugin;

    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin HopperFilterSimplified class so we can point back to the base class at protected functions.
     */
    public HfsCommandListenerAllowChestFilters(HopperFilterSimplified plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }

        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            if (!player.hasPermission("hopperfiltersimplified.setchestfilter")) {
                plugin.sendMessageInfo(sender, "You do not have hopperfiltersimplified.setchestfilter permission needed to set that.");
                return true;
            }
        }
        if (args.length == 0) {
            plugin.sendMessageInfo(sender, "Required flag was not included (true, false).");
            return false;
        } else {
            //try to set the flag and handle the response
            if (!plugin.allowChestFilters_Set(args[0])) {
                //set was unsuccessful so return false so the usage from the plugin.yml is displayed.
                return false;
            } else {
                plugin.sendMessageInfo(sender, "AllowChestFilters flag set to: " + args[0]);
            }
        }
        return true;
    }
}
