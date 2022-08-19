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
import java.util.Map;

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
import ch.interlis.ili2c.Ili2cException;
import ch.interlis.ili2c.Main;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ili2c.gui.UserSettings;
import ch.interlis.ili2c.metamodel.Model;
import ch.interlis.ili2c.metamodel.TransferDescription;
import ch.interlis.ili2c.modelscan.IliFile;
import ch.interlis.ili2c.modelscan.IliModel;
import ch.interlis.ilirepository.IliFiles;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.ilirepository.impl.ModelMetadata;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import ch.so.agi.repochecker.model.Check;
import ch.so.agi.repochecker.model.IliRepo;
import ch.so.agi.repochecker.model.Result;

import org.interlis2.validator.Validator;

@Service
public class CheckerService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{'${app.repositories}'.split(',')}")
    private List<String> repositories;
    
    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.workDirectoryPrefix}")
    private String workDirectoryPrefix;
    
    private String ILICACHE_FOLDER_PREFIX = ".ilicache_";

    public void fubar() throws IOException { // TODO
        UserSettings userSettings = new UserSettings();        
        userSettings.setValue(UserSettings.ILIDIRS, UserSettings.DEFAULT_ILIDIRS);
        Configuration config = new Configuration();
        
//        String repo = "https://models.kgk-cgc.ch";
        String repo = "https://models.geo.bl.ch";
        config.addFileEntry(new FileEntry(repo, FileEntryKind.ILIMODELFILE));
        config.setAutoCompleteModelList(true);
     
        HashSet<IliFile> failedFiles = new HashSet<IliFile>();
        ArrayList<MetaEntryProblem> inconsistentMetaEntry = new ArrayList<MetaEntryProblem>();
        
        File tmpFolder = ObjectPoolManager.getCacheTmpFilename();
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ILICACHE_FOLDER_PREFIX).toFile();        
        
        Map<Check, Result> resultMap = new HashMap<>();
        
        // TODO
        // - es braucht resultateOrdner für Logfiles. Dieser muss dann auch wieder erreichbar sein zum Herunterladen 
        // (siehe ilivalidator-web-service)
        // - Forschleife nicht hier? Sondern im Contrller? Resp. config.addFileEntry() ist immer ein Repo.
        
        
        try {
            Iterator<FileEntry> reposi = config.iteratorFileEntry();
            while (reposi.hasNext()) {
                FileEntry e = reposi.next();
                if (e.getKind() == FileEntryKind.ILIMODELFILE) {
                    // Verzeichnis, in das Resultate und benötigte Modelle/Schemen kopiert werden.
                    // Das Verzeichnis wird pro Repo erstellt.
                    // D.h. es gibt nur ein Repo in diesem Iterator. -> Es wird eine weitere
                    // Schleife benötigt. Entweder im Controller und im Service weiter aussen.
                    File workFolder = Files.createTempDirectory(new File(workDirectory).toPath(), workDirectoryPrefix).toFile();
                    
                    // Modelle, die zur Prüfung benötigt werden (um nicht selbst von einem Repo abhängig zu sein).
                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                    Resource[] resources = resolver.getResources("classpath:ili/*.ili");
                    for (Resource resource : resources) {
                        InputStream is = resource.getInputStream();
                        File iliFile = Paths.get(workFolder.getAbsolutePath(), resource.getFilename()).toFile();
                        Files.copy(is, iliFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    
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
                    
                    
                    // Result. String statt File und bereits die Url
                    
                    // Mit ilivalidator die ilimodels.xml-Datei validieren.
                    // Im Gegensatz zu ili2c, der ggü XSD prüft.
                    File ilimodelsXmlLogFile = Paths.get(workFolder.getAbsolutePath(), "ilimodels.xml.log").toFile();
                    Result ilimodelsXmlResult = null;
                    {
                        //EhiLogger.getInstance().setTraceFilter(false);
                        Settings settings = new Settings();
                        settings.setValue(Validator.SETTING_LOGFILE, ilimodelsXmlLogFile.getAbsolutePath());
//                        settings.setValue(Validator.SETTING_XTFLOG, logFileName + ".xtf");
                        settings.setValue(Validator.SETTING_ILIDIRS, workFolder.getAbsolutePath()+";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");//TODO hat das Implikationen?
                        boolean valid = Validator.runValidation(ilimodelsXmlFile.getAbsolutePath(), settings);
                        
                        ilimodelsXmlResult = new Result(valid, ilimodelsXmlLogFile);
                        resultMap.put(Check.ILIMODELS_XML, ilimodelsXmlResult);
                    }
                    
//                    IliRepo iliRepo = new IliRepo(repos, resultMap);
//                    log.info(iliRepo.toString());

                    // Modellprüfungen
                    ch.interlis.ilirepository.IliManager manager=new ch.interlis.ilirepository.IliManager();
                    ArrayList<String> modeldirv = new ArrayList<String>();
                    String ilidirs = userSettings.getValue(UserSettings.ILIDIRS);
                    String modeldirs[] = ilidirs.split(UserSettings.ILIDIR_SEPARATOR);
                    for(String m:modeldirs) {
                        if(!m.startsWith("%")) {
                            modeldirv.add(m);
                        }
                    }
                    manager.setRepositories((String[]) modeldirv.toArray(new String[1]));

                    // read file
                    IliFiles ilimodelsFiles=null;
                    List<ModelMetadata> ilimodelsEntries = null;
                    try {
                        ilimodelsEntries = RepositoryAccess.readIliModelsXml2(ilimodelsXmlFile);
                        ilimodelsEntries = RepositoryAccess.getLatestVersions2(ilimodelsEntries);
                        ilimodelsFiles = RepositoryAccess.createIliFiles2(repos, ilimodelsEntries);
                    } catch (RepositoryAccessException e2) {
                        // TODO
                        //EhiLogger.logError(e2);
                        continue;
                    }

                    for(Iterator<IliFile> filei=ilimodelsFiles.iteratorFile();filei.hasNext();){
                        IliFile ilimodelsFile=filei.next();
                        EhiLogger.logState("check file <"+ilimodelsFile.getPath()+"> in <"+ilimodelsFile.getRepositoryUri()+">");
                        List<String> modelsInFile=new ArrayList<String>();
                        ArrayList<String> requiredModels=new ArrayList<String>();
                        double iliversion=0.0;
                        for(Iterator modeli=ilimodelsFile.iteratorModel();modeli.hasNext();){
                            IliModel model=(IliModel)modeli.next();
                            iliversion=model.getIliVersion();
                            modelsInFile.add(model.getName());
                            for(String reqModel:(Iterable<String>)model.getDependencies()) {
                                if(!requiredModels.contains(reqModel)) {
                                    requiredModels.add(reqModel);
                                }
                            }
                        }
                        Iterator<String> requiredModelIt=requiredModels.iterator();
                        for(;requiredModelIt.hasNext();) {
                            String requiredModel=requiredModelIt.next();
                            if(modelsInFile.contains(requiredModel)) {
                                requiredModelIt.remove();
                            }
                        }
                        Configuration fileconfig = new Configuration();
                        try {
                            File localIliFileInIliCache = reposAccess.getLocalFileLocation(ilimodelsFile.getRepositoryUri(),ilimodelsFile.getPath(),0,ilimodelsFile.getMd5());
                            if(localIliFileInIliCache==null){
                                EhiLogger.logError("File <"+ilimodelsFile.getPath()+"> not found");
                                failedFiles.add(ilimodelsFile);
                                continue;
                            }
                            File localIliFile=new File(tmpFolder,localIliFileInIliCache.getName());
                            try {
                                RepositoryAccess.copyFile(localIliFile,localIliFileInIliCache);
                                
                                fileconfig.setAutoCompleteModelList(false);
                                if(requiredModels.size()>0){
                                    // get list of required files based on ilimodels.xml entries
                                    Configuration fconfig = manager.getConfig(requiredModels,iliversion);
                                    Iterator fi = fconfig.iteratorFileEntry();
                                    while (fi.hasNext()) {
                                        fileconfig.addFileEntry((FileEntry) fi.next());
                                    }
                                }
                                fileconfig.addFileEntry(new ch.interlis.ili2c.config.FileEntry(
                                        localIliFile.getAbsolutePath(),ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
                                ch.interlis.ili2c.Ili2c.logIliFiles(fileconfig);
                                fileconfig.setGenerateWarnings(false);
                                TransferDescription td=Main.runCompiler(fileconfig,userSettings);
                                if(td==null){
                                    failedFiles.add(ilimodelsFile);
                                }else{
                                    // check entries in ilimodels.xml
                                    String md5=RepositoryAccess.calcMD5(localIliFile);
                                    for(Iterator<Model> modeli=td.iterator();modeli.hasNext();){
                                        Model model=modeli.next();
                                        if(model==td.INTERLIS){
                                            continue;
                                        }
                                        if(model.getFileName()!=null && model.getFileName().equals(localIliFile.getAbsolutePath())){
                                            EhiLogger.logState("check entry of model "+model.getName());
                                            String csl=null;
                                            if(model.getIliVersion().equals(Model.ILI1)){
                                                csl=ModelMetadata.ili1;
                                            }else if(model.getIliVersion().equals(Model.ILI2_2)){
                                                csl=ModelMetadata.ili2_2;
                                            }else if(model.getIliVersion().equals(Model.ILI2_3)){
                                                csl=ModelMetadata.ili2_3;
                                            }else if(model.getIliVersion().equals(Model.ILI2_4)){
                                                csl=ModelMetadata.ili2_4;
                                            }else{
                                                throw new IllegalStateException("unexpected ili version");
                                            }
                                            ModelMetadata modelMetadata=RepositoryAccess.findModelMetadata2(ilimodelsEntries,model.getName(),csl);
                                            if(modelMetadata==null){
                                                inconsistentMetaEntry.add(new MetaEntryProblem(null,model.getName(),"entry missing or wrong model name in ilimodels.xml for "+ilimodelsFile.getPath()));
                                            }else{
                                                if(modelMetadata.getMd5()!=null && !modelMetadata.getMd5().equalsIgnoreCase(md5)){
                                                    inconsistentMetaEntry.add(new MetaEntryProblem(modelMetadata.getOid(),model.getName(),"wrong md5 value; correct would be "+md5));
                                                }
                                                if(model.getIliVersion().equals(Model.ILI2_3) || model.getIliVersion().equals(Model.ILI2_4)){
                                                    if(modelMetadata.getVersion()!=null && !modelMetadata.getVersion().equals(model.getModelVersion())){
                                                        inconsistentMetaEntry.add(new MetaEntryProblem(modelMetadata.getOid(),model.getName(),"wrong version value; correct would be "+model.getModelVersion()));
                                                    }
                                                    if(modelMetadata.getVersionComment()!=null && !modelMetadata.getVersionComment().equals(model.getModelVersionExpl())){
                                                        inconsistentMetaEntry.add(new MetaEntryProblem(modelMetadata.getOid(),model.getName(),"wrong versionComment value; correct would be "+model.getModelVersionExpl()));
                                                    }
                                                    if(modelMetadata.getIssuer()!=null && !modelMetadata.getIssuer().equals(model.getIssuer())){
                                                        inconsistentMetaEntry.add(new MetaEntryProblem(modelMetadata.getOid(),model.getName(),"wrong issuer value; correct would be "+model.getIssuer()));                                          
                                                    }
                                                }
                                                HashSet<String> depsMeta=new HashSet<String>();
                                                HashSet<String> depsIli=new HashSet<String>();
                                                for(String dep : modelMetadata.getDependsOnModel()){
                                                    depsMeta.add(dep);
                                                }
                                                String sep="";
                                                StringBuilder missingDeps=new StringBuilder();
                                                for(Model dep : model.getImporting()){
                                                    String depIli=dep.getName();
                                                    depsIli.add(depIli);
                                                    if(!depIli.equals("INTERLIS") && !depsMeta.contains(depIli)){
                                                        missingDeps.append(sep);
                                                        missingDeps.append(depIli);
                                                        sep=",";
                                                    }
                                                }
                                                if(missingDeps.length()>0){
                                                    inconsistentMetaEntry.add(new MetaEntryProblem(modelMetadata.getOid(),model.getName(),"wrong depends list; misssing models "+missingDeps.toString()));
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Ili2cException e1) {
                                e1.printStackTrace();
                                log.info(e1.getMessage());
                                EhiLogger.logError(e1);
                                failedFiles.add(ilimodelsFile);
                            }finally {
                                localIliFile.delete();
                            }
                        } catch (RepositoryAccessException e1) {
                            e1.printStackTrace();
                            log.info(e1.getMessage());
                            EhiLogger.logError(e1);
                            failedFiles.add(ilimodelsFile);
                        }

                }

   
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
