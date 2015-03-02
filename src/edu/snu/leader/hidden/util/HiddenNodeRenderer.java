/*
 * COPYRIGHT
 */
package edu.snu.leader.hidden.util;

import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;

/**
 * HiddenNodeRenderer
 *
 * TODO Class description
 *
 * @author Brent Eskridge
 * @version $Revision$ ($Author$)
 */
@ServiceProvider(service=Renderer.class, position=1)
public class HiddenNodeRenderer extends NodeRenderer
{

    @Override
    public String getDisplayName()
    {
        return "Hidden node renderer";
    }

    @Override
    public void render( Item item,
            RenderTarget target,
            PreviewProperties properties )
    {
        if( !isHidden( item ) )
        {
            super.render( item, target, properties );
        }
    }

    @Override
    public void renderPDF( Item item, PDFTarget target, PreviewProperties properties )
    {
        if( !isHidden( item ) )
        {
            super.renderPDF( item, target, properties );
        }
    }

    @Override
    public void renderProcessing( Item item,
            ProcessingTarget target,
            PreviewProperties properties )
    {
        if( !isHidden( item ) )
        {
            super.renderProcessing( item, target, properties );
        }
    }

    @Override
    public void renderSVG( Item item,
            SVGTarget target,
            PreviewProperties properties )
    {
        if( !isHidden( item ) )
        {
            super.renderSVG( item, target, properties );
        }
    }


    private boolean isHidden( Item item )
    {
        boolean hidden = false;

        if( item.getType().equals( Item.NODE ) )
        {
            Node source = (Node) item.getSource();
            NodeData sourceData = source.getNodeData();
            String label = sourceData.getLabel();
            if( (null == label) || (label.equals( "" ) ) )
            {
                hidden = true;
            }
        }

        return hidden;
    }
}
