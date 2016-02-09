package atlas.frisedejournee;

import java.io.Serializable;


public class Options implements Serializable{
	

	private static final long serialVersionUID = 2326908928825055759L;
	private boolean sound;
	private boolean gnar;
	private boolean horloge;
	private boolean sommaire;
	private boolean aide;	
	
	/**
	 * @param sound
	 * @param gnar
	 * @param horloge
	 * @param sommaire
	 * @param aide
	 */
	public Options(boolean sound, boolean gnar, boolean horloge,
			boolean sommaire, boolean aide) {
		this.sound = sound;
		this.gnar = gnar;
		this.horloge = horloge;
		this.sommaire = sommaire;
		this.aide = aide;
	}
	public boolean getHorloge() {
		return horloge;
	}
	public void setHorloge(boolean horloge) {
		this.horloge = horloge;
	}
	public boolean getGnar() {
		return gnar;
	}
	public void setGnar(boolean gnar) {
		this.gnar = gnar;
	}
	public boolean getSound() {
		return sound;
	}
	public void setSound(boolean sound) {
		this.sound = sound;
	}
	public boolean getAide() {
		return aide;
	}
	public void setAide(boolean aide) {
		this.aide = aide;
	}
	public boolean getSommaire() {
		return sommaire;
	}
	public void setSommaire(boolean sommaire) {
		this.sommaire = sommaire;
	}
	
	
	
}

