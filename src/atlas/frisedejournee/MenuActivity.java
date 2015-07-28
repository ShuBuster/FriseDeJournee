package atlas.frisedejournee;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import boutons.Bouton;
import boutons.ExitApplicationListener;
import boutons.TTSBouton;

import composants.Animer;
import composants.AnimatedGnar;
import composants.AnimatedText;
import composants.Bulle;
import composants.Ecran;
import composants.Police;
import composants.Utile;

public class MenuActivity extends Activity {

	private int H; // hauteur de l'ecran en px
	private ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
	private Options options = new Options(true, true, true, true, true);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Activity a = this;

		/* Taille ecran */

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		H = size.y;

		/* Passage en plein ecran */
		Ecran.fullScreen(a);
		setContentView(R.layout.activity_menu);

		/* Police du texte avant spinner */
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/Action_Man.ttf");
		TextView jeSuis = (TextView) findViewById(R.id.jeSuis);
		jeSuis.setTypeface(externalFont);
		jeSuis.setTextSize(35f);

		/* Police du titre menu */
		TextView titre_menu = (TextView) findViewById(R.id.texte_menu);
		titre_menu.setTypeface(externalFont);


		/* Police du spinner */
		Spinner mySpinner = (Spinner) findViewById(R.id.enfant_spinner);
		mySpinner.setBackground(getResources().getDrawable(
				R.drawable.spinner_back));
		mySpinner.setPadding(20, 10, 20, 10);

		// Importation des emplois du temps
		chargerEmplois();

		// recuperation des options
		Intent opt = getIntent();
		if ((Options) opt.getSerializableExtra("options") != null)
			options = (Options) opt.getSerializableExtra("options");
		;

		/* Spinner ajout des enfants */

		String[] array_spinner = new String[emplois.size()];
		for (int i = 0; i < emplois.size(); i++) {
			array_spinner[i] = emplois.get(i).getNomEnfant();
		}

		// bulle d'aide
		
			final TextView bulle = Bulle.create(mySpinner,
					"Choisis ton prénom dans la liste", "right", true, this);
			if (options.getSound())
				TTSBouton.parle(bulle, "Choisis ton prénom dans la liste", this);
		if(!options.getBulle()) bulle.setVisibility(View.INVISIBLE);
			
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_text, array_spinner);

		mySpinner.setAdapter(adapter);

		/* Clic sur spinner */
		mySpinner.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					TTSBouton.fermer(bulle, getApplicationContext());
					Bulle.destroy(bulle, a);
					
				}
				executeDelayed();
				return false;
			}
		});

		/* Boutons et titre */
		Drawable bouton_go_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
		final Button boutonGo = (Button) findViewById(R.id.go);
		//boutonGo.setTextSize(20f);
		boutonGo.setBackground(bouton_go_d);
		boutonGo.setTypeface(externalFont);

		boutonGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(!emplois.isEmpty()) {
				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				Drawable bouton_go_pressed = Bouton.pressedRoundedDrawable(a,R.color.light_blue3, 1f);
				boutonGo.setBackground(bouton_go_pressed);

				/* Recuperation du nom de l'enfant selectione */
				Spinner spinner = (Spinner) findViewById(R.id.enfant_spinner);
				final int indice = spinner.getSelectedItemPosition();

				/* animation rideau sur l'ecran violet */
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
				ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
				Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3, 700);
				// passage à l'autre activite
				EmploiDuTemps emploi = emplois.get(indice);
				RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				Animer.changeActivityAnimation(slide_bottom,
						FriseActivity.class, emploi, "emploi", options,
						"options");
			}
				else {
					alert("Impossible de lancer la frise",
							"Il doit y avoir au moins un emploi du temps de créée");
				}

			}
		});

		// Bouton des options
		Button bouton_options = (Button) findViewById(R.id.options_bouton);
		Drawable option_d = Bouton.roundedDrawable(this,R.color.amber7, 0.5f);
		bouton_options.setBackground(option_d);
		Police.setFont(a, bouton_options, "Action_Man.ttf");
		bouton_options.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
				ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
				Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3, 700);
				RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				Animer.changeActivityAnimation(slide_bottom,
						OptionsActivity.class, options, "options", null, null);
			}
		});

		// Bouton exit //
		Button exit = (Button) findViewById(R.id.exit);
		Utile.setSize(exit,H/13 , H/13);
		exit.setOnClickListener(new ExitApplicationListener(exit,getResources().getDrawable(R.drawable.close),
				MenuActivity.this));

		/* Apparition du logo puis de l'activite */
		ImageView logo = (ImageView) findViewById(R.id.logo);
		ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
		RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
		RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
		Animer.activityApparitionAnimation(logo, slide_bottom,
				slide_top,shadow, H);
		

		// Animation titre
		LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
		int[] colors = {R.color.red3,R.color.pink3,R.color.purple3,R.color.deep_purple3,R.color.indigo3};

		AnimatedText.add(this, layout_titre, "Frizz", colors, 100);

		// Animation Gnar
		RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		AnimatedGnar.addGnar(this, gnar);

		// Animation mini Gnar
		RelativeLayout mini_gnar = (RelativeLayout) findViewById(R.id.baby_gnar);
		AnimatedGnar.addMiniGnar(this, mini_gnar);

		if (!options.getGnar()) {
			mini_gnar.setVisibility(View.INVISIBLE);
			gnar.setVisibility(View.INVISIBLE);
		}

	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		final Button boutonGo = (Button) findViewById(R.id.go);
		Drawable bouton_go_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
		boutonGo.setBackground(bouton_go_d);
		executeDelayed();
		Ecran.fullScreenResume(this);

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

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Lit le fichier frise.txt
	 * 
	 * @param texte
	 */
	public File readFriseFile() {
		try {
			if (isExternalStorageReadable()) {
				File sdCard = Environment.getExternalStorageDirectory();
				File directory = new File(sdCard.getAbsolutePath()
						+ "/FilesFrise");
				directory.mkdirs();
				File file = new File(directory, "frise.txt");
				if (file.exists()) {
					return file;
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Ajoute les emplois du temps présents sur le fichier texte
	 */
	public void chargerEmplois() {
		File frise = readFriseFile();
		this.emplois = TaskReader.read(frise, this);
		if(emplois.size()==0) {
			File file = new File("/Frizz/emploiTest.txt");
			Log.d("file",file.toString());
			this.emplois = TaskReader.read(file , this);
		}
	}
	
	public void alert(String titre, String message) {
		new AlertDialog.Builder(this)
				.setTitle(titre)
				.setMessage(message)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert).show();
	}

}
