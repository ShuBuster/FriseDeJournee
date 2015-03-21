package atlas.frisedejournee;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MenuActivity extends Activity {

	RelativeLayout titre = null;
	
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
		setContentView(R.layout.activity_menu);
		
		/* Police avant spinner */
		Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/onthemove.ttf");
		TextView jeSuis = (TextView) findViewById(R.id.jeSuis);
		jeSuis.setTypeface(externalFont);
		
		/* Spinner ajout des enfants */
        String[] array_spinner=new String[2];
        array_spinner[0]="Romain";
        array_spinner[1]="Louise";
        Spinner s = (Spinner) findViewById(R.id.enfant_spinner);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,R.layout.spinner_item_text, array_spinner);
        s.setAdapter(adapter);
		
		/* Boutons et titre */
		final Button boutonGo = (Button) findViewById(R.id.go);
		final RelativeLayout titre = (RelativeLayout) findViewById(R.id.titre);
	    
	    boutonGo.setOnClickListener(new View.OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  
	    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
	    	Drawable d = getResources().getDrawable(R.drawable.bouton1e);
	    	boutonGo.setBackground(d);
	    	
	    	/* Recuperation du nom de l'enfant selectione */
	    	Spinner spinner = (Spinner)findViewById(R.id.enfant_spinner);
	    	String nom_enfant = spinner.getSelectedItem().toString();
	    	
	    	/* Passage a l'autre activite */
	        Intent intent = new Intent(MenuActivity.this, FriseActivity.class);
	        intent.putExtra("nom_enfant", nom_enfant);
	        startActivity(intent);
	      }
	    });
	    
		LayoutAnimationController layout_animation = AnimationUtils.loadLayoutAnimation(getApplicationContext(),R.anim.layout_saut);
		titre.setLayoutAnimation(layout_animation);
	    
	}
	
	@Override
	/* L'activite revient sur le devant de la scene */
	public void onResume(){
	    super.onResume();
	    final Button boutonGo = (Button) findViewById(R.id.go);
	    Drawable d = getResources().getDrawable(R.drawable.bouton1);
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
