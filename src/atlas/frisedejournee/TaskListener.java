package atlas.frisedejournee;

import android.view.View;
import android.view.View.OnClickListener;

public class TaskListener implements OnClickListener {

	int taskID;
	FriseActivity frise;

	public TaskListener(int taskID, FriseActivity frise) {
		this.taskID = taskID;
		this.frise = frise;
	}

	@Override
	public void onClick(View v) {
		frise.taskClicked(taskID);
		
	}

};
