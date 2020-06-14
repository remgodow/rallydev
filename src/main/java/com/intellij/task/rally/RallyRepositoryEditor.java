package com.intellij.task.rally;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.task.rally.models.Iteration;
import com.intellij.task.rally.models.Project;
import com.intellij.task.rally.models.Workspace;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.impl.TaskUiUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.task.rally.RallyRepository.CURRENT_ITERATION;
import static com.intellij.task.rally.RallyRepository.UNSCHEDULED;

public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

    private JBLabel myWorkspaceLabel;
    private ComboBox<Workspace> myWorkspaces;

    private JBLabel myProjectLabel;
    private ComboBox<Project> myProjects;

    private JBLabel myIterationLabel;
    private ComboBox<Iteration> myIterations;

    private JCheckBox myShowCompletedCheckbox;
    private JCheckBox myShowOnlyMineCheckbox;

    public RallyRepositoryEditor(com.intellij.openapi.project.Project project, RallyRepository repository, Consumer<RallyRepository> changeListener) {
        super(project, repository, changeListener);
        myPasswordLabel.setText("API Token:");
        myURLText.setEnabled(false);
        myURLText.setVisible(false);
        myUrlLabel.setEnabled(false);
        myUrlLabel.setVisible(false);
        myShareUrlCheckBox.setVisible(false);
        myTestButton.setEnabled(myRepository.isConfigured());
        UIUtil.invokeLaterIfNeeded(this::initialize);
    }

    @Override
    @Nullable
    protected JComponent createCustomPanel() {
        FormBuilder fb = FormBuilder.createFormBuilder();

        myWorkspaces = new ComboBox<>(300);
        myWorkspaces.setRenderer(SimpleListCellRenderer.create("Set user and token first", Workspace::toString));

        myWorkspaces.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                new FetchProjectsTask().queue();
            }
        });

        installListener(myWorkspaces);
        myWorkspaceLabel = new JBLabel("Workspace:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myWorkspaceLabel, myWorkspaces);

        myProjects = new ComboBox<>(300);
        myProjects.setRenderer(SimpleListCellRenderer.create("Set user and token first", Project::toString));
        myProjects.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                myRepository.setProject((com.intellij.task.rally.models.Project) e.getItem());
                new FetchIterationsTask().queue();
            }
        });

        installListener(myProjects);
        myProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myProjectLabel, myProjects);

        myIterations = new ComboBox<>(300);
        myIterations.setRenderer(SimpleListCellRenderer.create("Set user and token first", Iteration::toString));
        myIterations.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                myRepository.setIteration((com.intellij.task.rally.models.Iteration) e.getItem());
            }
        });
        installListener(myIterations);
        myIterationLabel = new JBLabel("Iteration:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myIterationLabel, myIterations);

        myShowCompletedCheckbox = new JCheckBox("Show completed tasks");
        myShowCompletedCheckbox.setSelected(myRepository.isShowCompletedTasks());
        fb.addComponent(myShowCompletedCheckbox);
        installListener(myShowCompletedCheckbox);

        myShowOnlyMineCheckbox = new JCheckBox("Only user's tasks");
        myShowOnlyMineCheckbox.setSelected(myRepository.isShowOnlyMine());
        fb.addComponent(myShowOnlyMineCheckbox);
        installListener(myShowOnlyMineCheckbox);

        return fb.getPanel();
    }

    private void initialize() {
        final Workspace workspace = myRepository.getWorkspace();
        if (workspace != null && myRepository.isConfigured()) {
            new FetchWorkspacesTask().queue();
        }
    }

    @Override
    public void apply() {
        super.apply();
        myRepository.setWorkspace((Workspace) myWorkspaces.getSelectedItem());

        myRepository.setShowCompletedTasks(myShowCompletedCheckbox.isSelected());
        myRepository.setShowOnlyMine(myShowOnlyMineCheckbox.isSelected());

        myTestButton.setEnabled(myRepository.isConfigured());
    }

    @Override
    protected void afterTestConnection(boolean connectionSuccessful) {
        super.afterTestConnection(connectionSuccessful);
        if (connectionSuccessful) {
            new FetchWorkspacesTask().queue();
        }
    }

    private class FetchWorkspacesTask extends TaskUiUtil.ComboBoxUpdater<Workspace> {
        private FetchWorkspacesTask() {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Workspaces...", myWorkspaces);
        }

        @Override
        public Workspace getExtraItem() {
            return null;
        }

        @Nullable
        @Override
        public Workspace getSelectedItem() {
            return myRepository.getWorkspace();
        }

        @NotNull
        @Override
        protected List<Workspace> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.fetchWorkspaces();
        }
    }

    private class FetchProjectsTask extends TaskUiUtil.ComboBoxUpdater<Project> {
        private FetchProjectsTask() {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Projects...", myProjects);
        }

        @Override
        public Project getExtraItem() {
            return null;
        }

        @Nullable
        @Override
        public Project getSelectedItem() {
            return myRepository.getProject();
        }

        @NotNull
        @Override
        protected List<Project> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.fetchProjects();
        }
    }

    private class FetchIterationsTask extends TaskUiUtil.ComboBoxUpdater<Iteration> {
        private FetchIterationsTask() {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Iterations...", myIterations);
        }

        @Nullable
        @Override
        public Iteration getSelectedItem() {
            return myRepository.getIteration();
        }

        public List<Iteration> getExtraItems() {
            var extras = new ArrayList<Iteration>();
            extras.add(CURRENT_ITERATION);
            extras.add(UNSCHEDULED);
            return extras;
        }

        @Override
        protected void updateUI() {
            if (myResult != null) {
                //noinspection unchecked
                myComboBox.setModel(new DefaultComboBoxModel(ArrayUtil.toObjectArray(myResult)));
                final List<Iteration> extras = getExtraItems();
                if (extras != null) {
                    for (int i = 0; i < extras.size() ; i++) {
                        myComboBox.insertItemAt(extras.get(i), i);
                    }
                }
                // ensure that selected ItemEvent will be fired, even if first item of the model
                // is the same as the next selected
                myComboBox.setSelectedItem(null);

                final Iteration selected = getSelectedItem();
                if (selected != null) {
                    if (!extras.contains(selected) && !myResult.contains(selected)) {
                        if (addSelectedItemIfMissing()) {
                            myComboBox.addItem(selected);
                            myComboBox.setSelectedItem(selected);
                        }
                        else {
                            if (myComboBox.getItemCount() > 0) {
                                myComboBox.setSelectedIndex(0);
                            }
                        }
                    }
                    else {
                        myComboBox.setSelectedItem(selected);
                    }
                }
                else {
                    if (myComboBox.getItemCount() > 0) {
                        myComboBox.setSelectedIndex(0);
                    }
                }
            }
            else {
                handleError();
            }
        }

        @NotNull
        @Override
        protected List<Iteration> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.fetchIterations();
        }
    }
}
