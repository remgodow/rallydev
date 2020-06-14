package com.intellij.task.rally;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.intellij.task.rally.models.*;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.util.QueryFilter;
import org.sbelei.rally.domain.constants.DefectState;
import org.sbelei.rally.helpers.QueryFilterBuilder;
import org.sbelei.rally.helpers.QueryRequestDecorator;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.sbelei.rally.helpers.FilterHelper.*;

public class RallyObjectsProvider {
    private RallyRestApi restApi;

    public RallyObjectsProvider(RallyRestApi restApi) {
        this.restApi = restApi;
    }

    public List<Workspace> getWorkspaces() throws IOException {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.WORKSPACE);
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        return deserializeWorkspaces(response.getResults());
    }

    public List<Project> getProjects(long workspaceId) throws IOException {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.PROJECT);
        request.setWorkspace(String.valueOf(workspaceId));
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        return deserializeProjects(response.getResults());
    }

    public List<Iteration> getIterations(long workspaceId, long projectId) throws IOException {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.ITERATION);
        request.setWorkspace(String.valueOf(workspaceId));
        var filters = new QueryFilterBuilder();
        filters.add(byProjectId(String.valueOf(projectId)));
        request.andFilter(filters.buildQuery());
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        return deserializeIterations(response.getResults());
    }

    public Iteration getCurrentIteration(long workspaceId, long projectId) throws IOException {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.ITERATION);
        request.setWorkspace(String.valueOf(workspaceId));
        var filters = new QueryFilterBuilder();
        filters.add(byProjectId(String.valueOf(projectId)));
        var date = new Date();
        QueryFilter startDateFilter = new QueryFilter("StartDate","<=",queryDate(date));
        QueryFilter endDateFilter = new QueryFilter("EndDate",">",queryDate(date));
        startDateFilter.and(endDateFilter);
        filters.add(startDateFilter);
        request.andFilter(filters.buildQuery());
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        var iterations =  deserializeIterations(response.getResults());
        if ((iterations == null) || (iterations.size()<1)){
            return null;
        } else {
            return iterations.get(0);
        }
    }

    public List<HierarchicalRequirement> getStories(long workspaceId, long projectId, long iterationId) throws Exception
    {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.STORY);
        request.setWorkspace(String.valueOf(workspaceId));
        var filters = new QueryFilterBuilder();
        filters.add(byProjectId(String.valueOf(projectId)));
        filters.add(byIterationId(iterationIdToString(workspaceId, projectId, iterationId)));
        request.andFilter(filters.buildQuery());
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        return deserializeStories(response.getResults());
    }

    public List<Defect> getDefects(long workspaceId, long projectId, long iterationId) throws Exception
    {
        QueryRequestDecorator request = new QueryRequestDecorator(org.sbelei.rally.domain.constants.Type.DEFECT);
        request.setWorkspace(String.valueOf(workspaceId));
        var filters = new QueryFilterBuilder();
        filters.add(byProjectId(String.valueOf(projectId)));
        filters.add(byIterationId(iterationIdToString(workspaceId, projectId, iterationId)));
        filters.add(includeByStates(DefectState.Submitted, DefectState.Open, DefectState.Reopened));
        request.andFilter(filters.buildQuery());
        var apiRequest =  request.getRequest();
        var response = restApi.query(apiRequest);
        return deserializeDefects(response.getResults());
    }

    private List<Workspace> deserializeWorkspaces(JsonArray response)
    {
        RuntimeTypeAdapterFactory<RallyObject> rallyAdapterFactory = RuntimeTypeAdapterFactory
                .of(RallyObject.class, "_type")
                .registerSubtype(Workspace.class, "Workspace");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rallyAdapterFactory).create();
        Type pType = new TypeToken<ArrayList<Workspace>>(){}.getType();
        List<Workspace> rallyObjects = gson.fromJson(response, pType);
        return rallyObjects;
    }

    private List<Project> deserializeProjects(JsonArray response)
    {
        RuntimeTypeAdapterFactory<RallyObject> rallyAdapterFactory = RuntimeTypeAdapterFactory
                .of(RallyObject.class, "_type")
                .registerSubtype(Workspace.class, "Project");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rallyAdapterFactory).create();
        Type pType = new TypeToken<ArrayList<Project>>(){}.getType();
        List<Project> rallyObjects = gson.fromJson(response, pType);
        return rallyObjects;
    }

    private List<Iteration> deserializeIterations(JsonArray response)
    {
        RuntimeTypeAdapterFactory<RallyObject> rallyAdapterFactory = RuntimeTypeAdapterFactory
                .of(RallyObject.class, "_type")
                .registerSubtype(Workspace.class, "Iteration");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rallyAdapterFactory).create();
        Type pType = new TypeToken<ArrayList<Iteration>>(){}.getType();
        List<Iteration> rallyObjects = gson.fromJson(response, pType);
        return rallyObjects;
    }

    private List<HierarchicalRequirement> deserializeStories(JsonArray response)
    {
        RuntimeTypeAdapterFactory<RallyObject> rallyAdapterFactory = RuntimeTypeAdapterFactory
                .of(RallyObject.class, "_type")
                .registerSubtype(Workspace.class, "Story");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rallyAdapterFactory).create();
        Type pType = new TypeToken<ArrayList<HierarchicalRequirement>>(){}.getType();
        List<HierarchicalRequirement> rallyObjects = gson.fromJson(response, pType);
        return rallyObjects;
    }

    private List<Defect> deserializeDefects(JsonArray response)
    {
        RuntimeTypeAdapterFactory<RallyObject> rallyAdapterFactory = RuntimeTypeAdapterFactory
                .of(RallyObject.class, "_type")
                .registerSubtype(Workspace.class, "Defect");
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(rallyAdapterFactory).create();
        Type pType = new TypeToken<ArrayList<Defect>>(){}.getType();
        List<Defect> rallyObjects = gson.fromJson(response, pType);
        return rallyObjects;
    }

    private String iterationIdToString(long workspaceId, long projectId, long iterationId) throws Exception {
        //use current iteration
        if (iterationId == -1)
        {
            var iteration = getCurrentIteration(workspaceId, projectId);
            if (iteration == null)
            {
                throw new Exception("Could not get current iteration");
            }
            else
            {
                return String.valueOf(iteration.ObjectID);
            }
        }
        //unscheduled
        else if (iterationId == -2)
        {
            return "null";
        }
        else
        {
            return String.valueOf(iterationId);
        }
    }
}
