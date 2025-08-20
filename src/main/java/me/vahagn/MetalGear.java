package me.vahagn;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.MetalAbility;
import com.projectkorra.projectkorra.ability.MultiAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import me.vahagn.hook.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Input;
import org.bukkit.Location;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MetalGear extends MetalAbility implements AddonAbility, MultiAbility {
    public static final int LEFT_HOOK_SLOT = 0;
    public static final int NEUTRAL_SLOT = 1; // Neutral slot for Metal Gear, can be used for other actions
    public static final int RIGHT_HOOK_SLOT = 2;
    public static final int DUAL_HOOK_SLOT = 3;
    public static final int SWORD_SLOT = 4; // Slot for the sword action
    public static final int WHIP_NAE_NAE_SLOT = 5; // Slot for the whip nae nae action, can be used for special actions
    public static final int END_SLOT = 6; // Slot for ending the Metal Gear ability, can be used for special actions
    public static final boolean DEBUG_MODE = false; // Set to 1 to enable dev mode to display debug messages, 0 to disable

    @Attribute(Attribute.COOLDOWN)
    private final long abilityCooldown;
    @Attribute(Attribute.DURATION)
    private final long maxDuration; // Maximum duration of the Metal Gear ability in ms (5 minutes)
    private final long hookCooldown; // Cooldown for the hook actions in milliseconds
    @Attribute(Attribute.RANGE)
    private final int hookRange;
    @Attribute(Attribute.DAMAGE)
    private final double hookDamage; // Damage dealt by the hook when it hits an entity
    @Attribute(Attribute.SELF_PUSH)
    private final double pullFactor; // Factor by which the player is accelerated when using the hook
    private final double propelFactor;


    enum MetalGearState {
        INIT, IDLE, LEFT_HOOK, RIGHT_HOOK, DUAL_HOOKS, SWORD, WHIP_NAE_NAE, END
    }

    enum Direction {
        FORWARD, BACKWARD, LEFT, RIGHT, UP, DOWN
    }

    private MetalGearState state;
    private long currentTick;
    private final Hook leftHook;
    private final Hook rightHook;

    private List<Direction> currentAccelDirections;

    private boolean pressedSprint = false; // Flag to check if sprint is pressed

    public MetalGear(Player player) {
        super(player);

        this.abilityCooldown = ConfigManager.getConfig().getLong("Abilities.Earth.MetalGear.Cooldown");
        this.maxDuration = ConfigManager.getConfig().getLong("Abilities.Earth.MetalGear.MaxDuration");
        this.hookCooldown = ConfigManager.getConfig().getLong("Abilities.Earth.MetalGear.HookCooldown");
        this.hookRange = ConfigManager.getConfig().getInt("Abilities.Earth.MetalGear.HookRange");
        this.hookDamage = ConfigManager.getConfig().getDouble("Abilities.Earth.MetalGear.HookDamage");
        this.pullFactor = ConfigManager.getConfig().getDouble("Abilities.Earth.MetalGear.PullFactor");
        this.propelFactor = ConfigManager.getConfig().getDouble("Abilities.Earth.MetalGear.PropelFactor");


        this.leftHook = new Hook(player, this, Hook.Side.LEFT);
        this.rightHook = new Hook(player, this, Hook.Side.RIGHT);
        this.pressedSprint = false;

        MultiAbilityManager.bindMultiAbility(player, "MetalGear");

        this.state = MetalGearState.INIT;
        this.currentTick = 0;
        this.start();
        if (DEBUG_MODE) {
            Bukkit.getServer().broadcast(Component.text("MetalGear: Ability started for player " + player.getName()));
        }
    }

    @Override
    public void progress() {
        checkProgressConditions();
        handleStateTransitions();
        handleStateActions();
        handlePhysics();

        this.leftHook.progress();
        // this.rightHook.progress();
        this.currentTick++;
    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return abilityCooldown;
    }

    @Override
    public String getName() {
        return "MetalGear";
    }

    @Override
    public Location getLocation() {
        return this.player.getLocation();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(new MetalGearListener(), ProjectKorra.plugin);

        ProjectKorra.log.info("MetalGear loaded successfully.");
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.MaxDuration", 100000);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.Cooldown", 50000);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.HookCooldown", 300);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.HookRange", 75);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.HookDamage", 5.0);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.PullFactor", 1.25);
        ConfigManager.getConfig().addDefault("Abilities.Earth.MetalGear.PropelFactor", .75);
        ConfigManager.defaultConfig.save();
    }

    @Override
    public void stop() {

    }

    @Override
    public void remove() {
        this.leftHook.remove();
        this.rightHook.remove();
        MultiAbilityManager.unbindMultiAbility(player);
        super.remove();
    }

    @Override
    public String getAuthor() {
        return "Vahagn";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public ArrayList<MultiAbilityManager.MultiAbilityInfoSub> getMultiAbilities() {
        ArrayList<MultiAbilityManager.MultiAbilityInfoSub> multiAbilityInfoSubs = new ArrayList<>(5);

        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("LeftHook", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("Neutral", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("RightHook", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("DualHook", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("Sword", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("WhipNaeNae", Element.METAL));
        multiAbilityInfoSubs.add(new MultiAbilityManager.MultiAbilityInfoSub("End", Element.METAL));

        return multiAbilityInfoSubs;
    }

    public void collectInputs(Input input) {
        if (input != null) {
            this.currentAccelDirections = new ArrayList<>();

            if(input.isForward()) {
                currentAccelDirections.add(Direction.FORWARD);
            }
            if(input.isBackward()) {
                currentAccelDirections.add(Direction.BACKWARD);
            }
            if(input.isLeft()) {
                currentAccelDirections.add(Direction.LEFT);
            }
            if(input.isRight()) {
                currentAccelDirections.add(Direction.RIGHT);
            }
            if(input.isJump()) {
                currentAccelDirections.add(Direction.UP);
            }
            if(input.isSneak()) {
                currentAccelDirections.add(Direction.DOWN);
            }

            if(input.isSprint()) {
                if (!this.pressedSprint) {
                    this.pressedSprint = true; // Set the flag to true when sprint is pressed
                }
            } else {
                this.pressedSprint = false; // Reset the flag when sprint is released
            }
        }
    }

    private void handleStateTransitions() {
        // System.out.println("Current State: " + this.state);
        // System.out.println("Current Tick: " + this.currentTick);
        switch (this.state) {
            case INIT:
                this.resetSlot(); // Reset the player's held item slot to the neutral slot
                this.state = MetalGearState.IDLE; // Transition to IDLE after initialization
                break;
            case IDLE:
                if (isPlayerSlot(LEFT_HOOK_SLOT) && !this.bPlayer.isOnCooldown("MetalGearLeftHook")) {
                    this.state = MetalGearState.LEFT_HOOK;
                } else if(isPlayerSlot(DUAL_HOOK_SLOT) && !this.bPlayer.isOnCooldown("MetalGearLeftHook") && !this.bPlayer.isOnCooldown("MetalGearRightHook")) {
                    this.state = MetalGearState.DUAL_HOOKS;
                } else if(isPlayerSlot(RIGHT_HOOK_SLOT) && !this.bPlayer.isOnCooldown("MetalGearRightHook")) {
                    this.state = MetalGearState.RIGHT_HOOK;
                } else if(isPlayerSlot(SWORD_SLOT) && !this.bPlayer.isOnCooldown("MetalGearSword")) {
                    this.state = MetalGearState.SWORD;
                } else if(isPlayerSlot(WHIP_NAE_NAE_SLOT)) {
                    this.state = MetalGearState.WHIP_NAE_NAE; // Example state transition for slot 6
                } else if(isPlayerSlot(END_SLOT)){
                    this.state = MetalGearState.END; // Default to IDLE if no valid slot is selected
                } else {
                    this.state = MetalGearState.IDLE; // Default to IDLE if no valid slot is selected
                }
                break;
            case LEFT_HOOK:
            case RIGHT_HOOK:
            case DUAL_HOOKS:
            case SWORD:
            case WHIP_NAE_NAE:
                this.resetSlot();
                this.state = MetalGearState.IDLE; // Transition back to IDLE after handling the action
                break;
            case END:
                break;
        }
        // System.out.println("New State: " + this.state);
    }

    /**
     * Handles the actions associated with the current state of the Metal Gear ability.
     * This method is called during each progress tick to execute the appropriate actions based on the current state.
     * == State Actions ==
     * - IDLE: No action needed.
     * - LEFT_HOOK: Launches the left hook and sets a cooldown.
     * - RIGHT_HOOK: Launches the right hook and sets a cooldown.
     * - DUAL_HOOK: Launches both hooks and sets cooldowns for both.
     * - SWORD: Handles sword action (currently not implemented).
     */
    private void handleStateActions() {
        switch (this.state) {
            case INIT:
            case IDLE:
                break;
            case LEFT_HOOK:
                handleHookState(this.leftHook);
                break;
            case RIGHT_HOOK:
                handleHookState(this.rightHook);
                break;
            case DUAL_HOOKS:
            case SWORD:
            case WHIP_NAE_NAE:
                break;
            case END:
                if (DEBUG_MODE) {
                    Bukkit.getServer().broadcast(Component.text("MetalGear: Ending Metal Gear ability for player: " + this.player.getName()));
                }
                this.bPlayer.addCooldown(this, this.abilityCooldown);
                this.remove();// Remove the ability
                break;
        }

    }

    private void handleHookState(Hook hook) {
        Hook.State hookState = hook.getState();
        switch(hookState) {
            case IDLE:
                // If the hook is idle, we can launch it
                hook.launch(this.hookRange);
                this.bPlayer.addCooldown("MetalGearLeftHook", this.hookCooldown);
                break;
            case LAUNCHING:
            case PULLING:
            case ATTACHING: // If hook is pulling, attaching, or launching, and we press left hook again it must release.
                hook.release();
                break;
            case RELEASING:
                break;
        }

    }

    private void handlePhysics() {
        Hook.State leftHookState = this.leftHook.getState();
        Hook.State rightHookState = this.rightHook.getState();
        Vector playerVelocity = this.player.getVelocity().clone();
        switch (leftHookState) {
            case IDLE:

                break;
            case PULLING:
                // check if player is on the ground by checking if the block below the player is solid
                if (leftHook.hasAttachedTarget()) {
                    HookTarget target = this.leftHook.getTarget();
                    if (target instanceof HookEntityTarget entityTarget) {
                        if (entityTarget instanceof HookFallingBlockTarget fallingBlockTarget) {
                            
                        } else if (entityTarget instanceof HookItemTarget itemTarget) {

                        } else if (entityTarget instanceof HookLivingEntityTarget blockTarget) {
                            // Handle other entity targets
                        } else {
                            // Handle other hook targets
                            
                        }
                    } else {
                        Vector leftForce = this.computeTensionForce(leftHook);

                        // Apply the computed force to the player
                        // System.out.println("Applying left hook force: " + leftForce);
                        playerVelocity.add(leftForce).multiply(new Vector(1, .98, 1));
                    }
                }
                break;
            case RELEASING:
                // On release of the left hook, stop applying tension to the player and target from the left hook
                break;
            case ATTACHING:
                // If the left hook is attached, apply tension only if rope is at max range
                break;
        }
        switch (rightHookState) {
            case IDLE:

                break;
            case PULLING:
                // check if player is on the ground by checking if the block below the player is solid
                if (rightHook.hasAttachedTarget()) {
                    HookTarget target = this.rightHook.getTarget();
                    if (target instanceof HookEntityTarget entityTarget) {
                        if (entityTarget instanceof HookFallingBlockTarget fallingBlockTarget) {

                        } else if (entityTarget instanceof HookItemTarget itemTarget) {

                        } else if (entityTarget instanceof HookLivingEntityTarget blockTarget) {
                            // Handle other entity targets
                        } else {
                            // Handle other hook targets

                        }
                    } else {
                        Vector rightForce = this.computeTensionForce(rightHook);
                        // Apply the computed force to the player
                        // System.out.println("Applying right hook force: " + rightForce);
                        playerVelocity.add(rightForce).multiply(new Vector(1, .98, 1));
                    }
                }
                break;
            case RELEASING:
                // On release of the left hook, stop applying tension to the player and target from the left hook
                break;
            case ATTACHING:
                // If the left hook is attached, apply tension only if rope is at max range
                break;
        }

        if(leftHook.getState() == Hook.State.PULLING || rightHook.getState() == Hook.State.PULLING) {
            Vector playerAcceleration = this.computePlayerForce(this.player);
            Vector playerAccelerationTangential = this.rejection(playerAcceleration, playerVelocity).normalize().multiply(propelFactor);
            this.player.setVelocity(playerVelocity.add(playerAccelerationTangential));
        }

    }

    private Vector computePlayerForce(Player player) {
        Vector force = new Vector();
        Location playerLocation = player.getLocation().clone();
        for (Direction direction : this.currentAccelDirections) {
            Vector playerDirection = playerLocation.getDirection().clone().setY(0).normalize();
            switch (direction) {
                case FORWARD:
                    force.add(playerDirection.multiply(propelFactor));
                    break;
                case BACKWARD:
                    force.add(playerDirection.multiply(-propelFactor));
                    break;
                case LEFT:
                    Location onLeft = GeneralMethods.getLeftSide(player.getLocation(), 1);
                    Vector leftDirection = onLeft.toVector().clone().subtract(player.getLocation().clone().toVector());
                    Vector leftAccel = leftDirection.multiply(propelFactor);
                    force.add(leftAccel);
                    break;
                case RIGHT:
                    Location onRight = GeneralMethods.getRightSide(player.getLocation(), 1);
                    Vector rightDirection = onRight.toVector().clone().subtract(player.getLocation().clone().toVector());
                    Vector rightAccel = rightDirection.multiply(propelFactor);
                    force.add(rightAccel);
                    break;
                case DOWN:
                    force.add(new Vector(0, -.08, 0));
                    break;
            }
            // spawn particles along line of force vector using general methods
            Location forceLocation = playerLocation.clone().add(force);
            for (double i = 0; i < force.length(); i += 0.1) {
                Location particleLocation = forceLocation.clone().add(force.clone().normalize().multiply(i));
                GeneralMethods.displayColoredParticle("FF0000", particleLocation);
            }
            // System.out.println("Current force added: " + force);
            // System.out.println("Direction: " + direction);
        }
        if(force.lengthSquared() > 1) {
            force.normalize().multiply(propelFactor);
        } else {
            force.multiply(propelFactor);
        }

        return force;
    }

    private Vector computeTensionForce(Hook hook) {
        HookTarget target = hook.getTarget();
        Location hookPosition = target.getPosition();
        Location playerLocation = this.player.getLocation().clone();
        return hookPosition.toVector().subtract(playerLocation.toVector()).normalize().multiply(this.pullFactor);
    }

    /**
     * Checks the progress conditions for the Metal Gear ability.
     * If the current tick exceeds the maximum duration, it adds a cooldown and removes the ability.
     */
    private void checkProgressConditions() {
        if(this.currentTick - this.getStartTick() >= maxDuration) {
            this.bPlayer.addCooldown(this, this.abilityCooldown);
            this.remove();
        }
    }

    /**
     * Resets the player's held item slot to the default slot (2).
     * This is used to ensure that the player can switch back to their normal inventory.
     */
    private void resetSlot() {
        this.player.getInventory().setHeldItemSlot(NEUTRAL_SLOT);
    }

    /**
     * Check if the player is using a specific slot for the Metal Gear ability.
     *
     * @param slot The slot to check (0-8).
     * @return true if the player is using the specified slot, false otherwise.
     */
    private boolean isPlayerSlot(int slot) {
        return this.player.getInventory().getHeldItemSlot() == slot;
    }

    public Location getLeftHandPos() {
        return getLeftHandPos(this.player);
    }

    public Location getRightHandPos() {
        return getRightHandPos(this.player);
    }

    /**
     * Calculate roughly where the player's left hand is - Credit to ProjectKorra WaterArms
     *
     * @return location of left hand
     */
    public static Location getRightHandPos(Player player) {
        return GeneralMethods.getRightSide(player.getLocation(), .34).add(0, 1.0, 0);
    }

    /**
     * Calculate roughly where the player's left hand is - Credit to ProjectKorra WaterArms
     *
     * @return location of left hand
     */
    public static Location getLeftHandPos(Player player) {
        return GeneralMethods.getLeftSide(player.getLocation(), .34).add(0, 1.0, 0);
    }

    private Vector rejection(Vector v1, Vector v2) {
        // Rejects vector v1 from vector v2
        return v1.subtract(v2.clone().multiply(v1.dot(v2) / v2.lengthSquared()));
    }

    private Vector projection(Vector v1, Vector v2) {
        // Projects vector v1 onto vector v2
        return v2.clone().multiply(v1.dot(v2) / v2.lengthSquared());
    }
}
