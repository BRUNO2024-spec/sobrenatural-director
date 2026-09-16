package com.sobrenaturaldirector.shadow;

import java.io.File; import java.io.FileInputStream; import java.security.MessageDigest;

/** Fail-closed external-artifact guard; the checkpoint never enters the JAR. */
public final class ShadowArtifactValidator {
    private ShadowArtifactValidator(){}
    public static void validate(File file,String expectedSha256,String expectedSchema,String actualSchema){
        if(file==null||!file.isFile()||expectedSha256==null||expectedSha256.length()!=64||expectedSchema==null||!expectedSchema.equals(actualSchema))throw new IllegalArgumentException("shadow model invalid");
        try{MessageDigest d=MessageDigest.getInstance("SHA-256");FileInputStream in=new FileInputStream(file);byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)d.update(b,0,n);in.close();StringBuilder x=new StringBuilder();for(byte z:d.digest())x.append(String.format("%02x",z&255));if(!expectedSha256.equals(x.toString()))throw new IllegalArgumentException("shadow model hash mismatch");}catch(Exception e){if(e instanceof IllegalArgumentException)throw (IllegalArgumentException)e;throw new IllegalArgumentException("shadow model unreadable",e);}
    }
}
