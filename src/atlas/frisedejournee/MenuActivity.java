package atlas.frisedejournee;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import boutons.ButtonCreator;
import boutons.ExitApplicationListener;

import composants.Animate;
import composants.AnimatedGnar;
import composants.AnimatedText;
import composants.BulleCreator;

public class MenuActivity extends Activity {

	int H; // hauteur de l'ecran en px
	ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
	
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		setContentView(R.layout.activity_menu);

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
		mySpinner.setBackground(getResources().getDrawable(R.drawable.spinner_back));
		mySpinner.setPadding(20, 10, 20, 10);
		
		// Importation des emplois du temps
		chargerEmplois();
		
		/* Spinner ajout des enfants */
		String[] array_spinner = new String[emplois.size()];
		for(int i=0;i<emplois.size();i++){
			array_spinner[i] = emplois.get(i).getNomEnfant();
		}

		final TextView bulle = BulleCreator.createBubble(mySpinner, "Choisis ton prénom dans la liste",
				"right", true, this);
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_text, array_spinner);
		mySpinner.setAdapter(adapter);

		/* Clic sur spinner */
		mySpinner.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
		            BulleCreator.destroyBubble(bulle, a);
		        }
				executeDelayed();
		        return false;
			}
		});
		
		/* Boutons et titre */
		Drawable bouton_go_d = ButtonCreator.roundedDrawable(this,getResources().getColor(R.color.light_blue3),1f);
		final Button boutonGo = (Button) findViewById(R.id.go);
		boutonGo.setBackground(bouton_go_d);
		boutonGo.setTypeface(externalFont);

		boutonGo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
				Drawable bouton_go_pressed = ButtonCreator.pressedRoundedDrawable(a,getResources().getColor(R.color.light_blue3),1f);
				boutonGo.setBackground(bouton_go_pressed);
				
				/* Recuperation du nom de l'enfant selectione */
				Spinner spinner = (Spinner) findViewById(R.id.enfant_spinner);
				final int indice = spinner.getSelectedItemPosition();

				/* animation rideau sur l'ecran violet */
				RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animate.translateDecelerate(slide_top, 0, 0, 0, -H/3, 700);
				
				RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				TranslateAnimation trans = new TranslateAnimation(0, 0, 0, H*1.1f);
				trans.setDuration(1300);
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
						Intent intent = new Intent(getApplicationContext(),
								FriseActivity.class);
						EmploiDuTemps emploi = emplois.get(indice);
						intent.putExtra("emploi", emploi);
						startActivity(intent);
						overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
						
					}
				});
				slide_bottom.startAnimation(trans);

			}
		});

		// Bouton exit //
		Button exit = (Button) findViewById(R.id.exit);
		Drawable exit_d = ButtonCreator.roundedDrawable(this,getResources().getColor(R.color.amber5),0.5f);
		Drawable exit_pressed = ButtonCreator.pressedRoundedDrawable(this,getResources().getColor(R.color.amber5),0.5f);
		exit.setBackground(exit_d);
		exit.setTypeface(externalFont);
		exit.setTextSize(30);
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
				Animate.translateDecelerate(slide_bottom, 0, H*1.1f, 0, 0, 1800);
				Animate.fade_out(logo_bouton, 500, true);
			}
		});
		
		// Animation titre
		LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
		int[] colors = {R.color.light_green3,R.color.light_green4,R.color.light_green5,R.color.green4,R.color.green5,R.color.blue3,R.color.blue5,R.color.red2,R.color.pink2,R.color.pink3,R.color.red3,R.color.pink4,R.color.red4,R.color.red5,};
		AnimatedText.addAnimatedText(this, layout_titre,"Frise de journee",colors, 80);
		
		// Animation Gnar
		RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		AnimatedGnar.addAnimatedGnar(this, gnar);
		
		// Animation mini Gnar
		RelativeLayout mini_gnar = (RelativeLayout) findViewById(R.id.baby_gnar);
		AnimatedGnar.addAnimatedMiniGnar(this, mini_gnar);
	}

	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume() {
		super.onResume();
		final Button boutonGo = (Button) findViewById(R.id.go);
		Drawable bouton_go_d = ButtonCreator.roundedDrawable(this,getResources().getColor(R.color.light_blue3),1f);
		boutonGo.setBackground(bouton_go_d);
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
	
	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * Lit le fichier frise.txt
	 * @param texte
	 */
	public File readFriseFile() {
		try{
			if(isExternalStorageReadable()){
				File sdCard = Environment.getExternalStorageDirectory();
				File directory = new File (sdCard.getAbsolutePath() + "/FilesFrise");
				directory.mkdirs();
				File file = new File(directory, "frise.txt");
				if(file.exists()){
					return file;
				}
				else{
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
	public void chargerEmplois(){
		File frise = readFriseFile();
		this.emplois = TaskReader.read(frise,this);
	}

}
