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
package org.jvending.masa.plugins.toolchains;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.model.PersistedToolchains;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.apache.maven.toolchain.model.io.xpp3.MavenToolchainsXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @goal toolchain
 * @phase validate
 */
public class ToolchainMojo
    extends AbstractMojo
{

    /**
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter
     */
    private Toolchains toolchains;

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private File toolchainsFile;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if(toolchains == null)
        {
            return;
        }
   
        if(toolchainsFile == null) {
        	//Check if toolchains.xml exists within project directory first
        	toolchainsFile = new File(  project.getBasedir(), "toolchains.xml" );
        	//Check if toolchains.xml exists within local .m2 directory
        	if(!toolchainsFile.exists()) {
        		 toolchainsFile =
                     new File( new File( session.getLocalRepository().getBasedir() ).getParentFile(), "toolchains.xml" );
        
        	}
        } 

        if ( !toolchainsFile.exists() )
        {
            this.getLog().info( "Not toolchains.xml file found." );
        }

        PersistedToolchains toolchainModels = null;
        Reader in = null;
        try
        {
            in = ReaderFactory.newXmlReader( toolchainsFile );
            toolchainModels = new MavenToolchainsXpp3Reader().read( in );
        }
        catch ( Exception e )
        {
            return;
        }
        finally
        {
            IOUtil.close( in );
        }

        Map<String, ToolchainModel> models = new HashMap<String, ToolchainModel>();

        // Capabilities
        List<List<Capability>> m = new ArrayList<List<Capability>>();
        for ( ToolchainModel model : (List<ToolchainModel>) toolchainModels.getToolchains() )
        {
            if ( !model.getType().equals( "android" ) )
            {
                continue;
            }

            List<Capability> c = new ArrayList<Capability>();
            Xpp3Dom dom = (Xpp3Dom) model.getProvides();
            for ( Xpp3Dom child : dom.getChildren() )
            {
                if ( child.getName().equals( "id" ) )
                {
                    models.put( child.getValue(), model );
                }
                c.add( new Capability( child.getName(), child.getValue() ) );
            }
            m.add( c );
        }
        Matcher matcher = new Matcher( m );

        // Requirements from pom
        String capabilityId = matcher.findMatchIdFor( toolchains.android );

        if ( capabilityId == null )
        {
            throw new MojoExecutionException( "Could not match capability to toolchain requirements" );
        }

        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        pluginDescriptor.setGroupId( "org.jvending.masa.plugins" );
        pluginDescriptor.setArtifactId( PluginDescriptor.getDefaultPluginArtifactId( "toolchains" ) );
        session.getPluginContext( pluginDescriptor, project ).put( "toolchain", models.get( capabilityId ) );
        System.out.println( "ID=" + capabilityId + ":" + models.get( capabilityId ).getType() );

    }

}