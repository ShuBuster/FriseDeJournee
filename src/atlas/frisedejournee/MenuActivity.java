package atlas.frisedejournee;

import java.util.ArrayList;

import modele.EmploiDuTemps;
import services.Bluetooth;
import services.Bluetooth_Constants;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import atlas.controller.Presenter;
import boutons.Bouton;
import boutons.ExitApplicationListener;
import boutons.TTSBouton;

import composants.AnimatedGnar;
import composants.AnimatedText;
import composants.Animer;
import composants.Bulle;
import composants.Police;
import composants.Utile;

public class MenuActivity extends Activity {

	private int H; // hauteur de l'ecran en px

	private ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();

	private Options options = new Options(true, true, true, true, true);

	private final Activity a = this;

	private final Presenter _presenter = new Presenter(this);

	// acces a la base de donnes

	private Spinner mySpinner;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
			/* Taille ecran */

		final Display display = getWindowManager().getDefaultDisplay();
		final Point size = new Point();
		display.getSize(size);
		H = size.y;

		/* Passage en plein ecran */
		Utile.fullScreen(a);
		setContentView(R.layout.activity_menu);

		/* Police du texte avant spinner */
		final Typeface externalFont = Typeface.createFromAsset(getAssets(),
				"fonts/Action_Man.ttf");
		final TextView jeSuis = (TextView) findViewById(R.id.jeSuis);
		jeSuis.setTypeface(externalFont);
		jeSuis.setTextSize(35f);

		/* Police du titre menu */
		final TextView titre_menu = (TextView) findViewById(R.id.texte_menu);
		titre_menu.setTypeface(externalFont);

		/* Police du spinner */
		mySpinner = (Spinner) findViewById(R.id.enfant_spinner);
		mySpinner.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.spinner_back));
		mySpinner.setPadding(20, 10, 20, 10);

		// recuperation des options
		final Intent opt = getIntent();
		if ((Options) opt.getSerializableExtra("options") != null) {
			options = (Options) opt.getSerializableExtra("options");
		}

		// bulle d'aide

		final TextView bulle = Bulle.create(mySpinner,
				"Choisis ton prenom dans la liste", "right", true, this);
		if (options.getSound()) {
			TTSBouton.parle(bulle, "Choisis ton prenom dans la liste", this);
		}
		if (!options.getBulle()) {
			bulle.setVisibility(View.INVISIBLE);
		}

		/* Clic sur spinner */
		mySpinner.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(final View v, final MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					TTSBouton.fermer(bulle, getApplicationContext());
					Bulle.destroy(bulle, a);

				}
				executeDelayed();
				return false;
			}
		});

		/* Boutons et titre */
		final Drawable bouton_go_d = Bouton.roundedDrawable(this,
				R.color.light_blue3, 1f);
		final Button boutonGo = (Button) findViewById(R.id.go);
		// boutonGo.setTextSize(20f);
		boutonGo.setBackgroundDrawable(bouton_go_d);
		boutonGo.setTypeface(externalFont);

		boutonGo.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {

				if (!emplois.isEmpty()) {
					/* Changement de l'aspect du bouton lorsqu'on l'enfonce */
					final Drawable bouton_go_pressed = Bouton
							.pressedRoundedDrawable(a, R.color.light_blue3, 1f);
					boutonGo.setBackgroundDrawable(bouton_go_pressed);

					/* Recuperation du nom de l'enfant selectione */
					final Spinner spinner = (Spinner) findViewById(R.id.enfant_spinner);
					final int indice = spinner.getSelectedItemPosition();

					/* animation rideau sur l'ecran violet */
					final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
					Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
					final ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
					Animer.translateDecelerate(slide_top_shadow, 0, 0, 0,
							-H / 3, 700);
					// passage a l'autre activite
					final EmploiDuTemps emploi = emplois.get(indice);
					final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
					Animer.changeActivityAnimation(slide_bottom,
							FriseActivity.class, emploi, "emploi", options,
							"options");
				} else {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.enfant_vide),
							Toast.LENGTH_LONG).show();
				}

			}
		});

		// Bouton des options
		final Button bouton_options = (Button) findViewById(R.id.options_bouton);
		final Drawable option_d = Bouton.roundedDrawable(this, R.color.amber7,
				0.5f);
		bouton_options.setBackgroundDrawable(option_d);
		Police.setFont(a, bouton_options, "Action_Man.ttf");
		bouton_options.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
				final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
				Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
				final ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
				Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3,
						700);
				final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
				Animer.changeActivityAnimation(slide_bottom,
						OptionsActivity.class, options, "options", null, null);
			}
		});

		// Bouton exit //
		final Button exit = (Button) findViewById(R.id.exit);
		Utile.setSize(exit, H / 13, H / 13);
		exit.setOnClickListener(new ExitApplicationListener(exit,
				getResources().getDrawable(R.drawable.close), MenuActivity.this));

		/* Apparition du logo puis de l'activite */
		final ImageView logo = (ImageView) findViewById(R.id.logo);
		final ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
		final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
		final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
		Animer.activityApparitionAnimation(logo, slide_bottom, slide_top,
				shadow, H);

		// Animation titre
		final LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
		final int[] colors = { R.color.red3, R.color.pink3, R.color.purple3,
				R.color.deep_purple3, R.color.indigo3 };

		AnimatedText.add(this, layout_titre, "Frizz", colors, 100,
				TypedValue.COMPLEX_UNIT_SP);

		// Animation Gnar
		final RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
		AnimatedGnar.addGnar(this, gnar);

		// Animation mini Gnar
		final RelativeLayout mini_gnar = (RelativeLayout) findViewById(R.id.baby_gnar);
		AnimatedGnar.addMiniGnar(this, mini_gnar);

		if (!options.getGnar()) {
			mini_gnar.setVisibility(View.INVISIBLE);
			gnar.setVisibility(View.INVISIBLE);
		}

	}

	/* L'activite revient sur le devant de la scene */
	@Override
	public void onResume() {
		super.onResume();
		// etat initial de l'appli
		final Button boutonGo = (Button) findViewById(R.id.go);
		final Drawable bouton_go_d = Bouton.roundedDrawable(this,
				R.color.light_blue3, 1f);
		boutonGo.setBackgroundDrawable(bouton_go_d);
		executeDelayed();
		// Utile.fullScreenResume(this);

		// lancement du bluetooth si autorise
		_presenter.startConnection(mHandler);

		// chargement des edt
		emplois = _presenter.chargeEmplois();
		
		setSpinner();

	}

	/**
	 * Remet a jour le spinner avec les noms des emplois du temps
	 */
	private void setSpinner() {
		/* Spinner ajout des enfants */

		final String[] array_spinner = new String[emplois.size()];
		for (int i = 0; i < emplois.size(); i++) {
			array_spinner[i] = emplois.get(i).getNomEnfant();
		}

		final ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, R.layout.spinner_item_text, array_spinner);

		mySpinner.setAdapter(adapter);
		mySpinner.refreshDrawableState();
	}

	/**
	 * The Handler that gets information back from the BluetoothService Then
	 * display informations or redirect it to the service class to perform an
	 * action.
	 */
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case Bluetooth_Constants.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case Bluetooth.STATE_CONNECTED:
					Toast.makeText(getApplicationContext(), "STATE_CONNECTED",
							Toast.LENGTH_SHORT).show();
					// MAJ de la BD du createur connecte par Bluetooth
					_presenter.syncBluetooth(emplois);
					break;
				case Bluetooth.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(), "STATE_CONNECTING",
							Toast.LENGTH_SHORT).show();
					break;
				case Bluetooth.STATE_LISTEN:
					Toast.makeText(getApplicationContext(), "STATE_LISTEN",
							Toast.LENGTH_SHORT).show();
					break;
				case Bluetooth.STATE_NONE:
					Toast.makeText(getApplicationContext(), "STATE_NONE",
							Toast.LENGTH_SHORT).show();
					// try to reconnect
					_presenter.startConnection(mHandler);
					break;
				}
				break;
			case Bluetooth_Constants.MESSAGE_WRITE:

				break;
			case Bluetooth_Constants.MESSAGE_READ: {
				// MAJ de la base de donnees envoyee par Bluetooth
				emplois = _presenter.getEdtBluetooth(msg.obj);
				setSpinner();

			}

				break;
			case Bluetooth_Constants.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				final String deviceName = msg.getData().getString(
						Bluetooth_Constants.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connection a " + deviceName + " ...",
						Toast.LENGTH_SHORT).show();
				break;
			case Bluetooth_Constants.MESSAGE_TOAST:

				//Toast.makeText(getApplicationContext(),
				//		msg.getData().getString(Bluetooth_Constants.TOAST),
				//		Toast.LENGTH_SHORT).show();
				break;

			}
		}

	};

	/**
	 * {@inheritDoc}.
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		_presenter.destroy(emplois);
	}

	private void executeDelayed() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			public void run() {
				// execute after 500ms
				Utile.hideNavBar(a);
			}
		}, 500);
	}

}
