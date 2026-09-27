package com.najmspace.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileNotFoundException;

public class ApkProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}

    private File resolve(Uri uri) throws FileNotFoundException {
        String last=uri.getLastPathSegment();
        if(last==null)throw new FileNotFoundException();
        File base=new File(getContext().getCacheDir(),"install");
        File f=new File(base,last);
        try{
            String bp=base.getCanonicalPath()+File.separator;
            if(!f.getCanonicalPath().startsWith(bp))throw new FileNotFoundException();
        }catch(Exception e){throw new FileNotFoundException();}
        if(!f.exists())throw new FileNotFoundException();
        return f;
    }

    @Override public String getType(Uri uri){return "application/vnd.android.package-archive";}

    @Override public ParcelFileDescriptor openFile(Uri uri,String mode) throws FileNotFoundException {
        return ParcelFileDescriptor.open(resolve(uri),ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){
        try{
            File f=resolve(uri);
            MatrixCursor c=new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE});
            c.addRow(new Object[]{f.getName(),f.length()});
            return c;
        }catch(Exception e){return null;}
    }

    @Override public Uri insert(Uri u,ContentValues v){return null;}
    @Override public int delete(Uri u,String s,String[] a){return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){return 0;}
}
