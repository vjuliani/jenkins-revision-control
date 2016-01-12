/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.model;

/**
 * @author viniciusj
 * 
 */
public class SVNInfoModel {

	private String path;
	private String URL;
	private String repositoryRoot;
	private String repositoryUUID;
	private String revision;
	private String nodeKind;
	private String lastChangeAuthor;
	private String lastChangeRev;
	private String lastChangeDate;

	public SVNInfoModel(String svnInfo) {
		createObjectFromString(svnInfo);
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the uRL
	 */
	public String getURL() {
		return URL;
	}

	/**
	 * @param uRL
	 *            the uRL to set
	 */
	public void setURL(String uRL) {
		URL = uRL;
	}

	/**
	 * @return the repositoryRoot
	 */
	public String getRepositoryRoot() {
		return repositoryRoot;
	}

	/**
	 * @param repositoryRoot
	 *            the repositoryRoot to set
	 */
	public void setRepositoryRoot(String repositoryRoot) {
		this.repositoryRoot = repositoryRoot;
	}

	/**
	 * @return the repositoryUUID
	 */
	public String getRepositoryUUID() {
		return repositoryUUID;
	}

	/**
	 * @param repositoryUUID
	 *            the repositoryUUID to set
	 */
	public void setRepositoryUUID(String repositoryUUID) {
		this.repositoryUUID = repositoryUUID;
	}

	/**
	 * @return the revision
	 */
	public String getRevision() {
		return revision;
	}

	/**
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(String revision) {
		this.revision = revision;
	}

	/**
	 * @return the nodeKind
	 */
	public String getNodeKind() {
		return nodeKind;
	}

	/**
	 * @param nodeKind
	 *            the nodeKind to set
	 */
	public void setNodeKind(String nodeKind) {
		this.nodeKind = nodeKind;
	}

	/**
	 * @return the lastChangeAuthor
	 */
	public String getLastChangeAuthor() {
		return lastChangeAuthor;
	}

	/**
	 * @param lastChangeAuthor
	 *            the lastChangeAuthor to set
	 */
	public void setLastChangeAuthor(String lastChangeAuthor) {
		this.lastChangeAuthor = lastChangeAuthor;
	}

	/**
	 * @return the lastChangeRev
	 */
	public String getLastChangeRev() {
		return lastChangeRev;
	}

	/**
	 * @param lastChangeRev
	 *            the lastChangeRev to set
	 */
	public void setLastChangeRev(String lastChangeRev) {
		this.lastChangeRev = lastChangeRev;
	}

	/**
	 * @return the lastChangeDate
	 */
	public String getLastChangeDate() {
		return lastChangeDate;
	}

	/**
	 * @param lastChangeDate
	 *            the lastChangeDate to set
	 */
	public void setLastChangeDate(String lastChangeDate) {
		this.lastChangeDate = lastChangeDate;
	}

	/**
	 * 
	 * @param svnInfo
	 */
	private void createObjectFromString(String svnInfo) {
		String[] svnInfoLines = svnInfo.split("\n");
		int count = 0;
		String value = "";
		for (String info : svnInfoLines) {
			if (count >= 0 && !info.trim().isEmpty()) {
				value = info.split(": ")[1].trim();
				switch (count) {
				case 0:
					setPath(value);
					break;
				case 1:
					setURL(value);
					break;
				case 2:
					setRepositoryRoot(value);
					break;
				case 3:
					setRepositoryUUID(value);
					break;
				case 4:
					setRevision(value);
					break;
				case 5:
					setNodeKind(value);
					break;
				case 6:
					setLastChangeAuthor(value);
					break;
				case 7:
					setLastChangeRev(value);
					break;
				case 8:
					setLastChangeDate(value);
					break;
				}
			}
			count++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SVNInfoModel [path=" + path + ", URL=" + URL + ", repositoryRoot=" + repositoryRoot
		        + ", repositoryUUID=" + repositoryUUID + ", revision=" + revision + ", nodeKind=" + nodeKind
		        + ", lastChangeAuthor=" + lastChangeAuthor + ", lastChangeRev=" + lastChangeRev + ", lastChangeDate="
		        + lastChangeDate + "]";
	}
}
