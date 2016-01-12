/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.service;

import java.util.Iterator;

import com.vjuliani.jenkins.revisioncontrol.dao.RevisionHistoryDAO;
import com.vjuliani.jenkins.revisioncontrol.model.RevisionHistoryListModel;
import com.vjuliani.jenkins.revisioncontrol.model.RevisionHistoryModel;

/**
 * @author viniciusj
 * 
 */
public class RevisionHistoryService {

	private RevisionHistoryDAO revisionHistoryDAO = new RevisionHistoryDAO();

	/**
	 * 
	 * @param StringscmURL
	 * @param scmRevision
	 */
	public void updateRevisionHistory(String StringscmURL, String scmRevision) {
		RevisionHistoryListModel revisionList = new RevisionHistoryListModel();
		revisionList = revisionHistoryDAO.getOrCreateObject();

		Iterator<RevisionHistoryModel> it = revisionList
				.getRevisionHistoryList().iterator();
		while (it.hasNext()) {
			RevisionHistoryModel revision = it.next();
			if (StringscmURL.equalsIgnoreCase(revision.getScmId())) {
				it.remove();
			}
		}

		RevisionHistoryModel newRevision = new RevisionHistoryModel();
		newRevision.setScmId(StringscmURL);
		newRevision.setScmLastRevision(scmRevision);
		revisionList.addRevisionHistoryList(newRevision);

		revisionHistoryDAO.save(revisionList);
	}

	/**
	 * 
	 * @param StringscmURL
	 * @param scmRevision
	 * @return
	 */
	public boolean isRevisionHistory(String StringScmURL, String scmRevision) {
		RevisionHistoryListModel revisionList = revisionHistoryDAO
				.getOrCreateObject();
		for (RevisionHistoryModel revision : revisionList
				.getRevisionHistoryList()) {

			if (StringScmURL.trim().equals(revision.getScmId().trim())
					&& !scmRevision.trim().equals(
							revision.getScmLastRevision().trim())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 
	 * @param StringScmURL
	 * @return
	 */
	public String getLastRevision(String StringScmURL) {
		
		RevisionHistoryListModel revisionList = revisionHistoryDAO
				.getOrCreateObject();

		for (RevisionHistoryModel revision : revisionList
				.getRevisionHistoryList()) {
			if (revision.getScmId().equals(StringScmURL)) {
				return revision.getScmLastRevision();
			}
		}

		return "";
	}
}
