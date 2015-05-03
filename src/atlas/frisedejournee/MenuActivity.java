package atlas.frisedejournee;

import boutons.ButtonCreator;
import boutons.ExitApplicationListener;
import bulles.BulleCreator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import animation.Animate;

public class MenuActivity extends Activity {

	int H; // hauteur de l'ecran en px
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Taille ecran */
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		H = size.y;
		
		/* Passage en plein ecran */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		setContentView(R.layout.activity_menu);

		/* Changement police de l'appli */
		// FontsOverride.changeDefaultFont(this);

		/* Police du texte avant spinner */
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/intsh.ttf");
		TextView jeSuis = (TextView) findViewById(R.id.jeSuis);
		jeSuis.setTypeface(externalFont);

		/* Police du titre menu */
		TextView titre_menu = (TextView) findViewById(R.id.texte_menu);
		titre_menu.setTypeface(externalFont);

		/* Police du sous-titre */
		TextView sous_titre = (TextView) findViewById(R.id.sous_titre);
		sous_titre.setTypeface(externalFont);

		/* Police du spinner */
		Spinner mySpinner = (Spinner) findViewById(R.id.enfant_spinner);
		
		
		/* Spinner ajout des enfants */
		String[] array_spinner = new String[2];
		array_spinner[0] = "Romain";
		array_spinner[1] = "Louise";
		Spinner s = (Spinner) findViewById(R.id.enfant_spinner);

		BulleCreator.createBubble(s, "Choisi ton prénom dans la liste",
				"right", true, this);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_text, array_spinner);
		s.setAdapter(adapter);

		/* Boutons et titre */
		final Button boutonGo = (Button) findViewById(R.id.go);
		final RelativeLayout titre = (RelativeLayout) findViewById(R.id.titre);
		boutonGo.setTypeface(externalFont);

		boutonGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				Drawable d = getResources().getDrawable(
						R.drawable.bouton_bleu_e);
				boutonGo.setBackground(d);

				/* Recuperation du nom de l'enfant selectione */
				Spinner spinner = (Spinner) findViewById(R.id.enfant_spinner);
				final String nom_enfant = spinner.getSelectedItem().toString();

				/* animation rideau sur l'ecran violet */
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animate.translateDecelerate(slide_top, 0, 0, 0, -H/3, 700);
				
				RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				TranslateAnimation trans = new TranslateAnimation(0, 0, 0, 3*H/4);
				trans.setDuration(1000);
				trans.setFillAfter(true);
				trans.setInterpolator(new DecelerateInterpolator());
				trans.setAnimationListener(new Animation.AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
					}
					
					@Override
					public void onAnimationRepeat(Animation animation) {
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						/* Passage a l'autre activite */
						Intent intent = new Intent(MenuActivity.this,
								FriseActivity.class);
						intent.putExtra("nom_enfant", nom_enfant);
						startActivity(intent);
						overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
						
					}
				});
				slide_bottom.startAnimation(trans);

			}
		});
		//Animation titre
		//LayoutAnimationController layout_animation = AnimationUtils
		//		.loadLayoutAnimation(this, R.anim.layout_saut);
		//titre.setLayoutAnimation(layout_animation);

		// Bouton exit //
		Button exit = (Button) findViewById(R.id.exit);
		ButtonCreator.setButtonStyle(this, exit, R.color.orange1, "Quitter",
				R.color.noir);
		Drawable exit_pressed = ButtonCreator.createButtonPressedDrawable(this,
				R.color.orange1);
		exit.setOnClickListener(new ExitApplicationListener(exit, exit_pressed,
				MenuActivity.this));
		
		/* Apparition du logo bouton */
		final Button logo_bouton = (Button) findViewById(R.id.logo_bouton);
		Animate.fade_in(logo_bouton,1000);
		logo_bouton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/* Arrivée du menu par le haut et le bas*/
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				slide_top.setVisibility(View.VISIBLE);
				Animate.translateDecelerate(slide_top, 0, -H/3, 0, 0, 1000);
				RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				slide_bottom.setVisibility(View.VISIBLE);
				Animate.translateDecelerate(slide_bottom, 0, 3*H/4, 0, 0, 1500);
				Animate.fade_out(logo_bouton, 500, true);
			}
		});
	
	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		final Button boutonGo = (Button) findViewById(R.id.go);
		Drawable d = getResources().getDrawable(R.drawable.bouton_bleu);
		boutonGo.setBackground(d);
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
