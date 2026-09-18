package com.sobrenaturaldirector.provider;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/** Stable, non-identifying fingerprint of the runtime provider registry. */
public final class ProviderStackFingerprint {
    private ProviderStackFingerprint() { }
    public static String of(DirectorProviderRegistry registry) {
        return of(registry, java.util.Collections.<String,String>emptyMap());
    }
    public static String of(DirectorProviderRegistry registry, Map<String,String> jarShaByModId) {
        StringBuilder value=new StringBuilder();
        Map<String,String> hashes=new TreeMap<String,String>(jarShaByModId==null?java.util.Collections.<String,String>emptyMap():jarShaByModId);
        if(registry!=null) for(Map.Entry<com.sobrenaturaldirector.content.model.ProviderId,DirectorContentProvider> e:registry.getProviders().entrySet()) { DirectorContentProvider p=e.getValue(); value.append(e.getKey().getValue()).append('|').append(p.getModId()).append('|').append(p.getDetectedVersion()).append('|').append(hashes.containsKey(p.getModId())?hashes.get(p.getModId()):"SHA_NOT_CAPTURED").append('|').append(p.getAdapterVersion()).append('|').append(p.getStatus()).append('|'); for(String c:new TreeSet<String>(p.getCapabilities().keySet())) value.append(c).append('=').append(p.getCapabilities().get(c)).append(','); value.append(';'); }
        try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); byte[] bytes=digest.digest(value.toString().getBytes(Charset.forName("UTF-8"))); StringBuilder result=new StringBuilder(); for(byte b:bytes) result.append(String.format("%02x",b&255)); return result.toString(); } catch(Exception failure) { throw new IllegalStateException("provider fingerprint unavailable",failure); }
    }
}
