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
import hudson.tasks.Builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.vjuliani.jenkins.revisioncontrol.commands.UnixShell;
import com.vjuliani.jenkins.revisioncontrol.commands.WinBatch;
import com.vjuliani.jenkins.revisioncontrol.model.SVNInfoModel;
import com.vjuliani.jenkins.revisioncontrol.service.RevisionHistoryService;

/**
 * @author viniciusj
 * 
 */
public class RevisionControlCheckBuilder extends Builder {

	private String condition;

	private String svnControlAddress;

	private String svnControlUsername;

	private String svnControlPassword;

	private String svnLastRevision;

	private boolean stopJob;

	private RevisionHistoryService revisionHistoryService;

	private AbstractProject<?, ?> project;

	@DataBoundConstructor
	public RevisionControlCheckBuilder(String condition,
			String svnControlAddress, String svnControlUsername,
			String svnControlPassword, String svnLastRevision, boolean stopJob) {
		this.condition = condition;
		this.svnControlAddress = svnControlAddress;
		this.svnControlUsername = svnControlUsername;
		this.svnControlPassword = svnControlPassword;
		setSvnLastRevision(svnLastRevision);
		this.stopJob = stopJob;
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}
	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		String extension = ".sh";
		if (!launcher.isUnix()) {
			extension = ".bat";
		}

		final Map<String, String> envAdd = new HashMap<String, String>();
		envAdd.put("UPDATE_REVISION", "1");
		revisionHistoryService = new RevisionHistoryService();

		FilePath ws = build.getWorkspace(), scriptFile = null;
		ByteArrayOutputStream commandOutputCondition = new ByteArrayOutputStream();
		final String prefix = build.getProject().getName();

		scriptFile = ws.createTextTempFile("check-condition-field-" + prefix,
				extension, condition, false);
		String outputCondition = "true";

		if (!condition.isEmpty()) {
			try {

				launcher.launch().cmds(buildCommandLine(launcher, scriptFile))
						.envs(build.getEnvironment(listener))
						.stderr(listener.getLogger())
						.stdout(commandOutputCondition).pwd(ws).join();

				outputCondition = new String(
						commandOutputCondition.toByteArray());
				listener.getLogger().println(
						"[Output] Execute Check Condition: " + outputCondition);
			} catch (Exception e) {
				listener.error(e.getMessage());
				envAdd.put("STOPJOB", "1");
			}
		}

		if (!("true".equals(outputCondition.toLowerCase().trim()) || "1"
				.equals(outputCondition.trim()))) {
			return true;
		}

		listener.getLogger().println(
				"#######################################################");
		listener.getLogger().println("Checking The Last Delivered Revision...");
		listener.getLogger().println(
				"#######################################################");
		String data = new StringBuffer().append(
				"svn info " + svnControlAddress + " --username "
						+ svnControlUsername + " --password "
						+ svnControlPassword).toString();

		scriptFile = ws.createTextTempFile("svn-info-" + prefix, extension,
				data, false);

		try {
			ByteArrayOutputStream commandOutputSVN = new ByteArrayOutputStream();

			launcher.launch().cmds(buildCommandLine(launcher, scriptFile))
					.envs(build.getEnvironment(listener))
					.stderr(listener.getLogger()).stdout(commandOutputSVN)
					.pwd(ws).join();

			String outputSVN = new String(commandOutputSVN.toByteArray());

			SVNInfoModel svnInfoModel = new SVNInfoModel(outputSVN);
			listener.getLogger().println(
					"[Output] Execute Check SVN Info: " + outputSVN);
			
			boolean diffRevisionHistory = revisionHistoryService.isRevisionHistory(svnInfoModel.getURL(),
					svnInfoModel.getLastChangeRev());
			
			listener.getLogger().println(
					"#######################################################");

			if (diffRevisionHistory) {
				envAdd.put("STOPJOB", "0");
				if (stopJob) {
					
					envAdd.put("STOPJOB", "1");
					envAdd.put("UPDATE_REVISION", "0");
					build.setResult(Result.FAILURE);
					build.getExecutor().interrupt(Result.FAILURE);
					build.doStop();
					listener.error("THE LAST DELIVERED REVISION IS DIFFERENT. PLEASE EXECUTE THE CODE MERGE AND UPDATE REVISION IN JOB CONFIGURATION.");
					return false;
				} else {
					envAdd.put("STOPJOB", "0");
					envAdd.put("UPDATE_REVISION", "0");
					build.setResult(Result.UNSTABLE);
				}
			}
		} catch (Exception e) {
			listener.error(e.getMessage());
		}
		return true;
	}

//	/**
//	 * The setUp method is used to execute the pre build.
//	 * 
//	 * If poll is enabled, the build is only executed if the scm has changes. If
//	 * wait is enabled, the build waits until the pre build is finished. Using
//	 * wait blocks the current executor until the pre build is finished! When
//	 * wait is enabled and the pre build fails, also this build fails.
//	 * 
//	 * @throws IOException
//	 */
//	public Environment setUp(AbstractBuild build, Launcher launcher,
//			BuildListener listener) throws InterruptedException, IOException {
//
//
////		return new Environment() {
////			@Override
////			public void buildEnvVars(Map<String, String> env) {
////				EnvVars envVars = new EnvVars(env);
////				envVars.putAll(envAdd);
////			}
////		};
//		return null;
//	}
	

	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
		
		public DescriptorImpl() {
			load();
		}
		
		@Override
		public boolean configure(StaplerRequest req, JSONObject json)
				throws hudson.model.Descriptor.FormException {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Execute SCM Revision Control Task (SVN Only)";
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> jobType) {
			return true;
		}
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

	/**
	 * @return the svnLastRevision
	 */
	public String getSvnLastRevision() {
		 RevisionHistoryService revisionHistoryService = new RevisionHistoryService();
		 return revisionHistoryService.getLastRevision(getSvnControlAddress());
	}

	/**
	 * @param svnLastRevision
	 *            the svnLastRevision to set
	 */
	public void setSvnLastRevision(String svnLastRevision) {

		RevisionHistoryService revisionHistoryService = new RevisionHistoryService();
		revisionHistoryService.updateRevisionHistory(getSvnControlAddress(),
		svnLastRevision);
		
		this.svnLastRevision = svnLastRevision;
	}

	/**
	 * @return the stopJob
	 */
	public boolean isStopJob() {
		return stopJob;
	}

	/**
	 * @param stopJob
	 *            the stopJob to set
	 */
	public void setStopJob(boolean stopJob) {
		this.stopJob = stopJob;
	}

	/**
	 * @return the project
	 */
	public AbstractProject<?, ?> getProject() {
		return project;
	}

	/**
	 * @param project
	 *            the project to set
	 */
	public void setProject(AbstractProject<?, ?> project) {
		this.project = project;
	}

	/**
	 * @return the revisionHistoryService
	 */
	public RevisionHistoryService getRevisionHistoryService() {
		return revisionHistoryService;
	}

	/**
	 * @param revisionHistoryService
	 *            the revisionHistoryService to set
	 */
	public void setRevisionHistoryService(
			RevisionHistoryService revisionHistoryService) {
		this.revisionHistoryService = revisionHistoryService;
	}
}