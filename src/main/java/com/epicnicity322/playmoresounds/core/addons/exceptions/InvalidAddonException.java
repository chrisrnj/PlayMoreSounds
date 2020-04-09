package com.epicnicity322.playmoresounds.core.addons.exceptions;

public class InvalidAddonException extends Exception
{
    public InvalidAddonException()
    {
    }

    public InvalidAddonException(String message)
    {
        super(message);
    }

    public InvalidAddonException(String message, Exception ex)
    {
        super(message, ex);
    }

    public InvalidAddonException(Exception ex)
    {
        super(ex);
    }
}
