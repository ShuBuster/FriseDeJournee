package atlas.frisedejournee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import boutons.Bouton;

import composants.Animer;
import composants.Ecran;
import composants.MyLayoutParams;
import composants.Police;


public class OptionsActivity extends Activity {

	private int H;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Passage en plein ecran */
		Ecran.fullScreen(this);
		setContentView(R.layout.activity_options);

		// recuperation des options precedentes
		Intent i = getIntent();
		final Options options = (Options) i.getSerializableExtra("options");

		/* Taille ecran */

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		H = size.y;
		
		// on cree le menu d'options
		
		MenuOptions m = new MenuOptions(this,options);

		
		m.createMenu();
		m.addTitre(" OPTIONS ");
		final RadioGroup gnar = (RadioGroup) m.addOption("gnar", 1);
		final RadioGroup horloge = (RadioGroup) m.addOption("horloge", 3);
		final RadioGroup son = (RadioGroup) m.addOption("son", 5);
		final RadioGroup bulle = (RadioGroup) m.addOption("bulle", 2);
		final RadioGroup sommaire = (RadioGroup) m.addOption("sommaire", 4);
		
		final RelativeLayout slide_bottom = (RelativeLayout)findViewById(R.id.slide_bottom);
		RelativeLayout slide_top = (RelativeLayout)findViewById(R.id.slide_top);
		slide_bottom.addView(m.getSlide());
		slide_top.addView(m.getTitre());
		
		
		
		gnar.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				options.setGnar(!options.getGnar());
				Log.d("options","gnar vaut "+options.getGnar());
			}
		});

		horloge.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				options.setHorloge(!options.getHorloge());
				Log.d("options","horloge vaut "+options.getHorloge());
			}
		});

		son.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				options.setSound(!options.getSound());
				Log.d("options","son vaut "+options.getSound());
			}
		});

		bulle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				options.setBulle(!options.getBulle());
				Log.d("options","bulle vaut "+options.getBulle());
			}
		});

		sommaire.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				options.setSommaire(!options.getSommaire());
				Log.d("options","sommaire vaut "+options.getSommaire());
			}
		});

		
		
		/* Apparition du logo bouton */
		Button logo_bouton = (Button) findViewById(R.id.logo_bouton);
		ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
		Animer.activityApparitionAnimation(logo_bouton,slide_bottom,slide_top,shadow,H);

		// bouton de validation des options
		Button bouton_menu = Bouton.createRoundedButton(this, R.color.amber7);
		bouton_menu.setText(" Valider ");
		bouton_menu.setTextSize(30);
		bouton_menu.setTextColor(this.getResources().getColor(R.color.blanc_casse));
		MyLayoutParams menu_params = new MyLayoutParams().centerHorizontal();
		menu_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		menu_params.setMargins(0, 20, 0, 0);
		bouton_menu.setLayoutParams(menu_params);
		Police.setFont(this, bouton_menu, "intsh.ttf");
		

		slide_top.addView(bouton_menu);
		
		bouton_menu.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// on recupere les parametres d'options
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
				ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
				Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3, 700);
				Animer.changeActivityAnimation(slide_bottom, MenuActivity.class,
						options, "options", null, null);
			}
		});

	}
	
	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		Ecran.fullScreenResume(this);

	}

}
