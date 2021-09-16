package CoordinatedMotionPlanningCoopINF421;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import raft.kilavuz.runtime.NoPathException;
import java.util.*;

public class PathPanel extends JPanel {
	/*
	 * Setup - Change variables to get desired treatment on the read configuration.
	 * 
	 * */
    static int unitCount = 4;
    static int offset = 5; // Adds offset to the size of the grid to allow robots to move out of it
    static String method = "O"; // 'O' in Order, 'A' random order at each iteration and 'SO' no particular order
    static String file= "the_king_94.instance";  // name of JSON solution created in the same directory
    int cellSize = 20; // modify the size of the cells 
    static int coop = 75; // changes the cooperation scope of the robots by modifying the depth of the search window of each robot
    static String readPath = "", writePath = ""; // Path of directory containing the file (variable file, same name will be used for result) as well as where the writing directory
    
    
    
    final Coordinater coordinater;
    final Grid grid;
    long beginTime = 0;
    long endTime = 0;
    boolean exec = true; 
    static int step = 0;
    
    public PathPanel(Coordinater coordinater) {
        this.coordinater = coordinater;
        this.grid = coordinater.grid;
        for (Unit unit : coordinater.units.values()) unitPositions.put(unit.id, new Point(unit.getLocation()));
    }
    
    public PathPanel(Coordinater coordinater, String file) {
        this.coordinater = coordinater;
        this.grid = coordinater.grid;
        this.file = file;
        for (Unit unit : coordinater.units.values())
            unitPositions.put(unit.id, new Point(unit.getLocation()));
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(20, 20);
        
        paintGrid(g2d);
        paintUnits(g2d);
    }
    
    private Stroke thinStroke = new BasicStroke(1);
    private Stroke thickStroke = new BasicStroke(2);
    
    private void paintUnits(Graphics2D g2d) {
        int unitRadius = cellSize * 2 / 3;
        int pathRadius = cellSize / 3;
        
        boolean allReached = true;
        for (Unit unit : coordinater.units.values()) {
            if (!unit.reached())
                allReached = false;
            
            g2d.setStroke(thinStroke);
            g2d.setColor(getUnitColor(unit));
            g2d.setFont(g2d.getFont().deriveFont(cellSize/2f));
            Point point = unitPositions.get(unit.id);
            if (point != null) {
                g2d.fillRect((int)(point.x * cellSize + (cellSize-unitRadius)/2), (int)(point.z * cellSize + (cellSize-unitRadius)/2),unitRadius, unitRadius);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.valueOf(unit.id), point.x * cellSize + (cellSize/3),
                        point.z * cellSize + (cellSize*2/3));
            }
            g2d.setColor(getUnitColor(unit));
            g2d.setStroke(thickStroke);
            g2d.drawRect(unit.getDestination().x * cellSize + (cellSize/8),
                    unit.getDestination().z * cellSize + (cellSize/8),
                    cellSize*3/4, cellSize*3/4);
            
            g2d.setStroke(thinStroke);
//            List<Unit.PathPoint> path = unit.getPath();
//            for (int i = unit.getPathIndex(); i < path.size(); i++) {
//                Unit.PathPoint pathPoint = path.get(i);
//                g2d.drawOval(pathPoint.x * cellSize + (cellSize-pathRadius)/2,
//                        pathPoint.z * cellSize + (cellSize-pathRadius)/2,
//                        pathRadius, pathRadius);
//                g2d.setFont(g2d.getFont().deriveFont((cellSize/4f)));
//                g2d.drawString(String.valueOf(i), pathPoint.x * cellSize + cellSize/5, 
//                        pathPoint.z * cellSize + cellSize/3);
//            }
        }
        
        if (allReached) {
        	if (exec) {
        	endTime = System.currentTimeMillis();
//        	System.out.print("Execution time : ");
//        	System.out.print(endTime-beginTime);
//        	System.out.println("s");
        	exec = false;
        	}
            g2d.setStroke(thinStroke);
            g2d.setColor(Color.BLACK);
            g2d.setFont(g2d.getFont().deriveFont(20f));
            String s = "Finished";
            g2d.drawString(s, 70, 300);
        }
    }
    
    private void paintGrid(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);
        
        for (int x = 0; x <= grid.columns; x++) {
            g2d.drawLine(x * cellSize, 0, x * cellSize, grid.rows * cellSize);
        }
        
        for (int y = 0; y <= grid.rows; y++) {
            g2d.drawLine(0, y * cellSize, grid.columns * cellSize, y * cellSize);
        }
        
//        g2d.setColor(Color.BLACK);
        for (Grid.Node node : grid.unwalkables) {
            g2d.fillRect(node.x * cellSize, node.y * cellSize, cellSize, cellSize);
        }
    }
    
    private Map<Integer, Color> unitColors = new HashMap<Integer, Color>();
    private List<Color> colors = Arrays.asList(Color.BLACK, Color.GRAY, Color.WHITE,
            Color.DARK_GRAY, Color.LIGHT_GRAY);
    int lastColorIndex = 0;
    
    private Color getUnitColor(Unit unit) {
        Color color = unitColors.get(unit.id);
        if (color == null) {
            color = colors.get(lastColorIndex);
            unitColors.put(unit.id, color);
            lastColorIndex++;
            if (lastColorIndex == colors.size())
                lastColorIndex = 0;
        }
        return color;
    }
    
    public static String nextMvmt(float x1,float z1,float x2,float z2){
    	if (x1 < x2) {
    		return "N";
    	}
    	if (x1 > x2) {
    		return "S";
    	}
    	if (z1 > z2) {
    		return "E";
    	}
    	if (z1 < z2) {
    		return "W";
    	}
    	return "Z";
    }
    class ButtonPanel extends JPanel {
    	
        JButton stepButton = new JButton(new AbstractAction("Next Step") {
            public void actionPerformed(ActionEvent event) {
                try {
                    coordinater.iterate();
                    for (Unit unit : coordinater.units.values()) {
                        unit.next();
                        unitPositions.put(unit.id, new Point(unit.getLocation()));
                    }
                } catch (NoPathException npe) {
                    npe.printStackTrace();
                }
                PathPanel.this.paintImmediately(PathPanel.this.getBounds());
            }
        });
        
        JButton resetButton = new JButton(new AbstractAction("Reset") {
            public void actionPerformed(ActionEvent event) {
                reset();
            }
        });
        
        JButton animateButton = new JButton(new AbstractAction("Animate") {
            public void actionPerformed(ActionEvent event) {
            	beginTime = System.currentTimeMillis();
                animate();
            }
        });
        
        JButton stopButton = new JButton(new AbstractAction("Stop") {
            public void actionPerformed(ActionEvent event) {
                animating = false;
            }
        });
        
        ButtonPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(stepButton);
            add(resetButton);
            add(animateButton);
            add(stopButton);
            stepButton.setMnemonic('s');
        }
    }
    Map<Integer, Point> unitPositions = new HashMap<Integer, Point>();
    Map<Integer, NodePool.Point> unitTargets = new HashMap<Integer, NodePool.Point>();
    boolean animating = false;
    void animate() {
        if (animating)
            return;
        animating = true;
        new Thread(){
            public void run() {
            	JSONObject sampleObject = new JSONObject();
                sampleObject.put("instance", file+".json");
                JSONArray steps = new JSONArray();
                JSONObject step = new JSONObject();
                while (animating) {
                	step = new JSONObject();
                    try {
                        coordinater.iterate();
                        boolean allReached = true;
                        for (Unit unit : coordinater.units.values()) {
                            if (!unit.reached())
                                allReached = false;
                        }
                        boolean print = false;
                        String ligne = "";
                        if (!allReached)
                        	ligne+="{";
                        for (Unit unit : coordinater.units.values()) {
                        	NodePool.Point from = unit.getLocation();
                            unit.next();
                            NodePool.Point to = unit.getLocation();
                            if (!allReached) {
                            	String mvmt = nextMvmt(from.x,from.z,to.x,to.z);
								if (!mvmt.equals("Z")) {
									print = true;
									ligne+= "\""+String.valueOf(unit.id) + "\" : ";
									ligne+="\""+mvmt+"\" ,";
									step.put(String.valueOf(unit.id),mvmt);
								}    
                            }
                            unitTargets.put(unit.id, unit.getLocation());
                        }
                        try {
                        for (Unit unit : coordinater.units.values()) {
                        	Point current = unitPositions.get(unit.id);
                            Unit.PathPoint target = unit.getTarget();
                        }}catch (Exception npe) {
                        	System.out.print("Erreur dans création du fichier JSON");
                            npe.printStackTrace();
                        }
                        int fps = 25;
                        for (int i = 0; i < fps; i++) {
                            for (Unit unit : coordinater.units.values()) {
                                Point current = unitPositions.get(unit.id);
                                NodePool.Point target = unitTargets.get(unit.id);
                           
                                if (current == null) {
                                    current = new Point(target);
                                    unitPositions.put(unit.id, current);
                                }
                                float move = 1f / fps;
                                float dX = target.x - current.x;
                                float dZ = target.z - current.z;
                                
                                current.x = (Math.abs(dX) < move) ? target.x : current.x + Math.signum(dX) * move;
                                current.z = (Math.abs(dZ) < move) ? target.z : current.z + Math.signum(dZ) * move;
                                
                            }
                            SwingUtilities.invokeAndWait(new Runnable() {
                                public void run() {
                                    paintImmediately(PathPanel.this.getBounds());
                                }
                            });
                            Thread.sleep(10/fps);
                        }
                        if (!step.isEmpty()) steps.add(step);
                    }catch (Exception npe) {
                        npe.printStackTrace();
                    }

                }
                sampleObject.put("steps", steps);
                try {
                    File myObj = new File("result_"+file+".json");
                    if (myObj.createNewFile()) {
                      System.out.println("File created: " + myObj.getName());
                    } else {
                      System.out.println("File already exists.");
                    }
                  } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                  }
                try {
                    FileWriter myWriter2 = new FileWriter(writePath+String.valueOf(coop)+"_"+file+"_"+method+".json");
                    myWriter2.write(sampleObject.toJSONString());
                    myWriter2.close();
                    System.out.println("Successfully wrote the file.");
                	exec = false;
                  } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                  }
            }
        } .start();
    }
    
    void reset() {
        coordinater.reset();
        
        List<Grid.Node> nodes = new ArrayList<Grid.Node>(grid.nodes.values());
        Collections.shuffle(nodes);
        
        for (int i = 0; i < unitCount; i++) {
            Unit unit = new Unit();
            coordinater.addUnit(unit);
            
            Grid.Node node = nodes.remove(0);
            while (grid.unwalkables.contains(node)) {
                node = nodes.remove(0);
            }
            unit.setLocation(node.x, node.y);
            unitPositions.put(unit.id, new Point(unit.getLocation()));
            
            node = nodes.remove(0);
            while (grid.unwalkables.contains(node)) {
                node = nodes.remove(0);
            }
            unit.setDestination(node.x, node.y);
            
            unit.setPath(new ArrayList<Unit.PathPoint>());
        }
        paintImmediately(getBounds());
    }
    
    class Point {
        float x, z;
        
        Point(float x, float z) {
            this.x = x;
            this.z = z;
        }
        
        Point(NodePool.Point p) {
            this(p.x, p.z);
        }
    }
    public static PathPanel call_file_manual(String file) {
   	 int[] list = new int[2];
        list[0] = coop;
        list[1] =  0;
        Grid.modifierGrid(grille(50,18,null));
        PathPanel.unitCount = list[1];
        Coordinater coordinater = new Coordinater(list[0]);
        PathPanel pathPanel = new PathPanel(coordinater);
        pathPanel.setPreferredSize(new Dimension(1080, 720));
        pathPanel.reset();
    	String filename = file; 
        FileReader reader = null;
		try {
			reader = new FileReader(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject)jsonParser.parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ArrayList<ArrayList<Long>> starts = (ArrayList<ArrayList<Long>>)jsonObject.get("starts");
        ArrayList<ArrayList<Long>> targets = (ArrayList<ArrayList<Long>>)jsonObject.get("targets");
        int mkspan = 0;
        int distTotal = 0;
        int mkspanTemp =0;
        for(int i=0; i< starts.size();i++)
        {
        	List<Long> s = starts.get(i);
        	List<Long> d = targets.get(i);
        	Long s0 = (Long) s.get(0);
        	Long s1 = (Long) s.get(1);
        	Long d0 = (Long) d.get(0);
        	Long d1 = (Long) d.get(1);
        	
        	Unit unit = new Unit();
        	mkspanTemp = Math.abs(s1.intValue()-d1.intValue())+Math.abs(s0.intValue()-d0.intValue());
        	if (mkspanTemp > mkspan) mkspan = mkspanTemp;
        	distTotal+=mkspanTemp;
        	unit.setLocation(s1.intValue(),s0.intValue());
        	unit.setDestination(d1.intValue(),d0.intValue());
        	coordinater.addUnit(unit);
        }
        System.out.println("Le Makespan est au moins de : "+String.valueOf(mkspan));
        System.out.println("La distance totale est au moins de : "+String.valueOf(distTotal));
        return pathPanel;
    }
    public static String[] grille(int n,int m,ArrayList<ArrayList<Long>> obstacles) {
    	Queue<int[]> obs = new LinkedList<int[]>();
    	if (obstacles != null) {
	    	for(int i=0; i< obstacles.size();i++)
	        {
	        	List<Long> s = obstacles.get(i);
	        	Long s0 = (Long) s.get(0);
	        	Long s1 = (Long) s.get(1);
	        	obs.add(new int[]{PathPanel.offset + s0.intValue(),PathPanel.offset + s1.intValue()} );
	        } 
    	}
    	String[] result = new String[n+2*PathPanel.offset];
    	int[] gK = new int[] {100000000,100000000};
    	int[] current;
    	if (obs.isEmpty()) current = gK;
    	else current = obs.poll();
    	for(int i=0;i<n+2*PathPanel.offset;i++) {
        	String ligne = "";
    		for (int j=0;j<m+2*PathPanel.offset;j++) {
    			if (current.equals(gK)) {
    				ligne = ligne + '.';
    			}
    			else if (current[0] == i && current[1] == j) {
    				ligne = ligne + "X";
    				if (obs.isEmpty()) current = gK;
    				else current = obs.poll();
    			}
    			else {
    				ligne = ligne + ".";
    			}
    		}
    		result[i] = ligne;
    		System.out.print(ligne+"\n");
    	}
    	return result;
    	
    }
    public static PathPanel grille_agents(int n,int m, int a) {
    	 int[] list = new int[2];
         list[0] = coop;
         list[1] = a;
         Grid.modifierGrid(grille(n,m,null));
         PathPanel.unitCount = list[1];
         Coordinater coordinater = new Coordinater(list[0]);
         PathPanel pathPanel = new PathPanel(coordinater);
         pathPanel.setPreferredSize(new Dimension(1080, 720));
         pathPanel.reset();
         return pathPanel;
    }
    
    public static PathPanel call_file_images(String file) {
    	String filename = file; 
        FileReader reader = null;
		try {
			reader = new FileReader(filename);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject)jsonParser.parse(reader);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Long> shape =  (ArrayList<Long>) ((JSONObject) ((JSONObject) ((JSONObject) jsonObject.get ("meta")).get("description")).get("parameters")).get("shape");
		Long n = (Long) shape.get(0);
       	Long m = (Long) shape.get(1);
        ArrayList<ArrayList<Long>> obstacles = (ArrayList<ArrayList<Long>>)jsonObject.get("obstacles");
      	 int[] list = new int[2];
           list[0] =  coop;
           list[1] =  0;
           Grid.modifierGrid(grille(n.intValue(),m.intValue(),obstacles));
           PathPanel.unitCount = list[1];
           Coordinater coordinater = new Coordinater(list[0]);
           PathPanel pathPanel = new PathPanel(coordinater);
           pathPanel.setPreferredSize(new Dimension(1080, 720));
           pathPanel.reset();
           ArrayList<ArrayList<Long>> starts = (ArrayList<ArrayList<Long>>)jsonObject.get("starts");
           ArrayList<ArrayList<Long>> targets = (ArrayList<ArrayList<Long>>)jsonObject.get("targets");
           int mkspan = 0;
           int distTotal = 0;
           int mkspanTemp =0;
           for(int i=0; i< starts.size();i++)
           {
           	List<Long> s = starts.get(i);
           	List<Long> d = targets.get(i);
           	Long s0 = (Long) s.get(0);
           	Long s1 = (Long) s.get(1);
           	Long d0 = (Long) d.get(0);
           	Long d1 = (Long) d.get(1);
        	mkspanTemp = Math.abs(s1.intValue()-d1.intValue())+Math.abs(s0.intValue()-d0.intValue());
        	if (mkspanTemp > mkspan) mkspan = mkspanTemp;
        	distTotal+=mkspanTemp;
           	Unit unit = new Unit();
           	unit.setLocation(PathPanel.offset+s1.intValue(),PathPanel.offset+s0.intValue());
           	unit.setDestination(PathPanel.offset+d1.intValue(),PathPanel.offset+d0.intValue());
           	coordinater.addUnit(unit);
           }
           System.out.println("Le Makespan est au moins de : "+String.valueOf(mkspan));
           System.out.println("La distance totale est au moins de : "+String.valueOf(distTotal));
           return pathPanel;
    }
    public static void main(String[] args) throws Exception {
    	String fileToCall = readPath+file+".json";
       //PathPanel pathPanel = grille_agents(3,3,5);
       PathPanel pathPanel = call_file_manual(fileToCall); //to choose either "manual" . (soc, election, king)
       //PathPanel pathPanel = call_file_images(fileToCall); //to choose instances from "images" (others)
       
        ButtonPanel buttonPanel = pathPanel.new ButtonPanel();
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pathPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        
        JFrame frame = new JFrame("PI project 2: Coordinated Robot Motion Planning");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}
