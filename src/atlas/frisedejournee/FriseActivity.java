package atlas.frisedejournee;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriseActivity extends Activity {

	private String nomEnfant; // le nom de l'enfant a qui appartient la frise
	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private Task scopedTask; // l'activite sur laquelle se trouve le scope
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des
									// activites
	private final int W; // largeur de la frise en px
	private final int H; // hauteur de la frise en px
	private final int margin; // marge entre les cases des taches
	private boolean modeManuel = false; // mode manuel desactive au debut

	TextToSpeech tts;

	Button audio1 = null;
	Button audio2 = null;
	Button audio3 = null;
	Button aide_retour = null;
	Button aide_menu = null;
	Button aide_manuel = null;
	Button aide = null;
	Button menu = null;
	Button retour = null;
	Button manual = null;
	Button right = null;
	Button left = null;
	ImageView scope = null;
	LinearLayout menuDeroulant = null;
	LinearLayout descriptionDeroulant = null;
	boolean isOpen = false;

	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		nomEnfant = "";
		myTasks = new ArrayList<Task>();
		scopedTask = null;
		h0 = 8; // debut a 8h
		h1 = 21; // debut a 21h
		W = 1638;
		H = 92;
		margin = 6;
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

		/* Passage en plein ecran */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		setContentView(R.layout.activity_frise);

		/* Recuperation du nom de l'enfant */
		Bundle bundle = getIntent().getExtras();
		String nom = bundle.getString("nom_enfant");
		nomEnfant = nom;

		/* Affichage du nom de l'enfant */
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		TextView nom_enfant = (TextView) findViewById(R.id.nom_enfant);
		nom_enfant.setText(nomEnfant);
		nom_enfant.setTypeface(externalFont);

		/* Changement de police du titre */
		TextView txtView1 = (TextView) findViewById(R.id.texte);
		txtView1.setTypeface(externalFont);

		/* Animation du decor */
		animateStar();

		/* Remplissage des taches selon l'enfant */
		switch (nomEnfant) {
		case "Romain":
			myTasks = Task.createTasksRomain(this);
			break;
		case "Louise":
			myTasks = Task.createTasksLouise(this);
			break;
		}

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
			if (task_indice != myTasks.size() - 1) { // Si ce n'est pas la
														// derniere tache de la
														// journee
				layoutParams = new LinearLayout.LayoutParams(Xwidth, H);
			} else { // si c'est la derniere tache de la journee
				layoutParams = new LinearLayout.LayoutParams(Xwidth - margin
						* (myTasks.size() - 2), H);
			}
			layoutParams.setMargins(margin, margin, 0, 0);
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

		aide = (Button) findViewById(R.id.bouton_aide);
		audio1 = (Button) findViewById(R.id.audio1);
		audio2 = (Button) findViewById(R.id.audio2);
		audio3 = (Button) findViewById(R.id.audio3);
		aide_retour = (Button) findViewById(R.id.aide_retour);
		aide_manuel = (Button) findViewById(R.id.aide_manuel);
		aide_menu = (Button) findViewById(R.id.aide_menu);

		// on recupere le menu a derouler
		// Drawable open = getResources().getDrawable(R.drawable.help_e);
		// Drawable close = getResources().getDrawable(R.drawable.help);
		menuDeroulant = (LinearLayout) findViewById(R.id.menuDeroulant);

		aide = (Button) findViewById(R.id.bouton_aide);

		// Slider menuAide = new Slider(menuDeroulant,aide,open,close);
		// menuAide.start();
		// a l'origine, le menu est cache
		menuDeroulant.setVisibility(View.INVISIBLE);
		// On rajoute un Listener sur le clic du bouton...
		aide.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View vue) {

				isOpen = toggle(menuDeroulant, isOpen);

				// ...pour afficher ou cacher le menu
				if (isOpen) {
					// Si le Slider est ouvert...
					// ... on change le bouton d'aide en mode appuye
					Drawable open = getResources().getDrawable(
							R.drawable.help_e);
					aide.setBackground(open);
				} else {
					// Sinon on remet le bouton en mode "lache"
					Drawable close = getResources()
							.getDrawable(R.drawable.help);
					aide.setBackground(close);
				}
			}
		});

		/* le texte to speech */

		start("pour retourner sur ta frise", audio1);
		start("pour retourner au menu principal", audio3);
		start("pour choisir toi-même ton activité", audio2);

		// TTSButton retour = new
		// TTSButton(audio1,"pour retourner sur ta frise");
		// TTSButton menu = new
		// TTSButton(audio2,"pour retourner au menu principal");
		// TTSButton manuel = new
		// TTSButton(audio3,"pour choisir toi-même ton activité");

		/* creation du mode manuel */
		manual = (Button) findViewById(R.id.bouton_manual);
		left = (Button) findViewById(R.id.bouton_left);
		right = (Button) findViewById(R.id.bouton_right);

		manual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (modeManuel) { // si on est en mode manuel

					manual.setBackground(getResources().getDrawable(
							R.drawable.manual)); // desenfonce le bouton
					modeManuel = false;
					left.setEnabled(false); // desactivation des boutons droite
											// et gauche
					right.setEnabled(false);
					left.setVisibility(View.INVISIBLE); // disparition des
														// boutons droite et
														// gauche
					right.setVisibility(View.INVISIBLE);
					moveScopeToCurrentTask(); // retour du scope a l'activite
												// courante
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
				left.setEnabled(false); // desactive les boutons pendant
										// l'animation du scope
				right.setEnabled(false);
			}
		});

		right.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				moveScope(1); // deplace le scope d'une activite vers l'avant
				displayTask(); // affiche la tache scoped au centre
				left.setEnabled(false); // desactive les boutons pendant
										// l'animation du scope
				right.setEnabled(false);

			}
		});

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
		if (nextScopedTask != null) {
			this.scopedTask = nextScopedTask;
			scope = (ImageView) findViewById(R.id.scope);

			/* Creation de l'animation */

			final int x1 = oldScopedTask.getXwidth(W, h0, h1);
			final int x2 = nextScopedTask.getXwidth(W, h0, h1);

			// Translation

			AnimationSet animationSet = new AnimationSet(true);
			animationSet.setDuration(1000);
			//animationSet.setInterpolator(new LinearInterpolator());
			
			int XDelta = nextScopedTask.getXbegin(W, h0, h1)
					- oldScopedTask.getXbegin(W, h0, h1);
			TranslateAnimation translate = null;
			if(XDelta>=0){
				translate = new TranslateAnimation(0, (XDelta) + pas*(x1 / x2)*margin + 7, 0, 0);
			}
			else{
				translate = new TranslateAnimation(0, (XDelta)*(x1/x2) + pas*(x1 / x2)*margin + 7, 0, 0);
			}
			animationSet.addAnimation(translate);

			// Mise a l'echelle

			double ratio = (double) x2 / x1;
			float ratioF = (float) ratio;
			ScaleAnimation scale = null;
			if(XDelta>=0){
				 scale = new ScaleAnimation(1f, ratioF, 1f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
			}
			else{
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
					scope.clearAnimation();
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
		int pas = Task.indexOfTask(myTasks, currentTask)
				- Task.indexOfTask(myTasks, scopedTask);
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
		paramsScope.leftMargin = 500 + XBegin + indice * margin;
		scope.setLayoutParams(paramsScope);

	}

	/**
	 * Affiche les informations de la scopedTask au milieu de l'ecran et de ses
	 * taches voisines
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
			prev.setAlpha(0.8f);
		} else {
			prev.setVisibility(View.INVISIBLE);
		}

		/* Affichage de la tache precedente si elle existe */
		if (next != null) {
			int couleur = next.getCouleur(); // recuperation de la couleur
			nxt.setBackgroundColor(couleur);
			nxt.setVisibility(View.VISIBLE);
			nxt.setAlpha(0.6f);
		} else {
			nxt.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * Separe les 4 chiffres qui constituent une heure
	 * 
	 * @param hour
	 *            l'heure
	 * @return le tableau rempli avec les chiffres
	 */
	public String[] splitHour(double hour) {
		String[] result = new String[4];
		int heure = (int) Math.floor(hour);
		int minute = (int) ((hour - heure) * 0.6);

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
	 * Assombrit une couleur
	 * 
	 * @param color
	 *            la couleur a assombrir
	 * @return la couleur assombrie
	 */
	public int darkenColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.85f;
		color = Color.HSVToColor(hsv);
		return color;
	}

	/**
	 * lance l'animation continue de l'etoile et du cercle
	 */
	public void animateStar() {
		Animation animation1 = AnimationUtils.loadAnimation(this,
				R.anim.rotate_star);
		ImageView star = (ImageView) findViewById(R.id.etoile);
		star.startAnimation(animation1);

		Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.scale);
		ImageView cercle = (ImageView) findViewById(R.id.cercle);
		cercle.startAnimation(animation2);
	}

	public void start(final String texteALire, Button ttsButton) {
		Log.d("tag","OH OH");
		tts = new TextToSpeech(getApplicationContext(),
				new TextToSpeech.OnInitListener() {
					public void onInit(int status) {
						if (status != TextToSpeech.ERROR)
							tts.setLanguage(Locale.FRANCE);
					}
				});
		ttsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("tag","OH");
				speakText(texteALire);
			}
		});
	}

	@Override
	public void onPause() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	public void speakText(String texteALire) {

		/*
		 * Toast.makeText(getApplicationContext(), texteALire,
		 * Toast.LENGTH_SHORT) .show();
		 */
		// affiche le texte qui est en train d'etre lu sous forme de toast
		tts.speak(texteALire, TextToSpeech.QUEUE_FLUSH, null);
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

		/* Pour le plein ecran */
		int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN;

		executeDelayed();
	}

	private void executeDelayed() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// execute after 500ms
				hideNavBar();
			}
		}, 500);
	}

	private void hideNavBar() {
		if (Build.VERSION.SDK_INT >= 19) {
			View v = getWindow().getDecorView();
			v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

}
