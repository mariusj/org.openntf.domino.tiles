package org.openntf.tiles.document;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.openntf.domino.Document;
import org.openntf.domino.EmbeddedObject;
import org.openntf.domino.Item;
import org.openntf.domino.MIMEEntity;
import org.openntf.domino.MIMEHeader;
import org.openntf.domino.RichTextItem;

import com.google.common.io.ByteStreams;

/**
 * A wrapper class around attachment embedded in a field on a document. The
 * attachment can be embedded in standard rich text field or as a MIME object.
 *
 * @author Mariusz Jakubowski
 */
public abstract class Attachment {
    
    protected String fileName;

    protected Document document;

    /**
     * Creates a list of attachments extracting files from a field in a
     * document.
     *
     * @param doc
     *            a document where an attachments are stored
     * @param fieldName
     *            a name of a field which contains attachments
     */
    public static List<Attachment> parseAttachments(final Document doc,
            final String fieldName) {
        Item item = doc.getFirstItem(fieldName);
        if (item == null) {
            throw new NullPointerException("no field named " + fieldName
                    + " on doc=" + doc.getUniversalID());
        }
        List<Attachment> ret = new ArrayList<>();
        switch (item.getTypeEx()) {
        case RICHTEXT:
            extractFromRT(doc, (RichTextItem) item, ret);
            break;
        case MIME_PART:
            extractFromMime(doc, item.getMIMEEntity(), ret, 0);
            doc.closeMIMEEntities(false, fieldName);
            break;
        default:
            throw new IllegalArgumentException("wrong type of a field "
                    + fieldName + " on doc=" + doc.getUniversalID());
        }
        return ret;
    }

    /**
     * Extracts an attachment from a given document.
     * @param doc a document with attachment
     * @param fieldName a field where attachment is stored
     * @return an attachment or null if attachment doesn't exists
     */
    public static Attachment parseFirstAttachment(final Document doc, 
            final String fieldName) {
        List<Attachment> att = parseAttachments(doc, fieldName);
        if (!att.isEmpty()) {
            return att.get(0);
        } else {
            return null;
        }
    }

    /**
     * Extracts attachments from an rich text field.
     *
     * @param doc
     *            parent document
     *
     * @param item
     *            a rich text item containing an embedded attachment.
     * @return
     */
    private static void extractFromRT(final Document doc, 
            final RichTextItem item,
            final List<Attachment> ret) {
        Vector<EmbeddedObject> eos = item.getEmbeddedObjects();
        if (eos != null) {
            for (EmbeddedObject embeddedObject : eos) {
                ret.add(new RTAttachment(doc, embeddedObject));
            }
        }
    }

    /**
     * Extracts an attachment from a mime field.
     *
     * @param doc
     *            parent document
     *
     * @param entity
     *            a mime containing an attachment.
     * @return
     */
    private static void extractFromMime(final Document doc, 
            final MIMEEntity entity,
            final List<Attachment> ret, 
            int count) {
        assert entity != null;
        MIMEHeader type = entity.getNthHeader("Content-Type");
        MIMEHeader disposition = entity.getNthHeader("Content-Disposition");
        if (type != null && type.getHeaderVal().contains("multipart")) {
            MIMEEntity child = entity.getFirstChildEntity();
            while (child != null) {
                count++;
                if (count > 10) {
                    return;
                }
                extractFromMime(doc, child, ret, count);
                child = child.getNextEntity();
            }
        } else if (disposition != null
                && disposition.getHeaderVal().contains("attachment")) {
            ret.add(new MimeAttachment(doc, entity));
        }
    }

    protected Attachment(final Document doc) {
        this.document = doc;
    }

    /**
     * Returns a file name of an attachment.
     *
     * @return a file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns contents of a file as a stream.
     *
     * @return stream of bytes
     */
    public abstract InputStream getStream();

    /**
     * Returns length of an attachment in bytes.
     * @return length of an attachment
     */
    public abstract int getSize();

    /**
     * Returns contents of a file encoded in base64 as stream.
     *
     * @return stream with contents of a file encoded in base64
     */
    public Base64InputStream streamBase64() {
        Base64InputStream encIs = new Base64InputStream(getStream(), true);
        return encIs;
    }

    /**
     * Returns contents of a file encoded in base64.
     *
     * @return contents of a file encoded in base64.
     * @throws IOException
     */
    public String asBase64() throws IOException {
        InputStream stream = getStream();
        byte[] buf = ByteStreams.toByteArray(stream);
        stream.close();
        return Base64.encodeBase64String(buf);
    }
    
    /**
     * Returns contents of a file as a byte array.
     * 
     * @return contents of a file
     * @throws IOException
     */
    public byte[] asBytes() throws IOException {
        InputStream stream = getStream();
        byte[] buf = ByteStreams.toByteArray(stream);
        stream.close();
        return buf;
    }

    /**
     * Returns contents of a file as a String.
     * 
     * @return contents of a file
     * @throws IOException
     */
    public String asString() throws IOException {
        byte[] buf = asBytes();
        return new String(buf, "UTF-8");
    }
    

    /**
     * Sends contents of a file to an output stream.
     *
     * @param out
     *            an output stream.
     * @throws IOException
     */
    public void sendTo(final OutputStream out) throws IOException {
        InputStream stream = getStream();
        try {
            int b;
            while ((b = stream.read()) != -1) {
                out.write(b);
            }
        } finally {
            stream.close();
        }
    }

    /**
     * Returns MIME type of a file based on a fileName.
     *
     * @param fileName
     *            name of a file
     * @return MIME type
     */
    public static String guessMIME(final String fileName) {
        String mime = URLConnection.guessContentTypeFromName(fileName);
        if (mime == null) {
            if (fileName.endsWith(".docx")) {
                mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else {
                mime = "application/octet-stream";
            }
        }
        return mime;
    }

    @Override
    public String toString() {
        return "[Attachment " + fileName + ", size=" + getSize() + "]";
    }

}
