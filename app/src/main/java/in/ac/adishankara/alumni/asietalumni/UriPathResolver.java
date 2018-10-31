package in.ac.adishankara.alumni.asietalumnip;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class UriPathResolver
{
    private ContentResolver contentResolver;

    public UriPathResolver(ContentResolver contentResolver)
    {
        this.contentResolver = contentResolver;
    }

    private ContentResolver getContentResolver()
    {
        return  contentResolver;
    }

    public String GetRealPath(Context ctx, Uri uri)
    {
        String ret = "";
        if (ctx != null && uri != null)
        {

            if (isContentUri(uri))
            {
                if (isGooglePhotoDoc(uri.getAuthority()))
                    ret = uri.getLastPathSegment();
                else
                    ret = getImageRealPath(getContentResolver(), uri, null);
            }
            else if (isFileUri(uri))
                ret = uri.getPath();
            else if (isDocumentUri(ctx, uri))
            {
                String documentId = DocumentsContract.getDocumentId(uri);
                String uriAuthority = uri.getAuthority();
                if (isMediaDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if (idArr.length == 2)
                    {
                        String docType = idArr[0];
                        String realDocId = idArr[1];
                        Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        if ("image".equals(docType))
                            mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        else if ("video".equals(docType))
                            mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        else if ("audio".equals(docType))
                            mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;
                        ret = getImageRealPath(getContentResolver(), mediaContentUri, whereClause);
                    }
                } else if (isDownloadDoc(uriAuthority))
                {
                    Uri downloadUri = Uri.parse("content://downloads/public_downloads");
                    Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));
                    ret = getImageRealPath(getContentResolver(), downloadUriAppendId, null);

                } else if (isExternalStoreDoc(uriAuthority))
                {
                    String idArr[] = documentId.split(":");
                    if (idArr.length == 2)
                    {
                        String type = idArr[0];
                        String realDocId = idArr[1];
                        if ("primary".equalsIgnoreCase(type))
                            ret = Environment.getExternalStorageDirectory() + "/" + realDocId;
                    }
                }
            }
        }

        return ret;
    }

    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause)
    {
        String ret = "";
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);
        if (cursor != null)
        {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst)
            {
                String columnName = MediaStore.Images.Media.DATA;
                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    columnName = MediaStore.Images.Media.DATA;
                else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                    columnName = MediaStore.Audio.Media.DATA;
                else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    columnName = MediaStore.Video.Media.DATA;
                int imageColumnIndex = cursor.getColumnIndex(columnName);
                ret = cursor.getString(imageColumnIndex);
            }
        }
        return ret;
    }

    private boolean isDocumentUri(Context ctx, Uri uri)
    {
        boolean ret = false;
        if (ctx != null && uri != null)
            ret = DocumentsContract.isDocumentUri(ctx, uri);
        return ret;
    }

    private boolean isContentUri(Uri uri)
    {
        boolean ret = false;
        if (uri != null)
        {
            String uriSchema = uri.getScheme();
            if ("content".equalsIgnoreCase(uriSchema))
                ret = true;
        }
        return ret;
    }

    private boolean isFileUri(Uri uri)
    {
        boolean ret = false;
        if (uri != null)
        {
            String uriSchema = uri.getScheme();
            if ("file".equalsIgnoreCase(uriSchema))
                ret = true;
        }
        return ret;
    }

    private boolean isExternalStoreDoc(String uriAuthority)
    {
        boolean ret = false;
        if ("com.android.externalstorage.documents".equals(uriAuthority))
            ret = true;
        return ret;
    }

    private boolean isDownloadDoc(String uriAuthority)
    {
        boolean ret = false;
        if ("com.android.providers.downloads.documents".equals(uriAuthority))
            ret = true;
        return ret;
    }

    private boolean isMediaDoc(String uriAuthority)
    {
        boolean ret = false;
        if ("com.android.providers.media.documents".equals(uriAuthority))
            ret = true;
        return ret;
    }

    private boolean isGooglePhotoDoc(String uriAuthority)
    {
        boolean ret = false;
        if ("com.google.android.apps.photos.content".equals(uriAuthority))
            ret = true;
        return ret;
    }
}
