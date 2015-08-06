package atlas.frisedejournee;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class EmploiDuTemps implements Serializable{
	private ArrayList<Task> emploi;
	private String nomEnfant;
	private Double[] marqueTemps;
	
	public Double[] getMarqueTemps() {
		return marqueTemps;
	}

	public void setMarqueTemps(Double[] marqueTemps) {
		this.marqueTemps = marqueTemps;
	}

	public EmploiDuTemps(ArrayList<Task> myTasks, String nom,Double[] marqueTemps){
		nomEnfant = nom;
		emploi = myTasks;
		this.marqueTemps = marqueTemps;
	}

	public static EmploiDuTemps emploiTest(){
	    final ArrayList<Task> tasks = new ArrayList<Task>();
	    final String  nom = "enfantTest";
	    final Double [] heuresMarquees = {7.0,12.0,14.0,17.0,19.0,20.5};

	    final Task t1 = new Task("Accueil","On arrive a l'ecole",1.0,8.0,"maison");
	    tasks.add(t1);
	    final Task t2 = new Task("Sport","On joue au foot en equipe",1.5,9.0,"jeu");
	    tasks.add(t2);
	    final Task t3 = new Task("Lecon","On apprend a lire",1.0,10.5,"cours");
	    tasks.add(t3);
	    final Task t4 = new Task("Temps libre","On fait ce qu'on veut",0.5,11.5,"etoile");
	    tasks.add(t4);
	    final Task t5 = new Task("Cantine","On se lave les mains",1.0,12.0,"dejeuner");
	    tasks.add(t5);
	    final Task t6 = new Task("Temps calme","On s'amuse calmement",1.0,13.0,"maison");
	    tasks.add(t6);
	    final Task t7 = new Task("Sortie","On va au cinema",2.0,14.0,"dodo");
	    tasks.add(t7);
	    final Task t8 = new Task("Chant","La chorale de l'ecole !",1.0,16.0,"cours");
	    tasks.add(t8);
	    final Task t9 = new Task("Temps libre","On joue librement",0.5,17.0,"etoile");
	    tasks.add(t9);
	    final Task t10 = new Task("Parents","La venue des parents",1.5,17.5,"jeu");
	    tasks.add(t10);
	    final Task t11 = new Task("Repas","Le repas du soir",1.0,19.0,"dejeuner");
	    tasks.add(t11);
	    final Task t12 = new Task("Temps libre","On se lave les dents !",0.5,20.0,"etoile");
	    tasks.add(t12);
	    final Task t13 = new Task("Dodo","On va se coucher",0.5,20.5,"dodo");
	    tasks.add(t13);
        return new EmploiDuTemps(tasks,nom,heuresMarquees);
	}

	
	
	public ArrayList<Task> getEmploi() {
		return emploi;
	}

	public String getNomEnfant() {
		return nomEnfant;
	}

	public void setEmploi(ArrayList<Task> emploi) {
		this.emploi = emploi;
	}

	public void setNomEnfant(String nomEnfant) {
		this.nomEnfant = nomEnfant;
	}
	
}
