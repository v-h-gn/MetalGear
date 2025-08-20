package me.vahagn.hook;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.util.DamageHandler;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class HookLivingEntityTarget extends HookEntityTarget {

    public HookLivingEntityTarget(LivingEntity livingEntity) {
        super(livingEntity);
    }

    @Override
    public void onGrapple(Player origin, Ability ability) {
        super.onGrapple(origin, ability);
        DamageHandler.damageEntity(this.target, origin, 5.0, ability);
    }
}
