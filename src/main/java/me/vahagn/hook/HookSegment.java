package me.vahagn.hook;

import com.projectkorra.projectkorra.GeneralMethods;
import me.vahagn.physics.Mass;
import me.vahagn.physics.Spring;
import org.bukkit.Location;


public abstract class HookSegment extends HookTarget {

    private final Mass start;
    private final Mass end;
    private final Spring edge;


    /**
     * Represents a segment of a hook, which is defined by a start and end mass, and an edge spring.
     *
     * @param start The starting mass of the segment.
     * @param end   The ending mass of the segment.
     * @param edge  The spring connecting the start and end masses.
     */
    public HookSegment(Mass start, Mass end, Spring edge) {
        super(null); // Position will be updated later
        this.start = start;
        this.end = end;
        this.edge = edge;

        updatePosition();
    }

    public void updatePosition() {
        Location startLocation = start.getLocation();
        Location endLocation = end.getLocation();

        // Update the position of the segment to be the midpoint of the start and end masses
        this.position = GeneralMethods.getPointOnLine(startLocation, endLocation, edge.getLength() / 2);
    }
}
