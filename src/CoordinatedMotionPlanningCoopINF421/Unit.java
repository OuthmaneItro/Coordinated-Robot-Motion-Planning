package CoordinatedMotionPlanningCoopINF421;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Unit implements Comparable<Unit>{
    
    private static int lastId = 0;
    private static synchronized final int nextId() {
        return lastId++;
    }
    
    public final int id = nextId();
    
    NodePool.Point destination = null;
    
    private Integer x = 0;
    private Integer z = 0;
    
    private int pathIndex = 0;
    private final List<PathPoint> path = new ArrayList<PathPoint>();
    
    // Creates a new instance of Unit
    public Unit() {
    }
    @Override
    public int compareTo(Unit o) {
        int d1 =0;
        if (this.x-this.destination.x != 0) d1++;
        if(this.z-this.destination.z != 0) d1++;
        int d2 =0;
        if (o.x-o.destination.x != 0) d2++;
        if(o.z-o.destination.z != 0) d2++;
        if (d1 == d2) {
        	float p1 = (this.x-this.destination.x)*(this.x-this.destination.x)+(this.z-this.destination.z)*(this.z-this.destination.z);
        	float p2 = (o.x-o.destination.x)*(o.x-o.destination.x)+(o.z-o.destination.z)*(o.z-o.destination.z);
        	if (p2 > p1) return -1;
        	return 1;
        }
        return d2-d1;	
    }
    public PathPoint getTarget() {
    	return path.get(pathIndex);
    }
    	
    public void setLocation(Integer x, Integer z) {
        this.x = x;
        this.z = z;
    }
    
    public NodePool.Point getLocation() {
        return new NodePool.Point(x, z);
    }
    int getPathIndex() {
        return pathIndex;
    }
    
    boolean reached() {
        return (x == destination.x) && (z == destination.z);
    }
    
    public void next() {
        pathIndex++;
        if (pathIndex < path.size()) {
            PathPoint location = path.get(pathIndex);
            setLocation(location.x, location.z);
        }
        
    }
    
    public void setDestination(Integer destX, Integer destZ) {
        this.destination = new NodePool.Point(destX, destZ);
    }
    
    public NodePool.Point getDestination() {
        return destination;
    }
    
    void setPath(List<PathPoint> path) {
        this.path.clear();
        this.path.addAll(path);
        
        this.pathIndex = 0;
        if (! path.isEmpty()) {
            PathPoint location = path.get(0);
            setLocation(location.x, location.z);
        }
    }
    
    List<PathPoint> getPath() {
        return Collections.unmodifiableList(path);
    }
    
    
    public int hashCode() {
        return id;
    }
    
    public boolean equals(Object o) {
        return (o instanceof Unit) ? equals((Unit)o) : false;
    }
    
    public boolean equals(Unit other) {
        return this.id == other.id;
    }
    
    public String toString() {
        return "Unit: " + id;
    }
    
    
    static class PathPoint {
        public final int x;
        public final int z;
        public final long t;
        
        public PathPoint(NodePool.Node node) {
            this(node.x, node.z, node.t);
        }
        
        public PathPoint(int x, int z, long t) {
            this.x = x;
            this.z = z;
            this.t = t;
        }
        
        public boolean isSamePlace(PathPoint other) {
            return (this.x == other.x) && (this.z == other.z);
        }
    }
    
    
}
