package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriseActivity extends Activity {

	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private Task scopedTask; // l'activite sur laquelle se trouve le scope
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des
									// activites
	private final int W; // largeur de la frise en px
	private final int H; // hauteur de la frise en px
	private boolean modeManuel = false; // mode manuel desactive au debut

	Button aide = null;
	Button menu = null;
	Button retour = null;
	Button manual = null;
	Button right = null;
	Button left = null;
	RelativeLayout menuDeroulant = null;
	boolean isOpen = false;

	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		myTasks = new ArrayList<Task>();
		scopedTask = null;
		h0 = 8; // debut a 8h
		h1 = 21; // debut a 21h
		W = 820;
		H = 47;
		colorTab = new int[11];
		colorTab[0] = R.color.vert1;
		colorTab[1] = R.color.vert2;
		colorTab[2] = R.color.vert3;
		colorTab[3] = R.color.bleu1;
		colorTab[4] = R.color.bleu2;
		colorTab[5] = R.color.jaune1;
		colorTab[6] = R.color.orange1;
		colorTab[7] = R.color.orange2;
		colorTab[8] = R.color.orange3;
		colorTab[9] = R.color.rose;
		colorTab[10] = R.color.fushia;
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
		// Drawable image =
		// getResources().getDrawable(R.drawable.image_dejeuner);
		// myTasks = TaskReader.read(this, "myTasks.txt");
		myTasks = Task.createTasks(this);

		/* Recuperation de la frise */
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);

		int color_indice = 0;
        int task_indice = 0;
		
		for (Task myTask : myTasks) {

			/* Affichage de ma tache sur la frise */
			ImageView rectTask = new ImageView(getApplicationContext());
			int Xwidth = myTask.getXwidth(W, h0, h1);

			/* Creation du rectangle et placement */
			LinearLayout.LayoutParams layoutParams;
			if(task_indice != myTasks.size()-1){ // Si ce n'est pas la derniere tache de la journee
			layoutParams = new LinearLayout.LayoutParams(Xwidth, H);
			}
			else{ // si c'est la derniere tache de la journee
			layoutParams = new LinearLayout.LayoutParams(Xwidth-3*(myTasks.size()-2), H);
			}
			layoutParams.setMargins(3, 3, 0, 0);
			rectTask.setLayoutParams(layoutParams);

			int couleur = getResources().getColor(colorTab[color_indice]);
			myTask.setCouleur(couleur); // on associe a la tache sa couleur
			rectTask.setBackgroundColor(couleur);
			frise.addView(rectTask);
			rectTask.setVisibility(View.VISIBLE);
			color_indice += 1;
			task_indice += 1;
		}

		/* Met le scope a l'activite en cours */
		// Task currentTask = findCurrentTask();
		Task currentTask = myTasks.get(4);
		if (!(currentTask == null)) {
			scopedTask = currentTask;
			replaceScope(); // place le scope sur la tahce
			displayTask(); // affiche les infos de la tache
		}

		/* creation des 3 boutons menu, aide et retour à l'activite precedente */
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
				Intent secondeActivite = new Intent(FriseActivity.this,
						MenuActivity.class);
				startActivity(secondeActivite);
			}
		});

		retour.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				Drawable d = getResources().getDrawable(R.drawable.back_e);
				retour.setBackground(d);

				/* Passage a l'autre activite */
				Intent secondeActivite = new Intent(FriseActivity.this,
						MenuActivity.class);
				startActivity(secondeActivite);
			}
		});

		// creation du menu deroulant de l'aide

		TextView texte = (TextView) findViewById(R.id.texte_aide);
		texte.setBackgroundColor(getResources().getColor(colorTab[4]));
		Resources res = getResources();
		String nomEnfant = res.getString(R.string.aide);

		TextView vue = (TextView) findViewById(R.id.texte_aide);

		vue.setText(nomEnfant);

		// on recupere le menu a derouler
		menuDeroulant = (RelativeLayout) findViewById(R.id.menuDeroulant);
		menuDeroulant.setVisibility(View.GONE);
		aide = (Button) findViewById(R.id.bouton_aide);
		// On récupère le bouton pour cacher/afficher le menu
		// On rajoute un Listener sur le clic du bouton...
		aide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View vue) {

				isOpen = toggle(menuDeroulant, isOpen);

				// ...pour afficher ou cacher le menu
				if (isOpen) {
					// Si le Slider est ouvert...
					// ... on change le bouton d'aide en mode appuye
					Drawable d = getResources().getDrawable(R.drawable.help_e);
					aide.setBackground(d);
				} else {
					// Sinon on remet le bouton en mode "lache"
					Drawable d = getResources().getDrawable(R.drawable.help);
					aide.setBackground(d);
				}
			}
		});

		/* creation du mode manuel */
		manual = (Button) findViewById(R.id.bouton_manual);
		left = (Button) findViewById(R.id.bouton_left);
		right = (Button) findViewById(R.id.bouton_right);
		Log.d("tag", "scoped task = " + Task.indexOfTask(myTasks, scopedTask));

		manual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (modeManuel) { // si on est en mode manuel

					manual.setBackground(getResources().getDrawable(
							R.drawable.manual)); // desenfonce le bouton
					modeManuel = false;
					left.setEnabled(false); // desactivation des boutons droite et gauche
					right.setEnabled(false);
					left.setVisibility(View.INVISIBLE); // disparition des
														// boutons droite et gauche
					right.setVisibility(View.INVISIBLE);
					moveScopeToCurrentTask(); // retour du scope a l'activite courante
				} else { // si on n'est pas en mode manuel

					/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
					Drawable d = getResources()
							.getDrawable(R.drawable.manual_e);
					manual.setBackground(d);

					/* passage en mode manuel */
					modeManuel = true;
					left.setEnabled(true); // activation des boutons
					right.setEnabled(true);
					left.setVisibility(View.VISIBLE); // affichage des boutons
														// fleches
					right.setVisibility(View.VISIBLE);
				}

			}
		});

		left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				moveScope(-1); // deplace le scope d'une activite vers l'arriere
				displayTask(); // affiche la tache scoped au centre
				left.setEnabled(false); // desactive les boutons pendant l'animation du scope
				right.setEnabled(false);
			}
		});

		right.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				moveScope(1); // deplace le scope d'une activite vers l'avant
				displayTask(); // affiche la tache scoped au centre
				left.setEnabled(false); // desactive les boutons pendant l'animation du scope
				right.setEnabled(false);

			}
		});

	}

	/**
	 * Utilisée pour ouvrir ou fermer le menu.
	 * 
	 * @return true si le menu est désormais ouvert.
	 */
	public boolean toggle(RelativeLayout menuDeroulant, boolean isOpen) {
		int SPEED = 300;
		// Animation de transition.
		AnimationSet animationSet = new AnimationSet(true);
		TranslateAnimation animation = null;

		// On passe de ouvert à fermé (ou vice versa)
		isOpen = !isOpen;

		// Si le menu est déjà ouvert
		if (isOpen) {
			// Animation de translation du bas vers le haut
			animation = new TranslateAnimation(0.0f, 0.0f,
					-menuDeroulant.getHeight(), 0.0f);
			animation.setAnimationListener(openListener);
			animationSet.setAnimationListener(openListener);
		} else {
			// Sinon, animation de translation du haut vers le bas
			animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
					-menuDeroulant.getHeight());
			animation.setAnimationListener(closeListener);
			animationSet.setAnimationListener(closeListener);
		}

		// On détermine la durée de l'animation
		animation.setDuration(SPEED);
		// On ajoute un effet d'accélération
		animation.setInterpolator(new AccelerateInterpolator());
		// Enfin, on lance l'animation
		animationSet.addAnimation(animation);
		menuDeroulant.startAnimation(animationSet);

		return isOpen;
	}

	/* Listener pour l'animation de fermeture du menu */
	Animation.AnimationListener closeListener = new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			// On dissimule le menu
			menuDeroulant.setVisibility(View.GONE);
		}

		public void onAnimationRepeat(Animation animation) {

		}

		public void onAnimationStart(Animation animation) {

		}
	};

	/* Listener pour l'animation d'ouverture du menu */
	Animation.AnimationListener openListener = new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
		}

		public void onAnimationRepeat(Animation animation) {
		}

		public void onAnimationStart(Animation animation) {
			// On affiche le menu
			menuDeroulant.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * Trouve la tache qui se deroule a l'heure actuelle
	 * 
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
				return t; // si l'instant actuel est compris entre le debut et
							// la fin de l'activite

		}
		return null;
	}

	/**
	 * Deplace le scope vers l'avant ou l'arriere avec une animation
	 * 
	 * @param pas
	 *            le nombre relatif de pas en nombre d'activite a faire
	 */
	public void moveScope(final int pas) {

		Task oldScopedTask = scopedTask;
		Task nextScopedTask = Task.findRelativeTask(myTasks, scopedTask, pas);
		if(nextScopedTask!=null){
		this.scopedTask = nextScopedTask;
		ImageView scope = (ImageView) findViewById(R.id.scope);

		/* Creation de l'animation */

		final int x1 = oldScopedTask.getXwidth(W, h0, h1);
		final int x2 = nextScopedTask.getXwidth(W, h0, h1);

		// Translation

		AnimationSet animationSet = new AnimationSet(true);
		int XDelta = nextScopedTask.getXbegin(W, h0, h1)
				- oldScopedTask.getXbegin(W, h0, h1);
		TranslateAnimation translate = new TranslateAnimation(0, (XDelta)
				* (x1 / x2) + pas * (x1 / x2) * 3 + 7, 0, 0);
		translate.setDuration(1000);
		//translate.setStartOffset(500);
		animationSet.addAnimation(translate);

		// Mise a l'echelle

		double ratio = (double) x2 / x1;
		float ratioF = (float) ratio;
		ScaleAnimation scale = new ScaleAnimation(1, ratioF, 1, 1);
		scale.setDuration(1000);
		animationSet.addAnimation(scale);

		animationSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				replaceScope(); // replace vraiment le scope a sa nouvelle
								// position
				left.setEnabled(true);
				right.setEnabled(true);
			}
		});

		scope.startAnimation(animationSet);
		}
	}

	/**
	 * Fait revenir le scope a la tache courante avec animation
	 */
	public void moveScopeToCurrentTask() {
		Task currentTask = findCurrentTask();
		int pas = Task.indexOfTask(myTasks,currentTask) - Task.indexOfTask(myTasks,scopedTask);
		moveScope(pas);
		displayTask();
	}
	/**
	 * Replace le scope a la position scopedTask sans animation
	 */
	public void replaceScope() {

		ImageView scope = (ImageView) findViewById(R.id.scope);
		int indice = Task.indexOfTask(myTasks, scopedTask);
		int XBegin = scopedTask.getXbegin(W, h0, h1);
		int XWidth = scopedTask.getXwidth(W, h0, h1);
		MarginLayoutParams paramsScope = (MarginLayoutParams) scope
				.getLayoutParams();
		paramsScope.width = XWidth + 20;
		paramsScope.leftMargin = 290 + XBegin + indice * 3;
		scope.setLayoutParams(paramsScope);

	}

	/**
	 * Affiche les informations de la scopedTask au milieu de l'ecran
	 */
	public void displayTask() {

		/* Recuperation du cadre et modification de sa couleur */
		ImageView cadre = (ImageView) findViewById(R.id.frame);
		int couleur = scopedTask.getCouleur(); // recuperation de la couleur
		cadre.setBackgroundColor(couleur);

		/* Affichage du titre de l'activite */
		TextView titreTask = (TextView) findViewById(R.id.titreTask);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		titreTask.setTypeface(externalFont);
		titreTask.setText(scopedTask.getNom());

		/* Affichage de l'image de l'activite */
		ImageView imageTask = (ImageView) findViewById(R.id.imageTask);
		imageTask.setBackground(scopedTask.getImage());

		/* Affiche l'heure de debut l'activite */
		// colore les rectangles de fond
		TextView heure10 = (TextView) findViewById(R.id.heure_dizaine);
		TextView heure1 = (TextView) findViewById(R.id.heure_unite);
		TextView minute10 = (TextView) findViewById(R.id.minute_dizaine);
		TextView minute1 = (TextView) findViewById(R.id.minute_unite);
		heure10.setBackgroundColor(darkenColor(scopedTask.getCouleur()));
		heure1.setBackgroundColor(darkenColor(scopedTask.getCouleur()));
		minute10.setBackgroundColor(darkenColor(scopedTask.getCouleur()));
		minute1.setBackgroundColor(darkenColor(scopedTask.getCouleur()));
		// rempli les cases avec l'heure
		String[] splitedHour = splitHour(scopedTask.getHeureDebut());
		heure10.setText(splitedHour[0]);
		heure1.setText(splitedHour[1]);
		minute10.setText(splitedHour[2]);
		minute1.setText(splitedHour[3]);
		
	}
	
	/**
	 * Separe les 4 chiffres qui constituent une heure
	 * @param hour l'heure
	 * @return le tableau rempli avec les chiffres
	 */
	public String[] splitHour(double hour){
		String[] result = new String[4];
		int heure = (int) Math.floor(hour);
		int minute = (int) ((hour - heure) * 0.6);
		
		int heure_dizaine = (int) Math.floor(heure/10);
		result[0] = String.valueOf(heure_dizaine);
		int heure_unite = heure - 10*heure_dizaine;
		result[1] = String.valueOf(heure_unite);
		
		int minute_dizaine = (int) Math.floor(minute/10);
		result[2] = String.valueOf(minute_dizaine);
		int minute_unite = minute - 10*minute_dizaine;
		result[3] = String.valueOf(minute_unite);
		
		return result;
	}
	
	/**
	 * Assombrit une couleur
	 * @param color la couleur a assombrir
	 * @return la couleur assombrie
	 */
	public int darkenColor(int color){
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.85f;
		color = Color.HSVToColor(hsv);
		return color;
	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
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
