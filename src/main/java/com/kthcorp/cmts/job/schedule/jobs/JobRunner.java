package com.kthcorp.cmts.job.schedule.jobs;

import com.kthcorp.cmts.service.*;
import com.kthcorp.cmts.util.hadoop.WordCountJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by Ice on 11/4/2016.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class JobRunner implements Job {
    static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

    private String jobType;
    @Value("${property.serverid}")
    private String serverid;

    @Autowired private CollectService collectService;
    @Autowired private RefineService refineService;
    @Autowired private AnalyzeService analyzeService;
    @Autowired private ItemsService itemsService;
    @Autowired private SftpService sftpService;
    @Autowired private ApiService apiService;
    @Autowired private CcubeService ccubeService;
    @Autowired private TestService testService;
    @Autowired private SchedTriggerService schedTriggerService;
    //@Autowired
    //private MainService mainServiceImpl;
    //@Autowired
    //private CollectService collecService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //someService.writeDataToLog(jobType);
        //mainServiceImpl.runMainService();
        int rt = 0;
        try {
            switch (jobType) {
                case "collectService":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = collectService.ollehTvMetaCollectScheduleCheck();
                        logger.info("#MLOG schedule.start jobType:" + jobType + "/rt:" + rt);
                    } else {
                        logger.info("#MLOG schedule.not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "refineService":
                    //if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = refineService.ollehTvMetaRefineScheduleCheck();
                        logger.info("#MLOG schedule.start jobType:" + jobType + "/rt:" + rt);
                    //} else {
                    //    logger.info("#MLOG schedule.not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    //}
                    break;
                case "analyzeService":
                    //if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = analyzeService.ollehTvMetaAnalyzeScheduleCheck();
                        logger.info("#MLOG schedule.start jobType:" + jobType + "/rt:" + rt);
                    //} else {
                    //    logger.info("#MLOG schedule.not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    //}
                    break;
                case "itemsService":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = itemsService.checkInItems();
                        logger.info("#MLOG schedule.checkInItems.start jobType:"+jobType+"/rt:"+rt);
                    } else {
                        logger.info("#MLOG schedule.checkInItems not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "snsTopWords":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = apiService.processSnsTopKeywordsByDateSched();
                        logger.info("#MLOG schedule.snsTopKeywords.start jobType:" + jobType + "/rt:" + rt);
                    } else {
                        logger.info("#MLOG schedule.snsTopKeywords not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "ccubeOutputNow":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = ccubeService.processCcubeOutputToJson();
                        logger.info("#MLOG schedule.ccubeOutputNow.start jobType:" + jobType + "/rt:" + rt);
                    } else {
                        logger.info("#MLOG schedule.ccubeOutputNow not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "ccubeOutputDay":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        testService.writeCcubeOutputDayToJsonByType("CcubeContent");
                        testService.writeCcubeOutputDayToJsonByType("CcubeSeries");
                        logger.info("#MLOG schedule.ccubeOutputDay.start jobType:" + jobType);
                    } else {
                        logger.info("#MLOG schedule.ccubeOutputDay not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "ccubeOutputAll":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        testService.writeCcubeOutputToJsonByType("CcubeContent");
                        testService.writeCcubeOutputToJsonDevide("CcubeSeries",120);
                        logger.info("#MLOG schedule.ccubeOutputAll.start jobType:" + jobType);
                    } else {
                        logger.info("#MLOG schedule.ccubeOutputAll not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "dummyService":
                    logger.info("#MLOG schedule.check dummyService");
                    break;
                case "sftpService":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = sftpService.pollingCcubeSftp();
                        logger.info("#MLOG schedule.sftpService start jobType:" + jobType);
                    } else {
                        logger.info("#MLOG schedule.sftpService not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;

                case "prismUpService":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = sftpService.uploadToPrismReq();
                        logger.info("#MLOG schedule.prismUpService start jobType:" + jobType);
                    } else {
                        //rt = sftpService.uploadToPrismReq();
                        logger.info("#MLOG schedule.prismUpService not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "prismDownService":
                    if(schedTriggerService.checkActiveServerByServerid()) {
                        rt = sftpService.pollingPrismSftp();
                        logger.info("#MLOG schedule.prismDownService start jobType:" + jobType);
                    } else {
                        //rt = sftpService.pollingPrismSftp();
                        logger.info("#MLOG schedule.prismDownService not start jobType:" + jobType + ". because serverid:"+serverid+" is not Active.");
                    }
                    break;
                case "processCollectHearbit":
                    rt = schedTriggerService.processCollectHearbit();
                    logger.info("#MLOG schedule.check processCollectHearbit");
                    break;
                case "hadoopWordCount":
                    WordCountJob.main(null);
                    logger.info("#MLOG schedule.hadoopWordCount");
                    break;
                case "manualJobs":
                    //testService.processSearchTxtManualAppendFile();
                    //testService.processItemsTagsMetasByResultTag();
                    //testService.writeCcubeContentsOutputCSV();
                    //testService.writeCcubeOutputToJsonByType("CcubeContent");
                    //testService.writeCcubeOutputToJsonByType("CcubeSeries");
                    //testService.processRankForDicKeywordsAndGenres();
                    //testService.processSubgenre2ByKeywords();
                    //testService.processMixedSubgenre();
                    //testService.processMixedSubgenre2();
                    //testService.writeCcubeContentsOutputCSV();
                    //testService.writeCcubeSeriesOutputCSV();
                    //testService.loadDicResultTagKeywords();
                    //testService.processItemsTagsMetasByResultTag();
                    //testService.processSubgenreToTags();
                    //testService.processSubgenreToTagsSer();
                    //testService.insDicSubgenreKeywords();
                    testService.writeAllContentsByMcid();
                    logger.info("#MLOG schedule.manualJobs");
                    break;
            }
        } catch (Exception e) {
            logger.info("#MLOG schedule.error jobType:"+jobType+"/rt:"+rt);
            e.printStackTrace();
        }
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public CollectService getCollectService() {
        return collectService;
    }

    public void setCollectService(CollectService collectService) {
        this.collectService = collectService;
    }

    public RefineService getRefineService() {
        return refineService;
    }

    public void setRefineService(RefineService refineService) {
        this.refineService = refineService;
    }

    public AnalyzeService getAnalyzeService() {
        return analyzeService;
    }

    public void setAnalyzeService(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    public ItemsService getItemsService() {
        return itemsService;
    }

    public void setItemsService(ItemsService itemsService) {
        this.itemsService = itemsService;
    }
}
