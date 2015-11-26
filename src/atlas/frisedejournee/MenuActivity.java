package atlas.frisedejournee;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
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
import android.view.View.OnClickListener;
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

import composants.AnimatedGnar;
import composants.AnimatedText;
import composants.Animer;
import composants.Bulle;
import composants.Police;
import composants.Utile;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;


public class MenuActivity extends Activity
{

    private int H; // hauteur de l'ecran en px

    private ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();

    private Options options = new Options(true, true, true, true, true);
    private final Activity a = this;
    
    protected void onCreate(final Bundle savedInstanceState)
    {
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
        final Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/Action_Man.ttf");
        final TextView jeSuis = (TextView) findViewById(R.id.jeSuis);
        jeSuis.setTypeface(externalFont);
        jeSuis.setTextSize(35f);

        /* Police du titre menu */
        final TextView titre_menu = (TextView) findViewById(R.id.texte_menu);
        titre_menu.setTypeface(externalFont);

        /* Police du spinner */
        final Spinner mySpinner = (Spinner) findViewById(R.id.enfant_spinner);
        setBackground(mySpinner, getResources().getDrawable(R.drawable.spinner_back));
        mySpinner.setPadding(20, 10, 20, 10);

        // Importation des emplois du temps
        chargerEmplois();

        // recuperation des options
        final Intent opt = getIntent();
        if ((Options) opt.getSerializableExtra("options") != null)
        {
            options = (Options) opt.getSerializableExtra("options");
        }

        /* Spinner ajout des enfants */

        final String[] array_spinner = new String[emplois.size()];
        for (int i = 0; i < emplois.size(); i++)
        {
            array_spinner[i] = emplois.get(i).getNomEnfant();
        }

        // bulle d'aide

        final TextView bulle =
            Bulle.create(mySpinner, "Choisis ton prenom dans la liste", "right", true, this);
        if (options.getSound())
        {
            TTSBouton.parle(bulle, "Choisis ton prenom dans la liste", this);
        }
        if (!options.getBulle())
        {
            bulle.setVisibility(View.INVISIBLE);
        }

        final ArrayAdapter<CharSequence> adapter =
            new ArrayAdapter<CharSequence>(this, R.layout.spinner_item_text, array_spinner);

        mySpinner.setAdapter(adapter);

        /* Clic sur spinner */
        mySpinner.setOnTouchListener(new View.OnTouchListener()
        {

            
            public boolean onTouch(final View v, final MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    TTSBouton.fermer(bulle, getApplicationContext());
                    Bulle.destroy(bulle, a);

                }
                executeDelayed();
                return false;
            }
        });

        /* Boutons et titre */
        final Drawable bouton_go_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
        final Button boutonGo = (Button) findViewById(R.id.go);
        // boutonGo.setTextSize(20f);
        setBackground(boutonGo, bouton_go_d);
        boutonGo.setTypeface(externalFont);

        boutonGo.setOnClickListener(new View.OnClickListener()
        {

            
            public void onClick(final View v)
            {

                if (!emplois.isEmpty())
                {
                    /* Changement de l'aspect du bouton lorsqu'on l'enfonce */
                    final Drawable bouton_go_pressed =
                        Bouton.pressedRoundedDrawable(a, R.color.light_blue3, 1f);
                    setBackground(boutonGo, bouton_go_pressed);

                    /* Recuperation du nom de l'enfant selectione */
                    final Spinner spinner = (Spinner) findViewById(R.id.enfant_spinner);
                    final int indice = spinner.getSelectedItemPosition();

                    /* animation rideau sur l'ecran violet */
                    final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
                    Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
                    final ImageView slide_top_shadow =
                        (ImageView) findViewById(R.id.slide_top_shadow);
                    Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3, 700);
                    // passage a l'autre activite
                    final EmploiDuTemps emploi = emplois.get(indice);
                    final RelativeLayout slide_bottom =
                        (RelativeLayout) findViewById(R.id.slide_bottom);
                    Animer.changeActivityAnimation(slide_bottom, FriseActivity.class, emploi,
                        "emploi", options, "options");
                }
                else
                {
                    alert("Impossible de lancer la frise",
                        "Il doit y avoir au moins un emploi du temps de creee");
                }

            }
        });

        // Bouton des options
        final Button bouton_options = (Button) findViewById(R.id.options_bouton);
        final Drawable option_d = Bouton.roundedDrawable(this, R.color.amber7, 0.5f);
        setBackground(bouton_options, option_d);
        Police.setFont(a, bouton_options, "Action_Man.ttf");
        bouton_options.setOnClickListener(new OnClickListener()
        {

            
            public void onClick(final View v)
            {
                final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
                Animer.translateDecelerate(slide_top, 0, 0, 0, -H / 3, 700);
                final ImageView slide_top_shadow = (ImageView) findViewById(R.id.slide_top_shadow);
                Animer.translateDecelerate(slide_top_shadow, 0, 0, 0, -H / 3, 700);
                final RelativeLayout slide_bottom =
                    (RelativeLayout) findViewById(R.id.slide_bottom);
                Animer.changeActivityAnimation(slide_bottom, OptionsActivity.class, options,
                    "options", null, null);
            }
        });

        // Bouton exit //
        final Button exit = (Button) findViewById(R.id.exit);
        Utile.setSize(exit, H / 13, H / 13);
        exit.setOnClickListener(new ExitApplicationListener(exit, getResources().getDrawable(
            R.drawable.close), MenuActivity.this));

        /* Apparition du logo puis de l'activite */
        final ImageView logo = (ImageView) findViewById(R.id.logo);
        final ImageView shadow = (ImageView) findViewById(R.id.slide_top_shadow);
        final RelativeLayout slide_top = (RelativeLayout) findViewById(R.id.slide_top);
        final RelativeLayout slide_bottom = (RelativeLayout) findViewById(R.id.slide_bottom);
        Animer.activityApparitionAnimation(logo, slide_bottom, slide_top, shadow, H);

        // Animation titre
        final LinearLayout layout_titre = (LinearLayout) findViewById(R.id.titre);
        final int[] colors =
            {R.color.red3, R.color.pink3, R.color.purple3, R.color.deep_purple3, R.color.indigo3};

        AnimatedText.add(this, layout_titre, "Frizz", colors, 100);

        // Animation Gnar
        final RelativeLayout gnar = (RelativeLayout) findViewById(R.id.gnar);
        AnimatedGnar.addGnar(this, gnar);

        // Animation mini Gnar
        final RelativeLayout mini_gnar = (RelativeLayout) findViewById(R.id.baby_gnar);
        AnimatedGnar.addMiniGnar(this, mini_gnar);

        if (!options.getGnar())
        {
            mini_gnar.setVisibility(View.INVISIBLE);
            gnar.setVisibility(View.INVISIBLE);
        }

    }

    
    /* L'activite revient sur le devant de la scene */
    public void onResume()
    {
        super.onResume();
        final Button boutonGo = (Button) findViewById(R.id.go);
        final Drawable bouton_go_d = Bouton.roundedDrawable(this, R.color.light_blue3, 1f);
        setBackground(boutonGo, bouton_go_d);
        executeDelayed();
        Utile.fullScreenResume(this);

    }

    /**
	 * sets the background of a view depending on the API
	 * 
	 * @param v
	 * @param d
	 */
	private static void setBackground(final View v, final Drawable d) {
		if (Build.VERSION.SDK_INT >= 16) {
			// v.setBackground(d);
			Method methodBackgroung;
			try {
				methodBackgroung = View.class.getMethod("setBackground",
						Drawable.class);
				methodBackgroung.invoke(v, d);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			v.setBackgroundDrawable(d);
		}
	}

    private void executeDelayed()
    {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {

            
            public void run()
            {
                // execute after 500ms
                Utile.hideNavBar(a);
            }
        }, 500);
    }

   
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable()
    {
        final String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            return true;
        }
        return false;
    }

    /**
     * Lit le fichier frise.txt
     * @param texte
     */
    public File readFriseFile()
    {
        try
        {
            if (isExternalStorageReadable())
            {
                final File sdCard = Environment.getExternalStorageDirectory();
                final File directory = new File(sdCard.getAbsolutePath() + "/FilesFrise");
                directory.mkdirs();
                final File file = new File(directory, "frise.txt");
                if (file.exists())
                {
                    return file;
                }
                else
                {
                    return null;
                }
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ajoute les emplois du temps presents sur le fichier texte
     */
    public void chargerEmplois()
    {
        final File frise = readFriseFile();
        emplois = TaskReader.read(frise, this);
        if (emplois.size() == 0)
        {
            emplois.add(EmploiDuTemps.emploiTest());
        }
    }

    public void alert(final String titre, final String message)
    {
        new AlertDialog.Builder(this).setTitle(titre).setMessage(message)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
            {

                
                public void onClick(final DialogInterface dialog, final int which)
                {
                    dialog.cancel();
                }
            }).setIcon(android.R.drawable.ic_dialog_alert).show();
    }

}
