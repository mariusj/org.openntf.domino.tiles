package org.openntf.tiles.document;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import org.openntf.domino.Document;
import org.openntf.domino.MIMEEntity;
import org.openntf.domino.MIMEHeader;
import org.openntf.domino.Stream;


/**
 * A wrapper around attachment. Attachment is stored in MIME field.
 * @author Mariusz Jakubowski
 *
 */
public class MimeAttachment extends Attachment {

    private int length;

    private ByteArrayInputStream bais;

    public MimeAttachment(final Document doc, final MIMEEntity entity) {
        super(doc);
        fileName = "bez_nazwy";
        Vector<MIMEHeader> headers = entity.getHeaderObjects();
        for (MIMEHeader header : headers) {
            String val = header.getHeaderValAndParams();
            if (val != null && val.contains("filename=")) {
                int idx = val.indexOf("filename=") + 9;
                fileName = val.substring(idx);
                if (fileName.startsWith("\"")) {
                    fileName = fileName.substring(1, fileName.length() - 1);
                }
            }
        }
        Stream stream = document.getParentDatabase().getParent().createStream();
        entity.getContentAsBytes(stream);
        length = stream.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
        stream.getContents(baos);
        bais = new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public InputStream getStream()  {
        return bais;
    }

    @Override
    public int getSize() {
        return length;
    }

}
