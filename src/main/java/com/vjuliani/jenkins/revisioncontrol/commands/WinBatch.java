/**
 * 
 */
package com.vjuliani.jenkins.revisioncontrol.commands;

import hudson.FilePath;

import java.util.ArrayList;
import java.util.List;

/**
 * @author viniciusj
 * 
 */
public class WinBatch {

	// Mostly stolen from managed-scripts-plugin
	public static String[] buildCommandLine(FilePath scriptFile) {
		List<String> cml = new ArrayList<String>();
		cml.add("cmd");
		cml.add("/c");
		cml.add("call");
		cml.add(scriptFile.getRemote());

		return (String[]) cml.toArray(new String[cml.size()]);
	}

}
