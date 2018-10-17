package org.openntf.tiles.document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.openntf.domino.Document;
import org.openntf.domino.DocumentCollection;
import org.openntf.domino.EmbeddedObject;
import org.openntf.domino.Item;
import org.openntf.domino.Item.Type;
import org.openntf.domino.MIMEEntity;
import org.openntf.domino.MIMEHeader;
import org.openntf.domino.RichTextItem;
import org.openntf.domino.Session;
import org.openntf.domino.Stream;


/**
 * Utilities for working with documents.
 *
 * @author Mariusz Jakubowski
 *
 */
public class DocumentUtils {

    /**
     * Removes all items from a document with given name.
     *
     * @param doc
     *            a document from where remove an item
     * @param fieldName
     *            name of an item
     */
    public static void removeItem(final Document doc, final String fieldName) {
        Item item = doc.getFirstItem(fieldName);
        while (item != null) {
            item.remove();
            item = doc.getFirstItem(fieldName);
        }
    }

    /**
     * Saves an attachment in a MIME field. If that field already exists and is
     * in mime format, the attachment is appended to that field. Otherwise a new
     * field is created.
     *
     * @param doc
     *            a document where to add an attachment
     * @param fieldName
     *            a field where to put an attachment
     * @param bytes
     *            an attachment content
     * @param fileName
     *            a file name of an attachment
     */
    public static void addMIMEAttachment(final Document doc, 
            final String fieldName, 
            final byte[] bytes, 
            final String fileName) {
        Session session = doc.getParentDatabase().getParent();
        Stream stream = session.createStream();
        stream.write(bytes);
        addMIMEAttachment(doc, fieldName, stream, fileName);
    }

    /**
     * Saves an attachment in a MIME field. If that field already exists and is
     * in mime format, the attachment is appended to that field. Otherwise a new
     * field is created.
     *
     * @param doc
     *            a document where to add an attachment
     * @param fieldName
     *            a field where to put an attachment
     * @param stream
     *            a stream of bytes
     * @param fileName
     *            a file name of an attachment
     */
    public static void addMIMEAttachment(final Document doc, 
            final String fieldName, 
            final InputStream is, 
            final String fileName) {
        Session session = doc.getParentDatabase().getParent();
        Stream stream = session.createStream();
        stream.setContents(is);
        addMIMEAttachment(doc, fieldName, stream, fileName);
    }


    /**
     * Saves an attachment in a MIME field. If that field already exists and is
     * in mime format, the attachment is appended to that field. Otherwise a new
     * field is created.
     *
     * @param doc
     *            a document where to add an attachment
     * @param fieldName
     *            a field where to put an attachment
     * @param stream
     *            an attachment content
     * @param fileName
     *            a file name of an attachment
     */
    public static void addMIMEAttachment(final Document doc, 
            final String fieldName, 
            final Stream stream, 
            final String fileName) {
        Item item = doc.getFirstItem(fieldName);
        MIMEEntity body = null;
        if (item != null) {
            if (item.getTypeEx() == Type.MIME_PART) {
                body = item.getMIMEEntity();
            }
        }
        if (body == null) {
            body = doc.createMIMEEntity(fieldName);
            MIMEHeader header = body.createHeader("Content-Type");
            header.setHeaderVal("multipart/mixed");
        }

        MIMEEntity att = body.createChildEntity();
        att.setContentFromBytes(stream, Attachment.guessMIME(fileName), MIMEEntity.ENC_IDENTITY_BINARY);
        MIMEHeader header = att.createHeader("Content-Disposition");
        String fileNameB;
        try {
            fileNameB = "=?UTF-8?B?" + Base64.encodeBase64String(fileName.getBytes("UTF-8")) + "?=";
        } catch (UnsupportedEncodingException e) {
            fileNameB = fileName;
            e.printStackTrace();
        }
        header.setHeaderVal("attachment; filename=\"" + fileNameB + "\"");
        header = att.createHeader("Content-ID");
        header.setHeaderVal(fileName);
        doc.closeMIMEEntities(true, fieldName);
        stream.close();
    }

    /**
     * Saves an attachment in a rich text field.
     *
     * @param doc
     *            a document where to add an attachment
     * @param fieldName
     *            a field where to put an attachment
     * @param file
     *            contents of an attachment
     */
    public static void addRTAttachment(final Document doc, 
            final String fieldName, 
            final File file) {
        Item item = doc.getFirstItem(fieldName);
        if (item != null) {
            if (!(item instanceof RichTextItem)) {
                String oldVal = item.getValueString();
                item.remove();
                item = doc.createRichTextItem(fieldName);
                ((RichTextItem) item).appendText(oldVal);
            }
        } else {
            item = doc.createRichTextItem(fieldName);
        }
        RichTextItem rt = (RichTextItem) item;
        try {
            rt.embedObject(EmbeddedObject.EMBED_ATTACHMENT, "", file.getCanonicalPath(), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes document and all its responses.
     */
    public static void deleteRecursively(final Document doc) {
        DocumentCollection responses = doc.getResponses();
        if (responses != null) {
            responses.removeAll(true);
        }
        doc.remove(true);
    }

    /**
     * Sets a value fValue on field fName on the doc document and all its
     * children.
     *
     * @param doc
     *            document to stamp
     * @param stampDoc
     *            if true stamps the document and all its children, if false
     *            stamps only children of this document
     * @param fName
     *            name of a field to update
     * @param fValue
     *            value to change to
     */
    public static void stampAllChildren(final Document doc, 
            final boolean stampDoc, 
            final String fName, 
            final String fValue) {
        DocumentCollection resp = doc.getResponses();
        resp.stampAll(fName, fValue);
        for (Document child : resp) {
            stampAllChildren(child, false, fName, fValue);
        }
        if (stampDoc) {
            doc.replaceItemValue(fName, fValue);
            doc.save(true);
        }
    }

}
