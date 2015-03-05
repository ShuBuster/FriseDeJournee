package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FriseActivity extends Activity {

	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise

	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		myTasks = new ArrayList<Task>();
		h0 = 8; // debut a 8h
		h1 = 21; // debut a 21h
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frise);

		/* Cahngement de police du titre */
		TextView txtView1 = (TextView) findViewById(R.id.texte);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		txtView1.setTypeface(externalFont);

		/* Remplissage de mes taches par lecture du fichier */
		// Drawable image =
		// getResources().getDrawable(R.drawable.image_dejeuner);
		// Task myTask = new Task("Dejeuner", "C'est l'heure de manger", 2,
		// 14,image);
		myTasks = TaskReader.read(this, "myTasks.txt");

		/* Recuperation de la frise et de sa largeur */
		ImageView scope = (ImageView) findViewById(R.id.scope);
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);
		int W = 820;
		int H = 47;

		for (Task myTask : myTasks) {

			/* Affichage de ma tache sur la frise */
			ImageView rectTask = new ImageView(getApplicationContext());
			int Xbegin = myTask.getXbegin(W, h0, h1);
			int Xwidth = myTask.getXwidth(W, h0, h1);

			/* Creation du rectangle et placement */
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(Xwidth, H);
			layoutParams.setMargins(Xbegin + 3, 3, 0, 0);
			rectTask.setLayoutParams(layoutParams);
			rectTask.setBackgroundColor(getResources().getColor(R.color.vert1));
			frise.addView(rectTask);
			rectTask.setVisibility(View.VISIBLE);
		}

		/* Met le scope a l'activite en cours */
		Task currentTask = findCurrentTask();
		if (!(currentTask == null)) {
			int Xmiddle = currentTask.getMiddle(W, h0, h1);
			MarginLayoutParams paramsScope = (MarginLayoutParams) scope.getLayoutParams();
			paramsScope.leftMargin = 250 + Xmiddle;
			scope.setLayoutParams(paramsScope);
		}
	}

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
}
