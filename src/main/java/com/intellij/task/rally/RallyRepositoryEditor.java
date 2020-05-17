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
import java.util.List;

public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

    private JButton myWorkspacesButton;
    private JBLabel myWorkspaceLabel;
    private ComboBox<Workspace> myWorkspaces;

    private JBLabel myProjectLabel;
    private ComboBox<org.sbelei.rally.domain.Project> myProjects;

    private JBLabel myIterationLabel;
    private ComboBox<Iteration> myIterations;
    private JCheckBox myIterationsCheckbox;
    private JCheckBox myShowCompletedCheckbox;
    private JCheckBox myShowOnlyMineCheckbox;

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

        myWorkspaces = new ComboBox<Workspace>();
        myWorkspaces.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                var projects = myRepository.fetchProjects();
                myProjects.removeAllItems();
                for (var project: projects) {
                    myProjects.addItem(project);
                }
                selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
                myProjectLabel.setVisible(true);
                myProjects.setVisible(true);
            }
        });

        installListener(myWorkspaces);
        myWorkspaceLabel = new JBLabel("Workspace:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myWorkspaceLabel, myWorkspaces);
        myWorkspaceLabel.setVisible(false);
        myWorkspaces.setVisible(false);

        myProjects = new ComboBox<org.sbelei.rally.domain.Project>();
        myProjects.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                var iterations = myRepository.fetchIterations();
                myIterations.removeAllItems();
                for (var iteration: iterations) {
                    myIterations.addItem(iteration);
                }
                myIterationLabel.setVisible(true);
                myIterations.setVisible(true);
            }
        });

        installListener(myProjects);
        myProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myProjectLabel, myProjects);
        myProjectLabel.setVisible(false);
        myProjects.setVisible(false);

        myIterations = new ComboBox<Iteration>();
        installListener(myIterations);
        myIterationLabel = new JBLabel("Iteration:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myIterationLabel, myIterations);
        myIterationLabel.setVisible(false);
        myIterations.setVisible(false);

        myIterationsCheckbox = new JCheckBox("Use current iteration");
        myIterationsCheckbox.setSelected(myRepository.isUseCurrentIteration());
        fb.addComponent(myIterationsCheckbox);
        installListener(myIterationsCheckbox);

        myShowCompletedCheckbox = new JCheckBox("Show completed tasks");
        myShowCompletedCheckbox.setSelected(myRepository.isShowCompletedTasks());
        fb.addComponent(myShowCompletedCheckbox);
        installListener(myShowCompletedCheckbox);

        myShowOnlyMineCheckbox = new JCheckBox("Only user's tasks");
        myShowOnlyMineCheckbox.setSelected(myRepository.isShowOnlyMine());
        fb.addComponent(myShowOnlyMineCheckbox);
        installListener(myShowOnlyMineCheckbox);

        if (myRepository.getWorkspaceId() != null)
        {
            myWorkspacesButton.setVisible(false);
            myWorkspaceLabel.setVisible(true);
            myWorkspaces.setVisible(true);
            restoreRepositorySettings();
        }

        return fb.getPanel();
    }

    private void onWorkspaceButtonClicked() {
        myWorkspacesButton.setVisible(false);
        var workspaces = myRepository.fetchWorkspaces();
        myWorkspaces.removeAllItems();
        for (var workspace: workspaces) {
            myWorkspaces.addItem(workspace);
        }
        myWorkspaceLabel.setVisible(true);
        myWorkspaces.setVisible(true);
    }

    private void selectIteration() {
        if (!myRepository.isUseCurrentIteration()) {
            selectByEntityId(myIterations, myRepository.getIterationId());
        }
    }

    private <T extends BasicEntity> void selectByEntityId(JComboBox<T> combo, String id) {
        for (int i=0; i< combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id.equals(id)) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private <T> void loadItemsToCombobox(ComboBox<T> combo, List<T> items) {
        combo.removeAllItems();
        for (T item : items) {
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
        myRepository.setShowCompletedTasks(myShowCompletedCheckbox.isSelected());
        myRepository.setShowOnlyMine(myShowOnlyMineCheckbox.isSelected());
    }

    @Override
    protected void afterTestConnection(boolean connectionSuccessful) {
        super.afterTestConnection(connectionSuccessful);
        if (connectionSuccessful) {
            //load workspaces/projects/iterations
            restoreRepositorySettings();
        }
    }

    private void restoreRepositorySettings() {
        loadItemsToCombobox(myWorkspaces, myRepository.fetchWorkspaces());
        selectByEntityId(myWorkspaces, myRepository.getWorkspaceId());
        loadItemsToCombobox(myProjects, myRepository.fetchProjects());
        selectByEntityId(myProjects, myRepository.getProjectId());
        loadItemsToCombobox(myIterations, myRepository.fetchIterations());
        selectIteration();
    }
}
