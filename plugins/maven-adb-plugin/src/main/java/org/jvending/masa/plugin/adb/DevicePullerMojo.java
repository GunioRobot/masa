/*
 * Copyright (C) 2007-2008 JVending Masa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvending.masa.plugin.adb;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jvending.masa.CommandExecutor;
import org.jvending.masa.ExecutionException;
import org.jvending.masa.MasaUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @goal pull
 * @requiresProject false
 * @description
 */
public final class DevicePullerMojo
    extends AbstractMojo
{

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     */
    public MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    public MavenSession session;

    /**
     * @parameter expression="${source}"
     * @required
     */
    private File sourceFileOrDirectory;

    /**
     * @parameter expression="${destination}"
     * @required
     */
    private File destinationFileOrDirectory;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger( this.getLog() );

        List<String> commands = new ArrayList<String>();
        commands.add( "pull" );
        commands.add( sourceFileOrDirectory.getAbsolutePath() );
        commands.add( destinationFileOrDirectory.getAbsolutePath() );

        getLog().info( "adb " + commands.toString() );
        try
        {
            executor.executeCommand( MasaUtil.getToolnameWithPath( session, project, "adb" ), commands );
        }
        catch ( ExecutionException e )
        {
        }
    }
}
