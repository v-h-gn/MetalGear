package me.vahagn.hook;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class HookItemTarget extends HookEntityTarget {
    private final Item item;

    public HookItemTarget(Item target) {
        super(target);
        this.item = target;
    }

    @Override
    public void onPull(Player origin, Ability ability) {
        if(item.getLocation().getNearbyPlayers(.5).contains(origin)) {
            // If the player is close enough, remove the item and give it to the player
            item.remove();
            origin.getInventory().addItem(item.getItemStack());
            origin.updateInventory();
            this.detach();
        }
    }


}
