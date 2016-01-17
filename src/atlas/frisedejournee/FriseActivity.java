package atlas.frisedejournee;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import modele.EmploiDuTemps;
import modele.HeuresMarquees;
import modele.Task;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import boutons.NextActivityListener;
import boutons.TTSBouton;

import composants.AnimatedGnar;
import composants.Animer;
import composants.Bulle;
import composants.Couleur;
import composants.GlowingButton;
import composants.Horloge;
import composants.MyLayoutParams;
import composants.Police;
import composants.Utile;

public class FriseActivity extends Activity {

	private ArrayList<Task> myTasks; // la liste des activites de la frise

	private Task scopedTask; // l'activite sur laquelle se trouve le scope

	private double h0; // l'heure a laquelle commence la frise

	private double h1; // l'heure a laquelle se termine la frise

	private final int[] colorTab; // les id des differentes couleurs des
									// activites

	int color_indice = 0;

	private int width; // largeur de l'ecran en px

	private int height; // hauteur de l'ecran en px

	private int W; // largeur de la frise en px

	private int H; // hauteur de la frise en px

	private int margin; // marge entre les cases des taches

	private boolean modeAide = false;

	private boolean sommaire_open = false;

	private boolean swiping = false;

	private Task currentTask = null;

	private int nbTask = 0;

	private TextView bulle_heure;

	private TextView bulle_aide_avant;

	private TextView bulle_aide_apres;

	private TextView bulle_description;

	private TextToSpeech tts;

	private TextView description_texte = null;

	private Button description_bouton = null;

	private Button aide = null;

	private Button menu = null;

	private Button sommaire = null;

	private ImageView scope = null;

	private ImageView logo;

	private RelativeLayout description_layout = null;

	private RelativeLayout slide_right = null;

	private OnClickListener menu_listenner;

	private Timer timer;

	private TimerTask timerTask;

	private final Handler handler = new Handler();

	private Options options;

	/**
	 * Constructeur par defaut
	 */
	public FriseActivity() {
		myTasks = new ArrayList<Task>();
		scopedTask = null;
		h0 = 8; // debut a 8h par defaut
		h1 = 21; // fin a 21h par defaut
		W = 0;
		H = 0;
		margin = 0;
		colorTab = Task.getColorTab();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Taille ecran */
		final int[] size = Utile.getScreenSize(this);
		width = size[0];
		height = size[1]; // hauteur de l'ecran en px

		/* Determination densite ecran */
		margin = 12 * size[0] / 2560;

		/* Passage en plein ecran */
		Utile.fullScreen(this);
		setContentView(R.layout.activity_frise);

		// recuperation de l'emploi du temps
		final Intent i = getIntent();
		final EmploiDuTemps emploi = (EmploiDuTemps) i
				.getSerializableExtra("emploi");
		emploi.fillHoles();

		// recuperation des options
		final Intent opt = getIntent();
		options = (Options) opt.getSerializableExtra("options");

		/* Remplissage des taches selon l'enfant */
		myTasks = emploi.getEmploi();
		setHourBounds();

		/* Ajustement taille frame */
		final ImageView frame = (ImageView) findViewById(R.id.frame);
		final ImageView frame_p = (ImageView) findViewById(R.id.frame_previous);
		final ImageView frame_n = (ImageView) findViewById(R.id.frame_next);
		setSize(frame, 5 * height / 8, 5 * width / 6);
		setSize(frame_p, 5 * height / 9, width / 4);
		setSize(frame_n, 5 * height / 9, width / 4);

		/* Ajustement taille frise */
		final LinearLayout frise = (LinearLayout) findViewById(R.id.frise);
		H = height / 11; // hauteur de la frise
		W = 3 * width / 4; // largeur de la frise
		setSize(frise, H, W);

		/* Ajustement taille scope */
		final ImageView scope = (ImageView) findViewById(R.id.scope);
		setSize(scope, (int) (H * 1.2), 0);

		/* Ajustement taille description */
		description_layout = (RelativeLayout) findViewById(R.id.description_layout);
		description_bouton = (Button) findViewById(R.id.description_bouton);
		setSize(description_layout,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT, width / 5);
		setSize(description_bouton, 0, width / 5);

		/* Remplissage de la frise */

		int task_indice = 0;

		for (final Task myTask : myTasks) {

			addTaskToFrise(myTask, frise, task_indice);
			task_indice += 1;
		}
		nbTask = task_indice;

		/* Met le scope a l'activite en cours */
		currentTask = findCurrentTask();
		if (!(currentTask == null)) {
			scopedTask = currentTask;
			drawRange();
			drawProgress();
			if (!options.getHorloge()) {
				final RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
				horloge.setVisibility(View.INVISIBLE);
			}
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
		final Drawable menu_presse = getResources().getDrawable(
				R.drawable.home_e);
		menu_listenner = new NextActivityListener(menu, menu_presse,
				FriseActivity.this, MenuActivity.class, options, "options");
		menu.setOnClickListener(menu_listenner);

		// creation de l'aide

		// Creation des bulles d'aide

		final LinearLayout heure = (LinearLayout) findViewById(R.id.heure_fond);
		final LinearLayout boutons_layout = (LinearLayout) findViewById(R.id.boutons);
		if (options.getBulle()) {
			bulle_heure = Bulle.create(heure, "L'heure de debut de l'activite",
					"right", false, this);
			bulle_aide_avant = Bulle.create(boutons_layout,
					"Clique sur ce bouton pour obtenir de l'aide", "right",
					true, this);
			bulle_aide_apres = Bulle.create(boutons_layout,
					"Clique sur ce bouton pour sortir de l'aide", "right",
					false, this);
			bulle_description = Bulle.create(description_bouton,
					"Pour afficher" + "\n" + "une description de l'activite",
					"below", false, this);
			/*
			 * if (Build.VERSION.SDK_INT >= 21) {
			 * bulle_aide_avant.setElevation(20); // met les bulle au premier //
			 * plan bulle_aide_apres.setElevation(20);
			 * bulle_heure.setElevation(20); bulle_description.setElevation(20);
			 * bulle_aide_avant.setOutlineProvider(null); // supprime les //
			 * ombres de // l'elevation
			 * bulle_aide_apres.setOutlineProvider(null);
			 * bulle_heure.setOutlineProvider(null);
			 * bulle_description.setOutlineProvider(null); }
			 */
			if (options.getSound()) {
				TTSBouton.parle(bulle_heure, "L'heure de debut de l'activite",
						this);
				TTSBouton.parle(bulle_aide_avant,
						"Clique sur ce bouton pour obtenir de l'aide", this);
				TTSBouton.parle(bulle_aide_apres,
						"Clique sur ce bouton pour sortir de l'aide", this);
				TTSBouton.parle(bulle_description,
						"Pour afficher une description de l'activite", this);
			}
		}

		aide.setOnClickListener(aide_listener);

		// creation de l'information sur l'activite courante

		description_bouton.setBackgroundColor(getResources().getColor(currentTask.getCouleur()));
		description_texte = (TextView) findViewById(R.id.description_text);
		description_texte
				.setTextColor(getResources().getColor(R.color.blanc));
		description_bouton.setTextColor(getResources()
				.getColor(R.color.blanc));
		Police.setFont(this, description_texte, "Action_Man.ttf");
		Police.setFont(this, description_bouton, "Action_Man.ttf");

		if (options.getSound()) {
			TTSBouton.parle(description_texte, currentTask.getDescription(),
					getApplicationContext());
		}

		// apparition/disparition de la description
		description_bouton.setOnClickListener(description_listener);

		// Ecran logo et apparition frise

		logo = (ImageView) findViewById(R.id.logo_image);
		logo.setVisibility(View.VISIBLE);
		final AlphaAnimation alpha1 = new AlphaAnimation(0, 1);
		alpha1.setDuration(500);
		alpha1.setFillAfter(true);
		alpha1.setAnimationListener(logo_listener);
		logo.startAnimation(alpha1);

		/* Gnar anime */

		final RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		if (options.getGnar()) {
			AnimatedGnar.addGnar(this, gnar);
		}

		// Sommaire
		final LinearLayout liste_activite = (LinearLayout) findViewById(R.id.liste_activite);
		int indice = 0;
		for (final Task task : myTasks) {
			final Button txt_activite = new Button(getApplicationContext());
			final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(50, 10, 0, 10);
			txt_activite.setLayoutParams(params);

			txt_activite.setBackgroundColor(getResources().getColor(
					R.color.blanc_casse));
			txt_activite.setText(formatHour(task.getHeureDebut()) + " - "
					+ formatHour(task.getHeureFin()) + "   " + task.getNom()
					+ " ");
			txt_activite.setTextColor(getResources().getColor(R.color.fushia));
			txt_activite.setTextSize(28f);
			txt_activite.setId(200 + indice);
			if (Build.VERSION.SDK_INT >= 17) {
				// txt_activite.setLayerPaint(new Paint(getResources().getColor(
				// R.color.grey1)));
				try {
					Button.class.getMethod("setLayerPaint", Paint.class)
							.invoke(txt_activite,
									new Paint(getResources().getColor(
											R.color.grey1)));
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final NotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				txt_activite.setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(
						getResources().getColor(R.color.grey1)));
			}
			final int i1 = indice;
			txt_activite.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					FriseActivity frise = FriseActivity.this;
					frise.taskClicked(i1, task);
				}
			});
			Police.setFont(this, txt_activite, "Action_Man.ttf");
			liste_activite.addView(txt_activite);
			indice++;
		}

		liste_activite.addView(new TextView(this));
		liste_activite.addView(new TextView(this));
		liste_activite.addView(new TextView(this));
		slide_right = (RelativeLayout) findViewById(R.id.slide_right);
		sommaire = (Button) findViewById(R.id.bouton_sommaire);
		if (options.getSommaire()) {

			// Sortie du sommaire
			Police.setFont(this, sommaire, "Action_Man.ttf");
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
		final RelativeLayout parent = (RelativeLayout) findViewById(R.id.slide_top);
		for (final HeuresMarquees heure_marquee : emploi.getMarqueTemps()) {
			final double temps_fort = heure_marquee.getHeure_marquee();
			final int x_pos = Task.getXHour(W, h0, h1, temps_fort);
			final TextView txt_temps = new TextView(this);
			txt_temps.setText(" " + formatHour(temps_fort) + " ");
			txt_temps.setTextColor(getResources().getColor(R.color.fushia));
			txt_temps.setTextSize(35f);
			Police.setFont(this, txt_temps, "Action_Man.ttf");
			final MyLayoutParams params = new MyLayoutParams();
			params.alignStart(frise).margins(x_pos - margin, margin * 4, 0, 0);
			parent.addView(txt_temps, params);
		}

		// affichage de l'horloge a l'heure
		final RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		final Calendar now = Calendar.getInstance();
		Horloge.create(horloge, this, now.get(Calendar.HOUR_OF_DAY),
				now.get(Calendar.MINUTE), 0);
		tts = new TextToSpeech(getApplicationContext(),
				new TextToSpeech.OnInitListener() {

					public void onInit(final int status) {
						if (status != TextToSpeech.ERROR) {
							tts.setLanguage(Locale.FRANCE);
						}
					}
				});
		horloge.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				moveScopeToCurrentTask();
				final String t = Horloge.dateActuelle();
				if (options.getSound()) {
					tts.speak(t, TextToSpeech.QUEUE_FLUSH, null);
				}
				Toast.makeText(getApplicationContext(), t, Toast.LENGTH_SHORT)
						.show();

			}

		});

		// drawProgression();
		if (!options.getHorloge()) {
			horloge.setVisibility(View.INVISIBLE);
		}

		// swipe
		final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
		slide_bottom.setOnTouchListener(new OnSwipeTouchListener(this) {

			@Override
			public void onSwipeRight() {
				final int index = Task.indexOfTask(myTasks, scopedTask);
				if (index - 1 >= 0 && !swiping) {
					taskClicked(index - 1, scopedTask);
				}
			}

			@Override
			public void onSwipeLeft() {
				final int index = Task.indexOfTask(myTasks, scopedTask);
				if (index + 1 < nbTask && !swiping) {
					taskClicked(index + 1, scopedTask);
				}
			}
		});

	}

	/**
	 * Changements d'IHM quand une task change de focus.
	 * @param taskId
	 * @param task
	 */
	private void taskClicked(final int taskId, final Task task) {
		swiping = true;
		final Button rectTask = (Button) findViewById(taskId);
		final int couleur = getResources().getColor(task.getCouleur());
		rectTask.setBackgroundColor(Couleur.lighten(couleur));
		final int scopedId = Task.indexOfTask(myTasks, scopedTask);
		final int deltaId = taskId - scopedId;
		final Task beforeTask = scopedTask;
		moveScope(deltaId);
		displayTask(); // affiche la tache scoped au centre
		changeHour(beforeTask.getHeureDebut());
		currentTask = scopedTask;
		if (description_layout.getVisibility() == View.INVISIBLE) {
			description_bouton
					.setBackgroundColor(getResources()
							.getColor(currentTask.getCouleur()));
		} else {
			description_bouton.setBackgroundColor(Couleur.lighten(getResources().getColor(
					currentTask.getCouleur())));
		}

		description_texte.setText(currentTask.getDescription() + "\nDuree : "
				+ formatHour(currentTask.getDuree()));
		if (options.getSound()) {
			TTSBouton.parle(description_texte, currentTask.getDescription(),
					getApplicationContext());
			// le menu d'information sur l'activite change avec l'activite
		}

	}

	/**
	 * Trouve la tache qui se deroule a l'heure actuelle
	 * 
	 * @return la tache actuelle
	 */
	Task findCurrentTask() {

		final Calendar now = Calendar.getInstance();
		final int year = now.get(Calendar.YEAR);
		final int month = now.get(Calendar.MONTH);
		final int day = now.get(Calendar.DAY_OF_MONTH);
		final Calendar before = Calendar.getInstance();
		final Calendar after = Calendar.getInstance();

		for (final Task t : myTasks) { // parcours toutes les tasks

			final double hDebut = t.getHeureDebut();
			final int heureD = (int) Math.floor(hDebut);
			final int minuteD = getMinute(hDebut);
			before.set(year, month, day, heureD, minuteD);

			final double hFin = hDebut + t.getDuree();
			final int heureF = (int) Math.floor(hFin);
			final int minuteF = getMinute(hFin);
			after.set(year, month, day, heureF, minuteF);

			if ((now.compareTo(after) == -1) && (now.compareTo(before) == 1)) {
				return t; // si l'instant actuel est compris entre le debut et
							// la fin de l'activite
			}

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
		final int scopedId = Task.indexOfTask(myTasks, scopedTask);
		final int taskId = pas + scopedId;
		final Task oldScopedTask = scopedTask;
		final Task nextScopedTask = Task.findRelativeTask(myTasks, scopedTask,
				pas);
		if (nextScopedTask != null) {
			scopedTask = nextScopedTask;

			// Affiche progression si tache courante sinon enleve
			final Task actual_task = findCurrentTask();

			if (scopedTask == actual_task && options.getHorloge()) {
				drawRange();
				drawProgress();
			} else if (options.getHorloge()) {
				removeProgression();
			}

			scope = (ImageView) findViewById(R.id.scope);
			/* Creation de l'animation */

			final int x1 = oldScopedTask.getXwidth(W, h0, h1, margin);
			final int x2 = nextScopedTask.getXwidth(W, h0, h1, margin);

			// Translation

			final AnimationSet animationSet = new AnimationSet(true);
			animationSet.setDuration(1000);
			// animationSet.setInterpolator(new LinearInterpolator());

			final int XDelta = nextScopedTask.getXbegin(W, h0, h1, margin,
					myTasks)
					- oldScopedTask.getXbegin(W, h0, h1, margin, myTasks);
			TranslateAnimation translate = null;
			translate = new TranslateAnimation(0, (XDelta) + pas * (x1 / x2)
					* margin + 7, 0, 0);

			// Mise a l'echelle

			final double ratio = (double) x2 / x1;
			final float ratioF = (float) ratio;
			ScaleAnimation scale = null;
			if (XDelta >= 0) {
				scale = new ScaleAnimation(1f, ratioF, 1f, 1f,
						Animation.RELATIVE_TO_SELF, 0f,
						Animation.RELATIVE_TO_SELF, 0f);
			} else {
				scale = new ScaleAnimation(1f, ratioF, 1f, 1f);
			}
			animationSet.addAnimation(scale);
			animationSet.addAnimation(translate);
			animationSet.setAnimationListener(new AnimationListener() {

				public void onAnimationStart(final Animation animation) {
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
					new Handler().post(new Runnable() {

						public void run() {
							final Button rectTask = (Button) findViewById(taskId);
							final int couleur = getResources().getColor(scopedTask.getCouleur());
							rectTask.setBackgroundColor(couleur);
							setEnableTaskButton(true);
							scope.clearAnimation();
							replaceScope(); // replace vraiment le scope a sa
											// nouvelle
											// position
							swiping = false;
						}
					});
				}
			});

			scope.startAnimation(animationSet);
		}
	}

	/**
	 * Fait revenir le scope a la tache courante avec animation
	 */
	public void moveScopeToCurrentTask() {
		final Task currentT = findCurrentTask();
		if (currentT != null) {
			final int index = Task.indexOfTask(myTasks, currentT);
			taskClicked(index, currentT);
		}
	}

	/**
	 * Replace le scope a la position scopedTask sans animation
	 */
	public void replaceScope() {

		final ImageView scope = (ImageView) findViewById(R.id.scope);
		final int indice = Task.indexOfTask(myTasks, scopedTask);
		final int XBegin = scopedTask.getXbegin(W, h0, h1, margin, myTasks);
		final int XWidth = scopedTask.getXwidth(W, h0, h1, margin);
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scope
				.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_LEFT, R.id.frise);
		final MarginLayoutParams paramsScope = (MarginLayoutParams) scope
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

		final LinearLayout heure_fond = (LinearLayout) findViewById(R.id.heure_fond);
		TTSBouton.parle(heure_fond, HeuresMarquees.toString(scopedTask.getHeureDebut()),
				this);

		/* Recuperation du cadre et modification de sa couleur */
		final ImageView cadre = (ImageView) findViewById(R.id.frame);
		final GradientDrawable drawable = (GradientDrawable) cadre
				.getBackground();
		final int couleur = getResources().getColor(scopedTask.getCouleur()); // recuperation de la
														// couleur
		final int couleur_clair = Couleur.lighten(couleur);
		final int[] colors = { couleur_clair, couleur };
		if (Build.VERSION.SDK_INT >= 16) {
			drawable.mutate();
			try {
				GradientDrawable.class.getMethod("setColors", int[].class)
						.invoke(drawable, colors);
			} catch (final IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			drawable.setColor(colors[0]);
		}

		/* Affichage du titre de l'activite */
		final TextView titreTask = (TextView) findViewById(R.id.titreTask);
		Police.setFont(this, titreTask, "Action_Man.ttf");
		titreTask.setText(" " + scopedTask.getNom() + " ");
		if (options.getSound()) {
			TTSBouton.parle(titreTask, titreTask.getText().toString(), this);
		}

		/* Affichage de l'image de l'activite */
		final ImageView imageTask = (ImageView) findViewById(R.id.imageTask);
		// imageTask.setBackground(getDrawable(scopedTask.getImage()));

		if (scopedTask.getImage().startsWith("@")) {
			String path = scopedTask.getImage();
			path = path.substring(1, path.length());
			Bitmap bm = BitmapFactory.decodeFile(path);
			if(bm!=null){
			imageTask.setImageBitmap(bm);
			}
			else{
				imageTask.setImageDrawable(getResources().getDrawable(R.drawable.etoile));
			}
		} else {
			final int imageId = getResources().getIdentifier(
					scopedTask.getImage(), "drawable", getPackageName());
			imageTask.setImageDrawable(getResources().getDrawable(imageId));
		}

		/* Affiche l'heure de debut l'activite */
		// colore les rectangles de fond
		final LinearLayout heure10 = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		final LinearLayout heure1 = (LinearLayout) findViewById(R.id.heure_unite_fond);
		final LinearLayout minute10 = (LinearLayout) findViewById(R.id.minute_dizaine_fond);
		final LinearLayout minute1 = (LinearLayout) findViewById(R.id.minute_unite_fond);
		final int color = getResources().getColor(scopedTask.getCouleur());
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
		final Task previous = Task.findRelativeTask(myTasks, scopedTask, -1);
		final Task next = Task.findRelativeTask(myTasks, scopedTask, 1);

		final ImageView prev = (ImageView) findViewById(R.id.frame_previous);
		final ImageView nxt = (ImageView) findViewById(R.id.frame_next);

		/* Affichage de la tache precedente si elle existe */
		if (previous != null) {
			final int couleur = getResources().getColor(previous.getCouleur()); // recuperation de la
														// couleur
			prev.setBackgroundColor(couleur);
			prev.setVisibility(View.VISIBLE);
			prev.setAlpha(0.95f);
		} else {
			prev.setVisibility(View.INVISIBLE);
		}

		/* Affichage de la tache precedente si elle existe */
		if (next != null) {
			final int couleur = getResources().getColor(next.getCouleur()); // recuperation de la couleur
			nxt.setBackgroundColor(couleur);
			sommaire = (Button) findViewById(R.id.bouton_sommaire);
			sommaire.setBackgroundColor(couleur);
			nxt.setVisibility(View.VISIBLE);
			nxt.setAlpha(0.95f);
		} else {
			nxt.setVisibility(View.INVISIBLE);
			final int couleur = getResources().getColor(scopedTask.getCouleur());
			sommaire = (Button) findViewById(R.id.bouton_sommaire);
			sommaire.setBackgroundColor(couleur);
		}

	}

	/**
	 * Affiche l'heure de l'activite actuellement scoped
	 */
	public void displayHour() {
		final TextView heure101 = (TextView) findViewById(R.id.heure_dizaine);
		final TextView heure11 = (TextView) findViewById(R.id.heure_unite);
		final TextView minute101 = (TextView) findViewById(R.id.minute_dizaine);
		final TextView minute11 = (TextView) findViewById(R.id.minute_unite);
		final String[] splitedHour = splitHour(scopedTask.getHeureDebut());
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
	public String[] splitHour(final double hour) {
		final String[] result = new String[4];
		final int heure = (int) Math.floor(hour);
		final int minute = (int) Math.round((hour - heure) * 60);

		final int heure_dizaine = (int) Math.floor(heure / 10);
		result[0] = String.valueOf(heure_dizaine);
		final int heure_unite = heure - 10 * heure_dizaine;
		result[1] = String.valueOf(heure_unite);

		final int minute_dizaine = (int) Math.floor(minute / 10);
		result[2] = String.valueOf(minute_dizaine);
		final int minute_unite = minute - 10 * minute_dizaine;
		result[3] = String.valueOf(minute_unite);

		return result;
	}

	/**
	 * Transforme une heure de type 12,5 en 12h30
	 * 
	 * @param hour
	 * @return
	 */
	public String formatHour(final double hour) {
		final String[] split = splitHour(hour);
		return split[0] + split[1] + "h" + split[2] + split[3];
	}

	/**
	 * Separe les 4 chiffres qui constituent une heure
	 * 
	 * @param hour
	 *            l'heure
	 * @return le tableau rempli avec les chiffres
	 */
	public int[] splitHour2(final double hour) {
		final int[] result = new int[4];
		final int heure = (int) Math.floor(hour);
		final int minute = (int) Math.round((hour - heure) * 60);

		final int heure_dizaine = (int) Math.floor(heure / 10);
		result[0] = heure_dizaine;
		final int heure_unite = heure - 10 * heure_dizaine;
		result[1] = heure_unite;

		final int minute_dizaine = (int) Math.floor(minute / 10);
		result[2] = minute_dizaine;
		final int minute_unite = minute - 10 * minute_dizaine;
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
	public int getMinute(final double hour) {
		final int heure = (int) Math.floor(hour);
		final int minute = (int) Math.round((hour - heure) * 60);
		return minute;
	}

	/**
	 * Change l'heure de l'afficheur avec une animation
	 * 
	 * @param hour
	 *            l'heure a laquelle se trouve actuellement l'afficheur
	 */
	public void changeHour(final double hour) {
		final int[] next = splitHour2(scopedTask.getHeureDebut());
		final int[] actual = splitHour2(hour);
		int toY = 0;
		final LinearLayout fond = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		final int h = fond.getHeight();

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
			if (view_clone != null) {
				view_clone.setText(String.valueOf(actual[i]));
			}
			Animer.translate(view_clone, 0, -toY, 0, 0, 1000);
			if (view != null) {
				view.setText(String.valueOf(next[i]));
			}
			Animer.translate(view, 0, -toY, 0, 0, 1000);

		}
	}

	/**
	 * Anime l'heure avec une animation d'echelle en chaine
	 * 
	 * @throws InterruptedException
	 */
	public void animateHour() throws InterruptedException {
		final View hd = findViewById(R.id.heure_dizaine);
		final View hu = findViewById(R.id.heure_unite);
		final View md = findViewById(R.id.minute_dizaine);
		final View mu = findViewById(R.id.minute_unite);

		Animer.scale(hd, 1f, 1.2f, 500, 0, false);
		Animer.scale(hu, 1f, 1.2f, 500, 200, false);
		Animer.scale(md, 1f, 1.2f, 500, 400, false);
		Animer.scale(mu, 1f, 1.2f, 500, 600, false);
	}

	public void setSize(final View view, final int h, final int w) {
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		if (h == 0) {
			params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		} else {
			params.height = h;
		}
		if (w == 0) {
			params.width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
		} else {
			params.width = w;
		}
		view.setLayoutParams(params);
	}

	/* L'activite revient sur le devant de la scene */
	@Override
	public void onResume() {
		super.onResume();
		final Button boutonMenu = (Button) findViewById(R.id.bouton_menu);
		final Drawable d2 = getResources().getDrawable(R.drawable.home);
		boutonMenu.setBackgroundDrawable(d2);
		final Button boutonAide = (Button) findViewById(R.id.bouton_aide);
		final Drawable d3 = getResources().getDrawable(R.drawable.help);
		boutonAide.setBackgroundDrawable(d3);
		startTimer();
		Utile.fullScreenResume(this);
	}

	public void setHourBounds() {
		final Task first_task = myTasks.get(0);
		h0 = first_task.getHeureDebut();
		final Task last_task = myTasks.get(myTasks.size() - 1);
		h1 = last_task.getHeureDebut() + last_task.getDuree();

	}

	public void setEnableTaskButton(final boolean b) {
		for (int i = 0; i < nbTask; i++) {
			final Button rectTask = (Button) findViewById(i);
			final Button sommaireTask = (Button) findViewById(200 + i);
			final RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
			horloge.setEnabled(b);
			sommaireTask.setEnabled(b);
			rectTask.setEnabled(b);
		}
	}

	public void addTaskToFrise(final Task myTask, final LinearLayout frise,
			final int task_indice) {
		/* Affichage de ma tache sur la frise */
		final Button rectTask = new Button(this);
		final int Xwidth = myTask.getXwidth(W, h0, h1, margin);
		/* Creation du rectangle et placement */
		LinearLayout.LayoutParams layoutParams;
		layoutParams = new LinearLayout.LayoutParams(Xwidth,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		layoutParams.setMargins(margin, margin, 0, margin);
		rectTask.setLayoutParams(layoutParams);

		final int color = myTask.getCouleur();
		if (color >= 0) {
			final int couleur = getResources().getColor(color);
			rectTask.setBackgroundColor(couleur);
		} else {
			final int couleur = getResources().getColor(colorTab[color_indice]);
			color_indice++;
			myTask.setCouleur(colorTab[color_indice]);
			rectTask.setBackgroundColor(couleur);
		}

		frise.addView(rectTask);
		rectTask.setId(task_indice);
		final FriseActivity frise_act = this;
		// rend le bouton clickable
		rectTask.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				frise_act.taskClicked(task_indice, myTask);
			}
		});
	}

	OnClickListener aide_listener = new View.OnClickListener() {

		ImageView glowMenu = null;

		public void onClick(final View v) {

			if (modeAide) { // on sort du mode aide

				final Drawable d = getResources().getDrawable(R.drawable.help);
				aide.setBackgroundDrawable(d); // desenfonce le bouton

				modeAide = false; // on sort du mode aide

				if (glowMenu != null) {
					GlowingButton.stopGlow(menu);
				}

				TTSBouton.fermer(menu, getApplicationContext());

				// les boutons retrouvent leurs anciens listenners

				menu.setOnClickListener(menu_listenner);

				description_bouton.setEnabled(true);

				// Replacement du bouton aide a droite du bouton menu//
				// final RelativeLayout.LayoutParams params_aide =
				// (RelativeLayout.LayoutParams) aide.getLayoutParams();
				// params_aide.addRule(RelativeLayout.RIGHT_OF,
				// R.id.bouton_menu);
				// aide.setLayoutParams(params_aide);

				// Disparition des bulles d'aide //
				if (options.getBulle()) {
					Animer.fade_out(bulle_aide_apres, 500, false);
					Animer.fade_out(bulle_heure, 500, false);
					Animer.fade_out(bulle_description, 500, false);
				}
			} else { // on entre en mode aide

				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				final Drawable d = getResources()
						.getDrawable(R.drawable.help_e);
				aide.setBackgroundDrawable(d);

				modeAide = true; // on passe en mode aide

				// Replacement du bouton aide a droite du bouton menu//
				// final RelativeLayout.LayoutParams params =
				// (RelativeLayout.LayoutParams) aide.getLayoutParams();
				// params.addRule(RelativeLayout.RIGHT_OF, 118);
				// aide.setLayoutParams(params);

				// text to speech sur les boutons //

				if (options.getSound()) {
					TTSBouton.parle(menu, "pour retourner au menu principal",
							getApplicationContext());
				}

				description_bouton.setEnabled(false);

				// Apparitions des bulles d'aide //
				if (options.getBulle()) {
					Animer.fade_in(bulle_heure, 500);
					Animer.fade_in(bulle_description, 500);
					Animer.fade_in(bulle_aide_apres, 500);
					Animer.fade_out(bulle_aide_avant, 500, true);
				}
			}
		}
	};

	OnClickListener description_listener = new View.OnClickListener() {

		public void onClick(final View vue) {

			if (description_layout.getVisibility() == View.INVISIBLE) {
				Animer.fade_in(description_layout, 500);
				description_texte.setText(currentTask.getDescription()
						+ "\nDuree : " + formatHour(currentTask.getDuree()));
				description_bouton.setBackgroundColor(Couleur
						.lighten(getResources().getColor(
								currentTask.getCouleur())));
			} else {
				Animer.fade_out(description_layout, 500, false);
				description_bouton.setBackgroundColor(getResources().getColor(
						currentTask.getCouleur()));
				description_bouton.setTextColor(getResources().getColor(
						R.color.blanc_casse));
			}
		}
	};

	AnimationListener logo_listener = new AnimationListener() {

		public void onAnimationStart(final Animation animation) {

		}

		public void onAnimationRepeat(final Animation animation) {
		}

		public void onAnimationEnd(final Animation animation) {
			new Handler().post(new Runnable() {

				public void run() {
					final AlphaAnimation alpha2 = new AlphaAnimation(1, 0);
					alpha2.setDuration(500);
					alpha2.setFillAfter(true);
					alpha2.setAnimationListener(new AnimationListener() {

						public void onAnimationStart(final Animation animation) {
						}

						public void onAnimationRepeat(final Animation animation) {
						}

						public void onAnimationEnd(final Animation animation) {
							/* Apparition de l'activite */
							new Handler().post(new Runnable() {

								public void run() {
									final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
									slide_top.setVisibility(View.VISIBLE);
									Animer.translateDecelerate(slide_top, 0,
											-width / 3, 0, 0, 1000);
									final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
									slide_bottom.setVisibility(View.VISIBLE);
									Animer.translateDecelerate(slide_bottom, 0,
											height * 1.1f, 0, 0, 1800);
									final ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
									shadow.setVisibility(View.VISIBLE);
									Animer.translateDecelerate(shadow, 0,
											-width / 3, 0, 0, 1000);
									sommaire = (Button) findViewById(R.id.bouton_sommaire);
									if (options.getSommaire()) {
										sommaire.setVisibility(View.VISIBLE);
										Animer.translateDecelerate(sommaire, 0,
												height * 1.1f, 0, 0, 1800);
									}
								}
							});
						}
					});
					logo.startAnimation(alpha2);
				}
			});

		}

	};

	OnClickListener sommaire_listener = new View.OnClickListener() {

		public void onClick(final View v) {
			if (!sommaire_open) {

				final TranslateAnimation trans = new TranslateAnimation(0,
						-width / 3, 0, 0);
				trans.setDuration(1000);
				trans.setFillAfter(true);
				trans.setInterpolator(new DecelerateInterpolator());
				trans.setAnimationListener(new AnimationListener() {

					public void onAnimationStart(final Animation animation) {

					}

					public void onAnimationRepeat(final Animation animation) {
					}

					public void onAnimationEnd(final Animation animation) {
						new Handler().post(new Runnable() {

							public void run() {
								slide_right.setTranslationX(0);
								slide_right.clearAnimation();
							}
						});
					}
				});
				slide_right.startAnimation(trans);

				sommaire.setTranslationX(0);
				sommaire.setBackgroundColor(getResources().getColor(
						R.color.bleu_lagon));
				sommaire.setTextColor(getResources().getColor(R.color.indigo3));
				sommaire_open = true;
			} else {

				final TranslateAnimation trans = new TranslateAnimation(0,
						width / 3, 0, 0);
				trans.setDuration(1000);
				trans.setFillAfter(true);
				trans.setInterpolator(new DecelerateInterpolator());
				trans.setAnimationListener(new AnimationListener() {

					public void onAnimationStart(final Animation animation) {
					}

					public void onAnimationRepeat(final Animation animation) {
					}

					public void onAnimationEnd(final Animation animation) {
						new Handler().post(new Runnable() {

							public void run() {
								slide_right.setTranslationX(width / 3);
							}
						});
					}
				});
				slide_right.startAnimation(trans);

				sommaire.setTranslationX(width / 3);
				final Task next = Task.findRelativeTask(myTasks, scopedTask, 1);
				if (next != null) {
					sommaire.setBackgroundColor(getResources().getColor(next.getCouleur()));
				}
				sommaire.setTextColor(getResources().getColor(R.color.blanc));
				sommaire_open = false;
			}

		}
	};

	public void startTimer() {
		timer = new Timer();
		initializeTimerTask();
		final Calendar now = Calendar.getInstance();
		final int s = 1000 * now.get(Calendar.SECOND);
		timer.schedule(timerTask, 60000 - s, 60000); //
	}

	public void stoptimertask(final View v) {
		// stop the timer, if it's not already null
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	public void initializeTimerTask() {
		final Activity a = this;
		timerTask = new TimerTask() {

			@Override
			public void run() {

				// use a handler to run a toast that shows the current timestamp
				handler.post(new Runnable() {

					public void run() {
						if (options.getHorloge()) {
							Horloge.incrementMin(a);
							if (scopedTask == findCurrentTask()) {
								drawProgress();
							}
						}
					}
				});
			}
		};
	}

	/**
	 * affiche la plage horaire de l'activite en cours
	 */
	public void drawRange() {
		final RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		ImageView ring = (ImageView) findViewById(89);
		if (ring != null) { // not first time
			ring.setVisibility(View.VISIBLE);
		} else { // first time
			final MyLayoutParams params = new MyLayoutParams()
					.centerHorizontal().centerVertical();
			ring = new ImageView(this);
			ring.setId(89);
			horloge.addView(ring, params);
		}
		// always
		final double hd = scopedTask.getHeureDebut();
		final double duree = scopedTask.getDuree();
		setRingBack(ring, hd, duree, R.color.amber2, true);
	}

	/**
	 * affiche la progression horaire
	 */
	public void drawProgress() {
		final RelativeLayout horloge = (RelativeLayout) findViewById(R.id.horloge);
		ImageView progress = (ImageView) findViewById(90);
		if (progress != null) {
			progress.setVisibility(View.VISIBLE);
		} else { // first time
			final MyLayoutParams params = new MyLayoutParams()
					.centerHorizontal().centerVertical();
			progress = new ImageView(this);
			progress.setId(90);
			horloge.addView(progress, params);
		}
		// always
		final double hd = scopedTask.getHeureDebut();
		final double duree = getCurrentHour() - hd;
		setRingBack(progress, hd, duree, R.color.light_blue1, false);
	}

	public double getCurrentHour() {
		final Calendar now = Calendar.getInstance();
		final int h = now.get(Calendar.HOUR_OF_DAY);
		final int m = now.get(Calendar.MINUTE);
		final int s = now.get(Calendar.SECOND);
		return h + (m / 60f) + (s / 3600f);
	}

	/**
	 * retire les deux anneaux sur l'horloge
	 */
	public void removeProgression() {
		final ImageView ring = (ImageView) findViewById(89);
		final ImageView progress = (ImageView) findViewById(90);
		if (progress != null) {
			progress.setVisibility(View.INVISIBLE);
		}
		if (ring != null) {
			ring.setVisibility(View.INVISIBLE);
		}
	}

	public void setRingBack(final ImageView img, final double heure,
			final double duree, final int colorId, final boolean isBig) {
		final Bitmap bit_horloge = ((BitmapDrawable) getResources()
				.getDrawable(R.drawable.clock_dial_w)).getBitmap();
		final int W_horloge = bit_horloge.getWidth();
		int min_d = 0;
		int min_duree = 0;
		if (duree > 1) { // duree > 1h
			min_duree = 360;
		} else { // duree < 1h
			min_d = getMinute(heure) * 6 - 90;
			min_duree = getMinute(duree) * 6;
		}
		final ShapeDrawable shape = new ShapeDrawable(new ArcShape(min_d,
				min_duree));
		if (isBig) {
			shape.setIntrinsicHeight((int) (W_horloge * 1.15));
			shape.setIntrinsicWidth((int) (W_horloge * 1.15));
		} else {
			shape.setIntrinsicHeight((int) (W_horloge * 1.1));
			shape.setIntrinsicWidth((int) (W_horloge * 1.1));
		}

		shape.getPaint().setColor(getResources().getColor(colorId));
		img.setBackgroundDrawable(shape);
	}


}
