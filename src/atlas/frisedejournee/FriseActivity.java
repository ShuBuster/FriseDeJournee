package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
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
import boutons.HomeActivityListener;
import boutons.TTSBouton;

import composants.AnimatedGnar;
import composants.Animer;
import composants.Bulle;
import composants.Couleur;
import composants.Ecran;
import composants.GlowingButton;
import composants.Horloge;
import composants.MyLayoutParams;
import composants.Police;

public class FriseActivity extends Activity {

	private String nomEnfant; // le nom de l'enfant a qui appartient la frise
	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private Task scopedTask; // l'activite sur laquelle se trouve le scope
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des
									// activites
	private int width; // largeur de l'ecran en px
	private int height; // hauteur de l'ecran en px
	private int W; // largeur de la frise en px
	private int H; // hauteur de la frise en px
	private int margin; // marge entre les cases des taches
	private boolean modeAide = false;
	private boolean sommaire_open = false;
	private Task currentTask = null;
	private int nbTask = 0;

	TextView bulle_heure;
	TextView bulle_aide_avant;
	TextView bulle_aide_apres;
	TextView bulle_description;

	TextToSpeech tts;
	TextView info_text = null;
	Button audio = null;
	Button info = null;
	Button aide = null;
	Button menu = null;
	Button sommaire = null;
	ImageView scope = null;
	ImageView logo;
	LinearLayout menuDeroulant = null;
	LinearLayout descriptionDeroulant = null;
	RelativeLayout slide_right = null;
	boolean isOpen = false;
	OnClickListener menu_listenner;
	OnClickListener manual_listenner;

	Timer timer;
	TimerTask timerTask;
	Handler handler = new Handler();
	private Options options;

	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		nomEnfant = "";
		myTasks = new ArrayList<Task>();
		scopedTask = null;
		h0 = 8; // debut a 8h par defaut
		h1 = 21; // fin a 21h par defaut
		W = 0;
		H = 0;
		margin = 0;
		colorTab = new int[15];
		colorTab[11] = R.color.deep_orange2;
		colorTab[12] = R.color.orange2;
		colorTab[13] = R.color.amber2;
		colorTab[14] = R.color.yellow2;
		colorTab[0] = R.color.light_green2;
		colorTab[1] = R.color.green2;
		colorTab[2] = R.color.teal2;
		colorTab[3] = R.color.cyan2;
		colorTab[4] = R.color.light_blue2;
		colorTab[5] = R.color.blue2;
		colorTab[6] = R.color.indigo2;
		colorTab[7] = R.color.deep_purple2;
		colorTab[8] = R.color.purple2;
		colorTab[9] = R.color.pink2;
		colorTab[10] = R.color.red2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Determination densite ecran */
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int density = metrics.densityDpi;
		BitmapFactory.Options option = new BitmapFactory.Options();

		switch (density) {
		case DisplayMetrics.DENSITY_XHIGH:
			Log.d("TAG", "densite= tres haute");
			option.inTargetDensity = DisplayMetrics.DENSITY_XHIGH;
			margin = 12;
			break;

		case DisplayMetrics.DENSITY_HIGH:
			Log.d("TAG", "densite= haute");
			option.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
			margin = 6;
			break;

		case DisplayMetrics.DENSITY_MEDIUM:
			Log.d("TAG", "densite= moyenne");
			option.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
			margin = 3;
			break;

		default:
			Log.d("TAG", "densite= défaut");
			option.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
			break;
		}

		/* Taille ecran */
		int[] size = Ecran.getSize(this);
		width = size[0];
		height = size[1]; // hauteur de l'ecran en px
		Display display = getWindowManager().getDefaultDisplay();
		Point taille = new Point();
		display.getSize(taille);
		H = taille.y;

		/* Passage en plein ecran */
		Ecran.fullScreen(this);
		setContentView(R.layout.activity_frise);

		// recuperation de l'emploi du temps
		Intent i = getIntent();
		EmploiDuTemps emploi = (EmploiDuTemps) i.getSerializableExtra("emploi");
		nomEnfant = emploi.getNomEnfant();

		// recuperation des options
		Intent opt = getIntent();
		options = (Options) opt.getSerializableExtra("options");

		/* Affichage du nom de l'enfant */
		/**
		 * TextView nom_enfant = (TextView) findViewById(R.id.nom_enfant);
		 * Police.setFont(this, nom_enfant, "onthemove.ttf");
		 * nom_enfant.setText(nomEnfant);
		 */

		/* Remplissage des taches selon l'enfant */
		myTasks = emploi.getEmploi();
		setHourBounds();

		/* Ajustement taille frame */
		ImageView frame = (ImageView) findViewById(R.id.frame);
		ImageView frame_p = (ImageView) findViewById(R.id.frame_previous);
		ImageView frame_n = (ImageView) findViewById(R.id.frame_next);
		setSize(frame, 5 * height / 8, 5 * width / 6);
		setSize(frame_p, 5 * height / 9, width / 4);
		setSize(frame_n, 5 * height / 9, width / 4);

		/* Ajustement taille frise */
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);
		H = height / 11; // hauteur de la frise
		W = 3 * width / 4; // largeur de la frise
		setSize(frise, H, W);

		/* Ajustement taille scope */
		ImageView scope = (ImageView) findViewById(R.id.scope);
		setSize(scope, (int) (H * 1.2), 0);

		/* Remplissage de la frise */

		int color_indice = 0;
		int task_indice = 0;

		for (Task myTask : myTasks) {

			addTaskToFrise(myTask, frise, task_indice, color_indice);
			color_indice += 1;
			task_indice += 1;
		}
		nbTask = task_indice;

		/* Met le scope a l'activite en cours */
		currentTask = findCurrentTask();
		if (!(currentTask == null)) {
			scopedTask = currentTask;
			drawRange();
			drawProgress();
			replaceScope(); // place le scope sur la tache
			displayTask(); // affiche les infos de la tache
			displayHour(); // afiche l'heure de la tache
		} else {
			currentTask = myTasks.get(0);
			scopedTask = currentTask;
			replaceScope(); // place le scope sur la tahce
			displayTask(); // affiche les infos de la tache
			displayHour(); // afiche l'heure de la tache
		}

		/* creation des 2 boutons menu, aide */
		aide = (Button) findViewById(R.id.bouton_aide);
		menu = (Button) findViewById(R.id.bouton_menu);
		menu_listenner = new HomeActivityListener(this, menu,
				FriseActivity.this, MenuActivity.class,options,"options");
		menu.setOnClickListener(menu_listenner);

		// creation de l'aide

		// Creation des bulles d'aide

		LinearLayout heure = (LinearLayout) findViewById(R.id.heure_fond);
		info = (Button) findViewById(R.id.description_bouton);
		View aide_parent = findViewById(R.id.aide_parent);
		if (options.getBulle()) {
			bulle_heure = Bulle.create(heure, "L'heure de début de l'activité",
					"right", false, this);
			bulle_aide_avant = Bulle.create(aide_parent,
					"Clique sur ce bouton pour obtenir de l'aide", "right",
					true, this);
			bulle_aide_apres = Bulle.create(aide_parent,
					"Clique sur ce bouton pour sortir de l'aide", "right",
					false, this);
			bulle_description = Bulle.create(info, "Pour afficher" + "\n"
					+ "une description de l'activité", "below", false, this);
			if (Build.VERSION.SDK_INT >= 21) {
				bulle_aide_avant.setElevation(20); // met les bulle au premier
													// plan
				bulle_aide_apres.setElevation(20);
				bulle_heure.setElevation(20);
				bulle_description.setElevation(20);
				bulle_aide_avant.setOutlineProvider(null); // supprime les
															// ombres de
															// l'elevation
				bulle_aide_apres.setOutlineProvider(null);
				bulle_heure.setOutlineProvider(null);
				bulle_description.setOutlineProvider(null);
			}
			if (options.getSound()) {
				TTSBouton.parle(bulle_heure, "L'heure de début de l'activité",
						this);
				TTSBouton.parle(bulle_aide_avant,
						"Clique sur ce bouton pour obtenir de l'aide", this);
				TTSBouton.parle(bulle_aide_apres,
						"Clique sur ce bouton pour sortir de l'aide", this);
				TTSBouton.parle(bulle_description,
						"Pour afficher une description de l'activité", this);
			}
		}

		aide.setOnClickListener(aide_listener);

		// creation de l'information sur l'activite courante

		info = (Button) findViewById(R.id.description_bouton);
		info.setBackgroundColor(currentTask.getCouleur());
		menuDeroulant = (LinearLayout) findViewById(R.id.info);
		audio = (Button) findViewById(R.id.info_audio);
		info_text = (TextView) findViewById(R.id.info_text);
		info_text.setTextColor(getResources().getColor(R.color.yellow3));
		info.setTextColor(getResources().getColor(R.color.yellow3));
		Police.setFont(this, info_text, "intsh.ttf");
		Police.setFont(this, info, "intsh.ttf");

		if (options.getSound())
			TTSBouton.parle(audio, currentTask.getDescription(),
					getApplicationContext());
		else {
			audio.setVisibility(View.INVISIBLE);
			audio.setEnabled(false);
		}
		info.setOnClickListener(info_listener);

		// Ecran logo et apparition frise

	
		 logo = (ImageView) findViewById(R.id.logo_image);
		 logo.setVisibility(View.VISIBLE); AlphaAnimation alpha1 = new
		 AlphaAnimation(0, 1); alpha1.setDuration(500);
		 alpha1.setFillAfter(true);
		 alpha1.setAnimationListener(logo_listener);
		 logo.startAnimation(alpha1);
		 

		/* Gnar anime */

		RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		if (options.getGnar())
			AnimatedGnar.addGnar(this, gnar);

		// Sommaire
		LinearLayout liste_activite = (LinearLayout) findViewById(R.id.liste_activite);
		int indice = 0;
		for(Task task : myTasks){
			Button txt_activite = new Button(getApplicationContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMarginStart(50);
			txt_activite.setLayoutParams(params);
			txt_activite.setText(formatHour(task.getHeureDebut())+" - "+formatHour(task.getHeureFin())+"   "+task.getNom());
			txt_activite.setTextColor(getResources().getColor(R.color.fushia));
			txt_activite.setTextSize(40f);
			txt_activite.setOnClickListener(new TaskListener(indice, this));
			Police.setFont(this, txt_activite, "intsh.ttf");
			liste_activite.addView(txt_activite);
			indice++;
		}
		
		//Sortie du sommaire
		slide_right = (RelativeLayout) findViewById(R.id.slide_right);
		sommaire = (Button) findViewById(R.id.bouton_sommaire);
		if (options.getSommaire()) {
			
			for (Task task : myTasks) {
				TextView txt_activite = new TextView(getApplicationContext());
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.setMarginStart(50);
				txt_activite.setLayoutParams(params);
				txt_activite.setText(formatHour(task.getHeureDebut()) + " - "
						+ formatHour(task.getHeureFin()) + "   "
						+ task.getNom());
				txt_activite.setTextColor(getResources().getColor(
						R.color.fushia));
				txt_activite.setTextSize(30f);
				Police.setFont(this, txt_activite, "intsh.ttf");
				liste_activite.addView(txt_activite);
			}

			// Sortie du sommaire

			Police.setFont(this, sommaire, "intsh.ttf");
			sommaire.setTextSize(20f);
			setSize(slide_right, 0, width / 3);
			slide_right.setTranslationX(width / 3);
			sommaire.setTranslationX(width / 3);
			sommaire.setOnClickListener(sommaire_listener);
		} else {
			sommaire.setVisibility(View.INVISIBLE);
			// sommaire.setEnabled(false);
			liste_activite.setVisibility(View.INVISIBLE);
			slide_right.setVisibility(View.INVISIBLE);
		}

		// Affichage des temps forts
		RelativeLayout parent = (RelativeLayout) findViewById(R.id.slide_top);
		for (double temps_fort : emploi.getMarqueTemps()) {
			int x_pos = Task.getXHour(W, h0, h1, temps_fort);
			TextView txt_temps = new TextView(this);
			txt_temps.setText(" " + formatHour(temps_fort) + " ");
			txt_temps.setTextColor(getResources().getColor(R.color.fushia));
			txt_temps.setTextSize(35f);
			Police.setFont(this, txt_temps, "intsh.ttf");
			MyLayoutParams params = new MyLayoutParams();
			params.alignStart(frise).margins(x_pos - margin, margin * 4, 0, 0);
			parent.addView(txt_temps, params);
		}

		// affichage de l'horloge à l'heure
		RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		if (options.getHorloge()) {
			final Calendar now = Calendar.getInstance();
			Horloge.create(horloge, this, now.get(Calendar.HOUR_OF_DAY),
					now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
			if (options.getSound())
				TTSBouton.parle(horloge, Horloge.dateActuelle(), this);
			// drawProgression();
		} else {
			horloge.setVisibility(View.INVISIBLE);
			horloge.setEnabled(false);
		}

	}

	/**
	 * Utilisee pour ouvrir ou fermer le menu.
	 * 
	 * @return true si le menu est désormais ouvert.
	 */
	public boolean toggle(LinearLayout menuDeroulant, boolean isOpen) {
		int duration = 600;
		// Animation de transition.
		TranslateAnimation animation = null;

		// On passe de ouvert à fermé (ou vice versa)
		isOpen = !isOpen;

		// Si le menu est déjà ouvert
		if (isOpen) {
			// Animation de translation du bas vers le haut
			animation = new TranslateAnimation(0.0f, 0.0f,
					-menuDeroulant.getHeight(), 0.0f);
			animation.setAnimationListener(openListener);
		} else {
			// Sinon, animation de translation du haut vers le bas
			animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
					-menuDeroulant.getHeight());
			animation.setAnimationListener(closeListener);
		}

		// On détermine la durée de l'animation
		animation.setDuration(duration);
		// On ajoute un effet d'accélération
		animation.setInterpolator(new AccelerateInterpolator());
		// Enfin, on lance l'animation
		menuDeroulant.startAnimation(animation);

		return isOpen;
	}

	/* Listener pour l'animation de fermeture du menu */
	Animation.AnimationListener closeListener = new Animation.AnimationListener() {
		public void onAnimationEnd(Animation animation) {
			// On dissimule le menu
			menuDeroulant.setVisibility(View.INVISIBLE);
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
	 * 
	 * @param taskId
	 */
	public void taskClicked(int taskId) {
		Button rectTask = (Button) findViewById(taskId);
		int couleur = getResources().getColor(colorTab[taskId]);
		rectTask.setBackgroundColor(Couleur.lighten(couleur));
		int scopedId = Task.indexOfTask(myTasks, scopedTask);
		int deltaId = taskId - scopedId;
		Task beforeTask = scopedTask;
		moveScope(deltaId);
		displayTask(); // affiche la tache scoped au centre
		changeHour(beforeTask.getHeureDebut());
		currentTask = scopedTask;
		if (isOpen) {
			info.setBackgroundColor(Couleur.lighten(currentTask.getCouleur()));
		} else {
			info.setBackgroundColor(currentTask.getCouleur());
		}

		menuDeroulant.setBackgroundColor(currentTask.getCouleur());
		info_text.setText(currentTask.getDescription() + "\nDuree : "
				+ formatHour(currentTask.getDuree()));
		if (options.getSound())
			TTSBouton.parle(audio, currentTask.getDescription(),
					getApplicationContext());
		// le menu d'information sur l'activite change avec l'activite

	}

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
			int minuteD = getMinute(hDebut);
			before.set(year, month, day, heureD, minuteD);

			double hFin = hDebut + t.getDuree();
			int heureF = (int) Math.floor(hFin);
			int minuteF = getMinute(hFin);
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
		setEnableTaskButton(false);
		int scopedId = Task.indexOfTask(myTasks, scopedTask);
		final int taskId = pas + scopedId;
		Task oldScopedTask = scopedTask;
		Task nextScopedTask = Task.findRelativeTask(myTasks, scopedTask, pas);
		if (nextScopedTask != null) {
			this.scopedTask = nextScopedTask;

			// Affiche progression si tache courante sinon enleve
			Task actual_task = findCurrentTask();

			if (scopedTask == actual_task) {
				drawRange();
				drawProgress();
			} else {
				removeProgression();
			}

			scope = (ImageView) findViewById(R.id.scope);
			/* Creation de l'animation */

			final int x1 = oldScopedTask.getXwidth(W, h0, h1);
			final int x2 = nextScopedTask.getXwidth(W, h0, h1);

			// Translation

			AnimationSet animationSet = new AnimationSet(true);
			animationSet.setDuration(1000);
			// animationSet.setInterpolator(new LinearInterpolator());

			int XDelta = nextScopedTask.getXbegin(W, h0, h1)
					- oldScopedTask.getXbegin(W, h0, h1);
			TranslateAnimation translate = null;
			if (XDelta >= 0) {
				translate = new TranslateAnimation(0, (XDelta) + pas
						* (x1 / x2) * margin + 7, 0, 0);
			} else {
				translate = new TranslateAnimation(0, (XDelta) * (x1 / x2)
						+ pas * (x1 / x2) * margin + 7, 0, 0);
			}
			animationSet.addAnimation(translate);

			// Mise a l'echelle

			double ratio = (double) x2 / x1;
			float ratioF = (float) ratio;
			ScaleAnimation scale = null;
			if (XDelta >= 0) {
				scale = new ScaleAnimation(1f, ratioF, 1f, 1f,
						ScaleAnimation.RELATIVE_TO_SELF, 1f,
						ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
			} else {
				scale = new ScaleAnimation(1f, ratioF, 1f, 1f);
			}
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
					Button rectTask = (Button) findViewById(taskId);
					int couleur = getResources().getColor(colorTab[taskId]);
					rectTask.setBackgroundColor(couleur);
					setEnableTaskButton(true);
					scope.clearAnimation();
					replaceScope(); // replace vraiment le scope a sa nouvelle
									// position
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
		int pas = Task.indexOfTask(myTasks, currentTask)
				- Task.indexOfTask(myTasks, scopedTask);
		Task beforeTask = scopedTask;
		moveScope(pas);
		changeHour(beforeTask.getHeureDebut());
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
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scope
				.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_START, R.id.frise);
		MarginLayoutParams paramsScope = (MarginLayoutParams) scope
				.getLayoutParams();
		paramsScope.width = XWidth + 20;
		paramsScope.leftMargin = XBegin + indice * margin - 7;
		scope.setLayoutParams(paramsScope);

	}

	/**
	 * Affiche les informations de la scopedTask au milieu de l'ecran et de ses
	 * taches voisines
	 */
	public void displayTask() {

		/* Recuperation du cadre et modification de sa couleur */
		ImageView cadre = (ImageView) findViewById(R.id.frame);
		GradientDrawable drawable = (GradientDrawable) cadre.getBackground();
		int couleur = scopedTask.getCouleur(); // recuperation de la couleur
		int couleur_clair = Couleur.lighten(couleur);
		int[] colors = { couleur_clair, couleur };
		drawable.setColors(colors);

		/* Affichage du titre de l'activite */
		TextView titreTask = (TextView) findViewById(R.id.titreTask);
		Police.setFont(this, titreTask, "intsh.ttf");
		titreTask.setText(" " + scopedTask.getNom() + " ");

		/* Affichage de l'image de l'activite */
		ImageView imageTask = (ImageView) findViewById(R.id.imageTask);
		// imageTask.setBackground(getDrawable(scopedTask.getImage()));
		imageTask.setImageDrawable(getDrawable(scopedTask.getImage()));

		/* Affiche l'heure de debut l'activite */
		// colore les rectangles de fond
		LinearLayout heure10 = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		LinearLayout heure1 = (LinearLayout) findViewById(R.id.heure_unite_fond);
		LinearLayout minute10 = (LinearLayout) findViewById(R.id.minute_dizaine_fond);
		LinearLayout minute1 = (LinearLayout) findViewById(R.id.minute_unite_fond);
		int color = scopedTask.getCouleur();
		heure10.setBackgroundColor(color);
		heure1.setBackgroundColor(color);
		minute10.setBackgroundColor(color);
		minute1.setBackgroundColor(color);

		/* Affiche les taches voisines */
		displayNearTasks();
	}

	/**
	 * Affiche de part et d'autre de la tache scoped, les deux taches d'avant et
	 * d'apres
	 */
	public void displayNearTasks() {
		/* Recupere les taches adjacentes */
		Task previous = Task.findRelativeTask(myTasks, scopedTask, -1);
		Task next = Task.findRelativeTask(myTasks, scopedTask, 1);

		ImageView prev = (ImageView) findViewById(R.id.frame_previous);
		ImageView nxt = (ImageView) findViewById(R.id.frame_next);

		/* Affichage de la tache precedente si elle existe */
		if (previous != null) {
			int couleur = previous.getCouleur(); // recuperation de la couleur
			prev.setBackgroundColor(couleur);
			prev.setVisibility(View.VISIBLE);
			prev.setAlpha(0.95f);
		} else {
			prev.setVisibility(View.INVISIBLE);
		}

		/* Affichage de la tache precedente si elle existe */
		if (next != null) {
			int couleur = next.getCouleur(); // recuperation de la couleur
			nxt.setBackgroundColor(couleur);
			sommaire = (Button) findViewById(R.id.bouton_sommaire);
			sommaire.setBackgroundColor(couleur);
			nxt.setVisibility(View.VISIBLE);
			nxt.setAlpha(0.95f);
		} else {
			nxt.setVisibility(View.INVISIBLE);
			int couleur = scopedTask.getCouleur();
			sommaire = (Button) findViewById(R.id.bouton_sommaire);
			sommaire.setBackgroundColor(couleur);
		}

	}

	/**
	 * Affiche l'heure de l'activite actuellement scoped
	 */
	public void displayHour() {
		TextView heure101 = (TextView) findViewById(R.id.heure_dizaine);
		TextView heure11 = (TextView) findViewById(R.id.heure_unite);
		TextView minute101 = (TextView) findViewById(R.id.minute_dizaine);
		TextView minute11 = (TextView) findViewById(R.id.minute_unite);
		String[] splitedHour = splitHour(scopedTask.getHeureDebut());
		heure101.setText(splitedHour[0]);
		heure11.setText(splitedHour[1]);
		minute101.setText(splitedHour[2]);
		minute11.setText(splitedHour[3]);

	}

	/**
	 * Separe les 4 chiffres qui constituent une heure
	 * 
	 * @param hour
	 *            l'heure
	 * @return le tableau de string rempli avec les chiffres
	 */
	public String[] splitHour(double hour) {
		String[] result = new String[4];
		int heure = (int) Math.floor(hour);
		int minute = (int) Math.round((hour - heure) * 60);

		int heure_dizaine = (int) Math.floor(heure / 10);
		result[0] = String.valueOf(heure_dizaine);
		int heure_unite = heure - 10 * heure_dizaine;
		result[1] = String.valueOf(heure_unite);

		int minute_dizaine = (int) Math.floor(minute / 10);
		result[2] = String.valueOf(minute_dizaine);
		int minute_unite = minute - 10 * minute_dizaine;
		result[3] = String.valueOf(minute_unite);

		return result;
	}

	/**
	 * Transforme une heure de type 12,5 en 12h30
	 * 
	 * @param hour
	 * @return
	 */
	public String formatHour(double hour) {
		String[] split = splitHour(hour);
		return split[0] + split[1] + "h" + split[2] + split[3];
	}

	/**
	 * Separe les 4 chiffres qui constituent une heure
	 * 
	 * @param hour
	 *            l'heure
	 * @return le tableau rempli avec les chiffres
	 */
	public int[] splitHour2(double hour) {
		int[] result = new int[4];
		int heure = (int) Math.floor(hour);
		int minute = (int) Math.round((hour - heure) * 60);

		int heure_dizaine = (int) Math.floor(heure / 10);
		result[0] = heure_dizaine;
		int heure_unite = heure - 10 * heure_dizaine;
		result[1] = heure_unite;

		int minute_dizaine = (int) Math.floor(minute / 10);
		result[2] = minute_dizaine;
		int minute_unite = minute - 10 * minute_dizaine;
		result[3] = minute_unite;

		return result;
	}

	/**
	 * Recupere le nb de minutes
	 * 
	 * @param hour
	 *            l'heure
	 * @return le nb de minutes
	 */
	public int getMinute(double hour) {
		int heure = (int) Math.floor(hour);
		int minute = (int) Math.round((hour - heure) * 60);
		return minute;
	}

	/**
	 * Change l'heure de l'afficheur avec une animation
	 * 
	 * @param hour
	 *            l'heure a laquelle se trouve actuellement l'afficheur
	 */
	public void changeHour(double hour) {
		int[] next = splitHour2(scopedTask.getHeureDebut());
		int[] actual = splitHour2(hour);
		int toY = 0;
		LinearLayout fond = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		int h = fond.getHeight();

		for (int i = 0; i <= 3; i++) {
			if (actual[i] < next[i]) {
				toY = -h;
			} else if (actual[i] > next[i]) {
				toY = +h;
			} else if (actual[i] == next[i]) {
				continue; // ne rienf faire pour celui-ci
			}
			TextView view = null;
			TextView view_clone = null;
			switch (i) {
			case 0:
				view = (TextView) findViewById(R.id.heure_dizaine);
				view_clone = (TextView) findViewById(R.id.heure_dizaine_clone);
				break;
			case 1:
				view = (TextView) findViewById(R.id.heure_unite);
				view_clone = (TextView) findViewById(R.id.heure_unite_clone);
				break;
			case 2:
				view = (TextView) findViewById(R.id.minute_dizaine);
				view_clone = (TextView) findViewById(R.id.minute_dizaine_clone);
				break;
			case 3:
				view = (TextView) findViewById(R.id.minute_unite);
				view_clone = (TextView) findViewById(R.id.minute_unite_clone);
				break;
			}
			view_clone.setText(String.valueOf(actual[i]));
			Animer.translate(view_clone, 0, -toY, 0, 0, 1000);
			view.setText(String.valueOf(next[i]));
			Animer.translate(view, 0, -toY, 0, 0, 1000);

		}
	}

	/**
	 * Anime l'heure avec une animation d'echelle en chaîne
	 * 
	 * @throws InterruptedException
	 */
	public void animateHour() throws InterruptedException {
		View hd = findViewById(R.id.heure_dizaine);
		View hu = findViewById(R.id.heure_unite);
		View md = findViewById(R.id.minute_dizaine);
		View mu = findViewById(R.id.minute_unite);

		Animer.scale(hd, 1f, 1.2f, 500, 0, false);
		Animer.scale(hu, 1f, 1.2f, 500, 200, false);
		Animer.scale(md, 1f, 1.2f, 500, 400, false);
		Animer.scale(mu, 1f, 1.2f, 500, 600, false);
	}

	public void setSize(View view, int h, int w) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		if (h == 0) {
			params.height = LayoutParams.WRAP_CONTENT;
		} else {
			params.height = h;
		}
		if (w == 0) {
			params.width = LayoutParams.WRAP_CONTENT;
		} else {
			params.width = w;
		}
		view.setLayoutParams(params);
	}

	@Override
	public void onPause() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onPause();
	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		final Button boutonMenu = (Button) findViewById(R.id.bouton_menu);
		Drawable d2 = getResources().getDrawable(R.drawable.home);
		boutonMenu.setBackground(d2);
		final Button boutonAide = (Button) findViewById(R.id.bouton_aide);
		Drawable d3 = getResources().getDrawable(R.drawable.help);
		boutonAide.setBackground(d3);
		startTimer();
		Ecran.fullScreenResume(this);
	}

	public void setHourBounds() {
		Task first_task = myTasks.get(0);
		h0 = first_task.getHeureDebut();
		Task last_task = myTasks.get(myTasks.size() - 1);
		h1 = last_task.getHeureDebut() + last_task.getDuree();

	}

	public void setEnableTaskButton(boolean b) {
		for (int i = 0; i < nbTask; i++) {
			Button rectTask = (Button) findViewById(i);
			rectTask.setEnabled(b);
		}
	}

	public void addTaskToFrise(Task myTask, LinearLayout frise,
			int task_indice, int color_indice) {
		/* Affichage de ma tache sur la frise */
		Button rectTask = new Button(this);
		int Xwidth = myTask.getXwidth(W, h0, h1);

		/* Creation du rectangle et placement */
		LinearLayout.LayoutParams layoutParams;
		if (task_indice != myTasks.size() - 1) {
			layoutParams = new LinearLayout.LayoutParams(Xwidth,
					LayoutParams.MATCH_PARENT);
		} else { // si c'est la derniere tache de la journee
			layoutParams = new LinearLayout.LayoutParams(Xwidth - margin * 5,
					LayoutParams.MATCH_PARENT);
		}
		layoutParams.setMargins(margin, margin, 0, margin);
		rectTask.setLayoutParams(layoutParams);

		int couleur = getResources().getColor(colorTab[color_indice]);
		myTask.setCouleur(couleur); // on associe a la tache sa couleur
		rectTask.setBackgroundColor(couleur);
		frise.addView(rectTask);
		rectTask.setId(task_indice);
		// rend le bouton clickable
		rectTask.setOnClickListener(new TaskListener(task_indice, this));

	}

	OnClickListener aide_listener = new View.OnClickListener() {

		ImageView glowMenu = null;

		@Override
		public void onClick(View v) {

			if (modeAide) { // on sort du mode aide

				Drawable d = getResources().getDrawable(R.drawable.help);
				aide.setBackground(d); // desenfonce le bouton

				modeAide = false; // on sort du mode aide

				ViewGroup parent = (ViewGroup) info.getParent();
				// RelativeLayout parent = (RelativeLayout)
				// findViewById(R.id.information);
				parent.setClipChildren(true);

				if (glowMenu != null)
					GlowingButton.stopGlow(menu);

				TTSBouton.fermer(menu, getApplicationContext());

				// les boutons retrouvent leurs anciens listenners

				menu.setOnClickListener(menu_listenner);

				info.setEnabled(true);

				// Replacement du bouton aide a droite du bouton menu//
				RelativeLayout.LayoutParams params_aide = (RelativeLayout.LayoutParams) aide
						.getLayoutParams();
				params_aide.addRule(RelativeLayout.END_OF, R.id.bouton_menu);
				aide.setLayoutParams(params_aide);

				// Disparition des bulles d'aide //
				if (options.getBulle()) {
					Animer.fade_out(bulle_aide_apres, 500, false);
					Animer.fade_out(bulle_heure, 500, false);
					Animer.fade_out(bulle_description, 500, false);
				}
			} else { // on entre en mode aide

				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				Drawable d = getResources().getDrawable(R.drawable.help_e);
				aide.setBackground(d);

				modeAide = true; // on passe en mode aide

				// glow sur les autres boutons //

				glowMenu = GlowingButton.makeGlow(menu,
						getApplicationContext(), 118);

				// Replacement du bouton aide a droite du bouton menu//
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) aide
						.getLayoutParams();
				params.addRule(RelativeLayout.END_OF, 118);
				aide.setLayoutParams(params);

				// text to speech sur les boutons //

				if (options.getSound())
					TTSBouton.parle(menu, "pour retourner au menu principal",
							getApplicationContext());

				info.setEnabled(false);

				// Apparitions des bulles d'aide //
				if (options.getBulle()) {
					Animer.fade_in(bulle_heure, 500);
					Animer.fade_in(bulle_description, 500);
					Animer.fade_out(bulle_aide_avant, 500, true);
				}
			}
		}
	};

	OnClickListener info_listener = new View.OnClickListener() {
		@Override
		public void onClick(View vue) {

			isOpen = toggle(menuDeroulant, isOpen);

			// ...pour afficher ou cacher le menu
			if (isOpen) {
				// Si le Slider est ouvert...
				// ... on change le bouton en mode enfonce

				info_text.setText(currentTask.getDescription() + "\nDuree : "
						+ formatHour(currentTask.getDuree()));
				menuDeroulant.setBackgroundColor(currentTask.getCouleur());
				info.setBackgroundColor(Couleur.lighten(currentTask
						.getCouleur()));
				info.setTextColor(getResources().getColor(R.color.yellow5));

			} else {
				// Sinon on remet le bouton en mode "relache"
				info.setBackgroundColor(currentTask.getCouleur());
				info.setTextColor(getResources().getColor(R.color.jaune1));
			}
		}
	};

	AnimationListener  logo_listener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			AlphaAnimation alpha2 = new AlphaAnimation(1, 0);
			alpha2.setDuration(200);
			alpha2.setFillAfter(true);
			alpha2.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					/* Apparition de l'activité */
					RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
					slide_top.setVisibility(View.VISIBLE);
					Animer.translateDecelerate(slide_top, 0, -width / 3, 0, 0,
							1000);
					RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
					slide_bottom.setVisibility(View.VISIBLE);
					Animer.translateDecelerate(slide_bottom, 0, height * 1.1f,
							0, 0, 1800);
					sommaire = (Button) findViewById(R.id.bouton_sommaire);
					if (options.getSommaire()) {
						sommaire.setVisibility(View.VISIBLE);
						Animer.translateDecelerate(sommaire, 0, height * 1.1f,
								0, 0, 1800);
					}
				}
			});
			logo.startAnimation(alpha2);

		}
	};

	OnClickListener sommaire_listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!sommaire_open) {
				Animer.translateDecelerate(slide_right, 0, 0, -width / 3, 0,
						1000);
				// Animer.translateDecelerate(sommaire, 0, 0, -width/4, 0,
				// 1000);
				sommaire.setTranslationX(0);
				sommaire.setBackgroundColor(getResources().getColor(
						R.color.bleu_lagon));
				sommaire.setTextColor(getResources().getColor(R.color.indigo3));
				sommaire_open = true;
			} else {
				Animer.translateDecelerate(slide_right, -width / 3, 0, 0, 0,
						1000);
				// Animer.translateDecelerate(sommaire, -width/4, 0, 0, 0,
				// 1000);
				sommaire.setTranslationX(width / 3);
				Task next = Task.findRelativeTask(myTasks, scopedTask, 1);
				if (next != null) {
					sommaire.setBackgroundColor(next.getCouleur());
				}
				sommaire.setTextColor(getResources().getColor(R.color.blanc));
				sommaire_open = false;
			}

		}
	};

	public void startTimer() {
		timer = new Timer();
		initializeTimerTask();
		timer.schedule(timerTask, 60000, 60000); //
	}

	public void stoptimertask(View v) {
		// stop the timer, if it's not already null
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void initializeTimerTask() {
		final Activity a = this;
		timerTask = new TimerTask() {
			public void run() {

				// use a handler to run a toast that shows the current timestamp
				handler.post(new Runnable() {
					public void run() {
						if (options.getHorloge())
							Horloge.incrementMin(a);
						drawProgress();
						if (scopedTask != findCurrentTask()) { // On cache si on
																// est pas sur
																// l'activité
																// courante
							ImageView progress = (ImageView) findViewById(90);
							if (progress != null) {
								progress.setVisibility(View.INVISIBLE);
							}
						}
					}
				});
			}
		};
	}

	public void drawRange() {
		RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		ImageView ring = (ImageView) findViewById(89);
		if (ring != null) { // not first time
			ring.setVisibility(View.VISIBLE);
		} else { // first time
			ring = new ImageView(this);
			ring.setId(89);
			horloge.addView(ring);
		}
		// always
		double hd = scopedTask.getHeureDebut();
		double duree = scopedTask.getDuree();
		setRingBack(ring, hd, duree, R.color.amber5);
	}

	public void drawProgress() {
		RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		ImageView progress = (ImageView) findViewById(90);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
		} else { // first time
			progress = new ImageView(this);
			progress.setId(90);
			horloge.addView(progress);
		}
		// always
		double hd = scopedTask.getHeureDebut();
		double duree = getCurrentHour() - hd;
		setRingBack(progress, hd, duree, R.color.orange1);
	}

	public double getCurrentHour() {
		Calendar now = Calendar.getInstance();
		int h = now.get(Calendar.HOUR_OF_DAY);
		int m = now.get(Calendar.MINUTE);
		return h + (m / 60);
	}

	public void removeProgression() {
		ImageView ring = (ImageView) findViewById(89);
		ImageView progress = (ImageView) findViewById(90);
		if (progress != null) {
			progress.setVisibility(View.INVISIBLE);
		}
		if (ring != null) {
			ring.setVisibility(View.INVISIBLE);
		}
	}

	public void setRingBack(ImageView img, double heure, double duree,
			int colorId) {
		Bitmap bit_horloge = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.clock_dial_w)).getBitmap();
		int W_horloge = bit_horloge.getWidth();
		int min_d = 0;
		int min_duree = 0;
		if (duree > 1) { // duree > 1h
			min_duree = 360;
		} else { // duree < 1h
			min_d = getMinute(heure) * 6 - 90;
			min_duree = getMinute(duree) * 6;
		}
		ShapeDrawable shape = new ShapeDrawable(new ArcShape(min_d, min_duree));
		shape.setIntrinsicHeight((int) (W_horloge * 1.1));
		shape.setIntrinsicWidth((int) (W_horloge * 1.1));
		shape.getPaint().setColor(getResources().getColor(colorId));
		img.setBackground(shape);
	}
}
