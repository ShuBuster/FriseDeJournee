package atlas.frisedejournee;

import java.io.Serializable;

import boutons.Bouton;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;

import composants.Animer;
import composants.Ecran;
import composants.MyLayoutParams;
import composants.Police;

import custom.FabriqueMenu;
import custom.Menu;
import custom.TypeMenu;

public class OptionsActivity extends Activity {

	private Menu m;
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
		
		final RelativeLayout parent = (RelativeLayout) findViewById(R.id.parent);
		// on cree le menu d'options
		
		try {
			m = FabriqueMenu.create(TypeMenu.Options, this);
		} catch (IllegalArgumentException | InstantiationException
				| IllegalAccessException e) {

			e.printStackTrace();
		}

		
		m.createMenu(parent);
		m.addTitre("OPTIONS GNAR !!  ");
		final RadioGroup gnar = (RadioGroup) m.addButton("gnar", 1);
		final RadioGroup horloge = (RadioGroup) m.addButton("horloge", 3);
		final RadioGroup son = (RadioGroup) m.addButton("son", 5);
		final RadioGroup bulle = (RadioGroup) m.addButton("bulle", 2);
		final RadioGroup sommaire = (RadioGroup) m.addButton("sommaire", 4);
		
		
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
		Animer.activityApparitionAnimation(logo_bouton,parent,null,H);

		
		Button bouton_menu = (Button) findViewById(R.id.bouton_menu);
		Drawable bouton_d = Bouton.roundedDrawable(this, getResources()
				.getColor(R.color.amber7), 0.5f);
		bouton_menu.setBackground(bouton_d);
			Police.setFont(this, bouton_menu, "intsh.ttf");
		
		bouton_menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// on recupere les parametres d'options
				Animer.changeActivityAnimation(parent, MenuActivity.class,
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
