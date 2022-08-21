package ch.so.agi.repochecker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.settings.Settings;
import ch.ehi.iox.objpool.ObjectPoolManager;
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
    
    @Autowired
    private IliRepoRepository iliRepoRepository;
    
    private String ILICACHE_FOLDER_PREFIX = ".ilicache_";

    public void checkRepos() throws IOException {
        for (String repository : repositories) {
            log.info("checking: " + repository);
            this.checkRepo(repository);
        } 
    }
    
    public synchronized void checkRepo(String repository) throws IOException { // TODO
        UserSettings userSettings = new UserSettings();        
        userSettings.setValue(UserSettings.ILIDIRS, UserSettings.DEFAULT_ILIDIRS);
        Configuration config = new Configuration();
        
//        repository = "https://models.kgk-cgc.ch";
//        repository = "https://models.geo.bl.ch";
//        repository = "https://geo.so.ch/models";
        config.addFileEntry(new FileEntry(repository, FileEntryKind.ILIMODELFILE));
        config.setAutoCompleteModelList(true);
     
        HashSet<IliFile> failedFiles = new HashSet<IliFile>();
        ArrayList<MetaEntryProblem> inconsistentMetaEntry = new ArrayList<MetaEntryProblem>();
        
        File tmpFolder = ObjectPoolManager.getCacheTmpFilename();
        
        // Damit die Daten nicht in den Standard-Cache-Ordner heruntergeladen werden.
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ILICACHE_FOLDER_PREFIX).toFile();        
          
        Map<Check, Result> resultMap = new HashMap<>();

        try {
            Iterator<FileEntry> reposi = config.iteratorFileEntry();
            
            // In unserem Fall immer nur ein (1) Repo.
            while (reposi.hasNext()) {
                FileEntry e = reposi.next();
                if (e.getKind() == FileEntryKind.ILIMODELFILE) {
                    // Verzeichnis, in das Resultate und benötigte Modelle/Schemen kopiert werden.
                    // Das Verzeichnis wird pro Repo erstellt.
                    // D.h. es gibt nur ein Repo in diesem Iterator. -> Es wird eine weitere
                    // Schleife benötigt. Entweder im Controller und im Service weiter aussen.
                    File workFolder = Files
                            .createTempDirectory(new File(workDirectory).toPath(), workDirectoryPrefix)
                            .toFile();

                    File ilisiteXmlLogFile = Paths.get(workFolder.getAbsolutePath(), "ilisite.xml.log").toFile();
                    File ilimodelsXmlLogFile = Paths.get(workFolder.getAbsolutePath(), "ilimodels.xml.log").toFile();
                    File modelsLogFile = Paths.get(workFolder.getAbsolutePath(), "models.log").toFile();
                    if(!modelsLogFile.exists()) {
                        modelsLogFile.createNewFile();
                    }
                    
                    // Modelle, die zur Prüfung benötigt werden (um nicht selbst von einem Repo
                    // abhängig zu sein).
//                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//                    Resource[] resources = resolver.getResources("*.ili");
//                    for (Resource resource : resources) {
//                        InputStream is = resource.getInputStream();
//                        File iliFile = Paths.get(workFolder.getAbsolutePath(), resource.getFilename()).toFile();
//                        Files.copy(is, iliFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                    }
                    
                    copyResource("ili/IliSite09-20091119.ili", workFolder.getAbsolutePath());
                    copyResource("ili/IliRepository09-20120220.ili", workFolder.getAbsolutePath());
                    copyResource("ili/IliRepository20.ili", workFolder.getAbsolutePath());

                    String repos = e.getFilename();
                    // get list of current files in repository
                    RepositoryAccess reposAccess = new RepositoryAccess();
                    reposAccess.setCache(ilicacheFolder);

                    // ilisite.xml-Datei validieren.
                    File ilisiteXmlFile;
                    try {
                        ilisiteXmlFile = reposAccess.getLocalFileLocation(repos, IliManager.ILISITE_XML, 0, null);
                    } catch (RepositoryAccessException e2) {
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), e2.getMessage()+"\n", StandardOpenOption.APPEND);
                        continue;
                    }
                    if (ilisiteXmlFile == null) {
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "URL <"+repos+"> contains no"+IliManager.ILISITE_XML+"; ignored"+"\n", StandardOpenOption.APPEND);
                        continue;
                    }
                    
                    Result ilisiteXmlResult = null;
                    {
                        Settings settings = new Settings();
                        settings.setValue(Validator.SETTING_LOGFILE, ilisiteXmlLogFile.getAbsolutePath());
                        settings.setValue(Validator.SETTING_ILIDIRS,workFolder.getAbsolutePath() + ";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");
                        boolean valid = Validator.runValidation(ilisiteXmlFile.getAbsolutePath(), settings);

                        ilisiteXmlResult = new Result(valid, ilisiteXmlLogFile.getAbsolutePath(), new Date());
                        resultMap.put(Check.ILISITE_XML, ilisiteXmlResult);
                    }

                    // Mit ilivalidator die ilimodels.xml-Datei validieren.
                    // Im Gegensatz zu ili2c, der ggü XSD prüft.
                    File ilimodelsXmlFile;
                    try {
                        ilimodelsXmlFile = reposAccess.getLocalFileLocation(repos, IliManager.ILIMODELS_XML, 0, null);
                    } catch (RepositoryAccessException e2) {
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), e2.getMessage()+"\n", StandardOpenOption.APPEND);
                        continue;
                    }
                    if (ilimodelsXmlFile == null) {
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "URL <"+repos+"> contains no"+IliManager.ILIMODELS_XML+"; ignored"+"\n", StandardOpenOption.APPEND);
                        continue;
                    }

                    Result ilimodelsXmlResult = null;
                    {
                        // EhiLogger.getInstance().setTraceFilter(false);
                        Settings settings = new Settings();
                        settings.setValue(Validator.SETTING_LOGFILE, ilimodelsXmlLogFile.getAbsolutePath());
                        settings.setValue(Validator.SETTING_ILIDIRS,workFolder.getAbsolutePath() + ";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");
                        boolean valid = Validator.runValidation(ilimodelsXmlFile.getAbsolutePath(), settings);

                        ilimodelsXmlResult = new Result(valid, ilimodelsXmlLogFile.getAbsolutePath(), new Date());
                        resultMap.put(Check.ILIMODELS_XML, ilimodelsXmlResult);
                    }

                    // Modelle validieren
                    ch.interlis.ilirepository.IliManager manager = new ch.interlis.ilirepository.IliManager();
                    ArrayList<String> modeldirv = new ArrayList<String>();
                    String ilidirs = userSettings.getValue(UserSettings.ILIDIRS);
                    String modeldirs[] = ilidirs.split(UserSettings.ILIDIR_SEPARATOR);
                    for (String m : modeldirs) {
                        if (!m.startsWith("%")) {
                            modeldirv.add(m);
                        }
                    }
                    manager.setRepositories((String[]) modeldirv.toArray(new String[1]));

                    // read file
                    IliFiles ilimodelsFiles = null;
                    List<ModelMetadata> ilimodelsEntries = null;
                    try {
                        ilimodelsEntries = RepositoryAccess.readIliModelsXml2(ilimodelsXmlFile);
                        ilimodelsEntries = RepositoryAccess.getLatestVersions2(ilimodelsEntries);
                        ilimodelsFiles = RepositoryAccess.createIliFiles2(repos, ilimodelsEntries);
                    } catch (RepositoryAccessException e2) {
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), e2.getMessage()+"\n", StandardOpenOption.APPEND);
                        continue;
                    }

                    for (Iterator<IliFile> filei = ilimodelsFiles.iteratorFile(); filei.hasNext();) {
                        IliFile ilimodelsFile = filei.next();
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "check file <" + ilimodelsFile.getPath() + "> in <"
                                + ilimodelsFile.getRepositoryUri() + ">\n", StandardOpenOption.APPEND);
                        List<String> modelsInFile = new ArrayList<String>();
                        ArrayList<String> requiredModels = new ArrayList<String>();
                        double iliversion = 0.0;
                        for (Iterator modeli = ilimodelsFile.iteratorModel(); modeli.hasNext();) {
                            IliModel model = (IliModel) modeli.next();
                            iliversion = model.getIliVersion();
                            modelsInFile.add(model.getName());
                            for (String reqModel : (Iterable<String>) model.getDependencies()) {
                                if (!requiredModels.contains(reqModel)) {
                                    requiredModels.add(reqModel);
                                }
                            }
                        }
                        Iterator<String> requiredModelIt = requiredModels.iterator();
                        for (; requiredModelIt.hasNext();) {
                            String requiredModel = requiredModelIt.next();
                            if (modelsInFile.contains(requiredModel)) {
                                requiredModelIt.remove();
                            }
                        }
                        Configuration fileconfig = new Configuration();
                        try {
                            File localIliFileInIliCache = reposAccess
                                    .getLocalFileLocation(ilimodelsFile.getRepositoryUri(),
                                            ilimodelsFile.getPath(),
                                            0,
                                            ilimodelsFile.getMd5());
                            if (localIliFileInIliCache == null) {
                                Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "File <" + ilimodelsFile.getPath() + "> not found\n", StandardOpenOption.APPEND);
                                failedFiles.add(ilimodelsFile);
                                continue;
                            }
                            File localIliFile = new File(tmpFolder, localIliFileInIliCache.getName());
                            
                            PrintStream old = System.err;
                            try {
                                RepositoryAccess.copyFile(localIliFile, localIliFileInIliCache);

                                fileconfig.setAutoCompleteModelList(false);
                                if (requiredModels.size() > 0) {
                                    // get list of required files based on ilimodels.xml entries
                                    // Manager findet "INTERLIS" Modell nicht. -> INTERLIS: model(s) not found
                                    Configuration fconfig = manager.getConfig(requiredModels, iliversion);
                                    Iterator fi = fconfig.iteratorFileEntry();
                                    while (fi.hasNext()) {
                                        FileEntry fileEntry = (FileEntry) fi.next();
                                        fileconfig.addFileEntry(fileEntry);
                                    }
                                }
                                fileconfig
                                        .addFileEntry(
                                                new ch.interlis.ili2c.config.FileEntry(localIliFile.getAbsolutePath(),
                                                        ch.interlis.ili2c.config.FileEntryKind.ILIMODELFILE));
                                ch.interlis.ili2c.Ili2c.logIliFiles(fileconfig);
                                fileconfig.setGenerateWarnings(false);
                                
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                PrintStream ps = new PrintStream(baos);
                                System.setErr(ps);
                                
                                TransferDescription td = Main.runCompiler(fileconfig, userSettings);
                                
                                System.err.flush();
                                System.setErr(old);
                                
                                Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), baos.toString(), StandardOpenOption.APPEND);

                                if (td == null) {
                                    failedFiles.add(ilimodelsFile);
                                } else {
                                    // check entries in ilimodels.xml
                                    String md5 = RepositoryAccess.calcMD5(localIliFile);
                                    for (Iterator<Model> modeli = td.iterator(); modeli.hasNext();) {
                                        Model model = modeli.next();
                                        if (model == td.INTERLIS) {
                                            continue;
                                        }
                                        if (model.getFileName() != null
                                                && model.getFileName().equals(localIliFile.getAbsolutePath())) {
                                            Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "check entry of model " + model.getName()+"\n", StandardOpenOption.APPEND);

                                            String csl = null;
                                            if (model.getIliVersion().equals(Model.ILI1)) {
                                                csl = ModelMetadata.ili1;
                                            } else if (model.getIliVersion().equals(Model.ILI2_2)) {
                                                csl = ModelMetadata.ili2_2;
                                            } else if (model.getIliVersion().equals(Model.ILI2_3)) {
                                                csl = ModelMetadata.ili2_3;
                                            } else if (model.getIliVersion().equals(Model.ILI2_4)) {
                                                csl = ModelMetadata.ili2_4;
                                            } else {
                                                throw new IllegalStateException("unexpected ili version");
                                            }
                                            ModelMetadata modelMetadata = RepositoryAccess
                                                    .findModelMetadata2(ilimodelsEntries, model.getName(), csl);
                                            if (modelMetadata == null) {
                                                inconsistentMetaEntry
                                                        .add(new MetaEntryProblem(null,
                                                                model.getName(),
                                                                "entry missing or wrong model name in ilimodels.xml for "
                                                                        + ilimodelsFile.getPath()));
                                            } else {
                                                if (modelMetadata.getMd5() != null
                                                        && !modelMetadata.getMd5().equalsIgnoreCase(md5)) {
                                                    inconsistentMetaEntry
                                                            .add(new MetaEntryProblem(modelMetadata.getOid(),
                                                                    model.getName(),
                                                                    "wrong md5 value; correct would be " + md5));
                                                }
                                                if (model.getIliVersion().equals(Model.ILI2_3)
                                                        || model.getIliVersion().equals(Model.ILI2_4)) {
                                                    if (modelMetadata.getVersion() != null && !modelMetadata
                                                            .getVersion()
                                                            .equals(model.getModelVersion())) {
                                                        inconsistentMetaEntry
                                                                .add(new MetaEntryProblem(modelMetadata.getOid(),
                                                                        model.getName(),
                                                                        "wrong version value; correct would be "
                                                                                + model.getModelVersion()));
                                                    }
                                                    if (modelMetadata.getVersionComment() != null && !modelMetadata
                                                            .getVersionComment()
                                                            .equals(model.getModelVersionExpl())) {
                                                        inconsistentMetaEntry
                                                                .add(new MetaEntryProblem(modelMetadata.getOid(),
                                                                        model.getName(),
                                                                        "wrong versionComment value; correct would be "
                                                                                + model.getModelVersionExpl()));
                                                    }
                                                    if (modelMetadata.getIssuer() != null
                                                            && !modelMetadata.getIssuer().equals(model.getIssuer())) {
                                                        inconsistentMetaEntry
                                                                .add(new MetaEntryProblem(modelMetadata.getOid(),
                                                                        model.getName(),
                                                                        "wrong issuer value; correct would be "
                                                                                + model.getIssuer()));
                                                    }
                                                }
                                                HashSet<String> depsMeta = new HashSet<String>();
                                                HashSet<String> depsIli = new HashSet<String>();
                                                for (String dep : modelMetadata.getDependsOnModel()) {
                                                    depsMeta.add(dep);
                                                }
                                                String sep = "";
                                                StringBuilder missingDeps = new StringBuilder();
                                                for (Model dep : model.getImporting()) {
                                                    String depIli = dep.getName();
                                                    depsIli.add(depIli);
                                                    if (!depIli.equals("INTERLIS") && !depsMeta.contains(depIli)) {
                                                        missingDeps.append(sep);
                                                        missingDeps.append(depIli);
                                                        sep = ",";
                                                    }
                                                }
                                                if (missingDeps.length() > 0) {
                                                    inconsistentMetaEntry
                                                            .add(new MetaEntryProblem(modelMetadata.getOid(),
                                                                    model.getName(),
                                                                    "wrong depends list; misssing models "
                                                                            + missingDeps.toString()));
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Ili2cException e1) {
                                Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), e1.getMessage()+"\n", StandardOpenOption.APPEND);
                                failedFiles.add(ilimodelsFile);
                            } finally {
                                System.setErr(old);
                                localIliFile.delete();
                            }
                        } catch (RepositoryAccessException e1) {
                            Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), e1.getMessage()+"\n", StandardOpenOption.APPEND);
                            failedFiles.add(ilimodelsFile);
                        }
                    }
                    boolean valid = true;
                    if (inconsistentMetaEntry.size() > 0) {
                        valid = false;
                        Collections.sort(inconsistentMetaEntry, new Comparator<MetaEntryProblem>() {
                            public int compare(MetaEntryProblem arg0, MetaEntryProblem arg1) {
                                int c1 = arg0.getModelName().compareTo(arg1.getModelName());
                                if (c1 == 0) {
                                    if (arg0.getTid() == null) {
                                        if (arg1.getTid() == null) {
                                            return 0;
                                        }
                                        return -1;
                                    }
                                    if (arg1.getTid() == null) {
                                        return 1;
                                    }
                                    return arg0.getTid().compareTo(arg1.getTid());
                                }
                                return c1;
                            }

                        });
                        for (MetaEntryProblem prb : inconsistentMetaEntry) {
                            if (prb.getTid() == null) {
                                Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), prb.getModelName() + ": " + prb.getMsg()+"\n", StandardOpenOption.APPEND);
                            } else {
                                Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "(TID=\"" + prb.getTid() + "\"): " + prb.getMsg()+"\n", StandardOpenOption.APPEND);
                            }
                        }
                    }
                    if(failedFiles.size()!=0){
                        valid = false;
                        StringBuilder failed=new StringBuilder();
                        String sep="";
                        for(IliFile f:failedFiles){
                            failed.append(sep);
                            failed.append(f.getPath());
                            sep=", ";
                        }
                        Files.writeString(Paths.get(modelsLogFile.getAbsolutePath()), "compile failed with files: "+failed+"\n", StandardOpenOption.APPEND);
                    }
                    Result modelsResult = new Result(valid, modelsLogFile.getAbsolutePath(), new Date());
                    resultMap.put(Check.MODELS, modelsResult);
                    
                    IliRepo iliRepo = new IliRepo(repos, resultMap);
                    
                    iliRepoRepository.save(iliRepo);
                }
            }
        } finally {
            tmpFolder.delete();
            ilicacheFolder.delete();
        }
    }
 
    private void copyResource(String resource, String targetDirectory) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        File file = Paths.get(targetDirectory, new File(resource).getName()).toFile();
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
