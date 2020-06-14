package com.intellij.task.rally;

import com.intellij.task.rally.models.HierarchicalRequirement;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

public class RallyStoryTask extends Task {

    private HierarchicalRequirement rallyStory;
    private Icon icon;

    public RallyStoryTask(HierarchicalRequirement story)
    {
        rallyStory = story;
        icon =  new ImageIcon(this.getClass().getClassLoader().getResource("rally_feature.png"), "Rally Feature Icon");
    }

    @Override
    public @NotNull String getId() {
        return rallyStory.FormattedID;
    }

    @Override
    public @NotNull String getSummary() {
        return rallyStory.Name;
    }

    @Override
    public @Nullable String getDescription() {
        return rallyStory.Description;
    }

    @Override
    public @NotNull Comment[] getComments() {
        return new Comment[0];
    }

    @Override
    public @NotNull Icon getIcon() {
        return icon;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.FEATURE;
    }

    @Override
    public @Nullable Date getUpdated() {
        return rallyStory.LastUpdateDate;
    }

    @Override
    public @Nullable Date getCreated() {
        return rallyStory.CreationDate;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isIssue() {
        return true;
    }

    @Override
    public @Nullable String getIssueUrl() {
        return rallyStory._ref;
    }
}
