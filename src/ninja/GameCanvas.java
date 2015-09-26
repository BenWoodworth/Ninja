package ninja;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import ninja.state.StateManager;
import ninja.textures.Textures;

public class GameCanvas extends Canvas implements Runnable, KeyListener {
	private static final long serialVersionUID = 6644375181764124582L;


	public static boolean debug = false;
	public static boolean fastForward = false;
	public static final int BUFFERS = 3;

	//Image
	public static final int WIDTH = 420;
	public static final int HEIGHT = 320;
	public static final int SCALE = 2;

	//Thread
	private Thread thread;
	public static boolean running = false;
	private final int maxTps = 60;

	//State Manager
	StateManager stateManager;

	public GameCanvas(StateManager stateManager) {
		this.stateManager = stateManager;
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		addKeyListener(this);
		setFocusable(true);
		requestFocus();
	}

	@Override
	public void addNotify(){
		super.addNotify();
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public void tick() {
		stateManager.tick();
	}

	public void draw() {
		if (getBufferStrategy() == null) createBufferStrategy(BUFFERS);
		Graphics g = getBufferStrategy().getDrawGraphics();
		g.setColor(Color.white);
		g.clearRect(0, 0, getWidth(), getHeight());
		stateManager.draw(g, SCALE);
		if (debug){
			Debug.drawDebug(g, SCALE, fps, stateManager);
		}
		getBufferStrategy().show();
	}

	HashMap<Integer, Boolean> keysPressed = new HashMap<Integer, Boolean>();
	@Override
	public void keyPressed(KeyEvent event) {
		if (keysPressed.get(event.getKeyCode()) == null){
			keysPressed.put(event.getKeyCode(), true);
			stateManager.keyPressed(event.getKeyCode());
			if (event.getKeyCode() == Controls.DEBUG) debug = !debug;
			else if (event.getKeyCode() == Controls.FAST_FORWARD) fastForward = !fastForward;
		}
	}
	@Override
	public void keyReleased(KeyEvent event) {
		keysPressed.put(event.getKeyCode(), null);
		stateManager.keyReleased(event.getKeyCode());
	}
	@Override
	public void keyTyped(KeyEvent event) {
		stateManager.keyTyped(event.getKeyCode());
	}

	public synchronized void pause(){
		keysPressed = new HashMap<Integer, Boolean>();
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public synchronized void unpause(){
		Ninja.gameCanvas.draw();
		notify();
	}

	int fps = 0;
	@Override
	public synchronized void run() {
		Textures.loadTextures();

		long b = 1000000000;

		long minTime = b / maxTps;
		long startTime;
		while (running) {
			startTime = System.nanoTime();
			tick();
			draw();
			if (!fastForward){
				try {
					int sleepTime = (int)(minTime - System.nanoTime() + startTime);
					if (sleepTime > 0) Thread.sleep(sleepTime / 1000000, sleepTime % 1000000);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}
			fps = (int)Math.round((double)b / (System.nanoTime() - startTime));
		}
	}
}
