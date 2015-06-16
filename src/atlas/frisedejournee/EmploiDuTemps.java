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
