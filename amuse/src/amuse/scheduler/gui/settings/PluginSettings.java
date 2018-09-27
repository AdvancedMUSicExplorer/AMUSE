/**
 * This file is part of AMUSE framework (Advanced MUsic Explorer).
 *
 * Copyright 2006-2010 by code authors
 *
 * Created at TU Dortmund, Chair of Algorithm Engineering
 * (Contact: <http://ls11-www.cs.tu-dortmund.de>)
 *
 * AMUSE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMUSE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with AMUSE. If not, see <http://www.gnu.org/licenses/>.
 *
 * Creation date: 02.06.2010
 */
package amuse.scheduler.gui.settings;

import amuse.data.datasets.PluginTableSet;
import amuse.interfaces.scheduler.SchedulerException;
import amuse.preferences.AmusePreferences;
import amuse.preferences.KeysStringValue;
import amuse.scheduler.gui.controller.WizardController;
import amuse.scheduler.pluginmanagement.PluginInstaller;
import amuse.scheduler.pluginmanagement.PluginRemover;
import amuse.util.AmuseLogger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import net.miginfocom.swing.MigLayout;
import org.apache.log4j.Level;

/**
 *
 * @author waeltken
 */
class PluginSettings extends AmuseSettingsPageBody {

    private static final String name = "Manage Plugins";
    private final JTable installedPluginsTable = new JTable();
    private final File pluginTableFile = new File(AmusePreferences.getPluginTablePath());
    private final JButton installButton = new JButton("Install New");
    private final JButton uninstallButton = new JButton("Uninstall Selected");

    public PluginSettings() {
	panel.setLayout(new MigLayout("fillx"));
	panel.setBorder(new TitledBorder(name));
	panel.add(installedPluginsPanel(), "grow , wrap");
	panel.add(installButton, "split 2");
	panel.add(uninstallButton);
	installedPluginsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	installButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		installPlugin();
	    }
	});
	uninstallButton.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		unInstallPlugin();
	    }
	});
    }

    @Override
    public String toString() {
	return name;
    }

    private JScrollPane installedPluginsPanel() {
	JScrollPane listPanel = new JScrollPane(installedPluginsTable);
	updateModel();
	listPanel.setBorder(new TitledBorder("Installed Plugins"));
	return listPanel;
    }

    private void updateModel() {
	String[] columns = {"ID", "Name", "Version"};
	DefaultTableModel model = new MutedDefaultTableModel();
	model.setColumnCount(3);
	model.setColumnIdentifiers(columns);
	try {
	    PluginTableSet pluginTable = new PluginTableSet(pluginTableFile);
	    for (int i = 0; i < pluginTable.getValueCount(); i++) {
		Object[] values = new Object[3];
		values[0] = pluginTable.getIDs().get(i);
		values[1] = pluginTable.getNames().get(i);
		values[2] = pluginTable.getVersionDescription().get(i);
		model.addRow(values);
	    }
	} catch (IOException ex) {
	    printErr(ex.getLocalizedMessage());
	}
	installedPluginsTable.setModel(model);
	installedPluginsTable.setRowSorter(new TableRowSorter<DefaultTableModel>(model));
	installedPluginsTable.getColumnModel().getColumn(0).setMinWidth(60);
	installedPluginsTable.getColumnModel().getColumn(0).setMaxWidth(60);
	installedPluginsTable.getColumnModel().getColumn(2).setMinWidth(75);
	installedPluginsTable.getColumnModel().getColumn(2).setMaxWidth(75);
    }

    private void printErr(String msg) {
	AmuseLogger.write(this.getClass().getCanonicalName(), Level.ERROR, msg);
    }

    private void installPlugin() {
	JFileChooser fc = new JFileChooser();
	fc.setDialogTitle("Select Plugin Folder");
	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	int option = fc.showDialog(panel, "Select");
	if (option == JFileChooser.APPROVE_OPTION) {
	    WizardController.getInstance().goToLogger();
	    new Installer(fc.getSelectedFile()).start();
	}
    }

    private void unInstallPlugin() {
	int row = installedPluginsTable.getSelectedRow();
	if (row < 0) {
	    return;
	}
	Integer pluginID = (Integer) installedPluginsTable.getModel().getValueAt(row, 0);
	WizardController.getInstance().goToLogger();
	new UnInstaller(pluginID).start();
    }

    private class MutedDefaultTableModel extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int row, int column) {
	    return false;
	}
    }

    private class Installer extends Thread {

	private final File folder;

	public Installer(File folder) {
	    this.folder = folder;
	}

	@Override
	public void run() {
	    super.run();
	    PluginInstaller installer = new PluginInstaller(folder.getAbsolutePath());
	    try {
		installer.installPlugin();
		updateModel();
	    } catch (SchedulerException ex) {
		printErr(ex.getLocalizedMessage());
	    }

	}
    }

    private class UnInstaller extends Thread {

	private final int id;

	public UnInstaller(int id) {
	    this.id = id;
	}

	@Override
	public void run() {
	    super.run();
	    try {
		PluginRemover pm = new PluginRemover(id);
		pm.removePlugin();
		updateModel();
	    } catch (SchedulerException ex) {
		printErr(ex.getLocalizedMessage());
	    }
	}
    }
}
