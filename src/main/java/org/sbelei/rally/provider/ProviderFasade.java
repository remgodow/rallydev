package org.sbelei.rally.provider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sbelei.rally.domain.*;

import com.rallydev.rest.RallyRestApi;

public class ProviderFasade {
	
	private RallyRestApi restApi;
	private String workspaceId;
	private String projectId;
	private boolean showOnlyMine;
	private boolean showAll;
	private String iterationId;
	private String userLogin;
	
	public ProviderFasade(RallyRestApi restApi) {
		this.restApi = restApi;
	}
	
	public List<Workspace> fetchWorkspaces() throws IOException {
		WorkspaceProvider wprovider = new WorkspaceProvider(restApi);
		return wprovider.fetch();
	}

	public List<Project> fetchProjects() throws IOException {
		ProjectProvider pprovider = new ProjectProvider(restApi, workspaceId);
		return pprovider.fetch();
	}
	
	public List<Iteration> fetchIterations() throws IOException {
		IterationProvider iprovider = new IterationProvider(restApi,workspaceId,projectId);
		return iprovider.fetch();		
	}
	
	public void setWorkspaceId(String id) {
		workspaceId = id;		
	}

	public void setProjectId(String id) {
		projectId = id;		
	}

	public void setIterationId(String id) { iterationId = id; }

	public BasicEntity fetchCurrentIteration() throws IOException {
		IterationProvider iprovider = new IterationProvider(restApi,workspaceId,projectId);
		return iprovider.fetchCurrentIteration();		
	}

	public void setOnlyMine(boolean yes) {
		showOnlyMine = yes;
	}

	/**
	 * if true show all tasks.
	 * if false show only not completed tasks.
	 * @param yes
	 */
	public void showAll(boolean yes) {
		showAll = yes;		
	}

	public List<BasicEntity> fetchStoriesAndDefects() throws IOException {
		List<BasicEntity> tasks = new ArrayList<BasicEntity>();
		var iteration = iterationId;
		if (iterationId == "-1") {
			iteration = fetchCurrentIteration().id;
		}
		DefectsProvider dprovider = new DefectsProvider(restApi, workspaceId, projectId, iteration);
		StoryProvider sprovider = new StoryProvider(restApi, workspaceId, projectId, iteration);
		
		if (showOnlyMine) {
			dprovider.setUserLogin(userLogin);
			sprovider.setUserLogin(userLogin);
			dprovider.onlyMine();
			sprovider.onlyMine();
		}
		List<Defect> defects;
		List<Story> stories;
		if (showAll) {
			defects = dprovider.fetchNotClosed();
			stories = sprovider.fetch();
		} else {
			defects = dprovider.fetch();
			stories = sprovider.fetch();
		}	

		tasks.addAll(defects);
		tasks.addAll(stories);

		return tasks;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

}
