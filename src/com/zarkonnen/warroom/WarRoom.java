package com.zarkonnen.warroom;

import com.zarkonnen.catengine.Condition;
import com.zarkonnen.catengine.Draw;
import com.zarkonnen.catengine.Fount;
import com.zarkonnen.catengine.Frame;
import com.zarkonnen.catengine.Game;
import com.zarkonnen.catengine.Img;
import com.zarkonnen.catengine.Input;
import com.zarkonnen.catengine.SlickEngine;
import com.zarkonnen.catengine.util.Clr;
import com.zarkonnen.catengine.util.ScreenMode;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;

public class WarRoom implements Game {
	public static void main(String[] args) {
		SlickEngine se = new SlickEngine("You can't fight here, this is the war room!", "/com/zarkonnen/warroom/images/", "/com/zarkonnen/warroom/sounds/", 24);
		se.setup(new WarRoom());
		se.runUntil(Condition.ALWAYS);
	}
	
	
	Fount fount;
	boolean starting = true;
	Img bg1 = new Img("bg1", 0, 0, 556, 184, false);
	Img map = new Img("world", 0, 0, 660, 371, false);
	Img phone = new Img("phone", 0, 0, 52, 85, false);
	Img strangeloveR = new Img("strangelove", 0, 0, 73, 98, false);
	Img strangeloveL = new Img("strangelove", 0, 0, 73, 98, true);
	Img personR = new Img("person", 0, 0, 47, 112, false);
	Img personL = new Img("person", 0, 0, 47, 112, true);
	Img titleImg = new Img("title");
	Img victoryImg = new Img("victory");
	Img failureImg = new Img("failure");
	
	Img[] boom = new Img[10];
	BufferedImage mapImg;
	
	Random r = new Random();
	
	String randomScream() {
		return new String[] {
			"No!",
			"You madman!",
			"What are you doing?",
			"We have to stop the bombers!",
			"Unhand me at once!",
			"Let me to the phone!"
		}[r.nextInt(6)];
	}
	
	String deathScream() {
		return new String[] {
			"Argh!",
			"Ghhk",
			"*gurgle*",
			"*thud*",
			"Aaah!"
		}[r.nextInt(5)];
	}
	
	String attackScream() {
		return new String[] {
			"Get to the phone!",
			"Doctor, get away from the red telephone.",
			"Rush him!",
			"We have to stop the bombers!",
			"The red phone! Quick!"
		}[r.nextInt(5)];
	}
	
	static final String[] PHONE_CONVO = {
		"Hello?",
		"Hello?",
		"Is this bomber command?",
		"Yes!",
		"No, I need to talk to him right now!",
		"Yes, it's urgent!",
		"...",
		"...",
		"...",
		"Call back the bombers!",
		"This is an order.",
		"Yes, I know.",
		"I know!",
		"And what good is that going to do?",
		"Stop arguing.",
		"I'm giving you a direct order.",
		"It's an automated system!",
		"Yes!",
		"I'm sure they're trying to turn it off right now.",
		"Yes!",
		"Now *call back the bombers*!",
		"Thank you."
	};
	
	@Override
	public void input(Input in) {
		ScreenMode sm = in.mode();
		
		if (starting) {
			in.setCursorVisible(false);
			in.setMode(new ScreenMode(800, 600, true));
			starting = false;
			try {
				mapImg = ImageIO.read(WarRoom.class.getResourceAsStream("images/world.png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (title || population <= 0 || defeat) {
			if (in.clicked() != null) {
				reset();
			}
			return;
		}
		
		if (in.keyPressed("SPACE")) {
			paused = !paused;
		}
		
		if (paused) { return; }
		
		if (in.keyDown("LEFT")) {
			strangeloveX -= in.msDelta() / 4;
			if (strangling != null) {
				strangling.x -= in.msDelta() / 4;
			} else {
				strangeloveLeft = true;
			}
		}
		
		if (in.keyDown("RIGHT")) {
			strangeloveX += in.msDelta() / 4;
			if (strangling != null) {
				strangling.x += in.msDelta() / 4;
			} else {
				strangeloveLeft = false;
			}
		}
		
		if (strangling != null && (strangeloveX < -73 || strangeloveX > sm.width)) {
			strangling.hp -= in.msDelta();
			if (strangling.hp <= 0) {
				people.remove(strangling);
				killCount++;
				strangling = null;
				screams.add(new Scream(deathScream(), strangeloveX < 0, strangeloveX < 0 ? 5 : sm.width - 5));
			} else {
				strangling.shoutTime += in.msDelta();
				if (strangling.shoutTime > 800 && r.nextInt(3) == 1) {
					screams.add(new Scream(randomScream(), strangeloveX < 0, strangeloveX < 0 ? 5 : sm.width - 5));
					strangling.shoutTime -= 800;
				}
			}
		}
		
		spawnTime += in.msDelta();
		
		if (spawnTime > Math.max(3000, population * 4) && r.nextInt(10) == 0) {
			people.add(new Person(r.nextBoolean()));
			spawnTime -= Math.max(3000, population * 4);
		}
		
		int handX = strangeloveX + (strangeloveLeft ? 16 : 57);
		
		for (Person p : people) {
			if (strangling == null) {
				int neckX = p.x + (p.left ? 37 : 10);
				if (Math.abs(neckX - handX) < 11) {
					strangling = p;
					if (phoning == p) {
						phoning = null;
						screams.add(new Scream("*click*", true, sm.width / 2));
						phoneProgress = 0;
						phoneTime = 0;
					}
				}
			}
			if (p != strangling) {
				p.age += in.msDelta();
				if (p.age > 500 && !p.attackShouted && r.nextInt(3) == 1) {
					screams.add(new Scream(attackScream(), false, p.x));
					p.attackShouted = true;
				}
				p.left = p.x + 23 > sm.width / 2;
				if (Math.abs(p.x + 23 - sm.width / 2) < 30) {
					if (phoning == null) {
						phoning = p;
					}
				} else {
					if (p.left) {
						p.x -= in.msDelta() / 10;
					} else {
						p.x += in.msDelta() / 10;
					}
				}
			}
		}
		
		if (phoning != null) {
			phoneTime += in.msDelta();
			if (phoneTime > 800) {
				phoneProgress++;
				if (phoneProgress == PHONE_CONVO.length) {
					defeat = true;
					return;
				} else {
					phoneTime -= 800;
					screams.add(new Scream(PHONE_CONVO[phoneProgress], true, sm.width / 2));
				}
			}
		}
		
		if (r.nextInt(20) == 0) {
			population -= 2 + r.nextInt(30);
			boolean valid = false;
			int x = 0;
			int y = 0;
			while (!valid) {
				x = r.nextInt(map.srcWidth - 32) + 16;
				y = r.nextInt(map.srcHeight - 32) + 16;
				valid = new Color(mapImg.getRGB(x, y)).getRed() < 30;
			}
			impacts.add(new Impact(x, y));
		}
		
		for (Iterator<Scream> it = screams.iterator(); it.hasNext();) {
			Scream s = it.next();
			s.y -= in.msDelta() / 10;
			if (s.y < -100) {
				it.remove();
			}
		}
		
		if (in.keyPressed("ESCAPE")) {
			in.quit();
		}
	}
	
	void reset() {
		impacts.clear();
		people.clear();
		screams.clear();
		spawnTime = 5000;
		population = 3264;
		strangeloveX = 300;
		strangeloveLeft = false;
		strangling = null;
		phoning = null;
		phoneProgress = 0;
		phoneTime = 0;
		defeat = false;
		title = false;
		killCount = 0;
		paused = false;
	}
	
	ArrayList<Streak> streaks = new ArrayList<>();
	ArrayList<Impact> impacts = new ArrayList<>();
	ArrayList<Person> people = new ArrayList<>();
	ArrayList<Scream> screams = new ArrayList<>();
	int spawnTime = 5000;
	int population = 3264;
	int strangeloveX = 300;
	boolean strangeloveLeft;
	Person strangling;
	Person phoning;
	int phoneProgress;
	int phoneTime;
	boolean defeat;
	boolean title = true;
	int killCount = 0;
	boolean paused = false;
	
	static class Impact {
		int x, y, age;

		public Impact(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	static class Streak {
		int x;
		int life;

		public Streak(int x, int life) {
			this.x = x;
			this.life = life;
		}
	}
	
	static class Person {
		int x;
		boolean left;
		int hp = 3000;
		int shoutTime = 0;
		int age = 0;
		boolean attackShouted = false;

		public Person(boolean left) {
			this.left = left;
			x = left ? 810 : -80;
		}
	}
	
	static class Scream {
		int y = 500;
		int x = 0;
		String text;
		boolean left;

		public Scream(String text, boolean left, int x) {
			this.text = text;
			this.left = left;
			this.x = x;
		}
	}

	@Override
	public void render(Frame f) {
		if (fount == null) {
			fount = Fount.fromResource("libmono12", "/com/zarkonnen/warroom/images/libmono12.txt");
			for (int i = 0; i < 10; i++) {
				boom[i] = new Img("boom", i * 32, 0, 32, 32, false);
			}
		}
		Draw d = new Draw(f);
		ScreenMode sm = f.mode();
		// BG Gradient
		for (int bgy = sm.height; bgy >= 0; bgy -= 30) {
			int amt = bgy / 30 + 10;
			d.rect(new Clr(amt, amt, amt), 0, sm.height - bgy, sm.width, 31);
		}

		if (title) {
			d.blit(titleImg, sm.width / 2 - titleImg.machineWCache / 2, sm.height / 2 - titleImg.machineHCache / 2);
		} else if (defeat) {
			d.blit(failureImg, sm.width / 2 - failureImg.machineWCache / 2, sm.height / 2 - failureImg.machineHCache / 2);
		} else if (population <= 0) {
			d.blit(victoryImg, sm.width / 2 - victoryImg.machineWCache / 2, sm.height / 2 - victoryImg.machineHCache / 2);
		} else {
			d.blit(map, sm.width / 2 - map.srcWidth / 2, 0);
			for (Impact in : impacts) {
				d.blit(boom[Math.min(9, in.age++)], in.x - 16 + sm.width / 2 - map.srcWidth / 2, in.y - 16);
			}
			d.text("[999999]Population: " + population + " million\nUnderground bunkers ready", fount, sm.width / 2 - map.srcWidth / 2 + 5, 5);
			d.blit(bg1, sm.width / 2 - bg1.srcWidth / 2, 230);

			// Floor
			d.rect(new Clr(20, 20, 20), 0, 450, sm.width, 151);
			d.blit(phone, sm.width / 2 - phone.srcWidth / 2, 460);

			for (Person p : people) {
				d.blit(p.left ? personL : personR, p.x, 473);
			}

			d.blit(strangeloveLeft ? strangeloveL : strangeloveR, strangeloveX, 470);

			for (Iterator<Scream> it = screams.iterator(); it.hasNext();) {
				Scream s = it.next();
				if (s.left) {
					d.text(s.text, fount, s.x, s.y);
				} else {
					d.text(s.text, fount, s.x - d.textSize(s.text, fount).x, s.y);
				}
			}
			
			if (killCount == 0) {
				if (strangling != null && strangling.hp < 3000) {
					if (strangling.hp < 500) {
						d.text("He's nearly dead!", fount, 5, sm.height - 15);
					} else {
						d.text("Keep strangling, Doctor Strangelove!", fount, 5, sm.height - 15);
					}
				} else {
					d.text("Arrow keys to move. Grab the people trying to reach the phone and drag them off-screen.", fount, 5, sm.height - 15);
				}
			}
		}
		
		int lightening = r.nextInt(3);
		d.rect(new Clr(255, 255, 255, lightening), 0, 0, sm.width, sm.height);
		// Streaks
		if (r.nextInt(40) == 1) {
			streaks.add(new Streak(r.nextInt(sm.width), r.nextInt(10)));
		}
		for (Iterator<Streak> it = streaks.iterator(); it.hasNext();) {
			Streak s = it.next();
			if (s.life-- <= 0) {
				it.remove();
			} else {
				d.rect(new Clr(255, 255, 255, 200), s.x, 0, 1 + r.nextInt(2), sm.height);
			}
		}
	}
}
