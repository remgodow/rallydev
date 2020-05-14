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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

    private JButton myWorkspacesButton;
    private JBLabel myWorkspaceLabel;
    private ComboBox myWorkspaces;

    private JBLabel myProjectLabel;
    private ComboBox myProjects;

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
            onWorkspaceButtonClicked();
        });

        if (myRepository.getWorkspaceId() != null)
        {
           onWorkspaceButtonClicked();
           selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
        }

        myWorkspacesModel = new DefaultComboBoxModel<Workspace>();
        myWorkspaces = new ComboBox(myWorkspacesModel);
        myWorkspaces.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Workspace item = (Workspace) e.getItem();
                var projects = myRepository.fetchProjects();
                myProjectsModel.removeAllElements();
                for (var project: projects) {
                    myProjectsModel.addElement((org.sbelei.rally.domain.Project)project);
                }
                myProjectLabel.setVisible(true);
                myProjects.setVisible(true);
            }
        });
        selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
        installListener(myWorkspaces);
        myWorkspaceLabel = new JBLabel("Workspace:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myWorkspaceLabel, myWorkspaces);
        myWorkspaceLabel.setVisible(false);
        myWorkspaces.setVisible(false);

        myProjectsModel = new DefaultComboBoxModel<org.sbelei.rally.domain.Project>();
        myProjects = new ComboBox(myProjectsModel, 440);
        myProjects.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                org.sbelei.rally.domain.Project item = (org.sbelei.rally.domain.Project) e.getItem();
                var iterations = myRepository.fetchIterations();
                myIterationsModel.removeAllElements();
                for (var iteration: iterations) {
                    myIterationsModel.addElement((Iteration) iteration);
                }
                myIterationLabel.setVisible(true);
                myIterations.setVisible(true);
            }
        });
        selectByEntityId(myProjects, myRepository.getProjectId());
        installListener(myProjects);
        myProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myProjectLabel, myProjects);
        myProjectLabel.setVisible(false);
        myProjects.setVisible(false);

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

    private void onWorkspaceButtonClicked() {
        myWorkspacesButton.setVisible(false);
        var workspaces = myRepository.fetchWorkspaces();
        for (var workspace: workspaces) {
            myWorkspacesModel.addElement((Workspace)workspace);
        }
        myWorkspaceLabel.setVisible(true);
        myWorkspaces.setVisible(true);
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
        var workspace = myWorkspaces.getSelectedItem();
        if (workspace != null)
        {
            myRepository.applyWorkspace(workspace);
        }
        var project = myProjects.getSelectedItem();
        if (project != null)
        {
            myRepository.applyProject(project);
        }

        myRepository.setUseCurrentIteration(myIterationsCheckbox.isSelected());
        if (!myRepository.isUseCurrentIteration()) {
            var iteration = myIterations.getSelectedItem();
            if (iteration != null)
            {
                myRepository.applyIteration(iteration);
            }

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
}
