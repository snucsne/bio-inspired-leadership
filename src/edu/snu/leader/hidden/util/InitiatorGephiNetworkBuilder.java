/*
 *  The Bio-inspired Leadership Toolkit is a set of tools used to
 *  simulate the emergence of leaders in multi-agent systems.
 *  Copyright (C) 2014 Southern Nazarene University
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.snu.leader.hidden.util;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.preview.SVGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.plugin.scale.ScaleLayout;
import org.gephi.preview.api.ManagedRenderer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractColorTransformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.EigenvectorCentrality;
import org.openide.util.Lookup;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * InitiatorGephiNetworkBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class InitiatorGephiNetworkBuilder
{
    public static void main( String[] args )
    {
        // Get the input and output files
        String networkDefFile = args[0];
        String outputFile = args[1];

        // Initialize the project and workspace
        ProjectController projController = Lookup.getDefault().lookup(
                ProjectController.class  );
        projController.newProject();
        Workspace workspace = projController.getCurrentWorkspace();

        // Get the controllers and models
        ImportController importController = Lookup.getDefault().lookup(
                ImportController.class );
        GraphModel graphModel = Lookup.getDefault().lookup(
                GraphController.class ).getModel();
        AttributeModel attributeModel = Lookup.getDefault().lookup(
                AttributeController.class ).getModel();

        //Import file
        Container container;
        try
        {
            File file = new File( networkDefFile );
            if( !file.exists() )
            {
                throw new FileNotFoundException( "File ["
                        + networkDefFile
                        + "] was not found" );
            }

            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(
                    EdgeDefault.DIRECTED ); //Force DIRECTED
        }
        catch( Exception ex )
        {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process( container,
                new DefaultProcessor(),
                workspace );

        // Calculate the eigenvector centrality
        EigenvectorCentrality eigenCentrality = new EigenvectorCentrality();
        eigenCentrality.setDirected( true );
        eigenCentrality.execute( graphModel, attributeModel );

        // Rank size by eigenvector centrality
        RankingController rankingController = Lookup.getDefault().lookup(
                RankingController.class );
        AttributeColumn ecColumn = attributeModel.getNodeTable().getColumn(
                EigenvectorCentrality.EIGENVECTOR );
        Ranking ecRanking = rankingController.getModel().getRanking(
                Ranking.NODE_ELEMENT,
                ecColumn.getId() );
        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer)
                rankingController.getModel().getTransformer(
                        Ranking.NODE_ELEMENT,
                        Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(5);
        sizeTransformer.setMaxSize(15);
        rankingController.transform( ecRanking, sizeTransformer );

        // Rank color by initiator status
        AttributeColumn initiatorColumn = attributeModel.getNodeTable().getColumn(
                "node0" );
        Ranking initiatorRanking = rankingController.getModel().getRanking(
                Ranking.NODE_ELEMENT,
                initiatorColumn.getId() );
        AbstractColorTransformer colorTransformer = (AbstractColorTransformer)
                rankingController.getModel().getTransformer(
                        Ranking.NODE_ELEMENT,
                        Transformer.RENDERABLE_COLOR );
        colorTransformer.setColors( new Color[] {
                new Color( 0xFFAA00 ),
                new Color( 0xFFFFFF ),
                new Color( 0x0000FF ) } );
        colorTransformer.setColorPositions( new float[] {
                0.0f,
                0.5f,
                1.0f } );
        rankingController.transform( initiatorRanking, colorTransformer );

        // Scale it up
        ScaleLayout scaleLayout = new ScaleLayout( new Expand(), 55.0f );
        scaleLayout.setGraphModel( graphModel );
        scaleLayout.initAlgo();
        if( scaleLayout.canAlgo() )
        {
            scaleLayout.goAlgo();
        }

        // Change the thickness and coloring of the edges
        PreviewModel previewModel = Lookup.getDefault().lookup(
                PreviewController.class ).getModel();
        previewModel.getProperties().putValue(
                PreviewProperty.EDGE_THICKNESS,
                10.0f );
        previewModel.getProperties().putValue(
                PreviewProperty.EDGE_COLOR,
                new EdgeColor( EdgeColor.Mode.MIXED ) );
        previewModel.getProperties().putValue(
                PreviewProperty.EDGE_CURVED,
                false );
        previewModel.getProperties().putValue(
                PreviewProperty.ARROW_SIZE,
                5.0 );

        ManagedRenderer[] renderers = previewModel.getManagedRenderers();
//        for( int i = 0; i < renderers.length; i++ )
//        {
//            System.out.println( "i=["
//                    + i
//                    + "] name=["
//                    + renderers[i].getRenderer().getDisplayName()
//                    + "]" );
//        }

        renderers[0] = new ManagedRenderer( new HiddenNodeRenderer(), true );

//        // Add the node one last
//        ManagedRenderer[] myRenderers = new ManagedRenderer[ renderers.length + 1 ];
//        System.arraycopy( renderers, 0, myRenderers, 0, renderers.length );
//        myRenderers[renderers.length] = renderers[0];
//
//        for( int i = 0; i < myRenderers.length; i++ )
//        {
//            System.out.println( "i=["
//                    + i
//                    + "] name=["
//                    + myRenderers[i].getRenderer().getDisplayName()
//                    + "]" );
//        }
//
        previewModel.setManagedRenderers( renderers );

        // Export the network
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        SVGExporter svgExporter = (SVGExporter) ec.getExporter( "svg" );
        PNGExporter pngExporter = (PNGExporter) ec.getExporter( "png" );
        pngExporter.setHeight( 720 );
        pngExporter.setWidth( 1280 );
        pngExporter.setMargin( 40 );
        pngExporter.setTransparentBackground( false );
        try
        {
            ec.exportFile( new File( outputFile ), svgExporter );
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
            return;
        }
    }
}
