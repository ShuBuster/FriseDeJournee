package atlas.frisedejournee;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		final Button boutonGo = (Button) findViewById(R.id.go);
		RelativeLayout titre = (RelativeLayout) findViewById(R.id.titre);
	    
	    boutonGo.setOnClickListener(new View.OnClickListener() {
	      @Override
	      public void onClick(View v) {
	    	  
	    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
	    	Drawable d = getResources().getDrawable(R.drawable.bouton1e);
	    	boutonGo.setBackground(d);
	    	
	    	/* Passage a l'autre activite */
	        Intent secondeActivite = new Intent(MenuActivity.this, FriseActivity.class);
	        startActivity(secondeActivite);
	      }
	    });
	    
	    Log.v("tag","Tou va bien");
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

	}
	
}
