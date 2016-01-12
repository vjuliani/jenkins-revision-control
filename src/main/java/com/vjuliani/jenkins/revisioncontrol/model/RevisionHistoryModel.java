/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.model;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author viniciusj
 * 
 */
public class RevisionHistoryModel {

	private String scmId;

	private String scmLastRevision;

	public RevisionHistoryModel() {
	}

	@DataBoundConstructor
	public RevisionHistoryModel(final String scmId, final String scmLastRevision) {
		this.setScmId(scmId);
		this.setScmLastRevision(scmLastRevision);
	}

	/**
	 * @return the scmId
	 */
    public String getScmId() {
	    return scmId;
    }

	/**
	 * @param scmId the scmId to set
	 */
    public void setScmId(String scmId) {
	    this.scmId = scmId;
    }

	/**
	 * @return the scmLastRevision
	 */
    public String getScmLastRevision() {
	    return scmLastRevision;
    }

	/**
	 * @param scmLastRevision the scmLastRevision to set
	 */
    public void setScmLastRevision(String scmLastRevision) {
	    this.scmLastRevision = scmLastRevision;
    }
}
