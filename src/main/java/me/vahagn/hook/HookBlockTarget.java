package me.vahagn.hook;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class HookBlockTarget extends HookTarget {

    private final Block target;

    public HookBlockTarget(Block block) {
        super(block.getLocation());
        this.target = block;
    }

    @Override
    public void onGrapple(Player origin, Ability ability) {
        this.attach();
    }

    @Override
    public void onRelease() {
        // Detach the hook from the block
        this.detach();
    }

    @Override
    public void onPull(Player origin, Ability ability) {

    }

    @Override
    public Location getPosition() {
        return target.getLocation().clone().add(.5,.5,.5); // Adjust to the center of the block
    }

    public HookEntityTarget convertToEntityTarget() {
        FallingBlock fallingBlock = target.getLocation().getWorld().spawn(this.getPosition(), FallingBlock.class);
        fallingBlock.setBlockData(target.getBlockData());
        fallingBlock.setDropItem(false); // Prevent dropping the block as an item
        fallingBlock.setHurtEntities(true); // Prevent hurting entities on impact
        fallingBlock.setVelocity(new Vector(0, 0, 0)); // Set initial velocity to zero
        this.onRelease();
        return new HookFallingBlockTarget(fallingBlock);
    }
}
