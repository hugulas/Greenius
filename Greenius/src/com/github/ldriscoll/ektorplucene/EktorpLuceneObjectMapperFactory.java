           perfinsight        ,org.eclipse.wst.jsdt.launching.JRE_CONTAINER                  TF:/workspace-newbmx/.metadata/.plugins/org.eclipse.wst.jsdt.core/libraries/system.js                             1org.eclipse.wst.jsdt.launching.baseBrowserLibrary                 `F:/workspace-newbmx/.metadata/.plugins/org.eclipse.wst.jsdt.core/libraries/baseBrowserLibrary.js                                   [F:/workspace-newbmx/.metadata/.plugins/org.eclipse.wst.jsdt.core/libraries/browserWindow.js                                   QF:/workspace-newbmx/.metadata/.plugins/org.eclipse.wst.jsdt.core/libraries/xhr.js                                   RF:/workspace-newbmx/.metadata/.plugins/org.eclipse.wst.jsdt.core/libraries/dom5.js                         	 )org.eclipse.wst.jsdt.launching.WebProject                                                                                                                                                                                 ObjectMapper createObjectMapper(CouchDbConnector connector) {
        ObjectMapper om = super.createObjectMapper(connector);
        applyDefaultConfiguration(om);
        return om;
    }

    private void applyDefaultConfiguration(ObjectMapper instance) {
        instance.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        instance.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        instance.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        instance.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        SimpleModule module = new SimpleModule("Serialization", new Version(1, 0, 0, null, null, null));
        module.addSerializer(DateTime.class, new DateTimeSerializer());
    }
}
