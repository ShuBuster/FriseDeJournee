package atlas.frisedejournee;


import java.util.ArrayList;
import java.util.Calendar;


import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import boutons.HomeActivityListener;
import boutons.NextActivityListener;
import boutons.TTSButton;

import composants.Animate;
import composants.AnimatedGnar;
import composants.BulleCreator;
import composants.Couleur;
import composants.Fonts;
import composants.GlowingButton;
import composants.Screen;

public class FriseActivity extends Activity {

	private String nomEnfant; // le nom de l'enfant a qui appartient la frise
	private ArrayList<Task> myTasks; // la liste des activites de la frise
	private Task scopedTask; // l'activite sur laquelle se trouve le scope
	private double h0; // l'heure a laquelle commence la frise
	private double h1; // l'heure a laquelle se termine la frise
	private final int[] colorTab; // les id des differentes couleurs des
									// activites
	private int W; // largeur de la frise en px
	private int H; // hauteur de la frise en px
	private int margin; // marge entre les cases des taches
	private boolean modeManuel = false; // mode manuel desactive au debut
	private boolean modeAide = false;
	private Task currentTask = null;
	
	TextToSpeech tts;
	TextView info_text = null;
	Button audio = null;
	Button info = null;
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
	OnClickListener retour_listenner;
	OnClickListener menu_listenner;
	OnClickListener manual_listenner;
	
	
	

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
		colorTab[11] = R.color.deep_orange3;
		colorTab[12] = R.color.orange3;
		colorTab[13] = R.color.amber3;
		colorTab[14] = R.color.yellow3;
		colorTab[0] = R.color.light_green3;
		colorTab[1] = R.color.green3;
		colorTab[2] = R.color.teal3;
		colorTab[3] = R.color.cyan3;
		colorTab[4] = R.color.light_blue3;
		colorTab[5] = R.color.blue3;
		colorTab[6] = R.color.indigo3;
		colorTab[7] = R.color.deep_purple3;
		colorTab[8] = R.color.purple3;
		colorTab[9] = R.color.pink3;
		colorTab[10] = R.color.red3;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Determination densite ecran */
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int density = metrics.densityDpi;
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		switch(density){
		case DisplayMetrics.DENSITY_XHIGH :
			Log.d("TAG", "densite= tres haute");
			options.inTargetDensity = DisplayMetrics.DENSITY_XHIGH;
			margin = 6;
			break;
		
		case DisplayMetrics.DENSITY_HIGH :
			Log.d("TAG", "densite= haute");
			options.inTargetDensity = DisplayMetrics.DENSITY_HIGH;
			margin = 6;
			break;
			
		case DisplayMetrics.DENSITY_MEDIUM :
			Log.d("TAG", "densite= moyenne");
			options.inTargetDensity = DisplayMetrics.DENSITY_MEDIUM;
			margin = 3;
			break;
			
		default :
			Log.d("TAG", "densite= défaut");
			options.inTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
			break;
		}

		/* Taille ecran */
		int[] size = Screen.getSize(this);
		int width = size[0];
		final int height = size[1]; // hauteur de l'ecran en px
		
		/* Passage en plein ecran */
		Screen.fullScreen(this);
		setContentView(R.layout.activity_frise);

		// recuperation de l'emploi du temps
		Intent i = getIntent();
		EmploiDuTemps emploi = (EmploiDuTemps)i.getSerializableExtra("emploi");
		nomEnfant = emploi.getNomEnfant();

		/* Affichage du nom de l'enfant */
		TextView nom_enfant = (TextView) findViewById(R.id.nom_enfant);
		Fonts.setFont(this, nom_enfant, "onthemove.ttf");
		nom_enfant.setText(nomEnfant);

		/* Changement de police du titre */
		TextView txtView1 = (TextView) findViewById(R.id.texte);
		Fonts.setFont(this, txtView1, "onthemove.ttf");

		/* Animation du decor */
		animateStar();

		/* Remplissage des taches selon l'enfant */
		myTasks = emploi.getEmploi();
		setHourBounds();
		/*
		switch (nomEnfant) {
		case "Romain":
			myTasks = Task.createTasksRomain(this);
			break;
		case "Louise":
			myTasks = Task.createTasksLouise(this);
			break;
		}

		/* Ajustement taille frame */
		ImageView frame = (ImageView) findViewById(R.id.frame);
		ImageView frame_p = (ImageView) findViewById(R.id.frame_previous);
		ImageView frame_n = (ImageView) findViewById(R.id.frame_next);
		setSize(frame, 5*height/8, 5*width/6);
		setSize(frame_p, 5*height/9, width/4);
		setSize(frame_n, 5*height/9, width/4);

		/* Ajustement taille frise */
		LinearLayout frise = (LinearLayout) findViewById(R.id.frise);
		H = height/11; // hauteur de la frise
		W = 2*width/3; // largeur de la frise
		setSize(frise,H,W);
		
		/* Ajustement taille scope */
		ImageView scope = (ImageView) findViewById(R.id.scope);
		setSize(scope,(int)(H*1.2),0);
		
		/* Remplissage de la frise */

		int color_indice = 0;
		int task_indice = 0;

		for (Task myTask : myTasks) {

			/* Affichage de ma tache sur la frise */
			Button rectTask = new Button(this);
			rectTask.setEnabled(false); //desactive au debut
			int Xwidth = myTask.getXwidth(W, h0, h1);

			/* Creation du rectangle et placement */
			LinearLayout.LayoutParams layoutParams;
			if (task_indice != myTasks.size() - 1) { // Si ce n'est pas la
														// derniere tache de la
														// journee
				layoutParams = new LinearLayout.LayoutParams(Xwidth,LayoutParams.MATCH_PARENT);
			} else { // si c'est la derniere tache de la journee
				layoutParams = new LinearLayout.LayoutParams(Xwidth - margin
						* (myTasks.size() - 2), LayoutParams.MATCH_PARENT);
			}
			layoutParams.setMargins(margin, margin,0,margin);
			rectTask.setLayoutParams(layoutParams);

			int couleur = getResources().getColor(colorTab[color_indice]);
			myTask.setCouleur(couleur); // on associe a la tache sa couleur
			rectTask.setBackgroundColor(couleur);
			frise.addView(rectTask);
			rectTask.setId(task_indice);
			// rend le bouton clickable
			rectTask.setOnClickListener(new TaskListener(task_indice, this));
			
			color_indice += 1;
			task_indice += 1;
		}
		final int nb_task = task_indice;
		
		/* Met le scope a l'activite en cours */
		currentTask = findCurrentTask();
		if (!(currentTask == null)) {
			scopedTask = currentTask;
			replaceScope(); // place le scope sur la tache
			displayTask(); // affiche les infos de la tache
			displayHour(); // afiche l'heure de la tache
		}
		else{
			currentTask = myTasks.get(0);
			scopedTask = currentTask;
			replaceScope(); // place le scope sur la tahce
			displayTask(); // affiche les infos de la tache
			displayHour(); // afiche l'heure de la tache
		}

		/* creation des 3 boutons menu, aide et retour à l'activite precedente */
		aide = (Button) findViewById(R.id.bouton_aide);
		menu = (Button) findViewById(R.id.bouton_menu);
		retour = (Button) findViewById(R.id.bouton_retour);

		Drawable d = getResources().getDrawable(R.drawable.back_e);
		retour_listenner = new NextActivityListener(retour, d,
				FriseActivity.this, MenuActivity.class);
		retour.setOnClickListener(retour_listenner);

		menu_listenner = new HomeActivityListener(this, menu,
				FriseActivity.this, MenuActivity.class);
		menu.setOnClickListener(menu_listenner);
		
		
		
		// creation de l'aide

			//Creation des bulles d'aide
		LinearLayout heure = (LinearLayout) findViewById(R.id.heure_fond);
		info = (Button) findViewById(R.id.description_bouton);
		final TextView bulle_heure = BulleCreator.createBubble(heure,"L'heure de début de l'activité", "right",false, this);
		final TextView bulle_aide_avant = BulleCreator.createBubble(aide,"Clique sur ce bouton pour obtenir de l'aide", "right",true, this);
		final TextView bulle_aide_apres = BulleCreator.createBubble(aide,"Clique sur ce bouton pour sortir de l'aide", "right",false, this);
		final TextView bulle_description = BulleCreator.createBubble(info,"Ce bouton permet d'afficher"+"\n"+"une description de l'activité", "below",false, this);
		if(Build.VERSION.SDK_INT>=21){
			bulle_aide_avant.setElevation(20); // met les bulle au premier plan
			bulle_aide_apres.setElevation(20);
			bulle_heure.setElevation(20);
			bulle_description.setElevation(20);
			bulle_aide_avant.setOutlineProvider(null); // supprime les ombres de l'elevation
			bulle_aide_apres.setOutlineProvider(null);
			bulle_heure.setOutlineProvider(null);
			bulle_description.setOutlineProvider(null);
		}
		
		aide.setOnClickListener(new View.OnClickListener() {
						
			ImageView glowRetour = null;
			ImageView glowMenu = null;
			ImageView glowManual = null;
			
			
			@Override
			public void onClick(View v) {

				if (modeAide) { // on sort du mode aide

					Drawable d = getResources().getDrawable(R.drawable.help);
					aide.setBackground(d); // desenfonce le bouton

					modeAide = false; // on sort du mode aide
					
					ViewGroup parent = (ViewGroup) info.getParent();
					//RelativeLayout parent = (RelativeLayout) findViewById(R.id.information);
					parent.setClipChildren(true);
					
					if(glowRetour != null) GlowingButton.stopGlow(retour);
					if(glowMenu != null) GlowingButton.stopGlow(menu);
					if(glowManual != null) GlowingButton.stopGlow(manual);
					
					TTSButton.fermer(retour,getApplicationContext());
					TTSButton.fermer(menu,getApplicationContext());
					TTSButton.fermer(manual,getApplicationContext());
										
					//les boutons retrouvent leurs anciens listenners
					
					menu.setOnClickListener(menu_listenner);
					retour.setOnClickListener(retour_listenner);
					manual.setOnClickListener(manual_listenner);
					
					info.setEnabled(true);
					
					// Replacement du bouton aide a droite du bouton menu//
					RelativeLayout.LayoutParams params_aide = (RelativeLayout.LayoutParams) aide.getLayoutParams();
					params_aide.addRule(RelativeLayout.END_OF,R.id.bouton_menu);
					aide.setLayoutParams(params_aide);
					
					// on replace la bulle des boutons brillants au bon endroit//
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
					params.addRule(RelativeLayout.ALIGN_TOP,R.id.bouton_manual);
					params.addRule(RelativeLayout.LEFT_OF,R.id.bouton_manual);
					
					// Disparition des bulles d'aide //
					Animate.fade_out(bulle_aide_apres, 500,false);
					Animate.fade_out(bulle_heure, 500,false);
					Animate.fade_out(bulle_description, 500,false);
					
				} else { // on entre en mode aide

					/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
					Drawable d = getResources().getDrawable(R.drawable.help_e);
					aide.setBackground(d);

					modeAide = true; // on passe en mode aide
					
					// glow sur les autres boutons //
					
					glowRetour = GlowingButton.makeGlow(retour, getApplicationContext(),116);
					glowMenu =  GlowingButton.makeGlow(menu, getApplicationContext(),118);
					glowManual = GlowingButton.makeGlow(manual, getApplicationContext(),117);

					// Replacement du bouton aide a droite du bouton menu//
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) aide.getLayoutParams();
					params.addRule(RelativeLayout.END_OF,118);
					aide.setLayoutParams(params);
					
					// text to speech sur les boutons //

					TTSButton.parle(retour,
							"pour retourner sur ta dernière activité",
							getApplicationContext());

					TTSButton.parle(menu, "pour retourner au menu principal",
							getApplicationContext());
					TTSButton
							.parle(manual,
									"pour passer en mode manuel",
									getApplicationContext());
					
					info.setEnabled(false);
					
					// Apparitions des bulles d'aide //
					Animate.fade_in(bulle_aide_apres, 500);
					Animate.fade_in(bulle_heure, 500);
					Animate.fade_in(bulle_description, 500);
					Animate.fade_out(bulle_aide_avant, 500, true);
					
				}
			}
		});

		// creation de l'information sur l'activite courante 

		info = (Button) findViewById(R.id.description_bouton);
		info.setBackgroundColor(currentTask.getCouleur());
		menuDeroulant = (LinearLayout) findViewById(R.id.info);
		audio = (Button) findViewById(R.id.info_audio);
		info_text = (TextView) findViewById(R.id.info_text);
		Typeface comic = Typeface.createFromAsset(getAssets(),"fonts/comic.otf");
		info_text.setTypeface(comic);
		info.setTypeface(comic);
		
		TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());

		info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View vue) {

				isOpen = toggle(menuDeroulant, isOpen);

				// ...pour afficher ou cacher le menu
				if (isOpen) {
					// Si le Slider est ouvert...
					// ... on change le bouton en mode enfonce

					info_text.setText(currentTask.getDescription());
					menuDeroulant.setBackgroundColor(currentTask.getCouleur());
					info.setBackgroundColor(currentTask.getCouleur());

					
				} else {
					// Sinon on remet le bouton en mode "relache"

				}
			}
		});

		/* creation du mode manuel */
		manual = (Button) findViewById(R.id.bouton_manual);
		left = (Button) findViewById(R.id.bouton_left);
		right = (Button) findViewById(R.id.bouton_right);

		manual_listenner = new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (modeManuel) { // si on est en mode manuel

					manual.setBackground(getResources().getDrawable(
							R.drawable.manual)); // desenfonce le bouton
					modeManuel = false;
					left.setEnabled(false); // desactivation des boutons droite
											// et gauche
					right.setEnabled(false);
					// desactivation des boutons des taches
					for(int id=0;id<nb_task;id++){
						Button task_button = (Button) findViewById(id);
						task_button.setEnabled(false);
					}
					Animate.pop_out(left,500,false); // Disparition des fleches
					Animate.pop_out(right, 500,false);
					Task actualTask = findCurrentTask();
					if(actualTask!=null){
						moveScopeToCurrentTask(); // retour du scope a l'activite courante
						currentTask = actualTask; // retour a l'activite courante
						menuDeroulant.setBackgroundColor(currentTask.getCouleur());
						info.setBackgroundColor(currentTask.getCouleur());
						// le menu deroulant et son bouton retrouvent la couleur de
						// l'activite actuelle
						info_text.setText(currentTask.getDescription());
						TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());
					}

				} else { // si on n'est pas en mode manuel
					try {
						animateHour();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
					Drawable d = getResources()
							.getDrawable(R.drawable.manual_e);
					manual.setBackground(d);

					/* passage en mode manuel */
					modeManuel = true;
					left.setEnabled(true); // activation des boutons
					right.setEnabled(true);					// activation des boutons des taches
					for(int id=0;id<nb_task;id++){
						Button task_button = (Button) findViewById(id);
						task_button.setEnabled(true);
					}
					Animate.pop_in(left,500); // Apparition des fleches
					Animate.pop_in(right, 500);
					TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());
				}

			}
		};
		
		manual.setOnClickListener(manual_listenner);

		left.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Task leftTask = Task.findRelativeTask(myTasks, currentTask, -1);// la tache a gauche
				if(leftTask!=null){
					Task beforeTask = scopedTask;
					moveScope(-1); // deplace le scope d'une activite vers l'arriere
					changeHour(beforeTask.getHeureDebut());
					displayTask(); // affiche la tache scoped au centre
					currentTask = leftTask;
					left.setEnabled(false); // desactive les boutons pendant
											// l'animation du scope
					right.setEnabled(false);
					
					info.setBackgroundColor(currentTask.getCouleur());
					menuDeroulant.setBackgroundColor(currentTask.getCouleur());
					info_text.setText(currentTask.getDescription());
					TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());
					//le menu d'information sur l'activite chanege avec l'activite
				}
			}
		});

		right.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Task rightTask = Task.findRelativeTask(myTasks, currentTask, 1);
				if(rightTask!=null){
					Task beforeTask = scopedTask;
					moveScope(1); // deplace le scope d'une activite vers l'avant
					changeHour(beforeTask.getHeureDebut());
					displayTask(); // affiche la tache scoped au centre
					currentTask = rightTask;
					left.setEnabled(false); // desactive les boutons pendant
											// l'animation du scope
					right.setEnabled(false);
					
					info.setBackgroundColor(currentTask.getCouleur());
					menuDeroulant.setBackgroundColor(currentTask.getCouleur());
					info_text.setText(currentTask.getDescription());
					TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());
					//le menu d'information sur l'activite chanege avec l'activite
				}

			}
		});

		final ImageView logo = (ImageView) findViewById(R.id.logo_image);
		logo.setVisibility(View.VISIBLE);
		AlphaAnimation alpha1 = new AlphaAnimation(0, 1);
		alpha1.setDuration(500);
		alpha1.setFillAfter(true);
		alpha1.setAnimationListener(new AnimationListener() {
			
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
						/*Apparition de l'activité */
						RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
						slide_top.setVisibility(View.VISIBLE);
						Animate.translateDecelerate(slide_top, 0, -height/3, 0, 0, 1000);
						RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
						slide_bottom.setVisibility(View.VISIBLE);
						Animate.translateDecelerate(slide_bottom, 0, height*1.1f, 0, 0, 1800);
					}
				});
				logo.startAnimation(alpha2);
				
			}
		});
		logo.startAnimation(alpha1);
		
		/* Gnar anime */
		RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		AnimatedGnar.addAnimatedGnar(this, gnar);
		
		// Sommaire
		LinearLayout liste_activite = (LinearLayout) findViewById(R.id.liste_activite);
		for(Task task : myTasks){
			TextView txt_activite = new TextView(getApplicationContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMarginStart(50);
			txt_activite.setLayoutParams(params);
			txt_activite.setText(task.getNom()+"  "+formatHour(task.getHeureDebut())+" - "+formatHour(task.getHeureFin()));
			txt_activite.setTextColor(getResources().getColor(R.color.indigo5));
			txt_activite.setTextSize(25f);
			liste_activite.addView(txt_activite);
		}
		
		//Sortie du sommaire
		
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
	public void taskClicked(int taskId){
		int scopedId = Task.indexOfTask(myTasks, scopedTask);
		int deltaId = taskId - scopedId;
		Task beforeTask = scopedTask;
		moveScope(deltaId);
		displayTask(); // affiche la tache scoped au centre
		changeHour(beforeTask.getHeureDebut());
		currentTask = scopedTask;
		
		info.setBackgroundColor(currentTask.getCouleur());
		menuDeroulant.setBackgroundColor(currentTask.getCouleur());
		info_text.setText(currentTask.getDescription());
		TTSButton.parle(audio,currentTask.getDescription(),getApplicationContext());
		//le menu d'information sur l'activite change avec l'activite
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
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)scope.getLayoutParams();
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
		int couleur_clair = Couleur.lightenColor(couleur);
		int[] colors = {couleur_clair,couleur};
		drawable.setColor(couleur);

		/* Affichage du titre de l'activite */
		TextView titreTask = (TextView) findViewById(R.id.titreTask);
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		titreTask.setTypeface(externalFont);
		titreTask.setText(scopedTask.getNom());

		/* Affichage de l'image de l'activite */
		ImageView imageTask = (ImageView) findViewById(R.id.imageTask);
		//imageTask.setBackground(getDrawable(scopedTask.getImage()));
		imageTask.setImageDrawable(getDrawable(scopedTask.getImage()));

		/* Affiche l'heure de debut l'activite */
		// colore les rectangles de fond
		LinearLayout heure10 = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		LinearLayout heure1 = (LinearLayout) findViewById(R.id.heure_unite_fond);
		LinearLayout minute10 = (LinearLayout) findViewById(R.id.minute_dizaine_fond);
		LinearLayout minute1 = (LinearLayout) findViewById(R.id.minute_unite_fond);
		int color = darkenColor(scopedTask.getCouleur());
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
	 * Affiche l'heure de l'activite actuellement scoped
	 */
	public void displayHour(){
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
		int minute = (int) ((hour - heure) * 60);

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
	 * @param hour
	 * @return
	 */
	public String formatHour(double hour){
		String[] split = splitHour(hour);
		return split[0]+split[1]+"h"+split[2]+split[3];
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
		int minute = (int) ((hour - heure) * 60);

		int heure_dizaine = (int) Math.floor(heure / 10);
		result[0] =heure_dizaine;
		int heure_unite = heure - 10 * heure_dizaine;
		result[1] = heure_unite;

		int minute_dizaine = (int) Math.floor(minute / 10);
		result[2] = minute_dizaine;
		int minute_unite = minute - 10 * minute_dizaine;
		result[3] = minute_unite;

		return result;
	}
	
	/**
	 * Change l'heure de l'afficheur avec une animation
	 * @param hour l'heure a laquelle se trouve actuellement l'afficheur
	 */
	public void changeHour(double hour){
		int[] next = splitHour2(scopedTask.getHeureDebut());
		int[] actual = splitHour2(hour);
		int toY =0;
		LinearLayout fond = (LinearLayout) findViewById(R.id.heure_dizaine_fond);
		int h = fond.getHeight();
		
		for(int i=0;i<=3;i++){
			if(actual[i]<next[i]){
				toY = -h;
			}
			else if (actual[i]>next[i]){
				toY = +h;
			}
			else if(actual[i]==next[i]){
				continue; // ne rienf faire pour celui-ci
			}
			TextView view = null;
			TextView view_clone = null;
			switch(i){
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
			Animate.translate(view_clone,0,-toY, 0,0, 1000);
			view.setText(String.valueOf(next[i]));
			Animate.translate(view,0, -toY, 0, 0, 1000);
			
			
			
		}
	}
	
	/**
	 * Anime l'heure avec une animation d'echelle en chaîne
	 * @throws InterruptedException 
	 */
	public void animateHour() throws InterruptedException{
		View hd = findViewById(R.id.heure_dizaine);
		View hu = findViewById(R.id.heure_unite);
		View md = findViewById(R.id.minute_dizaine);
		View mu = findViewById(R.id.minute_unite);
		
		Animate.scale(hd,1f,1.2f,500,0,false);
		Animate.scale(hu,1f,1.2f,500,200,false);
		Animate.scale(md,1f,1.2f,500,400,false);
		Animate.scale(mu,1f,1.2f,500,600,false);
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

	public void setSize(View view,int h, int w){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
		params.height = h;
		params.width = w;
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
	
	public void setHourBounds(){
		Task first_task = myTasks.get(0);
		h0 = first_task.getHeureDebut();
		Task last_task = myTasks.get(myTasks.size()-1);
		h1 = last_task.getHeureDebut()+last_task.getDuree();
		
	}

}
