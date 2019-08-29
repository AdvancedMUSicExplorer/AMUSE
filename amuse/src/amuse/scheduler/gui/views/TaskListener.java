package amuse.scheduler.gui.views;

import amuse.interfaces.nodes.TaskConfiguration;

public interface TaskListener {
	public void experimentStarted(TaskConfiguration experiment);
	
	public void experimentFinished(TaskConfiguration experiment);
}
