/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.util;

// Imports
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.scale.Expand;
import org.gephi.layout.plugin.scale.ScaleLayout;
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
import org.gephi.statistics.plugin.Degree;
import org.openide.util.Lookup;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

/**
 * GephiNetworkBuilder
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
public class GephiNetworkBuilder
{

    /**
     * TODO Method description
     *
     * @param args
     */
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

        // Calculate the out-degree
        Degree degree = new Degree();
        degree.execute( graphModel, attributeModel );

        // Rank size by out degree
        RankingController rankingController = Lookup.getDefault().lookup(
                RankingController.class );
        AttributeColumn mimickingColumn = attributeModel.getNodeTable().getColumn(
                "node4" );
        Ranking mimickingRanking = rankingController.getModel().getRanking(
                Ranking.NODE_ELEMENT,
                mimickingColumn.getId() );
//        Ranking outDegreeRanking = rankingController.getModel().getRanking(
//                Ranking.NODE_ELEMENT,
//                Ranking.OUTDEGREE_RANKING );
        AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer)
                rankingController.getModel().getTransformer(
                        Ranking.NODE_ELEMENT,
                        Transformer.RENDERABLE_SIZE);
        sizeTransformer.setMinSize(5);
        sizeTransformer.setMaxSize(20);
        rankingController.transform( mimickingRanking, sizeTransformer );

//        // Get all the columns
//        AttributeColumn[] allColumns = attributeModel.getNodeTable().getColumns();
//        for( int i = 0; i < allColumns.length; i++ )
//        {
//            System.out.println( "Attr: id=["
//                    + allColumns[i].getId()
//                    + "] index=["
//                    + allColumns[i].getIndex()
//                    + "] title=["
//                    + allColumns[i].getTitle()
//                    + "]" );
//        }

        // Rank color by success
        AttributeColumn successColumn = attributeModel.getNodeTable().getColumn(
                "node1" );
        Ranking successRanking = rankingController.getModel().getRanking(
                Ranking.NODE_ELEMENT,
                successColumn.getId() );
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
        rankingController.transform( successRanking, colorTransformer );


        // Scale it up
        ScaleLayout scaleLayout = new ScaleLayout( new Expand(), 45.0f );
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

        // Export the network
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try
        {
            ec.exportFile( new File( outputFile ) );
        }
        catch( IOException ex )
        {
            ex.printStackTrace();
            return;
        }
    }

}
