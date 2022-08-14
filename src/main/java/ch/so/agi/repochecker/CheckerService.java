package ch.so.agi.repochecker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.iox.objpool.ObjectPoolManager;
import ch.interlis.ili2c.CheckReposIlis;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ili2c.modelscan.IliFile;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import org.interlis2.validator.Validator;

@Service
public class CheckerService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{'${app.repositories}'.split(',')}")
    private List<String> repositories;
    
    @Value("${app.resultDirectoryPrefix}")
    private String resultDirectoryPrefix;
    
    private String ILICACHE_FOLDER_PREFIX = ".ilicache_";

    public void fubar() throws IOException { // TODO
        UserSettings settings = new UserSettings();        
        settings.setValue(UserSettings.ILIDIRS, UserSettings.DEFAULT_ILIDIRS);
        Configuration config = new Configuration();
        
//        String repo = "https://models.kgk-cgc.ch";
        String repo = "https://models.geo.bl.ch";
        config.addFileEntry(new FileEntry(repo, FileEntryKind.ILIMODELFILE));
        config.setAutoCompleteModelList(true);
     
        HashSet<IliFile> failedFiles = new HashSet<IliFile>();
        ArrayList<MetaEntryProblem> inconsistentMetaEntry = new ArrayList<MetaEntryProblem>();
        File tmpFolder = ObjectPoolManager.getCacheTmpFilename();
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ILICACHE_FOLDER_PREFIX).toFile();
        
        // TODO
        // - es braucht resultateOrdner für Logfiles. Dieser muss dann auch wieder erreichbar sein zum Herunterladen 
        // (siehe ilivalidator-web-service).
        
        // - Forschleife nicht hier? Sondern im Contrller? Resp. config.addFileEntry() ist immer ein Repo.
        
        // - oder doch Work nennen und die ilis dort rein kopieren. Bessere Transparenz.
        
        try {
            Iterator<FileEntry> reposi = config.iteratorFileEntry();
            while (reposi.hasNext()) {
                FileEntry e = reposi.next();
                if (e.getKind() == FileEntryKind.ILIMODELFILE) {
                    String repos = e.getFilename();
                    // get list of current files in repository
                    RepositoryAccess reposAccess = new RepositoryAccess();
                    reposAccess.setCache(ilicacheFolder);
                    File ilimodelsXmlFile;
                    try {
                        ilimodelsXmlFile = reposAccess.getLocalFileLocation(repos,IliManager.ILIMODELS_XML,0,null);
                        System.out.println(ilimodelsXmlFile);
                    } catch (RepositoryAccessException e2) {
                        //TODO
                        //EhiLogger.logError(e2);
                        continue;
                    }
                    if (ilimodelsXmlFile == null) {
                        //TODO
                        //EhiLogger.logAdaption("URL <"+repos+"> contains no "+IliManager.ILIMODELS_XML+"; ignored");
                        continue;
                    }
                    
                    // Mit ilivalidator die ilimodels.xml-Datei validieren.
                    // Im Gegensatz zu ili2c, der ggü XSD prüft.
                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                    Resource[] resources = resolver.getResources("classpath:ili/*.ili");
                    for (Resource resource : resources) {
                        InputStream is = resource.getInputStream();
                        File iliFile = Paths.get(ilicacheFolder.getAbsolutePath(), resource.getFilename()).toFile();
                        //log.info(iliFile.getAbsolutePath());
                        Files.copy(is, iliFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        //IOUtils.closeQuietly(is);
                    }
                    
                    //EhiLogger.getInstance().setTraceFilter(false);
                    
                    Settings ilivalidatorSettings = new Settings();
//                    settings.setValue(Validator.SETTING_LOGFILE, logFileName);
//                    settings.setValue(Validator.SETTING_XTFLOG, logFileName + ".xtf");
                    ilivalidatorSettings.setValue(Validator.SETTING_ILIDIRS, ilicacheFolder.getAbsolutePath()+";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");//TODO hat das Implikationen?
                    boolean valid = Validator.runValidation(ilimodelsXmlFile.getAbsolutePath(), ilivalidatorSettings);

                    
                }
            }
        } finally {
            tmpFolder.delete();   
            ilicacheFolder.delete();
        }
        
        
        
        
        
        
        
//        boolean failed = new CheckReposIlis().checkRepoIlis(config, settings);
//        log.info("{}", failed);

    }
    
    
    private class MetaEntryProblem {
        private String modelName = null;
        private String tid = null;
        private String msg = null;

        public MetaEntryProblem(String tid1, String modelName1, String msg1) {
            this.modelName = modelName1;
            this.tid = tid1;
            this.msg = msg1;
        }

        public String getModelName() {
            return modelName;
        }

        public String getTid() {
            return tid;
        }

        public String getMsg() {
            return msg;
        }
    }
}
