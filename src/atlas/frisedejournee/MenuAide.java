package atlas.frisedejournee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import atlas.frisedejournee.R.color;

public class MenuAide extends Activity{
	
	Button retour = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aide);
		
		
		TextView textView = new TextView(this);

		textView.setText(R.id.texte_aide);

		textView.setTextSize(8);

		textView.setBackgroundColor(getResources().getColor(color.bleu1));
		
		retour = (Button) findViewById(R.id.bouton_retour);
		retour.setOnClickListener(new View.OnClickListener() {
		      @Override
		      public void onClick(View v) {
		    	  
		    	/* Changement de l'aspect du bouton lorsqu'on l'enfonce */  
		    	Drawable d = getResources().getDrawable(R.drawable.back_e);
		    	retour.setBackground(d);
		    	
		    	/* Passage a l'autre activite */
		        Intent secondeActivite = new Intent(MenuAide.this,FriseActivity.class);
		        startActivity(secondeActivite);
		      }
  });
	}
		
		@Override
		/* L'activite revient sur le devant de la scene */
		public void onResume(){
		    super.onResume();
		    final Button boutonRetour = (Button) findViewById(R.id.bouton_retour);
		    Drawable d = getResources().getDrawable(R.drawable.back);
	    	boutonRetour.setBackground(d);

		}
}
	

