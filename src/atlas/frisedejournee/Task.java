package atlas.frisedejournee;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

@SuppressWarnings("serial")
public class Task implements Serializable{

	private String nom; // nom de l'activite
	private String description; // description de l'activite en quelques mots
	private double duree; // duree de l'activite en heure
	private double heureDebut; // heure du debut de l'activite
	private int image; // une image illustrant l'activite
	private int couleur; // la couleur de l'activite dans la frise
	
	public Task(String nom, String description, double duree, double heureDebut,
			int image) {
		this.nom = nom;
		this.description = description;
		this.duree = duree;
		this.heureDebut = heureDebut;
		this.image = image;
		this.couleur = Color.BLACK;
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

	public double getHeureFin() {
		return heureDebut+duree;
	}
	
	public int getImage() {
		return image;
	}
	
	public int getCouleur(){
		return couleur;
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

	public void setImage(int image) {
		this.image = image;
	}
	
	public void setCouleur(int couleur){
		this.couleur = couleur;
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
	
	/**
	 * Cree un planning par defaut
	 * @return
	 */
	public static ArrayList<Task> createTasksLouise(Context c){
		int image = R.drawable.dejeuner;
		Task t1 = new Task("Réveil", "C'est l'heure de se réveiller et de se préparer",1,8,image);
		Task t2 = new Task("Accueil", "Tous les enfants arrivent à l'école",1,9,image);
		Task t3 = new Task("Activites manuelles", "On fait des activités manuelles",2,10,image);
		Task t4 = new Task("Déjeuner", "C'est l'heure de manger !",1,12,image);
		Task t5 = new Task("Theatre", "On apprend a jouer la comedie",1.5,13,image);
		Task t6 = new Task("Cours de danse", "J'apprends la danse classique",1.75,14.5,image);
		Task t7 = new Task("Pause", "On prend une pause pour se détendre",0.75,16.25,image);
		Task t8 = new Task("Cours de chant", "On fait des vocalises",1,17,image);
		Task t9 = new Task("Retour à la maison", "On rentre à la maison pour se reposer !",1,18,image);
		Task t10 = new Task("Soiree", "On mange, les dents et au lit !",2,19,image);
		
		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(t1);
		tasks.add(t2);
		tasks.add(t3);
		tasks.add(t4);
		tasks.add(t5);
		tasks.add(t6);
		tasks.add(t7);
		tasks.add(t8);
		tasks.add(t9);
		tasks.add(t10);
		return tasks;
		
	}
	
	/**
	 * Cree un planning par defaut
	 * @return
	 */
	public static ArrayList<Task> createTasksRomain(Context c){
		int image = R.drawable.dejeuner;
		Task t1 = new Task("Réveil", "C'est l'heure de se réveiller et de se préparer",1,8,image);
		Task t2 = new Task("Accueil", "Tous les enfants arrivent à l'école",2,9,image);
		Task t3 = new Task("Jeux", "On fait des activités manuelles et des jeux",1,11,image);
		Task t4 = new Task("Déjeuner", "C'est l'heure de manger !",1,12,image);
		Task t5 = new Task("Temps libre", "On peut s'amuser librement après manger",1,13,image);
		Task t6 = new Task("Cours de Maths", "On étudie les additions et les fractions",1,14,image);
		Task t7 = new Task("Pause", "On prend une pause entre les deux leçons pour se détendre",0.5,15,image);
		Task t8 = new Task("Français", "On étudie la conjugaison et on s'entraîne à la dictée",1,15.5,image);
		Task t9 = new Task("Retour à la maison", "On rentre à la maison pour se reposer !",1,16.5,image);
		Task t10 = new Task("Soiree", "On mange, les dents et au lit !",3.5,17.5,image);
		
		ArrayList<Task> tasks = new ArrayList<Task>();
		tasks.add(t1);
		tasks.add(t2);
		tasks.add(t3);
		tasks.add(t4);
		tasks.add(t5);
		tasks.add(t6);
		tasks.add(t7);
		tasks.add(t8);
		tasks.add(t9);
		tasks.add(t10);
		return tasks;
		
	}
	
	/**
	 * Trouve une activite dans un planning relativement a une activite donne
	 * @param myTasks Le planning
	 * @param t L'activite de reference
	 * @param pas la distance relative a laquelle on veut trouver l'autre activite
	 * @return
	 */
	public static Task findRelativeTask(ArrayList<Task> myTasks,Task t,int pas){
		
		/* Retrouve l'indice de l'activite de reference dans le planning   */
		int id = indexOfTask(myTasks, t);
		
		/* Renvoie la bonne activite */
		if((id+pas>=0) && (id+pas<myTasks.size())){
		  return myTasks.get(id+pas);
		}
		/* Renvoie null si l'activite n'existe pas */
		else{
			return null;
		}
	}
	
	/**
	 * Trouve l'indice d'une activite dans un planning
	 * @param myTasks Le planning
	 * @param t L'activite
	 * @return l'indice
	 */
	public static int indexOfTask(ArrayList<Task> myTasks,Task t){
		
		/* Retrouve l'indice de l'activite de reference dans le planning */
		int id = 0;
		for(int i=0;i<myTasks.size();i++){
			if(myTasks.get(i)==t) id = i;
		}
		
		/* Renvoie la bonne activite */
		return id;
	}

}
