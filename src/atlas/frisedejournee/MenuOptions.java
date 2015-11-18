package atlas.frisedejournee;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import composants.AnimatedGnar;
import composants.Animer;
import composants.Horloge;
import composants.MyLayoutParams;
import composants.Police;

public class MenuOptions {

	private Context context;
	private RelativeLayout[] menu; // les elements du menu : un titre et 6
									// boutons
	private RelativeLayout slide_bottom; // le layout qui contient les options
	private Options options;

	/**
	 * 
	 * @param context
	 *            le contexte de l'application utilisatrice du menu
	 */
	public MenuOptions(Context context, Options options) {
		this.context = context;
		menu = new RelativeLayout[8];
		slide_bottom = new RelativeLayout(context);
		this.options = options;
	}

	/**
	 * cette methode permet la creation d'un certain type de menu : horizontal,
	 * avec des layouts sur la droite (en rang�es de 2 layouts fusionnables) et
	 * un gros titre a gauche auquel on peut rajouter des elements et animations
	 * 
	 * @param parent
	 *            le parent de l'activite
	 * @return
	 */
	public RelativeLayout[] createMenu() {

		int width = context.getApplicationContext().getResources()
				.getDisplayMetrics().widthPixels;
		int height = context.getApplicationContext().getResources()
				.getDisplayMetrics().heightPixels;

		// l'orienttation de l'ecran
		Activity a = (Activity) context;
		a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// les parametres du layout qui contient les options
		RelativeLayout.LayoutParams boutons_params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		boutons_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		slide_bottom.setLayoutParams(boutons_params);

		// on attribue un id a chaque emplacement de layout/titre
		for (int i = 0; i < 7; i++) {
			menu[i] = new RelativeLayout(context);
			menu[i].setId(i);
		}

		// le placement des emplacements
		for (int i = 0; i < 7; i++) {

			// param�tre de taille de chaque layout d'option
			int marge = height / 40;
			RelativeLayout.LayoutParams params = new LayoutParams(width / 2
					- marge, height / 5);
			switch (i) {

			// case 0 : layout du titre
			case 0:
				RelativeLayout.LayoutParams titre_params = new LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				titre_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				titre_params.addRule(RelativeLayout.CENTER_HORIZONTAL);
				titre_params.addRule(RelativeLayout.ABOVE, menu[0].getId());
				menu[0].setLayoutParams(titre_params);
				break;

			// les 6 layouts des options
			case 1:
				slide_bottom.addView(menu[1]);
				params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				params.setMargins(marge, 0, 0, 0);
				menu[1].setLayoutParams(params);

				break;

			case 2:
				slide_bottom.addView(menu[2]);
				params.addRule(RelativeLayout.RIGHT_OF, menu[1].getId());
				params.setMargins(marge, 0, marge, 0);
				menu[2].setLayoutParams(params);

				break;

			case 3:
				slide_bottom.addView(menu[3]);
				params.addRule(RelativeLayout.BELOW, menu[1].getId());
				params.addRule(RelativeLayout.ALIGN_LEFT, menu[1].getId());
				params.setMargins(0, marge, 0, 0);
				menu[3].setLayoutParams(params);

				break;

			case 4:
				slide_bottom.addView(menu[4]);
				params.addRule(RelativeLayout.BELOW, menu[1].getId());
				params.addRule(RelativeLayout.RIGHT_OF, menu[3].getId());
				params.setMargins(marge, marge, marge, 0);
				menu[4].setLayoutParams(params);

				break;

			case 5:
				slide_bottom.addView(menu[5]);
				params.addRule(RelativeLayout.BELOW, menu[3].getId());
				params.addRule(RelativeLayout.ALIGN_LEFT, menu[1].getId());
				params.setMargins(0, marge, 0, 0);
				menu[5].setLayoutParams(params);

				break;

			case 6:
				slide_bottom.addView(menu[6]);
				params.addRule(RelativeLayout.BELOW, menu[3].getId());
				params.addRule(RelativeLayout.RIGHT_OF, menu[5].getId());
				params.setMargins(marge, marge, marge, 0);
				menu[6].setLayoutParams(params);

				break;
			}

		}

		return menu;

	}

	/**
	 * on cherche a fusionner 2 layout contenus dans le menu qui sont sur une
	 * meme ligne, pour creer un layout qui part de l1 et qui a la taille de l1
	 * et l2 en comptant les marges entre les 2 remplace l1 par ce
	 * "grand layout" et supprime l2 de la scene et du menu (l2 <-null)
	 * 
	 * @param l1
	 *            l'emplacement a gauche (1,3,5)
	 * @param l2
	 *            l'emplacement a droite (2,4,6)
	 */
	public void rassembler(int l1, int l2) {
		int height = context.getApplicationContext().getResources()
				.getDisplayMetrics().heightPixels;
		int marge = height / 40;
		if ((l1 == 1 || l1 == 3 || l1 == 5) && (l2 == 2 || l2 == 4 || l2 == 6)) {
			RelativeLayout l = new RelativeLayout(context);
			slide_bottom.addView(l);
			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_LEFT, menu[l1].getId());
			params.addRule(RelativeLayout.ALIGN_RIGHT, menu[l2].getId());
			params.addRule(RelativeLayout.ALIGN_TOP, menu[l1].getId());
			params.addRule(RelativeLayout.ALIGN_BOTTOM, menu[l1].getId());
			l.setLayoutParams(params);
			params.setMargins(0, 0, marge, 0);
			menu[l1] = l;
			slide_bottom.removeView(menu[l2]);
			menu[l2] = null;
		} else
			Log.d("erreur",
					" les emplacements specifi�s l1 ou l2 ne sont pas corrects");

	}

	/**
	 * permet d'ajouter une option � la place voulue sur l'ecran
	 * 
	 * @param place
	 *            le numero de l'emplacement du bouton (entre 1 et 6)
	 * 
	 * @param typeOption
	 *            le type d'option � rajouter(son, horloge, gnar)
	 */
	public RadioGroup addOption(String typeOption, int place) {

		if (place < 7 && place > 0 && menu[place] != null) {

			RelativeLayout opt = new RelativeLayout(context);
			opt.setClipChildren(false);
			menu[place].addView(opt);
			RelativeLayout.LayoutParams params = new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			opt.setLayoutParams(params);

			RelativeLayout option = new RelativeLayout(context);
			option.setClipChildren(false);
			MyLayoutParams option_params = new MyLayoutParams()
					.centerVertical();
			option_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			option.setId(23);
			option.setLayoutParams(option_params);

			opt.addView(option);

			RadioButton oui = new RadioButton(context);
			oui.setText(" oui ");
			oui.setId(1213);
			float scaleFactor = 1.1f;
			oui.setScaleX(scaleFactor);
			oui.setScaleY(scaleFactor);
			oui.setTextSize(oui.getTextSize() * scaleFactor);
			Police.setFont((Activity) context, oui, "intsh.ttf");

			RadioButton non = new RadioButton(context);
			non.setText(" non ");
			non.setId(1214);
			non.setScaleX(scaleFactor);
			non.setScaleY(scaleFactor);
			non.setTextSize(non.getTextSize() * scaleFactor);
			Police.setFont((Activity) context, non, "intsh.ttf");

			RadioGroup group = new RadioGroup(context);
			group.setOrientation(LinearLayout.HORIZONTAL);
			MyLayoutParams group_params = new MyLayoutParams().centerVertical();
			// group_params.setMargins(40, 0, 0, 0);
			group_params.addRule(RelativeLayout.RIGHT_OF, option.getId());

			group.setLayoutParams(group_params);
			group.addView(oui);
			group.addView(non);

			if (typeOption.equals("gnar")) {
				option.setBackground(context.getResources().getDrawable(
						R.drawable.mini_tete_2));
				if (options.getGnar()) {
					group.check(1213);
					oui.setChecked(true);
				} else {
					group.check(1214);
					non.setChecked(true);
				}
			} else if (typeOption.equals("horloge")) {
				Horloge.create(option, context, 3, 30, 45);
				if (options.getHorloge()) {
					group.check(1213);
					oui.setChecked(true);
				} else {
					group.check(1214);
					non.setChecked(true);
				}
			} else if (typeOption.equals("son")) {
				option.setBackground(context.getResources().getDrawable(
						R.drawable.sound));
				if (options.getSound()) {
					group.check(1213);
					oui.setChecked(true);
				} else {
					group.check(1214);
					non.setChecked(true);
				}
			} else if (typeOption.equals("bulle")) {
				option.setBackground(context.getResources().getDrawable(
						R.drawable.bulle_bas));
				if (options.getBulle()) {
					group.check(1213);
					oui.setChecked(true);
				} else {
					group.check(1214);
					non.setChecked(true);
				}
			} else if (typeOption.equals("sommaire")) {
				TextView t = new TextView(context);
				t.setText("sommaire   ");
				Police.setFont((Activity) context, t, "intsh.ttf");
				t.setTextSize(30);
				t.setTextColor(context.getResources().getColor(R.color.grey7));
				RelativeLayout.LayoutParams t_params = new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.addRule(RelativeLayout.CENTER_IN_PARENT);
				t.setLayoutParams(t_params);
				t.setBackgroundColor(context.getResources().getColor(
						R.color.blanc));
				option.addView(t);

				if (options.getSommaire()) {
					group.check(1213);
					oui.setChecked(true);
				} else {
					group.check(1214);
					non.setChecked(true);
				}
			}

			opt.addView(group);

			return group;
		} else {
			Log.d("Attention", "le layout designe ne convient pas ou est nul");
			return null;
		}
	}

	/**
	 * permet de specifier le titre du menu, la taille du texte, sa couleur et
	 * sa police sont imposees
	 * 
	 * @param texte
	 *            le titre du menu
	 */
	public void addTitre(String texte) {

		int height = context.getApplicationContext().getResources()
				.getDisplayMetrics().heightPixels;
		TextView t = new TextView(context);
		menu[0].addView(t);
		RelativeLayout.LayoutParams params = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(0, height / 6, 0, 0);

		t.setGravity(Gravity.CENTER_HORIZONTAL);
		t.setLayoutParams(params);
		t.setText(texte);
		Police.setFont((Activity) context, t, "intsh.ttf");
		t.setTextSize(80);
		Animer.pop_in(t, 2000);
		t.setTextColor(context.getResources().getColor(R.color.orange2));

	}

	public RelativeLayout getTitre() {
		return menu[0];
	}

	public RelativeLayout getSlide() {
		return slide_bottom;
	}

	/**
	 * methode qui detruit toutes les vues qui se trouvent dans l'emplacement
	 * designe et laisse donc cet emplacement vide
	 * 
	 * @param place
	 *            l'emplacement que l'on veut remettre a zeros
	 */
	public void destroy(int place) {
		if (place < 7 && place > 0 && menu[place] != null) {
			menu[place].removeAllViews();
		} else {
			Log.d("Attention", "le layout designe ne convient pas ou est nul");
		}
	}

}
