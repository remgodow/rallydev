package com.intellij.task.rally;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.Task;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Tag;
import com.rallydev.rest.RallyRestApi;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jetbrains.annotations.Nullable;
import org.sbelei.rally.domain.BasicEntity;
import org.sbelei.rally.domain.Iteration;
import org.sbelei.rally.domain.Project;
import org.sbelei.rally.domain.Workspace;
import org.sbelei.rally.provider.ProviderFasade;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Tag("Rally")
public class RallyRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance("#com.intellij.tasks.rally.RallyRepository");


    private String workspaceId;
    private String projectId;
    private String iterationId;
    private boolean useCurrentIteration;
    private boolean showCompletedTasks;
    private boolean showOnlyMine;

    private RallyRestApi client;
    private ProviderFasade provider;



    @SuppressWarnings("unused")
    public RallyRepository() {
        super();
        setUrl("https://rally1.rallydev.com");
    }

    public RallyRepository(RallyRepositoryType type) {
        super(type);
        setUrl("https://rally1.rallydev.com");
    }

    public RallyRepository(RallyRepository rallyRepository) {
        super(rallyRepository);
        workspaceId = rallyRepository.getWorkspaceId();
        projectId = rallyRepository.getProjectId();
        iterationId = rallyRepository.getIterationId();
        useCurrentIteration = rallyRepository.isUseCurrentIteration();
        showCompletedTasks = rallyRepository.isShowCompletedTasks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RallyRepository)) return false;
        if (!super.equals(o)) return false;

        RallyRepository that = (RallyRepository) o;

        if (useCurrentIteration != that.useCurrentIteration) return false;
        if (showCompletedTasks != that.showCompletedTasks) return false;
        if (!Objects.equals(iterationId, that.iterationId)) return false;
        if (!Objects.equals(projectId, that.projectId)) return false;
        if (!Objects.equals(workspaceId, that.workspaceId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = workspaceId != null ? workspaceId.hashCode() : 0;
        result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
        result = 31 * result + (useCurrentIteration ? 1 : 0);
        result = 31 * result + (iterationId != null ? iterationId.hashCode() : 0);
        return result;
    }


    private HttpClient createHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);
        return HttpClients.custom().setConnectionManager(cm).build();
    }

    @Override
    public boolean isConfigured() {
        return super.isConfigured();
    }

    @Override
    public Task[] getIssues(@Nullable String query, int max, long since) throws Exception {
        if (provider == null) {
            refreshProvider();
        }

        Task[] result = null;
        try {
            Collection<BasicEntity> rallyTasks = provider.fetchStoriesAndDefects();
            result = new Task[rallyTasks.size()];
            int i = 0;
            for (BasicEntity entity : rallyTasks) {
                Task task = new RallyTask(entity);
                result[i] = task;
                i++;
            }
        } catch (Exception e) {
            LOG.error("Can\'t fetch rally issues", e);
        }

        return result;
    }

    private void refreshProvider() {
        if (provider == null) {
            try {
                URI uri = new URI(getUrl());
                client = new RallyRestApi(
                        uri,
                        myUsername,
                        myPassword,
                        createHttpClient()
                );
                provider = new ProviderFasade(client);
                provider.setUserLogin(myUsername);

            } catch (URISyntaxException uie) {
                LOG.error("Wrong URL", uie);
            }
        }
        if (provider != null) {
            provider.setUseCurrentIteration(useCurrentIteration);
            provider.showAll(showCompletedTasks);
            provider.setOnlyMine(showOnlyMine);

            provider.setWorkspaceId(workspaceId);
            provider.setProjectId(projectId);
            provider.setIterationId(iterationId);
        } else {
            LOG.error("Provider is not initialized properly");
        }
    }

    @Nullable
    @Override
    public Task findTask(String id) throws Exception {
        return null;
    }

    @Override
    public void testConnection() throws Exception {
        refreshProvider();
        provider.fetchWorkspaces().toArray();
    }


    @Override
    public BaseRepository clone() {
        return new RallyRepository(this);
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /*
    Helper methods to work with filters
     */

    public List<Workspace> fetchWorkspaces() throws IOException {
        refreshProvider();
        try {
            return provider.fetchWorkspaces();
        } catch (Exception e) {
            LOG.warn("Error while fetching workspaces",e);
            throw e;
        }
    }

    public void applyWorkspace(Object selectedItem) {
        String backupedId = workspaceId;
        try {
            workspaceId = ((Workspace) selectedItem).id;
        } catch (Exception e) {
            LOG.warn("Error while saving workspace number, restored previous value.",e);
            workspaceId = backupedId;
        }
    }

    public List<Project> fetchProjects() {
        refreshProvider();
        try {
            return provider.fetchProjects();
        } catch (Exception e) {
            LOG.warn("Error while fetching projects",e);
            return null;
        }
    }

    public void applyProject(Object selectedItem) {
        String backupedId = projectId;
        try {
            projectId = ((Project) selectedItem).id;
        } catch (Exception e) {
            LOG.warn("Error while saving workspace number, restored previous value.",e);
            projectId = backupedId;
        }
    }

    public List<Iteration> fetchIterations() {
        refreshProvider();
        try {
            return provider.fetchIterations();
        } catch (Exception e) {
            LOG.warn("Error while fetching iterations",e);
            return null;
        }
    }

    public boolean isUseCurrentIteration() {
        return useCurrentIteration;
    }

    public void setUseCurrentIteration(boolean useCurrentIteration) {
        this.useCurrentIteration = useCurrentIteration;
    }

    public String getIterationId() {
        return iterationId;
    }

    public void setIterationId(String iterationId) {
        this.iterationId = iterationId;
    }

    public void applyIteration(Object selectedItem) {
        String backupedId = iterationId;
        try {
            iterationId = ((Iteration) selectedItem).id;
        } catch (Exception e) {
            LOG.warn("Error while saving iteration number, restored previous value.",e);
            iterationId = backupedId;
            useCurrentIteration = true;
        }
    }

    public boolean isShowCompletedTasks() {
        return showCompletedTasks;
    }

    public void setShowCompletedTasks(boolean showCompletedTasks) {
        this.showCompletedTasks = showCompletedTasks;
    }

    public boolean isShowOnlyMine() {
        return showOnlyMine;
    }

    public void setShowOnlyMine(boolean showOnlyMine) {
        this.showOnlyMine = showOnlyMine;
    }
}
