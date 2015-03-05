package atlas.frisedejournee;

import android.graphics.drawable.Drawable;

public class Task {

	private String nom; // nom de l'activite
	private String description; // description de l'activite en quelques mots
	private double duree; // duree de l'activite en heure
	private double heureDebut; // heure du debut de l'activite
	private Drawable image; // une image illustrant l'activite
	
	public Task(String nom, String description, double duree, double heureDebut,
			Drawable image) {
		this.nom = nom;
		this.description = description;
		this.duree = duree;
		this.heureDebut = heureDebut;
		this.image = image;
	}

	public String getNom() {
		return nom;
	}

	public String getDescription() {
		return description;
	}

	public double getDuree() {
		return duree;
	}

	public double getHeureDebut() {
		return heureDebut;
	}

	public Drawable getImage() {
		return image;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDuree(int duree) {
		this.duree = duree;
	}

	public void setHeureDebut(int heureDebut) {
		this.heureDebut = heureDebut;
	}

	public void setImage(Drawable image) {
		this.image = image;
	}
	
	/**
	 * Donne la position en pixel du debut de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la position du debut
	 */
	public int getXbegin(int W, double h0, double h1){
		return (int) ((W/(h1-h0))*heureDebut - (h0*W)/(h1-h0));
	}
	
	/**
	 * Donne la largeur en pixel de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la largeur
	 */
	public int getXwidth(int W,double h0,double h1){
		return (int) ((duree*W)/(h1-h0));
	}
	
	/**
	 * Donne la position en pixel du milieu de l'activite sur la frise
	 * @param W longueur de la frise en pixel
	 * @param h0 heure de debut de la frise
	 * @param h1 heure de fin de la frise
	 * @return la largeur du milieu
	 */
	public int getMiddle(int W,double h0,double h1){
		int begin = getXbegin(W, h0, h1);
		int width = getXwidth(W, h0, h1);
		return begin + width/2;
	}

}
