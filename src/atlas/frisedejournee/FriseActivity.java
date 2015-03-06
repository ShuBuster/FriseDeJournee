package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriseActivity extends Activity {

	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des activites

	Button aide = null;
	Button menu = null;
	Button retour = null;
	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		myTasks = new ArrayList<Task>();
		h0 = 8; // debut a 8h
		h1 = 21; // debut a 21h
		colorTab = new int[11];
		colorTab[0] = R.color.vert1; colorTab[1]=R.color.vert2;colorTab[2]=R.color.vert3;colorTab[3]=R.color.bleu1;
		colorTab[4]=R.color.bleu2;colorTab[5]=R.color.jaune1;colorTab[6]=R.color.orange1;colorTab[7]=R.color.orange2;
		colorTab[8]=R.color.orange3;colorTab[9]=R.color.rose;colorTab[10]=R.color.fushia;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frise);

		/* Changement de police du titre */
		TextView txtView1 = (TextView) findViewById(R.id.texte);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		txtView1.setTypeface(externalFont);

		/* Remplissage de mes taches par lecture du fichier */
		Drawable image = getResources().getDrawable(R.drawable.image_dejeuner);
		//myTasks = TaskReader.read(this, "myTasks.txt");
		myTasks = Task.createTasks(this);

		/* Recuperation de la frise et de sa largeur */
		ImageView scope = (ImageView) findViewById(R.id.scope);
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);
		int W = 820;
		int H = 47;

		int color = 0;
		
		for (Task myTask : myTasks) {

			/* Affichage de ma tache sur la frise */
			ImageView rectTask = new ImageView(getApplicationContext());
			int Xbegin = myTask.getXbegin(W, h0, h1);
			int Xwidth = myTask.getXwidth(W, h0, h1);

			/* Creation du rectangle et placement */
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Xwidth, H);
			layoutParams.setMargins(3, 3, 0, 0);
			rectTask.setLayoutParams(layoutParams);
			rectTask.setBackgroundColor(getResources().getColor(colorTab[color]));
			frise.addView(rectTask);
			rectTask.setVisibility(View.VISIBLE);
			color += 1;
		}

		/* Met le scope a l'activite en cours */
		Task currentTask = findCurrentTask();
		if (!(currentTask == null)) {
			int Xmiddle = currentTask.getMiddle(W, h0, h1);
			MarginLayoutParams paramsScope = (MarginLayoutParams) scope.getLayoutParams();
			paramsScope.leftMargin = 250 + Xmiddle;
			scope.setLayoutParams(paramsScope);
		}
		
		/* creation des 3 boutons menu, aide et retour à l'activite precedente*/
		aide = (Button) findViewById(R.id.bouton_aide);
		menu = (Button) findViewById(R.id.bouton_menu);
		retour = (Button) findViewById(R.id.bouton_retour);
		
		menu.setOnClickListener(new View.OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  
	    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
	    	Drawable d = getResources().getDrawable(R.drawable.home_e);
	    	menu.setBackground(d);
	    	
	    	/* Passage a l'autre activite */
	        Intent secondeActivite = new Intent(FriseActivity.this,MenuActivity.class);
	        startActivity(secondeActivite);
	      }});
	      
	      aide.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  
		    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
		    	Drawable d = getResources().getDrawable(R.drawable.help_e);
		    	aide.setBackground(d);
		    	
		    	/* Passage a l'autre activite */
		        Intent secondeActivite = new Intent(FriseActivity.this,MenuAide.class);
		        startActivity(secondeActivite);
		      }});
		      
		      retour.setOnClickListener(new View.OnClickListener() {
			      @Override
			      public void onClick(View v) {
			    	  
			    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
			    	Drawable d = getResources().getDrawable(R.drawable.back_e);
			    	retour.setBackground(d);
			    	
			    	/* Passage a l'autre activite */
			        Intent secondeActivite = new Intent(FriseActivity.this,MenuActivity.class);
			        startActivity(secondeActivite);
			      }
	    });
		
		
	}

	/**
	 * Trouve la tache qui se deroule a l'heure actuelle
	 * @return la tache actuelle
	 */
	Task findCurrentTask() {

		final Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);
		Calendar before = Calendar.getInstance();
		Calendar after = Calendar.getInstance();

		for (Task t : myTasks) { // parcours toutes les tasks

			double hDebut = t.getHeureDebut();
			int heureD = (int) Math.floor(hDebut);
			int minuteD = (int) ((hDebut - heureD) * 0.6);
			before.set(year, month, day, heureD, minuteD);

			double hFin = hDebut + t.getDuree();
			int heureF = (int) Math.floor(hFin);
			int minuteF = (int) ((hFin - heureF) * 0.6);
			after.set(year, month, day, heureF, minuteF);

			if ((now.compareTo(after) == -1) && (now.compareTo(before) == 1))
				return t; // si l'instant actuel est compris entre le debut et la fin de l'activite

		}
		return null;
	}
	
	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume(){
	    super.onResume();
	    final Button boutonRetour = (Button) findViewById(R.id.bouton_retour);
	    Drawable d1 = getResources().getDrawable(R.drawable.back);
    	boutonRetour.setBackground(d1);
    	 final Button boutonMenu = (Button) findViewById(R.id.bouton_menu);
 	    Drawable d2 = getResources().getDrawable(R.drawable.home);
     	boutonMenu.setBackground(d2);
     	 final Button boutonAide = (Button) findViewById(R.id.bouton_aide);
 	    Drawable d3 = getResources().getDrawable(R.drawable.help);
     	boutonAide.setBackground(d3);

	}
	
}
