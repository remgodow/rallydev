package org.sbelei.rally.provider;

import org.sbelei.rally.domain.Workspace;
import org.sbelei.rally.domain.constants.Type;

import com.rallydev.rest.RallyRestApi;

import java.io.IOException;
import java.util.List;

public class WorkspaceProvider extends EntityProvider<Workspace> {

	public WorkspaceProvider(RallyRestApi restApi) {
		super(restApi, null, null);
	}

	@Override
	String getType() {
		return Type.WORKSPACE;
	}

	@Override
	public Workspace newEntity() {
		return new Workspace();
	}

	public List<Workspace> getWorkspaces() throws IOException {
		return fetch(null);
	}
}
