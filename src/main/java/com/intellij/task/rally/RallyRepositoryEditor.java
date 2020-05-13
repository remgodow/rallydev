package com.intellij.task.rally;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import org.sbelei.rally.domain.BasicEntity;
import org.sbelei.rally.domain.Iteration;
import org.sbelei.rally.domain.Workspace;

import javax.swing.*;

public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

    private JButton myWorkspacesButton;
    private JBLabel myWorkspaceLabel;
    private ComboBox myWorkspaces;
    private JButton loadProjects;

    private JBLabel myProjectLabel;
    private ComboBox myProjects;
    private JButton loadIterations;

    private JBLabel myIterationLabel;
    private ComboBox myIterations;
    private JCheckBox myIterationsCheckbox;
    private JCheckBox myShowCompleatedCheckbox;
    private DefaultComboBoxModel<Workspace> myWorkspacesModel;
    private DefaultComboBoxModel<org.sbelei.rally.domain.Project> myProjectsModel;
    private DefaultComboBoxModel<Iteration> myIterationsModel;

    public RallyRepositoryEditor(Project project, RallyRepository repository, Consumer<RallyRepository> changeListener) {
        super(project, repository, changeListener);
        myPasswordLabel.setText("API Token:");
        myURLText.setEnabled(false);
        myURLText.setVisible(false);
        myUrlLabel.setEnabled(false);
        myUrlLabel.setVisible(false);
        myShareUrlCheckBox.setVisible(false);
    }

    @Override
    @Nullable
    protected JComponent createCustomPanel() {
        FormBuilder fb = FormBuilder.createFormBuilder();

        myWorkspacesButton = new JButton("Get Workspaces");
        fb.addComponent(myWorkspacesButton);
        myWorkspacesButton.addActionListener(e -> {
            myWorkspacesButton.setVisible(false);
            var workspaces = myRepository.fetchWorkspaces();
            for (var workspace: workspaces) {
                myWorkspacesModel.addElement((Workspace)workspace);
            }
            myWorkspaceLabel.setVisible(true);
            myWorkspaces.setVisible(true);
            loadProjects.setVisible(true);
        });

        myWorkspacesModel = new DefaultComboBoxModel<Workspace>();
        myWorkspaces = new ComboBox(myWorkspacesModel);
        selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
        installListener(myWorkspaces);
        myWorkspaceLabel = new JBLabel("Workspace:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myWorkspaceLabel, myWorkspaces);
        myWorkspaceLabel.setVisible(false);
        myWorkspaces.setVisible(false);

        loadProjects = new JButton("Load projects");
        fb.addComponent(loadProjects);
        loadProjects.setVisible(false);
        loadProjects.addActionListener(e -> {
            loadProjects.setVisible(false);
            var projects = myRepository.fetchProjects();
            for (var project: projects) {
                myProjectsModel.addElement((org.sbelei.rally.domain.Project)project);
            }
            myProjectLabel.setVisible(true);
            myProjects.setVisible(true);
            loadIterations.setVisible(true);
        });
        myProjectsModel = new DefaultComboBoxModel<org.sbelei.rally.domain.Project>();
        myProjects = new ComboBox(myProjectsModel, 440);
        selectByEntityId(myProjects, myRepository.getProjectId());
        installListener(myProjects);
        myProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myProjectLabel, myProjects);
        myProjectLabel.setVisible(false);
        myProjects.setVisible(false);

        loadIterations = new JButton("Iterations");
        fb.addComponent(loadIterations);
        loadIterations.setVisible(false);
        loadIterations.addActionListener(e -> {
            loadIterations.setVisible(false);
            var iterations = myRepository.fetchIterations();
            for (var iteration: iterations) {
                myIterationsModel.addElement((Iteration) iteration);
            }
            myIterationLabel.setVisible(true);
            myIterations.setVisible(true);
        });
        myIterationsModel = new DefaultComboBoxModel<Iteration>();
        myIterations = new ComboBox(myIterationsModel, 440);
        selectIteration();
        installListener(myIterations);
        myIterationLabel = new JBLabel("Iteration:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myIterationLabel, myIterations);
        myIterationLabel.setVisible(false);
        myIterations.setVisible(false);

        myIterationsCheckbox = new JCheckBox("use current iteration");
        myIterationsCheckbox.setSelected(myRepository.isUseCurrentIteration());
        fb.addComponent(myIterationsCheckbox);
        installListener(myIterationsCheckbox);

        myShowCompleatedCheckbox = new JCheckBox("show compleated tasks");
        myShowCompleatedCheckbox.setSelected(myRepository.isShowCompleatedTasks());
        fb.addComponent(myShowCompleatedCheckbox);
        installListener(myShowCompleatedCheckbox);
        return fb.getPanel();
    }

    private void loadProjects() {

    }

    private void selectIteration() {
        if (!myRepository.isUseCurrentIteration()) {
            selectByEntityId(myIterations, myRepository.getIterationId());
        }
    }

    private void selectByEntityId(JComboBox combo, String id) {
        for (int i=0; i< combo.getItemCount(); i++) {
            BasicEntity enity = (BasicEntity) combo.getItemAt(i);
            if (enity.id.equals(id)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadItemsToCombobx(JComboBox combo, Object[] items) {
        combo.removeAllItems();
        for (Object item : items) {
            combo.addItem(item);
        }
    }

    @Override
    public void apply() {
        super.apply();
        myRepository.applyWorkspace(myWorkspaces.getSelectedItem());
        myRepository.applyProject(myProjects.getSelectedItem());
        myRepository.setUseCurrentIteration(myIterationsCheckbox.isSelected());
        if (!myRepository.isUseCurrentIteration()) {
            myRepository.applyIteration(myIterations.getSelectedItem());
        }
        myRepository.setShowCompleatedTasks(myShowCompleatedCheckbox.isSelected());
    }

    @Override
    protected void afterTestConnection(boolean connectionSuccessful) {
        super.afterTestConnection(connectionSuccessful);
        if (connectionSuccessful) {
            //load workspaces/projects/iterations
            loadItemsToCombobx(myWorkspaces, myRepository.fetchWorkspaces());
            selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
            loadItemsToCombobx(myProjects, myRepository.fetchWorkspaces());
            selectByEntityId(myProjects, myRepository.getProjectId());
            loadItemsToCombobx(myIterations, myRepository.fetchIterations());
            selectIteration();
        }
    }
	
	
	/**
	 * Contributed by: Peter Oxenham
 	 * https://github.com/peterox
 	 */
	@Override
    public void dispose() {
        super.dispose();
        myRepository.cleanup();
    }
}
