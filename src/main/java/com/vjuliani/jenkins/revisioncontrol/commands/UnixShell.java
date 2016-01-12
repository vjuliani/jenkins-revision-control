/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.commands;

import hudson.FilePath;
import hudson.tasks.Shell;

import java.util.ArrayList;
import java.util.List;

import jenkins.model.Jenkins;

/**
 * @author viniciusj
 * 
 */
public class UnixShell {

	public static String[] buildCommandLine(FilePath scriptFile) {
		Shell.DescriptorImpl shellDescriptor = Jenkins.getInstance().getDescriptorByType(Shell.DescriptorImpl.class);
		final String shell = shellDescriptor.getShellOrDefault(scriptFile.getChannel());

		List<String> cml = new ArrayList<String>();
		cml.add(shell);
		cml.add("-xe");
		cml.add(scriptFile.getRemote());

		return (String[]) cml.toArray(new String[cml.size()]);
	}

}