package me.vahagn.physics;


import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

public abstract class Mass {

    private Location position;
    private Vector gravity;
    private Vector acceleration = new Vector();
    private Vector velocity = new Vector();

    public Location getLocation() {
        return position.clone();
    }

    public Vector getGravity() {
        return gravity;
    }

    public Vector getAcceleration() {
        return acceleration;
    }

    public void setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }
}
