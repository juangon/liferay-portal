/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.css.builder.maven;

import com.liferay.css.builder.CSSBuilder;
import com.liferay.css.builder.CSSBuilderArgs;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.component.repository.ComponentDependency;
import org.codehaus.plexus.util.Scanner;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Compiles CSS files.
 *
 * @author Andrea Di Giorgi
 * @author Gregory Amerson
 * @goal build
 */
public class BuildCSSMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException {
		try {
			for (ComponentDependency componentDependency :
					_pluginDescriptor.getDependencies()) {

				String artifactId = componentDependency.getArtifactId();

				if (artifactId.equals("com.liferay.frontend.css.common") &&
					(_cssBuilderArgs.getPortalCommonPath() == null)) {

					Artifact artifact = _resolveArtifact(componentDependency);

					_cssBuilderArgs.setPortalCommonPath(artifact.getFile());
				}
			}

			if (_buildContext.isIncremental()) {
				Scanner scanner = _buildContext.newScanner(_baseDir);

				String[] includes = {"", "**/*.scss"};

				scanner.setIncludes(includes);

				scanner.scan();

				String[] includedFiles = scanner.getIncludedFiles();

				if (ArrayUtil.isNotEmpty(includedFiles)) {
					_execute();
				}
			}
			else {
				_execute();
			}
		}
		catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 * @parameter default-value="true"
	 */
	public void setAppendCssImportTimestamps(
		boolean appendCssImportTimestamps) {

		_cssBuilderArgs.setAppendCssImportTimestamps(appendCssImportTimestamps);
	}

	/**
	 * @parameter
	 */
	public void setDirNames(String dirNames) {
		_cssBuilderArgs.setDirNames(dirNames);
	}

	/**
	 * @parameter default-value="${project.build.directory}/${project.build.finalName}"
	 */
	public void setDocrootDirName(String docrootDirName) {
		File docrootDir = new File(docrootDirName);

		if (!docrootDir.isAbsolute()) {
			docrootDir = new File(_baseDir, docrootDirName);
		}

		_cssBuilderArgs.setDocrootDir(docrootDir);
	}

	/**
	 * @parameter
	 */
	public void setGenerateSourceMap(boolean generateSourceMap) {
		_cssBuilderArgs.setGenerateSourceMap(generateSourceMap);
	}

	/**
	 * @parameter default-value=".sass-cache/"
	 */
	public void setOutputDirName(String outputDirName) {
		_cssBuilderArgs.setOutputDirName(outputDirName);
	}

	/**
	 * @parameter
	 */
	public void setPortalCommonPath(File portalCommonPath) {
		_cssBuilderArgs.setPortalCommonPath(portalCommonPath);
	}

	/**
	 * @parameter
	 */
	public void setPrecision(int precision) {
		_cssBuilderArgs.setPrecision(precision);
	}

	/**
	 * @parameter
	 */
	public void setRtlExcludedPathRegexps(String rtlExcludedPathRegexps) {
		_cssBuilderArgs.setRtlExcludedPathRegexps(rtlExcludedPathRegexps);
	}

	/**
	 * @parameter
	 */
	public void setSassCompilerClassName(String sassCompilerClassName) {
		_cssBuilderArgs.setSassCompilerClassName(sassCompilerClassName);
	}

	private void _execute() throws Exception {
		try (CSSBuilder cssBuilder = new CSSBuilder(_cssBuilderArgs)) {
			cssBuilder.execute();
		}
	}

	private Artifact _resolveArtifact(ComponentDependency componentDependency)
		throws ArtifactResolutionException {

		Artifact artifact = new DefaultArtifact(
			componentDependency.getGroupId(),
			componentDependency.getArtifactId(), componentDependency.getType(),
			componentDependency.getVersion());

		ArtifactRequest artifactRequest = new ArtifactRequest();

		artifactRequest.setArtifact(artifact);

		List<RemoteRepository> repositories = new ArrayList<>();

		repositories.addAll(_project.getRemotePluginRepositories());
		repositories.addAll(_project.getRemoteProjectRepositories());

		artifactRequest.setRepositories(repositories);

		ArtifactResult artifactResult = _repositorySystem.resolveArtifact(
			_repositorySystemSession, artifactRequest);

		return artifactResult.getArtifact();
	}

	/**
	 * @parameter default-value="${project.basedir}"
	 * @readonly
	 */
	private File _baseDir;

	/**
	 * @component
	 */
	private BuildContext _buildContext;

	private final CSSBuilderArgs _cssBuilderArgs = new CSSBuilderArgs();

	/**
	 * @parameter default-value="${plugin}"
	 * @readonly
	 * @required
	 */
	private PluginDescriptor _pluginDescriptor;

	/**
	 * @parameter property="project"
	 * @required
	 * @readonly
	 */
	private MavenProject _project;

	/**
	 * @component
	 */
	private RepositorySystem _repositorySystem;

	/**
	 * @parameter property="repositorySystemSession"
	 * @readonly
	 * @required
	 */
	private RepositorySystemSession _repositorySystemSession;

}