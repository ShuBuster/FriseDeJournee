package atlas.frisedejournee;

import android.view.View;
import android.view.View.OnClickListener;

public class TaskListener implements OnClickListener {

	int taskID;
	Task task;
	FriseActivity frise;

	public TaskListener(int taskID,Task task, FriseActivity frise) {
		this.taskID = taskID;
		this.frise = frise;
		this.task = task;
	}

	@Override
	public void onClick(View v) {
		frise.taskClicked(taskID,task);
		
	}

};
