package com.rallydev.intellij

import com.intellij.tasks.TaskRepository
import com.intellij.tasks.impl.BaseRepositoryType
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import javax.swing.Icon
import javax.swing.ImageIcon

class RallyRepositoryType extends BaseRepositoryType<RallyRepository> {
    static final Icon ICON = loadIcon()

    private static ImageIcon loadIcon() {
        try {
            new ImageIcon(RallyRepositoryType.classLoader.getResource("rally.png"), "Rally Icon")
        } catch (Exception e) {
            return null
        }
    }

    @Override
    @NotNull
    String getName() {
        return 'Rally'
    }

    @Override
    @Nullable
    Icon getIcon() {
        return ICON
    }

    @Override
    @NotNull
    TaskRepository createRepository() {
        return new RallyRepository(this)
    }

    @Override
    Class<RallyRepository> getRepositoryClass() {
        return RallyRepository
    }

}