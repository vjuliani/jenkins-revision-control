/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.model;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author viniciusj
 * 
 */
public class RevisionHistoryListModel {

	private List<RevisionHistoryModel> revisionHistoryList = new ArrayList<RevisionHistoryModel>();

	@DataBoundConstructor
	public RevisionHistoryListModel() {
	}

	/**
	 * @return the revisionHistoryList
	 */
	public List<RevisionHistoryModel> getRevisionHistoryList() {
		return this.revisionHistoryList;
	}

	/**
	 * 
	 */
	public void addRevisionHistoryList(RevisionHistoryModel revisionHistory) {
		this.revisionHistoryList.add(revisionHistory);
	}
	
	/**
	 * 
	 */
	public void removeRevisionHistoryList(RevisionHistoryModel revisionHistory) {
		this.revisionHistoryList.remove(revisionHistory);
	}
}
