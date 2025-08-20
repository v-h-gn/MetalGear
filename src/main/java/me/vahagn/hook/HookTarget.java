package me.vahagn.hook;

import com.projectkorra.projectkorra.ability.Ability;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public abstract class HookTarget {

    private boolean attached;
    protected Location position;
    protected Vector acceleration;
    protected Vector velocity;

    public HookTarget(Location position) {
        this.attached = false;
        this.position = position;
        this.acceleration = new Vector();
        this.velocity = new Vector();
    }

    public boolean isAttached() {
        return attached;
    }

    protected void attach() {
        this.attached = true;
    }

    protected void detach() {
        this.attached = false;
    }

    public Vector getAcceleration() {
        return acceleration;
    }

    public void accelerate(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public Location getPosition() {
        return position;
    }

    public abstract void onGrapple(Player origin, Ability ability);
    public abstract void onRelease();
    public abstract void onPull(Player origin, Ability ability);

}
