package CoordinatedMotionPlanningCoopINF421;

import java.util.*;

public class NodePool {
    
    static final boolean RESERVE_TWO = true;
    
    private final SortedMap<String, Node> usedNodes = new TreeMap<String, Node>();
    private final List<Node> pool = new ArrayList<Node>();
    
    private final SortedMap<String, Unit> reserved = new TreeMap<String, Unit>();
    
    private final SortedMap<Integer, Unit> units = new TreeMap<Integer, Unit>();
    
    final Grid grid;
    
    // Creates a new instance of NodePool
    public NodePool(Grid grid) {
        this.grid = grid;
    }
    
    public void addUnit(Unit unit) {
        if (units.put(unit.id, unit) != null)
            throw new IllegalStateException("already has unit, id: " + unit.id);
    }
    
    
    Map<String, Unit> getReserved() {
        return Collections.unmodifiableMap(reserved);
    }
    
    public boolean isReserved(Node node) {
        return isReserved(node.x, node.z, node.t);
    }
    
    public boolean isReserved(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        return reserved.containsKey(key);
    }
    
    public void reserve(Unit unit, Node node) {
        reserve(unit, node.x, node.z, node.t);
    }
    public void reserve(Unit unit, int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        Unit oldUnit = reserved.get(key);
        if (oldUnit != null)
            throw new IllegalStateException("already reserved: " + key + " by " + oldUnit.id + " attempting: " + unit.id);
        reserved.put(key, unit);
    }
    
    public void reclaim(Node node) {
        reclaim(node.x, node.z, node.t);
    }
    public void reclaim(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        if (reserved.remove(key) == null)
            throw new IllegalStateException("not reserved: " + key);
    }
    public void reclaimAll() {
        reserved.clear();
    }
    
    public void releaseAllNodes() {
        pool.addAll(usedNodes.values());
        usedNodes.clear();
    }
    
    public Node acquireNode(int x, int y, long t) {
        String key = x + ":" + y + ":" + t;
        Node node = usedNodes.get(key);
        if (node == null) {
            if (pool.isEmpty()) {
                node = new Node(x, y, t);
            } else {
                node = pool.remove(0);
                node.init(x, y, t);
            }
            usedNodes.put(key, node);
        } else {
        }
        return node;
    }
    
    public class Node extends TimeAStar.Node {
        int x;
        int z;
        long t;
        
        private List<TimeAStar.Transition> transitions;
        
        private Node(int x, int z, long t) {
            init(x, z, t);
        }
        
        private void init(int x, int z, long t) {
            this.x = x;
            this.z = z;
            this.t = t;
            transitions = null;
        }
        
        public Collection<TimeAStar.Transition> getTransitions() {
            if (transitions == null) {
                transitions = new ArrayList<TimeAStar.Transition>();
                for (Grid.Node node : grid.getNeighbours(x, z)) {
                    transitions.add(new Transition(this, acquireNode(node.x, node.y, t + 1)));
                }
                // wait
                transitions.add(new Transition(this, acquireNode(x, z, t + 1)));
            }
            return transitions;
        }
        /** actual timeless cost */
        public float getActualTimelessCost(TimeAStar.Node dest) {
            Node node = (Node) dest;
            return grid.getActualCost(x, z, node.x, node.z);
        }
        
        public String toString() {
            return "(" + x + ", " + z + ", " + t + ")";
        }
        
    }
    
    public class Transition implements TimeAStar.Transition {
        final Node fromNode;
        final Node toNode;
        final boolean wait;
        
        private Transition(Node fromNode, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.wait = (fromNode.x == toNode.x) && (fromNode.z == toNode.z);
        }
        
        public TimeAStar.Node fromNode() {
            return fromNode;
        }
        
        public TimeAStar.Node toNode() {
            return toNode;
        }
        
        public float getCost(Unit unit) {
            if (isReserved(toNode))
                return INFINITE_COST;
            
            if (RESERVE_TWO) {
                if (!wait && isReserved(toNode.x, toNode.z, toNode.t - 1))
                    return INFINITE_COST;
            }
            
            
            if (wait && (unit.getDestination().x == fromNode.x) &&
                    (unit.getDestination().z == fromNode.z)) {
                return 0;
            }
            return 1;
        }
        
        public String toString() {
            return "to: " + toNode;
        }
    }
    
    
    static class Point {
        public final int x;
        public final int z;
        
        Point(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public boolean equals(Object o) {
            return (o instanceof Point) ? equals((Point)o) : false;
        }
        
        public boolean equals(Point other) {
            return (this.x == other.x) && (this.z == other.z);
        }
        
        public String toString() {
            return "P " + x + "," + z;
        }
        
    }
}
