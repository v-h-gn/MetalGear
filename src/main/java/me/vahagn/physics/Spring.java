package me.vahagn.physics;

import org.bukkit.Location;

public class Spring {

    private final Location center;
    private double springConstant;
    private double dampening;
    private double length;

    public Spring(Location center, double springConstant, double dampening) {
        this.center = center;
        this.springConstant = springConstant;
        this.dampening = dampening;
    }

    public Location getCenter() {
        return this.center;
    }

    public double getSpringConstant() {
        return this.springConstant;
    }

    public double getDampening() {
        return this.dampening;
    }

    public void stretch(double length) {
        this.length = length;
    }

    public double getLength() {
        return this.length;
    }
}
