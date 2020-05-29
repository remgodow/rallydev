package com.intellij.task.rally;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.impl.TaskUiUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sbelei.rally.domain.Iteration;
import org.sbelei.rally.domain.Workspace;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

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
        myProjects.setRenderer(SimpleListCellRenderer.create("Set user and token first", org.sbelei.rally.domain.Project::toString));
        myProjects.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                myRepository.setProject((org.sbelei.rally.domain.Project) e.getItem());
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
                myRepository.setIteration((Iteration) e.getItem());
            }
        });
        installListener(myIterations);
        myIterationLabel = new JBLabel("Iteration:", SwingConstants.RIGHT);
        fb.addLabeledComponent(myIterationLabel, myIterations);

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

        myRepository.setUseCurrentIteration(myIterationsCheckbox.isSelected());
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

    private class FetchProjectsTask extends TaskUiUtil.ComboBoxUpdater<org.sbelei.rally.domain.Project> {
        private FetchProjectsTask() {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Projects...", myProjects);
        }

        @Override
        public org.sbelei.rally.domain.Project getExtraItem() {
            return null;
        }

        @Nullable
        @Override
        public org.sbelei.rally.domain.Project getSelectedItem() {
            return myRepository.getProject();
        }

        @NotNull
        @Override
        protected List<org.sbelei.rally.domain.Project> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.fetchProjects();
        }
    }

    private class FetchIterationsTask extends TaskUiUtil.ComboBoxUpdater<Iteration> {
        private FetchIterationsTask() {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Iterations...", myIterations);
        }

        @Override
        public Iteration getExtraItem() {
            return null;
        }

        @Nullable
        @Override
        public Iteration getSelectedItem() {
            return myRepository.getIteration();
        }

        @NotNull
        @Override
        protected List<Iteration> fetch(@NotNull ProgressIndicator indicator) throws Exception {
            return myRepository.fetchIterations();
        }
    }
}
