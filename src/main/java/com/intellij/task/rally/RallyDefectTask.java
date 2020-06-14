package com.intellij.task.rally;

import com.intellij.task.rally.models.Defect;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

public class RallyDefectTask extends Task {

    private Defect defect;
    private Icon icon;

    public RallyDefectTask(Defect defect)
    {
        this.defect = defect;
        icon =  new ImageIcon(this.getClass().getClassLoader().getResource("rally_defect_P3.png"), "Rally Defect Icon");
    }

    @Override
    public @NotNull String getId() {
        return defect.FormattedID;
    }

    @Override
    public @NotNull String getSummary() {
        return defect.Name;
    }

    @Override
    public @Nullable String getDescription() {
        return null;
    }

    @Override
    public @NotNull Comment[] getComments() {
        return new Comment[0];
    }

    @Override
    public @NotNull Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.BUG;
    }

    @Override
    public @Nullable Date getUpdated() {
        return null;
    }

    @Override
    public @Nullable Date getCreated() {
        return null;
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
        return defect._ref;
    }
}
