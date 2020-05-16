package org.sbelei.rally.domain.constants;

public enum StoryState {
	
	Defined,
	In_Progress,
	Completed,
	Accepted,
	Tested;
	
	@Override
	public String toString(){
		return name().replaceAll("_", "-");
	}

}
