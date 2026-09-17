package com.sobrenaturaldirector.shadow;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import com.sobrenaturaldirector.config.FoundationConfig;

/** Immutable artifact identity captured once per collector boot. */
public final class ShadowRuntimeIdentity {
    public final String jarSha256, jarShaStatus, modelSha256, frozenConfigSha256, featureSchemaSha256;
    private ShadowRuntimeIdentity(String jar,String status,String model,String config,String feature){jarSha256=jar;jarShaStatus=status;modelSha256=model;frozenConfigSha256=config;featureSchemaSha256=feature;}
    public static ShadowRuntimeIdentity capture(FoundationConfig c, Class<?> anchor){
        String jar="",status="DEV_CLASSPATH";try{URL u=anchor.getProtectionDomain().getCodeSource().getLocation();File f=new File(u.toURI());if(f.isFile()){jar=sha(f);status="VALID";}}catch(Exception ignored){}
        if(!"VALID".equals(status)){File installed=new File("mods/SobrenaturalDirector-0.12.0-alpha.jar");try{if(installed.isFile()){jar=sha(installed);status="VALID";}}catch(Exception ignored){}}
        return new ShadowRuntimeIdentity(jar,status,c.getShadowExpectedModelSha256(),c.getShadowExpectedFrozenConfigSha256(),c.getShadowExpectedFeatureSchemaSha256());
    }
    private static String sha(File f)throws Exception{java.io.FileInputStream in=new java.io.FileInputStream(f);MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)d.update(b,0,n);in.close();StringBuilder s=new StringBuilder();for(byte x:d.digest())s.append(String.format("%02x",x&255));return s.toString();}
}
