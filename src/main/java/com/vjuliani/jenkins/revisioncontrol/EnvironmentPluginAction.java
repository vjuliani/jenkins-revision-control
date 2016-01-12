/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol;

import hudson.EnvVars;
import hudson.model.EnvironmentContributingAction;
import hudson.model.AbstractBuild;

import java.util.Map;

/**
 * @author viniciusj
 * 
 */
public class EnvironmentPluginAction implements EnvironmentContributingAction {

	private transient Map<String, String> envAdd;

	public EnvironmentPluginAction(Map<String, String> envAdd) {
		this.envAdd = envAdd;
	}

	public String getIconFileName() {
		return null;
	}

	public String getDisplayName() {
		return null;
	}

	public String getUrlName() {
		return "EnvironmentPluginAction";
	}

	public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {

		if (env == null) {
			return;
		}

		if (envAdd != null) {
			env.putAll(envAdd);
		}

	}

}
