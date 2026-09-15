package com.sobrenaturaldirector.content.catalog;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import com.sobrenaturaldirector.content.model.*;
import com.sobrenaturaldirector.decision.model.ProviderStatus;

/** Small Java 8 loader for the canonical, builder-owned catalog resource. */
public final class SemanticCatalogResource {
    public static final String PATH="assets/sobrenaturaldirector/content/semantic_catalog_v1.json";
    private static final Pattern ENTRY=Pattern.compile("\\{\\\"key\\\":\\\"([^\\\"]+)\\\",\\\"provider\\\":\\\"([^\\\"]+)\\\",\\\"kind\\\":\\\"([^\\\"]+)\\\",\\\"capabilities\\\":\\[([^]]*)\\],\\\"evidence\\\":\\\"([^\\\"]+)\\\",\\\"provenance\\\":\\\"([^\\\"]+)\\\",\\\"registryResolvable\\\":(true|false),\\\"adapterRequired\\\":(true|false),\\\"execution\\\":\\\"([^\\\"]+)\\\",\\\"sources\\\":\\[\\\"([^\\\"]*)\\\"\\]\\}");
    private SemanticCatalogResource() { }
    public static SemanticCatalog load(ClassLoader loader) {
        if(loader==null)throw new IllegalArgumentException("loader");
        InputStream in=loader.getResourceAsStream(PATH);if(in==null)throw new IllegalStateException("catalog resource unavailable");
        String json=read(in);if(!json.contains("\"schema\": 1"))throw new IllegalStateException("unsupported catalog schema");json=json.replaceAll("\\s+","");
        List<SemanticContentEntry> entries=new ArrayList<SemanticContentEntry>();Set<ProviderId> ids=new TreeSet<ProviderId>();Matcher m=ENTRY.matcher(json);
        while(m.find()){ProviderId p=new ProviderId(m.group(2));ids.add(p);entries.add(new SemanticContentEntry(new ContentKey(m.group(1)),p,ContentKind.valueOf(m.group(3)),capabilities(m.group(4)),EvidenceStatus.valueOf(m.group(5)),Provenance.valueOf(m.group(6)),Boolean.parseBoolean(m.group(7)),Boolean.parseBoolean(m.group(8)),ExecutionStatus.valueOf(m.group(9)),Collections.singleton(m.group(10))));}
        if(entries.isEmpty())throw new IllegalStateException("catalog has no entries");
        List<ProviderDescriptor> providers=new ArrayList<ProviderDescriptor>();for(ProviderId p:ids)providers.add(new ProviderDescriptor(p,p.getValue(),EvidenceStatus.CONFIRMED,true,ProviderStatus.UNKNOWN,p.getValue().equals("gravestone")||p.getValue().equals("customnpcs"),Collections.singleton("PHASE_1G2_CURATED_SEMANTIC_ENTRIES")));
        return new SemanticCatalog(providers,entries);
    }
    private static Set<SemanticCapability> capabilities(String value){TreeSet<SemanticCapability> r=new TreeSet<SemanticCapability>();Matcher m=Pattern.compile("\\\"([^\\\"]+)\\\"").matcher(value);while(m.find())r.add(new SemanticCapability(m.group(1)));return r;}
    private static String read(InputStream in){try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[1024];int n;while((n=in.read(b))!=-1)out.write(b,0,n);in.close();return new String(out.toByteArray(),"UTF-8");}catch(IOException e){throw new IllegalStateException("catalog read failed",e);}}
}
