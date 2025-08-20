package me.vahagn.hook;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.Ability;
import me.vahagn.MetalGear;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Hook {
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum Side {
        LEFT, RIGHT
    }
    public enum State {
        IDLE,
        LAUNCHING,
        ATTACHING,
        PULLING,
        RELEASING,
        END,
    }

    private final Player player;
    private final Ability ability;
    private final Side side;
    private State state;
    private Location position;
    private Vector velocity;
    private Vector acceleration;
    private HookTarget target;
    private final @NotNull BukkitTask animationTask;


    public Hook(Player player, Ability originAbility, Side side) {
        this.player = player;
        this.ability = originAbility;
        this.side = side;
        this.position = this.getOrigin().clone();
        this.velocity = new Vector();
        this.acceleration = new Vector();
        this.target = null;
        this.state = State.IDLE;

        this.animationTask = Bukkit.getScheduler().runTaskTimer(ProjectKorra.plugin, () -> {
            for (int i = 0; i < 150; i++) {
                // @TODO: After instantiation, position should be updated to the current position of the
                Location pointOnLine = GeneralMethods.getPointOnLine(this.getOrigin(), this.state == State.IDLE ? this.getOrigin() : this.position, position.distance(this.getOrigin()) * i / 150.0);
                GeneralMethods.displayColoredParticle("000000", pointOnLine);
            }
        }, 0, 0);
    }

    public void launch(int maxDistance) {
        if(MetalGear.DEBUG_MODE ) {
            Component message = Component.text("Left Hook launched by " + this.player.getName() + " at position " + this.position);
            Bukkit.getServer().broadcast(message);
        }
        getHookTarget(maxDistance);

        if (this.target == null) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("No valid target found for the hook!"));
            }
            return; // No valid target found
        }
        // get initial velocity towards target
        Location destination = target.getPosition().clone();
        Location origin = this.position.clone();
        this.velocity = destination.toVector().subtract(origin.toVector()).normalize().multiply(7.5);

        this.state = State.LAUNCHING;
    }

    public boolean hasAttachedTarget() {
        return this.target != null && this.target.isAttached();
    }

    private void pull() {
        if (this.state == State.ATTACHING && hasAttachedTarget()) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Pulling the target: " + this.target.getPosition()));
            }
            this.state = State.PULLING;
        }
    }

    public void release() {
        if (MetalGear.DEBUG_MODE) {
            Component message = Component.text("Left Hook released by " + this.player.getName() + " at position " + this.position);
            Bukkit.getServer().broadcast(message);
        }
        this.target.onRelease();
        this.state = State.RELEASING;
    }

    public void reset() {
        this.state = State.IDLE;
        this.target = null;
        this.position = this.getOrigin().clone();
        this.velocity = new Vector();
        this.acceleration = new Vector();
        if (MetalGear.DEBUG_MODE)
            Bukkit.getServer().broadcast(Component.text("Hook reset to initial state."));
    }

    public Location getOrigin() {
        return this.side == Side.LEFT ? MetalGear.getLeftHandPos(this.player) : MetalGear.getRightHandPos(this.player);
    }

    public void remove() {
        this.reset();
        cancelTask(animationTask);
    }

    public HookTarget getTarget() {
        return target;
    }

    private static void cancelTask(BukkitTask task) {
        Bukkit.getScheduler().cancelTask(task.getTaskId());
    }

    public void progress() {
        // Update the hook's position and state
        this.hookStateTransition();
        this.hookStateAction();
    }

    private void hookStateTransition() {
        //System.out.println("Current Hook State: " + this.state);
        switch (this.state) {
            case IDLE:
                // No action needed
                break;
            case LAUNCHING:
                if (this.hasAttachedTarget()) {
                    this.state = State.ATTACHING;
                    if (MetalGear.DEBUG_MODE) {
                        Bukkit.getServer().broadcast(Component.text("Hook is attaching to the target!"));
                    }
                }
                break;
            case ATTACHING:
                if(this.player.isSneaking() && this.hasAttachedTarget()) {
                    this.pull(); // changes state to PULLING if target is attached
                }
                break;
            case PULLING:
                if(this.player.isSneaking() && this.hasAttachedTarget()) {
                    this.pull();
                } else {
                    this.state = State.ATTACHING;
                }
                break;
            case RELEASING:
                this.state = State.END;
                if (MetalGear.DEBUG_MODE) {
                    Bukkit.getServer().broadcast(Component.text("Hook has ended its action!"));
                }
                break;
            case END:
                this.reset();
                break; // No action needed, hook is in end state
        }
        //System.out.println("New Hook State: " + this.state);
    }

    private void hookStateAction() {
        switch (this.state) {
            case IDLE:
                // No action needed
                break;
            case LAUNCHING:
                Location newPosition = this.position.clone().add(this.velocity);
                if (BoundingBox.of(this.position, newPosition).contains(this.target.getPosition().toVector())) {
                    if (MetalGear.DEBUG_MODE) {
                        Bukkit.getServer().broadcast(Component.text("Hook reached the target!"));
                    }
                    this.velocity = new Vector(0, 0, 0);
                    this.position = this.target.getPosition();
                    this.target.attach();
                } else {
                    // Update the hook's velocity and acceleration
                    this.position.add(this.velocity);
                    this.velocity.add(this.acceleration);
                    // System.out.println("Hook position updated to: " + this.position);
                }
                break;
            case ATTACHING:
                // Handle attached logic if needed
                this.target.onGrapple(this.player, this.ability);
                break;
            case PULLING:
                // Handle pulling logic if needed
                this.target.onPull(this.player, this.ability);
                break;
            case RELEASING:
                // TODO: Animation for hook returning to player
                break;
            case END:
                break;
        }
    }

    private void getHookTarget(int maxDistance) {
        Block targetBlock = this.player.getTargetBlockExact(maxDistance);
        Entity targetEntity = this.player.getTargetEntity(maxDistance);
        HookEntityTarget hookEntityTarget = null;
        HookBlockTarget hookBlockTarget = null;
        // check if target block and target entity are both null
        if (targetBlock == null && targetEntity == null) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Both target block and target entity are null!"));
            }
            return; // No valid target found
        }
        // construct the appropriate target based on what was found
        if (targetBlock != null) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Target block found: " + targetBlock.getType() + " at " + targetBlock.getLocation()));
            }
            hookBlockTarget = new HookBlockTarget(targetBlock);
        }
        if (targetEntity != null) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Target entity found: " + targetEntity.getType() + " at " + targetEntity.getLocation()));
            }
            // check if the target entity is an item or a living entity
            if (targetEntity instanceof Item item) {
                if (MetalGear.DEBUG_MODE) {
                    Bukkit.getServer().broadcast(Component.text("Target entity is an item, converting to HookItemTarget."));
                }
                hookEntityTarget = new HookItemTarget(item);
            } else if (targetEntity instanceof LivingEntity livingEntity) {
                if (MetalGear.DEBUG_MODE) {
                    Bukkit.getServer().broadcast(Component.text("Target entity is a living entity, creating HookEntityTarget."));
                }
                hookEntityTarget = new HookLivingEntityTarget(livingEntity);
            } else {
                if (MetalGear.DEBUG_MODE) {
                    Bukkit.getServer().broadcast(Component.text("Target entity is not an item or living entity, converting to HookEntityTarget."));
                }
                hookEntityTarget = new HookEntityTarget(targetEntity);
            }
        }

        double distanceToBlock = targetBlock != null ? targetBlock.getLocation().distance(this.position) : Double.POSITIVE_INFINITY;
        double distanceToEntity = targetEntity != null ? targetEntity.getLocation().distance(this.position) : Double.POSITIVE_INFINITY;
        // set target based on distance, closer target is chosen
        if (MetalGear.DEBUG_MODE) {
            Bukkit.getServer().broadcast(Component.text("Distance to block: " + distanceToBlock));
            Bukkit.getServer().broadcast(Component.text("Distance to entity: " + distanceToEntity));
        }
        if (distanceToBlock <= distanceToEntity) {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Choosing block target due to closer distance."));
            }
            this.target = hookBlockTarget;
        } else {
            if (MetalGear.DEBUG_MODE) {
                Bukkit.getServer().broadcast(Component.text("Choosing entity target due to closer distance."));
            }
            this.target = hookEntityTarget;
        }

    }

}
