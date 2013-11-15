package me.solhub.simple.engine;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class InputHandler implements KeyListener
{

    private boolean[] keys = new boolean[256];

    public InputHandler( Component c )
    {
        c.addKeyListener( this );
    }

    /**
     * Checks whether a specific key is down
     * 
     * @param keyCode The key to check
     * @return Whether the key is pressed or not
     */
    public boolean isKeyDown( int keyCode )
    {
        if( keyCode > 0 && keyCode < 256 )
        {
            return keys[keyCode];
        }
        return false;
    }

    @Override
    public void keyPressed( KeyEvent e )
    {
        if( e.getKeyCode() > 0 && e.getKeyCode() < 256 )
        {
            keys[e.getKeyCode()] = true;
        }
    }

    @Override
    public void keyReleased( KeyEvent e )
    {
        if( e.getKeyCode() > 0 && e.getKeyCode() < 256 )
        {
            keys[e.getKeyCode()] = false;
        }
    }

    @Override
    public void keyTyped( KeyEvent e )
    {
        // TODO Auto-generated method stub

    }

}
