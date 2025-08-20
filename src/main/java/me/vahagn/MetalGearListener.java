package me.vahagn;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.OfflineBendingPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class MetalGearListener implements Listener {

    private MetalGear instance = null;

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            BendingPlayer bendingPlayer = BendingPlayer.getBendingPlayer(event.getPlayer());
            if (bendingPlayer != null && bendingPlayer.getBoundAbilityName().equalsIgnoreCase("MetalGear")) {
                if (bendingPlayer.getBoundAbility() instanceof MetalGear && bendingPlayer.canBend(bendingPlayer.getBoundAbility())) {
                    this.instance = new MetalGear(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        if (this.instance != null) {
            instance.collectInputs(event.getInput());
        }
    }
}
