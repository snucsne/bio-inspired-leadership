package me.solhub.simple.engine;

import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;


/**
 * This is an abstract class to help structure a simple 2D game. Override how
 * you want to initialize your game, update the game at every frame, draw every
 * frame, and check to see if the game is finished. Then simply call the run
 * method from your subclass.
 * 
 * @author Tim Solum
 */
public abstract class AbstractGameStructure extends JFrame
{
    private static final long serialVersionUID = 1L;

    private int _fps = 30;

    private String _title = null;

    protected int _windowWidth = 0;

    protected int _windowHeight = 0;

    protected Insets _insets = null;

    protected BufferedImage _backBuffer = null;

    public AbstractGameStructure( String title,
            int windowWidth,
            int windowHeight,
            int fps )
    {
        _title = title;
        _windowWidth = windowWidth;
        _windowHeight = windowHeight;
        _fps = fps;

        setTitle( _title );
        setSize( _windowWidth, _windowHeight );
        setResizable( false );
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        setVisible( true );

        _insets = getInsets();
        setSize( _insets.left + _windowWidth + _insets.right, _insets.top
                + _windowHeight + _insets.bottom );

        _backBuffer = new BufferedImage( _windowWidth, _windowHeight,
                BufferedImage.TYPE_INT_RGB );
    }

    /**
     * For simple games this should not need changing. It structures the game as
     * such update() draw() delay for frame rate
     */
    public final void run()
    {
        initialize();

        while( isRunning() )
        {
            long time = System.currentTimeMillis();

            update();
            draw();

            time = ( 1000 / _fps ) - ( System.currentTimeMillis() - time );

            if( time > 0 )
            {
                try
                {
                    Thread.sleep( time );
                }
                catch( Exception e )
                {

                }
            }
        }

        setVisible( false );
    }

    /**
     * Initializes the game
     */
    protected abstract void initialize();

    /**
     * Updates the game every frame. Does game logic and checking for input
     */
    protected abstract void update();

    /**
     * Draws the game elements every frame
     */
    protected abstract void draw();

    /**
     * The method that checks to see if the game is still running
     * 
     * @return Whether or not the game is running
     */
    protected abstract boolean isRunning();
}
