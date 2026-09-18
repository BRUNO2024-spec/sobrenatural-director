package com.sobrenaturaldirector.provider;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Map;

/** Stable, non-identifying fingerprint of the runtime provider registry. */
public final class ProviderStackFingerprint {
    private ProviderStackFingerprint() { }
    public static String of(DirectorProviderRegistry registry) {
        StringBuilder value=new StringBuilder();
        if(registry!=null) for(Map.Entry<com.sobrenaturaldirector.content.model.ProviderId,DirectorContentProvider> e:registry.getProviders().entrySet()) { value.append(e.getKey().getValue()).append('|').append(e.getValue().getStatus()).append('|').append(e.getValue().getDetectedVersion()).append('|'); for(String c:e.getValue().getCapabilities().keySet()) value.append(c).append('=').append(e.getValue().getCapabilities().get(c)).append(','); value.append(';'); }
        try { MessageDigest digest=MessageDigest.getInstance("SHA-256"); byte[] bytes=digest.digest(value.toString().getBytes(Charset.forName("UTF-8"))); StringBuilder result=new StringBuilder(); for(byte b:bytes) result.append(String.format("%02x",b&255)); return result.toString(); } catch(Exception failure) { throw new IllegalStateException("provider fingerprint unavailable",failure); }
    }
}
