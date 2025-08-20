package me.vahagn.hook;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HookEntityTarget extends HookTarget {

    protected final Entity target;

    public HookEntityTarget(Entity target) {
        super(target.getLocation());
        this.target = target;
    }

    @Override
    public Location getPosition() {
        return target.getLocation().clone();
    }

    @Override
    public void onGrapple(Player origin, Ability ability) {
        this.attach();
    }

    @Override
    public void onRelease() {
        this.detach();
    }

    @Override
    public void onPull(Player origin, Ability ability) {

    }
}
