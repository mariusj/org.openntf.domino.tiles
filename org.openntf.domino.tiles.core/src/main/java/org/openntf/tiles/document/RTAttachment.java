package org.openntf.tiles.document;

import java.io.InputStream;

import org.openntf.domino.Document;
import org.openntf.domino.EmbeddedObject;

/**
 * A wrapper around attachment. Attachment is stored in Rich Text field.
 *
 * @author Mariusz Jakubowski
 *
 */
public class RTAttachment extends Attachment {

    private EmbeddedObject embeddedObject;
    private int size;

    public RTAttachment(final Document doc, final EmbeddedObject embeddedObject) {
        super(doc);
        this.embeddedObject = embeddedObject;
        fileName = embeddedObject.getName();
        size = embeddedObject.getFileSize();
    }

    @Override
    public InputStream getStream() {
        return embeddedObject.getInputStream();
    }

    @Override
    public int getSize() {
        return size;
    }

}
