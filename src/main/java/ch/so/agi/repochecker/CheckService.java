package ch.so.agi.repochecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.basics.logging.StdListener;
import ch.ehi.basics.settings.Settings;
import ch.interlis.ili2c.CheckReposIlis;
import ch.interlis.ili2c.Ili2cSettings;
import ch.interlis.ili2c.config.Configuration;
import ch.interlis.ili2c.config.FileEntry;
import ch.interlis.ili2c.config.FileEntryKind;
import ch.interlis.ilirepository.IliManager;
import ch.interlis.ilirepository.impl.RepositoryAccess;
import ch.interlis.ilirepository.impl.RepositoryAccessException;
import ch.interlis.iox_j.logging.FileLogger;
import ch.so.agi.repochecker.model.Check;
import ch.so.agi.repochecker.model.CheckType;
import ch.so.agi.repochecker.model.Repositories;
import ch.so.agi.repochecker.model.Repository;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.interlis2.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CheckService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("#{'${app.repositories}'.split(',')}")
    private List<String> repositories;
    
    @Value("${app.workDirectory}")
    private String workDirectory;
    
    @Value("${app.workDirectoryPrefix}")
    private String workDirectoryPrefix;
    
    @Autowired
    private XmlMapper xmlMapper;
    
    private String htmlString = new String();

    private String ILICACHE_FOLDER_PREFIX = ".ilicache_";
        
    public String getHtmlString() {
        return htmlString;
    }
    
    public void checkRepos() {
        List<Repository> repositoryList = new ArrayList<Repository>();
        for (String repository : repositories) {
            log.info("Checking: " + repository);
            try {
                Repository checkRepository = checkRepo(repository.trim());
                repositoryList.add(checkRepository);                    
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }  
        
//        try {
//            var xmlStringDebug = xmlMapper.writeValueAsString(repositoryList);
//            System.out.println(xmlStringDebug);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
        // Wie atomar ist die XSL-Transformation?
        // Request der Seite vs. zeitgleiche XSL-Transformation?
        Repositories repositories = new Repositories(repositoryList);
        
        try {
            var xmlString = xmlMapper.writeValueAsString(repositories);
            
            File workFolder = Files
                    .createTempDirectory(new File(workDirectory).toPath(), workDirectoryPrefix)
                    .toFile();
            copyResource("xsl/xml2html.xsl", workFolder.getAbsolutePath());
            File xslFile = Paths.get(workFolder.getAbsolutePath(), "xml2html.xsl").toFile();
                        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            Processor processor = new Processor(false);
            XsltCompiler compiler = processor.newXsltCompiler();
            XsltExecutable stylesheet = compiler.compile(new StreamSource(xslFile));
            Serializer out = processor.newSerializer(baos);

            Xslt30Transformer transformer = stylesheet.load30();
            transformer.transform(new StreamSource(new StringReader(xmlString)), out);

            htmlString = new String(baos.toByteArray(), "UTF-8");
            
        } catch (IOException | SaxonApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized Repository checkRepo(String repository) throws IOException { // TODO
        // Ich wünschte, dass das stdout logging disabled?
        EhiLogger.getInstance().removeListener(StdListener.getInstance());

        List<Check> checks = new ArrayList<Check>();
        
        File workFolder = Files
                .createTempDirectory(new File(workDirectory).toPath(), workDirectoryPrefix)
                .toFile();

        // Damit die Daten nicht in den Standard-Cache-Ordner heruntergeladen werden.
        // Funktioniert nicht bei der Validierung der einzelnen Modelle.
        File ilicacheFolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), ILICACHE_FOLDER_PREFIX).toFile();        

        // Modelle, die zur Prüfung benötigt werden (um nicht selbst von einem Repo
        // abhängig zu sein).
        // PathMatchingResourcePatternResolver funktioniert nicht mit native image.
        // FIXME: Sollte mit Spring Boot 3 funktionieren.
        // TODO
        copyResource("ili/IliSite09-20091119.ili", workFolder.getAbsolutePath());
        copyResource("ili/IliRepository09-20120220.ili", workFolder.getAbsolutePath());
        copyResource("ili/IliRepository20.ili", workFolder.getAbsolutePath());

        RepositoryAccess reposAccess = new RepositoryAccess();
        reposAccess.setCache(ilicacheFolder);

        // ilisite.xml
        {
            File logfile = Paths.get(workFolder.getAbsolutePath(), "ilisite.xml.txt").toFile();
            FileLogger fileLogger = new FileLogger(logfile, true);
            
            EhiLogger.getInstance().addListener(fileLogger);

            File ilisiteXmlFile;
            try {
                ilisiteXmlFile = reposAccess.getLocalFileLocation(repository, IliManager.ILISITE_XML, 0, null);
            } catch (RepositoryAccessException e) {
                EhiLogger.logError(e.getMessage());
                log.error(e.getMessage());
                
                Check check = new Check(CheckType.ILISITE_XML, null, logfile.getAbsolutePath(), new Date());
                checks.add(check);
                Repository checkedRepository = new Repository(repository, checks);

                return checkedRepository;
            }
            if (ilisiteXmlFile == null) {
                EhiLogger.logError("URL <"+repository+"> contains no"+IliManager.ILISITE_XML+"; ignored");
                log.error("URL <"+repository+"> contains no"+IliManager.ILISITE_XML+"; ignored");
                
                Check check = new Check(CheckType.ILISITE_XML, null, logfile.getAbsolutePath(), new Date());
                checks.add(check);
                Repository checkedRepository = new Repository(repository, checks);

                return checkedRepository;
            }

            EhiLogger.getInstance().removeListener(fileLogger);

            Settings settings = new Settings();
            settings.setValue(Validator.SETTING_LOGFILE, logfile.getAbsolutePath());
            settings.setValue(Validator.SETTING_ILIDIRS,workFolder.getAbsolutePath() + ";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");
            boolean valid = Validator.runValidation(ilisiteXmlFile.getAbsolutePath(), settings);
            
            Check check = new Check(CheckType.ILISITE_XML, valid, logfile.getAbsolutePath(), new Date());
            checks.add(check);
        }
        
        // ilimodels.xml
        {
            File logfile = Paths.get(workFolder.getAbsolutePath(), "ilimodels.xml.txt").toFile();
            FileLogger fileLogger = new FileLogger(logfile, true);
            
            EhiLogger.getInstance().addListener(fileLogger);
            
            File ilimodelsXmlFile;
            try {
                ilimodelsXmlFile = reposAccess.getLocalFileLocation(repository, IliManager.ILIMODELS_XML, 0, null);
            } catch (RepositoryAccessException e) {
                EhiLogger.logError(e.getMessage());
                log.error(e.getMessage());
                
                Check check = new Check(CheckType.ILIMODELS_XML, null, logfile.getAbsolutePath(), new Date());
                checks.add(check);
                Repository checkedRepository = new Repository(repository, checks);

                return checkedRepository;
            }
            if (ilimodelsXmlFile == null) {
                EhiLogger.logError("URL <"+repository+"> contains no"+IliManager.ILIMODELS_XML+"; ignored");
                log.error("URL <"+repository+"> contains no"+IliManager.ILISITE_XML+"; ignored");
                
                Check check = new Check(CheckType.ILIMODELS_XML, null, logfile.getAbsolutePath(), new Date());
                checks.add(check);
                Repository checkedRepository = new Repository(repository, checks);

                return checkedRepository;
            }

            EhiLogger.getInstance().removeListener(fileLogger);

            Settings settings = new Settings();
            settings.setValue(Validator.SETTING_LOGFILE, logfile.getAbsolutePath());
            settings.setValue(Validator.SETTING_ILIDIRS,workFolder.getAbsolutePath() + ";%ITF_DIR;http://models.interlis.ch/;%JAR_DIR/ilimodels");
            boolean valid = Validator.runValidation(ilimodelsXmlFile.getAbsolutePath(), settings);

            Check check = new Check(CheckType.ILIMODELS_XML, valid, logfile.getAbsolutePath(), new Date());
            checks.add(check);
        }
        
        // models
        {
            File logfile = Paths.get(workFolder.getAbsolutePath(), "models.txt").toFile();
            FileLogger fileLogger = new FileLogger(logfile, true);
            
            EhiLogger.getInstance().addListener(fileLogger);

            Ili2cSettings userSettings = new Ili2cSettings();        
            userSettings.setValue(Ili2cSettings.ILIDIRS, Ili2cSettings.DEFAULT_ILIDIRS);
            Configuration config = new Configuration();
            
            config.addFileEntry(new FileEntry(repository, FileEntryKind.ILIMODELFILE));
            config.setAutoCompleteModelList(true);

            boolean valid = new CheckReposIlis().checkRepoIlis(config, userSettings);

            EhiLogger.getInstance().removeListener(fileLogger);
            
            Check check = new Check(CheckType.MODELS, valid?false:true, logfile.getAbsolutePath(), new Date());
            checks.add(check);
        }
        
        Repository checkedRepository = new Repository(repository, checks);
        
        // Ilicache der ilisite- und ilimodels-Validierung löschen.
        ilicacheFolder.delete();
        
        // Ilicache der Modelle-Validierung:
        // ilicache-Verzeichnis ist mittels Umgebungsvariable steuerbar,
        // die nicht in Java gesetzt werden kann. Aus diesem Grund wird
        // das Standardverzeichnis verwendet und wird nach jedem Repo
        // gelöscht.
        File localCache=new File(System.getProperty("user.home"),".ilicache");
        localCache.delete();
        
        return checkedRepository;
    }
    
    private void copyResource(String resource, String targetDirectory) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        File file = Paths.get(targetDirectory, new File(resource).getName()).toFile();
        Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
