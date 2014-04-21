package distributed.systems.das.presentation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

import distributed.systems.das.BattleField;
import distributed.systems.das.units.Unit.UnitType;

public class BattleFieldReplayer extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	
	private final Scanner file;
	private final ReplayUnit[][] map = new ReplayUnit[BattleField.MAP_WIDTH][BattleField.MAP_HEIGHT];
	
	public BattleFieldReplayer() throws FileNotFoundException{
		File folder = new File(new File("").getAbsolutePath());
		String[] files = folder.list(new FilenameFilter(){
			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".gametrace");
			}
		});
		String filename = files[0];
		System.out.println("REPLAYING " + filename);
		
		file = new Scanner(new File(filename));
		
		Thread t = new Thread(this);
		t.start();
	}
	
	/**
	 * Paint the battlefield overview. Use a red color
	 * for dragons and a blue one for players. 
	 */
	public void paint(Graphics g) {
		double x = 0;
		double y = 0;
		double xRatio = (double)this.getWidth() / (double)BattleField.MAP_WIDTH;
		double yRatio = (double)this.getHeight() / (double)BattleField.MAP_HEIGHT;
		
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(int i = 0; i < BattleField.MAP_WIDTH; i++, x += xRatio, y = 0)
			for(int j = 0; j < BattleField.MAP_HEIGHT; j++, y += yRatio) {
				
				ReplayUnit u = map[i][j];
				
				if (u == null){
					continue; // Nothing to draw in this sector
				}
				
				UnitType type = u.getType();
				int id = u.getId();

				if (type == UnitType.dragon)
					g.setColor(Color.RED);
				else if (type == UnitType.player)
					g.setColor(Color.BLUE);
				else
					g.setColor(Color.BLACK);
				

				/* Fill the unit color */
				g.fillRect((int)x + 1, (int)y + 1, (int)xRatio - 1, (int)yRatio - 1);

				/* Draw the identifier */
				g.setColor(Color.WHITE);
				g.drawString("" + id, (int)x, (int)y + 15);
				g.setColor(Color.BLACK);

				/* Draw a rectangle around the unit */
				g.drawRect((int)x, (int)y, (int)xRatio, (int)yRatio);
			}

	}
	
	public static void main(String[] args) throws FileNotFoundException{
		BattleFieldReplayer bfr = new BattleFieldReplayer();
		JFrame f = new JFrame("Battlefield Replay");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(bfr);
		f.setMinimumSize(new Dimension(200, 200));
		f.setSize(1000, 1000);
		f.setVisible(true);
	}

	@Override
	public void run() {
		String line;
		String ptime = null;
		HashMap<String, UnitType> types = new HashMap<String, UnitType>();
		
		while (file.hasNextLine() && (line=file.nextLine())!=null){
			String[] elements = line.split("[,]");
			String unit = elements[1].trim();
			String timestamp = elements[2].trim();
			String event = elements[3].trim();
			String eventp2 = null;
			String eventp3 = null;

			try {
				if (ptime != null){
					long cts = Long.parseLong(timestamp);
					long pts = Long.parseLong(ptime);
					long sleep = cts-pts;
					if (sleep > 0)
						Thread.sleep(sleep);
				}
			} catch (InterruptedException e) {}
			ptime = timestamp;
			
			long painttime = System.currentTimeMillis();
			
			if (event.startsWith("SPAWN") || event.startsWith("MOVED")){
				eventp2 = elements[4].trim();
				eventp3 = elements[5].trim();
			} else if (event.startsWith("REMOVED")) {
				Point unitpoint = findUnit(Integer.parseInt(unit.substring(1)));
				if (unitpoint != null)
					map[unitpoint.x][unitpoint.y] = null;
				continue;
			} else {
				continue;
			}

			if (event.startsWith("SPAWN")) {
				// SPAWN
				UnitType type = UnitType.values()[Integer.parseInt(event.substring(event.length()-1))];
				int x = Integer.parseInt(eventp2);
				int y = Integer.parseInt(eventp3.substring(0, eventp3.length()-1));
				types.put(unit, type);
				map[x][y] = new ReplayUnit(type, unit);
			} else {
				// MOVE
				int ox = Integer.parseInt(event.substring(7));
				int oy = Integer.parseInt(eventp2.substring(0, eventp2.indexOf(')')));
				int nxi = eventp2.indexOf('(');
				int nx = Integer.parseInt(eventp2.substring(nxi+1, eventp2.length()));
				int ny = Integer.parseInt(eventp3.substring(0, eventp3.indexOf(')')));
				
				map[nx][ny] = new ReplayUnit(types.get(unit), unit);
				map[ox][oy] = null;
			}
			
			invalidate();
			repaint();
			
			painttime = System.currentTimeMillis() - painttime;
			ptime = (Long.parseLong(ptime) + painttime) + "";
		}
		
		System.out.println("Replay done!");
		
		file.close();
	}
	
	private Point findUnit(int id){
		for(int i = 0; i < BattleField.MAP_WIDTH; i++) {
			for(int j = 0; j < BattleField.MAP_HEIGHT; j++) {
				if (map[i][j] != null && (map[i][j].getId() == id))
					return new Point(i,j);
			}
		}
		return null;
	}
	
	private class ReplayUnit{
		
		private final UnitType type;
		private final int id;
		
		public ReplayUnit(UnitType type, String id){
			this.type = type;
			this.id = Integer.parseInt(id.substring(1));
		}

		public UnitType getType() {
			return type;
		}

		public int getId() {
			return id;
		}

	}
}
