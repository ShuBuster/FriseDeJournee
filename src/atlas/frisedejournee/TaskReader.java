package atlas.frisedejournee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public class TaskReader {

	public static ArrayList<Task> read(Context context, String fichier) {
		
		ArrayList<Task> myTasks = new ArrayList<Task>();
		Scanner s;
		
		try {
			String name = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fichier;
			File source = new File(name); // ouverture du fichier texte

			InputStream ips = new FileInputStream(source);//context.openFileInput(fichier);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;

			while ((ligne = br.readLine()) != null) { // tant qu'il y a des taches
				s = new Scanner(ligne);
				String nom = s.next();
				s.close();

				ligne = br.readLine();
				s = new Scanner(ligne);
				String description = s.next();
				s.close();
				
				ligne = br.readLine();
				s = new Scanner(ligne);
				int duree = s.nextInt();
				s.close();
				
				ligne = br.readLine();
				s = new Scanner(ligne);
				int heureDebut = s.nextInt();
				s.close();
				
				ligne = br.readLine();
				s = new Scanner(ligne);
				String nomImage = s.next();
				s.close();
				Log.d("tag", "FONCTONNE");
				// Creer la task et l'ajoute a la liste
				int imageId = context.getResources().getIdentifier(nomImage , "drawable", context.getPackageName());
				Drawable image = context.getResources().getDrawable(imageId);
				Task myTask = new Task(nom, description, duree, heureDebut,image);
				myTasks.add(myTask);
				
				br.readLine(); // passe le saut de ligne entre les requetes

			}

		}catch (Exception e) {
			Log.v("tag","Tout va bien");
			System.out.println(e.toString());
		}
		return myTasks;
	}
}
