/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import java.io.ByteArrayOutputStream;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.vjuliani.jenkins.revisioncontrol.commands.UnixShell;
import com.vjuliani.jenkins.revisioncontrol.commands.WinBatch;
import com.vjuliani.jenkins.revisioncontrol.dao.RevisionHistoryDAO;
import com.vjuliani.jenkins.revisioncontrol.model.SVNInfoModel;
import com.vjuliani.jenkins.revisioncontrol.service.RevisionHistoryService;

/**
 * @author viniciusj
 * 
 */
public class RevisionControlUpdateBuilder extends Recorder {

	private RevisionHistoryDAO revisionHistoryDAO;

	private String condition;

	private String svnControlAddress;

	private String svnControlUsername;

	private String svnControlPassword;

	private RevisionHistoryService revisionHistoryService;
	
	
	@DataBoundConstructor
	public RevisionControlUpdateBuilder(String condition, String svnControlAddress,
			String svnControlUsername, String svnControlPassword) {

		this.condition = condition;
		this.svnControlAddress = svnControlAddress;
		this.svnControlUsername = svnControlUsername;
		this.svnControlPassword = svnControlPassword;


	}

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		
		String stopJob = null;
		String updateRevision = null;
		try {
			stopJob = build.getEnvironment(listener).get("STOPJOB");
			updateRevision = build.getEnvironment(listener).get("UPDATE_REVISION");
		} catch (Exception e) {
			listener.error(e.getMessage());
		}

		if ("1".equals(stopJob) || "0".equals(updateRevision)) {			
			return false;
		}
		
		return true;
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) {
		
		if(build.getResult().equals(Result.FAILURE) || build.getResult().equals(Result.UNSTABLE)) {
			return true;
		}

		String extension = ".sh";
		if (!launcher.isUnix()) {
			extension = ".bat";
		}

		revisionHistoryService = new RevisionHistoryService();

		FilePath ws = build.getWorkspace(), scriptFile = null;
		ByteArrayOutputStream commandOutputCondition = new ByteArrayOutputStream();
		final String prefix = build.getProject().getName();

		try {
			scriptFile = ws.createTextTempFile("update-condition-field-" + prefix,
					extension, condition, false);
		} catch (Exception e) {
			listener.error(e.getMessage());
		}

		String outputCondition = "true";

		if (!condition.isEmpty()) {
			try {

				launcher.launch().cmds(buildCommandLine(launcher, scriptFile))
						.envs(build.getEnvironment(listener))
						.stderr(listener.getLogger()).stdout(commandOutputCondition)
						.pwd(ws).join();

				outputCondition = new String(commandOutputCondition.toByteArray());
				listener.getLogger().println(
						"[Output] Execute Update Condition: " + outputCondition);
			} catch (Exception e) {
				listener.error(e.getMessage());
			}
		}

		if (("true".equals(outputCondition.toLowerCase().trim()) || "1".equals(outputCondition.trim()))) {
			
			listener.getLogger().println(
					"#######################################################");
			listener.getLogger().println(
					"Updating Delivered Revision...");
			listener.getLogger().println(
					"#######################################################");
			
			String data = new StringBuffer().append(
					"svn info " + svnControlAddress + " --username "
							+ svnControlUsername + " --password "
							+ svnControlPassword).toString();

			try {
				ByteArrayOutputStream commandOutputSVN = new ByteArrayOutputStream();
				scriptFile = ws.createTextTempFile("update-svn-info-" + prefix,
						extension, data, false);

				launcher.launch().cmds(buildCommandLine(launcher, scriptFile))
						.envs(build.getEnvironment(listener))
						.stderr(listener.getLogger()).stdout(commandOutputSVN)
						.pwd(ws).join();

				String outputSVN = new String(commandOutputSVN.toByteArray());
				
				listener.getLogger().println(
						"[Output] Execute Update SVN Info: " + outputSVN);

				SVNInfoModel svnInfoModel = new SVNInfoModel(outputSVN);

				revisionHistoryService.updateRevisionHistory(
						svnInfoModel.getURL(), svnInfoModel.getLastChangeRev());

			} catch (Exception e) {
				listener.error(e.getMessage());
			}
			listener.getLogger().println(
					"#######################################################");
		}

		return true;
	}

	//@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		
		

		public DescriptorImpl() {
			super(RevisionControlUpdateBuilder.class);
		}

		@Override
		public String getDisplayName() {
			return "Update Revision Control Task (SVN Only)";
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws FormException {
			return true;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}		
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}

	/**
	 * 
	 * @param launcher
	 * @param scriptFile
	 * @return
	 */
	private String[] buildCommandLine(Launcher launcher, FilePath scriptFile) {

		if (launcher.isUnix()) {
			return UnixShell.buildCommandLine(scriptFile);
		} else {
			return WinBatch.buildCommandLine(scriptFile);
		}
	}

	/**
	 * @return the revisionHistoryDAO
	 */
	public RevisionHistoryDAO getRevisionHistoryDAO() {
		return revisionHistoryDAO;
	}

	/**
	 * @param revisionHistoryDAO
	 *            the revisionHistoryDAO to set
	 */
	public void setRevisionHistoryDAO(RevisionHistoryDAO revisionHistoryDAO) {
		this.revisionHistoryDAO = revisionHistoryDAO;
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * @return the svnControlAddress
	 */
	public String getSvnControlAddress() {
		return svnControlAddress;
	}

	/**
	 * @param svnControlAddress
	 *            the svnControlAddress to set
	 */
	public void setSvnControlAddress(String svnControlAddress) {
		this.svnControlAddress = svnControlAddress;
	}

	/**
	 * @return the svnControlUsername
	 */
	public String getSvnControlUsername() {
		return svnControlUsername;
	}

	/**
	 * @param svnControlUsername
	 *            the svnControlUsername to set
	 */
	public void setSvnControlUsername(String svnControlUsername) {
		this.svnControlUsername = svnControlUsername;
	}

	/**
	 * @return the svnControlPassword
	 */
	public String getSvnControlPassword() {
		return svnControlPassword;
	}

	/**
	 * @param svnControlPassword
	 *            the svnControlPassword to set
	 */
	public void setSvnControlPassword(String svnControlPassword) {
		this.svnControlPassword = svnControlPassword;
	}
}
