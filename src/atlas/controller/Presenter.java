package atlas.controller;

/*
 * Projet SIT, @copyright 2015 SAGEM DS
 * Les informations contenues dans ce fichier sont la propriété de
 * SAGEM DS et diffusées à titre confidentiel dans un but spécifique.
 * Le destinataire assure la garde et la surveillance de ce fichier et
 * convient qu'il ne sera ni copié ni reproduit en tout ou partie et
 * que son contenu ne sera révélé en aucune manière à aucune personne,
 * excepté pour répondre au but pour le quel il a été transmis.
 * Cette recommandation est applicable à tous les documents générés à
 * partir de ce fichier.
 */

import java.util.ArrayList;

import modele.DataBaseAccess.Activite;
import modele.DataBaseAccess.Emploi;
import modele.DataBaseAccess.Heure;
import modele.EmploiDuTemps;
import modele.HeuresMarquees;
import modele.Task;
import services.Bluetooth;
import services.Storage;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import atlas.frisedejournee.R;

/**
 * @author local
 */
public class Presenter {

	private final Context _context;

	// connection attributes

	/** The bluetooth adapter is the basis of bluetooth connection. */
	private final BluetoothAdapter _blueAdapter;

	/** Service for a bluetooth/network connection. */
	private Bluetooth _service = null;

	/**
	 * Constructor.
	 * 
	 * @param _context
	 */
	public Presenter(final Context _context) {
		super();
		this._context = _context;
		_blueAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (_blueAdapter == null) {
			Toast.makeText(_context, "Le Bluetooth n'est pas supporte",
					Toast.LENGTH_LONG).show();
		}
		_service = null;

	}

	/**
	 * Stop the service's Threads, end the connection. Save all the datas into
	 * the backup file.
	 * 
	 * @param emplois
	 */
	public void destroy(ArrayList<EmploiDuTemps> emplois) {
		if (_service != null) {
			_service.stop();
		}
		//sauvegardeFichier(emplois);
	}

	/**
	 * Si le bluetooth est active, lance ue connection bluetooth. Si le
	 * bluetooth n'est pas actif, ne fait rien.
	 * 
	 * @param mHandler
	 *            a Handler to allow communication with the presenter and the
	 *            UI.
	 * @return wether the connection is initialized.
	 */
	public void startConnection(final Handler mHandler) {

		// check if the device support bluetooth
		if (_blueAdapter == null) {
			return;
		}
		if (_blueAdapter.isEnabled()) {

			// do this once, if there is no connection service yet
			if (_service == null) {
				// Initialize the BluetoothService to perform bluetooth
				// connections
				_service = new Bluetooth(_context, mHandler);
			}
			_service.start();

		}

	}

	/**
	 * Recupere tous les emplois du temps envoyes par Bluetooth et remet a jour
	 * la base de donnee.
	 * 
	 * @param obj
	 * @return la liste des emplois charges (vide au pire)
	 */
	public ArrayList<EmploiDuTemps> getEdtBluetooth(Object obj) {
		final byte[] bytes = (byte[]) (obj);
		final ArrayList<EmploiDuTemps> edt = EmploiDuTemps.deserialize(bytes);
		if (!edt.isEmpty()) {
			Toast.makeText(_context,
					_context.getResources().getString(R.string.emplois_recus),
					Toast.LENGTH_SHORT).show();

			// remise a jour de la base de donnees
			updateDataBase(edt);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return getDataBase();

	}

	/**
	 * Recupere tous les emplois du temps VALIDES de la base de donnees ou du
	 * fichier de sauvegarde si cette derniere est vide, remet a jour la BD dans
	 * ce cas. Synchronize la base de la tablette avec celle du Smartphone par
	 * Bluetooth.
	 * 
	 * @return la liste des emplois charges (vide au pire)
	 */
	public ArrayList<EmploiDuTemps> chargeEmplois() {

		// lecture depuis la base de donnees
		ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();
		emplois = getDataBase();

		// lecture depuis le fichier si la base de donnees est vide

		/*if (emplois.isEmpty()) {
			emplois = EmploiDuTemps.deserialize(null);

			// remet a jour la base de donnees
			recoverDataBase();
		}*/

		// trie les taches des emplois du temps
		for (EmploiDuTemps emploi : emplois) {
			emploi.setEmploi(Task.trierTask(emploi.getEmploi()));
		}

		return emplois;
	}

	/**
	 * Charge tous les edt VALIDES de la base de donnees. Renvoi une liste vide
	 * si la base est vide ou s'il n'y a pas d'edt valides.
	 * 
	 * @return
	 */
	private ArrayList<EmploiDuTemps> getDataBase() {
		ContentResolver resolver = _context.getContentResolver();
		String[] projection = Emploi.PROJECTION_ALL;
		ArrayList<EmploiDuTemps> emplois = new ArrayList<EmploiDuTemps>();

		// tous les emplois valides
		Cursor c = resolver.query(Emploi.CONTENT_URI, projection, projection[2]
				+ " = ?", new String[] { String.valueOf(1) }, null);
		if (c != null) {
			// parcours tous les edt valides
			while (c.moveToNext()) {
				final int edt_id = c.getInt(0);
				final String nom_edt = c.getString(1);
				ArrayList<Task> tasks = getDataBaseTask(resolver, nom_edt);
				ArrayList<HeuresMarquees> heures = getDataBaseHours(resolver,
						nom_edt);
				final EmploiDuTemps edt = new EmploiDuTemps(edt_id, tasks,
						nom_edt, heures);
				edt.setValid(true);
				emplois.add(edt);
			}
			c.close();
		}
		return emplois;
	}

	/**
	 * 
	 * @param resolver
	 * @param nom_edt
	 * @return La liste des activites de la base de donnees.
	 */
	private ArrayList<Task> getDataBaseTask(ContentResolver resolver,
			String nom_edt) {
		ArrayList<Task> tasks = new ArrayList<Task>();
		// curseur contenant toutes les activites d'un meme edt
		// toutes les activites de cet edt
		String[] projection = Activite.PROJECTION_ALL;
		Cursor c = resolver.query(Activite.CONTENT_URI, projection,
				projection[1] + " LIKE ?", new String[] { nom_edt }, null);
		// parcours toutes les activites
		while (c.moveToNext()) {
			int id = c.getInt(0);
			String nomTask = c.getString(2);
			double heureDebut = c.getDouble(3);
			double heureFin = c.getDouble(4);
			String image = c.getString(5);
			int couleur = c.getInt(6);
			String description = c.getString(7);

			tasks.add(new Task(id, nom_edt, nomTask, description,heureDebut, heureFin, image, couleur));
		}
		c.close();
		return tasks;

	}

	/**
	 * 
	 * @param resolver
	 * @param nom_edt
	 * @return La liste des heuresMarquees de la base de donnees.
	 */
	private ArrayList<HeuresMarquees> getDataBaseHours(
			ContentResolver resolver, String nom_edt) {
		ArrayList<HeuresMarquees> heures = new ArrayList<HeuresMarquees>();
		// curseur contenant toutes les activites d'un meme edt
		// toutes les activites de cet edt
		String[] projection = Heure.PROJECTION_ALL;
		Cursor c = resolver.query(Heure.CONTENT_URI, projection, projection[1]
				+ " LIKE ?", new String[] { nom_edt }, null);
		// parcours toutes les activites
		while (c.moveToNext()) {
			final int id = c.getInt(0);
			final double heure = c.getDouble(2);
			heures.add(new HeuresMarquees(id, heure, nom_edt));
		}
		c.close();
		return heures;

	}

	/**
	 * Envoi tous les edt contenus dans la base de donnees pour mettre a jour la
	 * base de donnees du Smartphone ou de la tablette par Bluetooth.
	 * 
	 * @param emplois
	 */
	public void syncBluetooth(ArrayList<EmploiDuTemps> emplois) {

		// check if the device support bluetooth
		if (_blueAdapter == null) {
			return;
		}
		// Check that we're actually connected before trying anything
		if (!_blueAdapter.isEnabled()
				|| _service.getState() != Bluetooth.STATE_CONNECTED) {
			Toast.makeText(_context, R.string.not_connected_bluetooth,
					Toast.LENGTH_SHORT).show();
			return;

		}

		// Check that there's actually something to send
		if (!emplois.isEmpty()) {
			// Get the message bytes and tell the BluetoothService to write
			final byte[] send = EmploiDuTemps.serialize(emplois);
			_service.write(send);

		}

	}
	

	/**
	 * met a jour la base de donnees
	 * 
	 * @param emplois
	 *            Les emplois du temps a inserer/ modifier dans la base
	 */
	private void updateDataBase(ArrayList<EmploiDuTemps> emplois) {

		for (EmploiDuTemps emploi : emplois) {
			deleteEDT(emploi);
			insererEDT(emploi);
		}

	}

	/**
	 * supprime un emploi du temps de la base de donnees
	 * 
	 * @param emploi
	 */
	private void deleteEDT(EmploiDuTemps emploi) {
		ContentResolver resolver = _context.getContentResolver();

		// suppression des edt
		String[] edt_projection = Emploi.PROJECTION_ALL;
		resolver.delete(Emploi.CONTENT_URI, edt_projection[1] + " LIKE ?",
				new String[] { emploi.getNomEnfant() });

		// suppression des activites
		String[] task_projection = Activite.PROJECTION_ALL;
		resolver.delete(Activite.CONTENT_URI, task_projection[1] + " LIKE ?",
				new String[] { emploi.getNomEnfant() });

		// suppression des Heures
		String[] heure_projection = Heure.PROJECTION_ALL;
		resolver.delete(Heure.CONTENT_URI, heure_projection[1] + " LIKE ?",
				new String[] { emploi.getNomEnfant() });

	}

	/**
	 * insere un emploi du temps dans la base de donnees
	 * 
	 * @param emploi
	 */
	private void insererEDT(EmploiDuTemps emploi) {
		ContentResolver resolver = _context.getContentResolver();
		String[] edt_projection = Emploi.PROJECTION_ALL;
		ContentValues values = new ContentValues();
		// values.put(edt_projection[0], emploi.getId());
		values.put(edt_projection[1], emploi.getNomEnfant());
		values.put(edt_projection[2], emploi.isValid());

		resolver.insert(Emploi.CONTENT_URI, values);

		// insertion des activites
		String[] task_projection = Activite.PROJECTION_ALL;
		ArrayList<Task> tasks = emploi.getEmploi();
		for (Task task : tasks) {
			ContentValues task_values = new ContentValues();
			// task_values.put(task_projection[0], task.getId());
			task_values.put(task_projection[1], emploi.getNomEnfant());
			task_values.put(task_projection[2], task.getNom());
			task_values.put(task_projection[3], task.getHeureDebut());
			task_values.put(task_projection[4], task.getHeureFin());
			task_values.put(task_projection[5], task.getImage());
			task_values.put(task_projection[6], task.getCouleur());
			task_values.put(task_projection[7], task.getDescription());

			resolver.insert(Activite.CONTENT_URI, task_values);

		}

		// insertion des Heures
		String[] heure_projection = Heure.PROJECTION_ALL;
		ArrayList<HeuresMarquees> heures = emploi.getMarqueTemps();
		for (HeuresMarquees heure : heures) {
			ContentValues heure_values = new ContentValues();
			// heure_values.put(heure_projection[0], heure.getId());
			heure_values.put(heure_projection[1], emploi.getNomEnfant());
			heure_values.put(heure_projection[2], heure.getHeure_marquee());

			resolver.insert(Heure.CONTENT_URI, heure_values);

		}

	}

	/**
	 * Met a jour la base de donnees avec le fichier de sauvegarde (si la BD
	 * etait corrompue ou si on a reinstalle l'appli).
	 */
	private void recoverDataBase() {
		ArrayList<EmploiDuTemps> emplois = EmploiDuTemps.deserialize(null);
		for (EmploiDuTemps emploi : emplois) {
			insererEDT(emploi);
		}

	}

	/**
	 * Sauvegarde toutes la base de donnees dans un fichier de sauvegarde.
	 * 
	 * @param emplois
	 */
	private void sauvegardeFichier(ArrayList<EmploiDuTemps> emplois) {
		if (!emplois.isEmpty()) {
			final byte[] bytes = EmploiDuTemps.serialize(emplois);
			Storage.writeFriseFile(bytes);
			Toast.makeText(_context,
					_context.getResources().getString(R.string.sauvegarde),
					Toast.LENGTH_SHORT).show();
		}

	}

}
