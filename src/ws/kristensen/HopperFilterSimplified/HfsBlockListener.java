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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * This class listens to events related to blocks for this plugin.
 * 
 */
public class HfsBlockListener implements Listener {
    private final HopperFilterSimplified plugin;
    
    /**
     * Constructor that is called when class is instantiated.
     * 
     * @param plugin HopperFilterSimplified class so we can point back to the base class at protected functions.
     */
    public HfsBlockListener(HopperFilterSimplified plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        this.plugin = plugin;
    }

    /**
     * Event handler to watch whenever a block is placed by a player. 
     * 
     * @param event BlockPlaceEvent of the block being place.  We care about Chest(s) and Hopper(s) being placed
     */
    @EventHandler(ignoreCancelled=true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST) || event.getBlock().getType().equals(Material.TRAPPED_CHEST)) {
            //Get the chest Block from the event, so we can work with it easier.
            Block chestBlock = event.getBlock();

            //make sure chest is placed next to a hopper
            if (plugin.isChestNextToHopper(chestBlock.getLocation())) {
                if (event.getPlayer() instanceof Player) {
                    //check permissions setting
                    Player player = (Player)event.getPlayer();
                    if (!player.hasPermission("hopperfiltersimplified.build.place.chest")) {
                        plugin.sendMessageInfo(player, "You do not have rights to place this hopper filter chest.");
                        event.setCancelled(true);
                        return;
                    }
                }

                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest placed at (" + chestBlock.getLocation().toString() + ")");
                
                //a chest was placed. Clear the cache from the hoppers around the chest.
                plugin.knownHoppersCache_ClearAroundLocation(chestBlock.getLocation());
            }
        }
        if (event.getBlock().getType().equals(Material.HOPPER)) {
            //Get the chest Block from the event, so we can work with it easier.
            Block hopperBlock = event.getBlock();

            //See if hopper is placed next to a chest and does not feed into it.
            if (plugin.isHopperNextToChest(hopperBlock.getLocation())) {
                if (event.getPlayer() instanceof Player) {
                    //check permissions setting
                    Player player = (Player)event.getPlayer();
                    if (!player.hasPermission("hopperfiltersimplified.build.place.hopper")) {
                        plugin.sendMessageInfo(player, "You do not have rights to place this hopper filter chest.");
                        event.setCancelled(true);
                        return;
                    }
                }

                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Hopper placed at (" + hopperBlock.getLocation().toString() + ")");
                
                //a hopper was placed. Make sure the cache is clear for the hopper.
                plugin.knownHoppersCache_ClearLocation(hopperBlock.getLocation());
            }
        }
    }
    
    /**
     * Event handler to watch whenever a block is broken.
     * This will remove the cache of hoppers around the chest.
     * This will remove the cache of a hopper that was removed
     * 
     * @param event BlockBreakEvent of the block being broken. We care about Chest(s) and Hopper(s) being broken
     */
    @EventHandler(ignoreCancelled=true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.CHEST)) {
            //Get the chest Block from the event, so we can work with it easier.
            Location chestBlockLocation = event.getBlock().getLocation();

            //see if this chest is part of a hopper filter
            if (plugin.isChestNextToHopper(chestBlockLocation)) {
                if (event.getPlayer() instanceof Player) {
                    //check permissions setting
                    Player player = (Player)event.getPlayer();
                    if (!player.hasPermission("hopperfiltersimplified.build.break.chest")) {
                        plugin.sendMessageInfo(player, "You do not have rights to remove this hopper filter chest.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            //a chest was removed. Clear the cache from the hoppers around the chest.
            plugin.knownHoppersCache_ClearAroundLocation(chestBlockLocation);            

            //debug if requested
            if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest broken at (" + chestBlockLocation.toString() + ")");
        }
        if (event.getBlock().getType().equals(Material.HOPPER)) {
            Location hopperBlockLocation = event.getBlock().getLocation();

            // See if there is a filter for this hopper.
            if (plugin.isHopperPartOfFilter(hopperBlockLocation)) {
                //check to see if the player has permissions to remove it.
                if (event.getPlayer() instanceof Player) {
                    //check permissions setting
                    Player player = (Player)event.getPlayer();
                    if (!player.hasPermission("hopperfiltersimplified.build.break.hopper")) {
                        plugin.sendMessageInfo(player, "You do not have rights to remove this hopper with a filter.");
                        event.setCancelled(true);
                        return;
                    }
                }
                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Hopper broken at (" + hopperBlockLocation.toString() + ")");

                //a hopper was removed. Clear the cache from the hoppers around the chest.
                plugin.knownHoppersCache_ClearLocation(hopperBlockLocation);            
            }
        }
    }
    
    /**
     * Event handler to watch whenever an itemframe is placed.
     * This will remove the cache of the hopper it was placed on.
     * 
     * @param event HangingPlaceEvent containing data about the item being hung. We care about an ItemFrame being placed.
     */
    @EventHandler(ignoreCancelled=true)
    public void onHangingPlaceEvent(HangingPlaceEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            if (event.getBlock().getType().equals(Material.HOPPER)) {
                Player player = event.getPlayer();
                if (!player.hasPermission("hopperfiltersimplified.build.place.itemframe")) {
                    plugin.sendMessageInfo(player, "You do not have rights to place a hopper filter itemFrame.");
                    event.setCancelled(true);
                    return;
                }
                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame placed at (" + event.getBlock().getLocation().toString() + ")");

                //Clear the cache from the hopper around the Hopper the itemFrame was placed on.
                plugin.knownHoppersCache_ClearAroundLocation(event.getBlock().getLocation());
            }
        }        
    }
    
    /**
     * Event handler to watch whenever an itemframe is broken by a player.
     * This will remove the cache of the hoppers around it.
     * 
     * @param event HangingBreakEvent containing data about the broken hanging item. We care about the ItemFrame being removed.
     */
    @EventHandler(ignoreCancelled=true)
    public void onHangingBreakByEntityEvent(HangingBreakByEntityEvent event) {
        
        if (event.getEntity() instanceof ItemFrame) {
            //get the item frame entity
            ItemFrame itemFrame =  (ItemFrame)event.getEntity();

            //get the block location that represents the ItemFrame location
            Location itemFrameLocation = itemFrame.getLocation().getBlock().getLocation();

            //get the block the item frame is attached to
            Block testBlock = itemFrameLocation.getBlock().getRelative(itemFrame.getAttachedFace());

            //is the testBlock a Hopper
            if (testBlock.getType().equals(Material.HOPPER)) {
                if (event.getRemover() instanceof Player) {
                    Player player = (Player)event.getRemover();
                    if (!player.hasPermission("hopperfiltersimplified.build.break.itemframe")) {
                        plugin.sendMessageInfo(player, "You do not have rights to remove a hopper filter itemFrame.");
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            //debug if requested
            if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame broken at (" + itemFrameLocation.toString() + ")");

            //an itemFrame was broken. Clear the cache from the hoppers around the frame.
            plugin.knownHoppersCache_ClearAroundLocation(itemFrameLocation);
        }
    }

    /**
     * Event handler to watch whenever an itemframe is broken by something other than a player..
     * This will remove the cache of the hoppers around it.
     * 
     * @param event HangingBreakEvent containing data about the broken hanging item. We care about the ItemFrame being removed.
     */
    @EventHandler(ignoreCancelled=true)
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            //get the item frame entity
            ItemFrame itemFrame =  (ItemFrame)event.getEntity();

            //get the block location that represents the ItemFrame location
            Location itemFrameLocation = itemFrame.getLocation().getBlock().getLocation();
            
            //debug if requested
            if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame broken by non-player at (" + itemFrameLocation.toString() + ")");

            //an itemFrame was broken. Clear the cache from the hoppers around the frame.
            plugin.knownHoppersCache_ClearAroundLocation(itemFrameLocation);
        }
    }

    /**
     * Event handler to watch whenever a player right clicks on an itemFrame.
     * This will remove the cache of a hopper the item frame is attached to.
     * 
     * @param event PlayerInteractEntityEvent that is when a player right clicks on an object. We care about clicked on ItemFrame(s)
     */
    @EventHandler(ignoreCancelled=true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ItemFrame)  {
            //get the block location of the ItemFrame
            Location itemFrameLocation = event.getRightClicked().getLocation();
            //cast the right clicked on item into an itemFrame
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
            
            //Make sure item frame is hung on a hopper
            if (itemFrameLocation.getBlock().getRelative(itemFrame.getAttachedFace()).getType().equals(Material.HOPPER)) {
                ItemFrame test = (ItemFrame)(event.getRightClicked());
                boolean isAir = test.getItem().getType().equals(Material.AIR);
                
                if (event.getPlayer() instanceof Player) {
                    //check permissions setting
                    Player player = (Player)event.getPlayer();

                    if (isAir) {
                        //nothing in the frame yet, check to see if they have rights to place.
                        if (!player.hasPermission("hopperfiltersimplified.build.place.itemframe")) {
                            plugin.sendMessageInfo(player, "You do not have rights to place an item in a hopper filter itemFrame.");
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        // something is there. check to see if they have rights to alter.
                        if (!player.hasPermission("hopperfiltersimplified.build.alter.itemframe")) {
                            plugin.sendMessageInfo(player, "You do not have rights to alter a hopper filter itemFrame.");
                            event.setCancelled(true);
                            return;
                        }                    
                    }
                }
                
                //debug if requested
                if (isAir) {
                    if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame filter created at (" + itemFrameLocation.toString() + ")");
                } else {
                    if (plugin.debugLevel_get() > 0) plugin.getLogger().info("ItemFrame altered at (" + itemFrameLocation.toString() + ")");
                }

                //an itemFrame was added. Clear the cache from the hoppers around the frame.
                plugin.knownHoppersCache_ClearAroundLocation(itemFrameLocation);
            }
        }
    }

    /**
     * Event handler to watch whenever a chest is closed.
     * This will remove the cache of hoppers around the chest.
     *  
     * @param event InventoryCloseEvent representing the inventory object that was just closed. We care about a Chest inventory being closed
     */
    @EventHandler (ignoreCancelled=true)
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        //get the inventory holder for easier referencing
        InventoryHolder holder = event.getInventory().getHolder();
        //Make sure it is a chest
        if (holder instanceof Chest) {
            Location chestLocation = ((Chest) holder).getLocation(); 
            //see if a filter is around the location
            if (plugin.isChestNextToHopper(chestLocation)) {
                //clear the cache of all possible hoppers around this chest.
                //even if the player was unable to alter it.
                plugin.knownHoppersCache_ClearAroundLocation(((Chest) holder).getLocation());
                
                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest next to hopper closed at (" + chestLocation.toString() + ")");
            }
        }
    }
    
    /**
     * Event handler to watch whenever a chest is opened.
     * This will prevent the opening of the chest if necessary.
     *  
     * @param event inventoryOpenEvent representing the inventory object that was just opened. We care about a Chest inventory being opened
     */
    @EventHandler (ignoreCancelled=true)
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        //get the destination inventory
        InventoryHolder holder = event.getInventory().getHolder();
        //Make sure it is a chest
        if (holder instanceof Chest) {
            Location chestLocation = ((Chest) holder).getLocation();
            //see if a filter is around the location
            if (plugin.isChestNextToHopper(chestLocation)) {
                Player player = (Player) event.getPlayer();
                if (!player.hasPermission("hopperfiltersimplified.build.alter.chest")) {
                    plugin.sendMessageInfo(player, "You do not have rights to alter this hopper filter chest.");
                    event.setCancelled(true);
                }

                //debug if requested
                if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Chest next to hopper opened at (" + chestLocation.toString() + ")");
            }            
        }
    }
    
    /** 
     * Event handler to watch whenever a container tries to move an item to another container.
     * Hoppers for example are the only thing that triggers this so far.
     * If the item being moved is not allowed in the destination container, then the event will be canceled to prevent the move.
     *
     *  @param event InventoryMoveItemEvent containing data about the event given by the system. We care about hoppers being the receiving end of the transfer.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        //Get destination container
        InventoryHolder destHolder = event.getDestination().getHolder();
        
        //since we only care about hoppers, then ignore everything else
        if (destHolder instanceof Hopper) {
            //Get the location of the hopper in question.  
            Block hopperBlock = ((Hopper) destHolder).getBlock();

            //Get the cache, if any, for the hopper location.
            String cache = plugin.knownHoppersCache_Get(hopperBlock);

            //get the itemStack that was moved and convert it to a String for comparison
            String eventItemInformation = plugin.GetItemInformationForInventory(event.getItem(), false);
                        
            //debug if requested
            if (plugin.debugLevel_get() > 0) plugin.getLogger().info("Testing item (" + eventItemInformation + ") in hopper (" + hopperBlock.getLocation().toString() + ") against allowed:" + cache);
            
            //do the actual filtering
            if (cache.length() > 0)
                //we have a cache to compare against
                if (!cache.contains(eventItemInformation))
                    //the item is not allowed in the hopper
                    event.setCancelled(true);
        }
    }
}
