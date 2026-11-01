package model;

import java.io.Serializable;

public class Item implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String myName;
    private final String myDescription;

    public Item(final String theName,
                final String theDescription) {
        myName = theName;
        myDescription = theDescription;
    }

    public String getName() {
        return myName;
    }

    public String getDescription() {
        return myDescription;
    }

    @Override
    public String toString() {
        return myName + ": " + myDescription;
    }
}
