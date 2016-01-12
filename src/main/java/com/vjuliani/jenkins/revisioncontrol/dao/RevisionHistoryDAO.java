/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.dao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import jenkins.model.Jenkins;

import com.vjuliani.jenkins.revisioncontrol.model.RevisionHistoryListModel;

/**
 * @author viniciusj
 * 
 */
public class RevisionHistoryDAO {

	public RevisionHistoryListModel getOrCreateObject() {
		try {
			final String path = getFilePath();
			return (RevisionHistoryListModel) Jenkins.XSTREAM.fromXML(new FileInputStream(path));
		} catch (final Exception e) {
			RevisionHistoryListModel revisionHistoryListModel = new RevisionHistoryListModel();
			save(revisionHistoryListModel);
			return revisionHistoryListModel;
		}
	}

	public void save(RevisionHistoryListModel object) {
		try {
			Jenkins.XSTREAM.toXML(object, getFile());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private String getFilePath() {
		return Jenkins.getInstance().getRootDir().toPath() + "/jobs" + System.getProperty("file.separator", "/") + "revisionHistory.xml";
	}

	private OutputStreamWriter getFile() throws Exception {
		FileOutputStream outputStream = new FileOutputStream(getFilePath());
		return new OutputStreamWriter(outputStream, "UTF-8");
	}
}
