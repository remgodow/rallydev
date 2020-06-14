package com.intellij.task.rally;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.task.rally.models.HierarchicalRequirement;
import com.intellij.task.rally.models.Iteration;
import com.intellij.task.rally.models.Project;
import com.intellij.task.rally.models.Workspace;
import com.intellij.tasks.Task;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Tag;
import com.rallydev.rest.RallyRestApi;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@Tag("Rally")
public class RallyRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance("#com.intellij.tasks.rally.RallyRepository");
    public static final Iteration CURRENT_ITERATION = getCurrentIteration();
    public static final Iteration UNSCHEDULED = getUnscheduledIteration();
    private Workspace workspace;
    private Project project;
    private Iteration iteration;

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public void setIteration(Iteration iteration) {
        this.iteration = iteration != null && iteration.ObjectID == -1 ? CURRENT_ITERATION : iteration;
    }

    private boolean showCompletedTasks;
    private boolean showOnlyMine;

    private RallyRestApi client;
    private RallyObjectsProvider rallyProvider;



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
        workspace = rallyRepository.workspace;
        project = rallyRepository.project;
        iteration = rallyRepository.iteration;
        showCompletedTasks = rallyRepository.isShowCompletedTasks();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RallyRepository)) return false;
        if (!super.equals(o)) return false;

        RallyRepository that = (RallyRepository) o;

        if (showCompletedTasks != that.showCompletedTasks) return false;
        if (!Objects.equals(workspace, that.workspace)) return false;
        if (!Objects.equals(project, that.project)) return false;
        if (!Objects.equals(iteration, that.iteration)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = workspace != null ? workspace.hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (iteration != null ? iteration.hashCode() : 0);
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
        return super.isConfigured() && !myUsername.isEmpty() && !myPassword.isEmpty();
    }

    @Override
    public Task[] getIssues(@Nullable String query, int max, long since) throws Exception {
        if (rallyProvider == null) {
            refreshProvider();
        }

        Task[] result;
        var stories = rallyProvider.getStories(workspace.ObjectID, project.ObjectID, iteration.ObjectID);
        result = new Task[stories.size()];
        int i = 0;
        for (HierarchicalRequirement entity : stories) {
            Task task = new RallyStoryTask(entity);
            result[i] = task;
            i++;
        }

        return result;
    }

    private void refreshProvider() {
        if (rallyProvider == null) {
            try {
                URI uri = new URI(getUrl());
                client = new RallyRestApi(
                        uri,
                        myUsername,
                        myPassword,
                        createHttpClient()
                );
                rallyProvider = new RallyObjectsProvider(client);

            } catch (URISyntaxException uie) {
                LOG.error("Wrong URL", uie);
            }
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
        fetchWorkspaces().toArray();
    }


    @Override
    public BaseRepository clone() {
        return new RallyRepository(this);
    }

    /*
    Helper methods to work with filters
     */

    public List<Workspace> fetchWorkspaces() throws IOException {
        refreshProvider();
        try {
            return rallyProvider.getWorkspaces();
        } catch (Exception e) {
            LOG.warn("Error while fetching workspaces",e);
            throw e;
        }
    }

    public List<Project> fetchProjects() {
        refreshProvider();
        try {
            return rallyProvider.getProjects(workspace.ObjectID);
        } catch (Exception e) {
            LOG.warn("Error while fetching projects",e);
            return null;
        }
    }

    public List<Iteration> fetchIterations() {
        refreshProvider();
        try {
            return rallyProvider.getIterations(workspace.ObjectID, project.ObjectID);
        } catch (Exception e) {
            LOG.warn("Error while fetching iterations",e);
            return null;
        }
    }

    private static Iteration getCurrentIteration() {
        var iteration = new Iteration();
        iteration.ObjectID = -1;
        iteration.Name = "Use current iteration";
        return iteration;
    }

    private static Iteration getUnscheduledIteration() {
        var iteration = new Iteration();
        iteration.ObjectID = -2;
        iteration.Name = "Unscheduled";
        return iteration;
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

